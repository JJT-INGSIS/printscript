package printscript.v1.lexer.internal

import printscript.token.TokenType
import printscript.v1.internal.PrintScriptV1Lexemes
import printscript.v1.token.PrintScriptV1TokenType

internal val printScriptV1StringQuoteDelimiters: Set<Char> =
    setOf(
        PrintScriptV1Lexemes.SINGLE_QUOTE_DELIMITER,
        PrintScriptV1Lexemes.DOUBLE_QUOTE_DELIMITER,
    )

internal val printScriptV1KeywordTokenTypesByLexeme:
    Map<String, TokenType> = mapOf(
        PrintScriptV1Lexemes.DECLARATION_KEYWORD to PrintScriptV1TokenType.LET,
        PrintScriptV1Lexemes.NUMBER_TYPE_NAME to PrintScriptV1TokenType.NUMBER_TYPE,
        PrintScriptV1Lexemes.STRING_TYPE_NAME to PrintScriptV1TokenType.STRING_TYPE,
        PrintScriptV1Lexemes.PRINTLN_FUNCTION_NAME to PrintScriptV1TokenType.PRINTLN,
    )

internal val printScriptV1SymbolTokenTypesByLexeme:
    Map<String, TokenType> = mapOf(
        PrintScriptV1Lexemes.ADDITION_OPERATOR to PrintScriptV1TokenType.PLUS,
        PrintScriptV1Lexemes.SUBTRACTION_OPERATOR to PrintScriptV1TokenType.MINUS,
        PrintScriptV1Lexemes.MULTIPLICATION_OPERATOR to PrintScriptV1TokenType.STAR,
        PrintScriptV1Lexemes.DIVISION_OPERATOR to PrintScriptV1TokenType.SLASH,
        PrintScriptV1Lexemes.ASSIGNMENT_OPERATOR to PrintScriptV1TokenType.ASSIGN,
        PrintScriptV1Lexemes.COLON to PrintScriptV1TokenType.COLON,
        PrintScriptV1Lexemes.SEMICOLON to PrintScriptV1TokenType.SEMICOLON,
        PrintScriptV1Lexemes.LEFT_PARENTHESIS to PrintScriptV1TokenType.LEFT_PAREN,
        PrintScriptV1Lexemes.RIGHT_PARENTHESIS to PrintScriptV1TokenType.RIGHT_PAREN,
    )
