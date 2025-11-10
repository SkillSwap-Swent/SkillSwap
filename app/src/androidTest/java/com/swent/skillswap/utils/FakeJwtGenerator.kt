package com.swent.skillswap.utils

import android.util.Base64
import org.json.JSONObject

/**
 * A utility object for generating **fake (unsigned) Google ID tokens (JWTs)**.
 *
 * This class is primarily intended for **testing or local development** scenarios, where you need
 * to simulate Google Sign-In or Firebase Authentication behavior without interacting with real
 * Google APIs.
 *
 * The generated JWT is composed of three parts:
 * - **Header** — specifies `"alg": "none"`, indicating no signature algorithm.
 * - **Payload** — includes fake user information such as `sub`, `email`, `name`, and `picture`.
 * - **Signature** — a dummy string (`"sig"`) since the emulator and tests do not validate it.
 *
 * Example usage:
 * ```
 * val fakeToken = FakeJwtGenerator.createFakeGoogleIdToken(
 *     name = "John Doe",
 *     email = "john.doe@example.com"
 * )
 * ```
 *
 * The resulting string looks like a valid JWT but contains no cryptographic signature.
 *
 * ⚠️ **Important:** This is for **testing purposes only**. Never use this class or its output in
 * production — the tokens it produces are not secure.
 */
object FakeJwtGenerator {
    /**
     * Internal counter used to generate unique `sub` (subject) IDs for each fake token instance.
     *
     * This ensures that subsequent calls to [createFakeGoogleIdToken] produce tokens with distinct
     * subject identifiers.
     */
    private var _counter = 0
    /**
     * Returns the next counter value and increments it. Used to create unique `"sub"` fields in
     * fake JWT payloads.
     */
    private val counter
        get() = _counter++
    /**
     * Encodes the given [input] bytes into a Base64URL string compatible with JWT formatting.
     *
     * @param input The byte array to encode.
     * @return A Base64URL-encoded string with no padding or line breaks.
     */
    private fun base64UrlEncode(input: ByteArray): String {
        return Base64.encodeToString(input, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
    /**
     * Creates a fake Google ID token (JWT) containing mock user information.
     *
     * This method simulates what a real Google Sign-In token might look like, but without any
     * actual cryptographic signing or verification.
     *
     * The payload includes:
     * - `sub`: A sequentially generated fake user ID.
     * - `email`: The provided email address.
     * - `name`: The provided user name.
     * - `picture`: A static placeholder image URL.
     *
     * Example output:
     * ```
     * eyJhbGciOiJub25lIn0.eyJzdWIiOiIwIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIiwibmFtZSI6IkpvaG4gRG9lIiwicGljdHVyZSI6Imh0dHA6Ly9leGFtcGxlLmNvbS9hdmF0YXIucG5nIn0.sig
     * ```
     *
     * @param name The display name to include in the token payload.
     * @param email The email address to include in the token payload.
     * @return A fake, unsigned JWT string suitable for test environments.
     */
    fun createFakeGoogleIdToken(name: String, email: String): String {
        val header = JSONObject(mapOf("alg" to "none"))
        val payload =
            JSONObject(
                mapOf(
                    "sub" to counter.toString(),
                    "email" to email,
                    "name" to name,
                    "picture" to "http://example.com/avatar.png"
                )
            )

        val headerEncoded = base64UrlEncode(header.toString().toByteArray())
        val payloadEncoded = base64UrlEncode(payload.toString().toByteArray())

        // Signature can be anything, emulator doesn't check it
        val signature = "sig"

        return "$headerEncoded.$payloadEncoded.$signature"
    }
}
