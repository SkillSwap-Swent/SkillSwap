package com.swent.skillswap.resources

/**
 * Shared validation configuration used across the app. Ensures consistent validation rules for
 * email, password, etc.
 */
object ValidationConfig {
    /**
     * Regular expression for validating email formats. Used consistently across SignIn,
     * CreateAccount, and PasswordRecovery.
     *
     * Pattern: ^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$
     * - Allows alphanumeric, +, _, ., - before @
     * - Requires @ symbol
     * - Allows alphanumeric, ., - after @
     * - Requires . followed by 2-6 letter TLD
     */
    val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
}
