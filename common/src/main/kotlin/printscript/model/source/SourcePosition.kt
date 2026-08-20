package printscript.model.source

private const val INITIAL_LINE = 1
private const val INITIAL_COLUMN = 1
private const val INITIAL_OFFSET = 0L

private const val LINE_INCREMENT = 1
private const val COLUMN_INCREMENT = 1
private const val OFFSET_INCREMENT = 1L

data class SourcePosition(
    val line: Int,
    val column: Int,
    val offset: Long,
) {

    fun nextColumn(): SourcePosition {
        return copy(
            column = column + COLUMN_INCREMENT,
            offset = offset + OFFSET_INCREMENT,
        )
    }

    fun nextLine(): SourcePosition {
        return copy(
            line = line + LINE_INCREMENT,
            column = INITIAL_COLUMN,
            offset = offset + OFFSET_INCREMENT,
        )
    }

    fun nextOffset(): SourcePosition {
        return copy(
            offset = offset + OFFSET_INCREMENT,
        )
    }

    companion object {

        fun initial(): SourcePosition {
            return SourcePosition(
                line = INITIAL_LINE,
                column = INITIAL_COLUMN,
                offset = INITIAL_OFFSET,
            )
        }
    }
}