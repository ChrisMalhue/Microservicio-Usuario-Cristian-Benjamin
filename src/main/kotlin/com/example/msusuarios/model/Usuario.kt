package com.example.msusuarios.model

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

    @Column(nullable = false)
    val password: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val rol: Rol = Rol.PROFESOR,

    @Column(columnDefinition = "TEXT")
    val foto: String? = null
)