package printscript.lexer.internal

import printscript.lexer.SourceReadingError
import printscript.lexer.scanning.ScannerCharacterReadResult
import printscript.lexer.scanning.ScannerCursor
import printscript.model.source.SourcePosition
import printscript.model.source.SourceSpan
import printscript.source.SourceChunk
import printscript.source.SourceChunkReadResult
import printscript.source.SourceReader

private const val INITIAL_INDEX_IN_CHUNK = 0
private const val CHARACTER_INDEX_INCREMENT = 1

internal data class CharacterCursor(
    private val currentChunk: SourceChunk?,
    private val indexInChunk: Int,
    private val remainingSourceReader: SourceReader?,
    private val positionTracker: SourcePositionTracker,
) : ScannerCursor {

    override val position: SourcePosition
        get() = positionTracker.currentPosition

    override fun peek(): ScannerCharacterReadResult = readNextAvailableCharacter(this).toReadResult()

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

            is AvailableCharacter.Failure -> {
                ScannerCharacterReadResult.Failure(
                    error = availableCharacter.error,
                    resultingCursor = availableCharacter.cursor,
                )
            }
        }
    }

    private fun afterConsuming(character: Char): CharacterCursor = copy(
        indexInChunk = indexInChunk + CHARACTER_INDEX_INCREMENT,
        positionTracker = positionTracker.afterConsuming(character),
    )

    private tailrec fun readNextAvailableCharacter(cursor: CharacterCursor): AvailableCharacter {
        val currentCharacter = cursor.currentCharacterOrNull()

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

        return when (val chunkReadResult = sourceReader.readChunk()) {
            is SourceChunkReadResult.Success ->
                readNextAvailableCharacter(
                    cursor.afterReadingChunk(chunkReadResult),
                )

            is SourceChunkReadResult.Failure ->
                AvailableCharacter.Failure(
                    error = SourceReadingError(
                        sourceError = chunkReadResult.error,
                        span = cursor.currentSpan(),
                    ),
                    cursor = cursor.afterFailedChunkRead(chunkReadResult),
                )

            SourceChunkReadResult.EndOfInput ->
                AvailableCharacter.EndOfInput(
                    cursor = cursor.atEndOfInput(),
                )
        }
    }

    private fun AvailableCharacter.toReadResult(): ScannerCharacterReadResult {
        return when (this) {
            is AvailableCharacter.Success ->
                ScannerCharacterReadResult.Success(
                    character = character,
                    resultingCursor = cursor,
                )

            is AvailableCharacter.EndOfInput ->
                ScannerCharacterReadResult.EndOfInput(
                    resultingCursor = cursor,
                )

            is AvailableCharacter.Failure ->
                ScannerCharacterReadResult.Failure(
                    error = error,
                    resultingCursor = cursor,
                )
        }
    }

    private fun currentCharacterOrNull(): Char? = currentChunk
        ?.content
        ?.getOrNull(indexInChunk)

    private fun afterReadingChunk(readResult: SourceChunkReadResult.Success): CharacterCursor = copy(
        currentChunk = readResult.chunk,
        indexInChunk = INITIAL_INDEX_IN_CHUNK,
        remainingSourceReader = readResult.remainingReader,
    )

    private fun afterFailedChunkRead(readResult: SourceChunkReadResult.Failure): CharacterCursor = copy(
        currentChunk = null,
        indexInChunk = INITIAL_INDEX_IN_CHUNK,
        remainingSourceReader = readResult.remainingReader,
    )

    private fun currentSpan(): SourceSpan = SourceSpan(
        start = position,
        end = position,
    )

    private fun atEndOfInput(): CharacterCursor = copy(
        currentChunk = null,
        indexInChunk = INITIAL_INDEX_IN_CHUNK,
        remainingSourceReader = null,
    )

    companion object {

        fun initial(sourceReader: SourceReader): CharacterCursor = CharacterCursor(
            currentChunk = null,
            indexInChunk = INITIAL_INDEX_IN_CHUNK,
            remainingSourceReader = sourceReader,
            positionTracker = SourcePositionTracker.initial(),
        )
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

    data class Failure(
        val error: SourceReadingError,
        val cursor: CharacterCursor,
    ) : AvailableCharacter
}
