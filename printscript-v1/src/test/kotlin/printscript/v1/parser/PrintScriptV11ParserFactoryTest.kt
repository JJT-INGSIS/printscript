package printscript.v1.parser

import printscript.ast.DeclarationKind
import printscript.ast.statement.IfStatement
import printscript.ast.statement.VariableDeclarationStatement
import printscript.source.SourceReaderFactory
import printscript.v1.lexer.PrintScriptV11LexerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptV11ParserFactoryTest {

    @Test
    fun `connects the V1_1 lexer and parser`() {
        val sourceReader = SourceReaderFactory.fromString(
            """
            const active: boolean = true;
            if (active) {
                println(readEnv("BEST_FOOTBALL_CLUB"));
            }
            """.trimIndent(),
        )
        val tokenSource = PrintScriptV11LexerFactory.create().tokenize(sourceReader)
        val statementSource = PrintScriptV11ParserFactory.create().parse(tokenSource)

        val declaration = statementSource.assertNextStatement()
        assertEquals(
            DeclarationKind.CONSTANT,
            assertIs<VariableDeclarationStatement>(declaration.statement).declarationKind,
        )

        val conditional = declaration.remainingSource.assertNextStatement()
        assertIs<IfStatement>(conditional.statement)
        conditional.remainingSource.assertEndOfInput()
    }
}
