package com.example.moa_project.util

object MoaDeepLinkStore {
    var pendingJoinCode: String? = null

    fun consumeJoinCode(): String? {
        val code = pendingJoinCode
        pendingJoinCode = null
        return code
    }
}
