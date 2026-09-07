# PrintScript

Implementación modular de **PrintScript** en Kotlin/JVM 21, con soporte para
las versiones `1.0` y `1.1`.

El lenguaje se procesa mediante un pipeline pull y lazy. Cada etapa solicita el
siguiente elemento cuando lo necesita y entrega también la fuente que representa
el resto de la entrada. Los tokens y las sentencias de nivel superior se
consumen incrementalmente; el AST de cada bloque sí contiene sus sentencias.

## Requisitos

- **JDK 21.** La JVM de Gradle se selecciona mediante
  `gradle/gradle-daemon-jvm.properties`, y los convention plugins configuran
  la toolchain de compilación. Debe haber una instalación compatible disponible.

En Windows, usar `.\gradlew.bat` en lugar de `./gradlew` en los comandos siguientes.

## Puesta en marcha

Después de clonar el repositorio, una sola vez:

```bash
./gradlew installHooks
```

Eso instala el hook de pre-commit versionado en `.githooks/`. Ver
[Herramientas de desarrollo](#herramientas-de-desarrollo).

## Uso

```bash
./gradlew :cli:installDist
```

El ejecutable queda en `cli/build/install/printscript/bin/printscript`
(`printscript.bat` en Windows). Los ejemplos siguientes suponen que ese directorio
está en el `PATH` o que se invoca el ejecutable mediante su ruta completa.

```bash
printscript validation ejemplo.ps   # ¿el archivo es válido?
printscript execution  ejemplo.ps   # correlo
printscript formatting ejemplo.ps   # muestra el código formateado
printscript analysis   ejemplo.ps   # reportá problemas de estilo
```

Las cuatro operaciones aceptan `--version`. `formatting` y `analysis` también
aceptan `--config` con la ruta de un archivo JSON.

| Opción | Qué hace |
|---|---|
| `--version` | Versión del lenguaje: `1.0` o `1.1`. El valor por defecto es `1.0`. |
| `--config` | Configuración JSON para `formatting` o `analysis`. |
| `--help` | Ayuda. Disponible también por operación: `printscript formatting --help`. |

### Códigos de salida

| Código | Significado |
|---|---|
| `0` | La operación terminó bien. |
| `1` | Error de lectura, configuración, formateo, parsing o semántica detectado por la operación. |
| `3` | El análisis terminó con diagnósticos de estilo. No certifica validez semántica. |

Los errores de argumentos los gestiona Clikt con sus propios códigos de salida.
`analysis` revisa estilo sobre el AST; para comprobar semántica se utiliza
`validation`. Un código `3` permite distinguir hallazgos de estilo de un fallo
de la operación.

### Dependencias externas

`cli` depende de [Clikt 5.1.0](https://github.com/ajalt/clikt), que resuelve el
parseo de argumentos, la ayuda y la interacción con la terminal.

`printscript-v1` depende de
[kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) para
leer la configuración JSON del formatter y del linter. Es la única librería
JSON del proyecto: no hay un decoder genérico compartido, cada reader interpreta
sus propias claves.

Ambas dependencias entran como `implementation`: no forman parte de la API de
compilación que se expone al consumidor, aunque sí se necesitan en runtime.
Los motores no dependen directamente de Clikt ni de kotlinx.serialization.
El build y los tests usan además las herramientas declaradas en `buildSrc`.

## Pipeline

```text
SourceReader
    ├─ Lexer → TokenSource → Parser → StatementSource
    │                                    ├─ Interpreter → ProgramOutput
    │                                    ├─ Linter → DiagnosticSource
    │                                    └─ Validator → ValidationResult
    └─ Lexer con whitespace → TokenSource → Formatter → FormattedSource
```

- `SourceReader` entrega en bloques código proveniente de strings, archivos o
  streams de entrada.
- `TokenSource` produce un token por solicitud.
- `StatementSource` produce una sentencia por solicitud.
- El interpreter consume y ejecuta las sentencias en orden.
- El interpreter, el linter y el validator consumen el contrato `StatementSource`.
  Cada operación arma su propia fuente; no se comparte un stream consumible entre
  operaciones. El validator revisa ambas ramas de cada `if` sin ejecutar I/O.
- El formatter consume tokens que conservan whitespace; no utiliza parser ni AST.
- La CLI arma el pipeline, elige el consumidor según la operación pedida y
  traduce el resultado a un código de salida.

Los resultados exitosos transportan la fuente restante en lugar de modificar la
fuente actual. Los errores léxicos, sintácticos y semánticos se representan como
resultados de dominio y no mediante excepciones.

Las fuentes reproducibles, como strings y archivos, conservan estados
inmutables. Un `InputStream` es una fuente no reproducible: su reader encapsula
el avance del recurso y debe consumirse linealmente usando siempre el estado
restante. El stream sigue siendo propiedad de quien lo creó y el reader no lo
cierra.

## Módulos

| Módulo | Responsabilidad |
|---|---|
| `common` | Posiciones y rangos dentro del código fuente. |
| `source-reader` | Contrato y lectura por bloques del código fuente. |
| `token-source` | Tokens, errores léxicos y contrato entre lexer y parser. |
| `lexer` | Motor lazy de tokenización y contratos públicos para scanners externos. |
| `statement-source` | Contrato abierto de sentencia, errores sintácticos y fuente lazy entre parser y consumidores. |
| `printscript-ast` | AST oficial e inmutable compartido por las versiones de PrintScript. |
| `parser` | Motor lazy de parsing y contratos públicos para estrategias externas. |
| `interpreter` | Motor de interpretación y contratos públicos para executors externos. |
| `printscript-runtime` | Estado, valores y puertos públicos para extender la ejecución de PrintScript. |
| `formatter` | Motor lazy de formateo y contratos públicos para estrategias externas. |
| `linter` | Motor lazy de análisis de estilo y contratos públicos para reglas externas. |
| `printscript-v1` | Tokens, gramáticas, reglas y factories de `1.0` y `1.1`, incluyendo el validator estático. V1.1 agrega `if`, `const`, `boolean`, `readInput` y `readEnv`. |
| `cli` | Composición exterior, archivos, terminal, configuración y selección de versión mediante Clikt. |
| `integration-tests` | Pruebas de caja negra del pipeline completo. |

Los módulos se conectan mediante contratos pequeños. Los motores concretos son
internos; las factories ensamblan cada versión. Algunas reglas públicas del linter
se ofrecen también como componentes de composición:

```kotlin
PrintScriptV1LexerFactory.create()
PrintScriptV1FormattingLexerFactory.create()
PrintScriptV1ParserFactory.create()
PrintScriptV1FormatterFactory.create()
PrintScriptV1InterpreterFactory.create(output)
PrintScriptV1LinterFactory.create()
PrintScriptV1ValidatorFactory.create()
```

Cada factory tiene su par `PrintScriptV11...`. Las versiones comparten
componentes y agregan su composición específica (ver
`PrintScriptV11ParserFactory`, que reutiliza `println` y la asignación de `1.0`
en lugar de reconstruir su lista completa de parsers).

Los diagramas editables están en [módulos](docs/diagrams/modulos.puml),
[flujo](docs/diagrams/flujo.puml) y [contratos](docs/diagrams/estructura.puml).
Los planes de refactor del CLI en `docs/` están marcados como históricos.

## Decisiones de diseño

### Lectura y lexer

El código fuente se procesa en bloques. El lexer core mantiene un cursor
inmutable y delega el reconocimiento mediante contratos públicos para scanners.
Las reglas concretas de identificadores, literales y símbolos de V1 viven en
`printscript-v1`. El siguiente token solamente se calcula cuando el consumidor
lo solicita.

### Parser

El parser core coordina estrategias públicas de sentencias y ofrece un motor
genérico de expresiones por niveles de precedencia. El tipo producido por ese
motor es configurable; PrintScript V1 lo especializa con la jerarquía sellada
`Expression` de `printscript-ast` y mantiene su gramática concreta en
`printscript-v1`.

Las sentencias externas pueden implementar el contrato abierto `Statement`. El
dispatcher conserva el orden configurado y da prioridad a la primera estrategia
compatible. Reglas como `;`, paréntesis y tipos declarados pertenecen a los
parsers concretos de V1, no al motor.

El contrato `Statement` permanece abierto en `statement-source` para que un
consumidor externo pueda aportar sentencias propias. Los nodos oficiales de
PrintScript viven en `printscript-ast` y se comparten entre versiones. Una
factory de versión decide qué subconjunto puede construir; el AST no conoce la
versión. `Expression` permanece sellada para que agregar una expresión oficial
obligue al compilador a señalar todos los consumidores exhaustivos.

Cada operación devuelve un nuevo contexto de parsing. Ante un error se entrega un
resultado terminal y el consumidor debe detener la lectura.

### Formatter

El formatter core consume un `TokenSource` lossless de forma lazy y no tiene
noción de "sentencia": trabaja a nivel de `TokenGap`, el whitespace original
entre dos tokens consecutivos. Cada `TokenGapFormattingRule` decide si le
interesa un gap (`supports`) y, si le interesa, produce un
`WhitespaceFormattingResult` — `Success(whitespace)` o `Failure`, para que un
desbordamiento durante el formateo se reporte como error de dominio. Los readers
JSON rechazan configuraciones inválidas; los constructores de configuración
comprueban sus invariantes con `require`. Cuando ninguna regla aplica, el
whitespace original se preserva tal cual.

El dispatcher aplica la primera regla compatible. Las reglas externas tienen
prioridad sobre las reglas incluidas por la factory. Dentro de V1.1,
`IndentedFormattingRule` selecciona una regla base de llaves, saltos o espaciado
y luego aplica indentación sobre el whitespace resultante. También conserva e
indenta los saltos existentes cuando corresponde. Los saltos específicos de
`println` tienen prioridad sobre los generales después de un statement.

Las reglas reciben el whitespace emitido mediante `afterFormatting` y el token
consumido mediante `afterConsuming`, incluso si otra regla ganó la selección.
Devuelven el nuevo estado; esto permite alinear llaves con la salida real y
reconocer los límites de los bloques sin mutar las reglas oficiales.

El lexer normal de V1 continúa descartando whitespace antes del parser.
`PrintScriptV1FormattingLexerFactory` crea la variante que lo conserva para el
formatter. Las reglas concretas de espacios, saltos de línea e indentación
pertenecen a `printscript-v1`; el parser y el AST no participan del formateo.

### Interpreter

El interpreter core consume `StatementSource` de forma lazy y coordina
`StatementExecutor<S>` públicos. El estado es genérico únicamente en este punto
de variación: todos los executors configurados deben aceptar y producir el mismo
tipo. Eso garantiza compatibilidad del estado, no compatibilidad semántica entre
lenguajes; la selección de reglas corresponde a la factory.
El `Interpreter` que usa la CLI permanece no genérico.

El dispatcher conserva el orden configurado y da prioridad al primer executor
compatible. `StatementExecutionContext<S>` permite ejecutar sentencias anidadas
con el mismo motor y propagar estados nuevos sin mutar los anteriores.

`printscript-runtime` contiene el environment inmutable, los bindings, los
valores oficiales y los puertos compartidos de evaluación y salida. Sus
contratos permiten crear executors compatibles con PrintScript sin depender de
la implementación completa de V1. Los executors, el evaluador concreto y los
errores semánticos del lenguaje permanecen en `printscript-v1`. La salida se
abstrae mediante `ProgramOutput`, por lo que tampoco depende de la consola ni de
archivos.

`Environment` permite **shadowing** entre scopes: una declaración solo choca
con otra del mismo scope (`findBindingInCurrentScope`); una variable de un
scope exterior con el mismo nombre no es un error, es una sombra nueva que
`leaveScope()` descarta al volver. `Environment` en sí mismo solo guarda
bindings — no valida tipos ni constantes; esas comprobaciones son
responsabilidad de los executors concretos de `printscript-v1`.

Quien extienda la ejecución debe comprobar duplicados antes de `declare` y
existencia, mutabilidad y tipos antes de `reassign`. Se reasigna el binding
visible más cercano. No se puede abandonar el scope global. El evaluador y
`IfExecutor` comparten la resolución de variables inicializadas; la condición
del `if` mantiene además su comprobación de tipo booleano.

### Validation

`validation` reutiliza el mismo motor de `interpreter`, pero con
`StatementExecutor<ValidationEnvironment>` que nunca ejecutan de verdad: no
imprimen, no leen entrada y no consultan variables de entorno. Su
`IfValidator` es la diferencia clave con la ejecución real — recorre **las dos
ramas** de todo `if` sin importar el valor de la condición e intersecta el
estado de inicialización resultante, así que un error semántico en la rama que
la ejecución real nunca toma igual se reporta. `validation` y `execution`
comparten el mismo parser y el mismo AST; solo cambia qué `StatementExecutor`
se conecta al motor genérico.

Una variable se considera inicializada después de un `if` si ya lo estaba antes
o si ambas ramas la inicializan. Sin `else`, se contempla el camino que no entra
al bloque. Los errores que dependen de valores reales, como división por cero,
entrada inválida o variables del sistema ausentes, quedan para `execution`.

### Linter y API de composición

El core aplica todas las reglas configuradas mediante `CompositeRule`. Cada
regla devuelve diagnósticos y su estado siguiente en `RuleInspection`. V1.1
reutiliza esa composición y recorre las dos ramas de los bloques; no selecciona
una rama según el valor de su condición.

Estas clases de `printscript.v1.linter.rule` son API pública de composición:

| Regla | Responsabilidad |
|---|---|
| `PrintScriptV1IdentifierNamingRule` | Comprueba el nombre de las variables declaradas. |
| `PrintScriptV1PrintlnArgumentRule` | Comprueba la categoría del argumento de `println`. |
| `PrintScriptV11ReadInputArgumentRule` | Encuentra llamadas a `readInput` en las expresiones de una sentencia y comprueba sus prompts. |

Se conservan públicas para que un consumidor pueda construir una política propia
sin copiar las reglas. Las dos reglas de argumentos reciben un mapa que debe
cubrir `LITERAL`, `VARIABLE` y `COMPOSED`; copian el mapa recibido. Las expresiones
agrupadas, operaciones y llamadas pertenecen a `COMPOSED`.

Ejemplo de composición para V1.1, con el recorrido de bloques provisto por su factory:

```kotlin
import printscript.v1.linter.PrintScriptV11LinterConfiguration
import printscript.v1.linter.PrintScriptV11LinterFactory
import printscript.v1.linter.PrintScriptArgumentAcceptance
import printscript.v1.linter.PrintScriptExpressionKind
import printscript.v1.linter.PrintScriptV1NamingConvention
import printscript.v1.linter.rule.PrintScriptV1IdentifierNamingRule
import printscript.v1.linter.rule.PrintScriptV1PrintlnArgumentRule
import printscript.v1.linter.rule.PrintScriptV11ReadInputArgumentRule

val argumentPolicy = mapOf(
    PrintScriptExpressionKind.LITERAL to PrintScriptArgumentAcceptance.ACCEPTED,
    PrintScriptExpressionKind.VARIABLE to PrintScriptArgumentAcceptance.ACCEPTED,
    PrintScriptExpressionKind.COMPOSED to PrintScriptArgumentAcceptance.REJECTED,
)
val linter = PrintScriptV11LinterFactory.create(
    configuration = PrintScriptV11LinterConfiguration(rules = emptyList()),
    additionalRules = listOf(
        PrintScriptV1IdentifierNamingRule(PrintScriptV1NamingConvention.CAMEL_CASE),
        PrintScriptV1PrintlnArgumentRule(argumentPolicy),
        PrintScriptV11ReadInputArgumentRule(argumentPolicy),
    ),
)
```

`linter.lint(statements)` devuelve un `DiagnosticSource`; se consume hasta
`EndOfInput` o `Failure`. Las reglas adicionales se ejecutan antes de las
configuradas, pero no las reemplazan: todas participan. El ejemplo usa una
configuración vacía para evitar agregar dos veces la misma comprobación.
Las reglas individuales inspeccionan una sentencia; la factory V1.1 aporta el
recorrido de los bloques. Para otros árboles o lenguajes, ese recorrido lo
define su composición.

### Configuración JSON

Sin `--config`, el formatter preserva el whitespace original. El linter aplica
por defecto camel case y argumentos de `println` limitados a variables o
literales. Un JSON `{}` selecciona una configuración sin reglas, también en el
linter; no equivale a omitir `--config`.

Ejemplo de formatter V1.1:

```json
{
  "enforce-spacing-around-equals": true,
  "mandatory-space-surrounding-operations": true,
  "mandatory-line-break-after-statement": true,
  "line-breaks-after-println": 1,
  "if-brace-below-line": true,
  "indent-inside-if": 2
}
```

| Propiedad del formatter | Efecto |
|---|---|
| `enforce-spacing-around-equals` / `enforce-no-spacing-around-equals` | Agrega o elimina espacios alrededor de `=`; no pueden activarse juntas. |
| `enforce-spacing-before-colon-in-declaration` / `enforce-spacing-after-colon-in-declaration` | Espacio antes o después de `:`. |
| `mandatory-single-space-separation` | Separación de un espacio, subordinada a las reglas más específicas. |
| `mandatory-space-surrounding-operations` | Espacios alrededor de operadores binarios. |
| `mandatory-line-break-after-statement` | Un salto después de `;` cuando hay otro token. |
| `line-breaks-after-println` | Cantidad de líneas vacías: `0` produce un salto, `1` produce dos. |
| `if-brace-same-line` / `if-brace-below-line` | Ubicación de la llave de apertura del `if` en V1.1; son excluyentes. |
| `indent-inside-if` | Espacios por nivel de bloque en V1.1; ajusta líneas existentes o creadas por otras reglas. |

Ejemplo de linter V1.1:

```json
{
  "identifier_format": "camel case",
  "mandatory-variable-or-literal-in-println": true,
  "mandatory-variable-or-literal-in-readInput": true
}
```

`identifier_format` admite `camel case` o `snake case`. La propiedad de
`readInput` pertenece a V1.1. Cada reader rechaza claves desconocidas y tipos
JSON incorrectos, y conserva errores propios de su versión. Los readers del
linter comparten el armado de las reglas comunes. El CLI informa el motivo
específico de una configuración inválida.

### CLI

Los cuatro comandos heredan directamente de `CliktCommand`; no hay una clase
base propia. Lo compartido se reutiliza con funciones y grupos de opciones:

| Qué se comparte | Cómo |
|---|---|
| el argumento `<archivo>` | la extensión `sourceFileArgument()` |
| la opción `--version` | el `OptionGroup` `LanguageOptions` |
| la opción `--config` | la extensión `configurationFileOption()` |
| la lectura y resultado de una operación | `runOnSourceFile()` y `OperationOutcome` |

`PrintScriptCommandFactory` es la raíz de composición y el único lugar donde se
arma el grupo completo de comandos. Si un test construyera los comandos por su
cuenta en lugar de pasar por esta factory, podría quedar en verde verificando
un CLI distinto del que realmente se distribuye.

`PrintScriptToolchainFactory` concentra la selección de versión. Cada toolchain
agrupa lexer, parser, interpreter, validator, formatter y linter compatibles, por lo que
los comandos no necesitan conocer factories concretas de `1.0` o `1.1`.

La entrada, salida y consulta de variables de entorno se adaptan en la CLI a los
contratos de `printscript-runtime`. `validation` reutiliza el motor genérico con
estado simbólico y reglas de validación, sin los executors que realizan I/O.

## Gramática de PrintScript 1.0

```ebnf
program     = { statement } ;

statement   = declaration
            | assignment
            | println ;

declaration = "let" IDENTIFIER ":" type [ "=" expression ] ";" ;
assignment  = IDENTIFIER "=" expression ";" ;
println     = "println" "(" expression ")" ";" ;

type        = "number" | "string" ;

expression     = additive ;
additive       = multiplicative { ( "+" | "-" ) multiplicative } ;
multiplicative = unary { ( "*" | "/" ) unary } ;
unary          = ( "+" | "-" ) unary
               | primary ;
primary        = NUMBER_LITERAL
               | STRING_LITERAL
               | IDENTIFIER
               | "(" expression ")" ;
```

El interpreter valida declaraciones, inicializaciones, asignaciones, tipos,
operandos y división por cero. Los errores incluyen el rango correspondiente del
código fuente.

`1.1` extiende esta gramática: agrega `const`, el tipo `boolean` con sus
literales `true`/`false`, `if`/`else` con bloques `{ }`, y las expresiones
`readInput(...)`/`readEnv(...)` como alternativas de `primary`. Reutiliza las
construcciones de V1, pero reserva keywords adicionales; un identificador de V1
que coincida con una de ellas puede dejar de ser válido en V1.1.

La condición de `if` es un identificador que debe resolver a booleano.
`const` requiere inicializador. Los bloques se utilizan en `if`/`else`; no hay
`else if` directo, aunque se puede anidar otro `if` dentro del `else`.

`readInput` recibe un prompt String y `readEnv` un nombre String. El texto leído
se convierte según el tipo esperado del destino; si no hay destino, se utiliza
String. Por eso `let n: number = readInput("n") * 2;` puede ejecutarse, mientras
`println(readInput("n") * 2);` falla por operandos incompatibles. El mismo
criterio se aplica a `readEnv`.

### Ejemplo

```typescript
let name: string = "world";
let count: number = 2 + 3 * 4;
println("hello " + name);
println(count);
```

Salida:

```text
hello world
14
```

## Build y tests

```bash
./gradlew build
./gradlew test
./gradlew check
```

`check` compila lo requerido para los tests, verifica formato, ejecuta el
análisis estático, corre los tests y valida el umbral de cobertura.

La configuración compartida de Kotlin, Java 21, tests y herramientas de calidad
vive en convention plugins dentro de `buildSrc`:

- `printscript.kotlin-library` — todos los módulos de librería.
- `printscript.kotlin-application` — módulos con `main`.
- `printscript.publishable-library` — librerías que también generan artefactos Maven.

Las pruebas de cada módulo validan sus propias responsabilidades y
`integration-tests` verifica el flujo completo desde el código fuente hasta la
salida o el error correspondiente. Incluye InputStream con buffers pequeños,
UTF-8, compatibilidad V1/V1.1 y una extensión que atraviesa parser e interpreter.
Hay tests de consumo desde Java y de inmutabilidad de las colecciones del AST.
El TCK es un repositorio separado y no forma parte de este `check`.

## Herramientas de desarrollo

### CI y publicación

El workflow de CI ejecuta `check` en pull requests y admite ejecución manual.
El workflow de publicación se dispara al publicar una GitHub Release con tag
`vX.Y.Z`: toma esa versión, ejecuta `check`, valida las publicaciones con
`publishToMavenLocal` y publica en GitHub Packages.

Las librerías aplican `printscript.publishable-library`; `cli` e
`integration-tests` no se publican. `printscript-v1` es la fachada que expone
transitivamente los motores y contratos. La versión local predeterminada es
`1.0.0-SNAPSHOT`, reemplazable mediante `-PreleaseVersion=X.Y.Z`.
Las credenciales de publicación son `GITHUB_ACTOR` y `GITHUB_TOKEN`.

Este mecanismo no indica que exista una release actualizada con todos los
cambios de `main`. Elegir y publicar el artefacto final, conectarlo al TCK y
calibrar su heap en la Action de los profesores son tareas de entrega separadas.

### Herramientas locales

| Herramienta | Responde | Configuración |
|---|---|---|
| **ktlint** | ¿el código se ve como acordamos? | `.editorconfig` |
| **detekt** | ¿hay algo mal escrito? | `config/detekt/detekt.yml` |
| **JaCoCo** | ¿qué partes no ejercitan los tests? | convention plugin |
| **git hooks** | ¿cuándo corre todo lo anterior? | `.githooks/` |

```bash
./gradlew ktlintCheck      # verifica el formato
./gradlew ktlintFormat     # lo corrige
./gradlew detekt           # análisis estático
./gradlew test             # tests y reporte de cobertura
./gradlew check            # todo junto
```

Los reportes quedan en `<módulo>/build/reports/`.

### Formato — ktlint

`.editorconfig` está versionado y configura:

- `ktlint_code_style = intellij_idea`, para que el
  formateo del IDE y el de ktlint coincidan.
- `max_line_length = 120`.

### Análisis estático — detekt

`config/detekt/detekt.yml` parte de la configuración por defecto
(`buildUponDefaultConfig = true`) y ajusta las reglas que el equipo considera
relevantes. Las principales:

- `LongMethod` con umbral 60: el estilo de un argumento nombrado por línea infla
  el conteo de líneas sin agregar complejidad real.
- `ReturnCount` con máximo 5: el manejo de errores como valores produce un
  `return` por cada paso validado.
- `MagicNumber`, `FunctionNaming`, `VariableNaming`, `ClassNaming`,
  `NewLineAtEndOfFile` y `MatchingDeclarationName` activas.

### Cobertura — JaCoCo

El reporte se genera automáticamente al correr los tests y el umbral mínimo del
80 % se verifica dentro de `check`.

```bash
open interpreter/build/reports/jacoco/test/html/index.html
```

### Hooks — pre-commit

`.git/hooks/` no se versiona, así que el hook vive en `.githooks/` y se instala
con una tarea de Gradle:

```bash
./gradlew installHooks
```

Antes de cada commit se ejecutan `ktlintCheck` y `detekt`. Los tests quedan
fuera del hook a propósito, para que commitear no tarde minutos.

El hook es una comodidad local y voluntario —`git commit --no-verify` lo
saltea—, no una garantía para el equipo.
