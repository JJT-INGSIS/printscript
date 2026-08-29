package printscript.cli.internal.pipeline

import printscript.cli.internal.operation.LanguageVersion
import printscript.source.SourceReader
import printscript.statement.StatementSource
import printscript.v1.lexer.PrintScriptV1LexerFactory
import printscript.v1.parser.PrintScriptV1ParserFactory

internal class StatementSourcePipeline {

    fun statementsFrom(sourceReader: SourceReader, version: LanguageVersion): StatementSource {
        return when (version) {
            LanguageVersion.V1_0 -> v1StatementsFrom(sourceReader)
        }
    }

    private fun v1StatementsFrom(sourceReader: SourceReader): StatementSource {
        return PrintScriptV1ParserFactory.create().parse(
            tokens = PrintScriptV1LexerFactory.create().tokenize(
                sourceReader = sourceReader,
            ),
        )
    }
}
