package ru.tsu.visapp

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import android.content.Context
import android.graphics.Bitmap
import android.widget.TextView
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.FrameLayout
import ru.tsu.visapp.utils.ImageEditor
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources

class FiltersActivity: AppCompatActivity() {
    private val title = System.currentTimeMillis().toString() // Название изображения

    private lateinit var editor: ImageEditor // Редактор изображений
    private lateinit var bitmap: Bitmap // Картинка для редактирования
    private lateinit var pixels: IntArray // Массив пикселей

    private lateinit var currentImage: ImageView // Картинка текущего фильтра

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)

        val imageView: ImageView = findViewById(R.id.filtersImageView)

        // Получить локальное хранилище
        val sharedPreferences = this.getSharedPreferences(
            "ru.tsu.visapp",
            Context.MODE_PRIVATE
        )

        // Получить редактор изображений
        editor = ImageEditor(contentResolver)

        // Получить картинку и установить её
        val selectedUri = sharedPreferences.getString("selected_uri", "")
        bitmap = editor.createBitmapByURI(Uri.parse(selectedUri))
        imageView.setImageBitmap(bitmap)

        // Получить пиксели изображения
        pixels = editor.getPixelsFromBitmap(bitmap)

        // Example: Редактирование пикселей
        // pixels.forEachIndexed { index, _ ->
        //     pixels[index] = Color.BLUE
        // }
        // editor.setPixelsToBitmap(bitmap, pixels)

        val backButton: ImageButton = findViewById(R.id.backButton) // Кнопка "Назад"
        val saveButton: TextView = findViewById(R.id.saveButton) // Кнопка "Сохранить"

        // События кликов по кнопкам
        backButton.setOnClickListener {
            goToMainActivity()
        }
        saveButton.setOnClickListener {
            var text = "Изображение успешно сохранено"

            try {
                editor.saveImageToGallery(bitmap, title)
            } catch (error: Error) {
                text = "Не удалось сохранить изображение"
            }

            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }

        // Окошки фильтров
        val framesWithFilters: Array<FrameLayout> = arrayOf(
            findViewById(R.id.rotateFrame),
            findViewById(R.id.scalingFrame),
            findViewById(R.id.retouchFrame),
            findViewById(R.id.definitionFrame),
            findViewById(R.id.affinisFrame)
        )

        // Иконки фильтров (для подсветки)
        val imagesWithFilters: Array<ImageView> = arrayOf(
            findViewById(R.id.rotateImage),
            findViewById(R.id.scalingImage),
            findViewById(R.id.retouchImage),
            findViewById(R.id.definitionImage),
            findViewById(R.id.affinisImage)
        )

        changeFilter(imagesWithFilters[0])

        // События кликов по фреймам
        framesWithFilters.forEachIndexed { index, frame ->
            frame.setOnClickListener {
                changeFilter(imagesWithFilters[index])
            }
        }
    }

    // Сменить фильтр, ориентируясь на его картинку
    private fun changeFilter(image: ImageView) {
        if (::currentImage.isInitialized) {
            currentImage.background = null
        }
        image.background = AppCompatResources.getDrawable(
            this,
            R.drawable.filters_background
        )

        when (image.id) {
            R.id.rotateImage -> {}
            R.id.scalingImage -> {}
            R.id.retouchImage -> {}
            R.id.definitionImage -> {}
            R.id.affinisImage -> {}
        }
        currentImage = image
    }

    // Перейти на главный экран
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}