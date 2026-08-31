package printscript.lexer.internal

import printscript.model.source.SourcePosition

private const val CARRIAGE_RETURN = '\r'
private const val LINE_FEED = '\n'

internal data class SourcePositionTracker(
    val currentPosition: SourcePosition,
    private val previousCharacterWasCarriageReturn: Boolean,
) {

    fun afterConsuming(character: Char): SourcePositionTracker {
        return when (character) {
            CARRIAGE_RETURN -> afterConsumingCarriageReturn()
            LINE_FEED -> afterConsumingLineFeed()
            else -> afterConsumingNonLineBreakCharacter()
        }
    }

    private fun afterConsumingCarriageReturn(): SourcePositionTracker {
        return copy(
            currentPosition = currentPosition.nextLine(),
            previousCharacterWasCarriageReturn = true,
        )
    }

    private fun afterConsumingLineFeed(): SourcePositionTracker {
        val positionAfterLineFeed =
            if (previousCharacterWasCarriageReturn) {
                currentPosition.nextOffset()
            } else {
                currentPosition.nextLine()
            }

        return copy(
            currentPosition = positionAfterLineFeed,
            previousCharacterWasCarriageReturn = false,
        )
    }

    private fun afterConsumingNonLineBreakCharacter(): SourcePositionTracker {
        return copy(
            currentPosition = currentPosition.nextColumn(),
            previousCharacterWasCarriageReturn = false,
        )
    }

    companion object {

        fun initial(): SourcePositionTracker {
            return SourcePositionTracker(
                currentPosition = SourcePosition.initial(),
                previousCharacterWasCarriageReturn = false,
            )
        }
    }
}
