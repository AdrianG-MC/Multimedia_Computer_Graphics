package edu.up.cg.utils;

/**
 * Reads environment variables for API keys and configuration
 *
 * All sensitive values (API keys) are read from environment variables
 * It doesnt work properly.
 *
 */
public final class EnvConfig {

    private EnvConfig() {}

    /**
     * Returns the value of a required environment variable.
     *
     * @param name variable name (e.g. "GEMINI_API_KEY")
     * @return the non-blank value
     * @throws IllegalStateException with a friendly message if the variable is missing
     */
    public static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = System.getProperty(name);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Required environment variable '" + name + "' is not set.\n" +
                "Set it before running:\n" +
                "  macOS/Linux : export " + name + "=your_key_here\n" +
                "  Windows CMD : set    " + name + "=your_key_here\n" +
                "  Windows PS  : $env:" + name + "=your_key_here");
        }
        return value;
    }

    /**
     * Returns the value of an optional environment variable, or a default.
     *
     * @param name variable name
     * @param defaultValue returned when the variable is missing or blank
     * @return the variable's value, or {@code defaultValue}
     */
    public static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
