package ru.tsu.visapp.filters

import android.graphics.Color
import android.graphics.Bitmap
import androidx.core.graphics.red
import androidx.core.graphics.blue
import androidx.core.graphics.green
import ru.tsu.visapp.utils.ImageEditor
import ru.tsu.visapp.utils.PixelsEditor

class Scaling {
    private val imageEditor = ImageEditor()

    fun increaseImage(
        width: Int,
        height: Int,
        pixels: IntArray,
        scaleFactor: Int,
    ): Bitmap {
        val floatScaleFactor = scaleFactor.toFloat() / 100

        val pixelsEditor = PixelsEditor(pixels, width, height)

        val newWidth = (width * floatScaleFactor).toInt()
        val newHeight = (height * floatScaleFactor).toInt()

        val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val newPixels = imageEditor.getPixelsFromBitmap(newBitmap)
        val newPixelsEditor = PixelsEditor(newPixels, newWidth, newHeight)

        val forX = width.toFloat() / newWidth
        val forY = height.toFloat() / newHeight

        for (i in 0 until newWidth) {
            for (j in 0 until newHeight) {
                val scaledX = i * forX
                val scaledY = j * forY

                val startX = scaledX.toInt()
                val startY = scaledY.toInt()
                val newX = (startX + 1).coerceAtMost(width - 1)
                val newY = (startY + 1).coerceAtMost(height - 1)

                val pixelUp = pixelsEditor.getPixel(startX, startY) ?: 0
                val pixelRight = pixelsEditor.getPixel(newX, startY) ?: 0
                val pixelDown = pixelsEditor.getPixel(startX, newY) ?: 0
                val pixelLeft = pixelsEditor.getPixel(newX, newY) ?: 0

                val dStartX = scaledX - startX
                val dStartY = scaledY - startY

                newPixelsEditor.setPixel(
                    i,
                    j,
                    Color.argb(
                        255,
                        bilinInt(
                            dStartX,
                            dStartY,
                            pixelUp.red,
                            pixelRight.red,
                            pixelDown.red,
                            pixelLeft.red
                        ),
                        bilinInt(
                            dStartX,
                            dStartY,
                            pixelUp.green,
                            pixelRight.green,
                            pixelDown.green,
                            pixelLeft.green
                        ),
                        bilinInt(
                            dStartX,
                            dStartY,
                            pixelUp.blue,
                            pixelRight.blue,
                            pixelDown.blue,
                            pixelLeft.blue
                        )
                    )
                )
            }
        }

        imageEditor.setPixelsToBitmap(newBitmap, newPixels)

        return newBitmap
    }

    private fun bilinInt(
        dStartX: Float,
        dStartY: Float,
        pixelUp: Int,
        pixelRight: Int,
        pixelDown: Int,
        pixelLeft: Int,
    ): Int {
        val averageUpRight = linInt(pixelUp, pixelRight, dStartX)
        val averageDownLeft = linInt(pixelDown, pixelLeft, dStartX)

        return linInt(averageUpRight, averageDownLeft, dStartY)
    }

    fun decreaseImage(
        width: Int,
        height: Int,
        pixels: IntArray,
        scaleFactor: Int,
    ): Bitmap {
        val floatScaleFactor = scaleFactor.toFloat() / 100

        val pixelsEditor = PixelsEditor(pixels, width, height)

        val newWidth = (width * floatScaleFactor).toInt()
        val newHeight = (height * floatScaleFactor).toInt()

        val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val newPixels = imageEditor.getPixelsFromBitmap(newBitmap)
        val newPixelsEditor = PixelsEditor(newPixels, newWidth, newHeight)

        val forX = width.toFloat() / newWidth
        val forY = height.toFloat() / newHeight

        for (i in 0 until newWidth) {
            for (j in 0 until newHeight) {
                val scaledX = i * forX
                val scaledY = j * forY

                val startX = scaledX.toInt()
                val startY = scaledY.toInt()
                val newX = (startX + 1).coerceAtMost(width - 1)
                val newY = (startY + 1).coerceAtMost(height - 1)

                val dStartX = scaledX - startX
                val dStartY = scaledY - startY
                val diffStartX = 1.0f - dStartX

                val pixelUp = pixelsEditor.getPixel(startX, startY) ?: 0
                val pixelRight = pixelsEditor.getPixel(newX, startY) ?: 0
                val pixelDown = pixelsEditor.getPixel(startX, newY) ?: 0
                val pixelLeft = pixelsEditor.getPixel(newX, newY) ?: 0

                val pixelUpLeft = pixelsEditor.getPixel(startX, startY - 1)
                    ?: pixelUp
                val pixelUpRight = pixelsEditor.getPixel(newX + 1, startY)
                    ?: pixelRight
                val pixelDownRight = pixelsEditor.getPixel(startX, newY + 1)
                    ?: pixelDown
                val pixelDownRightLeft = pixelsEditor.getPixel(newX + 1, newY + 1)
                    ?: pixelLeft

                newPixelsEditor.setPixel(
                    i,
                    j,
                    Color.argb(
                        255,
                        triInt(
                            dStartX,
                            dStartY,
                            diffStartX,
                            pixelUp.red,
                            pixelRight.red,
                            pixelDown.red,
                            pixelLeft.red,
                            pixelUpLeft.red,
                            pixelUpRight.red,
                            pixelDownRight.red,
                            pixelDownRightLeft.red
                        ),
                        triInt(
                            dStartX,
                            dStartY,
                            diffStartX,
                            pixelUp.green,
                            pixelRight.green,
                            pixelDown.green,
                            pixelLeft.green,
                            pixelUpLeft.green,
                            pixelUpRight.green,
                            pixelDownRight.green,
                            pixelDownRightLeft.green
                        ),
                        triInt(
                            dStartX,
                            dStartY,
                            diffStartX,
                            pixelUp.blue,
                            pixelRight.blue,
                            pixelDown.blue,
                            pixelLeft.blue,
                            pixelUpLeft.blue,
                            pixelUpRight.blue,
                            pixelDownRight.blue,
                            pixelDownRightLeft.blue
                        )
                    )
                )
            }
        }

        imageEditor.setPixelsToBitmap(newBitmap, newPixels)

        return newBitmap
    }

    private fun triInt(
        dStartX: Float,
        dStartY: Float,
        diffStartX: Float,
        pixelUp: Int,
        pixelRight: Int,
        pixelDown: Int,
        pixelLeft: Int,
        pixelUpLeft: Int,
        pixelUpRight: Int,
        pixelDownRight: Int,
        pixelDownRightLeft: Int
    ): Int {
        val averageFirst = linInt(pixelUp, pixelRight, dStartX)
        val averageSecond = linInt(pixelUpLeft, pixelUpRight, dStartX)
        val averageThird = linInt(averageFirst, averageSecond, dStartY)

        val averageFourth = linInt(pixelDown, pixelLeft, dStartX)
        val averageFifth = linInt(pixelDownRight, pixelDownRightLeft, dStartX)
        val averageSixth = linInt(averageFourth, averageFifth, dStartY)

        return linInt(averageThird, averageSixth, diffStartX)
    }

    private fun linInt(pixelFirst: Int, pixelSecond: Int, ratio: Float): Int {
        return (pixelSecond * ratio + pixelFirst * (1 - ratio)).toInt()
    }
}