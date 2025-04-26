<?php
header("Content-Type: application/json");

$data = json_decode(file_get_contents("php://input"), true);
$izena = $data['izena'];
$eskaeraZenb = $data['eskaeraZenb'];

$conn = new mysqli("localhost:3306", "root", "1WMG2023", "3taldea");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit;
}

try {
    // Paso 1: Obtener platera_id desde platera
    $stmt = $conn->prepare("SELECT id FROM platera WHERE izena = ?");
    $stmt->bind_param("s", $izena);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        echo json_encode(["success" => false, "message" => "Plato no encontrado"]);
        exit;
    }
    $platera_id = $result->fetch_assoc()['id'];

    // Paso 2: Obtener eskaera_id desde eskaera_platera
    $stmt = $conn->prepare("
        SELECT e.id AS eskaera_id 
        FROM eskaera e
        JOIN eskaera_platera ep ON e.id = ep.eskaera_id
        WHERE e.eskaeraZenb = ? AND ep.platera_id = ?
    ");
    $stmt->bind_param("ii", $eskaeraZenb, $platera_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        echo json_encode(["success" => false, "message" => "Plato no encontrado en la orden"]);
        exit;
    }
    $eskaera_id = $result->fetch_assoc()['eskaera_id'];

    // Paso 3: Verificar estados
    $stmt = $conn->prepare("
        SELECT egoera, done 
        FROM eskaera_platera 
        WHERE eskaera_id = ? AND platera_id = ?
    ");
    $stmt->bind_param("ii", $eskaera_id, $platera_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $plato = $result->fetch_assoc();

    if ($plato['egoera'] == 1 || $plato['done'] == 1) {
        echo json_encode(["success" => false, "message" => "No se puede eliminar: Plato finalizado/entregado"]);
        exit;
    }

    // Paso 4: Eliminar de eskaera_platera
    $stmt = $conn->prepare("DELETE FROM eskaera_platera WHERE eskaera_id = ? AND platera_id = ?");
    $stmt->bind_param("ii", $eskaera_id, $platera_id);
    $stmt->execute();

    // Paso 5: Verificar si quedan platos en la orden
    $stmt = $conn->prepare("SELECT COUNT(*) AS count FROM eskaera_platera WHERE eskaera_id = ?");
    $stmt->bind_param("i", $eskaera_id);
    $stmt->execute();
    $count = $stmt->get_result()->fetch_assoc()['count'];

    if ($count == 0) {
        // Eliminar la orden de eskaera
        $stmt = $conn->prepare("DELETE FROM eskaera WHERE id = ?");
        $stmt->bind_param("i", $eskaera_id);
        $stmt->execute();
    }

    echo json_encode(["success" => true]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}

$conn->close();
?>