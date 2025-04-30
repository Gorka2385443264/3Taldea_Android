<?php
header('Content-Type: application/json');

$host = "localhost:3306";
$dbname = "3taldea";
$username = 'root';
$password = '1WMG2023';

$conn = new mysqli($host, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Error de conexión: " . $conn->connect_error]));
}

// Consulta SQL ajustada: solo columnas existentes en la tabla platera
$sql = "SELECT id, izena, deskribapena, kategoria, prezioa, menu, deletedBy
        FROM platera
        WHERE menu = 1
        ORDER BY 
            CASE kategoria
                WHEN 'Edaria' THEN 1
                WHEN 'Lehenengo platera' THEN 2
                WHEN 'Bigarren platera' THEN 3
                WHEN 'Postrea' THEN 4
                ELSE 5
            END";

$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $items = [];
    while ($row = $result->fetch_assoc()) {
        $items[] = [
            "id" => $row['id'],
            "izena" => $row['izena'],
            "deskribapena" => $row['deskribapena'],
            "kategoria" => $row['kategoria'],
            "prezioa" => $row['prezioa'],
            "menu" => $row['menu'],
            "deletedBy" => $row['deletedBy']
        ];
    }
    echo json_encode(["success" => true, "menu" => $items]); // Clave "menu" en la respuesta
} else {
    echo json_encode(["success" => false, "message" => "No se encontraron datos"]);
}

$conn->close();
?>