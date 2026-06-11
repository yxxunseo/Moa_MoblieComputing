package com.example.moa.service

import com.example.moa.controller.GroupMemberPreview
import com.example.moa.controller.GroupResponse
import com.example.moa.controller.GroupMemberResponse
import com.example.moa.entity.GroupMember
import com.example.moa.entity.MeetingGroup
import com.example.moa.repository.CalendarEventRepository
import com.example.moa.repository.GroupMemberRepository
import com.example.moa.repository.GroupRepository
import com.example.moa.repository.ScheduleRepository
import com.example.moa.repository.ScheduleReactionRepository
import com.example.moa.repository.TimeSlotRepository
import com.example.moa.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val scheduleRepository: ScheduleRepository,
    private val timeSlotRepository: TimeSlotRepository,
    private val scheduleReactionRepository: ScheduleReactionRepository,
    private val calendarEventRepository: CalendarEventRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun createGroup(userId: Long, name: String, description: String?, color: String): GroupResponse {
        val user = userRepository.findById(userId).orElseThrow { 
            IllegalArgumentException("사용자를 찾을 수 없습니다.") 
        }
        
        // 고유 초대코드 생성 (예: MOA-A3X9K2)
        val inviteCode = "MOA-" + UUID.randomUUID().toString().substring(0, 6).uppercase()
        
        val group = groupRepository.save(
            MeetingGroup(
                name = name,
                description = description,
                inviteCode = inviteCode,
                color = color,
                createdBy = user
            )
        )
        
        // 그룹 생성자는 자동으로 ADMIN 역할로 가입됨
        groupMemberRepository.save(
            GroupMember(
                group = group,
                user = user,
                role = "ADMIN"
            )
        )
        
        return group.toResponse(1, memberPreviewsFor(group))
    }

    @Transactional(readOnly = true)
    fun getUserGroups(userId: Long): List<GroupResponse> {
        val user = userRepository.findById(userId).orElseThrow { 
            IllegalArgumentException("사용자를 찾을 수 없습니다.") 
        }
        
        val members = groupMemberRepository.findAllByUser(user)
        return members.map { member ->
            val group = member.group!!
            val memberCount = groupMemberRepository.countByGroup(group)
            group.toResponse(memberCount, memberPreviewsFor(group))
        }
    }

    @Transactional(readOnly = true)
    fun getInvitePreview(inviteCode: String): Map<String, String> {
        val group = groupRepository.findByInviteCode(inviteCode.trim())
            ?: throw IllegalArgumentException("유효하지 않은 초대코드입니다.")
        val inviterName = group.createdBy?.nickname?.takeIf { it.isNotBlank() } ?: "친구"
        return mapOf(
            "inviterName" to inviterName,
            "groupName" to group.name,
        )
    }

    @Transactional
    fun joinGroup(userId: Long, inviteCode: String): Map<String, Any> {
        val user = userRepository.findById(userId).orElseThrow { 
            IllegalArgumentException("사용자를 찾을 수 없습니다.") 
        }
        val group = groupRepository.findByInviteCode(inviteCode) ?: throw IllegalArgumentException("유효하지 않은 초대코드입니다.")
        
        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw IllegalArgumentException("이미 가입된 그룹입니다.")
        }
        
        groupMemberRepository.save(
            GroupMember(
                group = group,
                user = user,
                role = "MEMBER"
            )
        )
        
        return mapOf(
            "groupId" to group.id,
            "groupName" to group.name,
            "message" to "그룹에 성공적으로 입장했습니다!"
        )
    }
    
    @Transactional(readOnly = true)
    fun getGroupDetail(userId: Long, groupId: Long): GroupResponse {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }
        val group = groupRepository.findById(groupId).orElseThrow {
            IllegalArgumentException("그룹을 찾을 수 없습니다.")
        }
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw IllegalArgumentException("해당 그룹의 멤버가 아닙니다.")
        }
        val count = groupMemberRepository.countByGroup(group)
        return group.toResponse(count, memberPreviewsFor(group))
    }

    private fun memberPreviewsFor(group: MeetingGroup): List<GroupMemberPreview> {
        return groupMemberRepository.findAllByGroup(group)
            .take(4)
            .map { member ->
                GroupMemberPreview(
                    nickname = member.user!!.nickname,
                    profileImageUrl = member.user!!.profileImageUrl
                )
            }
    }

    @Transactional(readOnly = true)
    fun getGroupMembers(userId: Long, groupId: Long): List<GroupMemberResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }
        val group = groupRepository.findById(groupId).orElseThrow {
            IllegalArgumentException("그룹을 찾을 수 없습니다.")
        }
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw IllegalArgumentException("해당 그룹의 멤버가 아닙니다.")
        }
        return groupMemberRepository.findAllByGroup(group).map { member ->
            GroupMemberResponse(
                userId = member.user!!.id,
                nickname = member.user!!.nickname,
                role = member.role,
                profileImageUrl = member.user!!.profileImageUrl
            )
        }
    }

    @Transactional
    fun updateGroupCover(userId: Long, groupId: Long, imageUrl: String): GroupResponse {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }
        val group = groupRepository.findById(groupId).orElseThrow {
            IllegalArgumentException("그룹을 찾을 수 없습니다.")
        }
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw IllegalArgumentException("해당 그룹의 멤버가 아닙니다.")
        }
        group.coverImageUrl = imageUrl
        val count = groupMemberRepository.countByGroup(group)
        return group.toResponse(count, memberPreviewsFor(group))
    }

    @Transactional(readOnly = true)
    fun isAdmin(userId: Long, groupId: Long): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false
        val group = groupRepository.findById(groupId).orElse(null) ?: return false
        val member = groupMemberRepository.findByGroupAndUser(group, user) ?: return false
        return member.role == "ADMIN"
    }

    @Transactional
    fun leaveGroup(userId: Long, groupId: Long) {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }
        val group = groupRepository.findById(groupId).orElseThrow {
            IllegalArgumentException("그룹을 찾을 수 없습니다.")
        }

        val member = groupMemberRepository.findByGroupAndUser(group, user)
            ?: throw IllegalArgumentException("해당 그룹의 멤버가 아닙니다.")

        if (member.role == "ADMIN") {
            // 관리자가 나가면 그룹 전체 삭제
            deleteGroupAndAllRelatedData(group)
        } else {
            groupMemberRepository.delete(member)
        }
    }

    /**
     * 그룹과 모든 연관 데이터(캘린더 이벤트, 타임슬롯, 반응, 일정, 멤버)를 자식→부모 순서로 삭제.
     * 기존엔 일정마다 findAll 후 deleteAll을 반복(N+1)했고, 일정을 참조하는 캘린더 이벤트를
     * 지우지 않아 확정 일정이 있는 그룹은 FK 위반으로 삭제가 실패할 수 있었다.
     * 이제 그룹 단위 단일 DELETE 쿼리들로 일정 수와 무관하게 일정한 쿼리 수로 삭제한다.
     */
    @Transactional
    fun deleteGroupAndAllRelatedData(group: MeetingGroup) {
        calendarEventRepository.deleteAllByGroup(group)
        timeSlotRepository.deleteAllByGroup(group)
        scheduleReactionRepository.deleteAllByGroup(group)
        scheduleRepository.deleteAllByGroup(group)
        groupMemberRepository.deleteAllByGroup(group)
        groupRepository.delete(group)
    }
}

fun MeetingGroup.toResponse(memberCount: Long, memberPreviews: List<GroupMemberPreview> = emptyList()) = GroupResponse(
    id = id,
    name = name,
    description = description,
    inviteCode = inviteCode,
    color = color,
    coverImageUrl = coverImageUrl,
    memberCount = memberCount,
    createdAt = createdAt.toString(),
    memberPreviews = memberPreviews
)
