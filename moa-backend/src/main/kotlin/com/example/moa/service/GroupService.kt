package com.example.moa.service

import com.example.moa.controller.GroupResponse
import com.example.moa.entity.GroupMember
import com.example.moa.entity.MeetingGroup
import com.example.moa.repository.GroupMemberRepository
import com.example.moa.repository.GroupRepository
import com.example.moa.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
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
        
        return group.toResponse(1)
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
            group.toResponse(memberCount)
        }
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
    fun getGroupDetail(groupId: Long): GroupResponse {
        val group = groupRepository.findById(groupId).orElseThrow { 
            IllegalArgumentException("그룹을 찾을 수 없습니다.") 
        }
        val count = groupMemberRepository.countByGroup(group)
        return group.toResponse(count)
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
            
        groupMemberRepository.delete(member)
    }
}

fun MeetingGroup.toResponse(memberCount: Long) = GroupResponse(
    id = id,
    name = name,
    description = description,
    inviteCode = inviteCode,
    color = color,
    memberCount = memberCount,
    createdAt = createdAt.toString()
)
