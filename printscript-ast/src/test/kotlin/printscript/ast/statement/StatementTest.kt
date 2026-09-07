package printscript.ast.statement

import printscript.ast.DeclarationKind
import printscript.ast.DeclaredType
import printscript.ast.Identifier
import printscript.ast.expression.BooleanLiteralExpression
import printscript.ast.expression.IdentifierExpression
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class StatementTest {

    private val identifierSpan = SourceSpan(SourcePosition(1, 5, 4), SourcePosition(1, 11, 10))
    private val statementSpan = SourceSpan(SourcePosition.initial(), SourcePosition(1, 22, 21))
    private val identifier = Identifier("active", identifierSpan)

    @Test
    fun `declarations without initializers default to variables for every declared type`() {
        DeclaredType.entries.forEach { type ->
            val declaration = VariableDeclarationStatement(identifier, type, null, statementSpan)

            assertSame(identifier, declaration.identifier)
            assertEquals(type, declaration.declaredType)
            assertNull(declaration.initializer)
            assertEquals(DeclarationKind.VARIABLE, declaration.declarationKind)
            assertEquals(statementSpan, declaration.span)
        }
    }

    @Test
    fun `constant declarations retain their initializer separately from the statement range`() {
        val initializer = BooleanLiteralExpression(true, identifierSpan)
        val declaration = VariableDeclarationStatement(
            identifier,
            DeclaredType.BOOLEAN,
            initializer,
            statementSpan,
            DeclarationKind.CONSTANT,
        )

        assertSame(initializer, declaration.initializer)
        assertEquals(DeclarationKind.CONSTANT, declaration.declarationKind)
        assertEquals(statementSpan, declaration.span)
        assertEquals(identifierSpan, declaration.identifier.span)
    }

    @Test
    fun `assignment and println retain their expressions and complete statement ranges`() {
        val expression = IdentifierExpression(identifier)
        val assignment = AssignmentStatement(identifier, expression, statementSpan)
        val println = PrintlnStatement(expression, statementSpan)

        assertSame(identifier, assignment.target)
        assertSame(expression, assignment.expression)
        assertSame(expression, println.argument)
        assertEquals(statementSpan, assignment.span)
        assertEquals(statementSpan, println.span)
        assertEquals(identifierSpan, expression.span)
    }

    @Test
    fun `if preserves its condition branches and enclosing span with an optional else`() {
        val println = PrintlnStatement(IdentifierExpression(identifier), statementSpan)
        val thenBranch = BlockStatement(listOf(println), statementSpan)
        val elseBranch = BlockStatement(emptyList(), identifierSpan)
        val conditional = IfStatement(identifier, thenBranch, elseBranch, statementSpan)
        val withoutElse = conditional.copy(elseBranch = null)

        assertSame(identifier, conditional.condition)
        assertSame(thenBranch, conditional.thenBranch)
        assertSame(elseBranch, conditional.elseBranch)
        assertEquals(statementSpan, conditional.span)
        assertEquals(statementSpan, thenBranch.span)
        assertEquals(identifierSpan, elseBranch.span)
        assertNull(withoutElse.elseBranch)
        assertSame(thenBranch, withoutElse.thenBranch)
        assertSame(elseBranch, conditional.elseBranch)
    }
}
