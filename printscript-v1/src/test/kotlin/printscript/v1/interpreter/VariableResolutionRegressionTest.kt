package printscript.v1.interpreter

import printscript.ast.DeclaredType
import printscript.interpreter.InterpretationResult
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramInput
import printscript.source.SourceReaderFactory
import printscript.v1.lexer.PrintScriptV11LexerFactory
import printscript.v1.parser.PrintScriptV11ParserFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class VariableResolutionRegressionTest {

    @Test
    fun `expressions and if conditions preserve the undeclared identifier span`() {
        listOf("println(target);", "if (target) {}").forEach { statement ->
            val prefix = "let other: boolean = true;"
            val error = failureIn("$prefix\n$statement")
            assertEquals(
                PrintScriptV1SemanticError.UndeclaredVariable("target", targetSpan(prefix, statement)),
                error,
            )
        }
    }

    @Test
    fun `expressions and if conditions preserve the uninitialized identifier span`() {
        listOf("println(target);", "if (target) {}").forEach { statement ->
            val prefix = "let target: boolean;"
            val error = failureIn("$prefix\n$statement")
            assertEquals(
                PrintScriptV1SemanticError.UninitializedVariable("target", targetSpan(prefix, statement)),
                error,
            )
        }
    }

    @Test
    fun `if conditions keep their boolean type check and identifier span`() {
        val prefix = "let target: number = 1;"
        val statement = "if (target) {}"

        assertEquals(
            PrintScriptV1SemanticError.InvalidIfCondition("target", DeclaredType.NUMBER, targetSpan(prefix, statement)),
            failureIn("$prefix\n$statement"),
        )
    }

    private fun failureIn(source: String): PrintScriptV1SemanticError {
        val tokens = PrintScriptV11LexerFactory.create().tokenize(SourceReaderFactory.fromString(source))
        val statements = PrintScriptV11ParserFactory.create().parse(tokens)
        val interpreter = PrintScriptV11InterpreterFactory.create(
            output = InMemoryOutput(),
            input = ProgramInput { error("Unexpected input") },
            environmentVariables = EnvironmentVariableProvider { error("Unexpected environment lookup") },
        )
        return assertIs<PrintScriptV1SemanticError>(
            assertIs<InterpretationResult.SemanticFailure>(interpreter.interpret(statements)).error,
        )
    }

    private fun targetSpan(prefix: String, statement: String): SourceSpan {
        val index = statement.indexOf("target")
        val start = SourcePosition(line = 2, column = index + 1, offset = (prefix.length + 1 + index).toLong())
        val end = start.copy(column = start.column + "target".length, offset = start.offset + "target".length)
        return SourceSpan(start, end)
    }
}
