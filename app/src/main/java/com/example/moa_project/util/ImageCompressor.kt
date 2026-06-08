package com.example.moa_project.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * 갤러리 원본 사진은 보통 3~12MB라 그대로 업로드하면 멀티파트 제한 초과·타임아웃·OOM으로 실패한다.
 * 업로드 전에 최대 변(1280px) 기준으로 다운스케일하고 JPEG 85%로 재인코딩해 1MB 미만으로 줄인다.
 *
 * content:// URI는 제공자마다 재오픈이 불안정하므로, 먼저 캐시 파일로 복사한 뒤 BitmapFactory로 디코딩한다.
 */
object ImageCompressor {
    private const val MAX_DIMENSION = 1280
    private const val JPEG_QUALITY = 85

    fun compressToTempFile(context: Context, uri: Uri, prefix: String): File {
        val appContext = context.applicationContext
        val sourceFile = copyUriToCache(appContext, uri, prefix)
        try {
            var bitmap = decodeBitmap(sourceFile)
            bitmap = scaleToMax(bitmap, MAX_DIMENSION)
            bitmap = applyExifRotation(sourceFile, bitmap)

            val output = File.createTempFile(prefix, ".jpg", appContext.cacheDir)
            FileOutputStream(output).use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) {
                    output.delete()
                    throw IllegalArgumentException("이미지를 저장할 수 없습니다.")
                }
            }
            bitmap.recycle()
            return output
        } finally {
            sourceFile.delete()
        }
    }

    private fun copyUriToCache(context: Context, uri: Uri, prefix: String): File {
        val temp = File.createTempFile("${prefix}src_", ".tmp", context.cacheDir)
        val input = openUriInputStream(context, uri)
            ?: run {
                temp.delete()
                throw IllegalArgumentException("이미지를 읽을 수 없습니다.")
            }
        try {
            input.use { src ->
                FileOutputStream(temp).use { dst ->
                    src.copyTo(dst)
                }
            }
        } catch (e: Exception) {
            temp.delete()
            throw IllegalArgumentException("이미지를 읽을 수 없습니다.", e)
        }
        if (temp.length() == 0L) {
            temp.delete()
            throw IllegalArgumentException("이미지 파일이 비어 있습니다.")
        }
        return temp
    }

    private fun openUriInputStream(context: Context, uri: Uri): InputStream? {
        context.contentResolver.openInputStream(uri)?.let { return it }
        return runCatching {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                FileInputStream(pfd.fileDescriptor)
            }
        }.getOrNull()
    }

    private fun decodeBitmap(file: File): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IllegalArgumentException("JPEG/PNG 형식의 이미지만 업로드할 수 있습니다.")
        }

        val sample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_DIMENSION)
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
            ?: throw IllegalArgumentException("이미지를 디코딩할 수 없습니다.")
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (w / 2 >= maxDim || h / 2 >= maxDim) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }

    private fun scaleToMax(src: Bitmap, maxDim: Int): Bitmap {
        val longest = maxOf(src.width, src.height)
        if (longest <= maxDim) return src
        val ratio = maxDim.toFloat() / longest
        val scaled = Bitmap.createScaledBitmap(
            src,
            (src.width * ratio).toInt().coerceAtLeast(1),
            (src.height * ratio).toInt().coerceAtLeast(1),
            true,
        )
        if (scaled != src) src.recycle()
        return scaled
    }

    private fun applyExifRotation(file: File, bitmap: Bitmap): Bitmap {
        val rotation = runCatching {
            val exif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ExifInterface(file)
            } else {
                @Suppress("DEPRECATION")
                ExifInterface(file.absolutePath)
            }
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }.getOrDefault(0f)

        if (rotation == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(rotation) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }
}
