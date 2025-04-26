<?php
header('Content-Type: application/json');
$host = "localhost:3306";
$dbname = "3taldea";
$username = 'root';
$password = '1WMG2023';

$conn = new mysqli($host, $username, $password, $dbname);

if ($conn->connect_error) {
    error_log("Error de conexión: " . $conn->connect_error);
    die(json_encode(["success" => false, "message" => "Error de conexión: " . $conn->connect_error]));
}

if (!isset($_GET['platera_id'])) {
    error_log("Falta el parámetro platera_id");
    echo json_encode(["success" => false, "message" => "Falta el parámetro platera_id"]);
    exit();
}

$platera_id = intval($_GET['platera_id']);
error_log("Platera ID recibido: $platera_id");

$conn->begin_transaction();

try {
    // Paso 1: Obtener los productos asociados al plato
    $sql = "SELECT produktua_id, kantitatea FROM platera_produktua WHERE platera_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $platera_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        throw new Exception("No hay productos asociados al plato con ID: $platera_id");
    }

    while ($row = $result->fetch_assoc()) {
        $produktua_id = $row['produktua_id'];
        $kantitatea = $row['kantitatea'];

        error_log("Producto ID obtenido: $produktua_id, Cantidad necesaria: $kantitatea");

        // Paso 2: Verificar el stock del producto
        $sqlStock = "SELECT stock FROM produktua WHERE id = ? FOR UPDATE";
        $stmtStock = $conn->prepare($sqlStock);
        $stmtStock->bind_param("i", $produktua_id);
        $stmtStock->execute();
        $stockRow = $stmtStock->get_result()->fetch_assoc();

        if ($stockRow['stock'] < $kantitatea) {
            throw new Exception("Stock insuficiente para el producto ID: $produktua_id");
        }

        error_log("Stock actual del producto ID $produktua_id: " . $stockRow['stock']);

        // Paso 3: Actualizar el stock
        $newStock = $stockRow['stock'] - $kantitatea;
        $sqlUpdate = "UPDATE produktua SET stock = ? WHERE id = ?";
        $stmtUpdate = $conn->prepare($sqlUpdate);
        $stmtUpdate->bind_param("ii", $newStock, $produktua_id);
        $stmtUpdate->execute();

        error_log("Stock actualizado para el producto ID $produktua_id. Nuevo stock: $newStock");
    }

    $conn->commit();
    echo json_encode(["success" => true]);
} catch (Exception $e) {
    $conn->rollback();
    error_log("Error durante la transacción: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}

$conn->close();
?>