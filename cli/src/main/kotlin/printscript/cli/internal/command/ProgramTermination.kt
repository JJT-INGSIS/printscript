package printscript.cli.internal.command

import com.github.ajalt.clikt.core.ProgramResult
import printscript.cli.internal.ExitCode

internal object ProgramTermination {

    fun endWith(exitCode: ExitCode) {
        if (exitCode == ExitCode.SUCCESS) {
            return
        }

        throw ProgramResult(exitCode.value)
    }
}
