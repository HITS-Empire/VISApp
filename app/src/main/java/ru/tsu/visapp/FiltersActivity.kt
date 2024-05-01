package ru.tsu.visapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.SeekBar
import android.graphics.Bitmap
import android.widget.TextView
import android.widget.ImageView
import android.widget.FrameLayout
import ru.tsu.visapp.utils.ImageEditor
import androidx.appcompat.content.res.AppCompatResources

/*
 * Экран для фильтров
 */

class FiltersActivity: ChildActivity() {
    private lateinit var title: String // Название изображения
    private lateinit var imageEditor: ImageEditor // Редактор изображений
    private lateinit var bitmap: Bitmap // Картинка для редактирования
    private lateinit var pixels: IntArray // Массив пикселей

    private lateinit var currentImage: ImageView // Картинка текущего фильтра
    private lateinit var seekBar: SeekBar // Ползунок

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView(R.layout.activity_filters)

        val imageView: ImageView = findViewById(R.id.filtersImageView)

        title = System.currentTimeMillis().toString()
        imageEditor = ImageEditor(contentResolver)
        seekBar = findViewById(R.id.filtersSeekBar)

        // Получить картинку и установить её
        val savedImageUri = imageEditor.getSavedImageUri(this, null)
        bitmap = imageEditor.createBitmapByUri(savedImageUri)
        imageView.setImageBitmap(bitmap)

        // Получить пиксели изображения
        pixels = imageEditor.getPixelsFromBitmap(bitmap)

        // Example: Редактирование пикселей
        // pixels.forEachIndexed { index, _ ->
        //     pixels[index] = Color.BLUE
        // }
        // editor.setPixelsToBitmap(bitmap, pixels)

        // Установить события ползунка
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)

        // Кнопка "Сохранить"
        val saveButton: TextView = findViewById(R.id.saveButton)

        // События кликов по кнопке
        saveButton.setOnClickListener {
            var text = "Изображение успешно сохранено"

            try {
                imageEditor.saveImageToGallery(bitmap, title)
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

    // События ползунка
    private val onSeekBarChangeListener = object: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            // Прогресс от 0 до 100
            println("Progress: $progress")
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            println("start tracking touch")
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            println("stop tracking touch")
        }
    }

    // Сменить фильтр, ориентируясь на его картинку
    private fun changeFilter(image: ImageView) {
        if (::currentImage.isInitialized) {
            if (image == currentImage) return

            currentImage.background = null

            when (currentImage.id) {
                R.id.rotateImage -> {}
                R.id.scalingImage -> {}
                R.id.retouchImage -> {}
                R.id.definitionImage -> {}
                R.id.affinisImage -> {
                    seekBar.visibility = View.VISIBLE
                }
            }
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
            R.id.affinisImage -> {
                seekBar.visibility = View.GONE
            }
        }
        currentImage = image
    }
}