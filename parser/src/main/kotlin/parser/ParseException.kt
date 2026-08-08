package parser

internal class ParseException(val error: ParseError) : RuntimeException()
