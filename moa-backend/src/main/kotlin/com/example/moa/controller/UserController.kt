package com.example.moa.controller

import com.example.moa.service.GroupService
import com.example.moa.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import com.example.moa.service.ProfileImageStorageService

data class UpdateProfileRequest(
    val nickname: String,
    val profileImageUrl: String? = null
)

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val groupService: GroupService,
    private val profileImageStorageService: ProfileImageStorageService
) {
    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<UserResponse> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(userService.getUserProfile(userId))
    }

    @PostMapping("/me/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadProfileImage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<UserResponse> {
        val userId = userDetails.username.toLong()
        val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        val imageUrl = profileImageStorageService.storeProfileImage(userId, file, baseUrl)
        val user = userService.getUserProfile(userId)
        return ResponseEntity.ok(
            userService.updateProfile(userId, user.nickname, imageUrl)
        )
    }

    @PutMapping("/me")
    fun updateProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserResponse> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(userService.updateProfile(userId, request.nickname, request.profileImageUrl))
    }

    @GetMapping("/me/groups")
    fun getMyGroups(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<List<GroupResponse>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(groupService.getUserGroups(userId))
    }
}
