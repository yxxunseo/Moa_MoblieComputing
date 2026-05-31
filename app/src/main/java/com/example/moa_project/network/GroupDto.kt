package com.example.moa_project.network

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

data class GroupMemberResponse(
    val userId: Long,
    val nickname: String,
    val role: String,
    val profileImageUrl: String?
)
