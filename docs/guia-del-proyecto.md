# Guía del proyecto PrintScript

> Escrita para que alguien que no vio nunca el código pueda entenderlo entero.
> Los términos técnicos están marcados así: **término**. Todos están explicados
> en el [glosario](#8-glosario) al final.

---

## Índice

1. [Qué es PrintScript](#1-qué-es-printscript)
2. [La idea central: el pipeline perezoso](#2-la-idea-central-el-pipeline-perezoso)
3. [Mapa de módulos](#3-mapa-de-módulos)
4. [Los módulos, uno por uno](#4-los-módulos-uno-por-uno)
5. [Qué es printscript-v1](#5-qué-es-printscript-v1)
6. [El interpreter, clase por clase](#6-el-interpreter-clase-por-clase)
7. [El CLI, clase por clase](#7-el-cli-clase-por-clase)
8. [Glosario](#8-glosario)

---

# 1. Qué es PrintScript

PrintScript es un **lenguaje de programación** chiquito, un subconjunto de
TypeScript. Un programa se ve así:

```typescript
let nombre: string = "mundo";
let cuenta: number = 2 + 3 * 4;
println("hola " + nombre);
println(cuenta);
```

Lo que construyeron **no es el lenguaje: son las herramientas que lo procesan.**
Cuatro operaciones distintas sobre un mismo archivo `.ps`:

| Operación | Qué hace |
|---|---|
| `validation` | Dice si el archivo es válido, sin correrlo |
| `execution` | Lo ejecuta y muestra lo que imprime |
| `formatting` | Lo reescribe con espaciado y saltos consistentes |
| `analysis` | Reporta problemas de estilo, sin modificar nada |

Las cuatro comparten la mayor parte del trabajo: leer el archivo, partirlo en
piezas y entender su estructura. Recién al final se separan.

---

# 2. La idea central: el pipeline perezoso

Todo el diseño del proyecto sale de una decisión: **procesar el archivo de a un
pedacito, sin cargarlo entero en memoria.**

## Las etapas

```
archivo.ps
    │
    ▼
SourceReader        entrega el código en bloques de caracteres
    │
    ▼
Lexer               agrupa caracteres en tokens
    │
    ▼
TokenSource         entrega un token por vez
    │
    ▼
Parser              agrupa tokens en sentencias
    │
    ▼
StatementSource     entrega una sentencia por vez
    │
    ├──▶ Interpreter    la ejecuta
    ├──▶ Formatter      la reescribe bonita
    └──▶ Linter         la revisa
```

Un ejemplo de las dos primeras traducciones. Con este código:

```typescript
let a: number = 5;
```

El **lexer** produce estos **tokens**:

```
LET("let")  IDENTIFIER("a")  COLON(":")  NUMBER_TYPE("number")
ASSIGN("=")  NUMBER_LITERAL("5")  SEMICOLON(";")
```

Y el **parser** los agrupa en un **AST** — un árbol que representa el
significado:

```
VariableDeclarationStatement
├── identifier:   Identifier("a")
├── declaredType: NUMBER
└── initializer:  NumberLiteralExpression(5)
```

## Qué significa "perezoso" y "pull"

**Perezoso** (*lazy*) significa que nada se calcula hasta que alguien lo pide.
**Pull** significa que la etapa de abajo *tira* de la de arriba, en vez de que
la de arriba *empuje* hacia abajo.

En un diseño normal harías:

```kotlin
val tokens: List<Token> = lexer.tokenize(codigo)      // todos los tokens en memoria
val sentencias: List<Statement> = parser.parse(tokens) // todas las sentencias también
```

Con un archivo de 500 MB, eso explota.

Acá en cambio el intérprete pide **una** sentencia; para armarla, el parser pide
los **tokens que necesite**; para armarlos, el lexer pide los **caracteres que
necesite**. Cuando esa sentencia se ejecutó, se descarta y se pide la siguiente.
**La memoria usada no depende del tamaño del archivo.**

## Cómo se logra sin variables mutables

Un lector que avanza normalmente guardaría una posición y la iría cambiando. Acá
no: **cada lectura exitosa devuelve también la fuente que representa el resto.**

```kotlin
public sealed interface StatementReadResult {
    data object EndOfInput : StatementReadResult
    data class Failure(val error: ParseError) : StatementReadResult
    data class Success(
        val statement: Statement,
        val remainingSource: StatementSource,   // ← el resto, como objeto nuevo
    ) : StatementReadResult
}
```

Nadie muta nada. Cada paso produce un objeto nuevo que representa "de acá en
adelante". Eso hace que todo el pipeline sea **inmutable** y que se pueda leer
el mismo `StatementSource` dos veces sin sorpresas.

## Errores como valores, no como excepciones

**En todo el proyecto no se lanza una sola excepción para manejar errores.**
Fijate que `StatementReadResult` tiene un caso `Failure`: un error de sintaxis
no es algo excepcional que interrumpe, es uno de los tres resultados posibles de
"dame la próxima sentencia".

Esto se repite en cada capa:

| Capa | Tipo de resultado | Casos |
|---|---|---|
| Abrir el archivo | `SourceReaderCreationResult` | Success / Failure |
| Leer tokens | `TokenReadResult` | Success / Failure / EndOfInput |
| Leer sentencias | `StatementReadResult` | Success / Failure / EndOfInput |
| Ejecutar una sentencia | `ExecutionResult<S>` | Success / Failure |
| Interpretar un programa | `InterpretationResult` | Success / ParseFailure / SemanticFailure |
| Una operación del CLI | `OperationOutcome` | Success / CompletedWithFindings / Failure |

**Por qué importa:** el compilador te obliga a mirar el caso de error. Con
excepciones te podés olvidar de atrapar una y el programa se cae; acá no podés
seguir sin decidir qué hacer con el `Failure`.

Hay **una sola** excepción en todo el proyecto, y no es manejo de errores: es
`ProgramTermination`, que le comunica a la librería del CLI con qué código
tiene que terminar el proceso. Está explicada en la [sección 7](#7-el-cli-clase-por-clase).

---

# 3. Mapa de módulos

El proyecto tiene 13 módulos de Gradle. Se dividen en **cuatro familias**:

```
┌─ APLICACIÓN ────────────────────────────────────────┐
│  cli                                                │
└─────────────────────────────────────────────────────┘
                         │ usa
                         ▼
┌─ IMPLEMENTACIÓN DE LENGUAJE ────────────────────────┐
│  printscript-v1                                     │
│  (las reglas concretas de PrintScript 1.0)          │
└─────────────────────────────────────────────────────┘
                         │ configura
                         ▼
┌─ MOTORES GENÉRICOS ─────────────────────────────────┐
│  lexer   parser   interpreter   formatter   linter  │
│  (no saben nada de PrintScript)                     │
└─────────────────────────────────────────────────────┘
                         │ hablan a través de
                         ▼
┌─ CONTRATOS Y DATOS ─────────────────────────────────┐
│  common  source-reader  token-source                │
│  statement-source  printscript-ast                  │
└─────────────────────────────────────────────────────┘
```

## La regla de oro

> **Las etapas del pipeline no se conocen entre sí.**

`lexer` no depende de `parser`. `parser` no depende de `interpreter`. Se
comunican **solo** a través de módulos de contrato:

```
lexer  ──produce──▶  token-source  ◀──consume──  parser
parser ──produce──▶  statement-source ◀──consume──  interpreter
                                      ◀──consume──  formatter
                                      ◀──consume──  linter
```

Eso permite reemplazar cualquier etapa sin recompilar las otras, y es lo que
hace que `interpreter`, `formatter` y `linter` sean **intercambiables**: los
tres consumen exactamente lo mismo y ninguno sabe que los otros existen.

---

# 4. Los módulos, uno por uno

## `common`

**Qué hace.** Dos tipos de datos: `SourcePosition` (línea, columna, offset) y
`SourceSpan` (desde una posición hasta otra).

**Por qué existe.** Todo error del proyecto tiene que decir *dónde* pasó.
Necesitás un vocabulario compartido para eso.

**Qué resuelve.** Que el mensaje sea `error: la variable 'x' no fue declarada —
línea 3, columnas 5 a 9` en vez de `error: la variable 'x' no fue declarada`.

Es el **sumidero** del grafo: seis módulos dependen de él y él no depende de
nadie.

## `source-reader`

**Qué hace.** El contrato para leer el código fuente en bloques, y una factory
que lo crea desde un `String` o desde un `Path`.

**Por qué existe.** Para que el lexer no sepa de dónde viene el código: puede
ser un archivo, un test, o mañana la red.

**Qué resuelve.** Además, la factory verifica que el archivo exista, sea un
archivo y sea legible, y devuelve un `SourceAccessError` tipado con la ruta —
que es lo que después el CLI traduce a castellano.

## `token-source`

**Qué hace.** Define qué es un **token**:

```kotlin
public data class Token(
    public val type: TokenType,
    public val lexeme: String,     // el texto tal cual apareció
    public val span: SourceSpan,   // dónde estaba
)
```

Y el contrato `TokenSource` para pedirlos de a uno.

**Por qué existe.** Es la **frontera entre el lexer y el parser**. Ninguno de
los dos se nombra al otro; los dos nombran este módulo.

**Detalle importante.** `TokenType` es una **interfaz abierta**, no un enum. El
módulo genérico no puede saber qué tokens tiene un lenguaje: `LET` y `PRINTLN`
son de PrintScript 1.0 y viven en `printscript-v1`.

## `printscript-ast`

**Qué hace.** Los nodos concretos del árbol: `VariableDeclarationStatement`,
`AssignmentStatement`, `PrintlnStatement`, la jerarquía `Expression` con sus
seis casos, `Identifier`, `DeclaredType`, los operadores.

**Por qué existe.** Es el módulo más nuevo, creado cuando se dieron cuenta de
que el AST de PrintScript 2.0 va a ser el de 1.0 más algunas cosas. Si el AST
viviera adentro de `printscript-v1`, V2 tendría que duplicarlo o depender de V1
solo para eso.

**Qué resuelve.** Que las versiones oficiales compartan los nodos que
representan lo mismo.

**Detalle importante.** `Expression` es **sellada** (`sealed`) a propósito. Eso
le da al compilador la lista completa de expresiones posibles, y por eso el
evaluador puede hacer un `when` sin `else` — si mañana aparece una expresión
nueva, deja de compilar en todos lados hasta que la contemplen.

## `statement-source`

**Qué hace.** El contrato del pipeline de sentencias:

- `Statement` — la interfaz raíz, **abierta**
- `StatementSource` — pedí la próxima sentencia
- `StatementReadResult` — Success / Failure / EndOfInput
- `ParseError` — los errores de sintaxis

**Por qué existe.** Es la **frontera entre el parser y sus tres consumidores**.

**Detalle importante.** Acá conviven las dos filosofías del proyecto:
`Statement` es abierta (cualquiera puede aportar sentencias nuevas), mientras
que `Expression`, en el otro módulo, es sellada. La explicación está en la
[sección 5](#la-regla-sealed-vs-interface).

## `lexer`

**Qué hace.** El motor que convierte caracteres en tokens. Mantiene un cursor
**inmutable** sobre el texto y delega el reconocimiento en estrategias.

**Por qué existe separado de PrintScript.** El motor no sabe qué es `let`. Lo
que sabe es: *"tengo una lista de escáneres; le pregunto a cada uno si reconoce
lo que sigue; el primero que dice que sí, gana."*

**Qué resuelve.** Agregar una palabra clave nueva no toca el motor: se agrega a
la configuración de la versión.

```kotlin
public object LexerFactory {
    public fun create(
        tokenScanners: List<TokenScanner>,        // ← las estrategias
        ignoredCharacterPolicy: IgnoredCharacterPolicy,
        endOfInputTokenType: TokenType,
    ): Lexer
}
```

## `parser`

**Qué hace.** El motor que convierte tokens en sentencias. Trae además un motor
genérico de expresiones por **niveles de precedencia** — el que hace que
`2 + 3 * 4` dé 14 y no 20.

**Por qué existe separado.** Igual que el lexer: coordina estrategias de
sentencias sin saber cuáles son.

**Qué resuelve.** El tipo que produce el motor de expresiones es configurable,
así que PrintScript lo especializa con su `Expression` sellada sin que el motor
tenga que conocerla.

## `interpreter`

**Qué hace.** El motor que recorre las sentencias y las ejecuta.

**Por qué existe separado.** Y acá está la parte más elegante del proyecto:

```kotlin
public interface StatementExecutor<S> {
    public fun supportsStatement(statement: Statement): Boolean
    public fun executeStatement(statement: Statement, state: S): ExecutionResult<S>
}
```

Ese `<S>` es el **estado**. El motor **no sabe que existe un "environment"**.
Sabe que hay algo llamado `S` que entra y sale de cada ejecución, y lo
transporta sin mirarlo. PrintScript 1.0 le pasa su `PrintScriptV1Environment`;
otro lenguaje podría pasarle una pila, o un contador.

**Qué resuelve.** El motor es reutilizable por cualquier lenguaje sin cambiar
una línea.

## `formatter`

**Qué hace.** Recorre las sentencias y las reescribe como texto con formato.

**Por qué existe separado.** Mismo patrón: coordina `StatementFormatter`
públicos y una `StatementSeparationPolicy` que decide qué va entre sentencia y
sentencia.

**Qué resuelve.** Que el usuario configure espacios y saltos sin que el motor
tenga reglas quemadas adentro.

## `linter`

**Qué hace.** Recorre las sentencias y produce **diagnósticos** — avisos de
estilo, no errores.

**Por qué existe separado.** Coordina `LintRule` públicas.

**Detalle importante.** A diferencia del intérprete, el linter **no despacha**:
hace *fan-out*. Toda regla mira toda sentencia. Por eso no existe el caso "no
hubo regla que supiera qué hacer con esto".

## `printscript-v1`

Tiene su propia sección: la [5](#5-qué-es-printscript-v1).

## `cli`

**Qué hace.** La aplicación de línea de comandos. Traduce lo que escribe el
usuario en la terminal a una operación, arma el pipeline, la corre y devuelve un
código de salida.

**Por qué existe separado.** Es el único módulo con una **dependencia externa**
(Clikt). Manteniéndolo aislado, los motores siguen sin depender de terceros.

Tiene su propia sección: la [7](#7-el-cli-clase-por-clase).

## `integration-tests`

**Qué hace.** Prueba el pipeline completo de punta a punta, en caja negra:
código fuente entra, salida o error sale.

**Por qué existe separado.** Los tests de cada módulo verifican
responsabilidades propias; este verifica que **el cableado** funcione.

---

# 5. Qué es printscript-v1

Esta es la pieza que más confunde al principio, y es la más importante de
entender.

## El problema que resuelve

Los cinco motores —lexer, parser, interpreter, formatter, linter— **no saben
nada de PrintScript**. No conocen la palabra `let`, ni el punto y coma, ni que
`println` recibe un argumento entre paréntesis.

Entonces, ¿dónde vive todo eso? En `printscript-v1`.

## La metáfora

Pensalo como un **reproductor de música y un disco**.

El motor es el reproductor: sabe girar, leer surcos y mandar sonido al parlante,
pero no tiene ninguna canción adentro. `printscript-v1` es el disco: tiene las
canciones, pero no puede sonar solo.

**El reproductor no sabe qué disco le van a poner. El disco sabe en qué
reproductor se pone.**

Por eso la dependencia va así:

```
printscript-v1  ────depende de───▶  lexer, parser, interpreter, formatter, linter
```

Y **nunca** al revés.

## Qué hay adentro

| Categoría | Ejemplos |
|---|---|
| Vocabulario | `PrintScriptV1TokenType` (LET, PRINTLN, SEMICOLON…) |
| Escáneres | reconocer identificadores, números, textos, símbolos |
| Gramática | `let X : T = E ;` y `println ( E ) ;` |
| Ejecución | los tres executors, el evaluador, las cuatro operaciones |
| Formateo | cómo se escribe cada sentencia |
| Reglas | camelCase, qué acepta `println` |
| Errores | `PrintScriptV1SemanticError`, `PrintScriptV1LexicalError` |

## Los cinco puntos de extensión

Cada factory de V1 acepta que quien la use agregue estrategias propias:

| Factory | Parámetro | Qué permite |
|---|---|---|
| `PrintScriptV1LexerFactory` | `additionalScanners` | tokens nuevos |
| `PrintScriptV1ParserFactory` | `additionalStatementParsers` | sentencias nuevas |
| `PrintScriptV1InterpreterFactory` | `additionalStatementExecutors` | ejecutarlas |
| `PrintScriptV1FormatterFactory` | `additionalStatementFormatters` | formatearlas |
| `PrintScriptV1LinterFactory` | `additionalRules` | reglas nuevas |

En los cinco casos, **lo que aporta quien llama va primero en la lista**. Como
gana el primero que dice "yo lo soporto", eso significa que con un solo
mecanismo podés *agregar* comportamiento o *pisar* el existente.

Ahí está el plan para PrintScript 2.0: su factory llamará a la de V1 pasándole
lo suyo, sin duplicar nada.

## La regla `sealed` vs `interface`

La pregunta que siempre aparece: *¿por qué algunas jerarquías son `sealed` y
otras `interface` abierta?* Son dos ejes distintos.

| Eje | Se usa | Ejemplos |
|---|---|---|
| **Versiones oficiales del lenguaje** | `sealed` | `Expression`, `RuntimeValue`, `DeclaredType` |
| **Extensiones de terceros** | `interface` | `Statement`, `TokenType`, `Diagnostic`, `ParseError` |

**Sellado** = conjunto cerrado. El compilador conoce todos los casos y te obliga
a cubrirlos. Agregar uno es un cambio deliberado que rompe la compilación en
todos los lugares que hay que revisar.

**Abierto** = cualquiera aporta el suyo desde afuera sin tocar el núcleo. El
precio es que el compilador ya no puede avisarte, y aparece un `else`.

> **El precio, en concreto.** Hoy hay siete jerarquías abiertas, y el
> `ErrorReporter` del CLI tiene cuatro ramas `else` con mensajes como *"error
> semántico desconocido"*. Si alguien agrega un caso y se olvida de redactarlo,
> nada se rompe: el usuario simplemente ve el mensaje genérico. Por eso existe
> `ErrorWordingCompletenessTest`, que es la red que reemplaza al compilador.

---

# 6. El interpreter, clase por clase

El intérprete está partido en dos: el **motor** (módulo `interpreter`) y las
**reglas de V1** (dentro de `printscript-v1`).

## 6.1 El motor genérico

### `Interpreter`

```kotlin
public interface Interpreter {
    public fun interpret(source: StatementSource): InterpretationResult
}
```

| | |
|---|---|
| **Recibe** | un `StatementSource` — la fuente perezosa de sentencias |
| **Devuelve** | `InterpretationResult` |
| **Por qué** | es la puerta de entrada. Todo el intérprete es una función de sentencias a resultado |

### `InterpretationResult`

```kotlin
public sealed interface InterpretationResult {
    data object Success
    data class ParseFailure(val error: ParseError)      // el archivo está mal escrito
    data class SemanticFailure(val error: SemanticError) // está bien escrito pero no tiene sentido
}
```

**Por qué distingue dos fallas.** `let a: number = 5` sin `;` es un
`ParseFailure`. `println(inexistente);` está perfectamente escrito pero usa una
variable que no existe: eso es `SemanticFailure`. Son problemas distintos y el
usuario merece mensajes distintos.

### `StatementExecutor<S>`

```kotlin
public interface StatementExecutor<S> {
    public fun supportsStatement(statement: Statement): Boolean
    public fun executeStatement(statement: Statement, state: S): ExecutionResult<S>
}
```

| | |
|---|---|
| **Recibe** | una sentencia y el estado actual |
| **Devuelve** | `ExecutionResult<S>` — el estado **nuevo**, o un error |
| **Por qué dos métodos** | `supportsStatement` es la pregunta, `executeStatement` es la acción. Eso permite tener una lista de executors y preguntarle a cada uno |
| **Por qué devuelve el estado** | porque el estado es **inmutable**: ejecutar `let a = 5` no modifica el environment, produce uno nuevo con `a` adentro |

Esto es el patrón **Strategy**.

### `ExecutionResult<T>`

```kotlin
public sealed interface ExecutionResult<out T> {
    data class Success<T>(val value: T)
    data class Failure(val error: SemanticError) : ExecutionResult<Nothing>
}
```

**Por qué `Nothing` en el Failure.** Un fracaso no tiene valor de ningún tipo,
así que sirve como resultado de cualquier operación. Es un detalle de tipos que
evita tener que escribir `Failure<Environment>`, `Failure<RuntimeValue>`, etc.

### `ConfigurableInterpreter<S>` — el corazón

Es la única clase con lógica del motor, y merece leerse despacio.

**El problema que resuelve:** recorrer un programa de un millón de sentencias
sin usar recursión (que reventaría el stack) y sin usar `var` (que rompería la
inmutabilidad).

**La solución:** modelar el recorrido como una **máquina de estados** de dos
casos.

```kotlin
private sealed interface InterpretationStep<out S> {
    data class Pending<S>(val source: StatementSource, val state: S)
    data class Finished(val result: InterpretationResult)
}
```

- **`Pending`** = "todavía queda programa; acá está lo que falta y acá el estado
  con el que sigo"
- **`Finished`** = "terminé, con este resultado"

Y después:

```kotlin
override fun interpret(source: StatementSource): InterpretationResult {
    return interpretationSteps(source)
        .filterIsInstance<InterpretationStep.Finished>()
        .first()
        .result
}
```

Se lee: *"generá los pasos, quedate con el primero que sea Finished, devolvé su
resultado"*.

La magia está en `generateSequence`, que es **perezoso**: no genera todos los
pasos y después filtra. Genera uno, pregunta si sirve, y solo si no sirve genera
el siguiente. Con un programa de un millón de sentencias, en memoria hay **un
paso a la vez**.

El avance de un paso al siguiente:

```kotlin
private fun readAndExecute(step: Pending<S>): InterpretationStep<S> {
    return when (val readResult = step.source.nextStatement()) {
        EndOfInput  -> Finished(InterpretationResult.Success)
        is Failure  -> Finished(InterpretationResult.ParseFailure(readResult.error))
        is Success  -> continueAfter(readResult, step.state)
    }
}
```

Y ejecutarla:

```kotlin
private fun continueAfter(readResult: Success, state: S): InterpretationStep<S> {
    return when (val execution = dispatcher.dispatchToExecutor(readResult.statement, state)) {
        is ExecutionResult.Failure -> Finished(SemanticFailure(execution.error))
        is ExecutionResult.Success -> Pending(readResult.remainingSource, execution.value)
                                              //  ↑ el resto            ↑ el estado nuevo
    }
}
```

**Cero `var`. Cero recursión. Cero mutación.**

### `StatementExecutorDispatcher<S>`

```kotlin
fun dispatchToExecutor(statement: Statement, state: S): ExecutionResult<S> {
    for (executor in statementExecutors) {
        if (executor.supportsStatement(statement)) {
            return executor.executeStatement(statement, state)
        }
    }
    return ExecutionResult.Failure(SemanticError.UnsupportedStatement(statement.span))
}
```

| | |
|---|---|
| **Recibe** | una sentencia y el estado |
| **Método** | recorre la lista y le pregunta a cada executor |
| **Por qué** | el motor no sabe qué sentencias existen. Pregunta. Gana el primero |
| **Si nadie puede** | `UnsupportedStatement` — un error de dominio, no una excepción |

Este es el patrón **Dispatcher**. Y el orden importa: por eso las factories
ponen los executors "adicionales" primero, para que puedan pisar a los de V1.

## 6.2 El intérprete de PrintScript 1.0

### `PrintScriptV1RuntimeValue`

**El problema:** durante la ejecución, `5` ya no es texto ni un nodo del árbol.
Es un **valor**.

```kotlin
public sealed interface PrintScriptV1RuntimeValue {
    public val type: DeclaredType
    public fun asText(): String
}

public data class PrintScriptV1NumberValue(val value: BigDecimal)
public data class PrintScriptV1StringValue(val value: String)
```

**Por qué `BigDecimal` y no `Double`.** Porque `0.1 + 0.2` con `Double` da
`0.30000000000000004`. `BigDecimal` es exacto.

**Por qué `asText()`.** Porque `println` tiene que imprimir tanto números como
textos, y cada uno sabe cómo se muestra.

### `PrintScriptV1Environment` y `PrintScriptV1VariableBinding`

```kotlin
public interface PrintScriptV1Environment {
    public fun lookupBinding(name: String): PrintScriptV1VariableBinding?
    public fun withBinding(name: String, binding: PrintScriptV1VariableBinding): PrintScriptV1Environment
}

public data class PrintScriptV1VariableBinding(
    public val type: DeclaredType,
    public val value: PrintScriptV1RuntimeValue?,   // ← nullable a propósito
)
```

El environment es **la memoria del programa**: qué variables existen y qué
valen.

**Por qué `withBinding` devuelve un Environment nuevo** en vez de modificar el
actual. Porque es inmutable. `MapEnvironment` lo implementa así:

```kotlin
override fun withBinding(name: String, binding: ...): PrintScriptV1Environment {
    return MapEnvironment(bindings + (name to binding))
}
```

`bindings + (name to binding)` crea un **mapa nuevo**. El anterior queda intacto.

**Por qué `value` es nullable.** Porque `let a: number;` declara una variable sin
darle valor. El binding existe (la variable está declarada) pero su valor es
`null`. Eso permite distinguir dos errores distintos: *"no existe"* y *"existe
pero nunca recibió valor"*.

### `PrintScriptV1ExpressionEvaluator` y `DefaultExpressionEvaluator`

```kotlin
public interface PrintScriptV1ExpressionEvaluator {
    public fun evaluateExpression(
        expression: Expression,
        environment: PrintScriptV1Environment,
    ): ExecutionResult<PrintScriptV1RuntimeValue>
}
```

Convierte un pedazo de árbol en un valor. `2 + 3 * 4` → `NumberValue(14)`.

La implementación:

```kotlin
return when (expression) {
    is NumberLiteralExpression -> evaluateNumberLiteral(expression)
    is StringLiteralExpression -> evaluateStringLiteral(expression)
    is GroupingExpression      -> evaluateExpression(expression.expression, environment)
    is IdentifierExpression    -> evaluateIdentifier(expression, environment)
    is UnaryExpression         -> evaluateUnary(expression, environment)
    is BinaryExpression        -> evaluateBinary(expression, environment)
}
```

**Fijate que no hay `else`.** Eso es la **exhaustividad** que da tener
`Expression` sellada: el compilador sabe que son exactamente seis. Si mañana
alguien agrega una séptima, esta línea deja de compilar.

**Es recursivo, y está bien que lo sea:** una expresión contiene expresiones. En
`2 + 3 * 4`, el caso `BinaryExpression` evalúa primero la izquierda y después la
derecha, y cada una puede ser otra binaria. La profundidad de la recursión es la
del árbol, que es chica.

### `BinaryOperationRegistry` y las cuatro operaciones

```kotlin
private val operations = mapOf(
    BinaryOperator.ADD      to AddOperation(),
    BinaryOperator.SUBTRACT to SubtractOperation(),
    BinaryOperator.MULTIPLY to MultiplyOperation(),
    BinaryOperator.DIVIDE   to DivideOperation(),
)
```

**Por qué un mapa y no un `when`.** Porque agregar un operador es agregar una
entrada, no editar una función.

`SubtractOperation`, `MultiplyOperation` y `DivideOperation` heredan de
`ArithmeticOperation`, que aplica **Template Method**: la clase base verifica
que ambos operandos sean números y, si lo son, delega el cálculo concreto. Cada
hija escribe solo su cuenta.

`AddOperation` es la excepción, porque en PrintScript el `+` hace dos cosas:
suma si ambos son números, concatena en cualquier otro caso.

### Los tres executors

Los tres siguen exactamente la misma forma:

```kotlin
override fun supportsStatement(statement: Statement) = statement is XStatement

override fun executeStatement(statement: Statement, state: Environment): ExecutionResult<Environment> {
    if (statement !is XStatement) { return Failure(UnsupportedStatement(statement.span)) }
    // ... el trabajo
}
```

**`DeclarationExecutor`** — para `let a: number = 5;`

1. `ensureNotAlreadyDeclared` → si `a` ya existe, `AlreadyDeclaredVariable`
2. `evaluateInitializer` → evalúa el `5`, y verifica que el tipo del valor
   coincida con el declarado
3. Devuelve `state.withBinding("a", VariableBinding(NUMBER, NumberValue(5)))`

**`AssignmentExecutor`** — para `a = 10;`

1. Busca `a`. Si no existe → `UndeclaredVariable`
2. Evalúa `10`
3. Verifica contra el tipo **con el que fue declarada**, no contra uno nuevo
4. Devuelve el environment con el valor reemplazado

**`PrintlnExecutor`** — para `println(a);`

1. Evalúa el argumento
2. `output.writeLine(value.asText())`
3. **Devuelve el estado sin cambios** — imprimir no modifica la memoria

### `orReturn`

Aparece en todos los executors y merece una explicación:

```kotlin
internal inline fun <S> ExecutionResult<S>.orReturn(
    onFailure: (ExecutionResult.Failure) -> Nothing
): S = when (this) {
    is ExecutionResult.Success -> value
    is ExecutionResult.Failure -> onFailure(this)
}
```

Convierte esto:

```kotlin
val resultado = evaluar(expr, state)
val valor = when (resultado) {
    is Failure -> return resultado
    is Success -> resultado.value
}
```

En esto:

```kotlin
val valor = evaluar(expr, state).orReturn { return it }
```

Se lee: *"dame el valor, o salí de la función con el error"*. Es el equivalente
del `?` de Rust.

## 6.3 Traza completa

Programa:

```typescript
let a: number = 5;
println(a);
```

| # | Dónde | Qué pasa |
|---|---|---|
| 1 | `PrintScriptV1InterpreterFactory.create(output)` | arma `ConfigurableInterpreter` con `MapEnvironment()` vacío y los 3 executors |
| 2 | `ConfigurableInterpreter.interpret(source)` | arranca la secuencia con `Pending(source, envVacío)` |
| 3 | `readAndExecute` | `source.nextStatement()` → `Success(VariableDeclarationStatement, resto)` |
| 4 | `dispatchToExecutor` | pregunta a los 3; `DeclarationExecutor` dice que sí |
| 5 | `DeclarationExecutor` | `a` no existe ✓ |
| 6 | `DefaultExpressionEvaluator` | `NumberLiteralExpression(5)` → `NumberValue(5)` |
| 7 | `verifyAccepts` | declarado NUMBER, valor NUMBER ✓ |
| 8 | `DeclarationExecutor` | devuelve `Success(env₂)` donde `env₂` tiene `a → NumberValue(5)` |
| 9 | `continueAfter` | produce `Pending(resto, env₂)` |
| 10 | `readAndExecute` | `nextStatement()` → `Success(PrintlnStatement, resto₂)` |
| 11 | `dispatchToExecutor` | `PrintlnExecutor` dice que sí |
| 12 | `DefaultExpressionEvaluator` | `IdentifierExpression("a")` → busca en `env₂` → `NumberValue(5)` |
| 13 | `PrintlnExecutor` | `output.writeLine("5")` → **aparece el 5 en pantalla** |
| 14 | | devuelve `Success(env₂)` — sin cambios |
| 15 | `readAndExecute` | `nextStatement()` → `EndOfInput` |
| 16 | | `Finished(InterpretationResult.Success)` |
| 17 | `interpret` | el filtro encuentra el `Finished` y devuelve `Success` |

---

# 7. El CLI, clase por clase

## 7.1 Las dos capas

El CLI está partido en **adaptador** y **dominio**:

```
┌─ ADAPTADOR (sabe de Clikt, argv, --help, códigos de salida) ─┐
│  Main · PrintScriptCommandFactory · PrintScriptCommandGroup  │
│  ValidationCommand · ExecutionCommand ·                      │
│  FormattingCommand · AnalysisCommand                         │
│  LanguageOptions · sourceFileArgument()                      │
│  runSourceOperation() · EchoTerminal · ProgramTermination    │
└──────────────────────────────────────────────────────────────┘
                            │
┌─ DOMINIO (no sabe que existe una terminal ni argv) ──────────┐
│  SourceOperationRunner · SourceOperationRequest              │
│  SourceOperation + sus 4 implementaciones                    │
│  OperationOutcome · ExitCode                                 │
│  StatementSourcePipeline · ProgressReportingStatementSource  │
│  ErrorReporter · DiagnosticReporter · PrintScriptWording     │
└──────────────────────────────────────────────────────────────┘
```

Esto se llama **Ports & Adapters**. La ventaja concreta: las cuatro operaciones
se pueden testear sin línea de comandos, y cambiar la librería de CLI tocaría
solo la capa de arriba.

## 7.2 El adaptador

### `Main.kt`

```kotlin
public fun main(args: Array<String>) {
    PrintScriptCommandFactory.create().main(args)
}
```

Tres líneas. Todo lo demás está detrás de la factory.

### `PrintScriptCommandFactory` — la raíz de composición

El único lugar donde se instancian los comandos:

```kotlin
return PrintScriptCommandGroup().subcommands(
    ValidationCommand(
        operationFactory = SourceOperationFactory { request ->
            when (request.version) {
                LanguageVersion.V1_0 -> ValidationOperation(errorReporter)
            }
        },
        errorReporter = errorReporter,
    ),
    // ... los otros tres
)
```

**Producción y tests la comparten a propósito.** Si cada uno armara su propia
composición, un test podría quedar en verde verificando un CLI distinto del que
se distribuye.

Y esos `when (request.version)` con un solo caso no son redundantes: son
**exhaustivos** sobre `LanguageVersion`. El día que se agregue `V2_0` al enum,
los cuatro dejan de compilar y el compilador entrega la lista completa de
decisiones a tomar.

### `PrintScriptCommandGroup`

Comando raíz. Su `run()` está **vacío a propósito**: no hace trabajo, solo
agrupa los cuatro subcomandos y le da a Clikt el nombre del ejecutable.

### Los cuatro comandos — composición, no herencia

Los cuatro heredan **directamente de `CliktCommand`**. No hay clase base propia:

```kotlin
internal class ValidationCommand(
    private val operationFactory: SourceOperationFactory,
    private val errorReporter: ErrorReporter,
    private val pipeline: StatementSourcePipeline = StatementSourcePipeline(),
) : CliktCommand(name = "validation") {

    private val sourceFilePath by sourceFileArgument()

    private val languageOptions by LanguageOptions()

    override fun help(context: Context): String {
        return "Verifica que el archivo sea válido, sin mostrar lo que el programa imprimiría"
    }

    override fun run() {
        runSourceOperation(
            request = SourceOperationRequest(
                sourceFilePath = sourceFilePath,
                version = languageOptions.version,
            ),
            operationFactory = operationFactory,
            errorReporter = errorReporter,
            pipeline = pipeline,
        )
    }
}
```

**Por qué no hay clase base.** Con Template Method los cuatro comandos quedaban
obligados a exponer **exactamente las mismas opciones**, porque las opciones se
declaran en el cuerpo de la clase. Declarándolas por comando, cada uno puede
tener las suyas — algo que va a hacer falta cuando vuelva `--config`, que
corresponde solo a `formatting` y `analysis`.

**Cómo se comparte sin heredar.** Tres mecanismos, todos de la propia librería:

| Qué | Cómo | Por qué así |
|---|---|---|
| el argumento `<archivo>` | `sourceFileArgument()` | es extensión porque `argument()` registra el parámetro **en el comando que la llama**, así que necesita saber cuál es |
| la opción `--version` | `LanguageOptions : OptionGroup` | es el mecanismo de Clikt para agrupar opciones reutilizables |
| la orquestación del `run()` | `runSourceOperation()` | extensión de nuevo: el receptor le da acceso al `echo` que necesita `EchoTerminal` |

**Qué es `by`.** Delegación de propiedad: no guarda un valor, se lo pide al
objeto que devolvió `sourceFileArgument()`. Y ese objeto, al crearse, **se
registró en el comando** — por eso Clikt genera el `--help` sin que nadie se lo
diga. Declarar el parámetro y documentarlo son el mismo acto.

**El pareo de nombres dice en qué capa vive cada cosa:** `ValidationCommand` es
adaptador, `ValidationOperation` es dominio.

### `SourceOperationFactory`

```kotlin
internal fun interface SourceOperationFactory {
    fun create(request: SourceOperationRequest): SourceOperation
}
```

El comando **no sabe qué operación monta**: eso lo decide la raíz de composición.
Es también el punto de sustitución de los tests —se inyecta una factory falsa— y
el lugar donde va a vivir la selección por versión cuando exista V2.

Es un `fun interface`, así que en el punto de uso se escribe como lambda.

### `EchoTerminal`

Patrón **Adapter**. Implementa el puerto `Terminal` del dominio escribiendo por
`echo`, que es el método de salida de Clikt.

**Por qué `echo` y no `println`.** Porque Clikt detecta el tipo de terminal
antes de escribir, y porque su helper de tests captura lo que pasa por `echo`
mientras que `println` se le escapa.

### `ProgramTermination`

```kotlin
internal object ProgramTermination {
    fun endWith(exitCode: ExitCode) {
        if (exitCode == ExitCode.SUCCESS) { return }
        throw ProgramResult(exitCode.value)
    }
}
```

**Es el único `throw` del proyecto.** Clikt señaliza el final del programa
lanzando `ProgramResult` — es su protocolo, no manejo de errores. Aislarlo en un
archivo de seis líneas lo hace auditable de un vistazo:

```bash
grep -rn "throw\|catch" cli/src/main --include=*.kt
```

## 7.3 El dominio

### `SourceOperation`

```kotlin
internal interface SourceOperation {
    fun outcomeFor(statements: StatementSource, terminal: Terminal): OperationOutcome
}
```

Patrón **Strategy**. *"Dame sentencias y una terminal, te digo cómo salió."* No
sabe de dónde vinieron las sentencias ni cómo se lo pidió el usuario.

### `OperationOutcome` y `ExitCode`

Son dos vocabularios distintos a propósito:

| `OperationOutcome` (dominio) | `ExitCode` (proceso) |
|---|---|
| `Success` | `SUCCESS(0)` |
| `CompletedWithFindings` | `FINDINGS(3)` |
| `Failure` | `SOURCE_ERROR(1)` |

**Por qué separarlos.** El dominio habla de *significado*; el sistema operativo
habla de *números*. La traducción se hace en un solo lugar.

**Por qué `FINDINGS(3)` existe.** Para que un CI pueda distinguir *"el código
está roto"* de *"el código funciona pero no respeta las convenciones"*.

### `SourceOperationRunner` — el orquestador

```kotlin
fun exitCodeFor(operation: SourceOperation, request: SourceOperationRequest): ExitCode {
    val outcome = outcomeOf(operation, request)   // calculá, sin imprimir
    reportOutcome(outcome)                         // imprimí, sin devolver
    return exitCodeOf(outcome)                     // traducí, función pura
}
```

Tres pasos, tres funciones, cada una con **una sola** responsabilidad. Esto es
**Command-Query Separation**: una función o hace algo, o responde algo, nunca
las dos.

`outcomeOf` hace el trabajo pesado:

```kotlin
return when (val creation = SourceReaderFactory.fromPath(request.sourceFilePath)) {
    is Failure -> OperationOutcome.Failure(errorReporter.describe(creation.error))
    is Success -> operation.outcomeFor(
        statements = progressReportingStatementsOf(creation.reader, request),
        terminal = terminal,
    )
}
```

Fijate que un archivo que no se puede abrir se modela como `OperationOutcome.Failure`,
igual que cualquier otro fracaso. Mismo camino, mismo código de salida.

### `StatementSourcePipeline`

```kotlin
fun statementsFrom(sourceReader: SourceReader, version: LanguageVersion): StatementSource {
    return when (version) {
        LanguageVersion.V1_0 -> v1StatementsFrom(sourceReader)
    }
}

private fun v1StatementsFrom(sourceReader: SourceReader): StatementSource {
    return PrintScriptV1ParserFactory.create().parse(
        tokens = PrintScriptV1LexerFactory.create().tokenize(sourceReader),
    )
}
```

**Es el único lugar del CLI que nombra al lexer y al parser.** Las operaciones
reciben un `StatementSource` ya armado y no saben que existen esas etapas. Por
eso las cuatro comparten el mismo pipeline.

### `ProgressReportingStatementSource`

Patrón **Decorator**. Envuelve un `StatementSource` y, cada vez que le piden una
sentencia, calcula qué porcentaje del archivo se leyó y avisa al cruzar cada
decena.

**Por qué decorator.** Porque el progreso no es responsabilidad del parser ni
del intérprete. Envolviendo, se agrega sin tocar a ninguno de los dos.

**Detalle:** el progreso se escribe a **stderr**, no a stdout. Así
`printscript execution programa.ps > salida.txt` guarda solo lo que el programa
imprimió, sin el ruido del progreso.

### `ValidationOperation` y `ExecutionOperation`

Las dos heredan de `InterpretingOperation`, que aplica **Template Method** otra
vez. Validar y ejecutar hacen exactamente lo mismo salvo dos cosas:

| | `ValidationOperation` | `ExecutionOperation` |
|---|---|---|
| `programOutputOn(terminal)` | `DiscardedProgramOutput` | `TerminalProgramOutput(terminal)` |
| `reportSuccessOn(terminal)` | escribe *"El archivo es válido."* | nada |

**`DiscardedProgramOutput` es un Null Object**: implementa la interfaz de salida
sin hacer nada. Gracias a eso, `validation` **corre el programa entero** —y por
lo tanto detecta errores semánticos— sin que el usuario vea lo que habría
impreso.

### `FormattingOperation` y `AnalysisOperation`

Las dos usan **recursión de cola** (`tailrec`) para recorrer su fuente:

```kotlin
private tailrec fun writeRemainingFormattedStatements(
    source: FormattedSource,
    terminal: Terminal,
): OperationOutcome { ... }
```

`tailrec` le pide al compilador que convierta la recursión en un bucle. Se lee
como recursión pero no consume stack.

En `AnalysisOperation`, el contador de hallazgos viaja **como parámetro** y no
como campo, así la clase no guarda estado entre corridas.

## 7.4 El reporte

Tres clases que responden preguntas distintas:

| Clase | Pregunta que responde |
|---|---|
| `ErrorReporter` | ¿qué salió mal y dónde? |
| `DiagnosticReporter` | ¿qué aviso de estilo hay y dónde? |
| `PrintScriptWording` | ¿cómo se dice cada elemento del lenguaje en castellano? |
| `SpanRenderer` | ¿cómo se escribe una posición? |

**Por qué `PrintScriptWording` es aparte.** Los reporters explican *qué pasó*;
esto explica *cómo se dice*. Ambos reporters la comparten, y mañana podría estar
traducida a otro idioma sin tocar nada más.

## 7.5 Traza completa

Comando: `printscript execution ejemplo.ps`

| # | Dónde | Qué pasa |
|---|---|---|
| 1 | `main(args)` | llama a `PrintScriptCommandFactory.create().main(args)` |
| 2 | Clikt | parsea `argv`, encuentra `execution`, llena las dos propiedades |
| 3 | `ExecutionCommand.run()` | arma `SourceOperationRequest(path, V1_0)` |
| 4 | `runSourceOperation(...)` | la extensión compartida por los cuatro comandos |
| 5 | `SourceOperationFactory.create(request)` | el `when (version)` devuelve `ExecutionOperation` |
| 6 | `SourceOperationRunner.exitCodeFor` | arranca |
| 7 | `SourceReaderFactory.fromPath` | existe, es archivo, es legible → `Success(reader)` |
| 8 | `StatementSourcePipeline.statementsFrom` | lexer + parser → `StatementSource` |
| 9 | `ProgressReportingStatementSource` | lo envuelve para reportar progreso |
| 10 | `ExecutionOperation.outcomeFor` | crea el intérprete con `TerminalProgramOutput` |
| 11 | El intérprete | corre el programa; cada `println` llega a la terminal |
| 12 | | devuelve `InterpretationResult.Success` → `OperationOutcome.Success` |
| 13 | `reportOutcome` | no imprime nada, porque fue Success |
| 14 | `exitCodeOf` | `Success` → `ExitCode.SUCCESS` |
| 15 | `ProgramTermination.endWith` | es SUCCESS → **no lanza nada**, vuelve normal |
| 16 | Clikt | `run()` terminó sin excepción → sale con 0 |

Si en el paso 11 el programa hubiera usado una variable inexistente:

| # | Dónde | Qué pasa |
|---|---|---|
| 11' | `DefaultExpressionEvaluator` | `lookupBinding("x")` → `null` → `Failure(UndeclaredVariable)` |
| 12' | `ConfigurableInterpreter` | `Finished(SemanticFailure(error))` |
| 13' | `InterpretingOperation` | `errorReporter.describe(error)` → texto en castellano con posición |
| 14' | | `OperationOutcome.Failure(mensaje)` |
| 15' | `reportOutcome` | `terminal.writeErrorLine(mensaje)` → va a **stderr** |
| 16' | `exitCodeOf` | `Failure` → `ExitCode.SOURCE_ERROR` |
| 17' | `ProgramTermination.endWith` | no es SUCCESS → `throw ProgramResult(1)` |
| 18' | Clikt | atrapa, sale con 1 |

---

# 8. Glosario

**AST** *(Abstract Syntax Tree)* — árbol de sintaxis abstracta. La
representación del programa como estructura anidada en vez de texto plano.

**Adapter** — patrón. Una clase que traduce una interfaz a otra. `EchoTerminal`
adapta el `echo` de Clikt al puerto `Terminal`.

**api vs implementation** — en Gradle, `api` significa que el tipo aparece en tu
firma pública y se re-expone a quien te use; `implementation` significa que lo
usás solo adentro y queda escondido.

**Command-Query Separation** — principio: una función o hace algo (comando) o
responde algo (consulta), nunca las dos.

**Decorator** — patrón. Envolver un objeto para agregarle comportamiento sin
modificarlo. `ProgressReportingStatementSource` decora un `StatementSource`.

**Dispatcher** — el que recibe algo y decide quién lo atiende.

**Exhaustividad** — cuando el compilador conoce todos los casos posibles de un
tipo y te obliga a cubrirlos. Solo funciona con `sealed`.

**Factory** — patrón. El único lugar autorizado para construir un objeto
complejo. `PrintScriptV1InterpreterFactory.create(...)`.

**generateSequence** — función de Kotlin que produce una secuencia perezosa a
partir de un valor inicial y una regla de avance.

**Inmutable** — que no se puede modificar después de creado. Para "cambiarlo" se
crea uno nuevo.

**Lexema** — el texto exacto que apareció en el archivo. En `let a`, los lexemas
son `"let"` y `"a"`.

**Lexer** — la etapa que convierte caracteres en tokens. También se llama
*tokenizador*.

**Null Object** — patrón. Una implementación que cumple la interfaz sin hacer
nada, para evitar chequeos de `null`. `DiscardedProgramOutput`.

**Parser** — la etapa que convierte tokens en un AST.

**Perezoso** *(lazy)* — que no calcula nada hasta que alguien lo pide.

**Ports & Adapters** — arquitectura donde el dominio define puertos (interfaces)
y la infraestructura los implementa. También se llama *hexagonal*.

**Pull** — modelo donde el consumidor pide datos, en vez de que el productor se
los empuje.

**Punto de extensión** — un parámetro que permite a quien te usa aportar
comportamiento propio. Los `additionalX` de las factories.

**Raíz de composición** *(composition root)* — el único lugar donde se crean y
conectan los objetos. En el proyecto es `Main.kt`.

**sealed** — una jerarquía cerrada. El compilador conoce todas las
implementaciones porque tienen que estar en el mismo módulo.

**Span** — un rango en el código fuente: desde dónde hasta dónde.

**Strategy** — patrón. Una familia de algoritmos intercambiables detrás de una
misma interfaz. `SourceOperation`, `StatementExecutor`, `LintRule`.

**tailrec** — modificador de Kotlin que convierte una función recursiva de cola
en un bucle, para que no consuma stack.

**Template Method** — patrón. Una clase base define el esqueleto de un algoritmo
y deja huecos para que las hijas los rellenen. `InterpretingOperation`,
`ArithmeticOperation`. El CLI lo usaba y se reemplazó por composición, para que
cada comando pueda tener sus propias opciones.

**Token** — la unidad mínima con significado del lenguaje. `let`, `a`, `:`,
`number`, `=`, `5`, `;` son siete tokens.
