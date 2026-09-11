package printscript.v1.formatter

import printscript.formatter.TokenGap
import printscript.formatter.TokenGapFormattingRule
import printscript.formatter.WhitespaceFormattingResult
import printscript.v1.formatter.configuration.IfBracePlacement
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfiguration
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class PrintScriptFormatterCompositionTest {

    @Test
    fun `inserts line breaks and indents them in the same pass`() {
        val configuration = PrintScriptV11FormatterConfiguration(
            v1Configuration = PrintScriptV1FormatterConfiguration(enforceLineBreakAfterStatement = true),
            indentationInsideIf = 2,
        )
        val source = "if(active){println(1);println(2);}"
        val expected = "if(active){println(1);\n  println(2);\n}"

        assertEquals(expected, formatSourceV11(source, configuration))
        assertEquals(expected, formatSourceV11(expected, configuration))
    }

    @Test
    fun `println blank lines and indentation override existing gaps together`() {
        val configuration = PrintScriptV11FormatterConfiguration(
            v1Configuration = PrintScriptV1FormatterConfiguration(
                enforceLineBreakAfterStatement = true,
                blankLinesAfterPrintln = 1,
            ),
            indentationInsideIf = 2,
        )
        val source = "if(active){\nprintln(1);\nprintln(2);\n}"
        val expected = "if(active){\n  println(1);\n\n  println(2);\n\n}"

        assertEquals(expected, formatSourceV11(source, configuration))
        assertEquals(expected, formatSourceV11(expected, configuration))
    }

    @Test
    fun `recognizes the first println in branches nested blocks and after a closing brace`() {
        val source = "if(a){if(b){println(1);x=1;}println(2);x=2;}else{println(3);x=3;}println(4);x=4;"
        val expected = "if(a){if(b){println(1);\n\nx=1;}println(2);\n\nx=2;}else{println(3);\n\nx=3;}" +
            "println(4);\n\nx=4;"
        val configuration = PrintScriptV11FormatterConfiguration(
            v1Configuration = PrintScriptV1FormatterConfiguration(blankLinesAfterPrintln = 1),
        )

        assertEquals(expected, formatSourceV11(source, configuration))
    }

    @Test
    fun `does not mistake declarations after a block for println statements`() {
        val source = "if(a){println(1);}let x:number=1;println(2);"
        val configuration = PrintScriptV11FormatterConfiguration(
            v1Configuration = PrintScriptV1FormatterConfiguration(blankLinesAfterPrintln = 1),
        )

        assertEquals("if(a){println(1);\n\n}let x:number=1;println(2);", formatSourceV11(source, configuration))
    }

    @Test
    fun `spaces binary operators after boolean literals without changing unary signs`() {
        val configuration = PrintScriptV11FormatterConfiguration(
            v1Configuration = PrintScriptV1FormatterConfiguration(enforceSpaceAroundBinaryOperators = true),
        )

        assertEquals(
            "println(true + \"text\");println(false + \"text\");println(-1 + +2);",
            formatSourceV11("println(true+\"text\");println(false+\"text\");println(-1++2);", configuration),
        )
    }

    @Test
    fun `aligns braces with the output if position after earlier rules insert a line break`() {
        val configuration = PrintScriptV11FormatterConfiguration(
            v1Configuration = PrintScriptV1FormatterConfiguration(enforceLineBreakAfterStatement = true),
            ifBracePlacement = IfBracePlacement.NEXT_LINE,
        )
        val expected = "println(1);\nif(active)\n{}"

        assertEquals(expected, formatSourceV11("println(1);if(active){}", configuration))
        assertEquals(expected, formatSourceV11(expected, configuration))
    }

    @Test
    fun `external rules replace the whole gap before built in line breaks and indentation`() {
        val configuration = PrintScriptV11FormatterConfiguration(
            v1Configuration = PrintScriptV1FormatterConfiguration(
                enforceLineBreakAfterStatement = true,
                blankLinesAfterPrintln = 1,
            ),
            indentationInsideIf = 2,
        )
        val source = "if(a){println(1);println(2);}"

        assertEquals(
            "if(a){println(1);\n\tprintln(2);\n\t}",
            formatSourceV11(source, configuration, listOf(ExternalStatementGapRule)),
        )
    }

    @Test
    fun `rule combinations are idempotent for nested empty and sibling blocks`() {
        val source = "if(a){\r\nif(b){println(true+\"x\");println(2);}else{}\r\n}" +
            "println(3);if(c){\nprintln(false+\"y\");\n}"
        val configurations = listOf(null, IfBracePlacement.SAME_LINE, IfBracePlacement.NEXT_LINE).flatMap { placement ->
            listOf(null, 0, 2).flatMap { indentation ->
                baseConfigurations().map { base ->
                    PrintScriptV11FormatterConfiguration(base, placement, indentation)
                }
            }
        }

        configurations.forEach { configuration ->
            val formatted = formatSourceV11(source, configuration)
            assertEquals(formatted, formatSourceV11(formatted, configuration), configuration.toString())
        }
    }

    @Test
    fun `V1 combinations preserve println priority and are idempotent`() {
        val source = "println(1+2);let x:number=1;println(x);"
        baseConfigurations().forEach { configuration ->
            val formatted = formatSource(source, configuration)
            assertEquals(formatted, formatSource(formatted, configuration), configuration.toString())
        }
    }

    private fun baseConfigurations(): List<PrintScriptV1FormatterConfiguration> {
        return listOf(null, 0, 1).flatMap { blankLines ->
            listOf(false, true).map { singleSpace ->
                PrintScriptV1FormatterConfiguration(
                    enforceLineBreakAfterStatement = true,
                    enforceSpaceAroundBinaryOperators = true,
                    enforceSingleSpaceSeparation = singleSpace,
                    blankLinesAfterPrintln = blankLines,
                )
            }
        }
    }
}

private data object ExternalStatementGapRule : TokenGapFormattingRule {

    override fun supports(gap: TokenGap): Boolean = gap.previousToken?.type == PrintScriptV1TokenType.SEMICOLON

    override fun formatWhitespace(gap: TokenGap): WhitespaceFormattingResult {
        return WhitespaceFormattingResult.Success("\n\t")
    }
}
