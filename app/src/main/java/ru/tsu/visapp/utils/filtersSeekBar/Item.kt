package ru.tsu.visapp.utils.filtersSeekBar

import android.view.View

class Item(
    initStart: Int? = null,
    initMax: Int? = null,
    initTitle: String = "",
    initUnit: String = ""
) {
    val visibility = if (initStart == null) View.INVISIBLE else View.VISIBLE
    val start = initStart ?: 0
    var progress = initStart ?: 0
    val max = initMax ?: 0
    val title = initTitle
    val unit = initUnit

    fun reset() {
        progress = start
    }
}