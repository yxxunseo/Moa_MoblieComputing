package com.example.moa.controller

import com.example.moa.service.GroupImageStorageService
import com.example.moa.service.GroupService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val color: String
)

data class JoinGroupRequest(
    val inviteCode: String
)

data class GroupMemberPreview(
    val nickname: String,
    val profileImageUrl: String?
)

data class GroupResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val inviteCode: String,
    val color: String,
    val coverImageUrl: String? = null,
    val memberCount: Long,
    val createdAt: String,
    val memberPreviews: List<GroupMemberPreview> = emptyList()
)

data class GroupMemberResponse(
    val userId: Long,
    val nickname: String,
    val role: String,
    val profileImageUrl: String?
)

@RestController
@RequestMapping("/api/groups")
class GroupController(
    private val groupService: GroupService,
    private val groupImageStorageService: GroupImageStorageService
) {
    @PostMapping
    fun createGroup(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: CreateGroupRequest
    ): ResponseEntity<GroupResponse> {
        val userId = userDetails.username.toLong()
        val group = groupService.createGroup(userId, request.name, request.description, request.color)
        return ResponseEntity.status(HttpStatus.CREATED).body(group)
    }

    @GetMapping("/{id}")
    fun getGroupDetail(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<GroupResponse> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(groupService.getGroupDetail(userId, id))
    }

    @GetMapping("/{id}/members")
    fun getGroupMembers(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<List<GroupMemberResponse>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(groupService.getGroupMembers(userId, id))
    }

    @PostMapping("/join")
    fun joinGroup(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: JoinGroupRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(groupService.joinGroup(userId, request.inviteCode))
    }

    @PostMapping("/{id}/cover-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadGroupCover(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long,
        @RequestParam("file") file: org.springframework.web.multipart.MultipartFile
    ): ResponseEntity<GroupResponse> {
        val userId = userDetails.username.toLong()
        val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        val imageUrl = groupImageStorageService.storeGroupCover(id, file, baseUrl)
        return ResponseEntity.ok(groupService.updateGroupCover(userId, id, imageUrl))
    }

    @DeleteMapping("/{id}/leave")
    fun leaveGroup(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<Map<String, Any>> {
        val userId = userDetails.username.toLong()
        val isAdmin = groupService.isAdmin(userId, id)
        groupService.leaveGroup(userId, id)
        return ResponseEntity.ok(mapOf(
            "groupDeleted" to isAdmin,
            "message" to if (isAdmin) "관리자가 나가 모임이 삭제되었습니다." else "모임에서 나갔습니다."
        ))
    }
}
