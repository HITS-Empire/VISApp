package ru.tsu.visapp.fragments

import java.io.File
import android.net.Uri
import ru.tsu.visapp.R
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.view.ViewGroup
import android.content.Context
import android.widget.ImageButton
import android.view.LayoutInflater
import ru.tsu.visapp.FiltersActivity
import androidx.fragment.app.Fragment
import androidx.core.content.FileProvider
import androidx.activity.result.contract.ActivityResultContracts

class HomeFragment: Fragment() {
    private lateinit var newImageURI: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получить локальное хранилище
        val sharedPreferences = requireContext().getSharedPreferences(
            "ru.tsu.visapp",
            Context.MODE_PRIVATE
        )

        // Перезагрузить функции для получения картинок
        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                sharedPreferences.edit().putString("selected_uri", uri.toString()).apply()
                goToFiltersActivity()
            }
        }
        val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { it: Boolean ->
            if (it) {
                sharedPreferences.edit().putString("selected_uri", newImageURI.toString()).apply()
                goToFiltersActivity()
            }
        }

        // Кнопки галереи и камеры
        val galleryButton: ImageButton = view.findViewById(R.id.galleryButton)
        val cameraButton: ImageButton = view.findViewById(R.id.cameraButton)

        // События кликов по кнопкам
        galleryButton.setOnClickListener {
            getContent.launch("image/*")
        }
        cameraButton.setOnClickListener {
            newImageURI = createImageUri()
            takePicture.launch(newImageURI)
        }
    }

    private fun createImageUri(): Uri = FileProvider.getUriForFile(
        requireContext(),
        "ru.tsu.visapp.FileProvider",
        File(requireContext().filesDir, "visapp_image.png")
    )

    private fun goToFiltersActivity() {
        val intent = Intent(requireContext(), FiltersActivity::class.java)
        startActivity(intent)
    }
}