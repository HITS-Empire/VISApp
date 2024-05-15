package ru.tsu.visapp

import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.widget.ImageView
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.imgproc.Imgproc
import ru.tsu.visapp.utils.ImageEditor
import ru.tsu.visapp.utils.ImageGetter
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream

/*
 * Экран для нейронной сети
 */

class NeuralActivity: ChildActivity() {
    private val imageEditor = ImageEditor() // Редактор изображений
    private lateinit var bitmap: Bitmap // Картинка для редактирования
    private lateinit var result: Bitmap // Результирующее изображение
    private lateinit var net: Net // Нейронная сеть

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

    private val findFaces = fun () {
        // Получить imageView
        val imageView: ImageView = findViewById(R.id.neuralImageView)

        // Получить картинку
        val savedImageUri = imageEditor.getSavedImageUri(this, null)
        bitmap = imageEditor.createBitmapByUri(savedImageUri)

        // Преобразование изображения в формат mat
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // Приведение изображения к верному размеру и формату
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB)
        val frame = Mat()
        Imgproc.resize(mat, frame, Size(300.0, 300.0))

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
        val threshold = 0.25

        // Отрисовка bounding boxes на изображении
        for (i in 0 until detections.rows()) {
            val confidence = detections.get(i, 2)[0]
            if (confidence > threshold) {
                val left = (detections.get(i, 3)[0] * cols).toInt()
                val top = (detections.get(i, 4)[0] * rows).toInt()
                val right = (detections.get(i, 5)[0] * cols).toInt()
                val bottom = (detections.get(i, 6)[0] * rows).toInt()

                // Отрисовка прямоугольника вокруг обнаруженного объекта
                Imgproc.rectangle(
                    frame,
                    Point(left.toDouble(), top.toDouble()),
                    Point(right.toDouble(), bottom.toDouble()),
                    Scalar(43.0, 203.0, 17.0)
                )
            }
        }

        // Перевести frame в bitmap
        result = Bitmap.createBitmap(
            frame.cols(),
            frame.rows(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(frame, result)

        // Установить картинку
        imageView.setImageBitmap(result)
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