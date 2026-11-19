package com.example.msusuarios.controller

import com.example.msusuarios.model.Usuario
import com.example.msusuarios.service.UsuarioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = ["*"])
class UsuarioController(
    private val usuarioService: UsuarioService
) {

    @PostMapping("/registro")
    fun registrar(@RequestBody usuario: Usuario): ResponseEntity<Any> {
        val nuevoUsuario = usuarioService.registrar(usuario)
            ?: return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(mapOf("mensaje" to "El correo ya está registrado"))

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario)
    }

    @PostMapping("/login")
    fun login(@RequestBody credenciales: Map<String, String>): ResponseEntity<Any> {
        val correo = credenciales["correo"] ?: ""
        val password = credenciales["password"] ?: ""

        val usuario = usuarioService.login(correo, password)
        return if (usuario != null) {
            ResponseEntity.ok(usuario)
        } else {
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("mensaje" to "Correo o contraseña incorrectos"))
        }
    }

    @GetMapping
    fun listarTodos(): ResponseEntity<List<Usuario>> =
        ResponseEntity.ok(usuarioService.listarTodos())

    @GetMapping("/{id}")
    fun obtenerPorId(@PathVariable id: Int): ResponseEntity<Usuario> {
        val usuario = usuarioService.obtenerPorId(id)
        return if (usuario != null) ResponseEntity.ok(usuario) else ResponseEntity.notFound().build()
    }

    @GetMapping("/correo/{correo}")
    fun obtenerPorCorreo(@PathVariable correo: String): ResponseEntity<Usuario> {
        val usuario = usuarioService.obtenerPorCorreo(correo)
        return if (usuario != null) ResponseEntity.ok(usuario) else ResponseEntity.notFound().build()
    }

    @PutMapping("/{id}")
    fun actualizar(@PathVariable id: Int, @RequestBody datos: Usuario): ResponseEntity<Usuario> {
        val actualizado = usuarioService.actualizar(id, datos)
        return if (actualizado != null) ResponseEntity.ok(actualizado) else ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Int): ResponseEntity<Void> {
        return if (usuarioService.eliminar(id)) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }
}