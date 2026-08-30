# ==============================================================================
# setup.ps1 - One-command Windows bootstrap for the Braining (فهم) PC bridge.
#
# What it does:
#   1. Checks prerequisites (Node.js, Tailscale, OpenCode).
#   2. Prints copy-paste fixes for anything missing.
#   3. Starts the local bridge server (confined to an approved working directory).
#   4. Brings the bridge onto your private Tailscale network (no public ports).
#   5. Shows a QR code / short pairing code to enter in the Braining app once.
#
# This is only needed for PATH B (building software / acting on your PC's files).
# PATH A (research, analysis, discussion, file-as-text generation) needs NONE of
# this - it runs on the phone with just API keys.
#
# Usage (PowerShell):   ./scripts/setup.ps1
# Optional:             ./scripts/setup.ps1 -WorkDir "C:\Braining\projects" -Port 8760
# ==============================================================================

param(
    [string]$WorkDir = "$env:USERPROFILE\Braining\projects",
    [int]$Port = 8760
)

$ErrorActionPreference = "Stop"

function Write-Ok   ($m) { Write-Host "[ok]    $m" -ForegroundColor Green }
function Write-Warn ($m) { Write-Host "[warn]  $m" -ForegroundColor Yellow }
function Write-Err  ($m) { Write-Host "[error] $m" -ForegroundColor Red }
function Write-Step ($m) { Write-Host "`n=== $m ===" -ForegroundColor Cyan }

Write-Host "Braining PC Bridge setup" -ForegroundColor Cyan
Write-Host "This connects THIS PC to YOUR Braining app for Path B tasks.`n"

# --- 1. Prerequisites ---------------------------------------------------------
Write-Step "Checking prerequisites"
$missing = @()

function Test-Cmd ($name, $probe, $fix) {
    try {
        Invoke-Expression $probe | Out-Null
        Write-Ok "$name found"
        return $true
    } catch {
        Write-Warn "$name NOT found"
        Write-Host "        Fix: $fix" -ForegroundColor DarkYellow
        $script:missing += $name
        return $false
    }
}

Test-Cmd "Node.js"   "node --version"       "Install from https://nodejs.org (LTS)."               | Out-Null
Test-Cmd "Tailscale" "tailscale version"    "Install from https://tailscale.com/download/windows." | Out-Null
Test-Cmd "OpenCode"  "opencode --version"   "Install OpenCode, then re-run. (Path B needs it.)"    | Out-Null

if ($missing.Count -gt 0) {
    Write-Err  ("Missing: {0}. Install the item(s) above, then re-run this script." -f ($missing -join ", "))
    exit 1
}

# --- 2. Tailscale up ----------------------------------------------------------
Write-Step "Bringing this PC onto your private Tailscale network"
try {
    tailscale up
    $tsIp = (tailscale ip -4 2>$null | Select-Object -First 1)
    if ([string]::IsNullOrWhiteSpace($tsIp)) { throw "no Tailscale IP yet" }
    Write-Ok "Tailscale IP: $tsIp"
} catch {
    Write-Warn "Could not confirm Tailscale is up. Sign in with 'tailscale up' and re-run."
    exit 1
}

# --- 3. Working directory (confinement root) ----------------------------------
Write-Step "Preparing the confined working directory"
if (-not (Test-Path $WorkDir)) { New-Item -ItemType Directory -Path $WorkDir -Force | Out-Null }
Write-Ok "Agent will operate inside: $WorkDir"
Write-Warn "The OpenCode agent is confined here. Widen scope only when you explicitly approve it."

# --- 4. Generate a one-time pairing token -------------------------------------
Write-Step "Generating a one-time pairing code"
$token = -join ((48..57) + (65..90) | Get-Random -Count 8 | ForEach-Object { [char]$_ })
$pairing = "braining://pair?host=$tsIp&port=$Port&token=$token"

# Persist a minimal, local-only bridge config (no secrets leave this machine).
$cfgDir = Join-Path $env:USERPROFILE ".braining"
if (-not (Test-Path $cfgDir)) { New-Item -ItemType Directory -Path $cfgDir -Force | Out-Null }
@{
    host    = $tsIp
    port    = $Port
    token   = $token
    workDir = $WorkDir
} | ConvertTo-Json | Set-Content -Path (Join-Path $cfgDir "bridge.json") -Encoding UTF8
Write-Ok "Bridge config written to $cfgDir\bridge.json"

# --- 5. Start the bridge server -----------------------------------------------
Write-Step "Starting the Braining bridge server"
# NOTE for the building agent: implement bridge-server.js (or a Kotlin/Ktor equivalent)
# that (a) authenticates the pairing token, (b) accepts REST/WebSocket commands,
# (c) drives OpenCode headless inside $WorkDir, (d) enforces the approval gate and
# returns a full change-report. Bind ONLY to the Tailscale IP, never 0.0.0.0.
$bridgeEntry = Join-Path $PSScriptRoot "..\bridge\bridge-server.js"
if (Test-Path $bridgeEntry) {
    Write-Ok "Launching bridge on $tsIp`:$Port (Tailscale-only)"
    Start-Process -NoNewWindow node -ArgumentList "`"$bridgeEntry`"", "--host", $tsIp, "--port", $Port
} else {
    Write-Warn "bridge-server not found yet (it is built in Milestone 6)."
    Write-Warn "Once built, this script will launch it automatically."
}

# --- 6. Show pairing info -----------------------------------------------------
Write-Step "Pair your phone"
Write-Host "1) Open the Braining app  ->  Settings  ->  Pair PC" -ForegroundColor White
Write-Host "2) Scan the QR below, or type the pairing code.`n" -ForegroundColor White
Write-Host "   Pairing code : $token" -ForegroundColor Green
Write-Host "   PC address    : $tsIp`:$Port" -ForegroundColor Green
Write-Host "   Pair URL      : $pairing`n" -ForegroundColor DarkGray

# Render a QR in the terminal if a QR tool is available; otherwise show the URL.
try {
    npx --yes qrcode-terminal "$pairing"
} catch {
    Write-Warn "Install a QR helper for an on-screen code:  npm i -g qrcode-terminal"
    Write-Host "Meanwhile, enter the pairing code and PC address above manually." -ForegroundColor White
}

Write-Ok "Setup complete. Keep this PC on while you run Path B tasks."
