package printscript.e2e

import printscript.interpreter.ExecutionResult
import printscript.interpreter.InterpretationResult
import printscript.interpreter.StatementExecutionContext
import printscript.interpreter.StatementExecutor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.parser.ParsingContext
import printscript.parser.ParsingResult
import printscript.parser.StatementParser
import printscript.parser.orReturn
import printscript.runtime.Environment
import printscript.runtime.ProgramOutput
import printscript.statement.Statement
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType
import printscript.v1.interpreter.PrintScriptV1InterpreterFactory
import printscript.v1.interpreter.PrintScriptV1SemanticError
import printscript.v1.parser.PrintScriptV1ParserFactory
import printscript.v1.token.PrintScriptV1TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptVersionCompatibilityTest {

    private val sharedCorpus = listOf(
        """
            let name: string = "Joe";
            println("hello " + name);
        """.trimIndent(),
        """
            let result: number = (2 + 3) * 4;
            println(result);
        """.trimIndent(),
        """
            let value: number = 12;
            value = value / 4;
            println("Result: " + value);
        """.trimIndent(),
    )

    @Test
    fun `every V1 program in the shared corpus behaves identically under V1_1`() {
        for (sourceCode in sharedCorpus) {
            val v1Execution = runV1Script(sourceCode)
            val v11Execution = runV11Script(sourceCode)

            assertEquals(
                expected = v1Execution.result,
                actual = v11Execution.result,
                message = sourceCode,
            )
            assertEquals(
                expected = v1Execution.outputLines,
                actual = v11Execution.outputLines,
                message = sourceCode,
            )
        }
    }

    @Test
    fun `if is a V1_1-only addition`() {
        val sourceCode = """
            let active: boolean = true;
            if (active) { println("yes"); }
        """.trimIndent()

        assertIs<InterpretationResult.ParseFailure>(runV1Script(sourceCode).result)
        assertEquals(expected = InterpretationResult.Success, actual = runV11Script(sourceCode).result)
    }

    @Test
    fun `const is a V1_1-only addition`() {
        val sourceCode = "const value: number = 1; println(value);"

        assertIs<InterpretationResult.ParseFailure>(runV1Script(sourceCode).result)
        assertEquals(expected = InterpretationResult.Success, actual = runV11Script(sourceCode).result)
    }

    @Test
    fun `boolean literals are a V1_1-only addition`() {
        val sourceCode = "println(true);"

        val v1Failure = assertIs<InterpretationResult.SemanticFailure>(runV1Script(sourceCode).result)

        assertIs<PrintScriptV1SemanticError.UndeclaredVariable>(v1Failure.error)
        assertEquals(expected = InterpretationResult.Success, actual = runV11Script(sourceCode).result)
    }

    @Test
    fun `readInput is a V1_1-only addition`() {
        val sourceCode = """println(readInput("n"));"""

        assertIs<InterpretationResult.ParseFailure>(runV1Script(sourceCode).result)
    }

    @Test
    fun `a third party statement extension works through the parser and the interpreter together`() {
        val tokens = ListTokenSource(
            listOf(
                token(PrintScriptV1TokenType.LET),
                token(PrintScriptV1TokenType.IDENTIFIER, "value"),
                token(PrintScriptV1TokenType.COLON),
                token(PrintScriptV1TokenType.NUMBER_TYPE),
                token(PrintScriptV1TokenType.ASSIGN),
                token(PrintScriptV1TokenType.NUMBER_LITERAL, "1"),
                token(PrintScriptV1TokenType.SEMICOLON),
                token(HaltTokenType, "halt"),
                token(PrintScriptV1TokenType.SEMICOLON),
                token(PrintScriptV1TokenType.PRINTLN),
                token(PrintScriptV1TokenType.LEFT_PAREN),
                token(PrintScriptV1TokenType.IDENTIFIER, "value"),
                token(PrintScriptV1TokenType.RIGHT_PAREN),
                token(PrintScriptV1TokenType.SEMICOLON),
                token(PrintScriptV1TokenType.EOF),
            ),
        )

        val output = RecordingProgramOutput()
        val parser = PrintScriptV1ParserFactory.create(
            additionalStatementParsers = listOf(HaltParser),
        )
        val interpreter = PrintScriptV1InterpreterFactory.create(
            output = output,
            additionalStatementExecutors = listOf(HaltExecutor(output)),
        )

        val statements = parser.parse(tokens = tokens)
        val result = interpreter.interpret(source = statements)

        assertEquals(expected = InterpretationResult.Success, actual = result)
        assertEquals(expected = listOf("halted", "1"), actual = output.lines())
    }

    private fun token(type: TokenType, lexeme: String = ""): Token {
        return Token(type = type, lexeme = lexeme, span = ANY_SPAN)
    }

    private object HaltTokenType : TokenType

    private data class HaltStatement(override val span: SourceSpan) : Statement

    private object HaltParser : StatementParser {

        override val startTokenType: TokenType = HaltTokenType

        override fun parseStatement(context: ParsingContext): ParsingResult<Statement> {
            val keyword = context.expect(HaltTokenType)
                .orReturn { return it }

            val terminator = keyword.resultingContext.expect(PrintScriptV1TokenType.SEMICOLON)
                .orReturn { return it }

            return ParsingResult.Success(
                value = HaltStatement(
                    span = SourceSpan(
                        start = keyword.value.span.start,
                        end = terminator.value.span.end,
                    ),
                ),
                resultingContext = terminator.resultingContext,
            )
        }
    }

    private class HaltExecutor(private val output: ProgramOutput) : StatementExecutor<Environment> {

        override fun supportsStatement(statement: Statement): Boolean {
            return statement is HaltStatement
        }

        override fun executeStatement(
            statement: Statement,
            context: StatementExecutionContext<Environment>,
        ): ExecutionResult<Environment> {
            output.writeLine("halted")

            return ExecutionResult.Success(context.state)
        }
    }

    private class ListTokenSource(
        private val tokens: List<Token>,
    ) : TokenSource {

        override fun nextToken(): TokenReadResult {
            val token = tokens.first()
            val remaining = if (tokens.size == 1) tokens else tokens.drop(1)

            return TokenReadResult.Success(
                token = token,
                remainingSource = ListTokenSource(remaining),
            )
        }
    }

    private companion object {
        val ANY_SPAN = SourceSpan(
            start = SourcePosition(line = 1, column = 1, offset = 0),
            end = SourcePosition(line = 1, column = 1, offset = 0),
        )
    }
}
