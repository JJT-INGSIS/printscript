package printscript.v1.linter

import printscript.linter.Diagnostic
import printscript.linter.Linter
import printscript.source.SourceReaderFactory
import printscript.v1.lexer.PrintScriptV11LexerFactory
import printscript.v1.parser.PrintScriptV11ParserFactory
import kotlin.test.Test
import kotlin.test.assertIs

class PrintScriptV11LinterIntegrationTest {

    private fun diagnosticsOfSource(sourceCode: String, linter: Linter): List<Diagnostic> {
        val tokens = PrintScriptV11LexerFactory.create().tokenize(
            sourceReader = SourceReaderFactory.fromString(sourceCode),
        )
        val statements = PrintScriptV11ParserFactory.create().parse(tokens = tokens)

        return linter.lint(statements).readAll()
    }

    @Test
    fun `rejects a composed readInput prompt parsed from source`() {
        val linter = v11LinterWith(rules = listOf(readInputArgumentRule()))

        val diagnostics = diagnosticsOfSource(
            sourceCode = """let input: string = readInput("Enter" + "something");""",
            linter = linter,
        )

        assertIs<PrintScriptV11Diagnostic.UnsupportedReadInputArgument>(diagnostics.single())
    }

    @Test
    fun `accepts a literal readInput prompt parsed from source`() {
        val linter = v11LinterWith(rules = listOf(readInputArgumentRule()))

        val diagnostics = diagnosticsOfSource(
            sourceCode = """let input: string = readInput("Enter");""",
            linter = linter,
        )

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `accepts a variable readInput prompt parsed from source`() {
        val linter = v11LinterWith(rules = listOf(readInputArgumentRule()))

        val diagnostics = diagnosticsOfSource(
            sourceCode = """
                let message: string = "Enter";
                let input: string = readInput(message);
            """.trimIndent(),
            linter = linter,
        )

        diagnostics.assertNoDiagnostics()
    }

    @Test
    fun `applies naming and readInput rules inside an if block from source`() {
        val linter = PrintScriptV11LinterFactory.create(
            configuration = PrintScriptV11LinterConfiguration(
                rules = listOf(identifierNamingRule(), readInputArgumentRule()),
            ),
        )

        val diagnostics = diagnosticsOfSource(
            sourceCode = """
                if (active) {
                    let my_input: string = readInput("Enter" + "!");
                }
            """.trimIndent(),
            linter = linter,
        )

        assertIs<PrintScriptV1Diagnostic.NamingConventionViolation>(diagnostics.first())
        assertIs<PrintScriptV11Diagnostic.UnsupportedReadInputArgument>(diagnostics.last())
    }
}
