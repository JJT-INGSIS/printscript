package parser


import parser.ast.Expression
import parser.ast.Statement
import parser.statements.AssignmentParser
import parser.statements.CallParser
import parser.statements.DeclarationParser
import parser.statements.StatementParser
import printscript.token.Token
import printscript.token.TokenSource
import printscript.token.TokenType

class Parser(
    tokens: TokenSource,
    private val config: ParserConfig = ParserConfig(),
) : Iterator<ParseResult>, ParsingContext {

    private val buffer = TokenBuffer(tokens)
    private var failed = false

    private val statementParsers: Map<TokenType, StatementParser> = mapOf(
        TokenType.LET to DeclarationParser(),
        TokenType.IDENTIFIER to AssignmentParser(),
        TokenType.PRINTLN to CallParser(),
    )

    override fun hasNext(): Boolean = !failed && !buffer.isAtEnd()

    override fun next(): ParseResult =
        try {
            ParseResult.Success(parseStatement())
        } catch (e: ParseException) {
            failed = true
            ParseResult.Failure(e.error)
        }

    private fun parseStatement(): Statement {
        val token = buffer.peek()
        val subParser = statementParsers[token.type]
            ?: throw ParseException(
                ParseError.Syntax("se esperaba una sentencia pero se encontró '${token.lexeme}'", token.span),
            )
        return subParser.parse(this)
    }

    override fun peek(): Token = buffer.peek()

    override fun consume(): Token = buffer.next()

    override fun expect(type: TokenType): Token {
        val token = buffer.peek()
        if (token.type != type) {
            throw ParseException(
                ParseError.Syntax("se esperaba $type pero se encontró '${token.lexeme}'", token.span),
            )
        }
        return buffer.next()
    }

    override fun parseExpression(): Expression = TODO("parsing de expresiones")
}
