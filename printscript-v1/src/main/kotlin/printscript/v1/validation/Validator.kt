package printscript.v1.validation

import printscript.statement.StatementSource

public fun interface Validator {

    public fun validate(source: StatementSource): ValidationResult
}
