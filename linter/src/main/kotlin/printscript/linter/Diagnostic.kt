package printscript.linter

import printscript.ast.Identifier
import printscript.ast.expression.Expression
import printscript.model.source.SourceSpan

public sealed interface Diagnostic {

    public val span: SourceSpan

    public data class NamingConventionViolation(
        public val identifier: Identifier,
        public val expectedConvention: NamingConvention,
    ) : Diagnostic {

        override val span: SourceSpan = identifier.span
    }

    public data class UnsupportedPrintlnArgument(
        public val argument: Expression,
    ) : Diagnostic {

        override val span: SourceSpan = argument.span
    }
}
