<?php
// Prevent PHP warnings from breaking JSON output
error_reporting(0);

ini_set('display_errors', 0);

$servername = "localhost"; // Use localhost when running ON the VPS
$username = "sql_herowin_top";
$password = "ed06627de5f77";
$dbname = "sql_herowin_top";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    // Return JSON error cleanly
    http_response_code(500);
    header('Content-Type: application/json');
    die(json_encode(["error" => "Database Connection Failed: " . $conn->connect_error]));
}
?>
