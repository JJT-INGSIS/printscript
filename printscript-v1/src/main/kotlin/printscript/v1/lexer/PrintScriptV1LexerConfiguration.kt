package printscript.v1.lexer

import printscript.lexer.scanning.IgnoredCharacterPolicy
import printscript.token.TokenType

public class PrintScriptV1LexerConfiguration(
    keywordTokenTypesByLexeme: Map<String, TokenType>,
    symbolTokenTypesByLexeme: Map<String, TokenType>,
    stringQuoteDelimiters: Set<Char>,
    public val ignoredCharacterPolicy: IgnoredCharacterPolicy,
) {

    public val keywordTokenTypesByLexeme: Map<String, TokenType> =
        keywordTokenTypesByLexeme.toMap()

    public val symbolTokenTypesByLexeme: Map<String, TokenType> =
        symbolTokenTypesByLexeme.toMap()

    public val stringQuoteDelimiters: Set<Char> =
        stringQuoteDelimiters.toSet()
}
