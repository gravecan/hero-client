$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$jarPath = Join-Path $PSScriptRoot "InjectableJar\InjectableJar\build\libs\InjectableJar-1.0.0.jar"
$headerPath = Join-Path $PSScriptRoot "InjectableJar\InjectableJar.jar.hpp"

if (Test-Path $jarPath) {
    $bytes = [System.IO.File]::ReadAllBytes($jarPath)
    $hex = $bytes | ForEach-Object { '0x{0:x2}' -f $_ }
    $size = $bytes.Count
    
    $content = @"
#pragma once
#include <array>
#include <cstdint>
inline constexpr std::array<uint8_t, $size> InjectableJar_jar =
{
	$($hex -join ', ')
};
"@
    $content | Out-File -Encoding ascii $headerPath
    Write-Host "Successfully updated $headerPath"
} else {
    Write-Error "JAR not found at $jarPath"
}