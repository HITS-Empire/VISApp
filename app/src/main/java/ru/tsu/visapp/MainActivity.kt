package ru.tsu.visapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import ru.tsu.visapp.fragments.HomeFragment
import ru.tsu.visapp.fragments.FiltersFragment
import androidx.appcompat.app.AppCompatActivity
import ru.tsu.visapp.fragments.InterestingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Нижнее меню навигации
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        val homeFragment = HomeFragment() // Главная
        val filtersFragment = FiltersFragment() // Фильтры
        val interestingFragment = InterestingFragment() // Интересное

        // Установить Главную по умолчанию
        setCurrentFragment(homeFragment)

        // События кликов по элементам меню
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigationHome -> {
                    setCurrentFragment(homeFragment)
                    true
                }
                R.id.navigationFilters -> {
                    // setCurrentFragment(filtersFragment)
                    false
                }
                R.id.navigationInteresting -> {
                    setCurrentFragment(interestingFragment)
                    true
                }
                else -> true
            }
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayout, fragment)
            commit()
        }
    }
}