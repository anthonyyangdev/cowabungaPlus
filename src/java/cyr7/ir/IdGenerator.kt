package cyr7.ir

/**
 * Generates temporary names, argument temps, return value temps, and label
 * names. No two names created by an instance of IdGenerator are the same.
 */
interface IdGenerator {
    /**
     * Creates a new label.
     */
    fun newLabel(): String

    /**
     * Creates a new temporary name.
     */
    fun newTemp(): String
    fun newTemp(description: String): String

    /**
     * Generating a temp for the nth return value
     */
    fun retTemp(n: Int): String

    /**
     * Generating a temp for the nth argument.
     */
    fun argTemp(n: Int): String
}
