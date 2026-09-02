# Plan — Simplificación del CLI

> Estado del repo al escribir esto: **32 archivos de producción** en `cli`.
> Objetivo: **~18**, sin perder testabilidad ni ensuciar los módulos core.
>
> Incorpora las correcciones de la revisión: el toolchain se inyecta como
> función y no como instancia, la interpretación compartida es una función y
> no una jerarquía, y `ExitCode` queda en su archivo.

---

## Por qué

El CLI construyó una arquitectura hexagonal completa **adentro de una capa que
ya es el borde del sistema**:

```
CliktCommand
  → SourceOperationFactory
    → SourceOperation
      → SourceOperationRunner
        → OperationOutcome
          → Terminal
            → EchoTerminal
```

Todas esas clases son `internal`, viven en `cli` y terminan llamando a Clikt.
La flexibilidad que dan no tiene consumidores reales.

Cuando se diseñó, el CLI **parseaba sus propios argumentos** y el puerto
`Terminal` era lo único que permitía testear el dominio sin `argv`. Desde que
existe Clikt con su `test()`, esa justificación desapareció.

**Dos fronteras que sí se conservan:**

- Clikt vive **solo** dentro de `cli`
- `ProgramOutput` y los módulos core **no conocen Clikt**

---

## Las cuatro decisiones de diseño

### 1. El toolchain se inyecta como función, no como instancia

La versión se conoce **después** de que Clikt parsea:

```
printscript execution archivo.ps --version 1.1
                                     ↓
                          recién acá sabemos qué toolchain
```

Por eso el comando no puede recibir un `PrintScriptToolchain` ya armado:

```kotlin
internal class ExecutionCommand(
    private val errorReporter: ErrorReporter,
    private val toolchainFor: (LanguageVersion) -> PrintScriptToolchain =
        PrintScriptToolchainFactory::forVersion,
) : CliktCommand(name = "execution")
```

Y eso **también es el punto de sustitución de los tests**: se inyecta otra
función, no se mockea un tipo.

### 2. `PrintScriptToolchain` es una clase concreta, no una interfaz

No se crea una interfaz solo para poder mockear. La variación real está en
`toolchainFor(version)`, no en el tipo.

Si algún día hay comportamiento propio por versión —no solo factories
distintas— ahí sí se justifica una interfaz, por Strategy y no por testing.

### 3. La interpretación compartida es una función

Validar y ejecutar comparten ~25 líneas: crear el intérprete, correrlo y mapear
los tres resultados. Eso **no vuelve a ser una jerarquía**.

Como función suelta queda deduplicado y, sobre todo, **fácil de borrar** el día
que validación deje de ejecutar el programa (ver [decisión pendiente](#decisión-pendiente)).

### 4. `ExitCode` se queda en su archivo

Son tres constantes, pero es el contrato del CLI con el sistema operativo y
está documentado en el README con su tabla. Enterrarlo no ahorra nada real.

---

## Estructura destino

```
Main.kt
internal/
  PrintScriptCommandFactory.kt
  ExitCode.kt
  OperationOutcome.kt
  command/
    PrintScriptCommandGroup.kt
    ValidationCommand.kt
    ExecutionCommand.kt
    FormattingCommand.kt
    AnalysisCommand.kt
    CommandInputs.kt            ← LanguageOptions + sourceFileArgument()
    RunSourceCommand.kt         ← flujo compartido + interpretación compartida
  toolchain/
    PrintScriptToolchain.kt
    PrintScriptToolchainFactory.kt
    LanguageVersion.kt
  report/
    ErrorReporter.kt
    DiagnosticReporter.kt
    PrintScriptWording.kt
    SpanRenderer.kt
```

**18 archivos contra 32.**

> `OperationOutcome` va en archivo propio y no adentro de `RunSourceCommand.kt`
> por una razón concreta: la regla `MatchingDeclarationName` de detekt exige que
> un archivo con una sola clase de nivel superior se llame igual que ella. Si se
> mete ahí, el build falla.

---

## Las piezas nuevas

### `PrintScriptToolchain`

Una clase inmutable que agrupa las cuatro herramientas de una versión:

```kotlin
internal class PrintScriptToolchain(
    val statementsFrom: (SourceReader) -> StatementSource,
    val interpreterWriting: (ProgramOutput) -> Interpreter,
    val formatter: () -> Formatter,
    val linter: () -> Linter,
)
```

Las configuraciones concretas de cada versión quedan **capturadas adentro de las
lambdas**, así que el tipo no nombra nada de V1.

> **Pensado para el `--config` que viene.** Cuando exista, `formatter` y `linter`
> pasan a recibir la ruta del archivo de configuración —`(Path?) -> Formatter`—
> y cada versión lo interpreta a su manera. La forma no hay que rehacerla.

### `PrintScriptToolchainFactory`

```kotlin
internal object PrintScriptToolchainFactory {

    fun forVersion(version: LanguageVersion): PrintScriptToolchain {
        return when (version) {
            LanguageVersion.V1_0 -> printScriptV1_0()
        }
    }
}
```

El `when` es exhaustivo. **Cuando se agregue `V1_1` al enum, este archivo deja de
compilar** — que es exactamente lo que queremos.

Acá se concentra lo que hoy está partido en cinco lugares: `StatementSourcePipeline`
elige lexer y parser, y cada operación instancia su herramienta a mano.

### `RunSourceCommand.kt`

El flujo compartido por los cuatro comandos:

```kotlin
internal fun CliktCommand.runOnSourceFile(
    sourceFilePath: Path,
    toolchain: PrintScriptToolchain,
    errorReporter: ErrorReporter,
    outcomeFrom: (StatementSource) -> OperationOutcome,
) {
    val outcome = when (val creation = SourceReaderFactory.fromPath(sourceFilePath)) {
        is SourceReaderCreationResult.Failure ->
            OperationOutcome.Failure(errorReporter.describe(creation.error))

        is SourceReaderCreationResult.Success ->
            outcomeFrom(toolchain.statementsFrom(creation.reader))
    }

    reportOutcome(outcome)

    val exitCode = exitCodeOf(outcome)

    if (exitCode != ExitCode.SUCCESS) {
        throw ProgramResult(exitCode.value)
    }
}
```

Y en el mismo archivo, la interpretación compartida —función, no clase—:

```kotlin
internal fun interpretationOutcome(
    interpreter: Interpreter,
    statements: StatementSource,
    errorReporter: ErrorReporter,
    onSuccess: () -> Unit = { },
): OperationOutcome
```

Más dos privadas: `reportOutcome` (usa `echo`) y `exitCodeOf` (pura).

### Los adaptadores de salida, en línea

```kotlin
// ExecutionCommand
val output = object : ProgramOutput {
    override fun writeLine(line: String) = echo(line)
}

// ValidationCommand
val discarded = object : ProgramOutput {
    override fun writeLine(line: String) = Unit
}
```

Se van `TerminalProgramOutput` y `DiscardedProgramOutput` como archivos.

---

## Los pasos

Cada paso compila y pasa `./gradlew :cli:check`.

### Paso 1 — El toolchain

**Nuevos:** `toolchain/PrintScriptToolchain.kt`, `toolchain/PrintScriptToolchainFactory.kt`
**Mueve:** `operation/LanguageVersion.kt` → `toolchain/`
**Todavía no borra nada.**

Es el paso que hace triviales a los siguientes: con el toolchain adentro,
`StatementSourcePipeline` y `SourceOperationFactory` se quedan sin trabajo.

### Paso 2 — Los cuatro comandos absorben su operación

De a uno, en este orden:

| Comando | Qué absorbe | Queda en |
|---|---|---|
| `ExecutionCommand` | `ExecutionOperation` + `InterpretingOperation` | ~45 líneas |
| `ValidationCommand` | `ValidationOperation` | ~45 líneas |
| `FormattingCommand` | `FormattingOperation` y su `tailrec` | ~70 líneas |
| `AnalysisCommand` | `AnalysisOperation`, su `tailrec` y el resumen | ~85 líneas |

Los dos primeros comparten `interpretationOutcome(...)`, que se crea acá.

Cada comando pasa a recibir `toolchainFor` y deja de recibir `operationFactory`.

**Nuevos:** `OperationOutcome.kt` (movido), `RunSourceCommand.kt` (reemplaza a `RunSourceOperation.kt`)

### Paso 3 — Colapsar los envoltorios de terminal

**Borra:** `io/Terminal.kt`, `command/EchoTerminal.kt`, `command/ProgramTermination.kt`,
`io/TerminalProgramOutput.kt`, `io/DiscardedProgramOutput.kt`

Cada comando usa `echo()`, `echo(err = true)` y `throw ProgramResult(...)` directo.
Los adaptadores de `ProgramOutput` quedan como objetos anónimos.

### Paso 4 — Unificar las entradas de comando

**Nuevo:** `command/CommandInputs.kt` con `LanguageOptions` y `sourceFileArgument()`
**Borra:** `command/LanguageOptions.kt`, `command/SourceFileArgument.kt`

> Ojo con `MatchingDeclarationName`: `CommandInputs.kt` va a tener la clase
> `LanguageOptions` como única clase de nivel superior y detekt va a pedir que el
> archivo se llame igual. Si salta, la salida es dejar los dos archivos separados
> como están hoy — es un archivo más y no vale pelear con la regla.

### Paso 5 — Borrar lo que quedó huérfano

**Recordá: los tests primero, después el código.**

```
operation/                          ← la carpeta entera
pipeline/StatementSourcePipeline.kt
SourceOperationRunner.kt
```

### Paso 6 — Tests

**`ExecutionCommandTest`** cambia de costura: hoy inyecta una
`SourceOperationFactory` falsa, pasa a inyectar un `toolchainFor` falso que
devuelve un toolchain con un intérprete de prueba.

Los ocho casos siguen valiendo. Se agrega uno: **que el toolchain se pida con la
versión que llegó por `--version`**.

**`PrintScriptCliTest`** no debería necesitar cambios: va por
`PrintScriptCommandFactory.create()` y prueba comportamiento observable.

### Paso 7 — Verificación

```bash
./gradlew ktlintFormat
./gradlew :cli:check
./gradlew :cli:clean :cli:installDist

./cli/build/install/printscript/bin/printscript --help
./cli/build/install/printscript/bin/printscript analysis ejemplo.ps ; echo $?
```

Y actualizar `docs/guia-del-proyecto.md`, cuyo capítulo 7 describe la estructura
que este refactor elimina.

---

## Decisión pendiente

**¿Validar debe ejecutar el programa?**

Hoy validar es interpretar descartando la salida. Con `readInput` y `readEnv` de
1.1, eso significa que `printscript validation programa.ps` **se cuelga esperando
entrada**.

La recomendación del equipo es que **no ejecute**: que sea chequeo estático de
tipos. Pero eso es trabajo en el módulo `interpreter`, no en el CLI, y cruza de
dueño.

Si no llega a tiempo, hay dos caminos intermedios:

| Opción | Qué pasa con `validation` sobre un programa con `readInput` |
|---|---|
| `ProgramInput` que falla al primer intento | *"no se puede validar un programa que lee entrada"* — honesto pero limitado |
| `ProgramInput` que devuelve un valor válido del tipo esperado | valida los tipos sin colgarse, pero con datos inventados |

**Por eso la interpretación compartida es una función y no una clase:** cuando se
resuelva esto, se borra una función y no hay que desarmar una jerarquía.

---

## Lo que este refactor **no** hace

- No agrega `V1_1` al enum `LanguageVersion` — eso rompería los `when`
  exhaustivos y obligaría a implementar 1.1 para que compile
- No crea `ProgramInput`, `BooleanValue` ni nodos nuevos del AST
- No arma el wording del error de "esa funcionalidad no existe en esta versión",
  que necesita dos versiones para poder testearse
- No toca ningún módulo fuera de `cli`
