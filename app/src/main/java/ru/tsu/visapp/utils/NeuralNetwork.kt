package ru.tsu.visapp.utils

import java.io.File
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.core.Scalar
import android.graphics.Bitmap
import org.opencv.android.Utils
import java.io.FileOutputStream
import org.opencv.imgproc.Imgproc
import java.io.BufferedInputStream
import org.opencv.android.OpenCVLoader
import androidx.appcompat.app.AppCompatActivity

/*
 * Нейронная сеть для распознавания лиц
 */

class NeuralNetwork(
    private val activity: AppCompatActivity
) {
    private val net: Net // Собственно нейросеть

    // Получить прямоугольники с лицами
    fun getBoundingBoxes(
        bitmap: Bitmap,
        isCorrect: Boolean = false
    ): ArrayList<ArrayList<Int>> {
        val result = ArrayList<ArrayList<Int>>()

        // Преобразование изображения в формат mat
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // Приведение изображения к верному размеру и формату
        val frame = Mat()
        Imgproc.cvtColor(mat, frame, Imgproc.COLOR_RGBA2RGB)

        Imgproc.resize(frame, frame, Size(300.0, 300.0))

        // Получение blob нового размера и с вычитанием среднего
        val blob = Dnn.blobFromImage(
            frame,
            1.0,
            Size(300.0, 300.0),
            Scalar(104.0, 177.0, 123.0),
            true,
            false
        )

        // Установка входных данных в модель
        net.setInput(blob)

        // Получение и преобразование детектированных объектов
        var detections = net.forward()
        detections = detections.reshape(1, detections.total().toInt() / 7)

        // Размеры изображения
        val cols: Int = frame.cols()
        val rows: Int = frame.rows()

        // Порог уверенности модели в предсказании
        val threshold = 0.4

        // Отрисовка bounding boxes на изображении
        for (i in 0 until detections.rows()) {
            val confidence = detections.get(i, 2)[0]
            if (confidence > threshold) {
                val (left, top, right, bottom) = scaleBoundingBox(
                    bitmap.width,
                    bitmap.height,
                    (detections.get(i, 3)[0] * cols).toInt(),
                    (detections.get(i, 4)[0] * rows).toInt(),
                    (detections.get(i, 5)[0] * cols).toInt(),
                    (detections.get(i, 6)[0] * rows).toInt()
                )

                val rectangleCoordinates = arrayListOf(
                    left,
                    top,
                    right,
                    bottom
                )

                result.add(rectangleCoordinates)
            }
        }

        // Обработка случая, когда объекты не найдены
        if (isCorrect && result.size == 0) {
            result.add(
                arrayListOf(
                    0,
                    0,
                    bitmap.width - 1,
                    bitmap.height - 1
                )
            )
        }

        return result
    }

    // Привести границы из Mat к Bitmap
    private fun scaleBoundingBox(
        width: Int,
        height: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): Array<Int> {
        return arrayOf(
            left * width / 300,
            top * height / 300,
            right * width / 300,
            bottom * height / 300
        )
    }

    // Получить путь к модели
    private fun getPath(name: String): String {
        val inputStream = BufferedInputStream(activity.assets.open(name))
        val data = ByteArray(inputStream.available()) { 0 }
        inputStream.apply {
            read(data)
            close()
        }

        val file = File(activity.filesDir, name)
        val outputStream = FileOutputStream(file)
        outputStream.apply {
            write(data)
            close()
        }

        return file.absolutePath
    }

    init {
        OpenCVLoader.initDebug()

        val pathProto = getPath("deploy.prototxt")
        val pathCaffe = getPath("ssd.caffemodel")

        net = Dnn.readNetFromCaffe(pathProto, pathCaffe)
    }
}