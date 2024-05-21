package ru.tsu.visapp

import java.io.File
import android.os.Bundle
import org.opencv.core.*
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import android.graphics.Bitmap
import android.widget.ImageView
import org.opencv.android.Utils
import java.io.FileOutputStream
import org.opencv.imgproc.Imgproc
import java.io.BufferedInputStream
import ru.tsu.visapp.utils.ImageEditor
import ru.tsu.visapp.utils.ImageGetter
import org.opencv.android.OpenCVLoader

/*
 * Экран для нейронной сети
 */

class NeuralActivity: ChildActivity() {
    private val imageEditor = ImageEditor() // Редактор изображений
    private lateinit var bitmap: Bitmap // Картинка для редактирования
    private lateinit var net: Net // Нейронная сеть
    private val color = Scalar(43.0, 203.0, 17.0) // Цвет прямоугольника

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OpenCVLoader.initDebug();

        initializeView(R.layout.activity_neural)
        imageEditor.contentResolver = contentResolver

        ImageGetter(this, null, findFaces)

        val pathProto = getPath("deploy.prototxt")
        val pathCaffe = getPath("ssd.caffemodel")

        net = Dnn.readNetFromCaffe(pathProto, pathCaffe)
    }

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

    private fun getBoundingBoxes(bitmap: Bitmap): ArrayList<ArrayList<Int>> {
        OpenCVLoader.initDebug();

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

        return result
    }

    private val findFaces = fun () {
        // Получить imageView
        val imageView: ImageView = findViewById(R.id.neuralImageView)

        // Получить картинку
        val savedImageUri = imageEditor.getSavedImageUri(this, null)
        bitmap = imageEditor.createBitmapByUri(savedImageUri)

        // Преобразование изображения в формат mat
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val boxes = getBoundingBoxes(bitmap)

        for (i in boxes.indices) {
            val (left,
            top,
            right,
            bottom) = boxes[i]

            println(left)
            println(top)
            println(right)
            println(bottom)

            // Отрисовка прямоугольника вокруг обнаруженного объекта
            Imgproc.rectangle(
                mat,
                Point(left.toDouble(), top.toDouble()),
                Point(right.toDouble(), bottom.toDouble()),
                color,
                3
            )
        }

        // Перевести mat в bitmap
        bitmap = Bitmap.createBitmap(
            mat.cols(),
            mat.rows(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(mat, bitmap)

        // Установить картинку
        imageView.setImageBitmap(bitmap)
    }

    // Получить путь к файлу из ресурсов
    private fun getPath(name: String): String {
        val inputStream = BufferedInputStream(assets.open(name))
        val data = ByteArray(inputStream.available()) { 0 }
        inputStream.apply {
            read(data)
            close()
        }

        val file = File(filesDir, name)
        val outputStream = FileOutputStream(file)
        outputStream.apply {
            write(data)
            close()
        }

        return file.absolutePath
    }
}