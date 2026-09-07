package printscript.e2e

import printscript.formatter.FormattedChunkReadResult
import printscript.formatter.FormattedSource
import printscript.v1.formatter.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.PrintScriptV11FormatterFactory
import printscript.v1.formatter.PrintScriptV1FormatterConfiguration
import printscript.v1.formatter.PrintScriptV1FormatterFactory
import printscript.v1.lexer.PrintScriptV11FormattingLexerFactory
import printscript.v1.lexer.PrintScriptV1FormattingLexerFactory

internal fun formatV1ScriptFromStream(
    sourceCode: String,
    bufferSizeInCharacters: Int,
    configuration: PrintScriptV1FormatterConfiguration,
): ProgramFormatting {
    val formattingTokens = PrintScriptV1FormattingLexerFactory.create()
        .tokenize(sourceReader = streamReaderFor(sourceCode, bufferSizeInCharacters))

    return collectFormattedText(
        source = PrintScriptV1FormatterFactory.create(configuration).format(formattingTokens),
    )
}

internal fun formatV11ScriptFromStream(
    sourceCode: String,
    bufferSizeInCharacters: Int,
    configuration: PrintScriptV11FormatterConfiguration,
): ProgramFormatting {
    val formattingTokens = PrintScriptV11FormattingLexerFactory.create()
        .tokenize(sourceReader = streamReaderFor(sourceCode, bufferSizeInCharacters))

    return collectFormattedText(
        source = PrintScriptV11FormatterFactory.create(configuration).format(formattingTokens),
    )
}

private tailrec fun collectFormattedText(source: FormattedSource, formattedText: String = ""): ProgramFormatting {
    return when (val chunk = source.nextFormattedChunk()) {
        FormattedChunkReadResult.EndOfInput ->
            ProgramFormatting.Success(formattedText)

        is FormattedChunkReadResult.Failure ->
            ProgramFormatting.Failure(chunk.error)

        is FormattedChunkReadResult.Success ->
            collectFormattedText(
                source = chunk.remainingSource,
                formattedText = formattedText + chunk.formattedText,
            )
    }
}
