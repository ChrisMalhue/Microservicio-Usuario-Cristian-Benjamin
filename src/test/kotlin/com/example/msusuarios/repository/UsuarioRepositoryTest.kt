package com.example.msusuarios.repository

import com.example.msusuarios.model.Rol
import com.example.msusuarios.model.Usuario
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pruebas de integración del repositorio: usan una base H2 real, pero en memoria
 * (equivalente a la SQLite en memoria de ProductoDaoInstrumentedTest en el ejemplo de clase).
 * No se conectan a Neon. Cada prueba corre dentro de una transacción que se revierte al terminar.
 */
@DataJpaTest
class UsuarioRepositoryTest(
    @Autowired private val repository: UsuarioRepository
) {

    /** Usuario ficticio, sin datos reales. */
    private fun usuario(
        correo: String = "ana@colegio.cl",
        nombre: String = "Ana Docente",
        rol: Rol = Rol.PROFESOR,
        foto: String? = null
    ) = Usuario(nombre = nombre, correo = correo, password = "Clave123", rol = rol, foto = foto)

    @BeforeEach
    fun partirConTablaVacia() {
        // Por si algún dato inicial se hubiera cargado, cada prueba parte desde cero.
        repository.deleteAll()
    }

    @Test
    fun guardarUsuarioLeAsignaUnId() {
        // Arrange
        val nuevo = usuario()
        // Act
        val guardado = repository.save(nuevo)
        // Assert
        assertNotNull(guardado.id)
    }

    @Test
    fun buscarPorCorreoExistenteDevuelveElUsuario() {
        // Arrange
        repository.save(usuario(correo = "ana@colegio.cl", nombre = "Ana Docente"))
        // Act
        val encontrado = repository.findByCorreo("ana@colegio.cl")
        // Assert
        assertEquals("Ana Docente", encontrado?.nombre)
    }

    @Test
    fun buscarPorCorreoInexistenteDevuelveNull() {
        // Act
        val encontrado = repository.findByCorreo("nadie@colegio.cl")
        // Assert
        assertNull(encontrado)
    }

    @Test
    fun existsByCorreoEsTrueSiElCorreoEstaGuardado() {
        // Arrange
        repository.save(usuario(correo = "ana@colegio.cl"))
        // Act + Assert
        assertTrue(repository.existsByCorreo("ana@colegio.cl"))
    }

    @Test
    fun existsByCorreoEsFalseSiElCorreoNoEstaGuardado() {
        // Act + Assert
        assertFalse(repository.existsByCorreo("nadie@colegio.cl"))
    }

    @Test
    fun noPermiteDosUsuariosConElMismoCorreo() {
        // Arrange
        repository.saveAndFlush(usuario(correo = "ana@colegio.cl"))
        // Act + Assert: la restricción única de la tabla rechaza el duplicado
        assertFailsWith<DataIntegrityViolationException> {
            repository.saveAndFlush(usuario(correo = "ana@colegio.cl", nombre = "Otra Persona"))
        }
    }

    @Test
    fun elRolSeGuardaYSeLeeCorrectamente() {
        // Arrange
        val guardado = repository.saveAndFlush(usuario(rol = Rol.INSPECTORIA))
        // Act
        val leido = repository.findById(guardado.id!!).orElse(null)
        // Assert
        assertEquals(Rol.INSPECTORIA, leido?.rol)
    }

    @Test
    fun unaFotoLargaSeGuardaCompleta() {
        // Arrange: la foto se almacena como texto largo (base64), por eso se prueba con 10.000 caracteres
        val fotoLarga = "A".repeat(10_000)
        val guardado = repository.saveAndFlush(usuario(foto = fotoLarga))
        // Act
        val leido = repository.findById(guardado.id!!).orElse(null)
        // Assert
        assertEquals(fotoLarga, leido?.foto)
    }

    @Test
    fun findAllDevuelveLosUsuariosGuardados() {
        // Arrange
        repository.save(usuario(correo = "ana@colegio.cl"))
        repository.save(usuario(correo = "luis@colegio.cl"))
        // Act
        val correos = repository.findAll().map { it.correo }
        // Assert
        assertEquals(2, correos.size)
        assertTrue(correos.containsAll(listOf("ana@colegio.cl", "luis@colegio.cl")))
    }

    @Test
    fun eliminarPorIdBorraElUsuario() {
        // Arrange
        val guardado = repository.save(usuario())
        // Act
        repository.deleteById(guardado.id!!)
        // Assert
        assertFalse(repository.existsById(guardado.id!!))
        assertNull(repository.findByCorreo("ana@colegio.cl"))
    }
}