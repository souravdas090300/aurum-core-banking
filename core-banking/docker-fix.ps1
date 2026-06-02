# Docker Compose Fix Script
# This script sets the correct DOCKER_HOST for docker compose commands

Write-Host "🔧 Setting Docker environment for this session..." -ForegroundColor Cyan

# Set DOCKER_HOST to Docker Desktop's named pipe
$env:DOCKER_HOST = "npipe:////./pipe/dockerDesktopLinuxEngine"

Write-Host "✅ DOCKER_HOST set to: $env:DOCKER_HOST" -ForegroundColor Green
Write-Host ""
Write-Host "You can now run docker compose commands:" -ForegroundColor Yellow
Write-Host "  docker compose up --build"
Write-Host "  docker compose ps"
Write-Host "  docker compose logs -f"
Write-Host ""
Write-Host "💡 To make this permanent, add this to your PowerShell profile:" -ForegroundColor Cyan
Write-Host '  $env:DOCKER_HOST = "npipe:////./pipe/dockerDesktopLinuxEngine"'
Write-Host ""
