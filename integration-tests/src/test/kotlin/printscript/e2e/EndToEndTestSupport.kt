package printscript.e2e

import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramInput
import printscript.runtime.ProgramOutput
import printscript.source.SourceReader
import printscript.source.SourceReaderCreationResult
import printscript.source.SourceReaderFactory
import printscript.v1.interpreter.PrintScriptV11InterpreterFactory
import printscript.v1.interpreter.PrintScriptV1InterpreterFactory
import printscript.v1.lexer.PrintScriptV11LexerFactory
import printscript.v1.lexer.PrintScriptV1LexerFactory
import printscript.v1.parser.PrintScriptV11ParserFactory
import printscript.v1.parser.PrintScriptV1ParserFactory
import java.io.ByteArrayInputStream
import kotlin.test.assertIs

internal fun runV1Script(sourceCode: String): ProgramExecution {
    return runV1Script(
        sourceReader = SourceReaderFactory.fromString(sourceCode),
    )
}

internal fun runV1ScriptFromStream(sourceCode: String, bufferSizeInCharacters: Int): ProgramExecution {
    return runV1Script(
        sourceReader = streamReaderFor(sourceCode, bufferSizeInCharacters),
    )
}

private fun runV1Script(sourceReader: SourceReader): ProgramExecution {
    val output = RecordingProgramOutput()

    val lexer = PrintScriptV1LexerFactory.create()
    val parser = PrintScriptV1ParserFactory.create()
    val interpreter = PrintScriptV1InterpreterFactory.create(output = output)

    val tokens = lexer.tokenize(sourceReader = sourceReader)
    val statements = parser.parse(tokens = tokens)
    val result = interpreter.interpret(source = statements)

    return ProgramExecution(
        result = result,
        outputLines = output.lines(),
    )
}

internal fun runV11Script(
    sourceCode: String,
    input: ProgramInput = ProgramInput { error("Unexpected program input") },
    environmentVariables: EnvironmentVariableProvider = EnvironmentVariableProvider { null },
): ProgramExecution {
    return runV11Script(
        sourceReader = SourceReaderFactory.fromString(sourceCode),
        input = input,
        environmentVariables = environmentVariables,
    )
}

internal fun runV11ScriptFromStream(
    sourceCode: String,
    bufferSizeInCharacters: Int,
    input: ProgramInput = ProgramInput { error("Unexpected program input") },
    environmentVariables: EnvironmentVariableProvider = EnvironmentVariableProvider { null },
): ProgramExecution {
    return runV11Script(
        sourceReader = streamReaderFor(sourceCode, bufferSizeInCharacters),
        input = input,
        environmentVariables = environmentVariables,
    )
}

private fun runV11Script(
    sourceReader: SourceReader,
    input: ProgramInput,
    environmentVariables: EnvironmentVariableProvider,
): ProgramExecution {
    val output = RecordingProgramOutput()

    val lexer = PrintScriptV11LexerFactory.create()
    val parser = PrintScriptV11ParserFactory.create()
    val interpreter = PrintScriptV11InterpreterFactory.create(
        output = output,
        input = input,
        environmentVariables = environmentVariables,
    )

    val tokens = lexer.tokenize(sourceReader = sourceReader)
    val statements = parser.parse(tokens = tokens)
    val result = interpreter.interpret(source = statements)

    return ProgramExecution(
        result = result,
        outputLines = output.lines(),
    )
}

private fun streamReaderFor(sourceCode: String, bufferSizeInCharacters: Int): SourceReader {
    val creation = SourceReaderFactory.fromInputStream(
        inputStream = ByteArrayInputStream(sourceCode.toByteArray(Charsets.UTF_8)),
        bufferSizeInCharacters = bufferSizeInCharacters,
    )

    return assertIs<SourceReaderCreationResult.Success>(creation).reader
}

private class RecordingProgramOutput : ProgramOutput {

    private val emittedLines =
        mutableListOf<String>()

    override fun writeLine(line: String) {
        emittedLines.add(line)
    }

    fun lines(): List<String> {
        return emittedLines.toList()
    }
}
