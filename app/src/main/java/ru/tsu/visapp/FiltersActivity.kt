package ru.tsu.visapp

import android.net.Uri
import android.os.Bundle
import android.content.Context
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class FiltersActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)

        val imageView: ImageView = findViewById(R.id.filtersImageView)

        // Получить локальное хранилище
        val sharedPreferences = this.getSharedPreferences(
            "ru.tsu.visapp",
            Context.MODE_PRIVATE
        )

        // Получить картинку и установить её
        val selectedUri = sharedPreferences.getString("selected_uri", "")
        imageView.setImageURI(Uri.parse(selectedUri))
    }
}