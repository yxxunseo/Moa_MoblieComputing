package com.example.moa.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.format.DateTimeParseException

/**
 * 전역 예외 핸들러.
 * 기존에는 서비스에서 던지는 IllegalArgumentException 등이 모두 500(Internal Server Error)로
 * 내려가 클라이언트가 "잘못된 요청"과 "서버 장애"를 구분할 수 없었다.
 * 여기서 예외 유형별로 적절한 HTTP 상태코드와 JSON 메시지({"message": ...})로 변환한다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    private fun body(message: String?) = mapOf("message" to (message ?: "요청을 처리할 수 없습니다."))

    /** 잘못된 입력값 → 400 */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(e.message))
    }

    /** 잘못된 상태에서의 요청(예: 확정 안 된 일정 완료) → 409 */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(e: IllegalStateException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(e.message))
    }

    /** 날짜/시간 파싱 실패 → 400 */
    @ExceptionHandler(DateTimeParseException::class)
    fun handleDateTimeParse(e: DateTimeParseException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(body("날짜/시간 형식이 올바르지 않습니다."))
    }

    /** @Valid 검증 실패 → 400 (첫 번째 필드 오류 메시지 반환) */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val message = e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "입력값이 올바르지 않습니다."
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(message))
    }

    /** 그 외 예측하지 못한 예외 → 500 (로그 기록) */
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<Map<String, String>> {
        log.error("처리되지 않은 예외 발생", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(body("서버 내부 오류가 발생했습니다."))
    }
}
