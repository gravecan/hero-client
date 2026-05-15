<?php
require 'db.php';

// Add session_token column to users table
$sql = "ALTER TABLE users ADD COLUMN session_token VARCHAR(255) DEFAULT NULL";

if ($conn->query($sql) === TRUE) {
    echo "Column 'session_token' added successfully";
}
else {
    echo "Error adding column: " . $conn->error;
}

$conn->close();
?>
