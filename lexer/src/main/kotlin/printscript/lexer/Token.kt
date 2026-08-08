package printscript.lexer

import printscript.model.source.SourceSpan

data class Token(
    val type: TokenType,
    val lexeme: String,
    val span: SourceSpan
)

