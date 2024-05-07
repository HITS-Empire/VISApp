package ru.tsu.visapp

import android.os.Bundle
import android.os.PersistableBundle
import androidx.fragment.app.Fragment
import ru.tsu.visapp.fragments.HomeFragment
import androidx.appcompat.app.AppCompatActivity
import ru.tsu.visapp.fragments.InterestingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/*
 * Главный экран приложения
 */

class MainActivity: AppCompatActivity() {
    private val homeFragment = HomeFragment() // Главная
    private val interestingFragment = InterestingFragment() // Интересное

    private var currentFragmentId = R.id.frameLayout // ID текущего фрагмента

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Нижнее меню навигации
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        // События кликов по элементам меню
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            currentFragmentId = menuItem.itemId
            updateCurrentFragment()
            true
        }

        currentFragmentId = (
            savedInstanceState?.getInt("currentFragmentId") // Сохранённый фрагмент
        ) ?: (
            R.id.navigationHome // Главная по умолчанию
        )
        updateCurrentFragment()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("currentFragmentId", currentFragmentId)
    }

    // Обновить нужный фрагмент
    private fun updateCurrentFragment() {
        var fragment: Fragment? = null
        when (currentFragmentId) {
            R.id.navigationHome -> fragment = homeFragment
            R.id.navigationInteresting -> fragment = interestingFragment
        }

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayout, fragment!!)
            commit()
        }
    }
}