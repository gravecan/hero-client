<?php
// CORS and JSON Headers
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json; charset=UTF-8");

require 'db.php';

// Get JSON input
$data = json_decode(file_get_contents("php://input"));

if (!isset($data->username)) {
    echo json_encode(["valid" => false, "error" => "No username provided"]);
    exit();
}

$username = $data->username;

// Use Prepared Statement to Validate Username
$stmt = $conn->prepare("SELECT plan, expiry_date, discord_username, discord_avatar, discord_id FROM users WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    // User exists
    $row = $result->fetch_assoc();
    echo json_encode([
        "valid" => true,
        "username" => $username,
        "plan" => $row['plan'],
        "expiry" => $row['expiry_date'],
        "discord_username" => $row['discord_username'],
        "discord_id" => $row['discord_id'],
        "discord_avatar" => $row['discord_avatar']
    ]);
}
else {
    // User does not exist
    echo json_encode([
        "valid" => false,
        "error" => "User not found"
    ]);
}

$stmt->close();
$conn->close();
?>
