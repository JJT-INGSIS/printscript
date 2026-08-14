package printscript.model.ast.statement

import printscript.model.ast.DeclaredType
import printscript.model.ast.Identifier
import printscript.model.ast.expression.Expression
import printscript.model.source.SourceSpan

data class VariableDeclarationStatement(
    val identifier: Identifier,
    val declaredType: DeclaredType,
    val initializer: Expression?,
    override val span: SourceSpan,
) : Statement