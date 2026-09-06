package printscript.v1.validation.internal

import printscript.interpreter.InterpretationResult
import printscript.interpreter.InterpreterFactory
import printscript.interpreter.StatementExecutor
import printscript.statement.StatementSource
import printscript.v1.validation.ValidationResult
import printscript.v1.validation.Validator

internal class StaticValidator(
    validators: List<StatementExecutor<ValidationEnvironment>>,
) : Validator {

    private val engine = InterpreterFactory.create(
        initialState = ValidationEnvironment(),
        statementExecutors = validators,
    )

    override fun validate(source: StatementSource): ValidationResult {
        return when (val result = engine.interpret(source)) {
            InterpretationResult.Success -> ValidationResult.Success
            is InterpretationResult.ParseFailure -> ValidationResult.ParseFailure(result.error)
            is InterpretationResult.SemanticFailure -> ValidationResult.SemanticFailure(result.error)
        }
    }
}
