package ru.tsu.visapp.filters

import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import android.graphics.Bitmap
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import ru.tsu.visapp.utils.ImageEditor
import ru.tsu.visapp.utils.PixelsEditor
import kotlinx.coroutines.coroutineScope

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

        when (angle) {
            in 0..90 -> {
                maxRow = centerRow + (rows - centerRow) * cos(radians) -
                        (0 - centerCol) * sin(radians)
                maxCol = centerCol + (cols - centerCol) * cos(radians) +
                        (rows - centerRow) * sin(radians)
                minRow = centerRow + (0 - centerRow) * cos(radians) -
                        (cols - centerCol) * sin(radians)
                minCol = centerCol + (0 - centerCol) * cos(radians) +
                        (0 - centerRow) * sin(radians)
            }

            in 90..180 -> {
                maxRow = centerRow + (0 - centerRow) * cos(radians) -
                        (0 - centerCol) * sin(radians)
                maxCol = centerCol + (0 - centerCol) * cos(radians) +
                        (rows - centerRow) * sin(radians)
                minRow = centerRow + (rows - centerRow) * cos(radians) -
                        (cols - centerCol) * sin(radians)
                minCol = centerCol + (cols - centerCol) * cos(radians) +
                        (0 - centerRow) * sin(radians)
            }

            in 180..270 -> {
                maxRow = centerRow + (0 - centerRow) * cos(radians) -
                        (cols - centerCol) * sin(radians)
                maxCol = centerCol + (0 - centerCol) * cos(radians) +
                        (0 - centerRow) * sin(radians)
                minRow = centerRow + (rows - centerRow) * cos(radians) -
                        (0 - centerCol) * sin(radians)
                minCol = centerCol + (cols - centerCol) * cos(radians) +
                        (rows - centerRow) * sin(radians)
            }

            else -> {
                maxRow = centerRow + (rows - centerRow) * cos(radians) -
                        (cols - centerCol) * sin(radians)
                maxCol = centerCol + (cols - centerCol) * cos(radians) +
                        (0 - centerRow) * sin(radians)
                minRow = centerRow + (0 - centerRow) * cos(radians) -
                        (0 - centerCol) * sin(radians)
                minCol = centerCol + (0 - centerCol) * cos(radians) +
                        (rows - centerRow) * sin(radians)
            }
        }

        return arrayOf(maxRow, maxCol, minRow, minCol)
    }

    data class ProcessPixel(
        private val angle: Int,
        private val radians: Float,
        private val minWidth: Float,
        private val minHeight: Float,
        private val newWidth: Int,
        private val newHeight: Int,
        private val halfWidth: Int,
        private val halfHeight: Int,
        private val initPixelsEditor: PixelsEditor,
        private val newPixelsEditor: PixelsEditor
    ) {
        fun start(i: Int, j: Int) {
            // Вычисление координат для пикселя в новом изображении
            val newI = (halfWidth + (i - halfWidth) * cos(radians) -
                    (j - halfHeight) * sin(radians) - minWidth).toInt()
            var newJ = (halfHeight + (j - halfHeight) * cos(radians) +
                    (i - halfWidth) * sin(radians) - minHeight).toInt()

            newPixelsEditor.setPixel(newI, newJ, initPixelsEditor.getPixel(i, j))

            // Заполнение пропущенных пикселей
            if (angle in 0..180) {
                // Если предыдущее значение пустой пиксель
                if (newJ > 0 && newPixelsEditor.getPixel(newI, newJ - 1) == 0) {
                    if (i > 0 && j > 0) {
                        while (newPixelsEditor.getPixel(newI, newJ - 1) == 0) {
                            newJ--

                            val a = newPixelsEditor.getPixel(newI + 1, newJ + 1) != 0
                            val b = newJ < 1

                            if (a || b) break
                        }
                    }
                    newPixelsEditor.setPixel(newI, newJ, initPixelsEditor.getPixel(i, j))
                }
            } else {
                // Если следующее значение - пустой пиксель
                if (newJ + 1 < newHeight && newPixelsEditor.getPixel(newI, newJ + 1) == 0) {
                    if (i > 0 && j > 0) {
                        while (newPixelsEditor.getPixel(newI, newJ + 1) == 0) {
                            newJ++

                            val a = newPixelsEditor.getPixel(newI - 1, newJ - 1) != 0
                            val b = newJ + 1 >= newHeight

                            if (a || b) break
                        }
                    }
                    newPixelsEditor.setPixel(newI, newJ, initPixelsEditor.getPixel(i, j))
                }
            }
        }
    }

    suspend fun rotate(
        initPixels: IntArray,
        initWidth: Int,
        initHeight: Int,
        angle: Int
    ): Bitmap = coroutineScope {
        val radians = toRadians(angle)

        // Получение вершин нового изображения
        val (
            maxWidth,
            maxHeight,
            minWidth,
            minHeight
        ) = getEdges(initWidth, initHeight, angle)

        // Получение размера нового изображения
        val (newWidth, newHeight) = getRotatedImageSize(
            maxWidth,
            maxHeight,
            minWidth,
            minHeight
        )

        val bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val newPixels = imageEditor.getPixelsFromBitmap(bitmap)

        val initPixelsEditor = PixelsEditor(initPixels, initWidth, initHeight)
        val newPixelsEditor = PixelsEditor(newPixels, newWidth, newHeight)

        val halfWidth = initWidth / 2
        val halfHeight = initHeight / 2

        val processPixel = ProcessPixel(
            angle,
            radians,
            minWidth,
            minHeight,
            newWidth,
            newHeight,
            halfWidth,
            halfHeight,
            initPixelsEditor,
            newPixelsEditor
        )

        val jobs = arrayOf(
            arrayOf(0, halfWidth, 0, halfHeight),
            arrayOf(halfWidth, initWidth, 0, halfHeight),
            arrayOf(0, halfWidth, halfHeight, initHeight),
            arrayOf(halfWidth, initWidth, halfHeight, initHeight)
        ).map { a ->
            async(Dispatchers.Default) {
                for (i in a[0] until a[1]) {
                    for (j in a[2] until a[3]) {
                        processPixel.start(i, j)
                    }
                }
            }
        }

        jobs.awaitAll()

        imageEditor.setPixelsToBitmap(bitmap, newPixels)

        return@coroutineScope bitmap
    }
}