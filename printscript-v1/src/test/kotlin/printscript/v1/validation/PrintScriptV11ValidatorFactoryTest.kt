package printscript.v1.validation

import printscript.v1.interpreter.PrintScriptV1SemanticError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV11ValidatorFactoryTest {

    @Test
    fun `accepts typed reads without supplying any input or environment provider`() {
        val source = """
            const active: boolean = readInput("Active?");
            let count: number = readEnv("PRINTSCRIPT_VALIDATION_ONLY_NUMBER") + 1;
            let name: string = readInput("Name?");
            println(readInput(name));
            println(readEnv("PRINTSCRIPT_VALIDATION_ONLY_TEXT"));
            if (active) { println(count); } else { println(name); }
        """.trimIndent()

        assertEquals(ValidationResult.Success, validateV11(source))
    }

    @Test
    fun `validates nested input prompts and environment names`() {
        assertEquals(ValidationResult.Success, validateV11("println(readInput(readEnv(\"PROMPT\")));"))
        validateV11("println(readInput(1));").semanticError<PrintScriptV1SemanticError.InvalidInputPrompt>()
        validateV11("println(readEnv(false));")
            .semanticError<PrintScriptV1SemanticError.InvalidEnvironmentVariableName>()
        validateV11("println(readInput(missing));").semanticError<PrintScriptV1SemanticError.UndeclaredVariable>()
    }

    @Test
    fun `reads inside println remain strings even when followed by multiplication`() {
        listOf("readInput(\"n\")", "readEnv(\"N\")").forEach { read ->
            validateV11("println($read * 2);").semanticError<PrintScriptV1SemanticError.InvalidBinaryOperands>()
            assertEquals(ValidationResult.Success, validateV11("let value: number = $read * 2;"))
        }
    }

    @Test
    fun `checks the semantic validity of both branches regardless of a literal condition value`() {
        validateV11("let active: boolean = true; if (active) {} else { println(missing); }")
            .semanticError<PrintScriptV1SemanticError.UndeclaredVariable>()
        validateV11("let active: boolean = false; if (active) { let count: number = \"bad\"; }")
            .semanticError<PrintScriptV1SemanticError.TypeMismatch>()
    }

    @Test
    fun `checks parsing in an unexecuted branch`() {
        assertIs<ValidationResult.ParseFailure>(
            validateV11("let active: boolean = true; if (active) {} else { println(1) }"),
        )
    }

    @Test
    fun `requires an initialized boolean condition`() {
        validateV11("if (missing) {}").semanticError<PrintScriptV1SemanticError.UndeclaredVariable>()
        validateV11("let active: boolean; if (active) {}")
            .semanticError<PrintScriptV1SemanticError.UninitializedVariable>()
        validateV11("let active: number = 1; if (active) {}")
            .semanticError<PrintScriptV1SemanticError.InvalidIfCondition>()
    }

    @Test
    fun `rejects constant reassignment in either branch`() {
        validateV11("const value: number = 1; value = 2;")
            .semanticError<PrintScriptV1SemanticError.ConstantReassignment>()
        validateV11("const value: number = 1; let active: boolean = true; if (active) {} else { value = 2; }")
            .semanticError<PrintScriptV1SemanticError.ConstantReassignment>()
    }

    @Test
    fun `allows independent declarations in inner scopes and sibling branches`() {
        val source = """
            const value: number = 1;
            let active: boolean = true;
            if (active) {
                let value: string = "inner";
                value = "changed";
                if (active) { const value: boolean = false; println(value); }
                println(value);
            } else {
                let value: boolean = true;
                println(value);
            }
            println(value + 1);
        """.trimIndent()

        assertEquals(ValidationResult.Success, validateV11(source))
    }

    @Test
    fun `rejects a duplicate in the same block`() {
        validateV11("let active: boolean = true; if (active) { let value: number; let value: string; }")
            .semanticError<PrintScriptV1SemanticError.AlreadyDeclaredVariable>()
    }

    @Test
    fun `does not make local declarations visible outside their branch`() {
        listOf(
            "let active: boolean = true; if (active) { let local: number = 1; } println(local);",
            "let active: boolean = true; if (active) { let local: number = 1; } else { println(local); }",
        ).forEach { source ->
            validateV11(source).semanticError<PrintScriptV1SemanticError.UndeclaredVariable>()
        }
    }

    @Test
    fun `only initialization guaranteed by both branches survives`() {
        val prefix = "let active: boolean = true; let value: number;"

        assertEquals(
            ValidationResult.Success,
            validateV11("$prefix if (active) { value = 1; } else { value = 2; } println(value);"),
        )
        listOf(
            "$prefix if (active) { value = 1; } println(value);",
            "$prefix if (active) {} else { value = 2; } println(value);",
            "$prefix if (active) { value = 1; } else { println(value); }",
        ).forEach { source ->
            validateV11(source).semanticError<PrintScriptV1SemanticError.UninitializedVariable>()
        }
    }

    @Test
    fun `initializing a shadow does not initialize the outer declaration`() {
        val source = """
            let active: boolean = true;
            let value: number;
            if (active) { let value: number = 1; } else { let value: number = 2; }
            println(value);
        """.trimIndent()

        validateV11(source).semanticError<PrintScriptV1SemanticError.UninitializedVariable>()
    }

    @Test
    fun `an inner uninitialized declaration hides an initialized outer declaration`() {
        val source = """
            let value: number = 1;
            let active: boolean = true;
            if (active) { let value: number; println(value); }
        """.trimIndent()

        validateV11(source)
            .semanticError<PrintScriptV1SemanticError.UninitializedVariable>()
    }

    @Test
    fun `preserves initialization before an if and merges nested branches`() {
        assertEquals(
            ValidationResult.Success,
            validateV11("let value: number = 1; let active: boolean = true; if (active) {} println(value);"),
        )
        val source = """
            let active: boolean = true;
            let value: number;
            if (active) {
                if (active) { value = 1; } else { value = 2; }
            } else { value = 1; }
            println(value);
        """.trimIndent()

        assertEquals(ValidationResult.Success, validateV11(source))
    }
}
