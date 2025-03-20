<?php
// Configuración de la base de datos
$host = "localhost:3306";
$dbname = "5_erronka1";
$username = 'root';
$password = '1WMG2023';

// Crear la conexión
$conn = new mysqli($host, $username, $password, $dbname);

// Comprobar la conexión
if ($conn->connect_error) {
    die("Conexión fallida: " . $conn->connect_error);
}

// Obtener el ID del plato seleccionado desde la solicitud GET
if (isset($_GET['plato_id'])) {
    $plato_id = $_GET['plato_id'];

    // Consultar la tabla almazena_platera para obtener almazena_id y cantidad del plato
    $sql = "SELECT almazena_id, kantitatea FROM almazena_platera WHERE platera_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $plato_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $almazena_stock = [];

    // Para cada fila de la tabla almazena_platera, obtener el stock de la tabla almazena
    while ($row = $result->fetch_assoc()) {
        $almazena_id = $row['almazena_id'];
        $cantidad = $row['kantitatea'];

        // Consultar el stock de la tabla almazena
        $sql_stock = "SELECT stock FROM almazena WHERE id = ?";
        $stmt_stock = $conn->prepare($sql_stock);
        $stmt_stock->bind_param("i", $almazena_id);
        $stmt_stock->execute();
        $stock_result = $stmt_stock->get_result();

        // Obtener el valor del stock
        if ($stock_row = $stock_result->fetch_assoc()) {
            $stock = $stock_row['stock'];
            $almazena_stock[] = [
                'almazena_id' => $almazena_id,
                'cantidad' => $cantidad,
                'stock' => $stock
            ];
        }
    }

    // Responder con los datos obtenidos
    if (!empty($almazena_stock)) {
        echo json_encode(['success' => true, 'data' => $almazena_stock]);
    } else {
        echo json_encode(['success' => false, 'message' => 'No se encontraron datos para el plato']);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'No se proporcionó el ID del plato']);
}

// Cerrar la conexión
$conn->close();
?>