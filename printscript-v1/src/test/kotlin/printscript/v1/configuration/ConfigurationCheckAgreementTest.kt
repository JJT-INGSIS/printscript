package printscript.v1.configuration

import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfiguration
import printscript.v1.formatter.configuration.PrintScriptV11FormatterConfigurationResult
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfiguration
import printscript.v1.formatter.configuration.PrintScriptV1FormatterConfigurationResult
import printscript.v1.linter.PrintScriptV11LinterConfigurationResult
import printscript.v1.linter.PrintScriptV11LinterFactory
import printscript.v1.linter.PrintScriptV1LinterConfigurationResult
import printscript.v1.linter.PrintScriptV1LinterFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigurationCheckAgreementTest {

    private val readers = listOf(
        Reader(
            name = "formatter 1.0",
            rules = PrintScriptV1FormatterConfiguration.rules(),
            check = PrintScriptV1FormatterConfiguration::check,
            accepts = { json ->
                PrintScriptV1FormatterConfiguration.fromJson(json) is
                    PrintScriptV1FormatterConfigurationResult.Success
            },
        ),
        Reader(
            name = "formatter 1.1",
            rules = PrintScriptV11FormatterConfiguration.rules(),
            check = PrintScriptV11FormatterConfiguration::check,
            accepts = { json ->
                PrintScriptV11FormatterConfiguration.fromJson(json) is
                    PrintScriptV11FormatterConfigurationResult.Success
            },
        ),
        Reader(
            name = "linter 1.0",
            rules = PrintScriptV1LinterFactory.configurationRules(),
            check = PrintScriptV1LinterFactory::checkConfiguration,
            accepts = { json ->
                PrintScriptV1LinterFactory.configurationFrom(json) is
                    PrintScriptV1LinterConfigurationResult.Success
            },
        ),
        Reader(
            name = "linter 1.1",
            rules = PrintScriptV11LinterFactory.configurationRules(),
            check = PrintScriptV11LinterFactory::checkConfiguration,
            accepts = { json ->
                PrintScriptV11LinterFactory.configurationFrom(json) is
                    PrintScriptV11LinterConfigurationResult.Success
            },
        ),
    )

    private val strictSamples = listOf(
        "true", "false", "null", "0", "1", "-1", "2147483647", "2147483648", "1.5",
        "\"camel case\"", "\"snake case\"", "\"kebab case\"", "[]", "{}",
    )

    private val samplesOnlyTheReaderCoerces = listOf("\"true\"", "\"1\"", "1e2")

    @Test
    fun `check accepts a single rule value exactly when the reader accepts it`() {
        assertEquals(emptyList(), disagreements(singleRuleDocuments(strictSamples)))
    }

    @Test
    fun `check takes values as written even where the reader coerces quoted or exponent values`() {
        val accepted = singleRuleDocuments(samplesOnlyTheReaderCoerces).filter { (reader, json) ->
            reader.check(json) == ConfigurationCheck.Valid
        }

        assertEquals(emptyList(), accepted)
    }

    @Test
    fun `check rejects a pair of turned on rules exactly when the reader rejects it`() {
        val documents = readers.flatMap { reader ->
            reader.rules.flatMap { first ->
                reader.rules.filter { it.id != first.id }.map { second ->
                    reader to """{"${first.id}": ${turnedOn(first)}, "${second.id}": ${turnedOn(second)}}"""
                }
            }
        }

        assertEquals(emptyList(), disagreements(documents))
    }

    private fun singleRuleDocuments(samples: List<String>): List<Pair<Reader, String>> {
        return readers.flatMap { reader ->
            reader.rules.flatMap { rule ->
                samples.map { sample -> reader to """{"${rule.id}": $sample}""" }
            } + listOf(reader to """{"unknown": true}""", reader to "[]", reader to "{ not json")
        }
    }

    private fun disagreements(documents: List<Pair<Reader, String>>): List<Pair<Reader, String>> {
        return documents.filter { (reader, json) ->
            reader.accepts(json) != (reader.check(json) == ConfigurationCheck.Valid)
        }
    }

    private fun turnedOn(rule: ConfigurationRule): String {
        return when (rule.type) {
            ConfigurationRuleType.BOOLEAN -> "true"
            ConfigurationRuleType.INTEGER -> "2"
            ConfigurationRuleType.CHOICE -> "\"${rule.choices.first()}\""
        }
    }

    private data class Reader(
        val name: String,
        val rules: List<ConfigurationRule>,
        val check: (String) -> ConfigurationCheck,
        val accepts: (String) -> Boolean,
    ) {
        override fun toString(): String = name
    }
}
