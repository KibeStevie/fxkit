<#
  Usage (from anywhere):   .\tools\release.ps1 -Version 0.1.2
  Optional:                -NextSnapshot 0.2.0-SNAPSHOT   (default: patch + 1)
                           -SkipTests    -Branch main
#>
param(
    [Parameter(Mandatory)][ValidatePattern('^\d+\.\d+\.\d+$')][string]$Version,
    [string]$NextSnapshot,
    [switch]$SkipTests,
    [string]$Branch = 'main'
)

$ErrorActionPreference = 'Stop'

function Invoke-Checked {
    param([string]$Exe, [string[]]$CmdArgs)
    & $Exe @CmdArgs
    if ($LASTEXITCODE -ne 0) {
        throw "$Exe $($CmdArgs -join ' ') failed (exit code $LASTEXITCODE)"
    }
}

# Always run from the repo root (this script lives in <root>\tools)
Set-Location (Split-Path $PSScriptRoot -Parent)

$tag = "v$Version"

# ---- Safety checks ----
$current = (git rev-parse --abbrev-ref HEAD).Trim()
if ($current -ne $Branch) { throw "You are on '$current'. Switch to '$Branch' first." }

if (git status --porcelain) { throw "Working tree is not clean. Commit or stash your changes first." }

Invoke-Checked git @('pull', '--ff-only', 'origin', $Branch)

if ([int](git rev-list "origin/$Branch..HEAD" --count) -gt 0) {
    throw "You have unpushed commits. Run 'git push origin $Branch' first."
}

if (git tag -l $tag) { throw "Tag $tag already exists locally." }
if (git ls-remote --tags origin $tag) { throw "Tag $tag already exists on GitHub." }

# ---- Work out the next SNAPSHOT ----
if (-not $NextSnapshot) {
    $p = $Version.Split('.')
    $NextSnapshot = "$($p[0]).$($p[1]).$([int]$p[2] + 1)-SNAPSHOT"
}

Write-Host "`nReleasing $Version, then moving the repo to $NextSnapshot`n" -ForegroundColor Cyan

# ---- Verify (tests, javadoc, enforcer) on the current code ----
$verifyArgs = @('clean', 'verify', '-pl', 'fxkit-core', '-am')
if ($SkipTests) { $verifyArgs += '-DskipTests' }
Invoke-Checked mvn $verifyArgs

# ---- Tag and push: this triggers the GitHub Actions deploy ----
Invoke-Checked git @('tag', '-a', $tag, '-m', "Release $Version")
Invoke-Checked git @('push', 'origin', $tag)

# ---- Move the repo to the next development version ----
Invoke-Checked mvn @('versions:set', "-DnewVersion=$NextSnapshot", '-DgenerateBackupPoms=false')
Invoke-Checked git @('add', '-A')
Invoke-Checked git @('commit', '-m', "Start $NextSnapshot")
Invoke-Checked git @('push', 'origin', $Branch)

Write-Host "`nTag $tag pushed. Watch the deploy at:" -ForegroundColor Green
Write-Host "https://github.com/KibeStevie/fxkit/actions"