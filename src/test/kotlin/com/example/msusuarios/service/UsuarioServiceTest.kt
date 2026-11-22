package com.example.msusuarios.service

import com.example.msusuarios.model.Rol
import com.example.msusuarios.model.Usuario
import com.example.msusuarios.repository.UsuarioRepository
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pruebas unitarias de UsuarioService.
 * El repositorio se simula con MockK (equivalente al FakeProductoDao del ejemplo de clase),
 * por lo que no se usa Spring ni la base de datos de Neon.
 */
class UsuarioServiceTest {

    // JUnit 5 crea una instancia nueva de esta clase por cada @Test,
    // así que cada prueba parte con un mock limpio.
    private val repository = mockk<UsuarioRepository>()
    private val service = UsuarioService(repository)

    /** Usuario ficticio, sin datos reales. */
    private fun usuario(
        id: Int? = 1,
        nombre: String = "Ana Docente",
        correo: String = "ana@colegio.cl",
        password: String = "Clave123",
        rol: Rol = Rol.PROFESOR
    ) = Usuario(id = id, nombre = nombre, correo = correo, password = password, rol = rol, foto = null)

    // ---------------------------------------------------------------- registrar

    @Test
    fun registraUsuarioConCorreoNuevo() {
        // Arrange
        val nuevo = usuario(id = null)
        every { repository.existsByCorreo("ana@colegio.cl") } returns false
        every { repository.save(any<Usuario>()) } answers { firstArg<Usuario>().copy(id = 7) }
        // Act
        val resultado = service.registrar(nuevo)
        // Assert
        assertNotNull(resultado)
        assertEquals(7, resultado.id)
        assertEquals("ana@colegio.cl", resultado.correo)
        verify(exactly = 1) { repository.save(any<Usuario>()) }
    }

    @Test
    fun registrarConCorreoExistenteDevuelveNullYNoGuarda() {
        // Arrange
        every { repository.existsByCorreo("ana@colegio.cl") } returns true
        // Act
        val resultado = service.registrar(usuario(id = null))
        // Assert
        assertNull(resultado)
        verify(exactly = 0) { repository.save(any<Usuario>()) }
    }

    @Test
    fun registrarIgnoraElIdRecibido() {
        // Arrange
        val guardado = slot<Usuario>()
        every { repository.existsByCorreo(any()) } returns false
        every { repository.save(capture(guardado)) } answers { guardado.captured.copy(id = 1) }
        // Act
        service.registrar(usuario(id = 99))
        // Assert
        assertNull(guardado.captured.id)
    }

    @Test
    fun registrarFuerzaElRolProfesor() {
        // Arrange: alguien intenta registrarse como Inspectoría
        val guardado = slot<Usuario>()
        every { repository.existsByCorreo(any()) } returns false
        every { repository.save(capture(guardado)) } answers { guardado.captured.copy(id = 1) }
        // Act
        val resultado = service.registrar(usuario(id = null, rol = Rol.INSPECTORIA))
        // Assert
        assertEquals(Rol.PROFESOR, guardado.captured.rol)
        assertEquals(Rol.PROFESOR, resultado?.rol)
    }

    // -------------------------------------------------------------------- login

    @Test
    fun loginCorrectoDevuelveElUsuario() {
        // Arrange
        val existente = usuario()
        every { repository.findByCorreo("ana@colegio.cl") } returns existente
        // Act
        val resultado = service.login("ana@colegio.cl", "Clave123")
        // Assert
        assertEquals(existente, resultado)
    }

    @Test
    fun loginConPasswordIncorrectaDevuelveNull() {
        // Arrange
        every { repository.findByCorreo("ana@colegio.cl") } returns usuario()
        // Act
        val resultado = service.login("ana@colegio.cl", "otraClave")
        // Assert
        assertNull(resultado)
    }

    @Test
    fun loginConCorreoInexistenteDevuelveNull() {
        // Arrange
        every { repository.findByCorreo("nadie@colegio.cl") } returns null
        // Act
        val resultado = service.login("nadie@colegio.cl", "Clave123")
        // Assert
        assertNull(resultado)
    }

    // ------------------------------------------------------------- listarTodos

    @Test
    fun listarTodosDevuelveTodosLosUsuarios() {
        // Arrange
        every { repository.findAll() } returns listOf(
            usuario(id = 1),
            usuario(id = 2, correo = "luis@colegio.cl")
        )
        // Act
        val resultado = service.listarTodos()
        // Assert
        assertEquals(2, resultado.size)
    }

    @Test
    fun listarTodosSinUsuariosDevuelveListaVacia() {
        // Arrange
        every { repository.findAll() } returns emptyList()
        // Act
        val resultado = service.listarTodos()
        // Assert
        assertTrue(resultado.isEmpty())
    }

    // ------------------------------------------------------------- obtenerPorId

    @Test
    fun obtenerPorIdExistenteDevuelveElUsuario() {
        // Arrange
        val existente = usuario(id = 1)
        every { repository.findById(1) } returns Optional.of(existente)
        // Act
        val resultado = service.obtenerPorId(1)
        // Assert
        assertEquals(existente, resultado)
    }

    @Test
    fun obtenerPorIdInexistenteDevuelveNull() {
        // Arrange
        every { repository.findById(999) } returns Optional.empty()
        // Act
        val resultado = service.obtenerPorId(999)
        // Assert
        assertNull(resultado)
    }

    // -------------------------------------------------------- obtenerPorCorreo

    @Test
    fun obtenerPorCorreoExistenteDevuelveElUsuario() {
        // Arrange
        val existente = usuario()
        every { repository.findByCorreo("ana@colegio.cl") } returns existente
        // Act
        val resultado = service.obtenerPorCorreo("ana@colegio.cl")
        // Assert
        assertEquals(existente, resultado)
    }

    @Test
    fun obtenerPorCorreoInexistenteDevuelveNull() {
        // Arrange
        every { repository.findByCorreo("nadie@colegio.cl") } returns null
        // Act
        val resultado = service.obtenerPorCorreo("nadie@colegio.cl")
        // Assert
        assertNull(resultado)
    }

    // --------------------------------------------------------------- actualizar

    @Test
    fun actualizarCambiaLosDatosYConservaElId() {
        // Arrange
        every { repository.findById(1) } returns Optional.of(usuario(id = 1))
        every { repository.save(any<Usuario>()) } answers { firstArg<Usuario>() }
        val datos = usuario(id = null, nombre = "Ana Actualizada", password = "NuevaClave1")
        // Act
        val resultado = service.actualizar(1, datos)
        // Assert
        assertNotNull(resultado)
        assertEquals(1, resultado.id)
        assertEquals("Ana Actualizada", resultado.nombre)
        assertEquals("NuevaClave1", resultado.password)
    }

    @Test
    fun actualizarUsuarioInexistenteDevuelveNullYNoGuarda() {
        // Arrange
        every { repository.findById(999) } returns Optional.empty()
        // Act
        val resultado = service.actualizar(999, usuario(id = null))
        // Assert
        assertNull(resultado)
        verify(exactly = 0) { repository.save(any<Usuario>()) }
    }

    @Test
    fun actualizarConCorreoDeOtroUsuarioLanzaExcepcionYNoGuarda() {
        // Arrange
        every { repository.findById(1) } returns Optional.of(usuario(id = 1))
        every { repository.existsByCorreo("luis@colegio.cl") } returns true
        val datos = usuario(id = null, correo = "luis@colegio.cl")
        // Act + Assert
        assertFailsWith<CorreoDuplicadoException> { service.actualizar(1, datos) }
        verify(exactly = 0) { repository.save(any<Usuario>()) }
    }

    @Test
    fun actualizarConCorreoNuevoYLibreFunciona() {
        // Arrange
        every { repository.findById(1) } returns Optional.of(usuario(id = 1))
        every { repository.existsByCorreo("ana.nueva@colegio.cl") } returns false
        every { repository.save(any<Usuario>()) } answers { firstArg<Usuario>() }
        val datos = usuario(id = null, correo = "ana.nueva@colegio.cl")
        // Act
        val resultado = service.actualizar(1, datos)
        // Assert
        assertEquals("ana.nueva@colegio.cl", resultado?.correo)
    }

    @Test
    fun actualizarConElMismoCorreoNoConsultaDuplicados() {
        // Arrange: el usuario guarda su perfil sin cambiar el correo
        every { repository.findById(1) } returns Optional.of(usuario(id = 1))
        every { repository.save(any<Usuario>()) } answers { firstArg<Usuario>() }
        // Act
        service.actualizar(1, usuario(id = null, nombre = "Otro Nombre"))
        // Assert
        verify(exactly = 0) { repository.existsByCorreo(any()) }
    }

    @Test
    fun actualizarNoPermiteCambiarElRol() {
        // Arrange: un profesor intenta ascenderse a Inspectoría
        every { repository.findById(1) } returns Optional.of(usuario(id = 1, rol = Rol.PROFESOR))
        every { repository.save(any<Usuario>()) } answers { firstArg<Usuario>() }
        // Act
        val resultado = service.actualizar(1, usuario(id = null, rol = Rol.INSPECTORIA))
        // Assert
        assertEquals(Rol.PROFESOR, resultado?.rol)
    }

    // ----------------------------------------------------------------- eliminar

    @Test
    fun eliminarUsuarioExistenteDevuelveTrueYLoBorra() {
        // Arrange
        every { repository.existsById(1) } returns true
        justRun { repository.deleteById(1) }
        // Act
        val resultado = service.eliminar(1)
        // Assert
        assertTrue(resultado)
        verify(exactly = 1) { repository.deleteById(1) }
    }

    @Test
    fun eliminarUsuarioInexistenteDevuelveFalseYNoBorra() {
        // Arrange
        every { repository.existsById(999) } returns false
        // Act
        val resultado = service.eliminar(999)
        // Assert
        assertFalse(resultado)
        verify(exactly = 0) { repository.deleteById(any()) }
    }
}