# ============================================================
# Keycloak Configuration Script for Aurum Core Banking
# ============================================================
# This script configures Keycloak with:
# - Banking realm
# - Banking-api client (OAuth2 resource server)
# - Roles: BANKING_USER, LOAN_OFFICER, COMPLIANCE_OFFICER
# - Test users with assigned roles
# ============================================================

$ErrorActionPreference = "Stop"
$KEYCLOAK_URL = "http://localhost:8180"
$ADMIN_USER = "admin"
$ADMIN_PASSWORD = "admin"

Write-Host "`n=== KEYCLOAK SETUP FOR AURUM CORE BANKING ===" -ForegroundColor Cyan

# ============================================================
# Step 1: Get Admin Access Token
# ============================================================
Write-Host "`n[1/7] Authenticating with Keycloak admin..." -ForegroundColor Yellow

$tokenBody = @{
    username = $ADMIN_USER
    password = $ADMIN_PASSWORD
    grant_type = "password"
    client_id = "admin-cli"
}

try {
    $tokenResponse = Invoke-RestMethod -Uri "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" `
        -Method POST -Body $tokenBody -ContentType "application/x-www-form-urlencoded"
    $ACCESS_TOKEN = $tokenResponse.access_token
    Write-Host "   ✓ Admin token acquired" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Failed to get admin token: $_" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $ACCESS_TOKEN"
    "Content-Type" = "application/json"
}

# ============================================================
# Step 2: Create Banking Realm
# ============================================================
Write-Host "`n[2/7] Creating 'banking' realm..." -ForegroundColor Yellow

$realmConfig = @{
    realm = "banking"
    enabled = $true
    displayName = "Aurum Banking"
    registrationAllowed = $false
    resetPasswordAllowed = $true
    rememberMe = $true
    editUsernameAllowed = $false
    accessTokenLifespan = 3600
    ssoSessionIdleTimeout = 1800
    ssoSessionMaxLifespan = 36000
} | ConvertTo-Json -Depth 10

try {
    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms" -Method POST -Headers $headers -Body $realmConfig
    Write-Host "   ✓ Banking realm created" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 409) {
        Write-Host "   ⚠ Realm already exists - skipping" -ForegroundColor Yellow
    } else {
        Write-Host "   ✗ Failed to create realm: $_" -ForegroundColor Red
        exit 1
    }
}

# ============================================================
# Step 3: Create Banking-API Client
# ============================================================
Write-Host "`n[3/7] Creating 'banking-api' OAuth2 client..." -ForegroundColor Yellow

$clientConfig = @{
    clientId = "banking-api"
    name = "Banking API Resource Server"
    description = "OAuth2 resource server for Aurum Core Banking"
    enabled = $true
    protocol = "openid-connect"
    publicClient = $false
    bearerOnly = $false
    standardFlowEnabled = $true
    directAccessGrantsEnabled = $true
    serviceAccountsEnabled = $false
    authorizationServicesEnabled = $false
    redirectUris = @("http://localhost:8080/*")
    webOrigins = @("http://localhost:8080")
    attributes = @{
        "access.token.lifespan" = "3600"
    }
} | ConvertTo-Json -Depth 10

try {
    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/banking/clients" -Method POST -Headers $headers -Body $clientConfig
    Write-Host "   ✓ Banking-api client created" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 409) {
        Write-Host "   ⚠ Client already exists - skipping" -ForegroundColor Yellow
    } else {
        Write-Host "   ✗ Failed to create client: $_" -ForegroundColor Red
    }
}

# ============================================================
# Step 4: Create Realm Roles
# ============================================================
Write-Host "`n[4/7] Creating realm roles..." -ForegroundColor Yellow

$roles = @("banking-user", "loan-officer", "compliance-officer")

foreach ($role in $roles) {
    $roleConfig = @{
        name = $role
        description = "Role for $role"
    } | ConvertTo-Json

    try {
        Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/banking/roles" -Method POST -Headers $headers -Body $roleConfig
        Write-Host "   ✓ Role '$role' created" -ForegroundColor Green
    } catch {
        if ($_.Exception.Response.StatusCode -eq 409) {
            Write-Host "   ⚠ Role '$role' already exists - skipping" -ForegroundColor Yellow
        } else {
            Write-Host "   ✗ Failed to create role '$role'" -ForegroundColor Red
        }
    }
}

# ============================================================
# Step 5: Create Test Users
# ============================================================
Write-Host "`n[5/7] Creating test users..." -ForegroundColor Yellow

$users = @(
    @{
        username = "john.doe"
        firstName = "John"
        lastName = "Doe"
        email = "john.doe@example.com"
        password = "password123"
        roles = @("banking-user")
    },
    @{
        username = "jane.smith"
        firstName = "Jane"
        lastName = "Smith"
        email = "jane.smith@example.com"
        password = "password123"
        roles = @("banking-user", "loan-officer")
    },
    @{
        username = "bob.wilson"
        firstName = "Bob"
        lastName = "Wilson"
        email = "bob.wilson@example.com"
        password = "password123"
        roles = @("compliance-officer")
    }
)

foreach ($user in $users) {
    $userConfig = @{
        username = $user.username
        firstName = $user.firstName
        lastName = $user.lastName
        email = $user.email
        enabled = $true
        emailVerified = $true
    } | ConvertTo-Json

    try {
        Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/banking/users" -Method POST -Headers $headers -Body $userConfig
        Write-Host "   ✓ User '$($user.username)' created" -ForegroundColor Green

        # Get user ID
        $username = $user.username
        $queryUrl = "$KEYCLOAK_URL/admin/realms/banking/users?username=$username`&exact=true"
        $createdUser = Invoke-RestMethod -Uri $queryUrl -Method GET -Headers $headers
        $userId = $createdUser[0].id

        # Set password
        $passwordConfig = @{
            type = "password"
            value = $user.password
            temporary = $false
        } | ConvertTo-Json

        Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/banking/users/$userId/reset-password" `
            -Method PUT -Headers $headers -Body $passwordConfig
        Write-Host "   ✓ Password set for '$($user.username)'" -ForegroundColor Green

        # Assign roles
        foreach ($roleName in $user.roles) {
            $roleData = Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/banking/roles/$roleName" `
                -Method GET -Headers $headers
            
            $roleAssignment = @(@{
                id = $roleData.id
                name = $roleData.name
            }) | ConvertTo-Json -AsArray

            Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/banking/users/$userId/role-mappings/realm" `
                -Method POST -Headers $headers -Body $roleAssignment -ContentType "application/json"
        }
        Write-Host "   ✓ Roles assigned to '$($user.username)': $($user.roles -join ', ')" -ForegroundColor Green

    } catch {
        if ($_.Exception.Response.StatusCode -eq 409) {
            Write-Host "   ⚠ User '$($user.username)' already exists - skipping" -ForegroundColor Yellow
        } else {
            Write-Host "   ✗ Failed to create user '$($user.username)': $_" -ForegroundColor Red
        }
    }
}

# ============================================================
# Step 6: Test Token Generation
# ============================================================
Write-Host "`n[6/7] Testing JWT token generation..." -ForegroundColor Yellow

$testTokenBody = @{
    username = "john.doe"
    password = "password123"
    grant_type = "password"
    client_id = "banking-api"
}

try {
    $testToken = Invoke-RestMethod -Uri "$KEYCLOAK_URL/realms/banking/protocol/openid-connect/token" `
        -Method POST -Body $testTokenBody -ContentType "application/x-www-form-urlencoded"
    
    Write-Host "   ✓ JWT token generated successfully" -ForegroundColor Green
    Write-Host "   ✓ Token expires in: $($testToken.expires_in) seconds" -ForegroundColor Green
    
    # Decode JWT header and payload to show structure
    $tokenParts = $testToken.access_token.Split('.')
    $payload = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($tokenParts[1] + "=="))
    $payloadJson = $payload | ConvertFrom-Json
    
    Write-Host "`n   JWT Token Preview:" -ForegroundColor Cyan
    Write-Host "   - Subject: $($payloadJson.sub)" -ForegroundColor Gray
    Write-Host "   - Preferred Username: $($payloadJson.preferred_username)" -ForegroundColor Gray
    Write-Host "   - Realm Roles: $($payloadJson.realm_access.roles -join ', ')" -ForegroundColor Gray
    
} catch {
    Write-Host "   ✗ Failed to generate test token: $_" -ForegroundColor Red
}

# ============================================================
# Step 7: Configuration Summary
# ============================================================
Write-Host "`n[7/7] Configuration Summary" -ForegroundColor Yellow
Write-Host "   ✓ Keycloak URL: http://localhost:8180" -ForegroundColor Green
Write-Host "   ✓ Admin Console: http://localhost:8180/admin" -ForegroundColor Green
Write-Host "   ✓ Admin Credentials: admin / admin" -ForegroundColor Green
Write-Host "   ✓ Realm: banking" -ForegroundColor Green
Write-Host "   ✓ Client: banking-api" -ForegroundColor Green
Write-Host "   ✓ JWK Set URI: http://localhost:8180/realms/banking/protocol/openid-connect/certs" -ForegroundColor Green

Write-Host "`n=== TEST USERS ===" -ForegroundColor Cyan
Write-Host "   john.doe / password123 [banking-user]" -ForegroundColor White
Write-Host "   jane.smith / password123 [banking-user, loan-officer]" -ForegroundColor White
Write-Host "   bob.wilson / password123 [compliance-officer]" -ForegroundColor White

Write-Host "`n=== NEXT STEPS ===" -ForegroundColor Cyan
Write-Host "1. Stop your Spring Boot application (Shift+F5 in VS Code)" -ForegroundColor White
Write-Host "2. Edit .vscode/launch.json: Change SPRING_PROFILES_ACTIVE from 'dev' to 'prod'" -ForegroundColor White
Write-Host "3. Press F5 to restart with OAuth2 enabled" -ForegroundColor White
Write-Host "4. Test endpoints with JWT tokens from Keycloak" -ForegroundColor White

Write-Host "`n=== KEYCLOAK SETUP COMPLETE ===`n" -ForegroundColor Green
