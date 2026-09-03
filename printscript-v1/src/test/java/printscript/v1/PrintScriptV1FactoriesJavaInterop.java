package printscript.v1;

import printscript.runtime.ProgramOutput;
import printscript.v1.formatter.PrintScriptV1FormatterFactory;
import printscript.v1.interpreter.PrintScriptV1ExpressionEvaluatorFactory;
import printscript.v1.interpreter.PrintScriptV1InterpreterFactory;
import printscript.v1.lexer.PrintScriptV1FormattingLexerFactory;
import printscript.v1.lexer.PrintScriptV1LexerFactory;
import printscript.v1.linter.PrintScriptV1LinterFactory;
import printscript.v1.parser.PrintScriptV1ParserFactory;

final class PrintScriptV1FactoriesJavaInterop {

    private PrintScriptV1FactoriesJavaInterop() {
    }

    static void consumeFactoriesFromJava(ProgramOutput output) {
        PrintScriptV1LexerFactory.defaultConfiguration();
        PrintScriptV1LexerFactory.create();
        PrintScriptV1FormattingLexerFactory.create();

        PrintScriptV1ParserFactory.defaultConfiguration();
        PrintScriptV1ParserFactory.create();

        PrintScriptV1ExpressionEvaluatorFactory.create();
        PrintScriptV1InterpreterFactory.create(output);

        PrintScriptV1FormatterFactory.defaultConfiguration();
        PrintScriptV1FormatterFactory.create();

        PrintScriptV1LinterFactory.defaultConfiguration();
        PrintScriptV1LinterFactory.create();
    }
}
