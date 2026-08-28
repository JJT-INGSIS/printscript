package printscript.v1.linter.rule

import printscript.ast.Identifier
import printscript.ast.statement.AssignmentStatement
import printscript.ast.statement.PrintlnStatement
import printscript.ast.statement.Statement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.linter.Diagnostic
import printscript.linter.StatelessLintRule
import printscript.v1.linter.PrintScriptV1Diagnostic
import printscript.v1.linter.PrintScriptV1NamingConvention

/**
 * Exige que los nombres declarados sigan una convención.
 *
 * No recuerda nada entre sentencias: alcanza con mirar la declaración.
 */
public class PrintScriptV1IdentifierNamingRule(
    private val convention: PrintScriptV1NamingConvention,
) : StatelessLintRule() {

    protected override fun diagnosticsIn(statement: Statement): List<Diagnostic> {
        return declaredIdentifiersOf(statement)
            .filterNot { identifier -> convention.matches(identifier.value) }
            .map { identifier -> violationOf(identifier) }
    }

    /**
     * Solo el sitio de declaración: revisar cada uso repetiría el mismo
     * diagnóstico sobre el mismo nombre.
     */
    private fun declaredIdentifiersOf(statement: Statement): List<Identifier> {
        return when (statement) {
            is VariableDeclarationStatement -> listOf(statement.identifier)

            is AssignmentStatement -> emptyList()

            is PrintlnStatement -> emptyList()

            else -> emptyList()
        }
    }

    private fun violationOf(identifier: Identifier): Diagnostic {
        return PrintScriptV1Diagnostic.NamingConventionViolation(
            identifier = identifier,
            expectedConvention = convention,
        )
    }
}
