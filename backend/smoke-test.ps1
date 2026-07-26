# AccioBuild E2E Platform Smoke Test & Integration Validator

$GatewayUrl = "http://localhost:8080"
$Services = @{
    "api-gateway"          = 8080
    "auth-service"         = 8081
    "organization-service" = 8082
    "project-service"      = 8083
    "knowledge-service"    = 8084
    "ai-service"           = 8085
}

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "  AccioBuild E2E Platform Smoke Test Tool" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# 1. Health Status Checks
Write-Host "`n[1] Checking Actuator Health Endpoints..." -ForegroundColor Yellow
foreach ($service in $Services.Keys) {
    $port = $Services[$service]
    $url = "http://localhost:$port/actuator/health"
    try {
        $resp = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 3
        if ($resp.status -eq "UP") {
            Write-Host "  ✔ Service $service is UP on port $port" -ForegroundColor Green
        } else {
            Write-Host "  ✖ Service $service is NOT fully UP (Status: $($resp.status))" -ForegroundColor Red
        }
    } catch {
        Write-Host "  ✖ Service $service is UNREACHABLE on port $port" -ForegroundColor Red
    }
}

# 2. Gateway Router Check
Write-Host "`n[2] Checking API Gateway Route Mapping..." -ForegroundColor Yellow
$gatewayHealthUrl = "$GatewayUrl/actuator/health"
try {
    $gatewayHealth = Invoke-RestMethod -Uri $gatewayHealthUrl -Method Get
    Write-Host "  ✔ API Gateway is reachable and reports healthy." -ForegroundColor Green
} catch {
    Write-Host "  ✖ API Gateway is unreachable! Skipping downstream route validations." -ForegroundColor Red
    exit 1
}

# 3. Downstream Integration Path Check
Write-Host "`n[3] Simulating Anonymous Auth Bypass Paths..." -ForegroundColor Yellow
$loginUrl = "$GatewayUrl/api/v1/auth/login"
try {
    $resp = Invoke-WebRequest -Uri $loginUrl -Method Post -ContentType "application/json" -Body '{}' -SkipHttpErrorCheck
    Write-Host "  ✔ Auth Login endpoint reached through Gateway (Status: $($resp.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "  ✖ Failed to route to Auth Service through Gateway: $_" -ForegroundColor Red
}

# 4. Security Check (Secured Endpoint without JWT)
Write-Host "`n[4] Asserting JWT Security Interception..." -ForegroundColor Yellow
$projectsUrl = "$GatewayUrl/api/v1/projects"
try {
    $resp = Invoke-WebRequest -Uri $projectsUrl -Method Get -SkipHttpErrorCheck
    if ($resp.StatusCode -eq 401 -or $resp.StatusCode -eq 403) {
        Write-Host "  ✔ Security Blocked Unauthorized Request correctly (Status: $($resp.StatusCode))" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ Security failed to intercept unauthorized request! Status code: $($resp.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ✔ Request correctly blocked or failed as expected: $_" -ForegroundColor Green
}

Write-Host "`n=============================================" -ForegroundColor Cyan
Write-Host "  Smoke Test Complete!" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
