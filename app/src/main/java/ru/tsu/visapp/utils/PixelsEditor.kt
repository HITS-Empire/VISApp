package ru.tsu.visapp.utils

/*
 * Пиксельный редактор
 */

class PixelsEditor(initPixels: IntArray, initWidth: Int, initHeight: Int) {
    private val pixels = initPixels
    private val width = initWidth
    private val height = initHeight

    private fun isExists(row: Int, column: Int): Boolean {
        return row in 0 ..< width && column in 0 ..< height
    }

    fun getPixel(row: Int, column: Int): Int? {
        if (isExists(row, column)) {
            return pixels[column * width + row]
        }
        return null
    }

    fun setPixel(row: Int, column: Int, color: Int?) {
        if (isExists(row, column) && color != null) {
            pixels[column * width + row] = color
        }
    }
}