package printscript.lexer.internal

import printscript.model.source.SourcePosition
import java.io.Reader

private const val READER_END_OF_INPUT = -1
private const val INITIAL_LINE = 1
private const val INITIAL_COLUMN = 1
private const val INITIAL_OFFSET = 0L

private const val CARRIAGE_RETURN = '\r'
private const val LINE_FEED = '\n'

internal class ReaderCharacterCursor(
    private val inputReader: Reader,
) {
    private var lookaheadCharacterCode: Int? = null

    private var currentLine: Int = INITIAL_LINE
    private var currentColumn: Int = INITIAL_COLUMN
    private var currentOffset: Long = INITIAL_OFFSET

    private var lastConsumedCharacterWasCarriageReturn: Boolean = false

    val position: SourcePosition
        get() = SourcePosition(
            line = currentLine,
            column = currentColumn,
            offset = currentOffset,
        )

    fun peek(): Char? {
        val nextCharacterCode = getLookaheadOrReadNextCharacterCode()

        if (isEndOfInput(nextCharacterCode)) {
            return null
        }

        return nextCharacterCode.toChar()
    }

    fun advance(): Char? {
        val consumedCharacter = peek() ?: return null

        lookaheadCharacterCode = null
        updatePositionAfterConsuming(consumedCharacter)

        return consumedCharacter
    }

    private fun getLookaheadOrReadNextCharacterCode(): Int {
        val storedLookaheadCharacterCode = lookaheadCharacterCode

        if (storedLookaheadCharacterCode != null) {
            return storedLookaheadCharacterCode
        }

        return readAndStoreLookaheadCharacterCode()
    }

    private fun readAndStoreLookaheadCharacterCode(): Int {
        val readCharacterCode = inputReader.read()

        lookaheadCharacterCode = readCharacterCode

        return readCharacterCode
    }

    private fun isEndOfInput(
        characterCode: Int,
    ): Boolean {
        return characterCode == READER_END_OF_INPUT
    }

    private fun updatePositionAfterConsuming(
        consumedCharacter: Char,
    ) {
        currentOffset++

        when (consumedCharacter) {
            CARRIAGE_RETURN -> updatePositionForCarriageReturn()
            LINE_FEED -> updatePositionForLineFeed()
            else -> updatePositionForCharacterInCurrentLine()
        }
    }

    private fun updatePositionForCarriageReturn() {
        moveToBeginningOfNextLine()
        lastConsumedCharacterWasCarriageReturn = true
    }

    private fun updatePositionForLineFeed() {
        if (!lastConsumedCharacterWasCarriageReturn) {
            moveToBeginningOfNextLine()
        }

        lastConsumedCharacterWasCarriageReturn = false
    }

    private fun updatePositionForCharacterInCurrentLine() {
        currentColumn++
        lastConsumedCharacterWasCarriageReturn = false
    }

    private fun moveToBeginningOfNextLine() {
        currentLine++
        currentColumn = INITIAL_COLUMN
    }
}