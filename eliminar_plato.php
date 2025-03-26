<?php
header("Content-Type: application/json");

// Obtener los datos del cuerpo de la solicitud
$data = json_decode(file_get_contents("php://input"), true);

$izena = $data['izena'];
$eskaeraZenb = $data['eskaeraZenb'];

// Conexión a la base de datos
$conn = new mysqli("localhost:3306", "root", "1WMG2023", "3taldea");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit;
}

try {
    // Paso 1: Obtener el ID de la tabla eskaera
    $stmt = $conn->prepare("SELECT id FROM eskaera WHERE eskaeraZenb = ? AND izena = ?");
    $stmt->bind_param("is", $eskaeraZenb, $izena);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $eskaera_id = $row['id'];

        // Paso 2: Eliminar registros en eskaera_platera
        $stmt = $conn->prepare("DELETE FROM eskaera_platera WHERE eskaera_id = ?");
        $stmt->bind_param("i", $eskaera_id);
        $stmt->execute();

        // Paso 3: Eliminar el registro en eskaera
        $stmt = $conn->prepare("DELETE FROM eskaera WHERE id = ?");
        $stmt->bind_param("i", $eskaera_id);
        $stmt->execute();

        echo json_encode(["success" => true]);
    } else {
        echo json_encode(["success" => false, "message" => "No se encontró el registro"]);
    }
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}

$conn->close();
?>