package ru.tsu.visapp.filters

import kotlin.math.abs
import ru.tsu.visapp.utils.ImageEditor.Pixel

class UnsharpMask {
    private fun gaussianBlur(image: Array<Array<Pixel>>, radius: Int): Array<Array<Pixel>> {
        val result = Array(image.size) { Array(image[0].size) { Pixel(0, 0, 0) } }

        // Основной цикл по изображению
        for (i in image.indices) {
            for (j in image[i].indices) {
                var totalRed = 0
                var totalGreen = 0
                var totalBlue = 0
                var count = 0

                // Цикл по окрестности пикселя с радиусом размытия
                for (x in -radius..radius) {
                    for (y in -radius..radius) {
                        val newX = i + x
                        val newY = j + y

                        if (newX in image.indices && newY in image[newX].indices) {
                            val neighbor = image[newX][newY]
                            totalRed += neighbor.red
                            totalGreen += neighbor.green
                            totalBlue += neighbor.blue
                            count++
                        }
                    }
                }

                val avgRed = totalRed / count
                val avgGreen = totalGreen / count
                val avgBlue = totalBlue / count

                result[i][j] = Pixel(avgRed, avgGreen, avgBlue)
            }
        }

        return result
    }

    private fun difference(image1: Array<Array<Pixel>>, image2: Array<Array<Pixel>>): Array<Array<Pixel>> {
        val result = Array(image1.size) { Array(image1[0].size) { Pixel(0, 0, 0) } }

        for (i in image1.indices) {
            for (j in image1[i].indices) {
                result[i][j] = image1[i][j] - image2[i][j]
            }
        }

        return result
    }

    private fun pixelAbs(pixel : Pixel) : Int {
        return abs(pixel.red) + abs(pixel.green) + abs(pixel.blue)
    }

    fun usm(image: Array<Array<Pixel>>, radius: Int,
            amountPercent: Int, threshold: Int) : Array<Array<Pixel>> {
        // Создание копии изображения с применением размытия по Гауссу
        val blurred = gaussianBlur(image, radius)

        // Вычитание заблюренного изображения из оригинального попиксельно
        val unsharpMask = difference(image, blurred)

        val unsharpImage = image.map { it.clone() }.toTypedArray()

        // Упрощенный алгоритм
        for (row in image.indices) {
            for (col in image.indices) {
                if (pixelAbs(unsharpImage[row][col]) > threshold)
                unsharpImage[row][col] = image[row][col] + (unsharpMask[row][col] *
                                                            amountPercent.toDouble())
            }
        }

        return unsharpImage
    }
}