package ru.tsu.visapp

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import ru.tsu.visapp.fragments.HomeFragment
import androidx.appcompat.app.AppCompatActivity
import ru.tsu.visapp.fragments.InterestingFragment
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity: AppCompatActivity() {
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // Сделать что-то с картинкой
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Нижнее меню навигации
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        val homeFragment = HomeFragment() // Главная
        val interestingFragment = InterestingFragment() // Интересное

        // Установить Главную по умолчанию
        setCurrentFragment(homeFragment)

        // Запросить картинку
        // getContent.launch("image/*")

        // События кликов по элементам меню
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigationHome -> setCurrentFragment(homeFragment)
                R.id.navigationInteresting -> setCurrentFragment(interestingFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayout, fragment)
            commit()
        }
    }
}
