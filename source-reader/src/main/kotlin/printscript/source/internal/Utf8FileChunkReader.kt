package printscript.source.internal

import printscript.source.SourceAccessError
import printscript.source.SourceReadError
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.channels.FileChannel
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.AccessDeniedException
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.StandardOpenOption

private const val UTF8_BOUNDARY_OVERLAP_IN_BYTES = 3
private const val EMPTY_FILE_READ_REASON = "The file read returned no bytes before the end of input"
private const val INCOMPLETE_FILE_READ_REASON = "The file read stopped before a complete character was available"

internal object Utf8FileChunkReader {

    fun read(path: Path, byteOffset: Long, bufferSizeInBytes: Int): FileChunkLoadResult {
        return try {
            FileChannel.open(path, StandardOpenOption.READ).use { channel ->
                readFromChannel(
                    channel = channel,
                    path = path,
                    byteOffset = byteOffset,
                    bufferSizeInBytes = bufferSizeInBytes,
                )
            }
        } catch (_: NoSuchFileException) {
            FileChunkLoadResult.Failure(SourceAccessError.NotFound(path))
        } catch (_: AccessDeniedException) {
            FileChunkLoadResult.Failure(SourceAccessError.NotReadable(path))
        } catch (cause: IOException) {
            failedRead(path = path, reason = cause.message.orEmpty())
        } catch (cause: SecurityException) {
            failedRead(path = path, reason = cause.message.orEmpty())
        }
    }

    private fun readFromChannel(
        channel: FileChannel,
        path: Path,
        byteOffset: Long,
        bufferSizeInBytes: Int,
    ): FileChunkLoadResult {
        val availableByteCount = channel.size() - byteOffset
        if (availableByteCount <= 0L) {
            return FileChunkLoadResult.EndOfInput
        }

        val candidateBytes = readCandidateBytes(
            channel = channel,
            byteOffset = byteOffset,
            bufferSizeInBytes = bufferSizeInBytes,
            availableByteCount = availableByteCount,
        )
        if (!candidateBytes.hasRemaining()) {
            return failedRead(path = path, reason = EMPTY_FILE_READ_REASON)
        }

        return decodeCompletePrefix(
            candidateBytes = candidateBytes,
            requestedByteCount = bufferSizeInBytes,
            reachedEndOfInput = candidateBytes.remaining().toLong() >= availableByteCount,
            path = path,
            byteOffset = byteOffset,
        )
    }

    private fun readCandidateBytes(
        channel: FileChannel,
        byteOffset: Long,
        bufferSizeInBytes: Int,
        availableByteCount: Long,
    ): ByteBuffer {
        val requestedCandidateByteCount = minOf(
            bufferSizeInBytes.toLong() + UTF8_BOUNDARY_OVERLAP_IN_BYTES,
            Int.MAX_VALUE.toLong(),
        )
        val candidateByteCount = minOf(
            requestedCandidateByteCount,
            availableByteCount,
        ).toInt()
        val buffer = ByteBuffer.allocate(candidateByteCount)
        var readByteCount = 0

        while (buffer.hasRemaining()) {
            val currentReadByteCount = channel.read(
                buffer,
                byteOffset + readByteCount,
            )

            if (currentReadByteCount <= 0) {
                break
            }

            readByteCount += currentReadByteCount
        }

        return buffer.flip()
    }

    private fun decodeCompletePrefix(
        candidateBytes: ByteBuffer,
        requestedByteCount: Int,
        reachedEndOfInput: Boolean,
        path: Path,
        byteOffset: Long,
    ): FileChunkLoadResult {
        var attemptedByteCount = minOf(
            requestedByteCount,
            candidateBytes.remaining(),
        )

        while (true) {
            val attempt = decode(
                candidateBytes = candidateBytes,
                attemptedByteCount = attemptedByteCount,
                reachedEndOfInput = reachedEndOfInput && attemptedByteCount == candidateBytes.limit(),
            )

            if (attempt.consumedByteCount > 0) {
                return FileChunkLoadResult.Success(
                    content = attempt.content,
                    consumedByteCount = attempt.consumedByteCount.toLong(),
                )
            }

            if (attempt.invalidByteIndex != null) {
                return invalidEncoding(
                    path = path,
                    byteOffset = byteOffset + attempt.invalidByteIndex,
                )
            }

            if (attemptedByteCount >= candidateBytes.limit()) {
                return if (reachedEndOfInput) {
                    invalidEncoding(path = path, byteOffset = byteOffset)
                } else {
                    failedRead(path = path, reason = INCOMPLETE_FILE_READ_REASON)
                }
            }

            attemptedByteCount += 1
        }
    }

    private fun decode(
        candidateBytes: ByteBuffer,
        attemptedByteCount: Int,
        reachedEndOfInput: Boolean,
    ): Utf8DecodingAttempt {
        val input = candidateBytes.asReadOnlyBuffer()
        input.limit(attemptedByteCount)

        val characters = CharBuffer.allocate(attemptedByteCount)
        val decodingResult = StandardCharsets.UTF_8
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(input, characters, reachedEndOfInput)

        characters.flip()

        return Utf8DecodingAttempt(
            content = characters.toString(),
            consumedByteCount = input.position(),
            invalidByteIndex = if (decodingResult.isError) input.position() else null,
        )
    }

    private fun invalidEncoding(path: Path, byteOffset: Long): FileChunkLoadResult.Failure {
        return FileChunkLoadResult.Failure(
            SourceReadError.InvalidEncoding(
                path = path,
                byteOffset = byteOffset,
            ),
        )
    }

    private fun failedRead(path: Path, reason: String): FileChunkLoadResult.Failure {
        return FileChunkLoadResult.Failure(
            SourceAccessError.ReadFailed(
                path = path,
                reason = reason,
            ),
        )
    }
}

private data class Utf8DecodingAttempt(
    val content: String,
    val consumedByteCount: Int,
    val invalidByteIndex: Int?,
)
