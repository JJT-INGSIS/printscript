# Demostración del CLI

Estos casos cubren las funcionalidades principales de PrintScript y están
preparados para ejecutarse durante la presentación.

## Preparación

Desde la raíz del repositorio:

```powershell
.\gradlew.bat :cli:installDist
```

El ejecutable utilizado en los ejemplos queda en:

```text
cli\build\install\printscript\bin\printscript.bat
```

Para recorrer automáticamente todos los casos:

```powershell
.\examples\demo\run-all.ps1
```

El script informa el caso antes de ejecutarlo y comprueba también los códigos
de salida esperados. No modifica los programas fuente.

## Casos incluidos

| Caso | Qué demuestra | Resultado esperado |
|---|---|---|
| Ejecución 1.0 | Declaración sin inicializar, asignación, aritmética, precedencia, concatenación y `println` | Imprime `Resultado: 14` y `División: 3.5` |
| Ejecución 1.1 | `const`, booleanos e `if`/`else` | Imprime `PrintScript 1.1` |
| `readInput` | Conversión de entrada a `string`, `number` y `boolean` | Con `Ada`, `4` y `true`, imprime el nombre, `5` y la rama verdadera |
| `readEnv` | Lectura y conversión de variables del ambiente a los tres tipos | Imprime el nombre, `8081` y la rama verdadera |
| Validación 1.1 | Valida sin ejecutar I/O | Informa que el archivo es válido sin pedir entrada |
| Rama no ejecutada | Diferencia entre ejecución y validación estática | Ejecutar funciona; validar detecta el error del `else` |
| Compatibilidad de versión | Una construcción 1.1 no pertenece a 1.0 | Falla con `1.0` y valida con `1.1` |
| Formatter | Todas las reglas principales y bloques anidados | Muestra el código formateado sin sobrescribir el archivo |
| Formatter alternativo | Configuración sin espacios alrededor de `=` | Muestra las asignaciones compactas |
| Formatter sin configuración | Preservación del whitespace original | La salida coincide con la entrada |
| Llave en otra línea | Configuración alternativa de bloques de 1.1 | Mueve la llave de apertura debajo del `if` |
| Linter | Naming, argumento de `println` y prompt de `readInput` | Reporta tres diagnósticos y termina con código `3` |
| Error sintáctico | Posición de un `;` faltante | Informa el error y termina con código `1` |
| Error semántico | División por cero durante la ejecución | Informa el error y termina con código `1` |

## Comandos individuales

Los siguientes comandos suponen PowerShell y la raíz del repositorio como
directorio actual:

```powershell
$cli = ".\cli\build\install\printscript\bin\printscript.bat"

& $cli execution .\examples\demo\execution\v1-basics.ps --version 1.0
& $cli execution .\examples\demo\execution\v1.1-language.ps --version 1.1

@("Ada", "4", "true") | & $cli execution .\examples\demo\execution\read-input.ps --version 1.1

$env:PRINTSCRIPT_DEMO_PORT = "8080"
$env:PRINTSCRIPT_DEMO_NAME = "producción"
$env:PRINTSCRIPT_DEMO_ENABLED = "true"
& $cli execution .\examples\demo\execution\read-env.ps --version 1.1

& $cli validation .\examples\demo\validation\valid-v1.1.ps --version 1.1

& $cli formatting .\examples\formatter\unformatted.ps --version 1.1 --config .\examples\demo\formatter\all-rules.json
& $cli formatting .\examples\demo\formatter\spaced-equals.ps --version 1.1 --config .\examples\demo\formatter\no-equals-spaces.json
& $cli formatting .\examples\demo\formatter\spaced-equals.ps --version 1.1
& $cli formatting .\examples\formatter\unformatted.ps --version 1.1 --config .\examples\formatter\v1.1-config.json

& $cli analysis .\examples\linter\style-violations.ps --version 1.1 --config .\examples\linter\v1.1-config.json
```

Los casos de linter con hallazgos terminan con código `3`. Los errores de
parsing, validación o ejecución terminan con código `1`; en la demostración son
resultados esperados, no fallas del script.

## Dos casos útiles para explicar

### La validación no ejecuta el programa

`valid-v1.1.ps` contiene `readInput` y `readEnv`. Al validarlo, no muestra el
prompt ni consulta el ambiente: solamente comprueba sintaxis y semántica.

### La validación revisa ambas ramas

`hidden-branch-error.ps` tiene una condición verdadera. La ejecución solo entra
al `if` y funciona, pero la validación también revisa el `else` y encuentra una
variable no declarada. Esto muestra la diferencia entre interpretar y validar.
