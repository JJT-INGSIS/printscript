package printscript.e2e

import printscript.interpreter.Interpreter
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramInput
import printscript.runtime.ProgramOutput
import printscript.source.SourceReader
import printscript.source.SourceReaderCreationResult
import printscript.source.SourceReaderFactory
import printscript.statement.StatementSource
import printscript.v1.interpreter.PrintScriptV11InterpreterFactory
import printscript.v1.interpreter.PrintScriptV1InterpreterFactory
import printscript.v1.lexer.PrintScriptV11LexerFactory
import printscript.v1.lexer.PrintScriptV1LexerFactory
import printscript.v1.parser.PrintScriptV11ParserFactory
import printscript.v1.parser.PrintScriptV1ParserFactory
import printscript.v1.validation.PrintScriptV11ValidatorFactory
import printscript.v1.validation.ValidationResult
import java.io.ByteArrayInputStream
import kotlin.test.assertIs

internal const val SINGLE_CHARACTER_BUFFER: Int = 1
internal const val TWO_CHARACTER_BUFFER: Int = 2

internal fun unexpectedProgramInput(): ProgramInput {
    return ProgramInput { error("El programa no debería pedir entrada") }
}

internal fun unexpectedEnvironmentVariables(): EnvironmentVariableProvider {
    return EnvironmentVariableProvider { error("El programa no debería leer variables de entorno") }
}

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

internal fun runV11Script(
    sourceCode: String,
    input: ProgramInput = unexpectedProgramInput(),
    environmentVariables: EnvironmentVariableProvider = unexpectedEnvironmentVariables(),
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
    input: ProgramInput = unexpectedProgramInput(),
    environmentVariables: EnvironmentVariableProvider = unexpectedEnvironmentVariables(),
): ProgramExecution {
    return runV11Script(
        sourceReader = streamReaderFor(sourceCode, bufferSizeInCharacters),
        input = input,
        environmentVariables = environmentVariables,
    )
}

internal fun validateV11Script(sourceCode: String): ValidationResult {
    return validateV11Script(
        sourceReader = SourceReaderFactory.fromString(sourceCode),
    )
}

internal fun validateV11ScriptFromStream(sourceCode: String, bufferSizeInCharacters: Int): ValidationResult {
    return validateV11Script(
        sourceReader = streamReaderFor(sourceCode, bufferSizeInCharacters),
    )
}

private fun runV1Script(sourceReader: SourceReader): ProgramExecution {
    return programExecution(
        statements = v1StatementsFrom(sourceReader),
        interpreterUsing = { output -> PrintScriptV1InterpreterFactory.create(output = output) },
    )
}

private fun runV11Script(
    sourceReader: SourceReader,
    input: ProgramInput,
    environmentVariables: EnvironmentVariableProvider,
): ProgramExecution {
    return programExecution(
        statements = v11StatementsFrom(sourceReader),
        interpreterUsing = { output ->
            PrintScriptV11InterpreterFactory.create(
                output = output,
                input = input,
                environmentVariables = environmentVariables,
            )
        },
    )
}

private fun validateV11Script(sourceReader: SourceReader): ValidationResult {
    return PrintScriptV11ValidatorFactory.create().validate(v11StatementsFrom(sourceReader))
}

private fun programExecution(
    statements: StatementSource,
    interpreterUsing: (ProgramOutput) -> Interpreter,
): ProgramExecution {
    val output = RecordingProgramOutput()
    val result = interpreterUsing(output).interpret(source = statements)

    return ProgramExecution(
        result = result,
        outputLines = output.lines(),
    )
}

internal fun v1StatementsFrom(sourceReader: SourceReader): StatementSource {
    return PrintScriptV1ParserFactory.create().parse(
        tokens = PrintScriptV1LexerFactory.create().tokenize(sourceReader = sourceReader),
    )
}

internal fun v11StatementsFrom(sourceReader: SourceReader): StatementSource {
    return PrintScriptV11ParserFactory.create().parse(
        tokens = PrintScriptV11LexerFactory.create().tokenize(sourceReader = sourceReader),
    )
}

internal fun streamReaderFor(sourceCode: String, bufferSizeInCharacters: Int): SourceReader {
    val creation = SourceReaderFactory.fromInputStream(
        inputStream = ByteArrayInputStream(sourceCode.toByteArray(Charsets.UTF_8)),
        bufferSizeInCharacters = bufferSizeInCharacters,
    )

    return assertIs<SourceReaderCreationResult.Success>(creation).reader
}
