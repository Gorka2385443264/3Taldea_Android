<?php
$servername = "localhost:3306";
$username = "root";
$password = "1WMG2023";
$dbname = "3taldea";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Conexión fallida: " . $conn->connect_error);
}

// Obtener el siguiente eskaeraZenb
$query = "SELECT MAX(eskaeraZenb) as max_eskaera FROM eskaera";
$result = mysqli_query($conn, $query);

if ($row = mysqli_fetch_assoc($result)) {
    $nextEskaeraZenb = $row['max_eskaera'] ? $row['max_eskaera'] + 1 : 1;
} else {
    $nextEskaeraZenb = 1;
}

// Recoger datos del cuerpo JSON
$data = json_decode(file_get_contents("php://input"), true);
$mesa_id = $data['mesa_id'];
$platera_items = $data['platera_items'];

$totalPrecio = 0;
$nombrePlato = '';

$conn->begin_transaction();

try {
    // Calcular el nombre del primer plato y el precio total
    foreach ($platera_items as $item) {
        $nombrePlato = $item['izena'];
        $totalPrecio += $item['precio'] * $item['cantidad'];
    }

    // INSERT en eskaera
    $eskaera_sql = "INSERT INTO eskaera (eskaeraZenb, izena, mesa_id, activo, prezioa) VALUES (?, ?, ?, 1, ?)";
    $stmt = $conn->prepare($eskaera_sql);
    $stmt->bind_param("isid", $nextEskaeraZenb, $nombrePlato, $mesa_id, $totalPrecio);
    $stmt->execute();

    // Obtener el ID real insertado (clave primaria de 'eskaera')
    $eskaera_inserted_id = $conn->insert_id;

    // INSERT en eskaera_platera por cada item
    foreach ($platera_items as $item) {
        $platera_id = $item['platera_id'];
        $nota = $item['nota'];
        $nota = trim($nota) === "" ? null : $nota;

        $platera_sql = "INSERT INTO eskaera_platera 
            (eskaera_id, platera_id, nota_gehigarriak, eskaeraOrdua, ateratzeOrdua, egoera, done) 
            VALUES (?, ?, ?, NOW(), NULL, 0, 0)";
        $stmt = $conn->prepare($platera_sql);
        $stmt->bind_param("iis", $eskaera_inserted_id, $platera_id, $nota);
        $stmt->execute();
    }

    $conn->commit();
    echo json_encode(['success' => true, 'message' => 'Comanda y platos insertados correctamente']);
} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(['success' => false, 'message' => 'Error al insertar la comanda: ' . $e->getMessage()]);
}

$conn->close();
?>