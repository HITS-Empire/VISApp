package ru.tsu.visapp

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.graphics.Color
import android.content.Intent
import android.content.Context
import android.graphics.Bitmap
import android.widget.TextView
import android.widget.ImageView
import android.widget.ImageButton
import ru.tsu.visapp.utils.Editor
import androidx.appcompat.app.AppCompatActivity

class FiltersActivity: AppCompatActivity() {
    private val title = System.currentTimeMillis().toString() // Название изображения

    private lateinit var editor: Editor // Редактор изображений
    private lateinit var bitmap: Bitmap // Картинка для редактирования
    private lateinit var pixels: IntArray // Массив пикселей

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
        editor = Editor(contentResolver)

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
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}