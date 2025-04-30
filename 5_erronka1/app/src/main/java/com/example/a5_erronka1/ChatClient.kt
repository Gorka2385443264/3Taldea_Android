// ChatClient.kt
package com.example.a5_erronka1

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

object ChatClient {
    private var socket: Socket? = null
    private var input: BufferedReader? = null
    private var output: PrintWriter? = null

    suspend fun connect(onConnected: (Boolean) -> Unit) = withContext(Dispatchers.IO) {
        try {
            socket = Socket("192.168.115.154", 5555)
            input = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            output = PrintWriter(socket!!.getOutputStream(), true)
            withContext(Dispatchers.Main) { onConnected(true) }
        } catch (e: Exception) {
            Log.e("ChatClient", "Conexión fallida: ${e.message}")
            withContext(Dispatchers.Main) { onConnected(false) }
        }
    }

    // Envía texto: cifra solo el mensaje (igual que JavaFX/C#)
    suspend fun sendText(username: String, plainMessage: String) = withContext(Dispatchers.IO) {
        try {
            val encrypted = EncryptionUtils.encrypt(plainMessage)
            val protocolo = "$username: $encrypted"
            Log.d("ChatClient", "Enviando texto cifrado -> $protocolo")
            output?.println(protocolo)
        } catch (e: Exception) {
            Log.e("ChatClient", "Error enviando texto: ${e.message}")
        }
    }

    // Envía ya-formateado (para imágenes)
    suspend fun sendRaw(protocolo: String) = withContext(Dispatchers.IO) {
        output?.println(protocolo)
    }

    // Escucha y distingue ambos protocolos
    suspend fun listenForMessages(onMessage: (tipo: String, contenido: String) -> Unit) =
        withContext(Dispatchers.IO) {
            try {
                while (true) {
                    val line = input?.readLine() ?: break
                    when {
                        line.startsWith("IMG_FILE:") -> {
                            onMessage("imagen", line)
                        }
                        else -> {
                            // formato esperado "usuario: base64…"
                            val parts = line.split(": ", limit = 2)
                            if (parts.size == 2) {
                                val user = parts[0]
                                val cipher = parts[1]
                                val plain = try {
                                    EncryptionUtils.decrypt(cipher)
                                } catch (e: Exception) {
                                    "[ERROR_DESCIFRADO]"
                                }
                                onMessage("texto", "$user: $plain")
                            } else {
                                onMessage("texto", line)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatClient", "Error recibiendo: ${e.message}")
            }
        }

    fun disconnect() {
        try {
            input?.close(); output?.close(); socket?.close()
        } catch (_: Exception) { }
    }
}
