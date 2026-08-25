package printscript.lexer.internal

import printscript.model.source.SourcePosition
import printscript.source.SourceChunk
import printscript.source.SourceChunkReadResult
import printscript.source.SourceReader

private const val INITIAL_INDEX_IN_CHUNK = 0
private const val CHARACTER_INDEX_INCREMENT = 1

internal data class CharacterCursor(
    private val currentChunk: SourceChunk?,
    private val indexInChunk: Int,
    private val remainingSourceReader: SourceReader?,
    private val sourceLocation: SourceLocation,
) {

    val position: SourcePosition
        get() = sourceLocation.position

    /**
     * Returns the next character without consuming it. The resulting cursor
     * may contain a newly read source chunk and must be used for subsequent
     * operations.
     */
    fun peek(): CharacterReadResult {
        return readNextAvailableCharacter(this)
    }

    fun advance(): CharacterReadResult {
        return when (val readResult = peek()) {
            is CharacterReadResult.Success ->
                consumeCharacter(readResult)

            is CharacterReadResult.EndOfInput ->
                readResult
        }
    }

    private fun consumeCharacter(readResult: CharacterReadResult.Success): CharacterReadResult.Success {
        return CharacterReadResult.Success(
            character = readResult.character,
            resultingCursor =
            readResult.resultingCursor.afterConsuming(
                readResult.character,
            ),
        )
    }

    private fun afterConsuming(character: Char): CharacterCursor {
        return copy(
            indexInChunk =
            indexInChunk + CHARACTER_INDEX_INCREMENT,
            sourceLocation = sourceLocation.after(character),
        )
    }

    private tailrec fun readNextAvailableCharacter(cursor: CharacterCursor): CharacterReadResult {
        val currentCharacter =
            cursor.currentCharacterOrNull()

        if (currentCharacter != null) {
            return CharacterReadResult.Success(
                character = currentCharacter,
                resultingCursor = cursor,
            )
        }

        val sourceReader = cursor.remainingSourceReader
            ?: return CharacterReadResult.EndOfInput(
                resultingCursor = cursor,
            )

        val chunkReadResult =
            sourceReader.readChunk()

        return when (chunkReadResult) {
            is SourceChunkReadResult.Success -> {
                readNextAvailableCharacter(
                    cursor.afterReadingChunk(chunkReadResult),
                )
            }

            SourceChunkReadResult.EndOfInput -> {
                CharacterReadResult.EndOfInput(
                    resultingCursor = cursor.atEndOfInput(),
                )
            }
        }
    }

    private fun currentCharacterOrNull(): Char? {
        val chunk = currentChunk
            ?: return null

        if (indexInChunk >= chunk.content.length) {
            return null
        }

        return chunk.content[indexInChunk]
    }

    private fun afterReadingChunk(readResult: SourceChunkReadResult.Success): CharacterCursor {
        return copy(
            currentChunk = readResult.chunk,
            indexInChunk = INITIAL_INDEX_IN_CHUNK,
            remainingSourceReader = readResult.remainingReader,
        )
    }

    private fun atEndOfInput(): CharacterCursor {
        return copy(
            currentChunk = null,
            indexInChunk = INITIAL_INDEX_IN_CHUNK,
            remainingSourceReader = null,
        )
    }

    companion object {

        fun initial(sourceReader: SourceReader): CharacterCursor {
            return CharacterCursor(
                currentChunk = null,
                indexInChunk = INITIAL_INDEX_IN_CHUNK,
                remainingSourceReader = sourceReader,
                sourceLocation = SourceLocation.initial(),
            )
        }
    }
}
