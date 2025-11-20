/** Author Alex */
package com.swent.skillswap.model.utils

/**
 * Custom exception for repository-related errors.
 *
 * @param message The detail message for the exception.
 * @param cause The cause of the exception.
 */
class RepositoryException(message: String, cause: Throwable? = null) :
    Exception(
        buildString {
            append(message)
            cause?.message?.let { append(": $it") } // recursively append cause message
        },
        cause
    )
