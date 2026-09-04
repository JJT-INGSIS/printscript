package printscript.ast.statement

import printscript.ast.DeclarationKind
import printscript.ast.DeclaredType
import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.model.source.SourceSpan
import printscript.statement.Statement

public data class VariableDeclarationStatement(
    public val identifier: Identifier,
    public val declaredType: DeclaredType,
    public val initializer: Expression?,
    override val span: SourceSpan,
    public val declarationKind: DeclarationKind = DeclarationKind.VARIABLE,
) : Statement
