package com.example.a5_erronka1

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

object ChatClient {
    private var socket: Socket? = null
    private var input: BufferedReader? = null
    private var output: PrintWriter? = null
    var izena: String? = null

    suspend fun connect(onConnected: (Boolean) -> Unit, izena:String) {
        setIzena(izena);
        withContext(Dispatchers.IO) {
            try {
                socket = Socket("192.168.115.155", 5555)
                input = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                output = PrintWriter(socket!!.getOutputStream(), true)
                withContext(Dispatchers.Main) {
                    onConnected(true)
                }
            } catch (e: IOException) {
                Log.e("ChatClient", "Error al conectar: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onConnected(false)
                }
            }
        }
    }

    suspend fun setIzena(izena: String){
        this.izena = izena
    }

    suspend fun sendMessage(message: String) {
        withContext(Dispatchers.IO) {
            try {
                output?.println(message)
            } catch (e: IOException) {
                Log.e("ChatClient", "Error al enviar mensaje: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    suspend fun listenForMessages(onMessageReceived: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                while (true) {
                    val message = input?.readLine() ?: break
                    withContext(Dispatchers.Main) {
                        onMessageReceived(message)
                    }
                }
            } catch (e: IOException) {
                Log.e("ChatClient", "Error al leer mensajes: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun disconnect() {
        try {
            input?.close()
            output?.close()
            socket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}