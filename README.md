# PrintScript

Implementación modular de **PrintScript 1.0** en Kotlin/JVM 21.

El lenguaje se procesa mediante un pipeline pull y lazy. Cada etapa solicita el
siguiente elemento cuando lo necesita y entrega también la fuente que representa
el resto de la entrada. De esta forma no se materializan listas completas de
tokens ni de sentencias.

## Requisitos

- **JDK 21.** La versión de la JVM que usa Gradle está fijada en
  `gradle/gradle-daemon-jvm.properties`, de modo que el build es reproducible
  sin importar qué JDK tenga instalado cada integrante.

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

El ejecutable queda en `cli/build/install/printscript/bin/printscript`.

```bash
printscript validation ejemplo.ps   # ¿el archivo es válido?
printscript execution  ejemplo.ps   # correlo
printscript formatting ejemplo.ps   # reescribilo con el formato configurado
printscript analysis   ejemplo.ps   # reportá problemas de estilo
```

Las cuatro operaciones aceptan las mismas opciones:

| Opción | Qué hace |
|---|---|
| `--version` | Versión del lenguaje. Hoy solo `1.0`, que es el default. |
| `--help` | Ayuda. Disponible también por operación: `printscript formatting --help`. |

### Códigos de salida

| Código | Significado |
|---|---|
| `0` | La operación terminó bien. |
| `1` | El archivo no se pudo leer, o tiene un error léxico, sintáctico o semántico. También los errores de uso. |
| `3` | El análisis encontró problemas de estilo. El archivo es válido. |

El `3` está separado del `1` a propósito: permite que un CI distinga *"el código
está roto"* de *"el código funciona pero no respeta las convenciones"*.

### Dependencias externas

`cli` es el único módulo que depende de una librería de terceros:
[Clikt 5.1.0](https://github.com/ajalt/clikt), que resuelve el parseo de
argumentos, la generación del `--help` y el autocompletado de shell. Entra como
`implementation`, así que no se propaga: los módulos motor siguen sin
dependencias externas.

## Pipeline

```text
código fuente
    │
    ▼
SourceReader → Lexer → TokenSource → Parser → StatementSource → Interpreter → ProgramOutput
                                                             ├→ Formatter
                                                             └→ Linter
```

- `SourceReader` entrega el código en bloques.
- `TokenSource` produce un token por solicitud.
- `StatementSource` produce una sentencia por solicitud.
- El interpreter consume y ejecuta las sentencias en orden.
- El formatter y el linter consumen la misma `StatementSource`: son
  consumidores alternativos del mismo pipeline, no etapas nuevas.
- La CLI arma el pipeline, elige el consumidor según la operación pedida y
  traduce el resultado a un código de salida.

Los resultados exitosos transportan la fuente restante en lugar de modificar la
fuente actual. Los errores léxicos, sintácticos y semánticos se representan como
resultados de dominio y no mediante excepciones.

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
| `printscript-v1` | Reglas y composición concreta de los componentes de PrintScript V1. |
| `cli` | Aplicación de línea de comandos. Único módulo con dependencias externas. |
| `integration-tests` | Pruebas de caja negra del pipeline completo. |

Los módulos se conectan mediante interfaces pequeñas. Las implementaciones
concretas son internas y cada versión se construye a través de una factory
pública:

```kotlin
PrintScriptV1LexerFactory.create()
PrintScriptV1ParserFactory.create()
PrintScriptV1FormatterFactory.create()
PrintScriptV1InterpreterFactory.create(output)
PrintScriptV1LinterFactory.create()
```

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

El formatter core consume `StatementSource` de forma lazy y coordina estrategias
públicas de formateo. El primer `StatementFormatter` compatible tiene prioridad,
por lo que un consumidor puede extender o reemplazar reglas sin modificar el
motor. `StatementSeparationPolicy` decide solamente qué texto insertar antes de
cada sentencia.

La configuración de espacios, los formatters de declaraciones, asignaciones,
`println` y expresiones, y la política concreta de separación pertenecen a
`printscript-v1`. La jerarquía `Expression` de `printscript-ast` permanece
sellada y no forma parte de los contratos extensibles del formatter core.

### Interpreter

El interpreter core consume `StatementSource` de forma lazy y coordina
`StatementExecutor<S>` públicos. El estado es genérico únicamente en este punto
de variación: todos los executors configurados deben aceptar y producir el mismo
tipo, por lo que el compilador impide mezclar estrategias de lenguajes distintos.
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

### CLI

La CLI está partida en dos capas. El **dominio** —`SourceOperation` y sus cuatro
implementaciones, `SourceOperationRunner`, los reporters— recibe sentencias y
una terminal, y devuelve un `OperationOutcome`; no conoce `argv`, ni los flags,
ni la consola. El **adaptador** son los cuatro comandos de Clikt.

Esa frontera es la que permite que el parseo de argumentos sea una librería en
lugar de código propio, y que las cuatro operaciones se testeen sin línea de
comandos.

Los cuatro comandos heredan **directamente de `CliktCommand`**: no hay clase base
propia. Lo que comparten se reutiliza por composición, con los mecanismos que la
propia librería ofrece para eso:

| Qué se comparte | Cómo |
|---|---|
| el argumento `<archivo>` | la extensión `sourceFileArgument()` |
| la opción `--version` | el `OptionGroup` `LanguageOptions` |
| la orquestación del `run()` | la extensión `runSourceOperation()` |
| qué operación montar | la `SourceOperationFactory` inyectada |

Se eligió composición sobre herencia por un motivo concreto: con una clase base,
los cuatro comandos quedaban obligados a exponer **exactamente las mismas
opciones**. Declarándolas por comando, cada uno puede tener las suyas — algo que
va a hacer falta cuando vuelva `--config`, que corresponde solo a `formatting` y
`analysis`.

`PrintScriptCommandFactory` es la raíz de composición y el único lugar donde se
instancian los comandos. Producción y tests la comparten: si cada uno armara la
suya, un test podría quedar en verde verificando un CLI distinto del que se
distribuye.

Dos decisiones deliberadas en contra de lo que ofrece la librería:

- **No usamos la validación de archivos de Clikt** (`path(mustExist = true)`).
  La existencia y los permisos los sigue verificando `SourceReaderFactory`, para
  conservar los mensajes de error en castellano y con la posición en el código.
- **El único `throw` del módulo vive en `ProgramTermination`.** Clikt señaliza
  el código de salida lanzando `ProgramResult`: es su protocolo, no manejo de
  errores. El dominio sigue devolviendo resultados y nunca lanza ni atrapa.

`Terminal` abstrae la salida: `EchoTerminal` la manda a Clikt en producción y
`DiscardedProgramOutput` la descarta cuando `validation` corre el programa sin
mostrar lo que imprimiría.

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

`check` es la orden que corre todo: compila, verifica formato, ejecuta el
análisis estático, corre los tests y valida el umbral de cobertura.

La configuración compartida de Kotlin, Java 21, tests y herramientas de calidad
vive en convention plugins dentro de `buildSrc`:

- `printscript.kotlin-library` — todos los módulos de librería.
- `printscript.kotlin-application` — módulos con `main`.

Las pruebas de cada módulo validan sus propias responsabilidades y
`integration-tests` verifica el flujo completo desde el código fuente hasta la
salida o el error correspondiente.

## Herramientas de desarrollo

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

`.editorconfig` está versionado y lo leen tanto ktlint como el IDE. Se apartan
dos valores del default:

- `ktlint_code_style = intellij_idea` en lugar de `ktlint_official`, para que el
  formateo del IDE y el de ktlint coincidan.
- `max_line_length = 120` en lugar de 140.

### Análisis estático — detekt

`config/detekt/detekt.yml` parte de la configuración por defecto
(`buildUponDefaultConfig = true`) y ajusta las reglas que el equipo considera
relevantes, cada una con su motivo documentado en el archivo. Las principales:

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
