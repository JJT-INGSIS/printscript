package printscript.cli.internal.command

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import printscript.cli.internal.operation.LanguageVersion

internal class LanguageOptions : OptionGroup() {

    val version: LanguageVersion by option("--version", help = "Versión del lenguaje")
        .enum<LanguageVersion>(key = LanguageVersion::label)
        .default(LanguageVersion.DEFAULT)
}
