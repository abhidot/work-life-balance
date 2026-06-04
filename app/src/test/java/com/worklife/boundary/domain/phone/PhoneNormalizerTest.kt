package com.worklife.boundary.domain.phone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneNormalizerTest {

    private val normalizer = PhoneNormalizer(defaultRegion = "US")

    @Test
    fun normalizes_us_number_with_country_code() {
        assertEquals("+14155552671", normalizer.normalize("+1 (415) 555-2671"))
    }

    @Test
    fun normalizes_us_number_without_country_code() {
        assertEquals("+14155552671", normalizer.normalize("4155552671"))
    }

    @Test
    fun returns_null_for_invalid_number() {
        assertNull(normalizer.normalize("not-a-phone"))
    }

    @Test
    fun matches_same_number_in_different_formats() {
        val a = normalizer.normalize("+14155552671")
        val b = normalizer.normalize("(415) 555-2671")
        assertEquals(a, b)
    }
}
