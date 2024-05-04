package ru.tsu.visapp.filters

import kotlin.math.abs

class UnsharpMask {
    data class Pixel(val red: Int, val green: Int, val blue: Int) {
        operator fun minus(other: Pixel) : Pixel {
            return Pixel(
                this.red - other.red,
                this.green - other.green,
                this.blue - other.blue
            )
        }

        operator fun plus(other: Pixel) : Pixel {
            return Pixel(
                this.red + other.red,
                this.green + other.green,
                this.blue + other.blue
            )
        }

        operator fun times(percent: Double) : Pixel {
            return Pixel(
                (red * percent).toInt(),
                (green * percent).toInt(),
                (blue * percent).toInt()
            )
        }
    }

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

    private fun increaseContrastColor(color : Int, amountPercent : Int) : Int {
        return (((color - 255 / 2) * (100 + amountPercent) / 100) + 255 / 2).coerceIn(0, 255)
    }

    private fun increaseContrast(image: Array<Array<Pixel>>, amountPercent: Int): Array<Array<Pixel>> {
        val result = Array(image.size) { Array(image[0].size) { Pixel(0, 0, 0) } }

        for (i in image.indices) {
            for (j in image[i].indices) {
                val pixel = image[i][j]

                val increasedRed = increaseContrastColor(pixel.red, amountPercent)
                val increasedGreen = increaseContrastColor(pixel.green, amountPercent)
                val increasedBlue = increaseContrastColor(pixel.blue, amountPercent)

                result[i][j] = Pixel(increasedRed, increasedGreen, increasedBlue)
            }
        }

        return result
    }

    // Преобразование цвета пикселя в яркость
    private fun luminancePercent(pixel: Pixel): Double {
        val luminance = (0.2126 * pixel.red + 0.7152 * pixel.green + 0.0722 * pixel.blue).toInt()

        // Нормализация яркости к процентам от максимальной яркости
        return (luminance.toDouble() / 255.0 * 100.0)
    }

    fun pixelAbs(pixel : Pixel) : Int {
        return abs(pixel.red) + abs(pixel.green) + abs(pixel.blue)
    }

    fun usm(image: Array<Array<Pixel>>, radius: Int,
            amountPercent: Int, threshold: Int) : Array<Array<Pixel>> {
        // Копирование изображения
        val resultArray = image.map { it.clone() }.toTypedArray()

        // Создание копии изображения с применением размытия по Гауссу
        val blurred = gaussianBlur(image, radius)

        // Вычитание заблюренного изображения из оригинального попиксельно
        val unsharpMask = difference(image, blurred)

        // Создание копии изображения с повышенным контрастом
        val highContrast = increaseContrast(image, amountPercent)

        // Основной алгоритм
        for (row in image.indices) {
            for (col in image[row].indices) {
                val originalPixel = image[row][col]
                val contrastPixel = highContrast[row][col]

                val difference = contrastPixel - originalPixel
                val percent = luminancePercent(unsharpMask[row][col])

                val delta = difference * percent

                // Сравнение с пороговым значением
                if (pixelAbs(delta) > threshold) {
                    resultArray[row][col] += delta
                }
            }
        }

        return resultArray
    }
}