<?php
header('Content-Type: application/json');

// Registrar en el log de Apache que el archivo PHP ha sido accedido
error_log("Archivo obtener_id_plato.php accedido");

// Configuración de la base de datos
$host = "localhost:3306";
$dbname = "3taldea";
$username = 'root';
$password = '1WMG2023';

// Conexión a la base de datos
$conn = new mysqli($host, $username, $password, $dbname);

// Registrar en el log de Apache si hay un error de conexión
if ($conn->connect_error) {
    error_log("Error de conexión: " . $conn->connect_error);
    die(json_encode(["success" => false, "message" => "Error de conexión: " . $conn->connect_error]));
}

// Verificar si se proporcionó el parámetro 'izena'
if (!isset($_GET['izena'])) {
    error_log("Falta el parámetro izena");
    echo json_encode(["success" => false, "message" => "Falta el parámetro izena"]);
    exit();
}

// Obtener el nombre del plato
$izena = urldecode($_GET['izena']);

// Registrar en el log de Apache el valor recibido para 'izena'
error_log("Valor recibido para izena: " . $izena);

// Consulta SQL para obtener el ID del plato
$sql = "SELECT id FROM platera WHERE izena = ?";
$stmt = $conn->prepare($sql);

if ($stmt === false) {
    error_log("Error al preparar la consulta SQL: " . $conn->error);
    echo json_encode(["success" => false, "message" => "Error al preparar la consulta SQL"]);
    exit();
}

$stmt->bind_param("s", $izena);
$stmt->execute();
$result = $stmt->get_result();

// Registrar en el log de Apache si no se encontró el plato
if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    error_log("Plato encontrado - ID: " . $row['id']);
    echo json_encode(["success" => true, "id" => $row['id']]);
} else {
    error_log("No se encontró el plato con izena: " . $izena);
    echo json_encode(["success" => false, "message" => "No se encontró el plato"]);
}

// Cerrar la conexión
$conn->close();
?>