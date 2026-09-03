package printscript.v1.lexer

import printscript.token.TokenType

/** Token types used only by the lossless lexer that feeds the formatter. */
public enum class PrintScriptV1FormattingTokenType : TokenType {
    WHITESPACE,
}
