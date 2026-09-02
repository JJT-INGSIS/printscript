package printscript.cli.internal.toolchain

import printscript.v1.formatter.PrintScriptV1FormatterFactory
import printscript.v1.interpreter.PrintScriptV1InterpreterFactory
import printscript.v1.lexer.PrintScriptV1LexerFactory
import printscript.v1.linter.PrintScriptV1LinterFactory
import printscript.v1.parser.PrintScriptV1ParserFactory

internal object PrintScriptToolchainFactory {

    fun forVersion(version: LanguageVersion): PrintScriptToolchain {
        return when (version) {
            LanguageVersion.V1_0 -> printScriptV1Toolchain()
        }
    }

    private fun printScriptV1Toolchain(): PrintScriptToolchain {
        return PrintScriptToolchain(
            statementsFrom = { sourceReader ->
                PrintScriptV1ParserFactory.create().parse(
                    tokens = PrintScriptV1LexerFactory.create().tokenize(sourceReader),
                )
            },
            interpreterWriting = { output ->
                PrintScriptV1InterpreterFactory.create(output)
            },
            formatter = {
                PrintScriptV1FormatterFactory.create()
            },
            linter = {
                PrintScriptV1LinterFactory.create()
            },
        )
    }
}
