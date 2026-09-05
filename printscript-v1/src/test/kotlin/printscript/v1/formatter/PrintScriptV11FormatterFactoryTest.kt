package printscript.v1.formatter

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class PrintScriptV11FormatterFactoryTest {

    @Test
    fun `preserves V1_1 source when no rule is configured`() {
        val source =
            """
            const active: boolean = true;
            if (active) {
              println(readEnv("BEST_FOOTBALL_CLUB"));
            }
            """.trimIndent()

        assertEquals(source, formatSourceV11(source))
    }

    @Test
    fun `places the opening brace on the same line`() {
        val source =
            """
            let something: boolean = true;
            if (something)
            {
              println("Entered if");
            }
            """.trimIndent()

        val formatted = formatSourceV11(
            sourceCode = source,
            configuration = PrintScriptV11FormatterConfiguration(
                ifBracePlacement = IfBracePlacement.SAME_LINE,
            ),
        )

        assertEquals(
            expected =
            """
                let something: boolean = true;
                if (something) {
                  println("Entered if");
                }
            """.trimIndent(),
            actual = formatted,
        )
    }

    @Test
    fun `places the opening brace on the following line`() {
        val source =
            """
            let something: boolean = true;
            if (something) {
              println("Entered if");
            }
            """.trimIndent()

        val formatted = formatSourceV11(
            sourceCode = source,
            configuration = PrintScriptV11FormatterConfiguration(
                ifBracePlacement = IfBracePlacement.NEXT_LINE,
            ),
        )

        assertEquals(
            expected =
            """
                let something: boolean = true;
                if (something)
                {
                  println("Entered if");
                }
            """.trimIndent(),
            actual = formatted,
        )
    }

    @Test
    fun `indents nested if blocks`() {
        val source =
            """
            let something: boolean = true;
            if (something) {
              if (something) {
                println("Entered two ifs");
              }
            }
            """.trimIndent()

        val formatted = formatSourceV11(
            sourceCode = source,
            configuration = PrintScriptV11FormatterConfiguration(
                indentationInsideIf = 4u,
            ),
        )

        assertEquals(
            expected =
            """
                let something: boolean = true;
                if (something) {
                    if (something) {
                        println("Entered two ifs");
                    }
                }
            """.trimIndent(),
            actual = formatted,
        )
    }

    @Test
    fun `uses configured indentation when a nested brace moves to the following line`() {
        val source =
            """
            if (outer) {
              if (inner) {
                println("inside");
              }
            }
            """.trimIndent()

        val formatted = formatSourceV11(
            sourceCode = source,
            configuration = PrintScriptV11FormatterConfiguration(
                ifBracePlacement = IfBracePlacement.NEXT_LINE,
                indentationInsideIf = 4u,
            ),
        )

        assertEquals(
            expected =
            """
                if (outer)
                {
                    if (inner)
                    {
                        println("inside");
                    }
                }
            """.trimIndent(),
            actual = formatted,
        )
    }

    @Test
    fun `preserves carriage return line endings while changing indentation`() {
        val source = "if (active) {\r\n  println(\"inside\");\r\n}"

        val formatted = formatSourceV11(
            sourceCode = source,
            configuration = PrintScriptV11FormatterConfiguration(
                indentationInsideIf = 4u,
            ),
        )

        assertEquals(
            expected = "if (active) {\r\n    println(\"inside\");\r\n}",
            actual = formatted,
        )
    }

    @Test
    fun `additional rules keep priority over V1_1 rules`() {
        val formatted = formatSourceV11(
            sourceCode = "if (active) {\n}",
            configuration = PrintScriptV11FormatterConfiguration(
                ifBracePlacement = IfBracePlacement.NEXT_LINE,
            ),
            additionalFormattingRules = listOf(OpeningBraceMarkerRule),
        )

        assertEquals(
            expected = "if (active)~{\n}",
            actual = formatted,
        )
    }
}

private data object OpeningBraceMarkerRule : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.nextToken?.type == PrintScriptV1TokenType.LEFT_BRACE
    }

    override fun formatWhitespace(gap: TokenGap): String {
        return "~"
    }
}
