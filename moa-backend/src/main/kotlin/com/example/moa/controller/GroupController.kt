package com.example.moa.controller

import com.example.moa.service.GroupService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val color: String
)

data class JoinGroupRequest(
    val inviteCode: String
)

data class GroupResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val inviteCode: String,
    val color: String,
    val memberCount: Long,
    val createdAt: String
)

@RestController
@RequestMapping("/api/groups")
class GroupController(
    private val groupService: GroupService
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
    fun getGroupDetail(@PathVariable id: Long): ResponseEntity<GroupResponse> {
        return ResponseEntity.ok(groupService.getGroupDetail(id))
    }

    @PostMapping("/join")
    fun joinGroup(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: JoinGroupRequest
    ): ResponseEntity<Map<String, Any>> {
        val userId = userDetails.username.toLong()
        return ResponseEntity.ok(groupService.joinGroup(userId, request.inviteCode))
    }

    @DeleteMapping("/{id}/leave")
    fun leaveGroup(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        val userId = userDetails.username.toLong()
        groupService.leaveGroup(userId, id)
        return ResponseEntity.noContent().build()
    }
}
