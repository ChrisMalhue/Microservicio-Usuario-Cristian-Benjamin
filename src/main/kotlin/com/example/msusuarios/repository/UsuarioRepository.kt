package com.example.msusuarios.repository

import com.example.msusuarios.model.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UsuarioRepository : JpaRepository<Usuario, Int> {
    fun findByCorreo(correo: String): Usuario?
    fun existsByCorreo(correo: String): Boolean
}