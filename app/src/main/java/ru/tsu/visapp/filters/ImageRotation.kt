package ru.tsu.visapp.filters

import kotlin.math.cos
import kotlin.math.sin
import ru.tsu.visapp.utils.ImageEditor.Pixel

/*
 * Реализация фильтра поворота изображения на любой градус
 */

class ImageRotation {
    private fun toRadians(angle : Int) : Float {
        return angle * (kotlin.math.PI / 180).toFloat()
    }

    private fun getRotatedImageSize(maxRow : Float, maxCol : Float,
                                    minRow : Float, minCol : Float) : Array<Int> {
        val newRows = (maxRow - minRow + 1).toInt()
        val newCols = (maxCol - minCol + 1).toInt()

        return arrayOf(newRows, newCols)
    }

    private fun getEdges(rows : Int, cols : Int, angle : Int) : Array<Float> {
        val radians = toRadians(angle)

        var maxRow: Float
        var maxCol: Float
        var minRow: Float
        var minCol: Float

        val centerRow = rows / 2
        val centerCol = cols / 2

        if (angle in 0..90) {
            maxRow = centerRow + (rows - centerRow) * cos(radians) - (0 - centerCol) * sin(radians)
            maxCol = centerCol + (cols - centerCol) * cos(radians) + (rows - centerRow) * sin(radians)
            minRow = centerRow + (0 - centerRow) * cos(radians) - (cols - centerCol) * sin(radians)
            minCol = centerCol + (0 - centerCol) * cos(radians) + (0 - centerRow) * sin(radians)
        } else if (angle in 91..180) {
            maxRow = centerRow + (0 - centerRow) * cos(radians) - (0 - centerCol) * sin(radians)
            maxCol = centerCol + (0 - centerCol) * cos(radians) + (rows - centerRow) * sin(radians)
            minRow = centerRow + (rows - centerRow) * cos(radians) - (cols - centerCol) * sin(radians)
            minCol = centerCol + (cols - centerCol) * cos(radians) + (0 - centerRow) * sin(radians)
        } else if (angle in 181..270) {
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

    fun rotate(image: Array<Array<Pixel>>, angle: Int) : Array<Array<Pixel>> {
        val radians = toRadians(angle)

        val rows = image.size
        val cols = image[0].size

        // Получение вершин нового изображения
        val (maxRow, maxCol, minRow, minCol) = getEdges(rows, cols, angle)

        // Получение размера нового изображения
        var (newRows, newCols) = getRotatedImageSize(maxRow, maxCol, minRow, minCol)
        newRows = maxOf(newRows, rows)
        newCols = maxOf(newCols, cols)

        val newImage = Array(newRows) { Array(newCols) { Pixel(-1, -1, -1) } }

        for (i in 0 until newRows) {
            for (j in 0 until newCols) {
                if (i < rows && j < cols) {
                    // Вычисление координат для пикселя в новом изображении
                    var newI = (rows / 2 + (i - rows / 2) * cos(radians) -
                               (j - cols / 2) * sin(radians) - minRow).toInt()
                    var newJ = (cols / 2 + (j - cols / 2) * cos(radians) +
                               (i - rows / 2) * sin(radians) - minCol).toInt()

                    newImage[newI][newJ] = image[i][j]

                    // Заполнение пропущенных пикселей
                    if (angle in 0..180) {
                        // Если предыдущее значение пустой пиксель
                        if (newJ > 0 && newImage[newI][newJ - 1].equals(-1)) {
                            if (i > 0 && j > 0) {
                                while (newImage[newI][newJ - 1].equals(-1)) {
                                    newJ -= 1
                                    if (newImage[newI + 1][newJ + 1].notEquals(-1) || newJ < 1)
                                        break
                                }
                            }
                            newImage[newI][newJ] = image[i][j]
                        }
                    } else {
                        // Если следующее значение - пустой пиксель
                        if (newJ + 1 < newCols && newImage[newI][newJ + 1].equals(-1)) {
                            if (i > 0 && j > 0) {
                                while (newImage[newI][newJ + 1].equals(-1)) {
                                    newJ++
                                    if (newImage[newI - 1][newJ - 1].notEquals(-1) || newJ > newCols - 1)
                                        break
                                }
                            }
                            newImage[newI][newJ] = image[i][j]
                        }
                    }
                }
            }
        }

        return newImage
    }
}