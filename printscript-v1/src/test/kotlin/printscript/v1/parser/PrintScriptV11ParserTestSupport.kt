package printscript.v1.parser

import printscript.ast.expression.Expression
import printscript.ast.statement.AssignmentStatement
import printscript.statement.Statement
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

internal fun sourceOfV11(tokens: List<TokenReadFixture>): StatementSource {
    return PrintScriptV11ParserFactory.create().parse(
        tokens = FakeTokenSource(tokens),
    )
}

internal fun parseFirstV11(tokens: List<TokenReadFixture>): StatementReadResult {
    return sourceOfV11(tokens).nextStatement()
}

internal inline fun <reified T : Statement> statementOfV11(tokens: List<TokenReadFixture>): T {
    return parseFirstV11(tokens).assertStatement()
}

internal fun expressionOfV11(expression: TokenListBuilder.() -> Unit): Expression {
    val statement = statementOfV11<AssignmentStatement>(
        tokens {
            id("target")
            assign()
            expression()
            semicolon()
            eof()
        },
    )

    return statement.expression
}
