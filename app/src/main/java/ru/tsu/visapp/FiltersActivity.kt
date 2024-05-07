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
import ru.tsu.visapp.utils.ImageEditor.Pixel
import ru.tsu.visapp.filters.UnsharpMask
import ru.tsu.visapp.filters.ImageRotation

/*
 * Экран для фильтров
 */

class FiltersActivity: ChildActivity() {
    private lateinit var imageView: ImageView
    private lateinit var title: String // Название изображения
    private lateinit var imageEditor: ImageEditor // Редактор изображений
    private lateinit var bitmap: Bitmap // Картинка для редактирования
    private lateinit var pixels: IntArray // Массив пикселей
    private lateinit var pixels2d: Array<Array<Pixel>> // Двумерный массив пикселей

    private lateinit var unsharpMask: UnsharpMask // Нерезкое маскирование
    private lateinit var imageRotation: ImageRotation // Поворот изображения

    // Изображения с наложенными ранее фильтрами
    private lateinit var unsharpMaskImage : Array<Array<Pixel>>
    private lateinit var rotatedImage : Array<Array<Pixel>>

    private lateinit var currentImage: ImageView // Картинка текущего фильтра
    private lateinit var seekBar: SeekBar // Ползунок

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView(R.layout.activity_filters)

        imageView = findViewById(R.id.filtersImageView)

        title = System.currentTimeMillis().toString()
        imageEditor = ImageEditor(contentResolver)
        seekBar = findViewById(R.id.filtersSeekBar)

        unsharpMask = UnsharpMask()
        imageRotation = ImageRotation()

        unsharpMaskImage = emptyArray()
        rotatedImage = emptyArray()

        // Получить картинку и установить её
        val savedImageUri = imageEditor.getSavedImageUri(this, null)
        bitmap = imageEditor.createBitmapByUri(savedImageUri)
        imageView.setImageBitmap(bitmap)

        // Получить пиксели изображения
        pixels = imageEditor.getPixelsFromBitmap(bitmap)

        // Получить двумерный массив пикселей
        pixels2d = imageEditor.bitmapToPixels(bitmap)

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
            R.id.rotateImage -> {
                // Если изображение еще не было получено с использованием фильтра ранее
                if (rotatedImage.contentEquals(emptyArray())) {
                    val result: Array<Array<Pixel>> = imageRotation.rotate(pixels2d, 15)

                    val newBitmap = imageEditor.pixelsToBitmap(result)
                    imageView.setImageBitmap(newBitmap)
                    rotatedImage = result
                }
                // Если изображение уже обрабатывалось этим фильтром
                else {
                    imageView.setImageBitmap(imageEditor.pixelsToBitmap(rotatedImage))
                }
            }
            R.id.scalingImage -> {}
            R.id.retouchImage -> {}
            R.id.definitionImage -> {
                // Если изображение еще не было получено с использованием фильтра ранее
                if (unsharpMaskImage.contentEquals(emptyArray())) {
                    val result : Array<Array<Pixel>> = unsharpMask.usm(pixels2d, 10, 30, 60)
                    val newBitmap = imageEditor.pixelsToBitmap(result)
                    imageView.setImageBitmap(newBitmap)
                    unsharpMaskImage = result
                }
                // Если изображение уже обрабатывалось этим фильтром
                else {
                    imageView.setImageBitmap(imageEditor.pixelsToBitmap(unsharpMaskImage))
                }
            }
            R.id.affinisImage -> {
                seekBar.visibility = View.GONE
            }
        }

        currentImage = image
    }
}