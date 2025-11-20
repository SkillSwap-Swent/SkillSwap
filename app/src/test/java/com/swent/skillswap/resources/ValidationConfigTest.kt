package com.swent.skillswap.resources

import com.swent.skillswap.resources.config.ValidationConfig
import org.junit.Assert.*
import org.junit.Test

/** Test suite for ValidationConfig email validation regex. */
class ValidationConfigTest {
    @Test
    fun emailRegex_validatesValidEmails() {
        listOf("user@example.com", "test+tag@domain.org", "user.name@company.co.uk").forEach {
            assertTrue("Email '$it' should be valid", ValidationConfig.EMAIL_REGEX.matches(it))
        }
    }

    @Test
    fun emailRegex_rejectsInvalidEmails() {
        listOf("", "userexample.com", "user@", "@example.com", "user@example").forEach {
            assertFalse("Email '$it' should be invalid", ValidationConfig.EMAIL_REGEX.matches(it))
        }
    }
}
