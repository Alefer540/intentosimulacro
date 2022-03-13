package com.example.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class RequestController (private val mensajeRepository: MensajeRepository, private val usuarioRepository: UsuarioRepository) {


    //curl --request POST  --header "Content-type:application/json; charset=utf-8" --data "{\"usuario\":\"Alexito\",\"pass\":\"123\"}" localhost:8083/crearUsuario
    @PostMapping("crearUsuario")
    fun registrarUsuario(@RequestBody datos:Usuario):Any? {
        val nuevo = Usuario(datos.usuario, datos.pass, token = generarClaveAleatoria())


        if (nuevo.usuario == datos.usuario) {
            if (nuevo.pass == datos.pass) {

            } else {
                val error = Error(1, "contraseña erronea")
                return error
            }
        } else {
            usuarioRepository.save(nuevo)
        }
       return nuevo.token
    }
    private fun generarClaveAleatoria(): String {
        var token = ""
        repeat(3){token += (0..9).random()}
        return token
    }
}