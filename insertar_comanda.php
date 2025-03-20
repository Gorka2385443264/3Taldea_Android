<?php
// Configuración de la conexión a la base de datos
$servername = "localhost:3306"; // Usar IP de XAMPP en el emulador de Android
$username = "root";
$password = "1WMG2023"; // Cambiar según corresponda
$dbname = "5_erronka1";

// Crear la conexión
$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    die("Conexión fallida: " . $conn->connect_error);
}

// Recibir datos JSON
$data = json_decode(file_get_contents("php://input"), true);

// Log de los datos recibidos
error_log("Datos recibidos: " . print_r($data, true));

// Verificar datos requeridos
if (!isset($data['langilea_id']) || !isset($data['mahaila_id']) || !isset($data['platera_items'])) {
    echo json_encode(['success' => false, 'message' => 'Faltan datos necesarios']);
    exit();
}

$langilea_id = $data['langilea_id'];
$mahaila_id = $data['mahaila_id'];
$platera_items = $data['platera_items'];

// Insertar en la tabla eskaera
$sql = "INSERT INTO eskaera (langilea_id, mahaila_id) VALUES (?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ii", $langilea_id, $mahaila_id);

if ($stmt->execute()) {
    // Obtener el ID generado para eskaera
    $eskaera_id = $stmt->insert_id;

    if (!$eskaera_id) {
        echo json_encode(['success' => false, 'message' => 'No se pudo generar eskaera_id']);
        exit();
    }

    error_log("Eskaera ID generado: " . $eskaera_id);

    // Verificar si el eskaera_id existe en la tabla eskaera antes de insertar en eskaera_platera
    $sql_check_eskaera = "SELECT id FROM eskaera WHERE id = ?";
    $stmt_check = $conn->prepare($sql_check_eskaera);
    $stmt_check->bind_param("i", $eskaera_id);
    $stmt_check->execute();
    $stmt_check->store_result();

    if ($stmt_check->num_rows == 0) {
        echo json_encode(['success' => false, 'message' => 'eskaera_id no existe en la base de datos']);
        exit();
    }

    // Insertar en la tabla eskaera_platera
    foreach ($platera_items as $item) {
        $platera_id = $item['platera_id'];
        $nota = isset($item['nota']) && !empty($item['nota']) ? $item['nota'] : null; // Insertar NULL si la nota está vacía
        $cantidad = isset($item['cantidad']) ? $item['cantidad'] : 1; // Por defecto, 1 si no se especifica

        // Hacer inserciones según la cantidad
        for ($i = 0; $i < $cantidad; $i++) {
            // Insertar con NULL para ateratze_ordua
            $sql_item = "INSERT INTO eskaera_platera (eskaera_id, platera_id, nota_gehigarriak, ateratze_ordua) 
                         VALUES (?, ?, ?, ?)";
            $ateratze_ordua = null; // Asignar NULL aquí
            $stmt_item = $conn->prepare($sql_item);
            $stmt_item->bind_param("iiss", $eskaera_id, $platera_id, $nota, $ateratze_ordua);

            if (!$stmt_item->execute()) {
                error_log("Error al insertar en eskaera_platera. Eskaera ID: $eskaera_id, Error: " . $stmt_item->error);
                echo json_encode(['success' => false, 'message' => 'Error al insertar plato', 'error' => $stmt_item->error]);
                exit();
            }
        }
    }
    // Todo correcto
    echo json_encode(['success' => true, 'message' => 'Comanda registrada con éxito']);
} else {
    error_log("Error al insertar en eskaera: " . $stmt->error);
    echo json_encode(['success' => false, 'message' => 'Error al insertar comanda', 'error' => $stmt->error]);
    exit();
}

// Cerrar conexión
$conn->close();
?>