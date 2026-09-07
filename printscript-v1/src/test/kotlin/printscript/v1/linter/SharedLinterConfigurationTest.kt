package printscript.v1.linter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SharedLinterConfigurationTest {

    @Test
    fun `both versions map shared properties in the same order`() {
        listOf("camel case", "snake case").forEach { convention ->
            val json = """{"identifier_format": "$convention", "mandatory-variable-or-literal-in-println": true}"""
            val v1 = assertIs<PrintScriptV1LinterConfigurationResult.Success>(
                PrintScriptV1LinterFactory.configurationFrom(json),
            ).configuration.rules
            val v11 = assertIs<PrintScriptV11LinterConfigurationResult.Success>(
                PrintScriptV11LinterFactory.configurationFrom(json),
            ).configuration.rules

            assertEquals(2, v1.size)
            assertEquals(2, v11.size)
            assertEquals(v1.first(), v11.first())
            assertEquals(
                assertIs<PrintScriptV1RuleConfiguration.PrintlnArgument>(v1.last()).acceptanceByKind,
                assertIs<PrintScriptV1RuleConfiguration.PrintlnArgument>(v11.last()).acceptanceByKind,
            )
        }
    }

    @Test
    fun `shared invalid values retain version specific errors`() {
        val json = """{"identifier_format": "unknown", "mandatory-variable-or-literal-in-println": true}"""
        val v1 = assertIs<PrintScriptV1LinterConfigurationResult.Failure>(
            PrintScriptV1LinterFactory.configurationFrom(json),
        )
        val v11 = assertIs<PrintScriptV11LinterConfigurationResult.Failure>(
            PrintScriptV11LinterFactory.configurationFrom(json),
        )

        assertEquals(PrintScriptV1LinterConfigurationError.UnknownIdentifierFormat("unknown"), v1.error)
        assertEquals(PrintScriptV11LinterConfigurationError.UnknownIdentifierFormat("unknown"), v11.error)
    }

    @Test
    fun `readInput remains a V1_1 property appended after the shared rules`() {
        val json = """
            {
                "identifier_format": "camel case",
                "mandatory-variable-or-literal-in-println": true,
                "mandatory-variable-or-literal-in-readInput": true
            }
        """.trimIndent()
        val v11 = assertIs<PrintScriptV11LinterConfigurationResult.Success>(
            PrintScriptV11LinterFactory.configurationFrom(json),
        ).configuration.rules

        assertIs<PrintScriptV1RuleConfiguration.IdentifierNaming>(v11[0])
        assertIs<PrintScriptV1RuleConfiguration.PrintlnArgument>(v11[1])
        assertIs<PrintScriptV1RuleConfiguration.ReadInputArgument>(v11[2])
        val v1 = assertIs<PrintScriptV1LinterConfigurationResult.Failure>(
            PrintScriptV1LinterFactory.configurationFrom(json),
        )
        assertIs<PrintScriptV1LinterConfigurationError.InvalidConfigurationDocument>(v1.error)
    }
}
