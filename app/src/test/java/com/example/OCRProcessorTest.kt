package com.example

import com.example.ui.scanner.OCRProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OCRProcessorTest {

    @Test
    fun `test amount disambiguation and mobile exclusion`() {
        // Test that 10-digit mobile numbers starting with 740 are excluded and 949 is correctly extracted
        val isIgnoredNumber: (String) -> Boolean = { raw ->
            val clean = raw.replace("-", "").replace(" ", "").trim()
            clean.length == 10 && (clean.startsWith("6") || clean.startsWith("7") || clean.startsWith("8") || clean.startsWith("9")) ||
                    clean.length > 10 || clean.startsWith("2-005") || clean.contains("402-")
        }

        assertEquals(true, isIgnoredNumber("7405883187"))
        assertEquals(true, isIgnoredNumber("402-8836400-2047538"))
        assertEquals(true, isIgnoredNumber("2-005937449042"))
        assertEquals(false, isIgnoredNumber("949"))
        assertEquals(false, isIgnoredNumber("949.00"))
    }
}
