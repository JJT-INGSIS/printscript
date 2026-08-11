package printscript.interpreter

import printscript.interpreter.environment.Environment
import printscript.interpreter.environment.MapEnvironment
import printscript.interpreter.expressions.ExpressionEvaluator
import printscript.interpreter.output.ProgramOutput
import printscript.interpreter.statements.AssignmentExecutor
import printscript.interpreter.statements.DeclarationExecutor
import printscript.interpreter.statements.PrintlnExecutor
import printscript.interpreter.value.RuntimeValue
import printscript.model.ast.expression.Expression
import printscript.model.ast.statement.AssignmentStatement
import printscript.model.ast.statement.PrintlnStatement
import printscript.model.ast.statement.Statement
import printscript.model.ast.statement.VariableDeclarationStatement
import printscript.statement.StatementReadResult
import printscript.statement.StatementSource

class Interpreter(
    private val output: ProgramOutput,
    override val environment: Environment = MapEnvironment(),
) : ExecutionContext {

    private val evaluator = ExpressionEvaluator(environment)

    private val declarationExecutor = DeclarationExecutor()
    private val assignmentExecutor = AssignmentExecutor()
    private val printlnExecutor = PrintlnExecutor()

    fun interpret(source: StatementSource): InterpretationResult {
        while (true) {
            val read: StatementReadResult = source.nextStatement()

            when (read) {
                is StatementReadResult.EndOfInput -> {
                    return InterpretationResult.Success
                }

                is StatementReadResult.Failure -> {
                    return InterpretationResult.ParseFailure(read.error)
                }

                is StatementReadResult.Success -> {
                    val executed: ExecutionResult<Unit> = execute(read.statement)
                    if (executed is ExecutionResult.Failure) {
                        return InterpretationResult.SemanticFailure(executed.error)
                    }
                }
            }
        }
    }

    private fun execute(statement: Statement): ExecutionResult<Unit> {
        return when (statement) {
            is VariableDeclarationStatement -> declarationExecutor.execute(statement, this)
            is AssignmentStatement -> assignmentExecutor.execute(statement, this)
            is PrintlnStatement -> printlnExecutor.execute(statement, this)
        }
    }

    override fun evaluate(expression: Expression): ExecutionResult<RuntimeValue> {
        return evaluator.evaluate(expression)
    }

    override fun emit(line: String) {
        output.emit(line)
    }
}