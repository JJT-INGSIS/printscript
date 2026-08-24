package printscript.cli.internal.command

internal sealed interface CommandOutcome {

    data object Success : CommandOutcome

    data class Failure(
        val message: String,
    ) : CommandOutcome
}