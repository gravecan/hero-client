<?php
session_start();
require 'db.php';
require 'discord_config.php';

// 1. Redirect to Discord Login
if (!isset($_GET['code'])) {
    // If user is not logged in locally (checked via JS mostly, but good to have safeguard), 
    // we assume the user clicked the link from the dashboard where they are logged in.
    // However, since this is a separate request, we need to know WHICH user connects.
    // We will verify the user's existence via a passed 'username' parameter or session.
    // For simplicity and security, we'll assume the frontend passes the username.

    if (isset($_GET['user'])) {
        $_SESSION['linking_user'] = $_GET['user'];
    }

    $params = [
        'client_id' => DISCORD_CLIENT_ID,
        'redirect_uri' => DISCORD_REDIRECT_URI,
        'response_type' => 'code',
        'scope' => 'identify'
    ];
    $url = "https://discord.com/api/oauth2/authorize?" . http_build_query($params);
    header("Location: $url");
    exit();
}

// 2. Handle Callback
if (isset($_GET['code'])) {
    if (!isset($_SESSION['linking_user'])) {
        die("Error: No user session found. Please try linking from the dashboard again.");
    }

    $username = $_SESSION['linking_user'];

    // Exchange code for token
    $tokenParams = [
        'client_id' => DISCORD_CLIENT_ID,
        'client_secret' => DISCORD_CLIENT_SECRET,
        'grant_type' => 'authorization_code',
        'code' => $_GET['code'],
        'redirect_uri' => DISCORD_REDIRECT_URI
    ];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'https://discord.com/api/oauth2/token');
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($tokenParams));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    curl_close($ch);

    $tokenData = json_decode($response, true);

    if (!isset($tokenData['access_token'])) {
        die("Error fetching token from Discord. Check your Client ID/Secret.");
    }

    $accessToken = $tokenData['access_token'];

    // Get User Info
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'https://discord.com/api/users/@me');
    curl_setopt($ch, CURLOPT_HTTPHEADER, ["Authorization: Bearer $accessToken"]);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $userResponse = curl_exec($ch);
    curl_close($ch);

    $userData = json_decode($userResponse, true);

    $discordId = $userData['id'];
    $discordUsername = $userData['username']; // discriminator is deprecated
    $discordAvatar = $userData['avatar'];

    // Update Database - With Avatar and ID
    $stmt = $conn->prepare("UPDATE users SET discord_id = ?, discord_username = ?, discord_avatar = ? WHERE username = ?");
    $stmt->bind_param("ssss", $discordId, $discordUsername, $discordAvatar, $username);

    if ($stmt->execute()) {
        echo "<script>alert('Discord Linked Successfully!'); window.location.href='dashboard.html';</script>";
    }
    else {
        echo "Error updating database: " . $conn->error;
    }

    $stmt->close();
    $conn->close();
}
?>
