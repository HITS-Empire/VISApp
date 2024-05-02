package ru.tsu.visapp.filters

import kotlin.math.cos
import kotlin.math.sin

/*
 * Реализация фильтра поворота изображения на любой градус
 */

class ImageRotation(pixels : Array<Array<Pixel>>, angle : Int,
                    rows : Int, cols : Int) {
    data class Pixel(val red: Int, val green: Int, val blue: Int)

    private val pixels: Array<Array<Pixel>> by lazy { pixels }

    private val centerRow = rows / 2
    private val centerCol = cols / 2

    // Вершины нового изображения
    private var maxRow: Double = 0.0
    private var maxCol: Double = 0.0
    private var minRow: Double = 0.0
    private var minCol: Double = 0.0

    private var pixelsResult : Array<Array<Pixel>>

    private fun newPosition(isRow : Boolean, x : Int, y : Int, radians : Double) : Double {
        return if (isRow) {
            centerRow + (x - centerRow) * cos(radians) - (y - centerCol) * sin(radians)
        } else {
            centerCol + (x - centerCol) * cos(radians) - (y - centerRow) * sin(radians)
        }
    }

    private fun toRadians(angle : Int) : Double {
        return angle * (kotlin.math.PI / 180)
    }

    private fun getNewCoordinates(angle : Int, rows : Int, cols : Int) : Array<Double> {
        // Координаты вершин нового изображения
        val radians = toRadians(angle)

        when (angle) {
            in 0..90 -> {
                maxRow = newPosition(true, rows, 0, radians)
                maxCol = newPosition(false, cols, rows, radians)
                minRow = newPosition(true, 0, cols, radians)
                minCol = newPosition(false, 0, 0, radians)
            }
            in 91..180 -> {
                maxRow = newPosition(true, 0, 0, radians)
                maxCol = newPosition(false, 0, rows, radians)
                minRow = newPosition(true, rows, cols, radians)
                minCol = newPosition(false, cols, 0, radians)
            }
            in 181..270 -> {
                maxRow = newPosition(true, 0, cols, radians)
                maxCol = newPosition(false, 0, 0, radians)
                minRow = newPosition(true, rows, 0, radians)
                minCol = newPosition(false, cols, rows, radians)
            }
            else -> {
                maxRow = newPosition(true, rows, cols, radians)
                maxCol = newPosition(false, cols, 0, radians)
                minRow = newPosition(true, 0, 0, radians)
                minCol = newPosition(false, 0, rows, radians)
            }
        }

        return arrayOf(maxRow, maxCol, minRow, minCol)
    }

    private fun notCheckedPixel(pixel : Pixel) : Boolean {
        return pixel.red == -1 && pixel.green == -1 && pixel.blue == -1
    }

    private fun checkedPixel(pixel : Pixel) : Boolean {
        return pixel.red != -1 && pixel.green != -1 && pixel.blue != -1
    }

    private fun rotate(rows : Int, cols : Int, newRows : Int, newCols : Int,
                       defaultRows : Int, defaultColumns : Int, angle : Int) : Array<Array<Pixel>> {
        val pixelNew = Array(newRows) { Array(newCols) { Pixel(-1, -1, -1) } }
        val radians = toRadians(angle)

        for (i in 0 until defaultRows) {
            for (j in 0 until defaultColumns) {
                if (i < rows && j < cols) {
                    val x = newPosition(true, i, j, radians)
                    val y = newPosition(false, j, i, radians)

                    val newI = x.toInt() - minRow.toInt()
                    val newJ = y.toInt() - minCol.toInt()

                    pixelNew[newI][newJ] = pixels[i][j]

                    if (angle in 0..180) {
                        if (newJ > 0 && notCheckedPixel(pixelNew[newI][newJ - 1])) {
                            var tempJ: Int = newJ
                            if (i > 0 && j > 0) {
                                while (notCheckedPixel(pixelNew[newI][tempJ - 1])) {
                                    tempJ--
                                    if (checkedPixel(pixelNew[newI + 1][tempJ + 1])) {
                                        break
                                    }
                                }
                            }
                            pixelNew[newI][tempJ] = pixels[i][j]
                        }
                    } else {
                        if (newJ + 1 < defaultColumns &&
                            notCheckedPixel(pixelNew[newI][newJ + 1])) {
                            var tempJ : Int = newJ
                            if (i > 0 && j > 0) {
                                while (notCheckedPixel(pixelNew[newI][tempJ + 1])) {
                                    tempJ++
                                    if (checkedPixel(pixelNew[newI - 1][tempJ - 1])) {
                                        break
                                    }
                                }
                            }
                            pixelNew[newI][tempJ] = pixels[i][j]
                        }
                    }
                }
            }
        }

        return pixelNew
    }

    fun getImage() : Array<Array<Pixel>> {
        return pixelsResult
    }

    init {
        val (maxRow, maxCol, minRow, minCol) = getNewCoordinates(angle, rows, cols)

        // Размер матрицы для нового изображения
        val newRows : Int = (maxRow - minRow + 1).toInt()
        val newCols : Int = (maxCol - minCol + 1).toInt()

        // Дефолтные значения если угол кратен 90 градусам
        val defaultRows = if (newRows < rows) rows else newRows
        val defaultColumns = if (newCols < cols) cols else newCols

        pixelsResult = rotate(rows, cols, newRows,
                              newCols, defaultRows, defaultColumns, angle)

    }
}