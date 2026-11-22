package com.example.msusuarios.service

class CorreoDuplicadoException(correo: String) :
    RuntimeException("El correo $correo ya está registrado")