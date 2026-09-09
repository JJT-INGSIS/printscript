package printscript.v1.lexer

import printscript.token.TokenType

public class PrintScriptV1LexerConfiguration(
    keywordTokenTypesByLexeme: Map<String, TokenType>,
    symbolTokenTypesByCharacter: Map<Char, TokenType>,
    stringQuoteDelimiters: Set<Char>,
) {

    public val keywordTokenTypesByLexeme: Map<String, TokenType> =
        keywordTokenTypesByLexeme.toMap()

    public val symbolTokenTypesByCharacter: Map<Char, TokenType> =
        symbolTokenTypesByCharacter.toMap()

    public val stringQuoteDelimiters: Set<Char> =
        stringQuoteDelimiters.toSet()
}
