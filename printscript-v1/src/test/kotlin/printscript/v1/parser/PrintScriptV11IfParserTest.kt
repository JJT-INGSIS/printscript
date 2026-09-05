package printscript.v1.parser

import printscript.ast.statement.IfStatement
import printscript.ast.statement.PrintlnStatement
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class PrintScriptV11IfParserTest {

    @Test
    fun `parses if with multiple statements in its block`() {
        val statement = statementOfV11<IfStatement>(
            tokens {
                ifKeyword()
                open()
                id("active")
                close()
                leftBrace()
                printlnStatement("\"first\"")
                printlnStatement("\"second\"")
                rightBrace()
                eof()
            },
        )

        assertEquals("active", statement.condition.value)
        assertEquals(2, statement.thenBranch.statements.size)
        statement.thenBranch.statements.forEach { nested ->
            assertIs<PrintlnStatement>(nested)
        }
        assertNull(statement.elseBranch)
    }

    @Test
    fun `parses nested if and else blocks`() {
        val statement = statementOfV11<IfStatement>(
            tokens {
                ifKeyword()
                open()
                id("outer")
                close()
                leftBrace()
                ifKeyword()
                open()
                id("inner")
                close()
                leftBrace()
                printlnStatement("\"then\"")
                rightBrace()
                rightBrace()
                elseKeyword()
                leftBrace()
                printlnStatement("\"else\"")
                rightBrace()
                eof()
            },
        )

        assertIs<IfStatement>(statement.thenBranch.statements.single())
        val elseBranch = requireNotNull(statement.elseBranch)
        assertIs<PrintlnStatement>(elseBranch.statements.single())
    }

    @Test
    fun `missing closing brace reports the expected token`() {
        parseFirstV11(
            tokens {
                ifKeyword()
                open()
                id("active")
                close()
                leftBrace()
                printlnStatement("\"inside\"")
                eof()
            },
        ).assertUnexpectedToken(
            expectedTokenTypes = setOf(PrintScriptV1TokenType.RIGHT_BRACE),
            actualTokenType = PrintScriptV1TokenType.EOF,
        )
    }

    @Test
    fun `statement after if remains available to the source`() {
        val source = sourceOfV11(
            tokens {
                ifKeyword()
                open()
                id("active")
                close()
                leftBrace()
                rightBrace()
                printlnStatement("\"outside\"")
                eof()
            },
        )

        val conditional = source.assertNextStatement()
        assertIs<IfStatement>(conditional.statement)

        val following = conditional.remainingSource.assertNextStatement()
        assertIs<PrintlnStatement>(following.statement)
        following.remainingSource.assertEndOfInput()
    }

    private fun TokenListBuilder.printlnStatement(argument: String) {
        println()
        open()
        string(argument)
        close()
        semicolon()
    }
}
