package com.example.moa.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Service
class GroupImageStorageService(
    @Value("\${moa.upload-dir:uploads}") private val uploadDir: String,
    @Value("\${server.public-url:http://localhost:8080}") private val publicUrl: String
) {
    fun storeGroupCover(groupId: Long, file: MultipartFile, baseUrl: String? = null): String {
        if (file.isEmpty) throw IllegalArgumentException("이미지 파일이 비어 있습니다.")
        val contentType = file.contentType ?: ""
        if (!contentType.startsWith("image/")) {
            throw IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.")
        }
        val ext = when {
            contentType.contains("png") -> "png"
            contentType.contains("webp") -> "webp"
            else -> "jpg"
        }
        val dir = Paths.get(uploadDir, "groups")
        Files.createDirectories(dir)
        val filename = "group_${groupId}_${UUID.randomUUID().toString().take(8)}.$ext"
        val target = dir.resolve(filename)
        Files.write(target, file.bytes)
        // 상대 경로만 저장 — 클라이언트가 SERVER_URL과 조합해 로드
        return "/uploads/groups/$filename"
    }
}
