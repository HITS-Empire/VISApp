package ru.tsu.visapp.utils

import java.io.File
import android.net.Uri
import ru.tsu.visapp.R
import android.content.Context
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import android.content.SharedPreferences
import androidx.core.content.FileProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

/*
 * Плагин, с помощью которого можно получить фото
 */

class ImageGetter(
    activity: AppCompatActivity?,
    fragment: Fragment?,
    callback: (() -> Unit)?
) {
    // Локальное хранилище
    private lateinit var sharedPreferences: SharedPreferences

    // URI нового фото с камеры
    private lateinit var newImageUri: Uri

    // Функция для получения URI нового фото
    private lateinit var createImageUri: () -> Uri

    // Получить картинку из галереи
    private lateinit var getContent: ActivityResultLauncher<String>

    // Создать новую фотографию
    private lateinit var takePicture: ActivityResultLauncher<Uri>

    // Кнопки галереи и камеры
    private var galleryButton: ImageButton? = null
    private var cameraButton: ImageButton? = null

    // Контракты для лаунчеров
    private val getContentContract = ActivityResultContracts.GetContent()
    private val takePhotoContract = ActivityResultContracts.TakePicture()

    // Обработчики результата для лаунчеров
    private val getContentCallback = fun(uri: Uri?) {
        if (uri != null) {
            sharedPreferences.edit().putString("selected_uri", uri.toString()).apply()
            if (callback != null) callback()
        }
    }
    private val takePictureCallback = fun(isTaken: Boolean) {
        if (isTaken) {
            sharedPreferences.edit().putString("selected_uri", newImageUri.toString()).apply()
            if (callback != null) callback()
        }
    }

    init {
        if (activity != null) {
            sharedPreferences = activity.getSharedPreferences(
                "ru.tsu.visapp",
                Context.MODE_PRIVATE
            )

            createImageUri = fun(): Uri = FileProvider.getUriForFile(
                activity,
                "ru.tsu.visapp.FileProvider",
                File(activity.filesDir, "visapp_image.png")
            )

            getContent = activity
                .registerForActivityResult(getContentContract) { uri: Uri? ->
                    getContentCallback(uri)
                }
            takePicture = activity
                .registerForActivityResult(takePhotoContract) { isTaken: Boolean ->
                    takePictureCallback(isTaken)
                }

            galleryButton = activity.findViewById(R.id.galleryButton)
            cameraButton = activity.findViewById(R.id.cameraButton)
        }
        if (fragment != null) {
            sharedPreferences = fragment.requireContext().getSharedPreferences(
                "ru.tsu.visapp",
                Context.MODE_PRIVATE
            )

            createImageUri = fun(): Uri = FileProvider.getUriForFile(
                fragment.requireContext(),
                "ru.tsu.visapp.FileProvider",
                File(fragment.requireContext().filesDir, "visapp_image.png")
            )

            getContent = fragment
                .registerForActivityResult(getContentContract) { uri: Uri? ->
                    getContentCallback(uri)
                }
            takePicture = fragment
                .registerForActivityResult(takePhotoContract) { isTaken: Boolean ->
                    takePictureCallback(isTaken)
                }

            galleryButton = fragment.view?.findViewById(R.id.galleryButton)
            cameraButton = fragment.view?.findViewById(R.id.cameraButton)
        }

        // События кликов по кнопкам
        galleryButton?.setOnClickListener {
            getContent.launch("image/*")
        }
        cameraButton?.setOnClickListener {
            newImageUri = createImageUri()
            takePicture.launch(newImageUri)
        }
    }
}