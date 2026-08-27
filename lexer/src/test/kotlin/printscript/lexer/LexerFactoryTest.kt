package printscript.lexer

import printscript.lexer.scanning.IgnoredCharacterPolicy
import kotlin.test.Test

class LexerFactoryTest {

    @Test
    fun `creates lexer from externally supplied components`() {
        val lexer = LexerFactory.create(
            tokenScanners = listOf(
                TestWordScanner(
                    tokenType = TestTokenType.FIRST_WORD,
                ),
            ),
            ignoredCharacterPolicy =
            IgnoredCharacterPolicy { character ->
                character == '_'
            },
            endOfInputTokenType = TestTokenType.END_OF_INPUT,
        )

        val tokenSource = lexer.tokenize(
            sourceReaderFor("_external"),
        )

        tokenSource.assertProducesTokenSequence(
            listOf(
                ExpectedToken(TestTokenType.FIRST_WORD, "external"),
                ExpectedToken(TestTokenType.END_OF_INPUT, ""),
            ),
        )
    }
}
