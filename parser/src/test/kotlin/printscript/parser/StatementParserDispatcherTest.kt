package printscript.parser

import printscript.model.ast.statement.AssignmentStatement
import printscript.model.ast.statement.PrintlnStatement
import printscript.model.ast.statement.VariableDeclarationStatement
import printscript.token.TokenType
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
                TokenType.LET,
                TokenType.IDENTIFIER,
                TokenType.PRINTLN,
            ),
            actualTokenType = TokenType.NUMBER_LITERAL,
        )
    }

    @Test
    fun `reports the mismatch of the parser that got furthest`() {
        // Un identificador sin "=" no es una asignación, pero el parser
        // de asignaciones llegó más lejos que los otros dos, así que su
        // desajuste es el que mejor describe el problema.
        parseFirst(
            tokens {
                id("a")
                plus()
                number("5")
                semicolon()
                eof()
            },
        ).assertUnexpectedToken(
            expectedTokenTypes = setOf(TokenType.ASSIGN),
            actualTokenType = TokenType.PLUS,
        )
    }
}