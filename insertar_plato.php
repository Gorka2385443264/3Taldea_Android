<?php
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

$mesa_id = $data['mesa_id'];
$eskaeraZenb = $data['eskaeraZenb'];
$platos = $data['platos'];

$conn = new mysqli("localhost:3306", "root", "1WMG2023", "3taldea");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit;
}

try {
    // Paso 1: Insertar en la tabla `eskaera`
    foreach ($platos as $plato) {
        $platoData = explode(":", $plato);
        $platera_id = $platoData[0];
        $izena = $platoData[1]; // Nombre del plato
        $prezioa = $platoData[3]; // Precio del plato

        // Insertar en la tabla `eskaera`
        $stmtEskaera = $conn->prepare("INSERT INTO eskaera (eskaeraZenb, izena, prezioa, mesa_id, activo) VALUES (?, ?, ?, ?, ?)");
        $activo = 1; // Suponemos que la eskaera está activa al crearla
        $stmtEskaera->bind_param("isdii", $eskaeraZenb, $izena, $prezioa, $mesa_id, $activo);
        $stmtEskaera->execute();

        // Obtener el ID de la eskaera recién creada
        $eskaera_id = $stmtEskaera->insert_id;

        // Paso 2: Insertar en la tabla `eskaera_platera` con valores predeterminados para `egoera` y `done`
        $stmtPlatera = $conn->prepare("INSERT INTO eskaera_platera (eskaera_id, platera_id, eskaeraOrdua, egoera, done) VALUES (?, ?, NOW(), 0, 0)");
        $stmtPlatera->bind_param("ii", $eskaera_id, $platera_id);
        $stmtPlatera->execute();
    }

    echo json_encode(["success" => true]);
} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}

$conn->close();
?>