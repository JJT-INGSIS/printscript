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
            CARRIAGE_RETURN -> {
                nextLocation(
                    position = position.nextLine(),
                    previousCharacterWasCarriageReturn = true,
                )
            }

            LINE_FEED -> {
                afterLineFeed()
            }

            else -> {
                nextLocation(
                    position = position.nextColumn(),
                    previousCharacterWasCarriageReturn = false,
                )
            }
        }
    }

    private fun afterLineFeed(): SourceLocation {
        if (previousCharacterWasCarriageReturn) {
            return nextLocation(
                position = position.nextOffset(),
                previousCharacterWasCarriageReturn = false,
            )
        }

        return nextLocation(
            position = position.nextLine(),
            previousCharacterWasCarriageReturn = false,
        )
    }

    private fun nextLocation(position: SourcePosition, previousCharacterWasCarriageReturn: Boolean): SourceLocation {
        return SourceLocation(
            position = position,
            previousCharacterWasCarriageReturn =
            previousCharacterWasCarriageReturn,
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
