package com.game.tic_tac_toe.game.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonDeserializationException(ex: HttpMessageNotReadableException): ResponseEntity<Any> {
        return ResponseEntity("Invalid JSON format: ${ex.message}", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity
            .badRequest()
            .body(exception.message)
    }
}
