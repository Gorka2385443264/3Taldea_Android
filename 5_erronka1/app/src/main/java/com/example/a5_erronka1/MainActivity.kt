package com.example.a5_erronka1

import com.example.a5_erronka1.ChatClient // no tocar
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import android.util.Base64
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.FileWriter
import java.io.IOException
import java.net.URLEncoder
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
                    ) { backStackEntry ->
                        val localUsername = backStackEntry.arguments?.getString("username") ?: "Usuario" // Cambio aquí
                        val mesaSeleccionada = backStackEntry.arguments?.getString("mesaSeleccionada") ?: "Mesa 1"

                        PantallaMenu(
                            navController = navController,
                            username = localUsername, // Usar la variable local
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
                                            "platera_id" to item.getInt("platera_id"),
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
                                username = username,
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
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    val coroutineScope = rememberCoroutineScope()

    // Selector de imágenes que envía al instante
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val protocolo = enviarImagen(context, it, izena)
                if (protocolo.isNotEmpty()) {
                    chatMessages = chatMessages + Pair("imagen", protocolo)
                }
            }
        }
    }

    // Conexión y escucha
    LaunchedEffect(Unit) {
        ChatClient.connect { success ->
            if (success) {
                coroutineScope.launch {
                    ChatClient.listenForMessages { tipo, contenido ->
                        chatMessages = chatMessages + Pair(tipo, contenido)
                    }
                }
            } else {
                Toast.makeText(context, "Conexión fallida", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFEFEFEF))
    ) {
        // Área de mensajes
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            chatMessages.forEach { (tipo, contenido) ->
                if (tipo == "imagen") {
                    // IMG_FILE:usuario:nombre:base64
                    val partes = contenido.split(":", limit = 4)
                    if (partes.size == 4) {
                        val usuario = partes[1]
                        val nombreArchivo = partes[2]
                        val bytes = Base64.decode(partes[3], Base64.NO_WRAP)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { guardarImagen(context, nombreArchivo, bytes) },
                            horizontalAlignment = if (usuario == izena) Alignment.End else Alignment.Start
                        ) {
                            Text("$usuario ha enviado imagen: $nombreArchivo")
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = nombreArchivo,
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                } else {
                    // Texto cifrado → "usuario: mensaje"
                    val usuario = contenido.substringBefore(":")
                    val texto = contenido.substringAfter(":").trim()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        contentAlignment = if (usuario == izena) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Column(
                            horizontalAlignment = if (usuario == izena) Alignment.End else Alignment.Start
                        ) {
                            Text(
                                text = usuario,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = texto,
                                modifier = Modifier
                                    .background(
                                        color = if (usuario == izena) Color(0xFF8B4513) else Color(0xFFD3D3D3),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp),
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Campo de texto
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Idatzi zerbait") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Botones
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    if (message.isNotBlank()) {
                        coroutineScope.launch {
                            ChatClient.sendText(izena, message)
                            chatMessages = chatMessages + Pair("texto", "$izena: $message")
                            message = ""
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Bidali")
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Argazkia bidali")
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate("pantallaMapa?username=$izena") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Atzera")
        }
    }
}

fun guardarImagen(context: Context, nombre: String, bytes: ByteArray) {
    try {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(path, nombre)
        FileOutputStream(file).use { it.write(bytes) }
        Toast.makeText(context, "Imagen guardada en Descargas", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

suspend fun enviarImagen(context: Context, uri: Uri, username: String): String =
    withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            val baos = ByteArrayOutputStream().apply { bitmap.compress(Bitmap.CompressFormat.PNG, 100, this) }
            val bytes = baos.toByteArray()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val filename = "imagen_${System.currentTimeMillis()}.png"
            val protocolo = "IMG_FILE:$username:$filename:$base64"
            ChatClient.sendRaw(protocolo)
            protocolo
        } catch (e: Exception) {
            ""
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
            text = "Saioa hasi",
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
                            Toast.makeText(/* context = */ navController.context, /* text = */
                                errorMessage, /* duration = */
                                Toast.LENGTH_SHORT).show()
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.saboreame),
                    contentDescription = "Logo de la empresa",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
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

            if (selectedMesa != null) {
                Text(
                    text = "Mahaia: $selectedMesa aukeratu da",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Text(
                    text = "Ez dago mahairik aukeratuta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                MapaDeMesas(selectedMesa, mesas) { mesaNumero ->
                    selectedMesa = mesaNumero
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Button(
                onClick = { navController.navigate("segundaPantalla") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
            ) {
                Text(text = "Atzera", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
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

        Button(
            onClick = {
                if (selectedMesa != null) {
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
                .align(Alignment.BottomEnd)
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

        Image(
            painter = painterResource(id = R.drawable.mapeo),
            contentDescription = "Mapa del restaurante",
            modifier = Modifier
                .width(700.dp)
                .height(500.dp)
        )

        mesas.forEach { mesa ->
            val id = mesa["id"] as Int
            val habilitado = (mesa["habilitado"] as? Boolean) ?: false
            val tieneEskaeraActiva = (mesa["tieneEskaeraActiva"] as? Boolean) ?: false
            val color = when {
                tieneEskaeraActiva -> Color(0xFFFFA500) // Naranja
                habilitado -> Color.Green // Verde
                else -> Color.Red // Rojo
            }

            val posicionX = when (id) {
                1 -> 25f
                2 -> 65f
                3 -> 105f
                4 -> 30f
                5 -> 65f
                6 -> 103f
                7 -> 139f
                8 -> 115f
                9 -> 157f
                10 -> 96f
                11 -> 137f
                12 -> 180f
                13 -> 260f
                14 -> 310f
                15 -> 260f
                16 -> 310f
                else -> 0f
            }

            val posicionY = when (id) {
                1 -> 0f
                2 -> 0f
                3 -> 0f
                4 -> 160f
                5 -> 160f
                6 -> 160f
                7 -> 160f
                8 -> 470f
                9 -> 470f
                10 -> 607f
                11 -> 607f
                12 -> 607f
                13 -> 440f
                14 -> 440f
                15 -> 613f
                16 -> 613f
                else -> 0f
            }

            MesaInteractiva(
                modifier = Modifier
                    .absoluteOffset(
                        x = ajustarX(posicionX),
                        y = ajustarY(posicionY)
                    ),
                mesaNumero = id,
                isSelected = selectedMesa == id,
                onMesaClicked = {
                    if (habilitado || tieneEskaeraActiva) {
                        onMesaSelected(id)
                    }
                },
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
            .size(40.dp)
            .background(color, shape = RoundedCornerShape(4.dp))
            .border(BorderStroke(2.dp, Color.Black), shape = RoundedCornerShape(4.dp))
            .clickable(enabled = color != Color.Red) { onMesaClicked(mesaNumero) },
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
            val url = "http://10.0.2.2/obtenerMesas.php"
            Log.d("ObtenerEstadoMesas", "Realizando solicitud GET a $url")
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            Log.d("ObtenerEstadoMesas", "Respuesta completa del servidor: $responseBody")

            if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                try {
                    val jsonResponse = JSONObject(responseBody)
                    Log.d("ObtenerEstadoMesas", "JSON parseado: $jsonResponse")
                    if (jsonResponse.getBoolean("success")) {
                        val mesasArray = jsonResponse.getJSONArray("mesas")
                        val mesasList = mutableListOf<Map<String, Any>>()
                        for (i in 0 until mesasArray.length()) {
                            val mesa = mesasArray.getJSONObject(i)
                            val mesaData = mapOf(
                                "id" to mesa.getInt("id"),
                                "habilitado" to mesa.getBoolean("habilitado"),
                                "tieneEskaeraActiva" to mesa.getBoolean("tieneEskaeraActiva")
                            )
                            Log.d("ObtenerEstadoMesas", "Mesa procesada: $mesaData")
                            mesasList.add(mesaData)
                        }
                        withContext(Dispatchers.Main) {
                            onMesasRecibidas(mesasList)
                        }
                    } else {
                        val errorMessage = jsonResponse.getString("message")
                        Log.e("ObtenerEstadoMesas", "Error del servidor: $errorMessage")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(navController.context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ObtenerEstadoMesas", "Error al parsear la respuesta JSON: ${e.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(navController.context, "Error de respuesta", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.e("ObtenerEstadoMesas", "Error de conexión o respuesta vacía")
                withContext(Dispatchers.Main) {
                    Toast.makeText(navController.context, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            Log.e("ObtenerEstadoMesas", "Excepción de red: ${e.message}")
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
    var mesaTieneEskaeraActiva by remember { mutableStateOf(false) } // Nuevo estado

    // Obtener eskaeras al cargar la pantalla
    LaunchedEffect(Unit) {
        obtenerEskaerasPorMesa(mesaSeleccionada.toInt()) { success, result ->
            if (success) {
                eskaeras = result
                mesaTieneEskaeraActiva = result.isNotEmpty() // Actualizar estado de eskaera activa
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
                                        navController.navigate("editarEskaera/$eskaeraZenb/${mesaSeleccionada}/${Uri.encode(JSONObject(eskaeraSeleccionada).toString())}/$username")
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Button(
                onClick = {
                    if (!mesaTieneEskaeraActiva) { // Habilitar solo si no hay eskaera activa
                        Log.d("Navegacion", "Botón 'Eskaera berria sortu' presionado")
                        navController.navigate("pantallaMenu/$username/$mesaSeleccionada")
                    } else {
                        Toast.makeText(
                            navController.context,
                            "La mesa ya tiene una eskaera activa.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (mesaTieneEskaeraActiva) Color.Gray else Color(0xFF8B4513)),
                enabled = !mesaTieneEskaeraActiva // Deshabilitar si hay eskaera activa
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
                        text = "Notak: $it",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                plato["eskaeraOrdua"]?.let {
                    Text(
                        text = "Ordua: $it",
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
    username: String // Parámetro añadido
) {
    var refresh by remember { mutableStateOf(false) }
    var platos by remember(refresh) {
        mutableStateOf(
            eskaeraDetalles.optJSONArray("platos")?.let { JSONArray(it.toString()) }
                ?: JSONArray()
        )
    }

    // Detector de cambios en la navegación para refrescar datos
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry?.destination?.route) {
        if (navBackStackEntry?.destination?.route == "editar_eskaera") {
            refresh = !refresh
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF091725)).padding(16.dp)) {
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
                text = "Eskaera: $eskaeraZenb editatu.",
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
                items(platos.length()) { i ->
                    val plato = platos.getJSONObject(i)
                    PlatoItem(plato, i) { index ->
                        val izena = plato.optString("izena")
                        eliminarPlatoDeBBDD(izena, eskaeraZenb) { success, message ->
                            if (success) {
                                platos = JSONArray().apply {
                                    for (j in 0 until platos.length()) {
                                        if (j != index) put(platos.getJSONObject(j))
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    navController.context,
                                    message ?: "Errorea platera ezabatzean",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                if (platos.length() == 0) {
                    item { Text("Ez dago xehetasunik eskuragarri") }
                }
            }
        }
        // Row de botones modificado
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
                    navController.navigate("agregarPlato/${username}/${mesaSeleccionada}/${eskaeraZenb}")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
            ) {
                Text("Gehitu platera", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlatoItem(
    plato: JSONObject,
    index: Int,
    onDelete: (Int) -> Unit
) {
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
                text = "Platera: ${plato.optString("izena")}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kantitatea: ${maxOf(plato.optInt("cantidad"), 1)}",
                fontSize = 14.sp
            )
            Text(
                text = "Prezioa: ${plato.optDouble("prezioa")}€",
                fontSize = 14.sp
            )
            Text(
                text = "Nota: ${plato.optString("nota_gehigarriak")}",
                fontSize = 14.sp
            )
        }
        Button(
            onClick = { onDelete(index) },
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

fun eliminarPlatoDeBBDD(
    izena: String,
    eskaeraZenb: Int,
    onResult: (Boolean, String?) -> Unit
) {
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
            val request = Request.Builder().url(url).post(requestBody).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                withContext(Dispatchers.Main) {
                    onResult(jsonResponse.optBoolean("success", false), jsonResponse.optString("message"))
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(false, "Errorea zerbitzariaren erantzunean")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult(false, "Konexio-errorea: ${e.message}")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    if (jsonResponse.optBoolean("success", false)) {
                        val menuArray = jsonResponse.getJSONArray("menu")
                        val items = mutableListOf<Map<String, Any>>()
                        for (i in 0 until menuArray.length()) {
                            val item = menuArray.getJSONObject(i)
                            items.add(mapOf(
                                "id" to item.getInt("id"),
                                "izena" to item.getString("izena"),
                                "prezioa" to item.getString("prezioa"),
                                "kategoria" to item.optString("kategoria", "Categoría desconocida"),
                                "deskribapena" to item.getString("deskribapena")
                            ))
                        }
                        withContext(Dispatchers.Main) { platos = items }
                    } else {
                        withContext(Dispatchers.Main) {
                            errorMessage = jsonResponse.optString("message", "Errorea menua eskuratzean")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Errorea: ${response.code}"
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
                    val categorias = platos.groupBy { it["kategoria"] as String }
                        .toSortedMap(compareBy { categoria ->
                            categoriasOrdenadas.indexOf(categoria).takeIf { it != -1 } ?: Int.MAX_VALUE
                        })
                    categorias.forEach { (categoria, items) ->
                        stickyHeader {
                            Text(
                                text = categoria,
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(items) { plato ->
                            val nombrePlato = plato["izena"] as String
                            val precioPlato = plato["prezioa"] as String
                            val descripcion = plato["deskribapena"] as String
                            val cantidadActual = cantidades[nombrePlato] ?: 0
                            val platoId = plato["id"] as Int
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
                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    val client = OkHttpClient()
                                                    val url = "http://10.0.2.2/almazena.php?platera_id=$platoId"
                                                    Log.d("HTTP_Request", "URL: $url") // Registro de la URL
                                                    val request = Request.Builder().url(url).get().build()
                                                    val response = client.newCall(request).execute()
                                                    Log.d("HTTP_Response", "Código: ${response.code}, Mensaje: ${response.body?.string()}") // Registro de la respuesta
                                                    if (response.isSuccessful) {
                                                        withContext(Dispatchers.Main) {
                                                            cantidades[nombrePlato] = cantidadActual + 1
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("HTTP_Error", "Excepción: ${e.message}") // Registro de errores
                                                    withContext(Dispatchers.Main) {
                                                        errorMessage = "Errorea: ${e.message}"
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
                        .mapNotNull { (nombre, cantidad) ->
                            platos.find { it["izena"] == nombre }?.let { plato ->
                                "${plato["id"]}:$nombre:$cantidad:${plato["prezioa"]}"
                            }
                        }
                        .joinToString(";")

                    if (selectedItems.isEmpty()) {
                        errorMessage = "Plater bat aukeratu behar duzu, gutxienez, jarraitzeko."
                    } else {
                        insertarPlatoEnEskaera(
                            mesaId = mesaSeleccionada.toInt(),
                            eskaeraZenb = eskaeraZenb,
                            selectedItems = selectedItems
                        ) { success ->
                            if (success) {
                                navController.popBackStack()
                            } else {
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
                put("platos", JSONArray(selectedItems.split(";")))
            }
            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaType(),
                jsonBody.toString()
            )
            val request = Request.Builder().url(url).post(requestBody).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                withContext(Dispatchers.Main) { onResult(jsonResponse.optBoolean("success", false)) }
            } else {
                withContext(Dispatchers.Main) { onResult(false) }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onResult(false) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PantallaMenu(navController: NavController, username: String, mesaSeleccionada: String) {
    var platos by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val cantidades = remember { mutableStateMapOf<String, Int>() }
    val coroutineScope = rememberCoroutineScope()

    // Cargar datos del servidor
    LaunchedEffect(Unit) {
        try {
            withContext(Dispatchers.IO) {
                Log.d("PantallaMenu", "Iniciando carga de datos desde el servidor")
                val client = OkHttpClient()
                val url = "http://10.0.2.2/menu.php"
                val request = Request.Builder().url(url).get().build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                    val jsonResponse = JSONObject(responseBody)

                    if (jsonResponse.optBoolean("success", false)) {
                        val menuArray = jsonResponse.optJSONArray("menu")

                        if (menuArray != null && menuArray.length() > 0) {
                            val items = mutableListOf<Map<String, Any>>()

                            for (i in 0 until menuArray.length()) {
                                val item = menuArray.getJSONObject(i)
                                if (item.optInt("menu", 0) == 1) {
                                    items.add(mapOf(
                                        "id" to item.optInt("id", -1),
                                        "izena" to item.optString("izena", "Sin nombre"),
                                        "prezioa" to item.optString("prezioa", "0"),
                                        "deskribapena" to item.optString("deskribapena", ""),
                                        "kategoria" to item.optString("kategoria", "Categoría desconocida")
                                    ))
                                }
                            }

                            withContext(Dispatchers.Main) {
                                platos = items
                                isLoading = false
                                Log.d("PantallaMenu", "Datos cargados correctamente: ${items.size} platos")
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                errorMessage = "La lista de menú está vacía"
                                isLoading = false
                            }
                        }
                    } else {
                        val errorMsg = jsonResponse.optString("message", "Error desconocido")
                        withContext(Dispatchers.Main) {
                            errorMessage = "Error del servidor: $errorMsg"
                            isLoading = false
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Error en la respuesta del servidor: ${response.code}"
                        isLoading = false
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PantallaMenu", "Error cargando menú: ${e.message}")
            withContext(Dispatchers.Main) {
                errorMessage = "Error: ${e.message}"
                isLoading = false
            }
        }
    }

    // UI Principal
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_the_bull),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Cabecera
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Menua",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "$username - Mahaia: $mesaSeleccionada",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenido principal
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                if (isLoading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                } else {
                    val categoriasOrdenadas = listOf("Edaria", "Lehenengo platera", "Bigarren platera", "Postrea")

                    val categorias = platos.groupBy {
                        it["kategoria"] as? String ?: "Categoría desconocida"
                    }.toSortedMap(compareBy { categoria ->
                        categoriasOrdenadas.indexOf(categoria).takeIf { it != -1 } ?: Int.MAX_VALUE
                    })

                    categorias.forEach { (categoria, items) ->
                        stickyHeader {
                            Text(
                                text = categoria,
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(items) { plato ->
                            val id = plato["id"] as Int
                            val nombre = plato["izena"] as String
                            val precio = plato["prezioa"] as String
                            val descripcion = plato["deskribapena"] as String
                            val cantidadActual = cantidades[nombre] ?: 0

                            var showPopup by remember { mutableStateOf(false) }

                            if (showPopup) {
                                AlertDialog(
                                    onDismissRequest = { showPopup = false },
                                    title = { Text(nombre, fontWeight = FontWeight.Bold) },
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
                                    .padding(8.dp)
                                    .background(Color.White)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Columna izquierda: Nombre y lupa
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    IconButton(
                                        onClick = { showPopup = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Search, "Ver descripción", tint = Color.Gray)
                                    }

                                    Text(
                                        text = nombre,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Columna derecha: Precio y botones
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "$precio€",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Botón "+"
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    try {
                                                        // --- 1) Obtener ID de la platera ---
                                                        val platoId = withContext(Dispatchers.IO) {
                                                            val encodedName = URLEncoder.encode(nombre, "UTF-8").replace("+", "%20")
                                                            val urlId = "http://10.0.2.2/obtener_id_plato.php?izena=$encodedName"
                                                            Log.d("BotonMas", "GET → $urlId")
                                                            val response = OkHttpClient().newCall(
                                                                Request.Builder().url(urlId).get().build()
                                                            ).execute()
                                                            val body = response.body?.string()
                                                            Log.d("BotonMas", "Resp obtener_id: $body")
                                                            if (!response.isSuccessful || body.isNullOrEmpty()) {
                                                                throw IOException("Error HTTP obtener_id ${response.code}")
                                                            }
                                                            val json = JSONObject(body)
                                                            if (!json.optBoolean("success", false)) {
                                                                throw IOException("JSON error obtener_id: ${json.optString("message")}")
                                                            }
                                                            json.getInt("id")
                                                        }

                                                        Log.d("BotonMas", "ID de platera obtenido: $platoId")

                                                        // --- 2) Llamar a almazena.php para procesar el pedido ---
                                                        val almacenado = withContext(Dispatchers.IO) {
                                                            val urlAlma = "http://10.0.2.2/almazena.php?platera_id=$platoId"
                                                            Log.d("BotonMas", "GET → $urlAlma")
                                                            val response2 = OkHttpClient().newCall(
                                                                Request.Builder().url(urlAlma).get().build()
                                                            ).execute()
                                                            val body2 = response2.body?.string()
                                                            Log.d("BotonMas", "Resp almazena: $body2")
                                                            if (!response2.isSuccessful || body2.isNullOrEmpty()) {
                                                                throw IOException("Error HTTP almazena ${response2.code}")
                                                            }
                                                            val json2 = JSONObject(body2)
                                                            if (!json2.optBoolean("success", false)) {
                                                                throw IOException("JSON error almazena: ${json2.optString("message")}")
                                                            }
                                                            true
                                                        }

                                                        // --- 3) Si todo va bien, incrementamos la cantidad en pantalla ---
                                                        if (almacenado) {
                                                            Log.d("BotonMas", "Stock actualizado en servidor para platoId=$platoId")
                                                            cantidades[nombre] = cantidadActual + 1
                                                        }

                                                    } catch (e: Exception) {
                                                        // Imprime toda la traza para depurar
                                                        Log.e("BotonMas", "¡Error en el proceso de +!", e)
                                                        // (Opcional) Muestra un Snackbar o Toast al usuario
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Text(text = "+", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }


                                        // Cantidad actual
                                        Text(
                                            text = "$cantidadActual",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        // Botón "-"
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    try {
                                                        // 1) Obtener el ID de la platera en un hilo de I/O
                                                        val platoId = withContext(Dispatchers.IO) {
                                                            val encodedName = URLEncoder.encode(nombre, "UTF-8").replace("+", "%20")
                                                            val urlId = "http://10.0.2.2/obtener_id_plato.php?izena=$encodedName"
                                                            Log.d("BotonMenos", "GET → $urlId")
                                                            val response = OkHttpClient().newCall(
                                                                Request.Builder().url(urlId).get().build()
                                                            ).execute()
                                                            val body = response.body?.string()
                                                            Log.d("BotonMenos", "Resp obtener_id: $body")
                                                            if (!response.isSuccessful || body.isNullOrEmpty()) {
                                                                throw IOException("HTTP obtener_id error ${response.code}")
                                                            }
                                                            val json = JSONObject(body)
                                                            if (!json.optBoolean("success", false)) {
                                                                throw IOException("JSON error obtener_id: ${json.optString("message")}")
                                                            }
                                                            json.getInt("id")
                                                        }

                                                        Log.d("BotonMenos", "ID de platera obtenido: $platoId")

                                                        // 2) Llamada a almazena2.php para incrementar stock en servidor
                                                        val rollbackOk = withContext(Dispatchers.IO) {
                                                            val urlAlma2 = "http://10.0.2.2/almazena2.php?platera_id=$platoId"
                                                            Log.d("BotonMenos", "GET → $urlAlma2")
                                                            val response2 = OkHttpClient().newCall(
                                                                Request.Builder().url(urlAlma2).get().build()
                                                            ).execute()
                                                            val body2 = response2.body?.string()
                                                            Log.d("BotonMenos", "Resp almazena2: $body2")
                                                            if (!response2.isSuccessful || body2.isNullOrEmpty()) {
                                                                throw IOException("HTTP almazena2 error ${response2.code}")
                                                            }
                                                            val json2 = JSONObject(body2)
                                                            if (!json2.optBoolean("success", false)) {
                                                                throw IOException("JSON error almazena2: ${json2.optString("message")}")
                                                            }
                                                            true
                                                        }

                                                        // 3) Si todo fue bien, actualizamos la UI en Main Thread
                                                        if (rollbackOk) {
                                                            Log.d("BotonMenos", "Stock incrementado en servidor para platoId=$platoId")
                                                            withContext(Dispatchers.Main) {
                                                                // Evitamos bajar de cero
                                                                val nueva = (cantidades[nombre] ?: 0).coerceAtLeast(1) - 1
                                                                cantidades[nombre] = nueva
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        // Ver traza completa en Logcat
                                                        Log.e("BotonMenos", "¡Error en el proceso de –!", e)
                                                        // (Opcional) notificar al usuario con un Snackbar o Toast
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Text(text = "-", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Botones de navegación inferiores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Botón "Atrás" (inferior izquierda)
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = Modifier.size(width = 120.dp, height = 48.dp)
                ) {
                    Text(
                        text = "Atzera",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón "Jarraitu" (inferior derecha)
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (cantidades.isNotEmpty()) {
                                val itemsArray = JSONArray().apply {
                                    cantidades.forEach { (nombre, cantidad) ->
                                        val plato = platos.firstOrNull { it["izena"] == nombre }
                                        if (plato != null) {
                                            put(JSONObject().apply {
                                                put("platera_id", plato["id"] as Int)
                                                put("nombre", nombre)
                                                put("cantidad", cantidad)
                                                put("precio", (plato["prezioa"] as String).toFloat())
                                                put("nota", "")
                                            })
                                        }
                                    }
                                }

                                val encoded = URLEncoder.encode(itemsArray.toString(), "UTF-8").replace("+", "%20")
                                navController.navigate(
                                    "pantallaFactura/$encoded/$username/$mesaSeleccionada"
                                )
                            } else {
                                errorMessage = "Debe seleccionar al menos un plato"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)),
                    modifier = Modifier.size(width = 120.dp, height = 48.dp)
                ) {
                    Text(
                        text = "Jarraitu",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

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
                        val id = plato["platera_id"] as Int
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
                title = { Text(text = "Gehitu oharra", color = Color.Black) },
                text = {
                    Column {
                        TextField(
                            value = currentNote.value,
                            onValueChange = { currentNote.value = it },
                            placeholder = { Text("Idatzi ohar bat...") }
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
                        Text(text = "Gorde", fontSize = 16.sp)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { noteDialogState.value = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text(text = "Atzera", fontSize = 16.sp)
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
                    put("platera_id", item["platera_id"] as? Int ?: 0)
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