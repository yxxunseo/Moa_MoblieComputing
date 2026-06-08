package com.example.moa.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 업로드된 이미지를 직접 제공한다.
 * ResourceHandler는 한글 경로 등 로컬 환경에서 500을 내는 경우가 있어 컨트롤러로 고정한다.
 */
@RestController
class UploadsController(
    @Value("\${moa.upload-dir:uploads}") private val uploadDir: String,
) {
    @GetMapping("/uploads/profiles/{filename}")
    fun profileImage(@PathVariable filename: String): ResponseEntity<ByteArray> =
        serveImage("profiles", filename)

    @GetMapping("/uploads/groups/{filename}")
    fun groupCover(@PathVariable filename: String): ResponseEntity<ByteArray> =
        serveImage("groups", filename)

    private fun serveImage(subdir: String, filename: String): ResponseEntity<ByteArray> {
        val safeName = Paths.get(filename).fileName.toString()
        if (safeName.isBlank() || safeName.contains("..")) {
            return ResponseEntity.badRequest().build()
        }

        val baseDir = Paths.get(uploadDir, subdir).toAbsolutePath().normalize()
        val path = baseDir.resolve(safeName).normalize()
        if (!path.startsWith(baseDir) || !Files.exists(path)) {
            return ResponseEntity.notFound().build()
        }

        val bytes = Files.readAllBytes(path)
        val contentType = when {
            safeName.endsWith(".png", true) -> MediaType.IMAGE_PNG
            safeName.endsWith(".webp", true) -> MediaType("image", "webp")
            else -> MediaType.IMAGE_JPEG
        }
        return ResponseEntity.ok().contentType(contentType).body(bytes)
    }
}
