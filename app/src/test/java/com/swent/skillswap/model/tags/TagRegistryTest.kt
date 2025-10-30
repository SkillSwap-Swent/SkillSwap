package com.swent.skillswap.model.tags

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TagRegistryTest {

    @Test
    fun everyTag_valueOf_resolvesSkillTag() {
        val tag = EveryTag.valueOf("CALCULUS")
        assertEquals(SkillTag.CALCULUS, tag)
    }

    @Test
    fun tagRegistry_throws_onUnknown() {
        assertThrows(IllegalArgumentException::class.java) {
            EveryTag.valueOf("UNKNOWN_TAG___XYZ")
        }
    }
}


