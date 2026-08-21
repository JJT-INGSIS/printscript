# PrintScript

Implementación modular de **PrintScript 1.0** en Kotlin/JVM 21.

El lenguaje se procesa mediante un pipeline pull y lazy. Cada etapa solicita el
siguiente elemento cuando lo necesita y entrega también la fuente que representa
el resto de la entrada. De esta forma no se materializan listas completas de
tokens ni de sentencias.

## Pipeline

```text
código fuente
    │
    ▼
SourceReader → Lexer → TokenSource → Parser → StatementSource → Interpreter → ProgramOutput
```

- `SourceReader` entrega el código en bloques.
- `TokenSource` produce un token por solicitud.
- `StatementSource` produce una sentencia por solicitud.
- El interpreter consume y ejecuta las sentencias en orden.

Los resultados exitosos transportan la fuente restante en lugar de modificar la
fuente actual. Los errores léxicos, sintácticos y semánticos se representan como
resultados de dominio y no mediante excepciones.

## Módulos

| Módulo | Responsabilidad |
|---|---|
| `common` | Posiciones y rangos dentro del código fuente. |
| `source-reader` | Contrato y lectura por bloques del código fuente. |
| `token-source` | Tokens, errores léxicos y contrato entre lexer y parser. |
| `lexer` | Reconocimiento lazy de tokens mediante scanners configurables. |
| `statement-source` | AST, errores sintácticos y contrato entre parser e interpreter. |
| `parser` | Parser recursive descent y producción lazy de sentencias. |
| `interpreter` | Evaluación del AST, validaciones semánticas y salida del programa. |
| `integration-tests` | Pruebas de caja negra del pipeline completo. |

Los módulos se conectan mediante interfaces pequeñas. Las implementaciones
concretas son internas y cada versión se construye a través de una factory
pública:

```kotlin
PrintScriptLexerFactory.createV1()
PrintScriptParserFactory.createV1()
PrintScriptInterpreterFactory.createV1(output)
```

## Decisiones de diseño

### Lectura y lexer

El código fuente se procesa en bloques. El lexer mantiene un cursor inmutable y
delega el reconocimiento de identificadores, literales y símbolos en scanners
especializados. El siguiente token solamente se calcula cuando el consumidor lo
solicita.

### Parser

El parser utiliza recursive descent por niveles de precedencia para las
expresiones. Las sentencias se seleccionan mediante dispatchers según sus tokens
iniciales, incluyendo un segundo nivel para las sentencias que comienzan con un
identificador.

Cada operación devuelve un nuevo contexto de parsing. Ante un error se entrega un
resultado terminal y el consumidor debe detener la lectura.

### Interpreter

El interpreter despacha cada tipo de sentencia a su executor correspondiente. El
entorno de variables es inmutable: cada ejecución exitosa produce un entorno
nuevo que se utiliza para la siguiente sentencia.

La salida se abstrae mediante `ProgramOutput`, por lo que el módulo no depende de
la consola ni de archivos.

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
```

La configuración compartida de Kotlin, Java 21 y tests vive en un convention
plugin dentro de `buildSrc`. Las pruebas de cada módulo validan sus propias
responsabilidades y `integration-tests` verifica el flujo completo desde el
código fuente hasta la salida o el error correspondiente.
