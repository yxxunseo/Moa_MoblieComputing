package com.example.moa_project.network

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val color: String
)

data class JoinGroupRequest(
    val inviteCode: String
)

data class GroupMemberPreviewDto(
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
    val memberPreviews: List<GroupMemberPreviewDto>? = null,
)

data class GroupMemberResponse(
    val userId: Long,
    val nickname: String,
    val role: String,
    val profileImageUrl: String?
)
