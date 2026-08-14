package printscript.model.source

data class SourcePosition(
    val line: Int,
    val column: Int,
    val offset: Long
)