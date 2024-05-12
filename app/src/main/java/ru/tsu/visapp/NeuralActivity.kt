package ru.tsu.visapp

import android.os.Bundle
import android.graphics.Bitmap
import android.widget.ImageView
import org.opencv.android.OpenCVLoader
import ru.tsu.visapp.utils.ImageEditor
import ru.tsu.visapp.utils.ImageGetter

/*
 * Экран для нейронной сети
 */

class NeuralActivity: ChildActivity() {
    private val imageEditor = ImageEditor() // Редактор изображений
    private lateinit var bitmap: Bitmap // Картинка для редактирования

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView(R.layout.activity_neural)

        ImageGetter(this, null, findFaces)

        OpenCVLoader.initDebug()
    }

    private val findFaces = fun () {
        setImage()

        // Логика работы нейросети
    }

    private val setImage = fun() {
        // Получить imageView
        val imageView: ImageView = findViewById(R.id.neuralImageView)

        // Получить картинку и установить её
        val savedImageUri = imageEditor.getSavedImageUri(this, null)
        bitmap = imageEditor.createBitmapByUri(savedImageUri)
        imageView.setImageBitmap(bitmap)
    }
}