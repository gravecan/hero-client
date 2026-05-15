# PowerShell C# Compiler Script for BloodLoader
param(
    [string]$SourceFile = "BloodLoader.cs",
    [string]$OutputPath = "..\build\Standalone\BloodLoader.exe",
    [string]$EmbeddedDllFile = "EmbeddedDll.txt"
)

Write-Host "[INFO] Compiling $SourceFile..."

# Read source code
$code = Get-Content $SourceFile -Raw

# Read embedded DLL
$base64Dll = Get-Content $EmbeddedDllFile -Raw

# Replace placeholder in code
$code = $code -replace '\$EMBEDDED_DLL_BASE64\$', $base64Dll

# Referenced assemblies
$refs = @(
    'System.dll',
    'System.Core.dll',
    'System.Management.dll',
    'System.IO.Compression.dll',
    'System.IO.Compression.FileSystem.dll'
)

try {
    # Create compiler parameters
    $cp = New-Object System.CodeDom.Compiler.CompilerParameters
    $cp.GenerateExecutable = $true
    $cp.OutputAssembly = $OutputPath
    $cp.CompilerOptions = '/platform:x64 /optimize+'
    $cp.ReferencedAssemblies.AddRange($refs)
    
    # Path to the DLL base64 file
    $resPath = Resolve-Path $EmbeddedDllFile
    $cp.EmbeddedResources.Add($resPath)
    
    # Compile
    $provider = New-Object Microsoft.CSharp.CSharpCodeProvider
    $results = $provider.CompileAssemblyFromSource($cp, $code)
    
    if ($results.Errors.Count -gt 0) {
        foreach ($err in $results.Errors) {
            Write-Host "[ERROR] $err"
        }
        exit 1
    }
    
    Write-Host "[OK] Compilation successful"
    Write-Host "[OK] Output: $OutputPath"
    exit 0
} catch {
    Write-Host "[ERROR] Compilation failed: $_"
    exit 1
}

