package printscript.lexer.internal

import printscript.model.source.SourcePosition
import printscript.source.SourceChunk
import printscript.source.SourceChunkReadResult
import printscript.source.SourceReader

private const val INITIAL_CHUNK_INDEX = 0
private const val CHUNK_INDEX_INCREMENT = 1

internal data class CharacterCursor(
    private val currentChunk: SourceChunk?,
    private val indexInChunk: Int,
    private val remainingReader: SourceReader,
    private val location: SourceLocation,
) {

    val position: SourcePosition
        get() = location.position

    fun peek(): CharacterReadResult {
        return prepareNextCharacter(this)
    }

    fun advance(): CharacterReadResult {
        val peekResult = peek()

        return when (peekResult) {
            is CharacterReadResult.Success -> {
                CharacterReadResult.Success(
                    character = peekResult.character,
                    resultingCursor =
                    peekResult.resultingCursor
                        .afterConsuming(
                            peekResult.character,
                        ),
                )
            }

            is CharacterReadResult.EndOfInput -> {
                peekResult
            }
        }
    }

    private fun afterConsuming(character: Char): CharacterCursor {
        return copy(
            indexInChunk =
            indexInChunk + CHUNK_INDEX_INCREMENT,
            location = location.after(character),
        )
    }

    private tailrec fun prepareNextCharacter(cursor: CharacterCursor): CharacterReadResult {
        val currentCharacter =
            cursor.currentCharacterOrNull()

        if (currentCharacter != null) {
            return CharacterReadResult.Success(
                character = currentCharacter,
                resultingCursor = cursor,
            )
        }

        val chunkReadResult =
            cursor.remainingReader.readChunk()

        return when (chunkReadResult) {
            is SourceChunkReadResult.Success -> {
                prepareNextCharacter(
                    cursor.withChunk(chunkReadResult),
                )
            }

            SourceChunkReadResult.EndOfInput -> {
                CharacterReadResult.EndOfInput(
                    resultingCursor =
                    cursor.withoutCurrentChunk(),
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

    private fun withChunk(readResult: SourceChunkReadResult.Success): CharacterCursor {
        return copy(
            currentChunk = readResult.chunk,
            indexInChunk = INITIAL_CHUNK_INDEX,
            remainingReader = readResult.remainingReader,
        )
    }

    private fun withoutCurrentChunk(): CharacterCursor {
        return copy(
            currentChunk = null,
            indexInChunk = INITIAL_CHUNK_INDEX,
        )
    }

    companion object {

        fun initial(sourceReader: SourceReader): CharacterCursor {
            return CharacterCursor(
                currentChunk = null,
                indexInChunk = INITIAL_CHUNK_INDEX,
                remainingReader = sourceReader,
                location = SourceLocation.initial(),
            )
        }
    }
}
