package com.example.msusuarios.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SaludController {

    @GetMapping("/api/health")
    fun salud(): Map<String, String> {
        return mapOf("estado" to "ok")
    }
}