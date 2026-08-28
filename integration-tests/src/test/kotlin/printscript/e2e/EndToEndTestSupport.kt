package printscript.e2e

import printscript.source.SourceReaderFactory
import printscript.v1.interpreter.PrintScriptV1InterpreterFactory
import printscript.v1.interpreter.PrintScriptV1ProgramOutput
import printscript.v1.lexer.PrintScriptV1LexerFactory
import printscript.v1.parser.PrintScriptV1ParserFactory

internal fun runV1Script(sourceCode: String): ProgramExecution {
    val output = RecordingProgramOutput()

    val lexer = PrintScriptV1LexerFactory.create()
    val parser = PrintScriptV1ParserFactory.create()
    val interpreter =
        PrintScriptV1InterpreterFactory.create(
            output = output,
        )

    val tokens = lexer.tokenize(
        sourceReader =
        SourceReaderFactory.fromString(sourceCode),
    )

    val statements = parser.parse(
        tokens = tokens,
    )

    val result = interpreter.interpret(
        source = statements,
    )

    return ProgramExecution(
        result = result,
        outputLines = output.lines(),
    )
}

private class RecordingProgramOutput : PrintScriptV1ProgramOutput {

    private val emittedLines =
        mutableListOf<String>()

    override fun writeLine(line: String) {
        emittedLines.add(line)
    }

    fun lines(): List<String> {
        return emittedLines.toList()
    }
}
