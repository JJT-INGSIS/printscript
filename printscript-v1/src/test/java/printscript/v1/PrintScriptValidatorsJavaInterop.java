package printscript.v1;

import printscript.statement.StatementSource;
import printscript.v1.validation.PrintScriptV11ValidatorFactory;
import printscript.v1.validation.PrintScriptV1ValidatorFactory;
import printscript.v1.validation.ValidationResult;
import printscript.v1.validation.Validator;

public final class PrintScriptValidatorsJavaInterop {

    private PrintScriptValidatorsJavaInterop() {
    }

    public static ValidationResult validateVersionOne(StatementSource source) {
        Validator validator = PrintScriptV1ValidatorFactory.create();
        return validator.validate(source);
    }

    public static ValidationResult validateVersionOneOne(StatementSource source) {
        Validator validator = PrintScriptV11ValidatorFactory.create();
        return validator.validate(source);
    }
}
