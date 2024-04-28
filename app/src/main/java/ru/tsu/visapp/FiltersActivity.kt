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
import ru.tsu.visapp.utils.Editor
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity

class FiltersActivity: AppCompatActivity() {
    private lateinit var bitmap: Bitmap // Картинка для редактирования

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)

        val imageView: ImageView = findViewById(R.id.filtersImageView)

        val editor = Editor(contentResolver) // Редактор изображений

        // Получить локальное хранилище
        val sharedPreferences = this.getSharedPreferences(
            "ru.tsu.visapp",
            Context.MODE_PRIVATE
        )

        // Получить картинку и установить её
        val selectedUri = sharedPreferences.getString("selected_uri", "")
        val uri = Uri.parse(selectedUri)
        val inputStream = contentResolver.openInputStream(uri)
        bitmap = BitmapFactory.decodeStream(inputStream)
        imageView.setImageBitmap(bitmap)

        val backButton: ImageButton = findViewById(R.id.backButton) // Кнопка "Назад"
        val saveButton: TextView = findViewById(R.id.saveButton) // Кнопка "Сохранить"

        // События кликов по кнопкам
        backButton.setOnClickListener {
            goToMainActivity()
        }
        saveButton.setOnClickListener {
            var text = "Изображение успешно сохранено"

            try {
                editor.saveImageToGallery(bitmap)
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