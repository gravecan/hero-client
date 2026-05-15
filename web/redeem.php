<?php
// ENFORCE ERROR REPORTING FOR DEBUGGING
error_reporting(E_ALL);
ini_set('display_errors', 1);

// CORS and JSON Headers
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json; charset=UTF-8");

// Custom Error Handler to return JSON
set_error_handler(function ($errno, $errstr, $errfile, $errline) {
    http_response_code(500);
    echo json_encode(["error" => "PHP Error: $errstr in $errfile:$errline"]);
    exit();
});

set_exception_handler(function ($e) {
    http_response_code(500);
    echo json_encode(["error" => "Exception: " . $e->getMessage()]);
    exit();
});

require 'db.php';

try {
    // Get JSON input
    $input = file_get_contents("php://input");
    if (empty($input)) {
        throw new Exception("No input data received.");
    }

    $data = json_decode($input);
    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception("Invalid JSON received: " . json_last_error_msg());
    }

    if (!isset($data->username) || !isset($data->key)) {
        throw new Exception("Key and username are required");
    }

    $username = $data->username;
    $key = $data->key;

    // 1. Check if user exists (Prepared Statement)
    $userStmt = $conn->prepare("SELECT id FROM users WHERE username = ?");
    $userStmt->bind_param("s", $username);
    $userStmt->execute();
    $userResult = $userStmt->get_result();

    if ($userResult->num_rows === 0) {
        throw new Exception("User not found");
    }
    $userStmt->close();

    // 2. Verify License Key (Prepared Statement)
    // Assuming table 'licenses' with columns: license_key, status ('unused', 'used'), plan_type, duration_days
    $keyStmt = $conn->prepare("SELECT * FROM licenses WHERE license_key = ? AND status = 'unused'");
    $keyStmt->bind_param("s", $key);
    $keyStmt->execute();
    $result = $keyStmt->get_result();

    if ($result->num_rows > 0) {
        $license = $result->fetch_assoc();
        $planType = $license['plan_type']; // e.g. 'lifetime', 'monthly'

        // Calculate expiry
        $expiry = null;
        if ($planType !== 'lifetime') {
            $days = isset($license['duration_days']) ? (int)$license['duration_days'] : 30;
            $expiry = date('Y-m-d H:i:s', strtotime("+$days days"));
        }

        // 3. Update User (Prepared Statement)
        // Handle NULL expiry
        $updateUserStmt = $conn->prepare("UPDATE users SET plan = ?, expiry_date = ? WHERE username = ?");
        $updateUserStmt->bind_param("sss", $planType, $expiry, $username);

        if ($updateUserStmt->execute()) {

            // 4. Mark Key as Used (Prepared Statement)
            $updateKeyStmt = $conn->prepare("UPDATE licenses SET status = 'used', used_by = ?, used_at = NOW() WHERE license_key = ?");
            $updateKeyStmt->bind_param("ss", $username, $key);
            $updateKeyStmt->execute();

            echo json_encode([
                "success" => true,
                "message" => "License redeemed successfully!",
                "plan" => $planType,
                "expiry" => $expiry
            ]);
            $updateKeyStmt->close();
        }
        else {
            throw new Exception("Failed to update user plan: " . $conn->error);
        }
        $updateUserStmt->close();
    }
    else {
        throw new Exception("Invalid or already used license key");
    }
    $keyStmt->close();

    $conn->close();

}
catch (Throwable $e) {
    http_response_code(500);
    echo json_encode(["error" => $e->getMessage()]);
}
?>
