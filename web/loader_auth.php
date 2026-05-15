<?php
// CORS and JSON Headers
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json; charset=UTF-8");

require 'db.php';

// Get JSON input
$data = json_decode(file_get_contents("php://input"));

// API Key protection - only the loader knows this key
$API_KEY = "hR9x$2mKpL7vNw4QdZf8bYcA3jT6eXsU";

if (!isset($data->api_key) || $data->api_key !== $API_KEY) {
    http_response_code(403);
    echo json_encode(["status" => "error", "code" => "unauthorized"]);
    exit();
}

if (!isset($data->action)) {
    http_response_code(400);
    echo json_encode(["status" => "error", "code" => "missing_action"]);
    exit();
}

$ip = $_SERVER['REMOTE_ADDR'];

// ============================================
// ACTION 1: Check HWID (auto-login)
// ============================================
if ($data->action === "check_hwid") {
    if (!isset($data->hwid) || empty($data->hwid)) {
        http_response_code(400);
        echo json_encode(["status" => "error", "code" => "missing_hwid"]);
        exit();
    }

    $hwid = $data->hwid;

    $stmt = $conn->prepare("SELECT id, username, plan, expiry_date FROM users WHERE hwid = ?");
    $stmt->bind_param("s", $hwid);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        // HWID not found - needs login
        echo json_encode(["status" => "need_login"]);
        $stmt->close();
        $conn->close();
        exit();
    }

    $row = $result->fetch_assoc();
    $stmt->close();

    // Check license
    $plan = strtolower($row['plan'] ?? 'free');
    if ($plan === 'free' || $plan === 'null' || $plan === '' || $plan === null) {
        http_response_code(403);
        echo json_encode(["status" => "error", "code" => "no_license", "message" => "No active license"]);
        $conn->close();
        exit();
    }

    // Check expiry
    if ($plan !== 'lifetime' && !empty($row['expiry_date'])) {
        $expiry = strtotime($row['expiry_date']);
        if ($expiry !== false && $expiry < time()) {
            http_response_code(403);
            echo json_encode(["status" => "error", "code" => "expired", "message" => "License expired"]);
            $conn->close();
            exit();
        }
    }

    // Update IP
    $update = $conn->prepare("UPDATE users SET ip = ? WHERE id = ?");
    $update->bind_param("si", $ip, $row['id']);
    $update->execute();
    $update->close();

    // Success - HWID found with active license
    echo json_encode([
        "status" => "success",
        "username" => $row['username'],
        "plan" => $row['plan']
    ]);
    $conn->close();
    exit();
}

// ============================================
// ACTION 2: Login + bind HWID
// ============================================
if ($data->action === "login") {
    if (!isset($data->username) || !isset($data->password) || !isset($data->hwid)) {
        http_response_code(400);
        echo json_encode(["status" => "error", "code" => "missing_fields"]);
        exit();
    }

    $username = $data->username;
    $password = $data->password;
    $hwid = $data->hwid;

    // Verify credentials
    $stmt = $conn->prepare("SELECT id, username, password, plan, expiry_date, hwid FROM users WHERE username = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        http_response_code(401);
        echo json_encode(["status" => "error", "code" => "invalid_credentials", "message" => "Invalid username or password"]);
        $stmt->close();
        $conn->close();
        exit();
    }

    $row = $result->fetch_assoc();
    $stmt->close();

    // Verify password
    if (!password_verify($password, $row['password'])) {
        http_response_code(401);
        echo json_encode(["status" => "error", "code" => "invalid_credentials", "message" => "Invalid username or password"]);
        $conn->close();
        exit();
    }

    // Check license
    $plan = strtolower($row['plan'] ?? 'free');
    if ($plan === 'free' || $plan === 'null' || $plan === '' || $plan === null) {
        http_response_code(403);
        echo json_encode(["status" => "error", "code" => "no_license", "message" => "No active license"]);
        $conn->close();
        exit();
    }

    // Check expiry
    if ($plan !== 'lifetime' && !empty($row['expiry_date'])) {
        $expiry = strtotime($row['expiry_date']);
        if ($expiry !== false && $expiry < time()) {
            http_response_code(403);
            echo json_encode(["status" => "error", "code" => "expired", "message" => "License expired"]);
            $conn->close();
            exit();
        }
    }

    // Check if account already has a different HWID
    if (!empty($row['hwid']) && $row['hwid'] !== $hwid) {
        http_response_code(403);
        echo json_encode(["status" => "error", "code" => "hwid_mismatch", "message" => "Account linked to another device"]);
        $conn->close();
        exit();
    }

    // Save HWID and IP
    $update = $conn->prepare("UPDATE users SET hwid = ?, ip = ? WHERE id = ?");
    $update->bind_param("ssi", $hwid, $ip, $row['id']);
    $update->execute();
    $update->close();

    // Success
    echo json_encode([
        "status" => "success",
        "username" => $row['username'],
        "plan" => $row['plan']
    ]);
    $conn->close();
    exit();
}

// Unknown action
http_response_code(400);
echo json_encode(["status" => "error", "code" => "unknown_action"]);
$conn->close();
?>
