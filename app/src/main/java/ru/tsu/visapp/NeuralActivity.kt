package ru.tsu.visapp

import android.os.Bundle
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import android.graphics.Bitmap
import android.widget.ImageView
import org.opencv.android.Utils
import org.opencv.imgproc.Imgproc
import ru.tsu.visapp.utils.ImageEditor
import ru.tsu.visapp.utils.ImageGetter
import ru.tsu.visapp.utils.NeuralNetwork

/*
 * Экран для нейронной сети
 */

class NeuralActivity : ChildActivity() {
    private val imageEditor = ImageEditor() // Редактор изображений
    private val color = Scalar(43.0, 203.0, 17.0) // Цвет прямоугольника

    private lateinit var neuralNetwork: NeuralNetwork // Нейронная сеть
    private lateinit var imageView: ImageView // Картинка
    private lateinit var bitmap: Bitmap // Битмап

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView(R.layout.activity_neural)

        ImageGetter(this, null, findFaces)

        imageEditor.contentResolver = contentResolver
        neuralNetwork = NeuralNetwork(this)
        imageView = findViewById(R.id.neuralImageView)
    }

    private val findFaces = fun() {
        // Получить картинку
        val savedImageUri = imageEditor.getSavedImageUri(this, null)
        bitmap = imageEditor.createBitmapByUri(savedImageUri)

        // Преобразование изображения в формат mat
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val boxes = neuralNetwork.getBoundingBoxes(bitmap)

        for (i in boxes.indices) {
            val (left, top, right, bottom) = boxes[i]

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
}