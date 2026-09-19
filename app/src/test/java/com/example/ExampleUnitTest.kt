package com.example

import com.example.model.OverlaySettings
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testFourStateOffsetResolution() {
    val settings = OverlaySettings(
      offsetX = 10,
      offsetY = 20,
      foldedRotatedOffsetX = 110,
      foldedRotatedOffsetY = 120,
      innerOffsetX = 210,
      innerOffsetY = 220,
      innerRotatedOffsetX = 310,
      innerRotatedOffsetY = 320
    )

    // Folded Upright (Cover Portrait)
    assertEquals(10 to 20, settings.getCurrentOffset(isUnfolded = false, isRotated = false))

    // Folded Rotated (Cover Landscape)
    assertEquals(110 to 120, settings.getCurrentOffset(isUnfolded = false, isRotated = true))

    // Inner Upright (Unfolded Portrait)
    assertEquals(210 to 220, settings.getCurrentOffset(isUnfolded = true, isRotated = false))

    // Inner Rotated (Unfolded Landscape)
    assertEquals(310 to 320, settings.getCurrentOffset(isUnfolded = true, isRotated = true))
  }
}
