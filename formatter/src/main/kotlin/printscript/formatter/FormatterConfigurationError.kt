package printscript.formatter

/**
 * A version-specific reason why a formatter configuration could not be built.
 * Lets version-agnostic callers hold and report the error without knowing
 * which language version produced it.
 */
public interface FormatterConfigurationError
