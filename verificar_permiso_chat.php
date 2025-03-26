<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

$servername = "localhost:3306";
$username_db = "root"; 
$password_db = "1WMG2023";      
$dbname = "3taldea";   

$conn = new mysqli($servername, $username_db, $password_db, $dbname);

if ($conn->connect_error) {
    echo json_encode(["error" => "Conexión fallida: " . $conn->connect_error]);
    exit();
}

if (isset($_GET['username'])) {
    $username = $_GET['username'];

    $stmt = $conn->prepare("SELECT txatBaimena FROM langilea WHERE izena = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $stmt->bind_result($permiso);

    if ($stmt->fetch()) {
        echo json_encode(["txatBaimena" => $permiso]);
    } else {
        echo json_encode(["error" => "Usuario no encontrado"]);
    }

    $stmt->close();
} else {
    echo json_encode(["error" => "Falta el parámetro username"]);
}

$conn->close();
?>