<?php
// Conexión a la base de datos
$host = "localhost:3306";
$dbname = "3taldea";
$username = 'root';
$password = '1WMG2023';

$conn = new mysqli($host, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    error_log("Error de conexión: " . $conn->connect_error);
    die(json_encode(["success" => false, "message" => "Error de conexión: " . $conn->connect_error]));
}

error_log("Conexión exitosa a la base de datos");

// Consultar todas las mesas
$sql = "SELECT id, habilitado FROM mahaia";
$result = $conn->query($sql);

if ($result === false) {
    error_log("Error en la consulta SQL: " . $conn->error);
    die(json_encode(["success" => false, "message" => "Error en la consulta SQL"]));
}

$mesas = [];

if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $mesaId = $row['id'];

        // Verificar si la mesa tiene una eskaera activa
        $sqlEskaera = "SELECT COUNT(*) AS count FROM eskaera WHERE mesa_id = ? AND activo = 1";
        $stmt = $conn->prepare($sqlEskaera);
        if ($stmt === false) {
            error_log("Error al preparar la consulta de eskaera: " . $conn->error);
            continue;
        }
        $stmt->bind_param("i", $mesaId);
        $stmt->execute();
        $resultEskaera = $stmt->get_result();
        if ($resultEskaera === false) {
            error_log("Error en la consulta de eskaera: " . $stmt->error);
            continue;
        }
        $rowEskaera = $resultEskaera->fetch_assoc();
        $tieneEskaeraActiva = $rowEskaera['count'] > 0;

        $mesas[] = [
            "id" => $mesaId,
            "habilitado" => (bool) $row['habilitado'],
            "tieneEskaeraActiva" => (bool) $tieneEskaeraActiva
        ];
    }

    error_log("Datos de mesas generados: " . json_encode($mesas));
    echo json_encode(["success" => true, "mesas" => $mesas]);
} else {
    error_log("No se encontraron mesas");
    echo json_encode(["success" => false, "message" => "No se encontraron mesas"]);
}

$conn->close();
?>