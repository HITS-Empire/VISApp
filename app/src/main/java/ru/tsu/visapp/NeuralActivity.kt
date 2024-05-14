package ru.tsu.visapp

import java.io.File
import android.os.Bundle
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.core.Mat
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

        assets.open("ssd.onnx")

        val path = getPath("ssd.onnx")
        net = Dnn.readNetFromONNX(path)
    }

    private val findFaces = fun () {
        // Получить imageView
        val imageView: ImageView = findViewById(R.id.neuralImageView)

        // Получить картинку
        val savedImageUri = imageEditor.getSavedImageUri(this, null)
        bitmap = imageEditor.createBitmapByUri(savedImageUri)

        // Логика работы нейросети
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

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