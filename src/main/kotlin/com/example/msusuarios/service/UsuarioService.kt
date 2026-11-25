package com.example.msusuarios.service

import com.example.msusuarios.model.Rol
import com.example.msusuarios.model.Usuario
import com.example.msusuarios.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository
) {
    private val logger = LoggerFactory.getLogger(UsuarioService::class.java)
    private val passwordEncoder = BCryptPasswordEncoder()

    fun registrar(usuario: Usuario): Usuario? {
        logger.info("Intentando registrar usuario con correo: {}", usuario.correo)
        if (usuarioRepository.existsByCorreo(usuario.correo)) {
            logger.warn("Registro fallido: el correo {} ya está registrado", usuario.correo)
            return null
        }

        val passwordEncriptada = passwordEncoder.encode(usuario.password)!!
        val nuevoUsuario = usuario.copy(
            id = null,
            password = passwordEncriptada,
            rol = Rol.PROFESOR
        )

        val guardado = usuarioRepository.save(nuevoUsuario)
        logger.info("Usuario registrado exitosamente con ID: {} y rol: {}", guardado.id, guardado.rol)
        return guardado
    }

    fun login(correo: String, password: String): Usuario? {
        logger.info("Intento de inicio de sesión para: {}", correo)
        val usuario = usuarioRepository.findByCorreo(correo)
        return if (usuario != null && passwordEncoder.matches(password, usuario.password)) {
            logger.info("Login exitoso para usuario ID: {} ({})", usuario.id, usuario.rol)
            usuario
        } else {
            logger.warn("Login fallido: credenciales incorrectas para {}", correo)
            null
        }
    }

    fun listarTodos(): List<Usuario> {
        logger.info("Consultando lista completa de usuarios")
        return usuarioRepository.findAll()
    }

    fun obtenerPorId(id: Int): Usuario? {
        logger.info("Buscando usuario por ID: {}", id)
        return usuarioRepository.findById(id).orElse(null)
    }

    fun obtenerPorCorreo(correo: String): Usuario? {
        logger.info("Buscando usuario por correo: {}", correo)
        return usuarioRepository.findByCorreo(correo)
    }

    fun actualizar(id: Int, datos: Usuario): Usuario? {
        logger.info("Intentando actualizar usuario con ID: {}", id)
        val existente = usuarioRepository.findById(id).orElse(null)
        if (existente == null) {
            logger.warn("Actualización fallida: no se encontró usuario con ID: {}", id)
            return null
        }
        if (datos.correo != existente.correo && usuarioRepository.existsByCorreo(datos.correo)) {
            logger.warn("Actualización fallida: el correo {} ya está registrado", datos.correo)
            throw CorreoDuplicadoException(datos.correo)
        }

        val passwordFinal = when {
            datos.password.isBlank() -> existente.password
            datos.password.startsWith("$2a$") || datos.password.startsWith("$2b$") -> datos.password
            else -> passwordEncoder.encode(datos.password)!!
        }

        val actualizado = existente.copy(
            nombre = datos.nombre,
            correo = datos.correo,
            password = passwordFinal,
            rol = existente.rol,
            foto = datos.foto
        )
        val guardado = usuarioRepository.save(actualizado)
        logger.info("Usuario con ID: {} actualizado correctamente", id)
        return guardado
    }

    fun eliminar(id: Int): Boolean {
        logger.info("Intentando eliminar usuario con ID: {}", id)
        if (!usuarioRepository.existsById(id)) {
            logger.warn("Eliminación fallida: no existe usuario con ID: {}", id)
            return false
        }
        usuarioRepository.deleteById(id)
        logger.info("Usuario con ID: {} eliminado correctamente", id)
        return true
    }
}