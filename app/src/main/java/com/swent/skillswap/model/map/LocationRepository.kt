package com.swent.skillswap.model.map

interface LocationRepository {
    suspend fun search(query: String): List<Location>
}
