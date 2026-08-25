package printscript.linter.internal.rule

import printscript.ast.Identifier
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.linter.Diagnostic
import printscript.linter.NamingConvention

internal class IdentifierNamingRule(
    private val convention: NamingConvention,
) : LintRule {

    override fun inspect(
        statement: Statement,
    ): List<Diagnostic> {
        return declaredIdentifiersOf(statement)
            .filterNot { identifier -> convention.matches(identifier.value) }
            .map { identifier -> violationOf(identifier) }
    }

    /**
     * Solo el sitio de declaración: revisar cada uso repetiría el mismo
     * diagnóstico sobre el mismo nombre.
     */
    private fun declaredIdentifiersOf(
        statement: Statement,
    ): List<Identifier> {
        return when (statement) {
            is VariableDeclarationStatement -> listOf(statement.identifier)

            is AssignmentStatement -> emptyList()

            is PrintlnStatement -> emptyList()
        }
    }

    private fun violationOf(
        identifier: Identifier,
    ): Diagnostic {
        return Diagnostic.NamingConventionViolation(
            identifier = identifier,
            expectedConvention = convention,
        )
    }
}
