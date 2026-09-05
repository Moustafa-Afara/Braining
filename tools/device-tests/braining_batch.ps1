param([int]$Cycles = 6, [int]$Start = 1)
$adb = "C:\Users\ASUS\AppData\Local\Android\Sdk\platform-tools-2\adb.exe"
$ys = @(356,500,644,788,932,1076)
& $adb shell settings put system accelerometer_rotation 0 | Out-Null
for ($i = $Start; $i -lt ($Start + $Cycles); $i++) {
    $mode = $i % 3
    if ($mode -eq 0) {
        & $adb shell input tap 228 200 | Out-Null
        Start-Sleep -Milliseconds 700
        & $adb shell input keyevent 4 | Out-Null
        Start-Sleep -Milliseconds 700
        & $adb shell input tap 957 200 | Out-Null
        Start-Sleep -Milliseconds 500
        & $adb shell input tap 850 $ys[$i % 6] | Out-Null
        Start-Sleep -Milliseconds 500
    } elseif ($mode -eq 1) {
        & $adb shell input keyevent 3 | Out-Null
        Start-Sleep -Milliseconds 1200
        & $adb shell am start -n com.braining.app/.MainActivity | Out-Null
        Start-Sleep -Milliseconds 1500
    } else {
        & $adb shell settings put system user_rotation 1 | Out-Null
        Start-Sleep -Milliseconds 1400
        & $adb shell settings put system user_rotation 0 | Out-Null
        Start-Sleep -Milliseconds 1600
    }
    $raw = (& $adb shell "uiautomator dump /sdcard/lp.xml >/dev/null 2>&1; wc -c < /sdcard/lp.xml") -join ''
    $t = $raw.Trim()
    Write-Output "cycle $i mode=$mode len=$t"
    if ($t -match '^\d+$' -and [int]$t -lt 6000) {
        Write-Output "*** COLLAPSED at cycle $i mode=$mode len=$t ***"
        break
    }
}
Write-Output "BATCH_DONE"
