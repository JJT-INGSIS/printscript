# Refactor del CLI — de parseo propio a librería

> Documento para revisión del equipo.
> Estado: **refactor completo** en la rama `refactor/cli`. Falta aprobación de las decisiones de la sección 8.

---

## 1. Por qué hacemos esto

Los profesores fueron explícitos: **no escribir un CLI a mano, usar librerías existentes y cosas reutilizables.**

Hoy el módulo `cli` implementa a mano cosas que son infraestructura resuelta hace veinte años:

| Lo que escribimos nosotros | Lo que hace una librería |
|---|---|
| `CliArgumentsParser` — 89 líneas leyendo `argv` por índice | `by option()` / `by argument()` |
| `CommandDispatcher` — buscar la operación por string | `.subcommands(...)` |
| Mensajes de error de uso a mano | Generados |
| `--help` | No existe hoy. Sale gratis |
| Validación de argumentos repetidos | Incluida |
| Autocompletado de shell | No existe hoy. Sale gratis |

No es que esté mal escrito. Es que es **código que no aporta valor al proyecto**: nadie va a evaluar PrintScript por la calidad de su parseo de `argv`, y cada línea ahí es una línea que hay que mantener y testear.

---

## 2. Qué librería, y por qué esa

**Elegimos [Clikt](https://github.com/ajalt/clikt) `5.1.0`** (enero 2026, Apache 2.0, Maven Central).

### Las tres candidatas

| Librería | Veredicto | Motivo |
|---|---|---|
| **Clikt** | ✅ elegida | Kotlin puro, *property delegates*, sin reflexión, multiplataforma, activa |
| `kotlinx-cli` | ❌ descartada | **Marcada como obsoleta** por JetBrains en su propio repo, y su API sigue siendo experimental |
| `picocli` | ❌ descartada | Es Java. Usa anotaciones + reflexión, lo que obliga a procesamiento de anotaciones en el build y no juega bien con `explicitApi()` |

La sorpresa fue `kotlinx-cli`: suena a la opción obvia por ser de JetBrains, pero está deprecada. Meter una dependencia abandonada en un proyecto que dura todo el semestre es empezar con deuda.

### El modelo de Clikt encaja con lo que ya tenemos

Nuestras cuatro operaciones (`validation`, `execution`, `formatting`, `analyzing`) son literalmente subcomandos. Clikt los modela nativo:

```
$ printscript validation ejemplo.ps
$ printscript execution ejemplo.ps --version 1.0
$ printscript formatting ejemplo.ps --config formato.json
```

---

## 3. La idea de fondo

**Es el mismo movimiento que ustedes ya hicieron con `lexer`, `parser`, `interpreter` y `formatter.**

Ustedes separaron *motor genérico* de *configuración V1*. Acá separamos:

- **Dominio** — "corré la validación sobre estas sentencias y decime cómo salió". No sabe que existe `argv`, ni los flags, ni la terminal.
- **Adaptador** — sabe de `argv`, `--help`, códigos de salida y consola. Es todo Clikt.

Eso es **Ports & Adapters (hexagonal)**, y es lo que los profesores están pidiendo cuando dicen "no te compliques": el parseo de argumentos es infraestructura, no dominio.

**Dato importante: el diseño ya estaba al 80%.** `CliCommand` ya era un puerto, `CommandOutcome` ya era el contrato de retorno, `StatementSourcePipeline` ya aislaba el pipeline. Que la abstracción haya aguantado este cambio es la prueba de que estaba bien puesta.

---

## 4. Fase 1 — TERMINADA ✅

**Sin Clikt. Sin dependencias nuevas.** Puro renombre y mover orquestación, para que el dominio quede testeable sin librería.

### Lo que se creó

```
cli/internal/operation/
├── SourceOperation.kt            ← el puerto
├── OperationOutcome.kt           ← ex CommandOutcome
├── SourceOperationRequest.kt     ← ex CliArguments (sin operationName)
├── SourceOperationRegistry.kt    ← ex CommandDispatcher
├── LanguageVersion.kt            ← mudado desde arguments/
├── ValidationOperation.kt
├── ExecutionOperation.kt
├── FormattingOperation.kt
└── AnalysisOperation.kt

cli/internal/
└── SourceOperationRunner.kt      ← la orquestación, sin argv
```

### Lo que se borró

```
cli/internal/command/             ← la carpeta entera (7 archivos)
cli/internal/arguments/CliArguments.kt
cli/internal/arguments/LanguageVersion.kt
```

### Las tres decisiones de diseño

**① `operationName` sale de la operación.**

Antes:

```kotlin
internal interface CliCommand {
    val operationName: String                    // ← se va
    fun runOperation(arguments, statements, terminal): CommandOutcome
}
```

Ese campo existía solo para que `CommandDispatcher` pudiera buscar por string. El nombre `"validation"` es **vocabulario de la línea de comandos**, no una propiedad de la operación: la misma lógica podría exponerse con otro nombre sin cambiar nada. Ahora el nombre vive en el registry, que se arma en `Main.kt`.

**② El parámetro muerto desaparece.**

Las cuatro operaciones recibían `arguments: CliArguments` y **ninguna lo leía**. La configuración ya la reciben por constructor. Así que el puerto quedó en dos parámetros:

```kotlin
internal interface SourceOperation {
    fun outcomeFor(statements: StatementSource, terminal: Terminal): OperationOutcome
}
```

**③ Los nombres dicen en qué capa vive cada cosa.**

| Capa | Clase | Sabe de |
|---|---|---|
| Adaptador (futuro) | `ValidationCommand : CliktCommand` | `argv`, `--help`, el string `"validation"` |
| Dominio | `ValidationOperation : SourceOperation` | sentencias, intérprete, resultado |

Además evita la colisión con `CliktCommand`: si nuestras clases siguieran llamándose `Command`, habría dos cosas con el mismo nombre y distinto significado en el mismo archivo.

### La prueba de que salió bien

**No se tocó ni una aserción de test.** Los 14 tests de `CliApplicationTest` y los 9 de `FormattingAndAnalysisTest` quedaron idénticos — solo cambió el bloque de construcción. Si el comportamiento hubiera cambiado, algo se habría roto.

### El resultado que importa

`SourceOperationRunner` y las cuatro operaciones **no mencionan `argv` en ninguna línea**. La Fase 3 va a poder reemplazar `CliApplication` + `CliArgumentsParser` + `SourceOperationRegistry` por Clikt sin tocar esas cinco clases.

---

## 5. Fases 2 a 7 — pendientes

Cada fase **compila y pasa `./gradlew check`**. Se puede parar en cualquiera sin dejar `main` roto.

| Fase | Qué hace | Riesgo |
|---|---|---|
| **2** | Agregar Clikt a `cli/build.gradle.kts`. Verificar que no se filtra a otros módulos | Bajo |
| **3** | `PrintScriptCommand` (raíz) + clase abstracta con el argumento del archivo, `--version` y `--config` compartidos. **Template Method** — el mismo patrón que `ArithmeticOperation` | Medio |
| **4** | Los cuatro subcomandos, ~6 líneas cada uno | Bajo |
| **5** | `CliktTerminal` implementa nuestro `Terminal` delegando en `echo()`. 3 líneas. Muere `ConsoleTerminal` | Bajo |
| **6** | Borrar `CliArgumentsParser`, `CliArguments`, `ArgumentsParsingResult`, `SourceOperationRegistry`, `CliApplication` y sus tests | Bajo |
| **7** | `ktlintFormat`, `check`, coverage ≥80%, probar las 4 operaciones a mano, actualizar README | — |

**Alcance acordado:** argumentos **y** salida por terminal. **Fuera de alcance a propósito:** la barra de progreso de Mordant (toca el decorator lazy, es el pedazo con más riesgo, y no lo quiero mezclar).

---

## 6. Lo bueno

- **Menos código nuestro que mantener.** Se van ~5 clases y sus tests.
- **`--help` gratis**, por comando y global. Hoy no existe.
- **Autocompletado de bash/zsh gratis.** Queda bien en la demo.
- **Validación de argumentos incluida** (repetidos, faltantes, tipos).
- **`--version` como enum**: Clikt lista los valores válidos en el `--help` y valida solo. Desaparece `LanguageVersion.fromLabel` devolviendo `null`.
- **Desbloquea el `--config`** (ver sección 9, punto ①).
- **El dominio queda testeable sin librería.** Los tests del dominio no importan Clikt.
- **Es lo que pidieron los profesores.**

---

## 7. Lo malo — y hay que decirlo

### ⚠️ Clikt señaliza con excepciones, y nuestra regla es "todo Results"

`CliktError` extiende `RuntimeException`. Sus hijos son `ProgramResult(statusCode)`, `UsageError`, `PrintMessage`, `PrintHelpMessage`. **Es su protocolo y no es negociable.**

**Cómo lo resolvemos:** el dominio nunca lanza ni atrapa nada. `SourceOperation` sigue devolviendo `OperationOutcome`, `SourceOperationRunner` sigue devolviendo `ExitCode`. Hay **exactamente un `throw`** en todo el CLI, en la última línea de la clase base de los subcomandos, traduciendo `ExitCode` al protocolo de Clikt, con un comentario que diga que eso es protocolo de librería y no manejo de errores.

Un `throw` en un archivo, en el borde. Adentro, Results.

> Hay una alternativa (usar `parse()` en vez de `main()`) que evita hasta ese `throw`, pero obliga a reimplementar el manejo de `--help`, que es justo lo que veníamos a no hacer. **Si al equipo le molesta el `throw`, se puede discutir.**

### ⚠️ Es la primera dependencia externa de producción del proyecto

Hasta hoy los módulos motor no dependen de nada de terceros. Eso es parte del valor del diseño.

**Propuesta:** Clikt va `implementation` y **solo en `:cli`**. Los módulos `lexer`, `parser`, `formatter`, `interpreter`, `linter` y los de contrato siguen limpios. En la Fase 2 se verifica explícitamente que no se filtró.

### ⚠️ Clikt arrastra Mordant — verificado

Mordant es su librería de terminal (colores, detección de TTY). Viene como dependencia transitiva, junto con Colormath y tres backends de JVM (`jna`, `ffm`, `graal-ffi`) que Mordant usa para detectar el tipo de terminal. Son unos 8 artefactos.

**Ya lo verificamos en la Fase 2:**

```
./gradlew :cli:dependencies --configuration runtimeClasspath
\--- com.github.ajalt.clikt:clikt:5.1.0
     \--- com.github.ajalt.clikt:clikt-jvm:5.1.0
          +--- com.github.ajalt.clikt:clikt-core:5.1.0
          +--- com.github.ajalt.mordant:mordant:3.0.2
               \--- com.github.ajalt.colormath:colormath:3.6.0
               +--- mordant-jvm-jna / mordant-jvm-ffm / mordant-jvm-graal-ffi
```

Y el chequeo de que **no se filtró a ningún módulo motor** da limpio:

```bash
./gradlew :common:dependencies :source-reader:dependencies :token-source:dependencies \
          :statement-source:dependencies :lexer:dependencies :parser:dependencies \
          :formatter:dependencies :interpreter:dependencies :linter:dependencies \
          :printscript-v1:dependencies --configuration runtimeClasspath \
  | grep -i -E "clikt|mordant" \
  || echo "OK — ningún módulo motor ve Clikt"
```

No nos molesta porque `cli` es una aplicación, no una librería que alguien más vaya a consumir. Pero conviene tenerlo escrito.

### ⚠️ Cobertura JaCoCo

El código de adaptador es más difícil de cubrir que el de dominio. El corte dominio/adaptador ayuda justamente acá — el dominio, que es la mayoría, se testea sin librería. Clikt además trae un helper `test()` que captura la salida de `echo`.

### ⚠️ Perdemos control fino si aceptamos todo lo que ofrece

Ver punto ⑤ de la sección siguiente: hay al menos una cosa de Clikt que conviene **rechazar** a propósito.

---

## 8. Decisiones que hay que tomar entre los tres

### ① Aprobar la dependencia

Es lo primero, antes de escribir una línea de la Fase 2. ¿Están de acuerdo con meter Clikt, `implementation`, solo en `:cli`?

### ② `analyzing` o `analysis`

Hoy la operación se llama `AnalysisCommand` pero se invoca `analyzing`. Las otras tres son consistentes: `validation`/`Validation`, `execution`/`Execution`, `formatting`/`Formatting`.

Propongo unificar en **`analysis`** en la Fase 4. **Rompe el uso actual del CLI y los tests** — por eso lo pregunto en vez de hacerlo.

### ③ ¿La consigna fija la sintaxis de invocación?

Hoy es posicional: `printscript <operación> <archivo> [versión] [configuración]`.
Con subcomandos sería: `printscript <operación> <archivo> [--version 1.0] [--config x.json]`.

**No sé si el enunciado exige un formato exacto.** Si alguien tiene el PDF a mano, que confirme.

Mientras tanto, el plan aísla la sintaxis: los nombres de subcomandos, los flags y el orden viven **solo** en las cuatro clases `*Command` y en la clase base. Cambiarla después cuesta esos archivos y nada más.

### ④ Los códigos de salida — resuelto, conservamos tres de cuatro

Teníamos cuatro propios: `SUCCESS(0)`, `SOURCE_ERROR(1)`, `USAGE_ERROR(2)`, `FINDINGS(3)`.

**`USAGE_ERROR(2)` se perdió.** Clikt reporta los errores de uso él mismo, y su `UsageError` sale con `statusCode = 1` por defecto. Desde que se borró `CliApplication`, nadie produce el 2 — quedó como constante muerta y la sacamos del enum.

Los otros tres se conservan. `FINDINGS(3)` era el importante: distingue "el linter encontró problemas" de "el archivo está roto", y eso es información real que un CI puede usar.

Es el ejemplo más concreto de lo que dice la sección 7: **heredás el vocabulario de la librería**. Si el equipo quiere recuperar el 2, hay que atrapar `UsageError` y relanzarlo con otro código — cuesta otra excepción y otra clase. Mi recomendación es no hacerlo.

### ⑤ La validación del archivo — propongo rechazar lo que ofrece Clikt

Clikt tiene `path(mustExist = true, mustBeReadable = true)`, que valida antes de que corra nuestro código.

**Propongo NO usarlo.** Duplicaría los chequeos de `SourceReaderFactory.fromPath` y nos haría perder nuestros mensajes en castellano (`"no se encontró el archivo 'x.ps'"`) a cambio de los genéricos en inglés de la librería.

Es un buen punto para la defensa oral: **usar una librería no es aceptar todo lo que ofrece.**

### ⑥ El lector de JSON — la segunda dependencia

Necesario para que `--config` funcione de verdad (ver sección 9). Candidato: `kotlinx.serialization`. **Es otra conversación, no la mezclo con esta.**

---

## 9. Deuda que este refactor NO resuelve

Cosas que encontramos en el camino y conviene que estén escritas.

### ① `--config` es código muerto, y hay una razón estructural

Seguí el camino:

```
CliArgumentsParser lee "reglas.json"
        ↓
SourceOperationRequest.configurationFilePath = "reglas.json"
        ↓
SourceOperationRunner recibe el request
        ↓
        ✗ nadie lo lee
```

Y del otro lado, `FormattingOperation` usa `PrintScriptV1FormatterFactory.defaultConfiguration()`.

**Por qué no se puede arreglar hoy:** las cuatro operaciones se construyen en `Main.kt`, al arrancar el programa, **antes** de leer `argv`. Es imposible inyectarles por constructor algo que todavía no existe.

**Clikt lo resuelve solo:** con subcomandos, `FormattingCommand.run()` ya tiene el `--config` en la mano, así que arma su `FormattingOperation` ahí, con la configuración cargada.

O sea: *"config hardcodeado"* y *"parseo a mano"* son el mismo problema. Las operaciones se construyen demasiado temprano.

Falta igual: el lector JSON (punto ⑥), un tipo de error para JSON mal formado (un `Result`, no una excepción), y lo mismo para `LinterConfiguration`.

### ② El `ErrorReporter` tiene cuatro ramas `else ->`

Desde que `TokenType`, `LexicalError`, `ParseError`, `FormattingError` y `Statement` pasaron de `sealed interface` a `interface` —cosa necesaria para que `printscript-v1` aporte sus implementaciones—, **el compilador dejó de avisarnos** cuando aparece un caso nuevo.

Hoy `ErrorReporter` tiene cuatro salidas silenciosas: `"error sintáctico desconocido"`, `"error semántico desconocido"`, `"error de formateo desconocido"`, `"error léxico desconocido"`.

**Propuesta:** un test por jerarquía que recorra los casos de V1 y verifique que ninguno cae en el `else`. Es la red que reemplaza al compilador.

### ③ Las operaciones están acopladas a V1, el pipeline no

`StatementSourcePipeline` es consciente de la versión:

```kotlin
when (version) { LanguageVersion.V1_0 -> v1StatementsFrom(sourceReader) }
```

Pero `ValidationOperation` y `ExecutionOperation` instancian `PrintScriptV1InterpreterFactory` a mano. Cuando llegue V2, el pipeline sabrá cambiar de lexer y parser, y las operaciones no.

**Idea para más adelante:** que el pipeline crezca a algo tipo `LanguageToolchain`, que entregue las cuatro herramientas de una versión —parser, intérprete, formatter, linter— en vez de solo el `StatementSource`. Es el mismo movimiento que ya hicieron ustedes, un nivel más arriba.

### ④ `formatter/build.gradle.kts` no declara `common`

`FormattingError` tiene `public val span: SourceSpan`, que es un tipo de `common`. Pero el módulo no lo declara: compila de rebote porque `api(":statement-source")` a su vez hace `api(":common")`.

Funciona, pero si algún día `statement-source` cambia esa línea a `implementation`, el formatter deja de compilar sin haber tocado su código.

**Arreglo:** agregar `api(project(":common"))`. Queda igual que `linter`, que ya lo tiene bien.

### ⑤ Los diagramas `.puml` — resuelto

Los diagramas de `docs/diagrams/` estaban muy bien hechos pero les faltaban cosas. Los actualicé los dos:

- Se agregó **`cli`** como paquete propio, con sus flechas `implementation` y una nota explicando que Clikt queda contenido ahí.
- Se agregó **`formatter`**, que no aparecía en ninguno.
- Se corrigieron `PrintScriptLexerFactory.createV1()` y `PrintScriptParserFactory.createV1()`, **que están borradas** desde el refactor de motores, por `LexerFactory` / `ParserFactory` y las factories de `printscript-v1`.
- Se agregó `SourceReaderFactory.fromPath` y `SourceAccessError`.
- En `estructura.puml` se agregó la relación *"V1 configura cada motor"*, que es lo que explica el patrón de todo el proyecto.

**Revísenlos** — toqué archivos que escribió otro, y puede que haya interpretado mal alguna intención del diseño original.

---

## 10. Lo que rescatamos de los refactors de ustedes

Tres cosas que vale la pena que sigamos haciendo entre todos:

1. **Testear por la costura pública.** `StatementExecutorDispatcherTest` (261 líneas contra una clase `internal`) fue reemplazado por `InterpreterFactoryTest` (241 líneas contra la factory). Misma cobertura, pero el test no se rompe cuando cambia un detalle interno. Nuestros tests del CLI van por `CliApplication`, así que estamos alineados.

2. **El idiom `additional* + defaults`.** En `PrintScriptV1InterpreterFactory` y `PrintScriptV1FormatterFactory`, los componentes que aporta quien llama van **antes** que los de V1. Como gana el primero que soporta, "extender" y "pisar el comportamiento" quedan en la misma operación. Una línea, dos capacidades.

3. **La convención `Configurable*` + `*Factory`.** Los cuatro motores se leen igual. El único que rompe el patrón es `ScanningLexer`, pero ahí el nombre dice algo real sobre cómo funciona, así que lo dejaría.

---

## 11. Preguntas concretas para ustedes

1. ¿Aprueban Clikt como dependencia, `implementation` y solo en `:cli`?
2. ¿Alguien tiene el enunciado a mano para confirmar si fija la sintaxis de invocación?
3. ¿Renombramos `analyzing` a `analysis`, aunque rompa el uso actual?
4. ¿Les molesta el único `throw` en el borde, o prefieren que investigue la alternativa sin excepciones?
5. ¿De acuerdo con mantener nuestros cuatro códigos de salida en vez de los de Clikt?
6. ¿Cuándo abrimos la conversación del lector de JSON?
