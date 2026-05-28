param(
    [string]$Version = "9.6.0"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$archiveName = "apache-skywalking-java-agent-$Version.tgz"
$downloadUrl = "https://archive.apache.org/dist/skywalking/java-agent/$Version/$archiveName"
$archivePath = Join-Path $scriptDir $archiveName
$agentDir = Join-Path $scriptDir "skywalking-agent"
$tempExtractDir = Join-Path $scriptDir "tmp-agent-extract"

Write-Host "Downloading SkyWalking Java Agent $Version..."
Invoke-WebRequest -Uri $downloadUrl -OutFile $archivePath

if (Test-Path $tempExtractDir) {
    Remove-Item -Recurse -Force $tempExtractDir
}

New-Item -ItemType Directory -Path $tempExtractDir | Out-Null
tar -xzf $archivePath -C $tempExtractDir

$extractedAgentDir = Get-ChildItem -Path $tempExtractDir -Directory | Select-Object -First 1
if (-not $extractedAgentDir) {
    throw "SkyWalking agent archive did not contain an extractable directory."
}

if (Test-Path $agentDir) {
    Remove-Item -Recurse -Force $agentDir
}

Move-Item -Path $extractedAgentDir.FullName -Destination $agentDir
Remove-Item -Recurse -Force $tempExtractDir

Write-Host "SkyWalking agent is ready at: $agentDir"
Write-Host "You can now run: docker compose -f ..\\..\\docker-compose-full.yml up -d"
