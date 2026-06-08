package com.example.moa_project.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/**
 * Android 13+ 사진 피커 우선, 구형 기기는 GetContent 폴백.
 */
@Composable
fun rememberImagePickerLauncher(onImageSelected: (Uri) -> Unit) =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) onImageSelected(uri)
    }

fun launchImagePicker(
    launcher: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>,
) {
    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
}
