package ru.tsu.visapp

import java.io.File
import android.os.Bundle
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.core.Scalar
import android.graphics.Bitmap
import android.widget.ImageView
import org.opencv.android.Utils
import java.io.FileOutputStream
import java.io.BufferedInputStream
import org.opencv.android.OpenCVLoader
import ru.tsu.visapp.utils.ImageEditor
import ru.tsu.visapp.utils.ImageGetter

/*
 * Экран для нейронной сети
 */

class NeuralActivity: ChildActivity() {
    private val imageEditor = ImageEditor() // Редактор изображений
    private lateinit var bitmap: Bitmap // Картинка для редактирования
    private lateinit var net: Net

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

        // Получение blob нового размера и с вычитанием среднего
        val blob = Dnn.blobFromImage(
            mat,
            1.0,
            Size(300.0, 300.0),
            Scalar(104.0, 177.0, 123.0)
        )

        net.setInput(blob)

        val outputs = net.forward()

        println(outputs.size())

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