package com.example.msusuarios.config

import com.example.msusuarios.model.Rol
import com.example.msusuarios.model.Usuario
import com.example.msusuarios.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
class DataInitializer(
    @Value("\${INSPECTORIA_NOMBRE:Inspectoría General}") private val nombreInspectoria: String,
    @Value("\${INSPECTORIA_CORREO:}") private val correoInspectoria: String,
    @Value("\${INSPECTORIA_PASSWORD:}") private val passwordInspectoria: String
) {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    @Bean
    fun inicializarDatos(usuarioRepository: UsuarioRepository) = CommandLineRunner {
        if (correoInspectoria.isBlank() || passwordInspectoria.isBlank()) {
            logger.warn("Variables INSPECTORIA_CORREO o INSPECTORIA_PASSWORD no definidas. Omitiendo creación de cuenta inicial.")
            return@CommandLineRunner
        }

        if (!usuarioRepository.existsByCorreo(correoInspectoria)) {
            val passwordEncoder = BCryptPasswordEncoder()
            val cuentaInspectoria = Usuario(
                nombre = nombreInspectoria,
                correo = correoInspectoria,
                password = passwordEncoder.encode(passwordInspectoria)!!,
                rol = Rol.INSPECTORIA,
                foto = null
            )
            usuarioRepository.save(cuentaInspectoria)
            logger.info("Cuenta inicial de Inspectoría creada exitosamente.")
        } else {
            logger.info("La cuenta inicial de Inspectoría ya existe en la base de datos.")
        }
    }
}