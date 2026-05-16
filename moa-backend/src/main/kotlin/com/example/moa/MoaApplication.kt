package com.example.moa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MoaApplication

fun main(args: Array<String>) {
    runApplication<MoaApplication>(*args)
}
