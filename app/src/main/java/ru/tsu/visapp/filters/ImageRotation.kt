package ru.tsu.visapp.filters

import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.max
import android.graphics.Bitmap
import ru.tsu.visapp.utils.ImageEditor
import ru.tsu.visapp.utils.PixelsEditor

/*
 * Реализация фильтра поворота изображения на любой градус
 */

class ImageRotation {
    private val imageEditor = ImageEditor() // Редактор изображений

    private fun toRadians(angle: Int): Float {
        return angle * (PI / 180).toFloat()
    }

    private fun getRotatedImageSize(
        maxRow: Float,
        maxCol: Float,
        minRow: Float,
        minCol: Float
    ): Array<Int> {
        val newRows = (maxRow - minRow + 1).toInt()
        val newCols = (maxCol - minCol + 1).toInt()

        return arrayOf(newRows, newCols)
    }

    private fun getEdges(rows: Int, cols: Int, angle: Int): Array<Float> {
        val radians = toRadians(angle)

        val maxRow: Float
        val maxCol: Float
        val minRow: Float
        val minCol: Float

        val centerRow = rows / 2
        val centerCol = cols / 2

        if (angle in 0 .. 90) {
            maxRow = centerRow + (rows - centerRow) * cos(radians) - (0 - centerCol) * sin(radians)
            maxCol = centerCol + (cols - centerCol) * cos(radians) + (rows - centerRow) * sin(radians)
            minRow = centerRow + (0 - centerRow) * cos(radians) - (cols - centerCol) * sin(radians)
            minCol = centerCol + (0 - centerCol) * cos(radians) + (0 - centerRow) * sin(radians)
        } else if (angle in 90 .. 180) {
            maxRow = centerRow + (0 - centerRow) * cos(radians) - (0 - centerCol) * sin(radians)
            maxCol = centerCol + (0 - centerCol) * cos(radians) + (rows - centerRow) * sin(radians)
            minRow = centerRow + (rows - centerRow) * cos(radians) - (cols - centerCol) * sin(radians)
            minCol = centerCol + (cols - centerCol) * cos(radians) + (0 - centerRow) * sin(radians)
        } else if (angle in 180 .. 270) {
            maxRow = centerRow + (0 - centerRow) * cos(radians) - (cols - centerCol) * sin(radians)
            maxCol = centerCol + (0 - centerCol) * cos(radians) + (0 - centerRow) * sin(radians)
            minRow = centerRow + (rows - centerRow) * cos(radians) - (0 - centerCol) * sin(radians)
            minCol = centerCol + (cols - centerCol) * cos(radians) + (rows - centerRow) * sin(radians)
        } else {
            maxRow = centerRow + (rows - centerRow) * cos(radians) - (cols - centerCol) * sin(radians)
            maxCol = centerCol + (cols - centerCol) * cos(radians) + (0 - centerRow) * sin(radians)
            minRow = centerRow + (0 - centerRow) * cos(radians) - (0 - centerCol) * sin(radians)
            minCol = centerCol + (0 - centerCol) * cos(radians) + (rows - centerRow) * sin(radians)
        }

        return arrayOf(maxRow, maxCol, minRow, minCol)
    }

    fun rotate(
        initPixels: IntArray,
        initWidth: Int,
        initHeight: Int,
        angle: Int
    ): Bitmap {
        val radians = toRadians(angle)

        // Получение вершин нового изображения
        val (
            maxWidth,
            maxHeight,
            minWidth,
            minHeight
        ) = getEdges(initWidth, initHeight, angle)

        // Получение размера нового изображения
        var (newWidth, newHeight) = getRotatedImageSize(
            maxWidth,
            maxHeight,
            minWidth,
            minHeight
        )
        newWidth = max(initWidth, newWidth)
        newHeight = max(initHeight, newHeight)

        val bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val newPixels = imageEditor.getPixelsFromBitmap(bitmap)

        val initPixelsEditor = PixelsEditor(initPixels, initWidth, initHeight)
        val newPixelsEditor = PixelsEditor(newPixels, newWidth, newHeight)

        val halfWidth = initWidth / 2
        val halfHeight = initHeight / 2

        for (i in 0 until newWidth) {
            for (j in 0 until newHeight) {
                if (i >= initWidth || j >= initHeight) continue

                // Вычисление координат для пикселя в новом изображении
                var newI = (halfWidth + (i - halfWidth) * cos(radians) -
                    (j - halfHeight) * sin(radians) - minWidth).toInt()
                var newJ = (halfHeight + (j - halfHeight) * cos(radians) +
                    (i - halfWidth) * sin(radians) - minHeight).toInt()

                newPixelsEditor.setPixel(newI, newJ, initPixelsEditor.getPixel(i, j))

                // Заполнение пропущенных пикселей
                if (angle in 0 .. 180) {
                    // Если предыдущее значение пустой пиксель
                    if (newJ > 0 && newPixelsEditor.getPixel(newI, newJ - 1) == 0) {
                        if (i > 0 && j > 0) {
                            while (newPixelsEditor.getPixel(newI, newJ - 1) == 0) {
                                newJ--

                                if ((
                                    newPixelsEditor.getPixel(newI + 1, newJ + 1) != 0
                                ) || (
                                    newJ < 1
                                )) {
                                    break
                                }
                            }
                        }
                        newPixelsEditor.setPixel(newI, newJ, initPixelsEditor.getPixel(i, j))
                    }
                    continue
                }

                // Если следующее значение - пустой пиксель
                if (newJ + 1 < newHeight && newPixelsEditor.getPixel(newI, newJ - 1) == 0) {
                    if (i > 0 && j > 0) {
                        while (newPixelsEditor.getPixel(newI, newJ + 1) == 0) {
                            newJ++

                            if ((
                                newPixelsEditor.getPixel(newI - 1, newJ - 1) != 0
                            ) || (
                                newJ + 1 >= newHeight
                            )) {
                                break
                            }
                        }
                    }
                    newPixelsEditor.setPixel(newI, newJ, initPixelsEditor.getPixel(i, j))
                }
            }
        }

        imageEditor.setPixelsToBitmap(bitmap, newPixels)

        return bitmap
    }
}