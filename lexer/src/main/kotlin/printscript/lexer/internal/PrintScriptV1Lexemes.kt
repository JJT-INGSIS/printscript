package printscript.lexer.internal

import printscript.lexer.TokenType

internal val printScriptV1FixedTokens: Map<String, TokenType> = mapOf(
    "let" to TokenType.LET,
    "number" to TokenType.NUMBER_TYPE,
    "string" to TokenType.STRING_TYPE,
    "println" to TokenType.PRINTLN,

    "+" to TokenType.PLUS,
    "-" to TokenType.MINUS,
    "*" to TokenType.STAR,
    "/" to TokenType.SLASH,
    "=" to TokenType.ASSIGN,

    ":" to TokenType.COLON,
    ";" to TokenType.SEMICOLON,
    "(" to TokenType.LEFT_PAREN,
    ")" to TokenType.RIGHT_PAREN
)