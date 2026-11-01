package printscript.v1.internal.configuration

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import printscript.v1.configuration.ConfigurationCheck
import printscript.v1.configuration.ConfigurationRule
import printscript.v1.configuration.ConfigurationRuleType
import printscript.v1.configuration.ConfigurationRuleViolation
import printscript.v1.configuration.ConfigurationRuleViolationReason
import java.math.BigInteger

internal class ConfigurationRuleChecker(
    private val rules: List<ConfigurationRule>,
) {

    private val rulesById: Map<String, ConfigurationRule> = rules.associateBy { it.id }

    fun check(json: String): ConfigurationCheck {
        val document = try {
            Json.parseToJsonElement(json)
        } catch (cause: SerializationException) {
            return ConfigurationCheck.MalformedDocument(cause.message ?: MALFORMED_DOCUMENT)
        }
        if (document !is JsonObject) {
            return ConfigurationCheck.MalformedDocument(NOT_AN_OBJECT)
        }
        val violations = valueViolations(document) + conflictViolations(document)
        return if (violations.isEmpty()) {
            ConfigurationCheck.Valid
        } else {
            ConfigurationCheck.InvalidRules(violations)
        }
    }

    private fun valueViolations(document: JsonObject): List<ConfigurationRuleViolation> {
        return document.mapNotNull { (id, value) ->
            reasonFor(id, value)?.let { reason -> ConfigurationRuleViolation(id, reason) }
        }
    }

    private fun conflictViolations(document: JsonObject): List<ConfigurationRuleViolation> {
        val enabled = rules.filter { rule -> isEnabled(rule, document[rule.id]) }.map { it.id }.toSet()
        return enabled
            .filter { id -> rulesById.getValue(id).conflictsWith.any { it in enabled } }
            .map { id -> ConfigurationRuleViolation(id, ConfigurationRuleViolationReason.CONFLICTING_RULES) }
    }

    private fun reasonFor(id: String, value: JsonElement): ConfigurationRuleViolationReason? {
        val rule = rulesById[id] ?: return ConfigurationRuleViolationReason.UNKNOWN_RULE
        return when {
            value is JsonNull && rule.acceptsNull -> null
            value !is JsonPrimitive || value is JsonNull -> ConfigurationRuleViolationReason.WRONG_TYPE
            else -> primitiveReason(rule, value)
        }
    }

    private fun primitiveReason(rule: ConfigurationRule, value: JsonPrimitive): ConfigurationRuleViolationReason? {
        return when (rule.type) {
            ConfigurationRuleType.BOOLEAN -> booleanReason(value)
            ConfigurationRuleType.INTEGER -> integerReason(value)
            ConfigurationRuleType.CHOICE -> choiceReason(rule, value)
        }
    }

    private fun booleanReason(value: JsonPrimitive): ConfigurationRuleViolationReason? {
        return when {
            value.isString || value.content !in BOOLEAN_LITERALS -> ConfigurationRuleViolationReason.WRONG_TYPE
            else -> null
        }
    }

    private fun integerReason(value: JsonPrimitive): ConfigurationRuleViolationReason? {
        val number = if (value.isString) null else value.content.toBigIntegerOrNull()
        return when {
            number == null -> ConfigurationRuleViolationReason.WRONG_TYPE
            number !in ACCEPTED_INTEGERS -> ConfigurationRuleViolationReason.OUT_OF_RANGE
            else -> null
        }
    }

    private fun choiceReason(rule: ConfigurationRule, value: JsonPrimitive): ConfigurationRuleViolationReason? {
        return when {
            !value.isString -> ConfigurationRuleViolationReason.WRONG_TYPE
            value.content !in rule.choices -> ConfigurationRuleViolationReason.UNKNOWN_CHOICE
            else -> null
        }
    }

    private fun isEnabled(rule: ConfigurationRule, value: JsonElement?): Boolean {
        return value is JsonPrimitive && value !is JsonNull && reasonFor(rule.id, value) == null &&
            (rule.type != ConfigurationRuleType.BOOLEAN || value.content == "true")
    }

    private companion object {
        val BOOLEAN_LITERALS = setOf("true", "false")
        val ACCEPTED_INTEGERS = BigInteger.ZERO..Int.MAX_VALUE.toBigInteger()
        const val MALFORMED_DOCUMENT = "the configuration is not valid JSON"
        const val NOT_AN_OBJECT = "the configuration must be a JSON object"
    }
}
