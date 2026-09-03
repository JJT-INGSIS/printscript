package printscript.v1.formatter

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import kotlin.test.Test
import kotlin.test.assertEquals

class PrintScriptV1FormatterFactoryTest {

    @Test
    fun `preserves the complete source when no rule is configured`() {
        val source = "let value :number= 1;\r\nprintln (value);"

        assertEquals(expected = source, actual = formatSource(source))
    }

    @Test
    fun `removes spacing around equals without changing other gaps`() {
        val source =
            """
            let something: string= "a really cool thing";
            let another_thing: string ="another really cool thing";
            let twice_thing: string = "another really cool thing twice";
            let third_thing: string="another really cool thing three times";
            """.trimIndent()

        val formatted = formatSource(
            sourceCode = source,
            configuration = PrintScriptV1FormatterConfiguration(
                equalsSpacing = EqualsSpacing.WITHOUT_SPACES,
            ),
        )

        assertEquals(
            expected =
            """
                let something: string="a really cool thing";
                let another_thing: string="another really cool thing";
                let twice_thing: string="another really cool thing twice";
                let third_thing: string="another really cool thing three times";
            """.trimIndent(),
            actual = formatted,
        )
    }

    @Test
    fun `inserts spacing around equals without changing other gaps`() {
        val source =
            """
            let something: string= "a really cool thing";
            let another_thing: string ="another really cool thing";
            let twice_thing: string="another really cool thing twice";
            let third_thing: string = "another really cool thing three times";
            """.trimIndent()

        val formatted = formatSource(
            sourceCode = source,
            configuration = PrintScriptV1FormatterConfiguration(
                equalsSpacing = EqualsSpacing.SURROUNDED_BY_SPACES,
            ),
        )

        assertEquals(
            expected =
            """
                let something: string = "a really cool thing";
                let another_thing: string = "another really cool thing";
                let twice_thing: string = "another really cool thing twice";
                let third_thing: string = "another really cool thing three times";
            """.trimIndent(),
            actual = formatted,
        )
    }

    @Test
    fun `inserts spacing after declaration colon without changing other gaps`() {
        val source =
            """
            let something:string = "a really cool thing";
            let another_thing: string = "another really cool thing";
            let twice_thing : string = "another really cool thing twice";
            let third_thing :string="another really cool thing three times";
            """.trimIndent()

        val formatted = formatSource(
            sourceCode = source,
            configuration = PrintScriptV1FormatterConfiguration(
                enforceSpaceAfterColonInDeclaration = true,
            ),
        )

        assertEquals(
            expected =
            """
                let something: string = "a really cool thing";
                let another_thing: string = "another really cool thing";
                let twice_thing : string = "another really cool thing twice";
                let third_thing : string="another really cool thing three times";
            """.trimIndent(),
            actual = formatted,
        )
    }

    @Test
    fun `inserts spacing before declaration colon without changing other gaps`() {
        val source =
            """
            let something:string = "a really cool thing";
            let another_thing :string = "another really cool thing";
            let twice_thing : string = "another really cool thing twice";
            let third_thing: string="another really cool thing three times";
            """.trimIndent()

        val formatted = formatSource(
            sourceCode = source,
            configuration = PrintScriptV1FormatterConfiguration(
                enforceSpaceBeforeColonInDeclaration = true,
            ),
        )

        assertEquals(
            expected =
            """
                let something :string = "a really cool thing";
                let another_thing :string = "another really cool thing";
                let twice_thing : string = "another really cool thing twice";
                let third_thing : string="another really cool thing three times";
            """.trimIndent(),
            actual = formatted,
        )
    }

    @Test
    fun `enforces single spacing inside statements`() {
        val formatted = formatSource(
            sourceCode =
            """
                let something:      string="a really cool thing";
                println(something);
            """.trimIndent(),
            configuration = PrintScriptV1FormatterConfiguration(
                enforceSingleSpaceSeparation = true,
            ),
        )

        assertEquals(
            expected =
            """
                let something : string = "a really cool thing";
                println ( something );
            """.trimIndent(),
            actual = formatted,
        )
    }

    @Test
    fun `enforces spacing around binary operators`() {
        val formatted = formatSource(
            sourceCode = "let result: number = 5+4*3/2;",
            configuration = PrintScriptV1FormatterConfiguration(
                enforceSpaceAroundBinaryOperators = true,
            ),
        )

        assertEquals(
            expected = "let result: number = 5 + 4 * 3 / 2;",
            actual = formatted,
        )
    }

    @Test
    fun `does not treat unary operators as binary operators`() {
        val formatted = formatSource(
            sourceCode = "let result:number=-5+ +4;",
            configuration = PrintScriptV1FormatterConfiguration(
                enforceSpaceAroundBinaryOperators = true,
            ),
        )

        assertEquals(expected = "let result:number=-5 + +4;", actual = formatted)
    }

    @Test
    fun `enforces one line break between statements`() {
        val formatted = formatSource(
            sourceCode =
            "let first:number = 1;let second : number=2;\n" +
                "let third:number = 3;",
            configuration = PrintScriptV1FormatterConfiguration(
                enforceLineBreakAfterStatement = true,
            ),
        )

        assertEquals(
            expected =
            """
                let first:number = 1;
                let second : number=2;
                let third:number = 3;
            """.trimIndent(),
            actual = formatted,
        )
    }

    @Test
    fun `uses configured blank lines only after println`() {
        val source =
            """
            let something:string = "a really cool thing";
            println(something);
            println("in the way she moves");
            """.trimIndent()

        assertEquals(
            expected = source,
            actual = formatSource(
                sourceCode = source,
                configuration = PrintScriptV1FormatterConfiguration(
                    lineBreaksAfterPrintln = 0u,
                ),
            ),
        )
        assertEquals(
            expected = source.replace(
                "println(something);\n",
                "println(something);\n\n",
            ),
            actual = formatSource(
                sourceCode = source,
                configuration = PrintScriptV1FormatterConfiguration(
                    lineBreaksAfterPrintln = 1u,
                ),
            ),
        )
        assertEquals(
            expected = source.replace(
                "println(something);\n",
                "println(something);\n\n\n",
            ),
            actual = formatSource(
                sourceCode = source,
                configuration = PrintScriptV1FormatterConfiguration(
                    lineBreaksAfterPrintln = 2u,
                ),
            ),
        )
    }

    @Test
    fun `does not append line breaks after the final println`() {
        val formatted = formatSource(
            sourceCode = "println(1);",
            configuration = PrintScriptV1FormatterConfiguration(
                lineBreaksAfterPrintln = 2u,
            ),
        )

        assertEquals(expected = "println(1);", actual = formatted)
    }

    @Test
    fun `println line breaks take priority over the general statement rule`() {
        val formatted = formatSource(
            sourceCode = "println(1);let value:number=2;",
            configuration = PrintScriptV1FormatterConfiguration(
                enforceLineBreakAfterStatement = true,
                lineBreaksAfterPrintln = 2u,
            ),
        )

        assertEquals(
            expected = "println(1);\n\n\nlet value:number=2;",
            actual = formatted,
        )
    }

    @Test
    fun `additional rules have priority and are copied defensively`() {
        val additionalRules = mutableListOf<TokenGapFormattingRule>(
            ReplacingEqualsGapRule,
        )
        val formatter = PrintScriptV1FormatterFactory.create(
            configuration = PrintScriptV1FormatterConfiguration(
                equalsSpacing = EqualsSpacing.SURROUNDED_BY_SPACES,
            ),
            additionalFormattingRules = additionalRules,
        )
        additionalRules.clear()

        val formatted = formatSourceWith(
            formatter = formatter,
            sourceCode = "let value:number=1;",
        )

        assertEquals(expected = "let value:number~=~1;", actual = formatted)
    }
}

private data object ReplacingEqualsGapRule : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean {
        return gap.previousToken?.lexeme == "=" || gap.nextToken?.lexeme == "="
    }

    override fun formatWhitespace(gap: TokenGap): String {
        return "~"
    }
}
