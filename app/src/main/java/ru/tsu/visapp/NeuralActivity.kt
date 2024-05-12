package ru.tsu.visapp

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.dnn.*
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

        val path = "C:/Users/Legion/Documents/GitHub/VISApp/app/src/main/resources/neuralNetwork/ssd.onnx"
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
}