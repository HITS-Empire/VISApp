package ru.tsu.visapp

import android.os.Bundle

/*
 * Экран для стенографии
 */

class StenographyActivity: ChildActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView(R.layout.activity_stenography)
    }
}