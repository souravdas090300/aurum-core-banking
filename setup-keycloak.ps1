# Wait for Keycloak to start
Write-Host "Waiting for Keycloak to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Get admin token
$token = (Invoke-RestMethod -Method Post -Uri "http://localhost:8180/realms/master/protocol/openid-connect/token" `
    -ContentType "application/x-www-form-urlencoded" `
    -Body "client_id=admin-cli&username=admin&password=admin&grant_type=password").access_token

$headers = @{ Authorization = "Bearer $token" }

# Create realm
$realmBody = @{
    id = "banking"
    realm = "banking"
    enabled = $true
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "http://localhost:8180/admin/realms" -Headers $headers -ContentType "application/json" -Body $realmBody -ErrorAction SilentlyContinue

# Create client
$clientBody = @{
    clientId = "banking-api"
    enabled = $true
    publicClient = $false
    serviceAccountsEnabled = $true
    standardFlowEnabled = $true
    directAccessGrantsEnabled = $true
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "http://localhost:8180/admin/realms/banking/clients" -Headers $headers -ContentType "application/json" -Body $clientBody

# Create roles
$roles = @("CUSTOMER", "TELLER", "MANAGER", "AUDITOR", "ADMIN")
foreach ($role in $roles) {
    $roleBody = @{ name = $role } | ConvertTo-Json
    Invoke-RestMethod -Method Post -Uri "http://localhost:8180/admin/realms/banking/roles" -Headers $headers -ContentType "application/json" -Body $roleBody -ErrorAction SilentlyContinue
}

Write-Host "Keycloak setup complete!" -ForegroundColor Green
