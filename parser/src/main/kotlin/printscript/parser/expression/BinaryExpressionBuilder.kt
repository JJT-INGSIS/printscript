package printscript.parser.expression

import printscript.token.Token

public fun interface BinaryExpressionBuilder<E> {

    public fun build(left: E, operatorToken: Token, right: E): E
}
