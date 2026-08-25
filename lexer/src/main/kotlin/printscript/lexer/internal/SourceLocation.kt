package printscript.lexer.internal

import printscript.model.source.SourcePosition

private const val CARRIAGE_RETURN = '\r'
private const val LINE_FEED = '\n'

internal data class SourceLocation(
    val position: SourcePosition,
    private val previousCharacterWasCarriageReturn: Boolean,
) {

    fun after(character: Char): SourceLocation {
        return when (character) {
            CARRIAGE_RETURN -> afterCarriageReturn()
            LINE_FEED -> afterLineFeed()
            else -> afterNonLineBreakCharacter()
        }
    }

    private fun afterCarriageReturn(): SourceLocation {
        return copy(
            position = position.nextLine(),
            previousCharacterWasCarriageReturn = true,
        )
    }

    private fun afterLineFeed(): SourceLocation {
        val positionAfterLineFeed =
            if (previousCharacterWasCarriageReturn) {
                position.nextOffset()
            } else {
                position.nextLine()
            }

        return copy(
            position = positionAfterLineFeed,
            previousCharacterWasCarriageReturn = false,
        )
    }

    private fun afterNonLineBreakCharacter(): SourceLocation {
        return copy(
            position = position.nextColumn(),
            previousCharacterWasCarriageReturn = false,
        )
    }

    companion object {

        fun initial(): SourceLocation {
            return SourceLocation(
                position = SourcePosition.initial(),
                previousCharacterWasCarriageReturn = false,
            )
        }
    }
}
