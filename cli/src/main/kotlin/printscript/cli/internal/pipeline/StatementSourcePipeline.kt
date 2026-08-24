package printscript.cli.internal.pipeline

import printscript.cli.internal.arguments.LanguageVersion
import printscript.lexer.PrintScriptLexerFactory
import printscript.parser.PrintScriptParserFactory
import printscript.source.SourceReader
import printscript.statement.StatementSource

/**
 * Conecta lector, lexer y parser según la versión pedida.
 *
 * Es el **único** lugar del CLI que nombra las factories de los otros
 * módulos. Los comandos reciben un [StatementSource] ya armado y no
 * saben que existen un lexer o un parser: eso es lo que permite que los
 * cuatro comandos compartan exactamente el mismo pipeline.
 */
internal class StatementSourcePipeline {

    fun statementsFrom(
        sourceReader: SourceReader,
        version: LanguageVersion,
    ): StatementSource {
        return when (version) {
            LanguageVersion.V1_0 -> v1StatementsFrom(sourceReader)
        }
    }

    private fun v1StatementsFrom(
        sourceReader: SourceReader,
    ): StatementSource {
        return PrintScriptParserFactory.createV1().parse(
            tokens = PrintScriptLexerFactory.createV1().tokenize(
                sourceReader = sourceReader,
            ),
        )
    }
}