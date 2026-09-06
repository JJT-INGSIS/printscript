package printscript.v1.validation

import printscript.interpreter.SemanticError
import printscript.source.SourceReaderFactory
import printscript.statement.StatementSource
import printscript.v1.lexer.PrintScriptV11LexerFactory
import printscript.v1.lexer.PrintScriptV1LexerFactory
import printscript.v1.parser.PrintScriptV11ParserFactory
import printscript.v1.parser.PrintScriptV1ParserFactory
import kotlin.test.assertIs

internal fun statementsFrom(source: String, versionOneOne: Boolean = true): StatementSource {
    val lexer = if (versionOneOne) PrintScriptV11LexerFactory.create() else PrintScriptV1LexerFactory.create()
    val parser = if (versionOneOne) PrintScriptV11ParserFactory.create() else PrintScriptV1ParserFactory.create()
    return parser.parse(lexer.tokenize(SourceReaderFactory.fromString(source)))
}

internal fun validateV1(source: String): ValidationResult {
    return PrintScriptV1ValidatorFactory.create().validate(statementsFrom(source, versionOneOne = false))
}

internal fun validateV11(source: String): ValidationResult {
    return PrintScriptV11ValidatorFactory.create().validate(statementsFrom(source))
}

internal inline fun <reified E : SemanticError> ValidationResult.semanticError(): E {
    return assertIs<E>(assertIs<ValidationResult.SemanticFailure>(this).error)
}
