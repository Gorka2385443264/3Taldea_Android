package com.example.a5_erronka1

import com.example.a5_erronka1.ChatClient // no tocar
import android.content.Context
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import android.os.Bundle
import android.util.Log
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.a5_erronka1.ui.theme._5_erronka1Theme
import kotlinx.coroutines.Dispatchers
import org.json.JSONException
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import androidx.navigation.NavType
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    var username:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {//HOLA
            _5_erronka1Theme {
                val navController = rememberNavController() // Inicializar el controlador de navegación
                NavHost(navController = navController, startDestination = "pantallaPrincipal") {
                    composable("pantallaPrincipal") {
                        PantallaPrincipal(navController = navController)
                    }
                    composable("segundaPantalla") {
                        SegundaPantalla(navController = navController)
                    }
                    composable(
                        route = "pantallaMapa?username={username}",
                        arguments = listOf(navArgument("username") { defaultValue = "Usuario" })
                    ) { backStackEntry ->
                        val username = backStackEntry.arguments?.getString("username") ?: "Usuario"
                        PantallaMapa(navController = navController, username = username)
                    }
                    composable(
                        route = "pantallaMenu/{username}/{mesaSeleccionada}",
                        arguments = listOf(
                            navArgument("username") { defaultValue = "Usuario" },
                            navArgument("mesaSeleccionada") { defaultValue = "Mesa 1" }
                        )
                    ){ backStackEntry ->
                        username = backStackEntry.arguments?.getString("username") ?: "Usuario"
                        val mesaSeleccionada =
                            backStackEntry.arguments?.getString("mesaSeleccionada") ?: "Mesa 1"
                        PantallaMenu(
                            navController = navController,
                            username = username,
                            mesaSeleccionada = mesaSeleccionada
                        )
                    }
                    composable(
                        route = "pantallaFactura/{selectedItems}/{username}/{mesaSeleccionada}",
                        arguments = listOf(
                            navArgument("selectedItems") { type = NavType.StringType },
                            navArgument("username") { type = NavType.StringType },
                            navArgument("mesaSeleccionada") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val selectedItemsJson = backStackEntry.arguments?.getString("selectedItems") ?: "[]"
                        val username = backStackEntry.arguments?.getString("username") ?: ""
                        val mesaSeleccionada = backStackEntry.arguments?.getString("mesaSeleccionada") ?: ""
                        val selectedItems = try {
                            JSONArray(selectedItemsJson).let { jsonArray ->
                                val groupedItems = mutableMapOf<String, MutableMap<String, Any>>()
                                for (i in 0 until jsonArray.length()) {
                                    val item = jsonArray.getJSONObject(i)
                                    val nombre = item.getString("nombre")
                                    val cantidad = item.getInt("cantidad")
                                    val precio = item.getDouble("precio").toFloat()
                                    if (groupedItems.containsKey(nombre)) {
                                        groupedItems[nombre]?.let {
                                            it["cantidad"] = it["cantidad"] as Int + cantidad
                                        }
                                    } else {
                                        groupedItems[nombre] = mutableMapOf(
                                            "id" to item.getInt("id"),
                                            "nombre" to nombre,
                                            "cantidad" to cantidad,
                                            "precio" to precio,
                                            "nota" to ""
                                        )
                                    }
                                }
                                groupedItems.values.toList()
                            }
                        } catch (e: JSONException) {
                            emptyList<Map<String, Any>>()
                        }
                        PantallaFactura(
                            navController = navController,
                            selectedItems = selectedItems,
                            username = username,
                            mesaSeleccionada = mesaSeleccionada
                        )
                    }
                    // Pantalla de chat fuera de otro composable
                    composable(
                        route = "pantallaChat?username={username}",
                        arguments = listOf(navArgument("username") { defaultValue = "Usuario" })
                    ) { backStackEntry ->
                        val username = backStackEntry.arguments?.getString("username") ?: "Usuario"
                        PantallaChat(navController = navController, izena = username)
                    }

                    composable("pantallaEskaeras/{username}/{mesaSeleccionada}") { backStackEntry ->
                        val username = backStackEntry.arguments?.getString("username") ?: ""
                        val mesaSeleccionada = backStackEntry.arguments?.getString("mesaSeleccionada") ?: ""
                        NuevaPantallaEskaeras(navController, username, mesaSeleccionada)
                    }

                    composable(
                        route = "editarEskaera/{eskaeraZenb}/{mesaSeleccionada}/{eskaeraDetalles}/{username}", // Agregar username como parámetro
                        arguments = listOf(
                            navArgument("eskaeraZenb") { type = NavType.IntType },
                            navArgument("mesaSeleccionada") { type = NavType.StringType },
                            navArgument("eskaeraDetalles") { type = NavType.StringType },
                            navArgument("username") { type = NavType.StringType } // Agregar username como argumento
                        )
                    ) { backStackEntry ->
                        val eskaeraZenb = backStackEntry.arguments?.getInt("eskaeraZenb") ?: -1
                        val mesaSeleccionada = backStackEntry.arguments?.getString("mesaSeleccionada") ?: ""
                        val eskaeraDetallesString = backStackEntry.arguments?.getString("eskaeraDetalles") ?: ""
                        val eskaeraDetalles = try {
                            JSONObject(Uri.decode(eskaeraDetallesString))
                        } catch (e: Exception) {
                            JSONObject() // Objeto vacío en caso de error
                        }
                        val username = backStackEntry.arguments?.getString("username") ?: "" // Obtener username

                        if (eskaeraZenb != -1) {
                            PantallaEditarEskaera(
                                navController = navController,
                                eskaeraZenb = eskaeraZenb,
                                mesaSeleccionada = mesaSeleccionada,
                                eskaeraDetalles = eskaeraDetalles,
                                username = username // Pasar username a la función
                            )
                        } else {
                            Text("Error: No se pudo cargar la eskaera.")
                        }
                    }

                    composable(
                        route = "agregarPlato/{username}/{mesaSeleccionada}/{eskaeraZenb}",
                        arguments = listOf(
                            navArgument("username") { type = NavType.StringType },
                            navArgument("mesaSeleccionada") { type = NavType.StringType },
                            navArgument("eskaeraZenb") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val username = backStackEntry.arguments?.getString("username") ?: ""
                        val mesaSeleccionada = backStackEntry.arguments?.getString("mesaSeleccionada") ?: ""
                        val eskaeraZenb = backStackEntry.arguments?.getInt("eskaeraZenb") ?: -1

                        if (username.isNotEmpty() && mesaSeleccionada.isNotEmpty() && eskaeraZenb != -1) {
                            PantallaAgregarPlato(
                                navController = navController,
                                username = username,
                                mesaSeleccionada = mesaSeleccionada,
                                eskaeraZenb = eskaeraZenb
                            )
                        } else {
                            Text("Error: Datos incompletos")
                        }
                    }



                }
            }
        }
    }
}


@Composable
fun PantallaPrincipal(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_the_bull),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenedor principal con los elementos
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo de la empresa
            Image(
                painter = painterResource(id = R.drawable.saboreame),
                contentDescription = "Logo de la empresa",
                modifier = Modifier
                    .size(400.dp)
                    .padding(bottom = 32.dp)
            )

            // Botón de empezar
            Button(
                onClick = {
                    navController.navigate("segundaPantalla")
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B4513),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Hasi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaChat(izena: String, navController: NavController) {
    var message by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<String>()) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            ChatClient.connect({ success ->
                if (success) {
                    Log.d("MainActivity", "Conexión exitosa al servidor.")
                    coroutineScope.launch {
                        ChatClient.listenForMessages { newMessage ->
                            synchronized(chatMessages) {
                                chatMessages = chatMessages + newMessage
                            }
                        }
                    }
                } else {
                    Log.e("MainActivity", "No se pudo conectar al servidor.")
                }
            }, izena)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFEFEFEF)) // Color de fondo
    ) {
        // Panel de mensajes de chat
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            chatMessages.forEach { msg ->
                val sender = msg.substringBefore(">")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = if (sender.trim() == izena.trim()) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                color = if (sender.trim() == izena.trim()) Color(0xFFADD8E6) else Color(
                                    0xFFD3D3D3
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color.Black
                    )
                }
            }
        }

        // Campo de texto para escribir mensajes
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Escribe un mensaje") },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botón para enviar mensajes
        Button(
            onClick = {
                if (message.isNotEmpty()) {
                    coroutineScope.launch {
                        val formattedMessage = "$izena> $message"
                        ChatClient.sendMessage(formattedMessage)
                        withContext(Dispatchers.Main) {
                            synchronized(chatMessages) {
                                chatMessages = chatMessages + formattedMessage
                            }
                            message = "" // Limpiar el campo de mensaje
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors()
        ) {
            Text("Enviar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón "Atrás"
        Button(
            onClick = {
                navController.navigate("pantallaMapa?username=$izena")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Atrás", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SegundaPantalla(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Imagen de fondo
    Image(
        painter = painterResource(id = R.drawable.fondo_the_bull),
        contentDescription = "Fondo",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        //   verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Imagen del logo en la parte superior
        Image(
            painter = painterResource(id = R.drawable.saboreame), // Asegúrate de tener tu logo en res/drawable
            contentDescription = "Logo",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp) // Espacio debajo del logo
        )

        Text(
            text = "Saio hasi",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(75.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Pasahitza") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    iniciarSesion(email, password, navController)
                } else {
                    Toast.makeText(navController.context, "Por favor ingrese ambos campos", Toast.LENGTH_SHORT).show()
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
        ) {
            Text(text = "Saioa hasi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

    }
}

fun iniciarSesion(email: String, password: String, navController: NavController) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = OkHttpClient()
            val url = "http://10.0.2.2/login.php"

            val formBody = FormBody.Builder()
                .add("email", email)
                .add("pasahitza", password)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            Log.d("RespuestaServidor", "Response Body: $responseBody")

            if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.getBoolean("success")) {
                        val username = jsonResponse.getString("izena")
                        withContext(Dispatchers.Main) {
                            // Navegar a la pantalla del mapa con el nombre de usuario
                            navController.navigate("pantallaMapa?username=$username")
                        }
                    } else {
                        val errorMessage = jsonResponse.getString("message")
                        withContext(Dispatchers.Main) {
                            // Mostrar mensaje de error del servidor, por ejemplo: "Usuario desactivado"
                            Toast.makeText(navController.context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: JSONException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(navController.context, "Respuesta no válida del servidor", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(navController.context, "Error de conexión: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(navController.context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun PantallaMapa(navController: NavController, username: String) {
    var selectedMesa by remember { mutableStateOf<Int?>(null) }
    var mesas by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // Cargar el estado de las mesas al cargar la pantalla
    LaunchedEffect(true) {
        obtenerEstadoMesas(navController) { mesasRecibidas ->
            mesas = mesasRecibidas
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091725))
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_the_bull),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenido de la pantalla
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FILA CON EL LOGO Y EL MENSAJE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo más grande
                Image(
                    painter = painterResource(id = R.drawable.saboreame),
                    contentDescription = "Logo de la empresa",
                    modifier = Modifier
                        .size(150.dp) // Aumentar tamaño
                        .padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f)) // Para centrar el texto
                Text(
                    text = "Kaixo $username!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 28.dp)
                        .padding(bottom = 16.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }

            // Texto que muestra el número de mesa seleccionado
            if (selectedMesa != null) {
                Text(
                    text = "Nº$selectedMesa Mahaia aukeratu da.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Text(
                    text = "Oraindik ez da mahaia aukeratu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Aquí colocamos la imagen de mapeo que ocupará el espacio entre el logo y los botones
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ocupa todo el espacio disponible entre el logo y los botones
            ) {
                MapaDeMesas(selectedMesa, mesas) { mesaNumero ->
                    selectedMesa = mesaNumero
                }
            }
        }

        // Botones abajo
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart) // Alineación en la parte inferior izquierda
                .padding(16.dp)
        ) {
            // Botón "Atzera" a la izquierda
            Button(
                onClick = { navController.navigate("segundaPantalla") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
            ) {
                Text(text = "Atzera", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp)) // Espacio entre los botones

            // Botón "Txat" a la derecha del "Atzera"
            Button(
                onClick = {
                    verificarPermisoTxat(username) { tienePermiso ->
                        if (tienePermiso) {
                            navController.navigate("pantallaChat?username=$username")
                        } else {
                            Toast.makeText(
                                navController.context,
                                "No tienes permiso para acceder al chat.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)),
                modifier = Modifier.height(50.dp)
            ) {
                Text(
                    text = "Txat",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }


        }

        // Botón "Jarraitu" abajo a la derecha
        Button(
            onClick = {
                if (selectedMesa != null) {
                    // Redirigir a pantallaEskaeras con el username y selectedMesa
                    navController.navigate("pantallaEskaeras/$username/$selectedMesa")
                } else {
                    Toast.makeText(
                        navController.context,
                        "Por favor, selecciona una mesa antes de continuar.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)),
            modifier = Modifier
                .align(Alignment.BottomEnd) // Alineado abajo a la derecha
                .padding(16.dp)
        ) {
            Text(text = "Jarraitu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

    }
}

fun verificarPermisoTxat(username: String, onResult: (Boolean) -> Unit) {
    val url = "http://10.0.2.2/verificar_permiso_chat.php?username=$username"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val result = URL(url).readText()
            val json = JSONObject(result)
            val permiso = json.optInt("txatBaimena", 0)

            withContext(Dispatchers.Main) {
                onResult(permiso == 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult(false)
            }
        }
    }
}

@Composable
fun MapaDeMesas(selectedMesa: Int?, mesas: List<Map<String, Any>>, onMesaSelected: (Int) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val currentWidth = constraints.maxWidth
        val currentHeight = constraints.maxHeight

        val baseWidth = 912f
        val baseHeight = 1282f

        val scaleWidth = currentWidth / baseWidth
        val scaleHeight = currentHeight / baseHeight

        fun ajustarX(baseX: Float): Dp {
            return (baseX * scaleWidth).dp
        }

        fun ajustarY(baseY: Float): Dp {
            return (baseY * scaleHeight).dp
        }

        // Ajustamos el tamaño de la imagen (puedes modificar los valores de width y height)
        Image(
            painter = painterResource(id = R.drawable.mapeo),
            contentDescription = "Mapa del restaurante",
            modifier = Modifier
                .width(700.dp)   // Ancho de 600 dp
                .height(500.dp)  // Alto de 500 dp
        )


        mesas.forEach { mesa ->
            val id = mesa["id"] as Int
            val habilitado = if (mesa["habilitado"] is Boolean) {
                if (mesa["habilitado"] as Boolean) 1 else 0
            } else {
                mesa["habilitado"] as Int
            }

            val color = when (habilitado) {
                1 -> Color.Green
                0 -> Color.Red
                else -> Color.Gray
            }
            val posicionX = when (id) {
                1 -> 5f
                2 -> 40f
                3 -> 75f
                //4 -> 105f
                4 -> 13f
                5 -> 42f
                6 -> 70f
                7 -> 100f
                //9 -> 81f
                //10 -> 115f
                8 -> 81f
                9 -> 115f
                //13 -> 147f
                10 -> 65f
                11 -> 98f
                12 -> 131f
                //17 -> 166f
                13 -> 197f
                14 -> 237f
                15 -> 197f
                16 -> 237f
                else -> 0f
            }

            val posicionY = when (id) {
                1 -> 70f
                2 -> 70f
                3 -> 70f
                //4 -> 70f
                4 -> 177f
                5 -> 177f
                6 -> 177f
                7 -> 177f
                // 9 -> 280f
                //10 -> 280f
                8 -> 380f
                9 -> 380f
                //13 -> 380f
                10 -> 475f
                11 -> 475f
                12 -> 475f
                // 17 -> 475f
                13 -> 375f
                14 -> 375f
                15 -> 475f
                16 -> 475f
                else -> 0f
            }

            // Ajusta la posición de los círculos según el mapa
            MesaInteractiva(
                modifier = Modifier
                    .absoluteOffset(
                        x = ajustarX(posicionX),
                        y = ajustarY(posicionY)
                    ),
                mesaNumero = id,
                isSelected = selectedMesa == id,
                onMesaClicked = { if (habilitado == 1) onMesaSelected(id) },
                color = color
            )
        }
    }
}

@Composable
fun MesaInteractiva(
    modifier: Modifier = Modifier,
    mesaNumero: Int,
    isSelected: Boolean,
    onMesaClicked: (Int) -> Unit,
    color: Color
) {
    Box(
        modifier = modifier
            .size(40.dp)  // Aumentado el tamaño
            .background(color, shape = RoundedCornerShape(4.dp)) // Bordes redondeados
            .border(
                BorderStroke(2.dp, Color.Black),
                shape = RoundedCornerShape(4.dp)
            ) // Borde negro
            .clickable(enabled = color == Color.Green) { // Solo seleccionable si está habilitado
                onMesaClicked(mesaNumero)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mesaNumero.toString(),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun obtenerEstadoMesas(navController: NavController, onMesasRecibidas: (List<Map<String, Any>>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = OkHttpClient()
            val url = "http://10.0.2.2/obtenerMesas.php"  // Cambia la URL al script correcto

            val request = Request.Builder()
                .url(url)
                .get()  // Usamos GET porque no estamos enviando parámetros en esta solicitud
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d("ObtenerEstadoMesas", "Respuesta completa: $responseBody")

            if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                try {
                    val jsonResponse = JSONObject(responseBody)

                    if (jsonResponse.getBoolean("success")) {
                        val mesasArray = jsonResponse.getJSONArray("mesas")
                        val mesasList = mutableListOf<Map<String, Any>>()

                        for (i in 0 until mesasArray.length()) {
                            val mesa = mesasArray.getJSONObject(i)
                            val mesaData = mapOf(
                                "id" to mesa.getInt("id"),
                                "habilitado" to mesa.getBoolean("habilitado")
                            )
                            mesasList.add(mesaData)
                        }

                        withContext(Dispatchers.Main) {
                            onMesasRecibidas(mesasList)
                        }
                    } else {
                        val errorMessage = jsonResponse.getString("message")
                        Log.d("ObtenerEstadoMesas", "Error del servidor: $errorMessage")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(navController.context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    Log.e("ObtenerEstadoMesas", "Error al parsear la respuesta: ${e.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(navController.context, "Error de respuesta", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(navController.context, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(navController.context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


@Composable
fun NuevaPantallaEskaeras(navController: NavController, username: String, mesaSeleccionada: String) {
    var eskaeras by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Obtener eskaeras al cargar la pantalla
    LaunchedEffect(Unit) {
        obtenerEskaerasPorMesa(mesaSeleccionada.toInt()) { success, result ->
            if (success) {
                eskaeras = result
            } else {
                error = result.firstOrNull()?.get("error") as? String ?: "Error desconocido"
            }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091725))
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_the_bull),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Eskaerak - Mahaia: $mesaSeleccionada",
                fontSize = 24.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                error != null -> {
                    Text(text = error!!, color = Color.Red)
                }
                eskaeras.isEmpty() -> {
                    Text(text = "Ez dago eskaerarik", color = Color.Black)
                }
                else -> {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(eskaeras) { eskaera ->
                            EskaeraItem(
                                eskaera = eskaera,
                                onEditarClick = { eskaeraZenb ->
                                    val eskaeraSeleccionada = eskaeras.find { it["eskaeraZenb"] == eskaeraZenb }
                                    if (eskaeraSeleccionada != null) {
                                        navController.navigate("editarEskaera/$eskaeraZenb/${mesaSeleccionada}/${Uri.encode(JSONObject(eskaeraSeleccionada).toString())}/$username")                                    }
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    navController.navigate("pantallaMenu/$username/$mesaSeleccionada")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
            ) {
                Text("Eskaera berria sortu", fontSize = 16.sp)
            }
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
        ) {
            Text("Atzera")
        }
    }
}

@Composable
fun EskaeraItem(
    eskaera: Map<String, Any>,
    onEditarClick: (Int) -> Unit // recibe eskaeraZenb para saber cuál editar
) {
    val eskaeraZenb = eskaera["eskaeraZenb"] as? Int ?: return
    val platos = eskaera["platos"] as? List<Map<String, Any>> ?: emptyList()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "EskaeraZenb: $eskaeraZenb",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            platos.forEach { plato ->
                Text(
                    text = "${plato["izena"]} - ${plato["prezioa"]}€",
                    color = Color.DarkGray,
                    fontSize = 16.sp
                )

                plato["nota_gehigarriak"]?.let {
                    Text(
                        text = "Ohar gehigarria: $it",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                plato["eskaeraOrdua"]?.let {
                    Text(
                        text = "Eskaera-ordua: $it",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onEditarClick(eskaeraZenb) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Editatu")
            }
        }
    }
}

fun obtenerEskaerasPorMesa(mesaId: Int, callback: (Boolean, List<Map<String, Any>>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = OkHttpClient()
            val url = "http://10.0.2.2/obtener_eskaeras.php?mesa_id=$mesaId"

            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            // Comprobar si la respuesta es exitosa y no está vacía
            if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                try {
                    val jsonResponse = JSONObject(responseBody)
                    val success = jsonResponse.getBoolean("success")

                    if (success) {
                        val eskaerasArray = jsonResponse.getJSONArray("eskaeras")
                        val lista = mutableListOf<Map<String, Any>>()

                        // Procesar cada eskaera
                        for (i in 0 until eskaerasArray.length()) {
                            val obj = eskaerasArray.getJSONObject(i)
                            val platosArray = obj.getJSONArray("platos")
                            val platos = mutableListOf<Map<String, Any>>()

                            // Procesar cada plato dentro de la eskaera
                            for (j in 0 until platosArray.length()) {
                                val platoObj = platosArray.getJSONObject(j)
                                platos.add(
                                    mapOf(
                                        "izena" to platoObj.getString("izena"),
                                        "prezioa" to platoObj.getDouble("prezioa"),
                                        "nota_gehigarriak" to platoObj.getString("nota_gehigarriak"),
                                        "eskaeraOrdua" to platoObj.getString("eskaeraOrdua")
                                    )
                                )
                            }

                            lista.add(
                                mapOf(
                                    "eskaeraZenb" to obj.getInt("eskaeraZenb"),
                                    "platos" to platos
                                )
                            )
                        }

                        // Llamar al callback con el resultado exitoso
                        withContext(Dispatchers.Main) {
                            callback(true, lista)
                        }
                    } else {
                        val errorMsg = jsonResponse.optString("message", "Errore ezezaguna")
                        // Llamar al callback con error
                        withContext(Dispatchers.Main) {
                            callback(false, listOf(mapOf("error" to errorMsg)))
                        }
                    }
                } catch (e: Exception) {
                    // Manejar error de parsing
                    withContext(Dispatchers.Main) {
                        callback(false, listOf(mapOf("error" to "Error al procesar los datos")))
                    }
                }
            } else {
                // Error en la respuesta o no hay contenido
                withContext(Dispatchers.Main) {
                    callback(false, listOf(mapOf("error" to "Errorea zerbitzariarekin konektatzean")))
                }
            }
        } catch (e: Exception) {
            // Error en la conexión o la solicitud
            withContext(Dispatchers.Main) {
                callback(false, listOf(mapOf("error" to (e.localizedMessage ?: "Errore ezezaguna"))))
            }
        }
    }
}

@Composable
fun PantallaEditarEskaera(
    navController: NavController,
    eskaeraZenb: Int,
    mesaSeleccionada: String,
    eskaeraDetalles: JSONObject,
    username: String // Agregar username como parámetro
) {
    // Lista mutable para manejar los platos dinámicamente
    var platos by remember { mutableStateOf(eskaeraDetalles.optJSONArray("platos")?.let { JSONArray(it.toString()) } ?: JSONArray()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091725))
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_the_bull),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Editatu la eskaera Nº$eskaeraZenb",
                fontSize = 24.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Mahaia: $mesaSeleccionada",
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (platos.length() > 0) {
                    for (i in 0 until platos.length()) {
                        val plato = platos.getJSONObject(i)
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Plato: ${plato.optString("izena")}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Kopurua: ${maxOf(plato.optInt("cantidad"), 1)}", // Cantidad mínima de 1
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Prezioa: ${plato.optDouble("prezioa")}€",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Ohar gehigarria: ${plato.optString("nota_gehigarriak")}",
                                        fontSize = 14.sp
                                    )
                                }
                                // Botón de eliminar
                                Button(
                                    onClick = {
                                        val izena = plato.optString("izena")
                                        eliminarPlatoDeBBDD(izena, eskaeraZenb) { success ->
                                            if (success) {
                                                // Crear una nueva lista sin el plato eliminado
                                                val nuevaLista = JSONArray()
                                                for (j in 0 until platos.length()) {
                                                    if (j != i) {
                                                        nuevaLista.put(platos.getJSONObject(j))
                                                    }
                                                }
                                                platos = nuevaLista
                                            } else {
                                                Toast.makeText(navController.context, "Error al eliminar el plato", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Ezabatu",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "Ez dago xehetasunik eskuragarri",
                            color = Color.Red
                        )
                    }
                }
            }
            // Botón "Volver"
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Atzera", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            // Botón "Agregar un plato"
            Button(
                onClick = {
                    navController.navigate("agregarPlato/$username/$mesaSeleccionada/$eskaeraZenb")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            ) {
                Text(text = "Plater bat gehitu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun eliminarPlatoDeBBDD(izena: String, eskaeraZenb: Int, onResult: (Boolean) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = OkHttpClient()
            val url = "http://10.0.2.2/eliminar_plato.php"
            val jsonBody = JSONObject().apply {
                put("izena", izena)
                put("eskaeraZenb", eskaeraZenb)
            }
            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaType(),
                jsonBody.toString()
            )
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val success = jsonResponse.optBoolean("success", false)
                withContext(Dispatchers.Main) {
                    onResult(success)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult(false)
            }
        }
    }
}

@Composable
fun PantallaAgregarPlato(
    navController: NavController,
    username: String,
    mesaSeleccionada: String,
    eskaeraZenb: Int
) {
    var platos by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val cantidades = remember { mutableStateMapOf<String, Int>() }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(2000)
            errorMessage = null
        }
    }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val url = "http://10.0.2.2/menu.php"
                val request = Request.Builder().url(url).get().build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d("PantallaAgregarPlato", "Respuesta del servidor: $responseBody") // Log de la respuesta del menú

                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.getBoolean("success")) {
                        // 🔴 Cambiar "data" por "menu"
                        val menuArray = jsonResponse.getJSONArray("menu")
                        val items = mutableListOf<Map<String, Any>>()

                        for (i in 0 until menuArray.length()) {
                            val item = menuArray.getJSONObject(i)
                            val plato = mapOf(
                                "id" to item.getInt("id"),
                                "izena" to item.getString("izena"),
                                "prezioa" to item.getString("prezioa"),
                                "kategoria" to item.optString("kategoria", "Categoría desconocida"),
                                "deskribapena" to item.getString("deskribapena")
                            )
                            items.add(plato)
                        }

                        withContext(Dispatchers.Main) {
                            platos = items
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            errorMessage = jsonResponse.getString("message")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Error de conexión: ${response.message}"
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Excepción: ${e.message}"
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091725))
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_the_bull),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Platera gehitu", fontSize = 24.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                        .background(Color(0x44FF0000))
                        .padding(8.dp)
                )
            }
            if (platos.isEmpty()) {
                Text(text = "Ez dago informazioarik", color = Color.Black)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    val categoriasOrdenadas = listOf("Edaria", "Lehenengo platera", "Bigarren platera", "Postrea")
                    val categorias = platos.groupBy {
                        (it["kategoria"] as? String) ?: "Categoría desconocida"
                    }.toSortedMap(compareBy { categoriasOrdenadas.indexOf(it).takeIf { it != -1 } ?: Int.MAX_VALUE })
                    categorias.forEach { (categoria, platosDeCategoria) ->
                        item {
                            Text(
                                text = categoria,
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(platosDeCategoria) { plato ->
                            val nombrePlato = plato["izena"] as String
                            val precioPlato = plato["prezioa"] as String
                            val descripcion = plato["deskribapena"] as String
                            val cantidadActual = cantidades[nombrePlato] ?: 0
                            val platoId = plato["id"] as? Int ?: -1
                            var showPopup by remember { mutableStateOf(false) }
                            if (showPopup) {
                                AlertDialog(
                                    onDismissRequest = { showPopup = false },
                                    title = { Text(nombrePlato, fontWeight = FontWeight.Bold) },
                                    text = { Text(descripcion) },
                                    confirmButton = {
                                        Button(
                                            onClick = { showPopup = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA450D))
                                        ) {
                                            Text("OK")
                                        }
                                    }
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = nombrePlato,
                                        color = Color.Black,
                                        fontSize = 16.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.width(120.dp)
                                    )
                                    IconButton(
                                        onClick = { showPopup = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Ver descripción",
                                            tint = Color.Black
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "$precioPlato€", color = Color.Black, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cnt: $cantidadActual",
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Button(
                                        onClick = {
                                            if (platoId != -1) {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    try {
                                                        val client = OkHttpClient()
                                                        val url = "http://10.0.2.2/almazena.php?platera_id=$platoId"
                                                        val request = Request.Builder().url(url).get().build()
                                                        val response = client.newCall(request).execute()
                                                        val responseBody = response.body?.string()
                                                        if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                                                            val jsonResponse = JSONObject(responseBody)
                                                            if (jsonResponse.getBoolean("success")) {
                                                                withContext(Dispatchers.Main) {
                                                                    cantidades[nombrePlato] = cantidadActual + 1
                                                                }
                                                            } else {
                                                                withContext(Dispatchers.Main) {
                                                                    errorMessage = jsonResponse.optString("message", "Error desconocido")
                                                                }
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        withContext(Dispatchers.Main) {
                                                            errorMessage = "Error de conexión: ${e.message}"
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text(text = "+", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
            ) {
                Text(text = "Atzera", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    val selectedItems = cantidades.filterValues { it > 0 }
                        .mapNotNull { plato ->
                            val platoData = platos.find { it["izena"] == plato.key }
                            val platoId = platoData?.get("id") as? Int ?: return@mapNotNull null
                            val precio = platoData["prezioa"] as? String ?: "0"
                            val nombrePlato = plato.key
                            "$platoId:$nombrePlato:${plato.value}:$precio"
                        }
                        .joinToString(";")
                    Log.d("PantallaAgregarPlato", "Hautatutako elementuak: $selectedItems") // Log de los elementos seleccionados

                    if (selectedItems.isEmpty()) {
                        errorMessage = "Plater bat aukeratu behar duzu, gutxienez, jarraitzeko."
                    } else {
                        insertarPlatoEnEskaera(mesaSeleccionada.toInt(), eskaeraZenb, selectedItems) { success ->
                            if (success) {
                                Log.d("PantallaAgregarPlato", "Platera behar bezala erantsita") // Log de éxito
                                navController.popBackStack()
                            } else {
                                Log.e("PantallaAgregarPlato", "Errorea platera gehitzean") // Log de error
                                errorMessage = "Errorea platera gehitzean."
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
            ) {
                Text(text = "Gorde", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun insertarPlatoEnEskaera(
    mesaId: Int,
    eskaeraZenb: Int,
    selectedItems: String,
    onResult: (Boolean) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = OkHttpClient()
            val url = "http://10.0.2.2/insertar_plato.php"
            val jsonBody = JSONObject().apply {
                put("mesa_id", mesaId)
                put("eskaeraZenb", eskaeraZenb)
                put("platos", JSONArray(selectedItems.split(";").map { it }))
            }
            Log.d("InsertarPlato", "Solicitud a enviar: $jsonBody") // Log de la solicitud

            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaType(),
                jsonBody.toString()
            )
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            Log.d("InsertarPlato", "Enviando solicitud al servidor...") // Log antes de enviar la solicitud
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                Log.d("InsertarPlato", "Respuesta del servidor: $responseBody") // Log de la respuesta del servidor

                val jsonResponse = JSONObject(responseBody)
                val success = jsonResponse.optBoolean("success", false)
                withContext(Dispatchers.Main) {
                    onResult(success)
                }
            } else {
                Log.e("InsertarPlato", "Error en la respuesta del servidor: ${response.message}") // Log de errores del servidor
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        } catch (e: Exception) {
            Log.e("InsertarPlato", "Excepción durante la inserción: ${e.message}") // Log de excepciones
            withContext(Dispatchers.Main) {
                onResult(false)
            }
        }
    }
}

@Composable
fun PantallaMenu(navController: NavController, username: String, mesaSeleccionada: String) {
    var platos by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val cantidades = remember { mutableStateMapOf<String, Int>() }
    var descripcionPlato by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val url = "http://10.0.2.2/menu.php"
                val request = Request.Builder().url(url).get().build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d("RespuestaServidor", "Response Body: $responseBody")

                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.getBoolean("success")) {
                        // 🔴 Clave "menu" en lugar de "data"
                        val menuArray = jsonResponse.getJSONArray("menu")
                        val items = mutableListOf<Map<String, Any>>()

                        for (i in 0 until menuArray.length()) {
                            val item = menuArray.getJSONObject(i)
                            val plato = mapOf(
                                "id" to item.getInt("id"),
                                "izena" to item.getString("izena"),
                                "prezioa" to item.getString("prezioa"),
                                "kategoria" to item.optString("kategoria", "Categoría desconocida"),
                                "deskribapena" to item.getString("deskribapena")
                            )
                            items.add(plato)
                        }
                        withContext(Dispatchers.Main) { platos = items }
                    } else {
                        withContext(Dispatchers.Main) {
                            errorMessage = jsonResponse.getString("message")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Error de conexión: ${response.message}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Excepción: ${e.message}"
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091725))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Menua",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$username, $mesaSeleccionada. mahaia aukeratu du",
                color = Color.White,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().background(Color(0x44FF0000)).padding(8.dp)
                )
            }

            if (platos.isEmpty()) {
                CircularProgressIndicator(color = Color(0xFFBA450D))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    val categoriasOrdenadas = listOf("Edaria", "Lehenengo platera", "Bigarren platera", "Postrea")
                    val categorias = platos.groupBy {
                        (it["kategoria"] as? String) ?: "Categoría desconocida"
                    }.toSortedMap(compareBy { categoriasOrdenadas.indexOf(it).takeIf { it != -1 } ?: Int.MAX_VALUE })

                    categorias.forEach { (categoria, platosCategoria) ->
                        item {
                            Text(
                                text = categoria,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(platosCategoria) { plato ->
                            val nombrePlato = plato["izena"] as String
                            val precioPlato = plato["prezioa"] as String
                            val descripcion = plato["deskribapena"] as String
                            val cantidadActual = cantidades[nombrePlato] ?: 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // 🔍 Botón de descripción a la izquierda
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { descripcionPlato = descripcion },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Ikusi deskribapena",
                                            tint = Color.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = nombrePlato,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "$precioPlato€",
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                // 🔴 Botones de cantidad a la derecha
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = { cantidades[nombrePlato] = cantidadActual + 1 },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = cantidadActual.toString(),
                                        color = Color.Black,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Button(
                                        onClick = {
                                            if (cantidadActual > 0) {
                                                cantidades[nombrePlato] = cantidadActual - 1
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("-", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo de descripción
        if (descripcionPlato != null) {
            AlertDialog(
                onDismissRequest = { descripcionPlato = null },
                confirmButton = {
                    Button(
                        onClick = { descripcionPlato = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA450D))
                    ) {
                        Text("Itxi", color = Color.White)
                    }
                },
                title = { Text("Plateraren deskribapena") },
                text = { Text(descripcionPlato!!) }
            )
        }

        // Botones inferiores (Volver y Siguiente)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
            ) {
                Text("Atzera", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val groupedItems = cantidades.filterValues { it > 0 }.map { (nombre, cantidad) ->
                        val platoData = platos.find { it["izena"] == nombre }!!
                        mapOf(
                            "id" to platoData["id"] as Int,
                            "nombre" to platoData["izena"] as String,
                            "cantidad" to cantidad,
                            "precio" to platoData["prezioa"].toString().toFloat(),
                            "nota" to "" // Nota inicial vacía
                        )
                    }
                    if (groupedItems.isNotEmpty()) {
                        navController.navigate("pantallaFactura/${Uri.encode(JSONArray(groupedItems).toString())}/$username/$mesaSeleccionada")
                    } else {
                        errorMessage = "Aukeratu plater bat gutxienez"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
            ) {
                Text("Jarraitu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PantallaFactura(navController: NavController, selectedItems: List<Map<String, Any>>, username: String, mesaSeleccionada: String) {
    val langileaId = remember { mutableStateOf<Int?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    val noteDialogState = remember { mutableStateOf(false) }
    val currentNote = remember { mutableStateOf("") }
    val currentItemIndex = remember { mutableStateOf(-1) }

    // Convertimos selectedItems en una MutableList para permitir modificaciones
    val mutableSelectedItems = remember { mutableStateOf(selectedItems.map { it.toMutableMap() }.toMutableList()) }

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)

    LaunchedEffect(username) {
        langileaId.value = obtenerIdUsuario(username)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091725))
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_the_bull),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Eskaera baieztatu", fontSize = 24.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            Text(
                text = "$username aurkeratu du $mesaSeleccionada mahail zenb",
                color = Color.Black,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (mutableSelectedItems.value.isEmpty()) {
                Text(text = "No hay platos seleccionados", color = Color.Black)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ID", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Izena", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                            Text("Kant", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Prezioa", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Notak", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        }
                    }
                    itemsIndexed(mutableSelectedItems.value) { index, plato ->
                        val id = plato["id"] as Int
                        val nombre = plato["nombre"] as String
                        val cantidad = plato["cantidad"] as Int
                        val precio = plato["precio"] as Float
                        val nota = plato["nota"] as? String ?: ""

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$id", modifier = Modifier.weight(1f))
                            Text(nombre, modifier = Modifier.weight(2f))
                            Text("(x$cantidad)", modifier = Modifier.weight(1f))
                            Text("${precio}€", modifier = Modifier.weight(1f))
                            Button(
                                onClick = {
                                    currentItemIndex.value = index
                                    currentNote.value = nota
                                    noteDialogState.value = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (nota.isEmpty()) "Gehitu oharra" else "Editatu oharra")
                            }
                        }
                    }
                }            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
            ) {
                Text(text = "Atzea", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    Log.d("PantallaFactura", "Botón 'Jarraitu' pulsado")  // Log para verificar si se presiona el botón

                    langileaId.value?.let {
                        if (mesaSeleccionada.isNotEmpty() && mutableSelectedItems.value.isNotEmpty()) {
                            Log.d("PantallaFactura", "Datos enviados: langileaId=$it, mesaSeleccionada=$mesaSeleccionada, items=${mutableSelectedItems.value.size}")

                            insertarComandaEnBBDD(it, mesaSeleccionada, mutableSelectedItems.value) { success ->
                                if (success) {
                                    Log.d("PantallaFactura", "Comanda insertada correctamente")
                                    showDialog.value = true
                                } else {
                                    Log.e("PantallaFactura", "Error al insertar la comanda")
                                    Toast.makeText(context, "Error al insertar la comanda", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Por favor, seleccione una mesa y algunos platos", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
            ) {
                Text(text = "Jarraitu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }


        }

        if (noteDialogState.value) {
            AlertDialog(
                onDismissRequest = { noteDialogState.value = false },
                title = { Text(text = "Añadir/Editar Nota", color = Color.Black) },
                text = {
                    Column {
                        TextField(
                            value = currentNote.value,
                            onValueChange = { currentNote.value = it },
                            placeholder = { Text("Escribe una nota...") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Aseguramos de que el índice esté dentro de un rango válido
                            if (currentItemIndex.value >= 0 && currentItemIndex.value < mutableSelectedItems.value.size) {
                                val updatedItem = mutableSelectedItems.value[currentItemIndex.value].toMutableMap()
                                updatedItem["nota"] = currentNote.value
                                mutableSelectedItems.value[currentItemIndex.value] = updatedItem
                            }
                            noteDialogState.value = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA450D))
                    ) {
                        Text(text = "Guardar", fontSize = 16.sp)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { noteDialogState.value = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text(text = "Cancelar", fontSize = 16.sp)
                    }
                }
            )
        }

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                    navController.navigate("pantallaMapa?username=$username")
                },
                title = { Text(text = "Komanda baieztatua", color = Color.Black) },
                text = { Text("Eskaera ondo erregistratu da.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog.value = false
                            navController.navigate("pantallaMapa?username=$username")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA450D))
                    ) {
                        Text(text = "Jarraitu", fontSize = 16.sp)
                    }
                }
            )
        }
    }
}

fun insertarComandaEnBBDD(
    langileaId: Int,
    mesaSeleccionada: String,
    mutableSelectedItems: List<Map<String, Any>>,
    onResult: (Boolean) -> Unit
) {
    val client = OkHttpClient()
    val url = "http://10.0.2.2/insertar_comanda.php"

    val jsonBody = JSONObject().apply {
        put("mesa_id", mesaSeleccionada.toIntOrNull() ?: 0)
        // Se elimina eskaeraZenb ya que el PHP lo generará automáticamente

        put("platera_items", JSONArray().apply {
            mutableSelectedItems.forEach { item ->
                put(JSONObject().apply {
                    put("platera_id", item["id"] as? Int ?: 0)
                    put("izena", item["nombre"] as? String ?: "") // Enviar el nombre del plato como 'izena'
                    put("nota", item["nota"] as? String ?: "")
                    put("cantidad", item["cantidad"] as? Int ?: 1)
                    put("precio", item["precio"] as? Float ?: 0.0f)
                })
            }
        })

    }

    Log.d("insertarComandaEnBBDD", "Cuerpo JSON a enviar: ${jsonBody.toString()}")  // Log del JSON enviado

    val requestBody = RequestBody.create(
        "application/json; charset=utf-8".toMediaType(),
        jsonBody.toString()
    )

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("insertarComandaEnBBDD", "Error en la llamada: ${e.message}")
            onResult(false)
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    Log.d("insertarComandaEnBBDD", "Respuesta del servidor: $responseBody")  // Log de la respuesta
                    val jsonResponse = JSONObject(responseBody)
                    val success = jsonResponse.optBoolean("success", false)
                    onResult(success)
                } else {
                    Log.e("insertarComandaEnBBDD", "Error en la respuesta: ${response.message}")
                    onResult(false)
                }
            }
        }
    })
}

suspend fun obtenerIdUsuario(username: String): Int? {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val url = "http://10.0.2.2/obtener_id_usuario.php"

        val jsonBody = JSONObject().apply {
            put("username", username)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            jsonBody.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string())
                val id = jsonResponse.optInt("id", -1)
                return@withContext if (id != -1) id else null
            } else {
                return@withContext null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext null
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaPrincipal() {
    _5_erronka1Theme {
        PantallaPrincipal(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaSegundaPantalla() {
    _5_erronka1Theme {
        SegundaPantalla(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaMapa() {
    _5_erronka1Theme {
        PantallaMapa(navController = rememberNavController(), username = "Juan")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaMenu() {
    _5_erronka1Theme {
        PantallaMenu(
            navController = rememberNavController(), username = "Juan",  mesaSeleccionada = "Mesa 1"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaFactura() {
    // NavController ficticio para el Preview
    val navController = rememberNavController()

    // Ejemplo de productos seleccionados con nombre, ID, cantidad y precio
    val selectedItems = listOf(
        mapOf("id" to 1, "nombre" to "Pizza Margarita", "candida" to 2, "precio" to 5.0f),
        mapOf("id" to 2, "nombre" to "Ensalada César", "cantidad" to 1, "precio" to 3.5f)
    )

    // Muestra un mensaje placeholder
    PantallaFactura(
        navController = navController,
        selectedItems = selectedItems,
        username = "Nombre del" +
                " usuario",
        mesaSeleccionada = "Mesa 1"
    )
}