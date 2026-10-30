package com.educationwithhassan.pdfimageconverter

import org.junit.Assert.*
import org.junit.Test

class ConversionLogicTest {

    @Test
    fun testPageDimensionCalculations() {
        val a4Width = 595
        val a4Height = 842
        assertTrue(a4Height > a4Width)

        val isLandscape = true
        val effectiveWidth = if (isLandscape) a4Height else a4Width
        val effectiveHeight = if (isLandscape) a4Width else a4Height
        assertEquals(842, effectiveWidth)
        assertEquals(595, effectiveHeight)
    }

    @Test
    fun testQualityClamping() {
        val inputQuality = 120
        val clamped = inputQuality.coerceIn(10, 100)
        assertEquals(100, clamped)

        val lowQuality = 5
        val clampedLow = lowQuality.coerceIn(10, 100)
        assertEquals(10, clampedLow)
    }

    @Test
    fun testPageRangeParsing() {
        val rawInput = "1, 2, 4, invalid, 5"
        val parsed = rawInput.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
        assertEquals(listOf(1, 2, 4, 5), parsed)
    }
}
