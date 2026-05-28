# Clean Large Files - Prepare for GitHub Upload
# This script will delete all compiled/generated large folders

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Clean Large Files - Prepare for GitHub" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "WARNING: This will delete the following folders:" -ForegroundColor Yellow
Write-Host "  - All target/ folders (Maven compiled files)" -ForegroundColor Yellow
Write-Host "  - All node_modules/ folders (Node.js dependencies)" -ForegroundColor Yellow
Write-Host "  - All dist/ folders (Frontend build files)" -ForegroundColor Yellow
Write-Host "  - All unpackage/ folders (uni-app compiled files)" -ForegroundColor Yellow
Write-Host ""
Write-Host "These files can be regenerated with:" -ForegroundColor Green
Write-Host "  - Maven: mvn clean install" -ForegroundColor Green
Write-Host "  - Node.js: npm install" -ForegroundColor Green
Write-Host ""

$confirm = Read-Host "Confirm deletion? (Y/N)"
if ($confirm -ne "Y" -and $confirm -ne "y") {
    Write-Host "Operation cancelled" -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "Starting cleanup..." -ForegroundColor Green
Write-Host ""

# Statistics
$deletedCount = 0
$totalSize = 0

# Delete all target folders
Write-Host "[1/4] Deleting target/ folders..." -ForegroundColor Yellow
$targetFolders = Get-ChildItem -Path . -Recurse -Directory -Filter "target" -ErrorAction SilentlyContinue
foreach ($folder in $targetFolders) {
    try {
        $size = (Get-ChildItem -Path $folder.FullName -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum
        $sizeMB = [math]::Round($size / 1MB, 2)
        Write-Host "  Deleting: $($folder.FullName) ($sizeMB MB)" -ForegroundColor Gray
        Remove-Item -Path $folder.FullName -Recurse -Force -ErrorAction SilentlyContinue
        $deletedCount++
        $totalSize += $size
    } catch {
        Write-Host "  Skipped: $($folder.FullName) (cannot delete)" -ForegroundColor Red
    }
}

# Delete all node_modules folders
Write-Host "[2/4] Deleting node_modules/ folders..." -ForegroundColor Yellow
$nodeModulesFolders = Get-ChildItem -Path . -Recurse -Directory -Filter "node_modules" -ErrorAction SilentlyContinue
foreach ($folder in $nodeModulesFolders) {
    try {
        $size = (Get-ChildItem -Path $folder.FullName -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum
        $sizeMB = [math]::Round($size / 1MB, 2)
        Write-Host "  Deleting: $($folder.FullName) ($sizeMB MB)" -ForegroundColor Gray
        Remove-Item -Path $folder.FullName -Recurse -Force -ErrorAction SilentlyContinue
        $deletedCount++
        $totalSize += $size
    } catch {
        Write-Host "  Skipped: $($folder.FullName) (cannot delete)" -ForegroundColor Red
    }
}

# Delete all dist folders
Write-Host "[3/4] Deleting dist/ folders..." -ForegroundColor Yellow
$distFolders = Get-ChildItem -Path . -Recurse -Directory -Filter "dist" -ErrorAction SilentlyContinue
foreach ($folder in $distFolders) {
    try {
        $size = (Get-ChildItem -Path $folder.FullName -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum
        $sizeMB = [math]::Round($size / 1MB, 2)
        Write-Host "  Deleting: $($folder.FullName) ($sizeMB MB)" -ForegroundColor Gray
        Remove-Item -Path $folder.FullName -Recurse -Force -ErrorAction SilentlyContinue
        $deletedCount++
        $totalSize += $size
    } catch {
        Write-Host "  Skipped: $($folder.FullName) (cannot delete)" -ForegroundColor Red
    }
}

# Delete all unpackage folders
Write-Host "[4/4] Deleting unpackage/ folders..." -ForegroundColor Yellow
$unpackageFolders = Get-ChildItem -Path . -Recurse -Directory -Filter "unpackage" -ErrorAction SilentlyContinue
foreach ($folder in $unpackageFolders) {
    try {
        $size = (Get-ChildItem -Path $folder.FullName -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum
        $sizeMB = [math]::Round($size / 1MB, 2)
        Write-Host "  Deleting: $($folder.FullName) ($sizeMB MB)" -ForegroundColor Gray
        Remove-Item -Path $folder.FullName -Recurse -Force -ErrorAction SilentlyContinue
        $deletedCount++
        $totalSize += $size
    } catch {
        Write-Host "  Skipped: $($folder.FullName) (cannot delete)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Cleanup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Statistics:" -ForegroundColor Cyan
Write-Host "  Deleted folders: $deletedCount" -ForegroundColor White
Write-Host "  Space freed: $([math]::Round($totalSize / 1MB, 2)) MB" -ForegroundColor White
Write-Host ""
Write-Host "Project is ready to upload to GitHub!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  1. Create repository on GitHub" -ForegroundColor White
Write-Host "  2. Upload project files" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
