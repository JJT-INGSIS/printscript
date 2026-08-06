package parser.token

/**
 * Tipos de token de PrintScript 1.0 (los produce el lexer).
 *
 * OJO: esto NO es lo mismo que tu enum GrammaticExpressions. Aquel enumera
 * las REGLAS de la gramática (Statement, Expression, Term...). Esto enumera
 * los TOKENS que emite el lexer (las "palabras" mínimas). Son dos conceptos
 * distintos: el parser consume TokenType y produce nodos según las reglas.
 */
enum class TokenType {
    LET,
    IDENTIFIER,      // nombres de variable, de tipo, y println

    NUMBER_LITERAL,
    STRING_LITERAL,

    ASSIGN,          // =
    COLON,           // :
    SEMICOLON,       // ;
    COMMA,           // ,

    PLUS, MINUS, STAR, SLASH,

    OPEN_PAREN,      // (
    CLOSE_PAREN,     // )

    EOF,             // fin del input
}
