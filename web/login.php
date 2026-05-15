<?php
// CORS and JSON Headers
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json; charset=UTF-8");

require 'db.php';

// Get JSON input
$data = json_decode(file_get_contents("php://input"));

if (!isset($data->username) || !isset($data->password)) {
    echo json_encode(["error" => "Username and password required"]);
    exit();
}

$username = $data->username;
$password = $data->password;

// Use Prepared Statement for SQL Injection prevention
$stmt = $conn->prepare("SELECT id, username, password, plan, expiry_date FROM users WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    if (password_verify($password, $row['password'])) {
        echo json_encode([
            "message" => "Login successful",
            "username" => $row['username'],
            "plan" => $row['plan'],
            "expiry" => $row['expiry_date']
        ]);
    }
    else {
        http_response_code(401);
        echo json_encode(["error" => "Invalid credentials"]);
    }
}
else {
    http_response_code(401);
    echo json_encode(["error" => "Invalid credentials"]);
}

$stmt->close();
$conn->close();
?>
