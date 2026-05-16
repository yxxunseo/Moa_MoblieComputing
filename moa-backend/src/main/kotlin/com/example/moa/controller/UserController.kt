package com.example.moa.controller

import com.example.moa.service.GroupService
import com.example.moa.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

data class UpdateProfileRequest(
    val nickname: String,
    val profileImageUrl: String? = null
)

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val groupService: GroupService
) {
    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<UserResponse> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(userService.getUserProfile(userId))
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
