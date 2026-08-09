package parser


import printscript.model.source.SourceSpan
import printscript.token.LexicalError

sealed interface ParseError {
    val span: SourceSpan

    data class Syntax(val message: String, override val span: SourceSpan) : ParseError

    data class Lexical(val error: LexicalError) : ParseError {
        override val span: SourceSpan get() = error.span
    }
}
