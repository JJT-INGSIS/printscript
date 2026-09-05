package printscript.cli.internal.toolchain

import printscript.formatter.Formatter
import printscript.linter.Linter
import printscript.v1.formatter.PrintScriptV11FormatterConfigurationResult
import printscript.v1.formatter.PrintScriptV11FormatterFactory
import printscript.v1.formatter.PrintScriptV1FormatterConfigurationResult
import printscript.v1.formatter.PrintScriptV1FormatterFactory
import printscript.v1.interpreter.PrintScriptV11InterpreterFactory
import printscript.v1.interpreter.PrintScriptV1InterpreterFactory
import printscript.v1.lexer.PrintScriptV11FormattingLexerFactory
import printscript.v1.lexer.PrintScriptV11LexerFactory
import printscript.v1.lexer.PrintScriptV1FormattingLexerFactory
import printscript.v1.lexer.PrintScriptV1LexerFactory
import printscript.v1.linter.PrintScriptV11LinterConfigurationResult
import printscript.v1.linter.PrintScriptV11LinterFactory
import printscript.v1.linter.PrintScriptV1LinterConfigurationResult
import printscript.v1.linter.PrintScriptV1LinterFactory
import printscript.v1.parser.PrintScriptV11ParserFactory
import printscript.v1.parser.PrintScriptV1ParserFactory

internal object PrintScriptToolchainFactory {

    fun forVersion(version: LanguageVersion): PrintScriptToolchain {
        return when (version) {
            LanguageVersion.V1_0 -> printScriptV1Toolchain()
            LanguageVersion.V1_1 -> printScriptV11Toolchain()
        }
    }

    private fun printScriptV1Toolchain(): PrintScriptToolchain {
        return PrintScriptToolchain(
            statementsFrom = { sourceReader ->
                PrintScriptV1ParserFactory.create().parse(
                    tokens = PrintScriptV1LexerFactory.create().tokenize(sourceReader),
                )
            },
            formattingTokensFrom = { sourceReader ->
                PrintScriptV1FormattingLexerFactory.create().tokenize(sourceReader)
            },
            interpreterUsing = { output, _, _ ->
                PrintScriptV1InterpreterFactory.create(output)
            },
            formatterConfiguredBy = ::printScriptV1FormatterConfiguredBy,
            linterConfiguredBy = ::printScriptV1LinterConfiguredBy,
        )
    }

    private fun printScriptV11Toolchain(): PrintScriptToolchain {
        return PrintScriptToolchain(
            statementsFrom = { sourceReader ->
                PrintScriptV11ParserFactory.create().parse(
                    tokens = PrintScriptV11LexerFactory.create().tokenize(sourceReader),
                )
            },
            formattingTokensFrom = { sourceReader ->
                PrintScriptV11FormattingLexerFactory.create().tokenize(sourceReader)
            },
            interpreterUsing = { output, input, environmentVariables ->
                PrintScriptV11InterpreterFactory.create(
                    output = output,
                    input = input,
                    environmentVariables = environmentVariables,
                )
            },
            formatterConfiguredBy = ::printScriptV11FormatterConfiguredBy,
            linterConfiguredBy = ::printScriptV11LinterConfiguredBy,
        )
    }

    private fun printScriptV1FormatterConfiguredBy(json: String?): ConfiguredToolResult<Formatter> {
        if (json == null) {
            return ConfiguredToolResult.Success(PrintScriptV1FormatterFactory.create())
        }

        return when (val result = PrintScriptV1FormatterFactory.configurationFrom(json)) {
            is PrintScriptV1FormatterConfigurationResult.Failure ->
                ConfiguredToolResult.Failure(INVALID_FORMATTER_CONFIGURATION)

            is PrintScriptV1FormatterConfigurationResult.Success ->
                ConfiguredToolResult.Success(
                    PrintScriptV1FormatterFactory.create(result.configuration),
                )
        }
    }

    private fun printScriptV11FormatterConfiguredBy(json: String?): ConfiguredToolResult<Formatter> {
        if (json == null) {
            return ConfiguredToolResult.Success(PrintScriptV11FormatterFactory.create())
        }

        return when (val result = PrintScriptV11FormatterFactory.configurationFrom(json)) {
            is PrintScriptV11FormatterConfigurationResult.Failure ->
                ConfiguredToolResult.Failure(INVALID_FORMATTER_CONFIGURATION)

            is PrintScriptV11FormatterConfigurationResult.Success ->
                ConfiguredToolResult.Success(
                    PrintScriptV11FormatterFactory.create(result.configuration),
                )
        }
    }

    private fun printScriptV1LinterConfiguredBy(json: String?): ConfiguredToolResult<Linter> {
        if (json == null) {
            return ConfiguredToolResult.Success(PrintScriptV1LinterFactory.create())
        }

        return when (val result = PrintScriptV1LinterFactory.configurationFrom(json)) {
            is PrintScriptV1LinterConfigurationResult.Failure ->
                ConfiguredToolResult.Failure(INVALID_LINTER_CONFIGURATION)

            is PrintScriptV1LinterConfigurationResult.Success ->
                ConfiguredToolResult.Success(
                    PrintScriptV1LinterFactory.create(result.configuration),
                )
        }
    }

    private fun printScriptV11LinterConfiguredBy(json: String?): ConfiguredToolResult<Linter> {
        if (json == null) {
            return ConfiguredToolResult.Success(PrintScriptV11LinterFactory.create())
        }

        return when (val result = PrintScriptV11LinterFactory.configurationFrom(json)) {
            is PrintScriptV11LinterConfigurationResult.Failure ->
                ConfiguredToolResult.Failure(INVALID_LINTER_CONFIGURATION)

            is PrintScriptV11LinterConfigurationResult.Success ->
                ConfiguredToolResult.Success(
                    PrintScriptV11LinterFactory.create(result.configuration),
                )
        }
    }

    private const val INVALID_FORMATTER_CONFIGURATION = "la configuración del formatter no es válida"
    private const val INVALID_LINTER_CONFIGURATION = "la configuración del linter no es válida"
}
