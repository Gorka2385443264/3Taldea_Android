<?php
// Conexión a la base de datos
$host = "localhost:3306";
$dbname = "3taldea";
$username = 'root';
$password = '1WMG2023';

$conn = new mysqli($host, $username, $password, $dbname);


// Verificar conexión
if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Error de conexión: " . $conn->connect_error]));
}

// Obtener datos enviados desde la app
$email = $_POST['email'] ?? '';
$pasahitza = $_POST['pasahitza'] ?? '';


// Validar que se recibieron datos
if (empty($email) || empty($pasahitza)) {
    echo json_encode(["success" => false, "message" => "Faltan datos"]);
    exit();
}



// Consultar la base de datos
$sql = "SELECT izena FROM langilea WHERE korreoa = ? AND pasahitza = ? AND deletedData IS NULL";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ss", $email, $pasahitza);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo json_encode(["success" => true, "izena" => $row['izena']]);
} else {
    echo json_encode(["success" => false, "message" => "Credenciales incorrectas o usuario desactivado"]);
}

$stmt->close();
$conn->close();

?>