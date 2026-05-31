package com.example.moa.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration}") private val expiration: Long,
    @Value("\${jwt.refresh-expiration}") private val refreshExpiration: Long
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun generateToken(userId: Long): String = buildToken(userId, expiration, "access")

    fun generateRefreshToken(userId: Long): String = buildToken(userId, refreshExpiration, "refresh")

    private fun buildToken(userId: Long, ttl: Long, type: String): String {
        val now = Date()
        val expiryDate = Date(now.time + ttl)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun isRefreshToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
            claims["type"] == "refresh"
        } catch (e: Exception) {
            false
        }
    }

    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
        return claims.subject.toLong()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }
}
