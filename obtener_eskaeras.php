<?php
header('Content-Type: application/json');
$conexion = new mysqli("localhost:3306", "root", "1WMG2023", "3taldea");

$mesa_id = isset($_GET['mesa_id']) ? intval($_GET['mesa_id']) : 0;

if ($mesa_id <= 0) {
    echo json_encode([
        "success" => false,
        "message" => "Mesa ID no válido"
    ]);
    exit();
}

// Log: Mesa ID recibido
error_log("Mesa ID recibido: $mesa_id");

$stmt = $conexion->prepare("
    SELECT e.eskaeraZenb, e.izena, e.prezioa, p.nota_gehigarriak, p.eskaeraOrdua
    FROM eskaera e
    JOIN eskaera_platera p ON e.id = p.eskaera_id
    WHERE e.mesa_id = ? AND e.activo = 1
    ORDER BY e.eskaeraZenb DESC
");

$stmt->bind_param("i", $mesa_id);
$stmt->execute();
$resultado = $stmt->get_result();

$eskaerak = [];

while ($fila = $resultado->fetch_assoc()) {
    $zenb = $fila['eskaeraZenb'];
    if (!isset($eskaerak[$zenb])) {
        $eskaerak[$zenb] = [
            "eskaeraZenb" => $zenb,
            "platos" => []
        ];
    }
    $eskaerak[$zenb]["platos"][] = [
        "izena" => $fila["izena"],
        "prezioa" => $fila["prezioa"],
        "nota_gehigarriak" => $fila["nota_gehigarriak"],
        "eskaeraOrdua" => $fila["eskaeraOrdua"]
    ];
}

// Log: Verificar que los datos se hayan recuperado correctamente
error_log("Datos recuperados: " . json_encode($eskaerak));

// Reindexar el array para que no tenga claves personalizadas
$eskaerak = array_values($eskaerak);

echo json_encode([
    "success" => true,
    "eskaeras" => $eskaerak
]);

$stmt->close();
$conexion->close();

?>