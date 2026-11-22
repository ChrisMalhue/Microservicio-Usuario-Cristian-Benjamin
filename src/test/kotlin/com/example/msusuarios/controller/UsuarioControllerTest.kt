package com.example.msusuarios.controller

import com.example.msusuarios.model.Rol
import com.example.msusuarios.model.Usuario
import com.example.msusuarios.service.CorreoDuplicadoException
import com.example.msusuarios.service.UsuarioService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * Pruebas del controller: verifican que cada endpoint responde con el código HTTP correcto.
 * El servicio se simula con MockK y no se levanta Spring completo (MockMvc en modo "standalone").
 */
class UsuarioControllerTest {

    private val service = mockk<UsuarioService>()
    private val mockMvc: MockMvc =
        MockMvcBuilders.standaloneSetup(UsuarioController(service)).build()

    /** Usuario ficticio, sin datos reales. */
    private fun usuario(id: Int? = 1, correo: String = "ana@colegio.cl") = Usuario(
        id = id, nombre = "Ana Docente", correo = correo,
        password = "Clave123", rol = Rol.PROFESOR, foto = null
    )

    private val jsonUsuario =
        """{"nombre":"Ana Docente","correo":"ana@colegio.cl","password":"Clave123","rol":"PROFESOR"}"""

    // ----------------------------------------------------------------- registro

    @Test
    fun registroExitosoResponde201ConElUsuario() {
        // Arrange
        every { service.registrar(any()) } returns usuario(id = 1)
        // Act + Assert
        mockMvc.perform(
            post("/api/usuarios/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUsuario)
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("\$.id").value(1))
            .andExpect(jsonPath("\$.correo").value("ana@colegio.cl"))
    }

    @Test
    fun registroConCorreoExistenteResponde409ConMensaje() {
        // Arrange
        every { service.registrar(any()) } returns null
        // Act + Assert
        mockMvc.perform(
            post("/api/usuarios/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUsuario)
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("\$.mensaje").value("El correo ya está registrado"))
    }

    // -------------------------------------------------------------------- login

    @Test
    fun loginCorrectoResponde200ConElUsuario() {
        // Arrange
        every { service.login("ana@colegio.cl", "Clave123") } returns usuario()
        // Act + Assert
        mockMvc.perform(
            post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"correo":"ana@colegio.cl","password":"Clave123"}""")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$.correo").value("ana@colegio.cl"))
    }

    @Test
    fun loginConCredencialesIncorrectasResponde401ConMensaje() {
        // Arrange
        every { service.login(any(), any()) } returns null
        // Act + Assert
        mockMvc.perform(
            post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"correo":"ana@colegio.cl","password":"mala"}""")
        )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("\$.mensaje").value("Correo o contraseña incorrectos"))
    }

    @Test
    fun loginSinCamposUsaTextoVacioYResponde401() {
        // Arrange
        every { service.login("", "") } returns null
        // Act + Assert
        mockMvc.perform(
            post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isUnauthorized())
        verify(exactly = 1) { service.login("", "") }
    }

    // ------------------------------------------------------------------- listar

    @Test
    fun listarTodosResponde200ConLaLista() {
        // Arrange
        every { service.listarTodos() } returns listOf(
            usuario(id = 1),
            usuario(id = 2, correo = "luis@colegio.cl")
        )
        // Act + Assert
        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$.length()").value(2))
    }

    // ------------------------------------------------------------- obtener por id

    @Test
    fun obtenerPorIdExistenteResponde200() {
        // Arrange
        every { service.obtenerPorId(1) } returns usuario(id = 1)
        // Act + Assert
        mockMvc.perform(get("/api/usuarios/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$.id").value(1))
    }

    @Test
    fun obtenerPorIdInexistenteResponde404() {
        // Arrange
        every { service.obtenerPorId(999) } returns null
        // Act + Assert
        mockMvc.perform(get("/api/usuarios/999"))
            .andExpect(status().isNotFound())
    }

    // ---------------------------------------------------------- obtener por correo

    @Test
    fun obtenerPorCorreoExistenteResponde200() {
        // Arrange
        every { service.obtenerPorCorreo("ana@colegio.cl") } returns usuario()
        // Act + Assert
        mockMvc.perform(get("/api/usuarios/correo/{correo}", "ana@colegio.cl"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$.correo").value("ana@colegio.cl"))
    }

    @Test
    fun obtenerPorCorreoInexistenteResponde404() {
        // Arrange
        every { service.obtenerPorCorreo("nadie@colegio.cl") } returns null
        // Act + Assert
        mockMvc.perform(get("/api/usuarios/correo/{correo}", "nadie@colegio.cl"))
            .andExpect(status().isNotFound())
    }

    // --------------------------------------------------------------- actualizar

    @Test
    fun actualizarExistenteResponde200ConElUsuarioActualizado() {
        // Arrange
        every { service.actualizar(1, any()) } returns usuario(id = 1)
        // Act + Assert
        mockMvc.perform(
            put("/api/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUsuario)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("\$.id").value(1))
    }

    @Test
    fun actualizarInexistenteResponde404() {
        // Arrange
        every { service.actualizar(999, any()) } returns null
        // Act + Assert
        mockMvc.perform(
            put("/api/usuarios/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUsuario)
        )
            .andExpect(status().isNotFound())
    }

    @Test
    fun actualizarConCorreoDeOtroUsuarioResponde409ConMensaje() {
        // Arrange
        every { service.actualizar(1, any()) } throws CorreoDuplicadoException("luis@colegio.cl")
        // Act + Assert
        mockMvc.perform(
            put("/api/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUsuario)
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("\$.mensaje").value("El correo ya está registrado"))
    }

    // ----------------------------------------------------------------- eliminar

    @Test
    fun eliminarExistenteResponde204() {
        // Arrange
        every { service.eliminar(1) } returns true
        // Act + Assert
        mockMvc.perform(delete("/api/usuarios/1"))
            .andExpect(status().isNoContent())
    }

    @Test
    fun eliminarInexistenteResponde404() {
        // Arrange
        every { service.eliminar(999) } returns false
        // Act + Assert
        mockMvc.perform(delete("/api/usuarios/999"))
            .andExpect(status().isNotFound())
    }
}