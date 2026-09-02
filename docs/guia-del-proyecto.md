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

## 7.1 La forma del módulo

Diecinueve archivos en tres grupos:

```
┌─ COMANDOS · la superficie de la línea de comandos ───────────┐
│  Main · PrintScriptCommandFactory · PrintScriptCommandGroup  │
│  ValidationCommand · ExecutionCommand ·                      │
│  FormattingCommand · AnalysisCommand                         │
│  LanguageOptions · sourceFileArgument()                      │
│  runOnSourceFile() · interpretationOutcome()                 │
│  OperationOutcome · ExitCode                                 │
└──────────────────────────────────────────────────────────────┘
                            │ le pide las herramientas a
                            ▼
┌─ TOOLCHAIN · qué herramientas usa cada versión ──────────────┐
│  PrintScriptToolchain · PrintScriptToolchainFactory          │
│  LanguageVersion                                             │
└──────────────────────────────────────────────────────────────┘
                            │ y los errores se los da a
                            ▼
┌─ REPORTE · traduce errores de dominio a castellano ──────────┐
│  ErrorReporter · DiagnosticReporter                          │
│  PrintScriptWording · SpanRenderer                           │
└──────────────────────────────────────────────────────────────┘
```

**La frontera que importa: Clikt vive solo adentro de `cli`.** Ningún módulo
core lo conoce. Lo que cruza hacia afuera son los contratos de los módulos
—`ProgramOutput`, `StatementSource`, `Linter`— que no saben que existe una
terminal.

Antes había una capa más: un puerto `Terminal`, una interfaz `SourceOperation`
con cuatro implementaciones, un `SourceOperationRunner` y una
`SourceOperationFactory`. Se sacaron porque **duplicaban lo que Clikt ya
resuelve**: `echo` para escribir, `ProgramResult` para terminar y `test()` para
probar. Eran 32 archivos y quedaron 19.

## 7.2 Los comandos

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
    ValidationCommand(errorReporter),
    ExecutionCommand(errorReporter),
    FormattingCommand(errorReporter),
    AnalysisCommand(errorReporter, diagnosticReporter),
)
```

**Producción y tests la comparten a propósito.** Si cada uno armara su propia
composición, un test podría quedar en verde verificando un CLI distinto del que
se distribuye.

### `PrintScriptCommandGroup`

Comando raíz. Su `run()` está **vacío a propósito**: no hace trabajo, solo
agrupa los cuatro subcomandos y le da a Clikt el nombre del ejecutable.

### Los cuatro comandos

Los cuatro heredan **directamente de `CliktCommand`**. No hay clase base propia:

```kotlin
internal class ExecutionCommand(
    private val errorReporter: ErrorReporter,
    private val toolchainFor: (LanguageVersion) -> PrintScriptToolchain =
        PrintScriptToolchainFactory::forVersion,
) : CliktCommand(name = "execution") {

    private val sourceFilePath by sourceFileArgument()

    private val languageOptions by LanguageOptions()

    override fun help(context: Context) = "Ejecuta el programa y muestra su salida"

    override fun run() {
        val toolchain = toolchainFor(languageOptions.version)

        runOnSourceFile(
            sourceFilePath = sourceFilePath,
            toolchain = toolchain,
            errorReporter = errorReporter,
        ) { statements ->
            interpretationOutcome(
                interpreter = toolchain.interpreterWriting(terminalOutput()),
                statements = statements,
                errorReporter = errorReporter,
            )
        }
    }
}
```

**Por qué no hay clase base.** Con una, los cuatro comandos quedarían obligados
a exponer **exactamente las mismas opciones**, porque las opciones se declaran
en el cuerpo de la clase. Declarándolas por comando, cada uno puede tener las
suyas — algo que va a hacer falta cuando vuelva `--config`, que corresponde solo
a `formatting` y `analysis`.

**Por qué el toolchain entra como función y no como instancia.** Porque la
versión recién se conoce **después** de que Clikt parsea: en el constructor
todavía no existe. Y eso mismo es el punto de sustitución de los tests — se
inyecta otra función, no se mockea un tipo.

**Cómo se comparte sin heredar.** Tres mecanismos, dos de la propia librería:

| Qué | Cómo | Por qué así |
|---|---|---|
| el argumento `<archivo>` | `sourceFileArgument()` | es extensión porque `argument()` registra el parámetro **en el comando que la llama**, así que necesita saber cuál es |
| la opción `--version` | `LanguageOptions : OptionGroup` | es el mecanismo de Clikt para agrupar opciones reutilizables |
| la orquestación del `run()` | `runOnSourceFile()` | extensión de nuevo: el receptor le da acceso al `echo` de Clikt |

**Qué es `by`.** Delegación de propiedad: no guarda un valor, se lo pide al
objeto que devolvió `sourceFileArgument()`. Y ese objeto, al crearse, **se
registró en el comando** — por eso Clikt genera el `--help` sin que nadie se lo
diga. Declarar el parámetro y documentarlo son el mismo acto.

### `RunSourceCommand.kt` — el flujo compartido

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

Abrir el archivo, armar las sentencias, dejar que el comando haga lo suyo,
reportar y terminar. Un archivo que no se puede abrir se modela como
`OperationOutcome.Failure`, igual que cualquier otro fracaso: mismo camino,
mismo código de salida.

En el mismo archivo vive `interpretationOutcome(...)`, lo único que comparten
validar y ejecutar. **Es una función y no una jerarquía a propósito:** el día que
validar deje de ejecutar el programa —por `readInput` de la 1.1— se borra una
función y no hay que desarmar nada.

**El `throw ProgramResult` es el único del módulo.** Clikt señaliza el código de
salida lanzando esa excepción: es su protocolo, no manejo de errores. Auditable
de un vistazo:

```bash
grep -rn "throw\|catch" cli/src/main --include=*.kt
```

### `OperationOutcome` y `ExitCode`

Dos vocabularios distintos a propósito:

| `OperationOutcome` (significado) | `ExitCode` (proceso) |
|---|---|
| `Success` | `SUCCESS(0)` |
| `CompletedWithFindings` | `FINDINGS(3)` |
| `Failure` | `SOURCE_ERROR(1)` |

**Por qué `FINDINGS(3)` existe.** Para que un CI pueda distinguir *"el código
está roto"* de *"el código funciona pero no respeta las convenciones"*.

## 7.3 El toolchain

```kotlin
internal class PrintScriptToolchain(
    val statementsFrom: (SourceReader) -> StatementSource,
    val interpreterWriting: (ProgramOutput) -> Interpreter,
    val formatter: () -> Formatter,
    val linter: () -> Linter,
)
```

Las cuatro herramientas de una versión del lenguaje, ya cableadas. Es una clase
concreta y **no una interfaz**: la variación entre versiones está en qué
factories se usan para armarla, no en cómo se comporta. Por eso el punto de
sustitución es la función que la construye.

Las herramientas son lambdas para que la configuración concreta de cada versión
quede capturada adentro — así el tipo no nombra nada de V1.

```kotlin
internal object PrintScriptToolchainFactory {

    fun forVersion(version: LanguageVersion): PrintScriptToolchain {
        return when (version) {
            LanguageVersion.V1_0 -> printScriptV1Toolchain()
        }
    }
}
```

**Es el único archivo del CLI que nombra las factories de los módulos.** Antes
esa decisión estaba partida en cuatro, y en tres de esos casos la versión ni
siquiera se consultaba: `StatementSourcePipeline` elegía lexer y parser según la
versión, pero el intérprete, el formatter y el linter estaban clavados en V1.

El `when` es exhaustivo. Cuando se agregue una versión al enum, **este archivo
deja de compilar** y el compilador obliga a decidir con qué herramientas se arma.

## 7.4 Qué hace cada comando

### `ExecutionCommand` y `ValidationCommand`

Los dos corren el intérprete y comparten `interpretationOutcome(...)`. Difieren
en dos cosas nada más:

| | `ValidationCommand` | `ExecutionCommand` |
|---|---|---|
| a dónde va lo que el programa imprime | se descarta | a la salida estándar |
| qué se informa al terminar bien | *"El archivo es válido."* | nada |

Cada uno arma su `ProgramOutput` como objeto anónimo:

```kotlin
// ExecutionCommand
private fun terminalOutput(): ProgramOutput {
    return object : ProgramOutput {
        override fun writeLine(line: String) {
            echo(line)
        }
    }
}

// ValidationCommand
private fun discardedOutput(): ProgramOutput {
    return object : ProgramOutput {
        override fun writeLine(line: String) = Unit
    }
}
```

Ese segundo es un **Null Object**: cumple el contrato sin hacer nada. Gracias a
él, `validation` **corre el programa entero** —y por lo tanto detecta errores
semánticos— sin que el usuario vea lo que habría impreso.

> ⚠️ Eso va a cambiar con PrintScript 1.1. Cuando exista `readInput`, correr el
> programa para validarlo significaría **quedarse esperando entrada por
> teclado**. Es una decisión abierta del equipo, y por eso
> `interpretationOutcome` es una función suelta y fácil de borrar.

### `FormattingCommand` y `AnalysisCommand`

Los dos recorren su fuente con **recursión de cola**:

```kotlin
private tailrec fun writeRemainingFormattedStatements(source: FormattedSource): OperationOutcome {
    return when (val readResult = source.nextFormattedStatement()) {
        FormattedStatementReadResult.EndOfInput -> OperationOutcome.Success

        is FormattedStatementReadResult.Failure ->
            OperationOutcome.Failure(errorReporter.describe(readResult.error))

        is FormattedStatementReadResult.Success -> {
            echo(readResult.formattedText, trailingNewline = false)

            writeRemainingFormattedStatements(readResult.remainingSource)
        }
    }
}
```

`tailrec` le pide al compilador que convierta la recursión en un bucle: se lee
como recursión pero no consume stack.

En `AnalysisCommand` el contador de hallazgos viaja **como parámetro** y no como
campo, así el comando no guarda estado entre corridas — se puede llamar dos veces
seguidas y el conteo arranca de cero.

> **Sobre SRP.** Estos dos comandos declaran opciones de CLI **y** contienen el
> bucle. Es una responsabilidad más gorda de lo ideal, y fue una decisión
> consciente: lo que absorbieron es orquestación —iterar e imprimir—, no reglas.
> Qué es un diagnóstico sigue en el módulo `linter`, y cómo se redacta sigue en
> `DiagnosticReporter`.

## 7.5 El reporte

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
| 2 | Clikt | parsea `argv`, encuentra `execution`, llena `sourceFilePath` y `languageOptions` |
| 3 | `ExecutionCommand.run()` | `toolchainFor(V1_0)` → el toolchain de PrintScript 1.0 |
| 4 | `runOnSourceFile(...)` | la extensión compartida por los cuatro comandos |
| 5 | `SourceReaderFactory.fromPath` | existe, es archivo, es legible → `Success(reader)` |
| 6 | `toolchain.statementsFrom(reader)` | lexer + parser → `StatementSource` |
| 7 | `ExecutionCommand` | arma el `ProgramOutput` que escribe con `echo` |
| 8 | `toolchain.interpreterWriting(output)` | el intérprete de 1.0, cableado a esa salida |
| 9 | `interpretationOutcome(...)` | corre el programa; cada `println` llega a la terminal |
| 10 | | `InterpretationResult.Success` → `OperationOutcome.Success` |
| 11 | `reportOutcome` | no imprime nada, porque fue Success |
| 12 | `exitCodeOf` | `Success` → `ExitCode.SUCCESS` |
| 13 | `runOnSourceFile` | es SUCCESS → **no lanza nada**, vuelve normal |
| 14 | Clikt | `run()` terminó sin excepción → sale con 0 |

Si en el paso 9 el programa hubiera usado una variable inexistente:

| # | Dónde | Qué pasa |
|---|---|---|
| 9' | `DefaultExpressionEvaluator` | `lookupBinding("x")` → `null` → `Failure(UndeclaredVariable)` |
| 10' | `ConfigurableInterpreter` | `Finished(SemanticFailure(error))` |
| 11' | `interpretationOutcome` | `errorReporter.describe(error)` → texto en castellano con posición |
| 12' | | `OperationOutcome.Failure(mensaje)` |
| 13' | `reportOutcome` | `echo(mensaje, err = true)` → va a **stderr** |
| 14' | `exitCodeOf` | `Failure` → `ExitCode.SOURCE_ERROR` |
| 15' | `runOnSourceFile` | no es SUCCESS → `throw ProgramResult(1)` |
| 16' | Clikt | atrapa, sale con 1 |

---

# 8. Glosario

**AST** *(Abstract Syntax Tree)* — árbol de sintaxis abstracta. La
representación del programa como estructura anidada en vez de texto plano.

**Adapter** — patrón. Una clase que traduce una interfaz a otra. El
`ProgramOutput` anónimo de `ExecutionCommand` adapta el `echo` de Clikt al
contrato de salida que espera el intérprete.

**api vs implementation** — en Gradle, `api` significa que el tipo aparece en tu
firma pública y se re-expone a quien te use; `implementation` significa que lo
usás solo adentro y queda escondido.

**Command-Query Separation** — principio: una función o hace algo (comando) o
responde algo (consulta), nunca las dos.

**Decorator** — patrón. Envolver un objeto para agregarle comportamiento sin
modificarlo. Cuando PrintScript 1.1 necesite evaluar expresiones nuevas, su
evaluador va a decorar al de 1.0 en vez de reescribirlo.

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
