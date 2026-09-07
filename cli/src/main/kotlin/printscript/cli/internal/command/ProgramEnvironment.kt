package printscript.cli.internal.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.terminal.prompt
import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramInput
import printscript.runtime.ProgramOutput

internal fun CliktCommand.terminalOutput(): ProgramOutput {
    return object : ProgramOutput {
        override fun writeLine(line: String) {
            echo(line)
        }
    }
}

internal fun CliktCommand.terminalInput(): ProgramInput {
    return ProgramInput { message ->
        terminal.prompt(
            prompt = message,
            promptSuffix = "",
        )
    }
}

internal fun systemEnvironmentVariables(): EnvironmentVariableProvider {
    return EnvironmentVariableProvider { name -> System.getenv(name) }
}
