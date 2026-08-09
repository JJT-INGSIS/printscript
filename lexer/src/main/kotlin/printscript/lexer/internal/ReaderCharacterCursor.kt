package printscript.lexer.internal

import printscript.model.source.SourcePosition
import java.io.Reader

private const val READER_EOF = -1
private const val INITIAL_LINE = 1
private const val INITIAL_COLUMN = 1
private const val INITIAL_OFFSET = 0L

internal class ReaderCharacterCursor(
    private val reader: Reader,
) {
    private var lookahead: Int? = null

    private var line: Int = INITIAL_LINE
    private var column: Int = INITIAL_COLUMN
    private var offset: Long = INITIAL_OFFSET

    private var previousCharacterWasCarriageReturn: Boolean = false

    val position: SourcePosition
        get() = SourcePosition(
            line = line,
            column = column,
            offset = offset,
        )

    fun peek(): Char? {
        val nextCharacterCode = lookahead
            ?: reader.read().also { lookahead = it }

        return if (nextCharacterCode == READER_EOF) {
            null
        } else {
            nextCharacterCode.toChar()
        }
    }

    fun advance(): Char? {
        val currentCharacter = peek() ?: return null

        lookahead = null
        updatePosition(currentCharacter)

        return currentCharacter
    }

    private fun updatePosition(consumedCharacter: Char) {
        offset++

        when {
            consumedCharacter == '\r' -> {
                line++
                column = INITIAL_COLUMN
                previousCharacterWasCarriageReturn = true
            }

            consumedCharacter == '\n' &&
                    previousCharacterWasCarriageReturn -> {
                previousCharacterWasCarriageReturn = false
            }

            consumedCharacter == '\n' -> {
                line++
                column = INITIAL_COLUMN
                previousCharacterWasCarriageReturn = false
            }

            else -> {
                column++
                previousCharacterWasCarriageReturn = false
            }
        }
    }
}