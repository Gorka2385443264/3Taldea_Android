<?php

$host = "localhost:3306";
$dbname = '3taldea';
$username = 'root';
$password = '1WMG2023';

// Conexión a la base de datos
try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    echo json_encode(['error' => 'Connection failed: ' . $e->getMessage()]);
    exit;
}

// Recibimos los datos de la solicitud POST
$data = json_decode(file_get_contents('php://input'), true);

// Comprobamos si el username fue enviado
if (isset($data['username'])) {
    $username = $data['username'];

    // Consulta para obtener el id del usuario
    $query = "SELECT id FROM langilea WHERE izena = :username LIMIT 1";
    $stmt = $pdo->prepare($query);
    $stmt->bindParam(':username', $username, PDO::PARAM_STR);

    if ($stmt->execute()) {
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($user) {
            // Enviamos el id del usuario
            echo json_encode(['id' => $user['id']]);
        } else {
            // Si no se encuentra el usuario, devolvemos un error
            echo json_encode(['error' => 'Usuario no encontrado']);
        }
    } else {
        echo json_encode(['error' => 'Error al ejecutar la consulta']);
    }
} else {
    echo json_encode(['error' => 'Faltan parámetros']);
}
?>