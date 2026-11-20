package com.example.msusuarios.config

import com.example.msusuarios.model.Rol
import com.example.msusuarios.model.Usuario
import com.example.msusuarios.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataInitializer {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    @Bean
    fun inicializarDatos(usuarioRepository: UsuarioRepository) = CommandLineRunner {
        val correoInspectoria = "inspectoria@colegio.cl"

        if (!usuarioRepository.existsByCorreo(correoInspectoria)) {
            val cuentaInspectoria = Usuario(
                nombre = "Inspectoría General",
                correo = correoInspectoria,
                password = "Admin123!",
                rol = Rol.INSPECTORIA,
                foto = null
            )
            usuarioRepository.save(cuentaInspectoria)
            logger.info("Cuenta inicial de Inspectoría creada exitosamente: {}", correoInspectoria)
        } else {
            logger.info("La cuenta de Inspectoría ({}) ya existe en la base de datos.", correoInspectoria)
        }
    }
}