Write-Host "Starting FPT Dev Environment..." -ForegroundColor Green

# 1. Start Emulator
$emulatorPath = "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe"
if (Test-Path $emulatorPath) {
    Write-Host "Launching Emulator: Pixel_6_Pro..."
    Start-Process -FilePath $emulatorPath -ArgumentList "-avd Pixel_6_Pro" -WindowStyle Minimized
    # Wait a bit for emulator to start booting
    Start-Sleep -Seconds 15
} else {
    Write-Warning "Emulator path not found at $emulatorPath. Skipping emulator launch."
}

# 2. Start Project
Write-Host "Starting React Native Project..." -ForegroundColor Green
Set-Location "frontend-mobile"
npm run android
