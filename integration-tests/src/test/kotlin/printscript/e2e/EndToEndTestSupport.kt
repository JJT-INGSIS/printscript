package printscript.e2e

import printscript.interpreter.InterpretationResult
import printscript.interpreter.PrintScriptInterpreterFactory
import printscript.interpreter.output.ProgramOutput
import printscript.lexer.PrintScriptLexerFactory
import printscript.parser.PrintScriptParserFactory
import java.io.StringReader

internal data class ProgramExecution(
    val result: InterpretationResult,
    val outputLines: List<String>,
)

internal fun runV1Script(
    sourceCode: String,
): ProgramExecution {
    val output = RecordingProgramOutput()

    val lexer = PrintScriptLexerFactory.createV1()
    val parser = PrintScriptParserFactory.createV1()
    val interpreter =
        PrintScriptInterpreterFactory.createV1(
            output = output,
        )

    return StringReader(sourceCode).use { reader ->
        val tokens = lexer.tokenize(
            inputSource = reader,
        )

        val statements = parser.parse(
            tokens = tokens,
        )

        val result = interpreter.interpret(
            source = statements,
        )

        ProgramExecution(
            result = result,
            outputLines = output.lines(),
        )
    }
}

private class RecordingProgramOutput : ProgramOutput {

    private val emittedLines =
        mutableListOf<String>()

    override fun writeLine(
        line: String,
    ) {
        emittedLines.add(line)
    }

    fun lines(): List<String> {
        return emittedLines.toList()
    }
}