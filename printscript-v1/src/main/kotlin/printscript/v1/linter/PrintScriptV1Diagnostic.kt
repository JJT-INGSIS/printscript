package printscript.v1.linter

import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.linter.Diagnostic
import printscript.model.source.SourceSpan

/**
 * Lo que observan las reglas de PrintScript 1.0.
 *
 * Sellado: el conjunto lo fija la versión del lenguaje, así que quien lo
 * reporte tiene un `when` exhaustivo. Una regla externa aporta su propio
 * diagnóstico implementando [Diagnostic], sin tocar esta jerarquía.
 */
public sealed interface PrintScriptV1Diagnostic : Diagnostic {

    public data class NamingConventionViolation(
        public val identifier: Identifier,
        public val expectedConvention: PrintScriptV1NamingConvention,
    ) : PrintScriptV1Diagnostic {

        override val span: SourceSpan = identifier.span
    }

    public data class UnsupportedPrintlnArgument(
        public val argument: Expression,
    ) : PrintScriptV1Diagnostic {

        override val span: SourceSpan = argument.span
    }
}
