package com.example.msusuarios.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*

@Entity
@Table(
    name = "usuarios",
    uniqueConstraints = [UniqueConstraint(columnNames = ["correo"])]
)
data class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false)
    val nombre: String = "",

    @Column(nullable = false, unique = true)
    val correo: String = "",

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    val password: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val rol: Rol = Rol.PROFESOR,

    @Column(columnDefinition = "TEXT")
    val foto: String? = null
)