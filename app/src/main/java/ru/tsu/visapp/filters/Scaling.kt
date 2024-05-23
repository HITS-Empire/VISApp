package ru.tsu.visapp.filters

import ru.tsu.visapp.filters.UnsharpMask
import android.graphics.Color
import android.graphics.Bitmap

class Scaling : UnsharpMask() {
    fun increaseImage(
        width: Int,
        height: Int,
        pixels: IntArray,
        scaleFactor: Int,
    ) : Bitmap {
        val floatScaleFactor = scaleFactor.toFloat() / 100
        val newWidth = (width * floatScaleFactor).toInt()
        val newHeight = (height * floatScaleFactor).toInt()
        val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        val scaleX = width.toFloat() / newWidth
        val scaleY = height.toFloat() / newHeight

        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {
                val extraX = x * scaleX
                val extraY = y * scaleY

                val x1 = extraX.toInt()
                val y1 = extraY.toInt()
                val x2 = (x1 + 1).coerceAtMost(width - 1)
                val y2 = (y1 + 1).coerceAtMost(height - 1)

                val pixel1 = pixels[x1 + y1 * width]
                val pixel2 = pixels[x2 + y1 * width]
                val pixel3 = pixels[x1 + y2 * width]
                val pixel4 = pixels[x2 + y2 * width]

                val dx = extraX - x1
                val dy = extraY - y1

                val red = bilinearInterpolation(pixel1 shr 16 and 0xFF, pixel2 shr 16 and 0xFF, pixel3 shr 16 and 0xFF, pixel4 shr 16 and 0xFF, dx, dy)
                val green = bilinearInterpolation(pixel1 shr 8 and 0xFF, pixel2 shr 8 and 0xFF, pixel3 shr 8 and 0xFF, pixel4 shr 8 and 0xFF, dx, dy)
                val blue = bilinearInterpolation(pixel1 and 0xFF, pixel2 and 0xFF, pixel3 and 0xFF, pixel4 and 0xFF, dx, dy)

                val newColor = Color.argb(255, red, green, blue)
                scaledBitmap.setPixel(x, y, newColor)
            }
        }
        //bitmap.recycle()
        return scaledBitmap
    }

    private fun bilinearInterpolation(pixel1: Int, pixel2: Int, pixel3: Int, pixel4: Int, dx: Float, dy: Float): Int {
        val pixels1 = (pixel1 * (1 - dx) + pixel2 * dx).toInt()
        val pixels2 = (pixel3 * (1 - dx) + pixel4 * dx).toInt()
        return ((pixels1 * (1 - dy) + pixels2 * dy).toInt())
    }

    fun decreaseImage(
        width: Int,
        height: Int,
        pixels: IntArray,
        scaleFactor: Int,
    ) : Bitmap {
        var floatScaleFactor = scaleFactor.toFloat() / 100
        val scaledWidth = (width * floatScaleFactor).toInt()
        val scaledHeight = (height * floatScaleFactor).toInt()
        val scaledBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)

        val scaleX = width.toFloat() / scaledWidth
        val scaleY = height.toFloat() / scaledHeight

        val gaussianRadius = when {
            width * floatScaleFactor <= 100 || height * floatScaleFactor <= 100 -> 10
            width * floatScaleFactor <= 200 || height * floatScaleFactor <= 200 -> 3
            else -> 1
        }

        val blurredPixels = gaussianBlur(pixels, gaussianRadius, width, height)

        val newPixels = IntArray(scaledWidth * scaledHeight)

        for (x in 0 until scaledWidth) {
            for (y in 0 until scaledHeight) {
                val extraX = x * scaleX
                val extraY = y * scaleY

                val x1 = extraX.toInt()
                val y1 = extraY.toInt()
                val x2 = (x1 + 1).coerceAtMost(width - 1)
                val y2 = (y1 + 1).coerceAtMost(height - 1)

                val dx1 = extraX - x1
                val dy1 = extraY - y1
                val dx2 = 1.0f - dx1
                val dy2 = 1.0f - dy1

                val pixel1 = blurredPixels[x1 + y1 * width]
                val pixel2 = blurredPixels[x2 + y1 * width]
                val pixel3 = blurredPixels[x1 + y2 * width]
                val pixel4 = blurredPixels[x2 + y2 * width]

                val pixel5 = if (y1 > 0) blurredPixels[x1 + (y1 - 1) * width] else pixel1
                val pixel6 = if (x2 < width - 1) blurredPixels[x2 + 1 + y1 * width] else pixel2
                val pixel7 = if (y2 < height - 1) blurredPixels[x1 + (y2 + 1) * width] else pixel3
                val pixel8 = if (x2 < width - 1 && y2 < height - 1) blurredPixels[x2 + 1 + (y2 + 1) * width] else pixel4

                val red = trilinearInterpolation(
                    pixel1 shr 16 and 0xFF, pixel2 shr 16 and 0xFF, pixel3 shr 16 and 0xFF, pixel4 shr 16 and 0xFF,
                    pixel5 shr 16 and 0xFF, pixel6 shr 16 and 0xFF, pixel7 shr 16 and 0xFF, pixel8 shr 16 and 0xFF,
                    dx1, dy1, dx2, dy2
                )
                val green = trilinearInterpolation(
                    pixel1 shr 8 and 0xFF, pixel2 shr 8 and 0xFF, pixel3 shr 8 and 0xFF, pixel4 shr 8 and 0xFF,
                    pixel5 shr 8 and 0xFF, pixel6 shr 8 and 0xFF, pixel7 shr 8 and 0xFF, pixel8 shr 8 and 0xFF,
                    dx1, dy1, dx2, dy2
                )
                val blue = trilinearInterpolation(
                    pixel1 and 0xFF, pixel2 and 0xFF, pixel3 and 0xFF, pixel4 and 0xFF,
                    pixel5 and 0xFF, pixel6 and 0xFF, pixel7 and 0xFF, pixel8 and 0xFF,
                    dx1, dy1, dx2, dy2
                )

                newPixels[x + y * scaledWidth] = Color.argb(255, red, green, blue)
            }
        }
        scaledBitmap.setPixels(newPixels, 0, scaledWidth, 0, 0, scaledWidth, scaledHeight)
        return scaledBitmap
    }
    
    private fun trilinearInterpolation(
        pixel1: Int, pixel2: Int, pixel3: Int, pixel4: Int,
        pixel5: Int, pixel6: Int, pixel7: Int, pixel8: Int,
        dx1: Float, dy1: Float, dx2: Float, dy2: Float
    ): Int {
        val pixels1 = linearInterpolation(pixel1, pixel2, dx1)
        val pixels2 = linearInterpolation(pixel5, pixel6, dx1)
        val pixels3 = linearInterpolation(pixels1, pixels2, dy1)
    
        val pixels4 = linearInterpolation(pixel3, pixel4, dx1)
        val pixels5 = linearInterpolation(pixel7, pixel8, dx1)
        val pixels6 = linearInterpolation(pixels4, pixels5, dy1)
    
        return linearInterpolation(pixels3, pixels6, dx2)
    }
    
    private fun linearInterpolation(pixel1: Int, pixel2: Int, ratio: Float): Int {
        return (pixel1 * (1 - ratio) + pixel2 * ratio).toInt()
    }
}