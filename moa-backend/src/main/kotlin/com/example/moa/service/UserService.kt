package com.example.moa.service

import com.example.moa.controller.UserResponse
import com.example.moa.controller.toResponse
import com.example.moa.repository.GroupMemberRepository
import com.example.moa.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val groupMemberRepository: GroupMemberRepository
) {
    @Transactional(readOnly = true)
    fun getUserProfile(userId: Long): UserResponse {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }
        return user.toResponse()
    }

    @Transactional
    fun updateProfile(userId: Long, nickname: String, profileImageUrl: String?): UserResponse {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }
        
        user.nickname = nickname
        if (profileImageUrl != null) {
            user.profileImageUrl = profileImageUrl
        }
        
        return user.toResponse()
    }
}
