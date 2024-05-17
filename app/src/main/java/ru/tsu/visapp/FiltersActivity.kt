package ru.tsu.visapp

import android.os.Bundle
import android.widget.Toast
import java.lang.Integer.min
import java.lang.Integer.max
import android.widget.SeekBar
import ru.tsu.visapp.filters.*
import android.graphics.Bitmap
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageView
import kotlinx.coroutines.launch
import android.widget.FrameLayout
import ru.tsu.visapp.utils.ImageEditor
import android.annotation.SuppressLint
import androidx.lifecycle.lifecycleScope
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
    private val imageRotation = ImageRotation() // Поворот изображения
    private val colorCorrection = ColorCorrection() // Цветокоррекция
    private val coloring = Coloring() // Цвета
    private val inversion = Inversion() // Инверсия
    private val popArt = PopArt() // Поп арт
    private val retouching = Retouching() // Ретушь
    private val unsharpMask = UnsharpMask() // Нерезкое маскирование
    private val glitch = Glitch() // Глитч

    private var filtersIsAvailable = false // Можно ли запускать фильтры
    private var filterIsActive = false // Запущен ли сейчас какой-то фильтр

    @SuppressLint("ClickableViewAccessibility", "CutPasteId")
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
                R.id.correctionImage,
                arrayOf(
                    Item(0, 255, "Яркость"),
                    Item(0, 255, "Насыщ."),
                    Item(0, 255, "Контраст")
                )
            ),
            Instruction(
                R.id.coloringImage,
                arrayOf(
                    Item(0, 255, "Красный"),
                    Item(0, 255, "Зеленый"),
                    Item(0, 255, "Синий")
                )
            ),
            Instruction(
                R.id.inversionImage,
                arrayOf(
                    Item(0, 1, "Красный"),
                    Item(0, 1, "Зеленый"),
                    Item(0, 1, "Синий")
                )
            ),
            Instruction(
                R.id.popArtImage,
                arrayOf(
                    Item(),
                    Item(85, 255, "Порог 1"),
                    Item(170, 255, "Порог 2")
                )
            ),
            Instruction(
                R.id.glitchImage,
                arrayOf(
                    Item(),
                    Item(0, 100, "Эффект", "%"),
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
                    Item(5, 100, "Размер"),
                    Item(5, 10, "Эффект")
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
            Instruction(R.id.affinisImage, arrayOf(Item(), Item(), Item())),
        )

        // Получить картинку и установить её
        val savedImageUri = imageEditor.getSavedImageUri(this, null)
        bitmap = imageEditor.createBitmapByUri(savedImageUri)
        imageView.setImageBitmap(bitmap)
        updatePixelsInfo()

        // Окошки фильтров
        val framesWithFilters: Array<FrameLayout> = arrayOf(
            findViewById(R.id.rotateFrame),
            findViewById(R.id.correctionFrame),
            findViewById(R.id.coloringFrame),
            findViewById(R.id.inversionFrame),
            findViewById(R.id.popArtFrame),
            findViewById(R.id.glitchFrame),
            findViewById(R.id.scalingFrame),
            findViewById(R.id.retouchFrame),
            findViewById(R.id.definitionFrame),
            findViewById(R.id.affinisFrame)
        )

        // Иконки фильтров (для подсветки)
        val imagesWithFilters: Array<ImageView> = arrayOf(
            findViewById(R.id.rotateImage),
            findViewById(R.id.correctionImage),
            findViewById(R.id.coloringImage),
            findViewById(R.id.inversionImage),
            findViewById(R.id.popArtImage),
            findViewById(R.id.glitchImage),
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

        // События нажатия на картинку
        imageView.setOnTouchListener { _, event ->
            if (!filterIsActive && currentImage.id == R.id.retouchImage) {
                filterIsActive = true

                val point = imageEditor.getPointFromImageView(
                    imageView,
                    event.x,
                    event.y,
                    width,
                    height
                )

                if (point != null) {
                    val size = currentInstruction.items[1].progress
                    val coefficient = currentInstruction.items[2].progress

                    retouching.retouch(
                        pixels,
                        width,
                        height,
                        point[0],
                        point[1],
                        size,
                        coefficient
                    )
                    imageEditor.setPixelsToBitmap(bitmap, pixels)
                    imageView.setImageBitmap(bitmap)
                }

                filterIsActive = false
            }
            true
        }
    }

    // Получить пиксели изображения
    private fun updatePixelsInfo() {
        pixels = imageEditor.getPixelsFromBitmap(bitmap)
        width = bitmap.width
        height = bitmap.height
    }

    // Запустить функцию фильтра
    private fun startFilter() {
        if (!filtersIsAvailable || filterIsActive) return

        filterIsActive = true

        lifecycleScope.launch {
            when (currentImage.id) {
                R.id.rotateImage -> {
                    val angle = currentInstruction.items[1].progress

                    bitmap = imageRotation.rotate(
                        pixels,
                        width,
                        height,
                        angle
                    )
                    imageView.setImageBitmap(bitmap)
                }
                R.id.correctionImage -> {
                    val brightnessValue = currentInstruction.items[0].progress
                    val saturationValue = currentInstruction.items[1].progress
                    val contrastValue = currentInstruction.items[2].progress

                    imageEditor.setPixelsToBitmap(
                        bitmap,
                        colorCorrection.correctColor(
                            pixels,
                            width,
                            height,
                            brightnessValue,
                            saturationValue,
                            contrastValue
                        )
                    )

                    imageView.setImageBitmap(bitmap)
                }

                R.id.coloringImage -> {
                    val redValue = currentInstruction.items[0].progress
                    val greenValue = currentInstruction.items[1].progress
                    val blueValue = currentInstruction.items[2].progress

                    imageEditor.setPixelsToBitmap(
                        bitmap,
                        coloring.coloring(
                            pixels,
                            width,
                            height,
                            redValue,
                            greenValue,
                            blueValue
                        )
                    )

                    imageView.setImageBitmap(bitmap)
                }
                R.id.inversionImage -> {
                    val isRedInverting =
                        currentInstruction.items[0].progress == 1
                    val isGreenInverting =
                        currentInstruction.items[1].progress == 1
                    val isBlueInverting =
                        currentInstruction.items[2].progress == 1

                    imageEditor.setPixelsToBitmap(
                        bitmap,
                        inversion.inverse(
                            pixels,
                            width,
                            height,
                            isRedInverting,
                            isGreenInverting,
                            isBlueInverting
                        )
                    )

                    imageView.setImageBitmap(bitmap)
                }
                R.id.popArtImage -> {
                    val threshold1 = currentInstruction.items[1].progress
                    val threshold2 = currentInstruction.items[2].progress

                    // Обновление размера bitmapa
                    bitmap = Bitmap.createBitmap(
                        2 * width,
                        2 * height,
                        Bitmap.Config.ARGB_8888
                    )

                    imageEditor.setPixelsToBitmap(
                        bitmap,
                        popArt.popArtFiltering(
                            pixels,
                            width,
                            height,
                            threshold1,
                            threshold2
                        )
                    )

                    imageView.setImageBitmap(bitmap)
                }
                R.id.glitchImage -> {
                    val effect = currentInstruction.items[1].progress

                    imageEditor.setPixelsToBitmap(
                        bitmap,
                        glitch.rgbGlitch(
                            pixels,
                            width,
                            height,
                            effect
                        )
                    )

                    imageView.setImageBitmap(bitmap)
                }
                R.id.scalingImage -> {}
                R.id.definitionImage -> {
                    val percent = currentInstruction.items[0].progress
                    val radius = currentInstruction.items[1].progress
                    val threshold = currentInstruction.items[2].progress

                    imageEditor.setPixelsToBitmap(bitmap, unsharpMask.usm(
                        pixels,
                        width,
                        height,
                        radius,
                        percent,
                        threshold
                    ))
                    imageView.setImageBitmap(bitmap)
                }
                R.id.affinisImage -> {}
            }

            filterIsActive = false
        }
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
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
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
                R.id.correctionImage -> {}
                R.id.coloringImage -> {}
                R.id.inversionImage -> {}
                R.id.popArtImage -> {}
                R.id.glitchImage -> {}
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
            R.id.correctionImage -> {}
            R.id.coloringImage -> {}
            R.id.inversionImage -> {}
            R.id.popArtImage -> {}
            R.id.glitchImage -> {}
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