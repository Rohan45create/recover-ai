$ErrorActionPreference = 'Stop'

Write-Host "Running Backend Tests..." -ForegroundColor Cyan
Set-Location -Path "backend"
# Use Maven wrapper to run tests
./mvnw clean test
if ($LASTEXITCODE -ne 0) {
    Write-Host "Backend tests failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}
Set-Location -Path ".."

Write-Host "Building Frontend..." -ForegroundColor Cyan
Set-Location -Path "frontend"
npm install
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host "Frontend build failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}
Set-Location -Path ".."

Write-Host "CI Lite completed successfully!" -ForegroundColor Green
