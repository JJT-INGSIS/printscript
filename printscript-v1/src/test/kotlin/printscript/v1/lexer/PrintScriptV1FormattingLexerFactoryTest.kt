package printscript.v1.lexer

import printscript.source.SourceReaderFactory
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV1FormattingLexerFactoryTest {

    @Test
    fun `preserves consecutive whitespace as a formatting token`() {
        val tokens = allTokensFrom("let \tvalue\r\n")

        assertEquals(
            expected = listOf(
                PrintScriptV1TokenType.LET to "let",
                PrintScriptV1FormattingTokenType.WHITESPACE to " \t",
                PrintScriptV1TokenType.IDENTIFIER to "value",
                PrintScriptV1FormattingTokenType.WHITESPACE to "\r\n",
                PrintScriptV1TokenType.EOF to "",
            ),
            actual = tokens.map { token -> token.type to token.lexeme },
        )
    }

    private fun allTokensFrom(sourceCode: String): List<Token> {
        var source: TokenSource = PrintScriptV1FormattingLexerFactory.create().tokenize(
            SourceReaderFactory.fromString(sourceCode),
        )
        val tokens = mutableListOf<Token>()

        while (true) {
            val readResult = assertIs<TokenReadResult.Success>(source.nextToken())
            tokens.add(readResult.token)

            if (readResult.token.type == PrintScriptV1TokenType.EOF) {
                return tokens.toList()
            }

            source = readResult.remainingSource
        }
    }
}
