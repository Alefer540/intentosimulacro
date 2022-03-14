package com.example.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.security.MessageDigest
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@RestController
class RequestController (
    private val mensajeRepository: MensajeRepository,
    private val usuarioRepository: UsuarioRepository,
    private val adminsRepository:AdminsRepository
    ) {
    //CREAR USUARIO
    // Debe haber una request que permita recibir un usuario (nombre y contraseña). Como respuesta deberá devolver un id de usuario y una clave de cifrado generada
    // específicamente para ese usuario. La clave de cifrado debe generarse de aleatoria y estar compuesta por 20 dígitos. En caso de que el usuario exista y
    // la contraseña no sea valida, deberá devolver un error que contendrá los siguientes campos: CODIGO_ERROR = 1, MOTIVO = "Contraseña inválida".
    //curl --request POST  --header "Content-type:application/json; charset=utf-8" --data "{\"nombre\":\"Alexito\",\"pass\":\"123\"}" localhost:8083/crearUsuario
    @PostMapping("crearUsuario")

    fun registrarUsuario(@RequestBody datos: Usuario): Any? {
        usuarioRepository.findAll().forEach {
            if (it.nombre == datos.nombre) {
                if (it.pass == datos.pass) {
                    return it.cifrado
                } else {
                    return Error(1, "Pass invalida")
                }
            }
        }
        usuarioRepository.save(datos)
        return datos.cifrado
    }

    //CREAR MENSAJE
    // Debe haber una request que reciba un mensaje (texto cifrado y el nombre del usuario que lo ha publicado). Atención, la clave de cifrado NO DEBE enviarse de nuevo.
    // En caso de que el usuario no exista, deberá devolver CODIGO_ERROR = 2, MOTIVO = "Usuario inexistente".
    //curl --request POST  --header "Content-type:application/json" --data "{\"texto\":\"Holaquetal\",\"usuarioId\":\"Alexito\",\"id\":0}" localhost:8083/crearMensaje
    //curl --request POST  --header "Content-type:application/json" --data "{\"texto\":\"pepepepe\",\"usuarioId\":\"Alexito\",\"id\":0}" localhost:8083/crearMensaje
    @PostMapping("crearMensaje")

    fun crearMensaje(@RequestBody datos:Mensaje):Any?{
        usuarioRepository.findAll().forEach {
            if(it.nombre==datos.usuarioId){
                mensajeRepository.save(datos)
                return "Success"
            }
        }
        return Error(2,"Usuario Inexistente")
    }
 //DESCARGAR TODOS LOS MENSAJES Y METERLOS EN UNA LISTA
//curl -v localhost:8083/descargarMensajes
    @GetMapping("descargarMensajes")
    fun descargarMensaje():Any{
        vaciarLista()
        mensajeRepository.findAll().forEach {
            Lista.list.add(it)
        }
        return Lista
    }

    //DESCARGAR MENSAJES FILTRADOS
    //Descargar todos los mensajes que contengan un texto especificado por la request.
//curl --request GET  --header "Content-type:application/json" --data "Hola" localhost:8083/descargarMensajesFiltrados
    @GetMapping("descargarMensajesFiltrados")
    fun descargarMensajesFiltrados(@RequestBody palabraBuscar:String):Any{
        vaciarLista()
        mensajeRepository.findAll().forEach {
            if(it.texto.contains(palabraBuscar))
                Lista.list.add(it)
        }
        return Lista
//) Enviando las credenciales de un usuario "administrador" compuesto por un nombre de usuario y una contraseña válida (ese usuario administrador debe crearse al iniciarse el servidor y guardarse en la base de datos)
    // obtener una lista con todos los usuarios y todas las contraseñas de cifrado de todos los usuarios. En caso de que la contraseña sea incorrecta,
    // deberá recibir: CODIGO_ERROR = 3, MOTIVO = "Contraseña de administrador inválida".
//curl --request GET  --header "Content-type:application/json" --data "{\"Nombre\":\"DAM2\",\"Pass\":\"123456\"}" localhost:8083/obtenerMensajesYLlaves
    }
    @GetMapping("obtenerMensajesYLlaves")
    fun obtenerMensajesYLlaves(@RequestBody admin: Admin):Any{
        vaciarLista()
        adminsRepository.findAll().forEach {
            if (it.Nombre == admin.Nombre) {
                if (it.Pass == admin.Pass) {
                    mensajeRepository.findAll().forEach { mensaje ->
                        val claveCifrado = usuarioRepository.getById(mensaje.usuarioId).cifrado
                        Lista.list.add(MensajesYLlaves(mensaje, claveCifrado))
                    }
                    return Lista
                }
            }
        }
        return Error(3, "Pass de administrador incorrecta")
    }
//EJEMPLO DE MENSAJES DESCIFRADOS
    @GetMapping("obtenerMensajesDescifrados")
    fun obtenerMensajesDescifrados(@RequestBody admin: Admin): Any {

        adminsRepository.findAll().forEach { admin ->
            if (admin.Nombre == admin.Nombre) {
                if (admin.Pass == admin.Pass) {
                    mensajeRepository.findAll().forEach { mensaje ->
                        val textoDescifrado: String = try {
                            descifrar(mensaje.texto, usuarioRepository.getById(mensaje.usuarioId).cifrado)
                        } catch (e: Exception) {
                            return "Texto indescifrable"
                        }
                        mensaje.texto = textoDescifrado
                        Lista.list.add(mensaje)
                    }
                    return listOf(Lista)
                }
            }
        }
        return listOf(Error(3, "Pass de administrador incorrecta"))
    }

    @Throws(BadPaddingException::class)
    private fun descifrar(textoCifradoYEncodado: String, llaveEnString: String): String {
        val type = "AES/ECB/PKCS5Padding"
        println("Voy a descifrar $textoCifradoYEncodado")
        val cipher = Cipher.getInstance(type)
        cipher.init(Cipher.DECRYPT_MODE, getKey(llaveEnString))
        val textCifradoYDencodado = Base64.getUrlDecoder().decode(textoCifradoYEncodado)
        println("Texto cifrado $textCifradoYDencodado")
        val textDescifradoYDesencodado = String(cipher.doFinal(textCifradoYDencodado))
        println("Texto cifrado y desencodado $textDescifradoYDesencodado")
        return textDescifradoYDesencodado
    }

    private fun getKey(llaveEnString: String): SecretKeySpec {
        var llaveUtf8 = llaveEnString.toByteArray(Charsets.UTF_8)
        val sha = MessageDigest.getInstance("SHA-1")
        llaveUtf8 = sha.digest(llaveUtf8)
        llaveUtf8 = llaveUtf8.copyOf(16)
        return SecretKeySpec(llaveUtf8, "AES")
    }


    private fun cifrar(textoEnString : String, llaveEnString : String) : String {
        val type = "AES/ECB/PKCS5Padding"
        println("Voy a cifrar: $textoEnString")
        val cipher = Cipher.getInstance(type)
        cipher.init(Cipher.ENCRYPT_MODE, getKey(llaveEnString))
        val textCifrado = cipher.doFinal(textoEnString.toByteArray(Charsets.UTF_8))
        println("Texto cifrado $textCifrado")
        val textCifradoYEncodado = Base64.getUrlEncoder().encodeToString(textCifrado)
        println("Texto cifrado y encodado $textCifradoYEncodado")
        return textCifradoYEncodado
        //return textCifrado.toString()
    }

    fun vaciarLista() {
        Lista.list.clear()
    }
}