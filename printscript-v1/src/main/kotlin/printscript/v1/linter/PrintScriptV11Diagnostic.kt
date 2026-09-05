package printscript.v1.linter

import printscript.ast.expression.Expression
import printscript.linter.Diagnostic
import printscript.model.source.SourceSpan

public sealed interface PrintScriptV11Diagnostic : Diagnostic {

    public data class UnsupportedReadInputArgument(
        public val argument: Expression,
    ) : PrintScriptV11Diagnostic {

        override val span: SourceSpan = argument.span
    }
}
