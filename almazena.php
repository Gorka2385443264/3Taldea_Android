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

if (!isset($_GET['platera_id'])) {
    echo json_encode(["success" => false, "message" => "Falta el parámetro platera_id"]);
    exit();
}

$platera_id = intval($_GET['platera_id']);
$conn->begin_transaction();

try {
    $sql = "SELECT produktua_id, kantitatea FROM platera_produktua WHERE platera_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $platera_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        throw new Exception("No hay productos asociados a este plato");
    }

    while ($row = $result->fetch_assoc()) {
        $produktua_id = $row['produktua_id'];
        $kantitatea = $row['kantitatea'];

        $sqlStock = "SELECT stock FROM produktua WHERE id = ? FOR UPDATE";
        $stmtStock = $conn->prepare($sqlStock);
        $stmtStock->bind_param("i", $produktua_id);
        $stmtStock->execute();
        $stockRow = $stmtStock->get_result()->fetch_assoc();

        if ($stockRow['stock'] < $kantitatea) {
            throw new Exception("Stock insuficiente para el producto ID: $produktua_id");
        }

        $newStock = $stockRow['stock'] - $kantitatea;
        $sqlUpdate = "UPDATE produktua SET stock = ? WHERE id = ?";
        $stmtUpdate = $conn->prepare($sqlUpdate);
        $stmtUpdate->bind_param("ii", $newStock, $produktua_id);
        $stmtUpdate->execute();
    }

    $conn->commit();
    echo json_encode(["success" => true]);
} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}

$conn->close();
?>