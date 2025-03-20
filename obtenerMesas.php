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

// Consultar todas las mesas
$sql = "SELECT id, habilitado FROM mahaia";
$result = $conn->query($sql);

$mesas = [];

if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $mesas[] = [
            "id" => $row['id'],
            "habilitado" => (bool) $row['habilitado']
        ];
    }

    echo json_encode(["success" => true, "mesas" => $mesas]);
} else {
    echo json_encode(["success" => false, "message" => "No se encontraron mesas"]);
}

$conn->close();
?>