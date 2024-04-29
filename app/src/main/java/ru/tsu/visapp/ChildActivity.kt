package ru.tsu.visapp

import android.content.Intent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

/*
 * Родительский класс для всех дочерних экранов
 */

open class ChildActivity: AppCompatActivity() {
    fun initializeView(layout: Int) {
        setContentView(layout)

        // Кнопка "Назад"
        val backButton: ImageButton = findViewById(R.id.backButton)

        // События кликов по кнопке
        backButton.setOnClickListener {
            goToMainActivity()
        }
    }

    // Перейти на главный экран
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}