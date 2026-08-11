package printscript.parser.internal

import printscript.model.ast.statement.Statement
import printscript.parser.internal.statement.StatementMatch
import printscript.parser.internal.statement.StatementMismatch
import printscript.parser.internal.statement.StatementParser
import printscript.statement.ParseError
import printscript.token.Token
import printscript.token.TokenReadResult
import printscript.token.TokenSource
import printscript.token.TokenType

internal class DefaultParsingContext(
    tokens: TokenSource,
    private val statementParsers: List<StatementParser>,
) : ParsingContext {

    private val cursor = TokenCursor(tokens)

    override fun peekAt(
        offset: Int,
    ): ParsingResult<Token> {
        return cursor.peekAt(offset).toParsingResult()
    }

    override fun consume(): ParsingResult<Token> {
        return cursor.advance().toParsingResult()
    }

    override fun expect(
        expected: Set<TokenType>,
    ): ParsingResult<Token> {
        val token = peek()
            .orReturn { return it }

        if (token.type !in expected) {
            return ParsingResult.Failure(
                ParseError.UnexpectedToken(
                    expected = expected,
                    actual = token,
                ),
            )
        }

        return consume()
    }

    override fun parseStatement(): ParsingResult<Statement> {
        val mismatches = mutableListOf<StatementMismatch>()

        for (statementParser in statementParsers) {
            when (val match = statementParser.match(this)) {
                StatementMatch.Match -> {
                    return statementParser.parse(this)
                }

                is StatementMatch.NoMatch -> {
                    mismatches.add(match.mismatch)
                }

                is StatementMatch.Failure -> {
                    return ParsingResult.Failure(match.error)
                }
            }
        }

        return noMatchingStatement(mismatches)
    }

    private fun noMatchingStatement(
        mismatches: List<StatementMismatch>,
    ): ParsingResult.Failure {
        val furthestOffset = mismatches.maxOf {
            it.lookaheadOffset
        }

        val furthestMismatches = mismatches.filter {
            it.lookaheadOffset == furthestOffset
        }

        val actualToken = furthestMismatches.first().actual

        val expectedTokens = furthestMismatches
            .flatMap { it.expected }
            .toSet()

        return ParsingResult.Failure(
            ParseError.UnexpectedToken(
                expected = expectedTokens,
                actual = actualToken,
            ),
        )
    }

    private fun TokenReadResult.toParsingResult(): ParsingResult<Token> {
        return when (this) {
            is TokenReadResult.Success -> {
                ParsingResult.Success(token)
            }

            is TokenReadResult.Failure -> {
                ParsingResult.Failure(
                    ParseError.Lexical(error),
                )
            }
        }
    }
}