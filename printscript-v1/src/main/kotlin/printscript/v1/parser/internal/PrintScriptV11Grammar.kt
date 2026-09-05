package printscript.v1.parser.internal

import printscript.ast.DeclaredType
import printscript.token.TokenType
import printscript.v1.token.PrintScriptV1TokenType

internal val printScriptV11DeclaredTypesByTokenType: Map<TokenType, DeclaredType> =
    printScriptV1DeclaredTypesByTokenType +
        (PrintScriptV1TokenType.BOOLEAN_TYPE to DeclaredType.BOOLEAN)

internal val printScriptV11BooleanValuesByTokenType: Map<TokenType, Boolean> =
    mapOf(
        PrintScriptV1TokenType.TRUE to true,
        PrintScriptV1TokenType.FALSE to false,
    )
