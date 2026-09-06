package printscript.v1;

import org.junit.jupiter.api.Test;
import printscript.interpreter.InterpretationResult;
import printscript.interpreter.Interpreter;
import printscript.lexer.Lexer;
import printscript.parser.Parser;
import printscript.runtime.ProgramOutput;
import printscript.source.SourceReader;
import printscript.source.SourceReaderFactory;
import printscript.statement.StatementSource;
import printscript.token.TokenSource;
import printscript.v1.interpreter.PrintScriptV1InterpreterFactory;
import printscript.v1.lexer.PrintScriptV1LexerFactory;
import printscript.v1.parser.PrintScriptV1ParserFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrintScriptV1JavaConsumptionTest {

    @Test
    void runsACompleteV1ProgramFromJava() {
        List<String> lines = new ArrayList<>();
        ProgramOutput output = lines::add;

        Lexer lexer = PrintScriptV1LexerFactory.create();
        Parser parser = PrintScriptV1ParserFactory.create();
        Interpreter interpreter = PrintScriptV1InterpreterFactory.create(output);

        SourceReader reader = SourceReaderFactory.fromString(
                "let count: number = 2 + 3 * 4;\nprintln(count);\n"
        );
        TokenSource tokens = lexer.tokenize(reader);
        StatementSource statements = parser.parse(tokens);
        InterpretationResult result = interpreter.interpret(statements);

        assertInstanceOf(InterpretationResult.Success.class, result);
        assertEquals(List.of("14"), lines);
    }

    @Test
    void reportsASemanticErrorFromJavaWithoutThrowing() {
        List<String> lines = new ArrayList<>();
        ProgramOutput output = lines::add;

        Lexer lexer = PrintScriptV1LexerFactory.create();
        Parser parser = PrintScriptV1ParserFactory.create();
        Interpreter interpreter = PrintScriptV1InterpreterFactory.create(output);

        SourceReader reader = SourceReaderFactory.fromString("println(missingVariable);\n");
        TokenSource tokens = lexer.tokenize(reader);
        StatementSource statements = parser.parse(tokens);
        InterpretationResult result = interpreter.interpret(statements);

        InterpretationResult.SemanticFailure failure =
                assertInstanceOf(InterpretationResult.SemanticFailure.class, result);
        assertTrue(lines.isEmpty());
        assertTrue(failure.getError().toString().contains("missingVariable"));
    }
}
