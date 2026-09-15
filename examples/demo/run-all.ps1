param(
    [string]$CliPath = ".\cli\build\install\printscript\bin\printscript.bat"
)

$resolvedCli = Resolve-Path -LiteralPath $CliPath -ErrorAction SilentlyContinue
if ($null -eq $resolvedCli) {
    Write-Error "No se encontró el CLI. Ejecutá primero: .\gradlew.bat :cli:installDist"
    exit 1
}

$cli = $resolvedCli.Path
$demoRoot = $PSScriptRoot
$repositoryRoot = (Resolve-Path -LiteralPath (Join-Path $demoRoot "..\..")).Path
$failedCases = 0

function Invoke-DemoCase {
    param(
        [string]$Title,
        [int]$ExpectedExitCode,
        [scriptblock]$Command
    )

    Write-Host ""
    Write-Host "=== $Title ===" -ForegroundColor Cyan
    & $Command
    $actualExitCode = $LASTEXITCODE

    if ($actualExitCode -ne $ExpectedExitCode) {
        Write-Host "Código esperado: $ExpectedExitCode; recibido: $actualExitCode" -ForegroundColor Red
        $script:failedCases++
    } else {
        Write-Host "Código de salida esperado: $actualExitCode" -ForegroundColor DarkGreen
    }
}

Push-Location $repositoryRoot
try {
    Invoke-DemoCase "Ejecución PrintScript 1.0" 0 {
        & $cli execution ".\examples\demo\execution\v1-basics.ps" --version 1.0
    }

    Invoke-DemoCase "Ejecución PrintScript 1.1: const, boolean e if/else" 0 {
        & $cli execution ".\examples\demo\execution\v1.1-language.ps" --version 1.1
    }

    Invoke-DemoCase "readInput: conversión a string, number y boolean" 0 {
        @("Ada", "4", "true") | & $cli execution ".\examples\demo\execution\read-input.ps" --version 1.1
    }

    Invoke-DemoCase "readEnv: conversión a string, number y boolean" 0 {
        $previousPort = $env:PRINTSCRIPT_DEMO_PORT
        $previousName = $env:PRINTSCRIPT_DEMO_NAME
        $previousEnabled = $env:PRINTSCRIPT_DEMO_ENABLED
        try {
            $env:PRINTSCRIPT_DEMO_PORT = "8080"
            $env:PRINTSCRIPT_DEMO_NAME = "producción"
            $env:PRINTSCRIPT_DEMO_ENABLED = "true"
            & $cli execution ".\examples\demo\execution\read-env.ps" --version 1.1
        } finally {
            $env:PRINTSCRIPT_DEMO_PORT = $previousPort
            $env:PRINTSCRIPT_DEMO_NAME = $previousName
            $env:PRINTSCRIPT_DEMO_ENABLED = $previousEnabled
        }
    }

    Invoke-DemoCase "Validación 1.1 sin ejecutar readInput ni readEnv" 0 {
        & $cli validation ".\examples\demo\validation\valid-v1.1.ps" --version 1.1
    }

    Invoke-DemoCase "Ejecución ignora la rama no alcanzada" 0 {
        & $cli execution ".\examples\demo\validation\hidden-branch-error.ps" --version 1.1
    }

    Invoke-DemoCase "Validación detecta el error en la rama no alcanzada" 1 {
        & $cli validation ".\examples\demo\validation\hidden-branch-error.ps" --version 1.1
    }

    Invoke-DemoCase "Una construcción 1.1 es rechazada por 1.0" 1 {
        & $cli validation ".\examples\demo\execution\v1.1-language.ps" --version 1.0
    }

    Invoke-DemoCase "La misma construcción es válida en 1.1" 0 {
        & $cli validation ".\examples\demo\execution\v1.1-language.ps" --version 1.1
    }

    Invoke-DemoCase "Formatter con reglas de espaciado, saltos, llaves e indentación" 0 {
        & $cli formatting ".\examples\formatter\unformatted.ps" --version 1.1 --config ".\examples\demo\formatter\all-rules.json"
    }

    Invoke-DemoCase "Formatter sin espacios alrededor de igual" 0 {
        & $cli formatting ".\examples\demo\formatter\spaced-equals.ps" --version 1.1 --config ".\examples\demo\formatter\no-equals-spaces.json"
    }

    Invoke-DemoCase "Formatter sin configuración preserva el whitespace" 0 {
        & $cli formatting ".\examples\demo\formatter\spaced-equals.ps" --version 1.1
    }

    Invoke-DemoCase "Formatter con llave de if en la línea siguiente" 0 {
        & $cli formatting ".\examples\formatter\unformatted.ps" --version 1.1 --config ".\examples\formatter\v1.1-config.json"
    }

    Invoke-DemoCase "Linter con tres reglas configuradas" 3 {
        & $cli analysis ".\examples\linter\style-violations.ps" --version 1.1 --config ".\examples\linter\v1.1-config.json"
    }

    Invoke-DemoCase "Error sintáctico con ubicación" 1 {
        & $cli validation ".\examples\demo\errors\syntax-error.ps" --version 1.0
    }

    Invoke-DemoCase "Error semántico de ejecución" 1 {
        & $cli execution ".\examples\demo\errors\division-by-zero.ps" --version 1.0
    }
} finally {
    Pop-Location
}

Write-Host ""
if ($failedCases -eq 0) {
    Write-Host "Demostración terminada: todos los casos devolvieron el código esperado." -ForegroundColor Green
    exit 0
}

Write-Host "Demostración terminada con $failedCases resultado(s) inesperado(s)." -ForegroundColor Red
exit 1
