package ru.tsu.visapp.filters

import android.graphics.Color
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.alpha
import androidx.core.graphics.green
import ru.tsu.visapp.utils.PixelsEditor
import kotlin.math.max
import kotlin.math.min

class PopArt {
    private lateinit var pixelsEditor: PixelsEditor
    private lateinit var pixelsEditorResult: PixelsEditor

    private lateinit var resultPixels: IntArray

    private val inversion = Inversion()

    fun doubleColoring(
        pixels: IntArray,
        width: Int,
        height: Int,
        threshold1: Int,
        threshold2: Int
    ): IntArray {
        resultPixels = IntArray(pixels.size) { 0 }

        pixelsEditor = PixelsEditor(pixels, width, height)
        pixelsEditorResult = PixelsEditor(resultPixels, width, height)

        for (i in 0 ..< width) {
            for (j in 0..< height) {
                val pixel = pixelsEditor.getPixel(i, j)

                val alpha = pixel?.alpha ?: 0
                var red = pixel?.red ?: 0
                var green = pixel?.green ?: 0
                var blue = pixel?.blue ?: 0

                if (red + green + blue in 0..< 3 * min(threshold1, threshold2)) {
                    red = 0
                    green = 0
                    blue = 0
                }
                else if (red + green + blue in
                    3 * min(threshold1, threshold2) ..< 3 * max(threshold1, threshold2)) {
                    red = 128
                    green = 128
                    blue = 128
                }
                else {
                    red = 255
                    green = 255
                    blue = 255
                }

                pixelsEditorResult.setPixel(i, j, Color.argb(alpha, red, green, blue))
            }
        }

        return resultPixels
    }

    fun popArtFiltering(
        pixels: IntArray,
        width: Int,
        height: Int,
        threshold1: Int,
        threshold2: Int
    ): IntArray {
        resultPixels = IntArray(pixels.size * 4) { 0 }

        pixelsEditor = PixelsEditor(pixels, width, height)
        pixelsEditorResult = PixelsEditor(resultPixels, 4 * width, 4 * height)

        // Получение изображения в 3-х оттенках
        val thresholdedImage = doubleColoring(
            pixels,
            width,
            height,
            threshold1,
            threshold2
        )

        val firstImage = inversion.inverse(
            thresholdedImage,
            width,
            height,
            isRedInverting = true,
            isGreenInverting = true,
            isBlueInverting = false
        )

        val firstImageEditor = PixelsEditor(firstImage, width, height)

        for (i in 0 ..< width) {
            for (j in 0..< height) {
                pixelsEditorResult.setPixel(
                    i,
                    j,
                    firstImageEditor.getPixel(i, j))
            }
        }

        val secondImage = inversion.inverse(
            thresholdedImage,
            width,
            height,
            isRedInverting = true,
            isGreenInverting = false,
            isBlueInverting = true
        )

        val secondImageEditor = PixelsEditor(secondImage, width, height)

        for (i in 0 ..< width) {
            for (j in 0..< height) {
                pixelsEditorResult.setPixel(
                    i + width,
                    j,
                    secondImageEditor.getPixel(i, j))
            }
        }

        val thirdImage = inversion.inverse(
            thresholdedImage,
            width,
            height,
            isRedInverting = false,
            isGreenInverting = true,
            isBlueInverting = true
        )

        val thirdImageEditor = PixelsEditor(thirdImage, width, height)

        for (i in 0 ..< width) {
            for (j in 0..< height) {
                pixelsEditorResult.setPixel(
                    i,
                    j + height,
                    thirdImageEditor.getPixel(i, j))
            }
        }

        val fourthImage = inversion.inverse(
            thresholdedImage,
            width,
            height,
            isRedInverting = false,
            isGreenInverting = true,
            isBlueInverting = false
        )

        val fourthImageEditor = PixelsEditor(fourthImage, width, height)

        for (i in 0 ..< width) {
            for (j in 0..< height) {
                pixelsEditorResult.setPixel(
                    i + width,
                    j + height,
                    fourthImageEditor.getPixel(i, j))
            }
        }

        return resultPixels
    }
}