package printscript.ast.statement

import printscript.ast.DeclaredType
import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.model.source.SourceSpan

data class VariableDeclarationStatement(
    val identifier: Identifier,
    val declaredType: DeclaredType,
    val initializer: Expression?,
    override val span: SourceSpan,
) : Statement