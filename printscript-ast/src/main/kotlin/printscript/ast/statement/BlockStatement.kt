package printscript.ast.statement

import printscript.model.source.SourceSpan
import printscript.statement.Statement

public class BlockStatement(
    statements: List<Statement>,
    override val span: SourceSpan,
) : Statement {

    public val statements: List<Statement> = statements.toList()
}
