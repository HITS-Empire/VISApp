package ru.tsu.visapp

import android.os.Bundle
import android.widget.Toast
import java.lang.Integer.min
import java.lang.Integer.max
import android.widget.SeekBar
import android.graphics.Bitmap
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageView
import android.widget.FrameLayout
import ru.tsu.visapp.utils.ImageEditor
import ru.tsu.visapp.filters.UnsharpMask
import ru.tsu.visapp.filters.ImageRotation
import ru.tsu.visapp.utils.filtersSeekBar.*
import androidx.core.widget.addTextChangedListener
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout

/*
 * Экран для фильтров
 */

class FiltersActivity: ChildActivity() {
    private lateinit var imageView: ImageView
    private lateinit var title: String // Название изображения
    private lateinit var bitmap: Bitmap // Картинка для редактирования
    private lateinit var pixels: IntArray // Массив пикселей
    private var width = 0 // Ширина картинки
    private var height = 0 // Высота картинки

    private lateinit var currentImage: ImageView // Картинка текущего фильтра

    private lateinit var seekBarLayouts: Array<ConstraintLayout> // Контейнеры для ползунков
    private lateinit var seekBarTitles: Array<TextView> // Названия ползунков
    private lateinit var seekBars: Array<SeekBar> // Сами ползунки
    private lateinit var seekBarEditors: Array<EditText> // Отображения текущего значения
    private lateinit var seekBarUnits: Array<TextView> // Единицы измерения ползунка

    private lateinit var filtersSeekBarInstructions: Array<Instruction> // Описание для ползунков
    private lateinit var currentInstruction: Instruction // Текущая инструкция

    private val imageEditor = ImageEditor() // Редактор изображений
    private val unsharpMask = UnsharpMask() // Нерезкое маскирование
    private val imageRotation = ImageRotation() // Поворот изображения

    private var filtersIsAvailable = false // Можно ли запускать фильтры
    private var filterIsActive = false // Запущен ли сейчас какой-то фильтр

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView(R.layout.activity_filters)

        imageView = findViewById(R.id.filtersImageView)

        title = System.currentTimeMillis().toString()
        imageEditor.contentResolver = contentResolver

        seekBarLayouts = arrayOf(
            findViewById(R.id.firstSeekBarLayout),
            findViewById(R.id.secondSeekBarLayout),
            findViewById(R.id.thirdSeekBarLayout)
        )
        seekBarTitles = arrayOf(
            findViewById(R.id.firstSeekBarTitle),
            findViewById(R.id.secondSeekBarTitle),
            findViewById(R.id.thirdSeekBarTitle)
        )
        seekBars = arrayOf(
            findViewById(R.id.firstSeekBar),
            findViewById(R.id.secondSeekBar),
            findViewById(R.id.thirdSeekBar)
        )
        seekBarEditors = arrayOf(
            findViewById(R.id.firstSeekBarEditor),
            findViewById(R.id.secondSeekBarEditor),
            findViewById(R.id.thirdSeekBarEditor)
        )
        seekBarUnits = arrayOf(
            findViewById(R.id.firstSeekBarUnit),
            findViewById(R.id.secondSeekBarUnit),
            findViewById(R.id.thirdSeekBarUnit)
        )

        filtersSeekBarInstructions = arrayOf(
            Instruction(
                R.id.rotateImage,
                arrayOf(
                    Item(),
                    Item(0, 360, "Угол", "°"),
                    Item()
                )
            ),
            Instruction(
                R.id.scalingImage,
                arrayOf(
                    Item(),
                    Item(50, 100, "Масштаб", "%"),
                    Item()
                )
            ),
            Instruction(
                R.id.retouchImage,
                arrayOf(
                    Item(),
                    Item(10, 20, "Размер"),
                    Item()
                )
            ),
            Instruction(
                R.id.definitionImage,
                arrayOf(
                    Item(0, 100, "Эффект", "%"),
                    Item(0, 100, "Радиус"),
                    Item(0, 100, "Изогелия")
                )
            ),
            Instruction(R.id.affinisImage, arrayOf(Item(), Item(), Item()))
        )

        // Получить картинку и установить её
        val savedImageUri = imageEditor.getSavedImageUri(this, null)
        bitmap = imageEditor.createBitmapByUri(savedImageUri)
        imageView.setImageBitmap(bitmap)
        updatePixelsInfo()

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

        // Установить события ползунка
        seekBars.forEach { seekBar ->
            seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        }
        seekBarEditors.forEachIndexed { index, seekBarEditor ->
            seekBarEditor.addTextChangedListener { editable ->
                val item = currentInstruction.items[index]

                val text = editable.toString()
                val progress = min(
                    item.max,
                    max(
                        0,
                        if (text == "") 0 else text.toInt()
                    )
                )
                val trim = progress.toString()

                if (text == trim) {
                    item.progress = progress
                    seekBars[index].progress = progress

                    startFilter()
                } else {
                    seekBarEditor.setText(trim)
                }
            }
        }

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
    }

    // Получить пиксели изображения
    fun updatePixelsInfo() {
        pixels = imageEditor.getPixelsFromBitmap(bitmap)
        width = bitmap.width
        height = bitmap.height
    }

    // Запустить функцию фильтра
    fun startFilter() {
        if (!filtersIsAvailable || filterIsActive) return

        filterIsActive = true

        when (currentImage.id) {
            R.id.rotateImage -> {
                val angle = currentInstruction.items[1].progress

                bitmap = imageRotation.rotate(pixels, width, height, angle)
                imageView.setImageBitmap(bitmap)
            }
            R.id.scalingImage -> {}
            R.id.retouchImage -> {}
            R.id.definitionImage -> {
                val percent = currentInstruction.items[0].progress
                val radius = currentInstruction.items[1].progress
                val threshold = currentInstruction.items[2].progress

                // val result = unsharpMask.usm(pixels2d, radius, percent, threshold)
                // val newBitmap = imageEditor.pixelsToBitmap(result)
                // imageView.setImageBitmap(newBitmap)
            }
            R.id.affinisImage -> {}
        }

        filterIsActive = false
    }

    // События ползунка
    private val onSeekBarChangeListener = object: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (!fromUser) return

            seekBars.forEachIndexed { index, otherSeekBar ->
                if (seekBar.id == otherSeekBar.id) {
                    currentInstruction.items[index].progress = progress
                    seekBarEditors[index].setText(progress.toString())
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) = startFilter()
    }

    // Получить нужную инструкцию для фильтра
    private fun getInstructionByFilterImageId(imageId: Int): Instruction {
        filtersSeekBarInstructions.forEach { instruction ->
            if (instruction.id == imageId) {
                return instruction
            }
        }

        return filtersSeekBarInstructions[0]
    }

    // Сменить фильтр, ориентируясь на его картинку
    private fun changeFilter(image: ImageView) {
        filtersIsAvailable = false

        if (::currentImage.isInitialized) {
            if (image == currentImage) return

            currentImage.background = null

            when (currentImage.id) {
                R.id.rotateImage -> {}
                R.id.scalingImage -> {}
                R.id.retouchImage -> {}
                R.id.definitionImage -> {}
                R.id.affinisImage -> {}
            }
        }
        image.background = AppCompatResources.getDrawable(
            this,
            R.drawable.filters_background
        )

        currentInstruction = getInstructionByFilterImageId(image.id)

        // Изменить настройки ползунков
        seekBarLayouts.forEachIndexed { index, seekBarLayout ->
            val item = currentInstruction.items[index]
            item.reset()

            seekBarLayout.visibility = item.visibility
            seekBarTitles[index].text = item.title
            seekBars[index].progress = item.start
            seekBars[index].max = item.max
            seekBarEditors[index].setText(item.start.toString())
            seekBarUnits[index].text = item.unit
        }

        when (image.id) {
            R.id.rotateImage -> {}
            R.id.scalingImage -> {}
            R.id.retouchImage -> {}
            R.id.definitionImage -> {}
            R.id.affinisImage -> {}
        }
        currentImage = image
        updatePixelsInfo()

        filtersIsAvailable = true
    }
}