package printscript.parser.expression

import printscript.token.Token

public fun interface UnaryExpressionBuilder<E> {

    public fun build(operatorToken: Token, operand: E): E
}
