package printscript.v1;

import printscript.runtime.EnvironmentVariableProvider;
import printscript.runtime.ProgramInput;
import printscript.runtime.ProgramOutput;
import printscript.v1.formatter.PrintScriptV11FormatterFactory;
import printscript.v1.formatter.PrintScriptV1FormatterFactory;
import printscript.v1.interpreter.PrintScriptV1ExpressionEvaluatorFactory;
import printscript.v1.interpreter.PrintScriptV1InterpreterFactory;
import printscript.v1.interpreter.PrintScriptV11ExpressionEvaluatorFactory;
import printscript.v1.interpreter.PrintScriptV11InterpreterFactory;
import printscript.v1.lexer.PrintScriptV11FormattingLexerFactory;
import printscript.v1.lexer.PrintScriptV11LexerFactory;
import printscript.v1.lexer.PrintScriptV1FormattingLexerFactory;
import printscript.v1.lexer.PrintScriptV1LexerFactory;
import printscript.v1.linter.PrintScriptV11LinterFactory;
import printscript.v1.linter.PrintScriptV1LinterFactory;
import printscript.v1.parser.PrintScriptV1ParserFactory;
import printscript.v1.parser.PrintScriptV11ParserFactory;

final class PrintScriptV1FactoriesJavaInterop {

    private PrintScriptV1FactoriesJavaInterop() {
    }

    static void consumeFactoriesFromJava(ProgramOutput output) {
        ProgramInput input = prompt -> "value";
        EnvironmentVariableProvider environmentVariables = name -> "value";

        PrintScriptV1LexerFactory.defaultConfiguration();
        PrintScriptV1LexerFactory.create();
        PrintScriptV1FormattingLexerFactory.create();
        PrintScriptV11LexerFactory.defaultConfiguration();
        PrintScriptV11LexerFactory.create();
        PrintScriptV11FormattingLexerFactory.create();

        PrintScriptV1ParserFactory.defaultConfiguration();
        PrintScriptV1ParserFactory.create();
        PrintScriptV11ParserFactory.defaultConfiguration();
        PrintScriptV11ParserFactory.create();

        PrintScriptV1ExpressionEvaluatorFactory.create();
        PrintScriptV1InterpreterFactory.create(output);
        PrintScriptV11ExpressionEvaluatorFactory.create(input, environmentVariables);
        PrintScriptV11InterpreterFactory.create(output, input, environmentVariables);

        PrintScriptV1FormatterFactory.defaultConfiguration();
        PrintScriptV1FormatterFactory.configurationFrom("{}");
        PrintScriptV1FormatterFactory.create();
        PrintScriptV11FormatterFactory.defaultConfiguration();
        PrintScriptV11FormatterFactory.configurationFrom("{}");
        PrintScriptV11FormatterFactory.create();

        PrintScriptV1LinterFactory.defaultConfiguration();
        PrintScriptV1LinterFactory.configurationFrom("{}");
        PrintScriptV1LinterFactory.create();

        PrintScriptV11LinterFactory.defaultConfiguration();
        PrintScriptV11LinterFactory.configurationFrom("{}");
        PrintScriptV11LinterFactory.create();
    }
}
