package printscript.cli.internal.command

internal class CommandDispatcher(
    commands: List<CliCommand>,
) {

    private val commandsByOperationName: Map<String, CliCommand> =
        commands.associateBy { command -> command.operationName }

    fun commandFor(operationName: String): CliCommand? {
        return commandsByOperationName[operationName]
    }

    fun availableOperationNames(): List<String> {
        return commandsByOperationName.keys.sorted()
    }
}
