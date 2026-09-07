package printscript.v1.linter

import printscript.source.SourceReaderFactory
import printscript.v1.lexer.PrintScriptV11LexerFactory
import printscript.v1.linter.rule.PrintScriptV1IdentifierNamingRule
import printscript.v1.linter.rule.PrintScriptV1PrintlnArgumentRule
import printscript.v1.linter.rule.PrintScriptV1ReadInputArgumentRule
import printscript.v1.parser.PrintScriptV11ParserFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PublicLinterCompositionTest {

    @Test
    fun `README composition applies all three public rules inside blocks`() {
        val argumentPolicy = mapOf(
            PrintScriptV1ExpressionKind.LITERAL to PrintScriptV1ArgumentAcceptance.ACCEPTED,
            PrintScriptV1ExpressionKind.VARIABLE to PrintScriptV1ArgumentAcceptance.ACCEPTED,
            PrintScriptV1ExpressionKind.COMPOSED to PrintScriptV1ArgumentAcceptance.REJECTED,
        )
        val linter = PrintScriptV11LinterFactory.create(
            configuration = PrintScriptV11LinterConfiguration(rules = emptyList()),
            additionalRules = listOf(
                PrintScriptV1IdentifierNamingRule(PrintScriptV1NamingConvention.CAMEL_CASE),
                PrintScriptV1PrintlnArgumentRule(argumentPolicy),
                PrintScriptV1ReadInputArgumentRule(argumentPolicy),
            ),
        )
        val source = """
            let active: boolean = true;
            if (active) {
                let bad_name: string = readInput("a" + "b");
                println("hello" + bad_name);
            }
        """.trimIndent()
        val tokens = PrintScriptV11LexerFactory.create().tokenize(SourceReaderFactory.fromString(source))
        val statements = PrintScriptV11ParserFactory.create().parse(tokens)

        val diagnostics = linter.lint(statements).readAll()

        assertEquals(3, diagnostics.size)
        assertIs<PrintScriptV1Diagnostic.NamingConventionViolation>(diagnostics[0])
        assertIs<PrintScriptV11Diagnostic.UnsupportedReadInputArgument>(diagnostics[1])
        assertIs<PrintScriptV1Diagnostic.UnsupportedPrintlnArgument>(diagnostics[2])
    }
}
