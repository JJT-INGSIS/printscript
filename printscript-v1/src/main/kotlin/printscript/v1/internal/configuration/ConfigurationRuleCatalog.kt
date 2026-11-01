package printscript.v1.internal.configuration

import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import printscript.v1.configuration.ConfigurationRule
import printscript.v1.configuration.ConfigurationRuleType

internal object ConfigurationRuleCatalog {

    fun from(
        document: SerialDescriptor,
        choices: Map<String, List<String>> = emptyMap(),
        conflicts: List<Set<String>> = emptyList(),
    ): List<ConfigurationRule> {
        val rules = (0 until document.elementsCount).map { index ->
            ruleAt(document, index, choices, conflicts)
        }
        val ids = rules.map { it.id }.toSet()
        require(ids.containsAll(choices.keys + conflicts.flatten())) {
            "every declared choice or conflict must name a rule of the document"
        }
        return rules
    }

    private fun ruleAt(
        document: SerialDescriptor,
        index: Int,
        choices: Map<String, List<String>>,
        conflicts: List<Set<String>>,
    ): ConfigurationRule {
        val id = document.getElementName(index)
        val value = document.getElementDescriptor(index)
        val type = typeOf(id, value)
        return ConfigurationRule(
            id = id,
            type = type,
            acceptsNull = value.isNullable,
            choices = if (type == ConfigurationRuleType.CHOICE) choicesOf(id, choices) else emptyList(),
            conflictsWith = conflicts.filter { id in it }.flatten().toSet() - id,
        )
    }

    private fun typeOf(id: String, value: SerialDescriptor): ConfigurationRuleType {
        return when (value.kind) {
            PrimitiveKind.BOOLEAN -> ConfigurationRuleType.BOOLEAN
            PrimitiveKind.INT -> ConfigurationRuleType.INTEGER
            PrimitiveKind.STRING -> ConfigurationRuleType.CHOICE
            else -> error("rule $id has a value type that the catalog cannot describe: ${value.kind}")
        }
    }

    private fun choicesOf(id: String, choices: Map<String, List<String>>): List<String> {
        return choices[id] ?: error("rule $id is a text value without declared choices")
    }
}
