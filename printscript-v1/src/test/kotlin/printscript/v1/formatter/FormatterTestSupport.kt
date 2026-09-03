package printscript.v1.formatter

import printscript.formatter.FormattedChunkReadResult
import printscript.formatter.Formatter
import printscript.formatter.TokenGapFormattingRule
import printscript.source.SourceReaderFactory
import printscript.token.TokenSource
import printscript.v1.lexer.PrintScriptV1FormattingLexerFactory

internal fun formatSource(
    sourceCode: String,
    configuration: PrintScriptV1FormatterConfiguration =
        PrintScriptV1FormatterFactory.defaultConfiguration(),
    additionalFormattingRules: List<TokenGapFormattingRule> = emptyList(),
): String {
    val tokens = PrintScriptV1FormattingLexerFactory.create().tokenize(
        SourceReaderFactory.fromString(sourceCode),
    )
    val formatter = PrintScriptV1FormatterFactory.create(
        configuration = configuration,
        additionalFormattingRules = additionalFormattingRules,
    )

    return collectFormattedText(formatter, tokens)
}

internal fun formatSourceWith(formatter: Formatter, sourceCode: String): String {
    val tokens = PrintScriptV1FormattingLexerFactory.create().tokenize(
        SourceReaderFactory.fromString(sourceCode),
    )

    return collectFormattedText(formatter, tokens)
}

private fun collectFormattedText(formatter: Formatter, tokens: TokenSource): String {
    var source = formatter.format(tokens)
    val formattedText = StringBuilder()

    while (true) {
        when (val result = source.nextFormattedChunk()) {
            is FormattedChunkReadResult.Success -> {
                formattedText.append(result.formattedText)
                source = result.remainingSource
            }

            is FormattedChunkReadResult.Failure ->
                error("Unexpected formatting failure: ${result.error}")

            FormattedChunkReadResult.EndOfInput ->
                return formattedText.toString()
        }
    }
}
