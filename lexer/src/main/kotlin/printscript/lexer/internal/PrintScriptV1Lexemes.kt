package printscript.lexer.internal

import printscript.token.TokenType

internal val printScriptV1KeywordTokenTypesByLexeme:
        Map<String, TokenType> = mapOf(
    "let" to TokenType.LET,
    "number" to TokenType.NUMBER_TYPE,
    "string" to TokenType.STRING_TYPE,
    "println" to TokenType.PRINTLN,
)

internal val printScriptV1SymbolTokenTypesByLexeme:
        Map<String, TokenType> = mapOf(
    "+" to TokenType.PLUS,
    "-" to TokenType.MINUS,
    "*" to TokenType.STAR,
    "/" to TokenType.SLASH,
    "=" to TokenType.ASSIGN,
    ":" to TokenType.COLON,
    ";" to TokenType.SEMICOLON,
    "(" to TokenType.LEFT_PAREN,
    ")" to TokenType.RIGHT_PAREN,
)