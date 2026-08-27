package printscript.v1.lexer.internal

import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType

internal val printScriptV1StringQuoteDelimiters: Set<Char> =
    setOf(
        '\'',
        '"',
    )

internal val printScriptV1KeywordTokenTypesByLexeme:
    Map<String, TokenType> = mapOf(
        "let" to PrintScriptV1TokenType.LET,
        "number" to PrintScriptV1TokenType.NUMBER_TYPE,
        "string" to PrintScriptV1TokenType.STRING_TYPE,
        "println" to PrintScriptV1TokenType.PRINTLN,
    )

internal val printScriptV1SymbolTokenTypesByLexeme:
    Map<String, TokenType> = mapOf(
        "+" to PrintScriptV1TokenType.PLUS,
        "-" to PrintScriptV1TokenType.MINUS,
        "*" to PrintScriptV1TokenType.STAR,
        "/" to PrintScriptV1TokenType.SLASH,
        "=" to PrintScriptV1TokenType.ASSIGN,
        ":" to PrintScriptV1TokenType.COLON,
        ";" to PrintScriptV1TokenType.SEMICOLON,
        "(" to PrintScriptV1TokenType.LEFT_PAREN,
        ")" to PrintScriptV1TokenType.RIGHT_PAREN,
    )
