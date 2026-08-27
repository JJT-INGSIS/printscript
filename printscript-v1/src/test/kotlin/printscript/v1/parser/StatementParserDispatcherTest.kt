package printscript.v1.parser

import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertIs

class StatementParserDispatcherTest {

    @Test
    fun `dispatches to the declaration parser`() {
        val statement = parseFirst(
            tokens {
                let()
                id("a")
                colon()
                numberType()
                semicolon()
                eof()
            },
        ).assertSuccessStatement()

        assertIs<VariableDeclarationStatement>(statement)
    }

    @Test
    fun `dispatches to the assignment parser`() {
        val statement = parseFirst(
            tokens {
                id("a")
                assign()
                number("5")
                semicolon()
                eof()
            },
        ).assertSuccessStatement()

        assertIs<AssignmentStatement>(statement)
    }

    @Test
    fun `dispatches to the println parser`() {
        val statement = parseFirst(
            tokens {
                println()
                open()
                id("a")
                close()
                semicolon()
                eof()
            },
        ).assertSuccessStatement()

        assertIs<PrintlnStatement>(statement)
    }

    @Test
    fun `reports every statement start when nothing matches`() {
        parseFirst(
            tokens {
                number("12")
                plus()
                number("3")
                semicolon()
                eof()
            },
        ).assertUnexpectedToken(
            expectedTokenTypes = setOf(
                PrintScriptV1TokenType.LET,
                PrintScriptV1TokenType.IDENTIFIER,
                PrintScriptV1TokenType.PRINTLN,
            ),
            actualTokenType = PrintScriptV1TokenType.NUMBER_LITERAL,
        )
    }

    @Test
    fun `reports valid continuations for identifier statements`() {
        // Después de un identificador, V1 solamente admite
        // una asignación como continuación de la sentencia.
        parseFirst(
            tokens {
                id("a")
                plus()
                number("5")
                semicolon()
                eof()
            },
        ).assertUnexpectedToken(
            expectedTokenTypes = setOf(PrintScriptV1TokenType.ASSIGN),
            actualTokenType = PrintScriptV1TokenType.PLUS,
        )
    }
}
