package printscript.parser

import printscript.model.ast.DeclaredType
import printscript.model.ast.expression.BinaryExpression
import printscript.model.ast.expression.BinaryOperator
import printscript.model.ast.expression.Expression
import printscript.model.ast.expression.GroupingExpression
import printscript.model.ast.expression.IdentifierExpression
import printscript.model.ast.expression.NumberLiteralExpression
import printscript.model.ast.expression.StringLiteralExpression
import printscript.model.ast.expression.StringQuoteStyle
import printscript.model.ast.expression.UnaryExpression
import printscript.model.ast.expression.UnaryOperator
import printscript.model.ast.statement.AssignmentStatement
import printscript.model.ast.statement.PrintlnStatement
import printscript.model.ast.statement.VariableDeclarationStatement
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.statement.StatementReadResult
import printscript.token.LexicalError
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class TokensSource(private val results: List<TokenReadResult>) : TokenSource {
    private var index = 0
    override fun nextToken(): TokenReadResult =
        if (index < results.size) results[index++] else results.last()
}

private val ANY_SPAN = SourceSpan(SourcePosition(1, 1, 0), SourcePosition(1, 1, 0))

private fun tk(type: TokenType, lexeme: String) = TokenReadResult.Success(Token(type, lexeme, ANY_SPAN))

private fun lexicalError() = TokenReadResult.Failure(LexicalError.UnexpectedCharacter('@', ANY_SPAN))

private fun parse(vararg results: TokenReadResult): StatementReadResult =
    PrintScriptParser().parse(TokensSource(results.toList())).nextStatement()

private fun statementOf(vararg results: TokenReadResult) =
    (parse(*results) as StatementReadResult.Success).statement

private fun expressionOf(vararg exprTokens: TokenReadResult): Expression {
    val statement = statementOf(
        tk(TokenType.IDENTIFIER, "x"),
        tk(TokenType.ASSIGN, "="),
        *exprTokens,
        tk(TokenType.SEMICOLON, ";"),
        tk(TokenType.EOF, ""),
    )
    return (statement as AssignmentStatement).expression
}

class PrintScriptParserTest {

    // ---------- declaration ----------

    @Test
    fun `declaration with number initializer`() {
        // given
        val tokens = arrayOf(
            tk(TokenType.LET, "let"), tk(TokenType.IDENTIFIER, "a"), tk(TokenType.COLON, ":"),
            tk(TokenType.NUMBER_TYPE, "number"), tk(TokenType.ASSIGN, "="),
            tk(TokenType.NUMBER_LITERAL, "5"), tk(TokenType.SEMICOLON, ";"), tk(TokenType.EOF, ""),
        )
        // when
        val statement = statementOf(*tokens) as VariableDeclarationStatement
        // then
        assertEquals("a", statement.identifier.value)
        assertEquals(DeclaredType.NUMBER, statement.declaredType)
        assertEquals(BigDecimal("5"), (statement.initializer as NumberLiteralExpression).value)
    }

    @Test
    fun `declaration with string type`() {
        // given / when
        val statement = statementOf(
            tk(TokenType.LET, "let"), tk(TokenType.IDENTIFIER, "name"), tk(TokenType.COLON, ":"),
            tk(TokenType.STRING_TYPE, "string"), tk(TokenType.SEMICOLON, ";"), tk(TokenType.EOF, ""),
        ) as VariableDeclarationStatement
        // then
        assertEquals(DeclaredType.STRING, statement.declaredType)
    }

    @Test
    fun `declaration without initializer keeps it null`() {
        // given / when
        val statement = statementOf(
            tk(TokenType.LET, "let"), tk(TokenType.IDENTIFIER, "a"), tk(TokenType.COLON, ":"),
            tk(TokenType.NUMBER_TYPE, "number"), tk(TokenType.SEMICOLON, ";"), tk(TokenType.EOF, ""),
        ) as VariableDeclarationStatement
        // then
        assertNull(statement.initializer)
    }

    @Test
    fun `declaration with invalid type fails`() {
        // given / when
        val result = parse(
            tk(TokenType.LET, "let"), tk(TokenType.IDENTIFIER, "a"), tk(TokenType.COLON, ":"),
            tk(TokenType.IDENTIFIER, "notAType"), tk(TokenType.SEMICOLON, ";"), tk(TokenType.EOF, ""),
        )
        // then
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `declaration missing semicolon fails`() {
        // given / when
        val result = parse(
            tk(TokenType.LET, "let"), tk(TokenType.IDENTIFIER, "a"), tk(TokenType.COLON, ":"),
            tk(TokenType.NUMBER_TYPE, "number"), tk(TokenType.EOF, ""),
        )
        // then
        assertTrue(result is StatementReadResult.Failure)
    }

    // ---------- assignment ----------

    @Test
    fun `assignment of an identifier`() {
        // given / when
        val statement = statementOf(
            tk(TokenType.IDENTIFIER, "x"), tk(TokenType.ASSIGN, "="),
            tk(TokenType.IDENTIFIER, "y"), tk(TokenType.SEMICOLON, ";"), tk(TokenType.EOF, ""),
        ) as AssignmentStatement
        // then
        assertEquals("x", statement.target.value)
        assertEquals("y", (statement.expression as IdentifierExpression).identifier.value)
    }

    @Test
    fun `assignment without equals fails`() {
        // given / when
        val result = parse(
            tk(TokenType.IDENTIFIER, "x"), tk(TokenType.NUMBER_LITERAL, "5"),
            tk(TokenType.SEMICOLON, ";"), tk(TokenType.EOF, ""),
        )
        // then
        assertTrue(result is StatementReadResult.Failure)
    }

    // ---------- println ----------

    @Test
    fun `println with a literal`() {
        // given / when
        val statement = statementOf(
            tk(TokenType.PRINTLN, "println"), tk(TokenType.LEFT_PAREN, "("),
            tk(TokenType.NUMBER_LITERAL, "168"), tk(TokenType.RIGHT_PAREN, ")"),
            tk(TokenType.SEMICOLON, ";"), tk(TokenType.EOF, ""),
        ) as PrintlnStatement
        // then
        assertEquals(BigDecimal("168"), (statement.argument as NumberLiteralExpression).value)
    }

    @Test
    fun `println with a concatenation expression`() {
        // given / when
        val statement = statementOf(
            tk(TokenType.PRINTLN, "println"), tk(TokenType.LEFT_PAREN, "("),
            tk(TokenType.STRING_LITERAL, "\"Result: \""), tk(TokenType.PLUS, "+"),
            tk(TokenType.IDENTIFIER, "c"), tk(TokenType.RIGHT_PAREN, ")"),
            tk(TokenType.SEMICOLON, ";"), tk(TokenType.EOF, ""),
        ) as PrintlnStatement
        // then
        val concat = statement.argument as BinaryExpression
        assertEquals(BinaryOperator.ADD, concat.operator)
        assertTrue(concat.left is StringLiteralExpression)
        assertTrue(concat.right is IdentifierExpression)
    }

    @Test
    fun `println missing closing paren fails`() {
        // given / when
        val result = parse(
            tk(TokenType.PRINTLN, "println"), tk(TokenType.LEFT_PAREN, "("),
            tk(TokenType.NUMBER_LITERAL, "1"), tk(TokenType.SEMICOLON, ";"), tk(TokenType.EOF, ""),
        )
        // then
        assertTrue(result is StatementReadResult.Failure)
    }

    // ---------- expressions ----------

    @Test
    fun `decimal number literal`() {
        // when
        val expression = expressionOf(tk(TokenType.NUMBER_LITERAL, "3.14"))
        // then
        assertEquals(BigDecimal("3.14"), (expression as NumberLiteralExpression).value)
    }

    @Test
    fun `single quoted string keeps SINGLE quote style and strips quotes`() {
        // when
        val expression = expressionOf(tk(TokenType.STRING_LITERAL, "'hi'"))
        // then
        val literal = expression as StringLiteralExpression
        assertEquals("hi", literal.value)
        assertEquals(StringQuoteStyle.SINGLE, literal.quoteStyle)
    }

    @Test
    fun `double quoted string keeps DOUBLE quote style`() {
        // when
        val expression = expressionOf(tk(TokenType.STRING_LITERAL, "\"hi\""))
        // then
        assertEquals(StringQuoteStyle.DOUBLE, (expression as StringLiteralExpression).quoteStyle)
    }

    @Test
    fun `each binary operator maps correctly`() {
        // given
        val cases = mapOf(
            TokenType.PLUS to BinaryOperator.ADD,
            TokenType.MINUS to BinaryOperator.SUBTRACT,
            TokenType.STAR to BinaryOperator.MULTIPLY,
            TokenType.SLASH to BinaryOperator.DIVIDE,
        )
        // when / then
        for ((tokenType, operator) in cases) {
            val expression = expressionOf(
                tk(TokenType.NUMBER_LITERAL, "1"), tk(tokenType, "op"), tk(TokenType.NUMBER_LITERAL, "2"),
            )
            assertEquals(operator, (expression as BinaryExpression).operator)
        }
    }

    @Test
    fun `multiplication binds tighter than addition`() {
        // when  2 + 3 * 4
        val expression = expressionOf(
            tk(TokenType.NUMBER_LITERAL, "2"), tk(TokenType.PLUS, "+"),
            tk(TokenType.NUMBER_LITERAL, "3"), tk(TokenType.STAR, "*"), tk(TokenType.NUMBER_LITERAL, "4"),
        )
        // then  ADD(2, MULTIPLY(3, 4))
        val add = expression as BinaryExpression
        assertEquals(BinaryOperator.ADD, add.operator)
        assertEquals(BinaryOperator.MULTIPLY, (add.right as BinaryExpression).operator)
    }

    @Test
    fun `subtraction is left associative`() {
        // when  a - b - c
        val expression = expressionOf(
            tk(TokenType.IDENTIFIER, "a"), tk(TokenType.MINUS, "-"),
            tk(TokenType.IDENTIFIER, "b"), tk(TokenType.MINUS, "-"), tk(TokenType.IDENTIFIER, "c"),
        )
        // then  SUB(SUB(a, b), c)
        val outer = expression as BinaryExpression
        assertEquals(BinaryOperator.SUBTRACT, outer.operator)
        assertEquals(BinaryOperator.SUBTRACT, (outer.left as BinaryExpression).operator)
        assertTrue(outer.right is IdentifierExpression)
    }

    @Test
    fun `parentheses override precedence`() {
        // when  (2 + 3) * 4
        val expression = expressionOf(
            tk(TokenType.LEFT_PAREN, "("), tk(TokenType.NUMBER_LITERAL, "2"), tk(TokenType.PLUS, "+"),
            tk(TokenType.NUMBER_LITERAL, "3"), tk(TokenType.RIGHT_PAREN, ")"),
            tk(TokenType.STAR, "*"), tk(TokenType.NUMBER_LITERAL, "4"),
        )
        // then  MULTIPLY(Grouping(ADD), 4)
        val product = expression as BinaryExpression
        assertEquals(BinaryOperator.MULTIPLY, product.operator)
        val grouping = product.left as GroupingExpression
        assertEquals(BinaryOperator.ADD, (grouping.expression as BinaryExpression).operator)
    }

    @Test
    fun `unclosed parenthesis fails`() {
        // when
        val result = parse(
            tk(TokenType.IDENTIFIER, "x"), tk(TokenType.ASSIGN, "="),
            tk(TokenType.LEFT_PAREN, "("), tk(TokenType.NUMBER_LITERAL, "1"),
            tk(TokenType.SEMICOLON, ";"), tk(TokenType.EOF, ""),
        )
        // then
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `expression starting with a non factor fails`() {
        // when  x = ;
        val result = parse(
            tk(TokenType.IDENTIFIER, "x"), tk(TokenType.ASSIGN, "="),
            tk(TokenType.SEMICOLON, ";"), tk(TokenType.EOF, ""),
        )
        // then
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `malformed number literal fails`() {
        // when
        val result = parse(
            tk(TokenType.IDENTIFIER, "x"), tk(TokenType.ASSIGN, "="),
            tk(TokenType.NUMBER_LITERAL, "1.2.3"), tk(TokenType.SEMICOLON, ";"), tk(TokenType.EOF, ""),
        )
        // then
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `unary minus on a number`() {
        // when  -5
        val expression = expressionOf(tk(TokenType.MINUS, "-"), tk(TokenType.NUMBER_LITERAL, "5"))
        // then
        val unary = expression as UnaryExpression
        assertEquals(UnaryOperator.MINUS, unary.operator)
        assertEquals(BigDecimal("5"), (unary.operand as NumberLiteralExpression).value)
    }

    @Test
    fun `unary binds tighter than multiplication`() {
        // when  -a * b
        val expression = expressionOf(
            tk(TokenType.MINUS, "-"), tk(TokenType.IDENTIFIER, "a"),
            tk(TokenType.STAR, "*"), tk(TokenType.IDENTIFIER, "b"),
        )
        // then  MULTIPLY(Unary(-a), b)
        val product = expression as BinaryExpression
        assertEquals(BinaryOperator.MULTIPLY, product.operator)
        assertEquals(UnaryOperator.MINUS, (product.left as UnaryExpression).operator)
    }

    @Test
    fun `nested unary operators`() {
        // when  - - 5
        val expression = expressionOf(
            tk(TokenType.MINUS, "-"), tk(TokenType.MINUS, "-"), tk(TokenType.NUMBER_LITERAL, "5"),
        )
        // then  Unary(Unary(5))
        assertTrue((expression as UnaryExpression).operand is UnaryExpression)
    }

    // ---------- dispatch / source ----------

    @Test
    fun `unknown statement start fails`() {
        // when
        val result = parse(tk(TokenType.PLUS, "+"), tk(TokenType.EOF, ""))
        // then
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `empty input is end of input`() {
        // when / then
        assertEquals(StatementReadResult.EndOfInput, parse(tk(TokenType.EOF, "")))
    }

    @Test
    fun `lexical error is surfaced as failure`() {
        // when
        val result = parse(lexicalError())
        // then
        assertTrue(result is StatementReadResult.Failure)
    }

    @Test
    fun `streams several statements until end of input`() {
        // given
        val source = PrintScriptParser().parse(
            TokensSource(
                listOf(
                    tk(TokenType.LET, "let"), tk(TokenType.IDENTIFIER, "a"), tk(TokenType.COLON, ":"),
                    tk(TokenType.NUMBER_TYPE, "number"), tk(TokenType.SEMICOLON, ";"),
                    tk(TokenType.IDENTIFIER, "a"), tk(TokenType.ASSIGN, "="),
                    tk(TokenType.NUMBER_LITERAL, "5"), tk(TokenType.SEMICOLON, ";"),
                    tk(TokenType.EOF, ""),
                ),
            ),
        )
        // when / then
        assertTrue(source.nextStatement() is StatementReadResult.Success)
        assertTrue(source.nextStatement() is StatementReadResult.Success)
        assertEquals(StatementReadResult.EndOfInput, source.nextStatement())
    }
}
