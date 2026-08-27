package printscript.lexer.internal

import printscript.lexer.scanning.ScannerCharacterReadResult
import printscript.lexer.scanning.ScannerCursor
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
) : ScannerCursor {

    override val position: SourcePosition
        get() = sourceLocation.position

    /**
     * Returns the next character without consuming it. The resulting cursor
     * may contain a newly read source chunk and must be used for subsequent
     * operations.
     */
    override fun peek(): ScannerCharacterReadResult {
        return readNextAvailableCharacter(this).toReadResult()
    }

    override fun advance(): ScannerCharacterReadResult {
        return when (val availableCharacter = readNextAvailableCharacter(this)) {
            is AvailableCharacter.Success -> {
                ScannerCharacterReadResult.Success(
                    character = availableCharacter.character,
                    resultingCursor =
                    availableCharacter.cursor.afterConsuming(
                        availableCharacter.character,
                    ),
                )
            }

            is AvailableCharacter.EndOfInput -> {
                ScannerCharacterReadResult.EndOfInput(
                    resultingCursor = availableCharacter.cursor,
                )
            }
        }
    }

    private fun afterConsuming(character: Char): CharacterCursor {
        return copy(
            indexInChunk =
            indexInChunk + CHARACTER_INDEX_INCREMENT,
            sourceLocation = sourceLocation.after(character),
        )
    }

    private tailrec fun readNextAvailableCharacter(cursor: CharacterCursor): AvailableCharacter {
        val currentCharacter =
            cursor.currentCharacterOrNull()

        if (currentCharacter != null) {
            return AvailableCharacter.Success(
                character = currentCharacter,
                cursor = cursor,
            )
        }

        val sourceReader = cursor.remainingSourceReader
            ?: return AvailableCharacter.EndOfInput(
                cursor = cursor,
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
                AvailableCharacter.EndOfInput(
                    cursor = cursor.atEndOfInput(),
                )
            }
        }
    }

    private fun AvailableCharacter.toReadResult(): ScannerCharacterReadResult {
        return when (this) {
            is AvailableCharacter.Success -> {
                ScannerCharacterReadResult.Success(
                    character = character,
                    resultingCursor = cursor,
                )
            }

            is AvailableCharacter.EndOfInput -> {
                ScannerCharacterReadResult.EndOfInput(
                    resultingCursor = cursor,
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

private sealed interface AvailableCharacter {

    data class Success(
        val character: Char,
        val cursor: CharacterCursor,
    ) : AvailableCharacter

    data class EndOfInput(
        val cursor: CharacterCursor,
    ) : AvailableCharacter
}
