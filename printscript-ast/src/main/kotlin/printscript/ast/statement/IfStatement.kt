package printscript.ast.statement

import printscript.ast.Identifier
import printscript.model.source.SourceSpan
import printscript.statement.Statement

public data class IfStatement(
    public val condition: Identifier,
    public val thenBranch: BlockStatement,
    public val elseBranch: BlockStatement?,
    override val span: SourceSpan,
) : Statement
