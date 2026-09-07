package printscript.cli.internal.toolchain

import printscript.runtime.EnvironmentVariableProvider
import printscript.runtime.ProgramInput
import printscript.runtime.ProgramOutput

internal class ProgramEnvironment(
    val output: ProgramOutput,
    val input: ProgramInput,
    val variables: EnvironmentVariableProvider,
)
