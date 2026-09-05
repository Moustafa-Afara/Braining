$adb = "C:\Users\ASUS\AppData\Local\Android\Sdk\platform-tools-2\adb.exe"
$out = "C:\Dev\Braining\log_watchdog.txt"
Add-Content -Path $out -Value "watchdog started $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
for ($i = 1; $i -le 2000; $i++) {
    Start-Sleep -Seconds 90
    $top = (& $adb shell "dumpsys window | grep mCurrentFocus") -join ''
    if ($top -notlike '*com.braining.app*') { continue }
    $raw = (& $adb shell "uiautomator dump /sdcard/wd.xml >/dev/null 2>&1; wc -c < /sdcard/wd.xml") -join ''
    $t = $raw.Trim()
    if (-not ($t -match '^\d+$')) { continue }
    if ([int]$t -ge 6000) { continue }
    $stamp = Get-Date -Format 'yyyy-MM-dd_HH-mm-ss'
    Add-Content -Path $out -Value "=== BLACK SCREEN DETECTED $stamp len=$t ==="
    & $adb shell screencap -p /sdcard/black_$stamp.png | Out-Null
    & $adb pull /sdcard/black_$stamp.png "C:\Dev\Braining\screen_black_$stamp.png" | Out-Null
    Add-Content -Path $out -Value "--- current focus ---"
    Add-Content -Path $out -Value $top
    Add-Content -Path $out -Value "--- gfxinfo ---"
    Add-Content -Path $out -Value ((& $adb shell "dumpsys gfxinfo com.braining.app") -join "`r`n")
    Add-Content -Path $out -Value "--- window state ---"
    Add-Content -Path $out -Value ((& $adb shell "dumpsys window windows | grep -A 6 braining") -join "`r`n")
    Add-Content -Path $out -Value "--- ui tree ---"
    Add-Content -Path $out -Value ((& $adb shell cat /sdcard/wd.xml) -join '')
    Add-Content -Path $out -Value "=== END OF CAPTURE $stamp ==="
    break
}
Add-Content -Path $out -Value "watchdog finished $(Get-Date -Format 'HH:mm:ss')"
