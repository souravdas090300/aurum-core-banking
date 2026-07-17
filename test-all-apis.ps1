# test-all-apis.ps1 - Complete API Test Script
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "   AURUM CORE BANKING API TEST SUITE   " -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

# Configuration
$keycloakUrl = "http://localhost:8180"
$apiUrl = "http://localhost:8080"
$clientId = "banking-api"
$clientSecret = "eOIX652lYdwYGKmYI6XI6ztN22DK2QkS"
$username = "alice"
$password = "test123"

# Helper function to get token
function Get-AuthToken {
    Write-Host "`n📝 Getting JWT token..." -ForegroundColor Yellow
    $body = "client_id=$clientId&client_secret=$clientSecret&username=$username&password=$password&grant_type=password"
    $response = Invoke-RestMethod -Method Post -Uri "$keycloakUrl/realms/banking/protocol/openid-connect/token" -ContentType "application/x-www-form-urlencoded" -Body $body
    Write-Host "✅ Token obtained successfully" -ForegroundColor Green
    return $response.access_token
}

# Helper function for API calls
function Invoke-ApiCall {
    param($Method, $Endpoint, $Body = $null, $Token)
    
    $headers = @{ Authorization = "Bearer $Token" }
    if ($Body) { $headers["Content-Type"] = "application/json" }
    
    try {
        $response = Invoke-RestMethod -Method $Method -Uri "$apiUrl$Endpoint" -Headers $headers -Body $Body
        Write-Host "✅ Success" -ForegroundColor Green
        return $response
    } catch {
        Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails.Message) {
            Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
        }
        return $null
    }
}

# Get token
$token = Get-AuthToken

# ============================================
# 1. PUBLIC ENDPOINTS (No Auth Required)
# ============================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "1. PUBLIC ENDPOINTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n📊 Health Check:" -ForegroundColor Yellow
$health = Invoke-RestMethod "$apiUrl/actuator/health" -ErrorAction SilentlyContinue
Write-Host "   Status: $($health.status)" -ForegroundColor Green

Write-Host "`nℹ️  App Info:" -ForegroundColor Yellow
$info = Invoke-RestMethod "$apiUrl/actuator/info" -ErrorAction SilentlyContinue
$info | ConvertTo-Json

# ============================================
# 2. ACCOUNT ENDPOINTS
# ============================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "2. ACCOUNT ENDPOINTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# GET all accounts
Write-Host "`n📋 GET /api/v1/accounts - Get all accounts:" -ForegroundColor Yellow
$accounts = Invoke-ApiCall -Method Get -Endpoint "/api/v1/accounts" -Token $token
if ($accounts) {
    $accounts | ConvertTo-Json -Depth 3
    $firstAccountId = $accounts[0].id
    Write-Host "   First Account ID: $firstAccountId" -ForegroundColor Green
}

# GET account by ID (if accounts exist)
if ($firstAccountId) {
    Write-Host "`n🔍 GET /api/v1/accounts/$firstAccountId - Get specific account:" -ForegroundColor Yellow
    $account = Invoke-ApiCall -Method Get -Endpoint "/api/v1/accounts/$firstAccountId" -Token $token
    if ($account) { $account | ConvertTo-Json }
}

# POST create account (requires TELLER/MANAGER role)
Write-Host "`n➕ POST /api/v1/accounts - Create new account:" -ForegroundColor Yellow
$newAccount = @{
    accountNumber = "MT01BANK" + (Get-Random -Minimum 1000 -Maximum 9999)
    accountType = "CURRENT"
    currency = "EUR"
    initialDeposit = 1000.00
} | ConvertTo-Json

$createdAccount = Invoke-ApiCall -Method Post -Endpoint "/api/v1/accounts" -Body $newAccount -Token $token
if ($createdAccount) { 
    Write-Host "   Created Account: $($createdAccount.accountNumber)" -ForegroundColor Green
}

# ============================================
# 3. TRANSFER ENDPOINTS
# ============================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "3. TRANSFER ENDPOINTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# First, get two accounts for transfer
$accounts = Invoke-RestMethod "$apiUrl/api/v1/accounts" -Headers @{Authorization = "Bearer $token"}
if ($accounts.Count -ge 2) {
    $fromId = $accounts[0].id
    $toId = $accounts[1].id
    
    Write-Host "`n💰 POST /api/v1/transfers - Execute transfer:" -ForegroundColor Yellow
    $transferBody = @{
        fromAccountId = $fromId
        toAccountId = $toId
        amount = 100.00
        currency = "EUR"
        reference = "Test Transfer $(Get-Date -Format 'HH:mm:ss')"
        idempotencyKey = [guid]::NewGuid().ToString()
    } | ConvertTo-Json
    
    $transfer = Invoke-ApiCall -Method Post -Endpoint "/api/v1/transfers" -Body $transferBody -Token $token
    if ($transfer) {
        $transfer | ConvertTo-Json
        $transferId = $transfer.transactionId
        
        # GET transfer by ID
        Write-Host "`n🔍 GET /api/v1/transfers/$transferId - Get transfer details:" -ForegroundColor Yellow
        $transferDetails = Invoke-ApiCall -Method Get -Endpoint "/api/v1/transfers/$transferId" -Token $token
        if ($transferDetails) { $transferDetails | ConvertTo-Json }
    }
} else {
    Write-Host "⚠️ Need at least 2 accounts to test transfer" -ForegroundColor Yellow
}

# ============================================
# 4. LOAN ENDPOINTS
# ============================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "4. LOAN ENDPOINTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n🏦 POST /api/v1/loans/applications - Apply for loan:" -ForegroundColor Yellow
$loanBody = @{
    customerId = "49e73f39-7ae8-4ddd-b0e2-8ff38858ac65"
    requestedAmount = 5000.00
    currency = "EUR"
    termMonths = 36
    purpose = "Home Improvement"
    creditScore = 720
    monthlyIncome = 5000.00
    debtToIncomeRatio = 0.30
    isPep = $false
} | ConvertTo-Json

$loanApp = Invoke-ApiCall -Method Post -Endpoint "/api/v1/loans/applications" -Body $loanBody -Token $token
if ($loanApp) { $loanApp | ConvertTo-Json }

Write-Host "`n📋 GET /api/v1/loans/tasks/queue - Get task queue:" -ForegroundColor Yellow
$tasks = Invoke-ApiCall -Method Get -Endpoint "/api/v1/loans/tasks/queue" -Token $token
if ($tasks) { $tasks | ConvertTo-Json }

# ============================================
# 5. SUMMARY
# ============================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ API testing completed!" -ForegroundColor Green
Write-Host "📊 Check your database for changes" -ForegroundColor Yellow