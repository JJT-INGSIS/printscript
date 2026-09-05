package printscript.v1.lexer.internal

import printscript.token.TokenType
import printscript.v1.internal.PrintScriptV11Lexemes
import printscript.v1.token.PrintScriptV1TokenType

internal val printScriptV11KeywordTokenTypesByLexeme: Map<String, TokenType> =
    printScriptV1KeywordTokenTypesByLexeme +
        mapOf(
            PrintScriptV11Lexemes.CONSTANT_DECLARATION_KEYWORD to PrintScriptV1TokenType.CONST,
            PrintScriptV11Lexemes.BOOLEAN_TYPE_NAME to PrintScriptV1TokenType.BOOLEAN_TYPE,
            PrintScriptV11Lexemes.IF_KEYWORD to PrintScriptV1TokenType.IF,
            PrintScriptV11Lexemes.ELSE_KEYWORD to PrintScriptV1TokenType.ELSE,
            PrintScriptV11Lexemes.READ_INPUT_FUNCTION_NAME to PrintScriptV1TokenType.READ_INPUT,
            PrintScriptV11Lexemes.READ_ENVIRONMENT_FUNCTION_NAME to PrintScriptV1TokenType.READ_ENV,
            PrintScriptV11Lexemes.TRUE_LITERAL to PrintScriptV1TokenType.TRUE,
            PrintScriptV11Lexemes.FALSE_LITERAL to PrintScriptV1TokenType.FALSE,
        )

internal val printScriptV11SymbolTokenTypesByLexeme: Map<String, TokenType> =
    printScriptV1SymbolTokenTypesByLexeme +
        mapOf(
            PrintScriptV11Lexemes.LEFT_BRACE to PrintScriptV1TokenType.LEFT_BRACE,
            PrintScriptV11Lexemes.RIGHT_BRACE to PrintScriptV1TokenType.RIGHT_BRACE,
        )
