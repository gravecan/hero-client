<?php
require 'db.php';

// Add discord_id column
$sql = "ALTER TABLE users ADD COLUMN discord_id VARCHAR(255) DEFAULT NULL";
if ($conn->query($sql) === TRUE) {
    echo "Column 'discord_id' added successfully.<br>";
}
else {
    echo "Error adding column 'discord_id': " . $conn->error . "<br>";
}

// Add discord_username column
$sql = "ALTER TABLE users ADD COLUMN discord_username VARCHAR(255) DEFAULT NULL";
if ($conn->query($sql) === TRUE) {
    echo "Column 'discord_username' added successfully.<br>";
}
else {
    echo "Error adding column 'discord_username': " . $conn->error . "<br>";
}

// Add discord_avatar column
$sql = "ALTER TABLE users ADD COLUMN discord_avatar VARCHAR(255) DEFAULT NULL";
if ($conn->query($sql) === TRUE) {
    echo "Column 'discord_avatar' added successfully.<br>";
}
else {
    echo "Error adding column 'discord_avatar': " . $conn->error . "<br>";
}

$conn->close();
?>
