$adb = "C:\Users\ASUS\AppData\Local\Android\Sdk\platform-tools-2\adb.exe"
$ys = @(356,500,644,788,932,1076)
& $adb shell am start -n com.braining.app/.MainActivity | Out-Null
Start-Sleep -Milliseconds 2000
# put a real message in the chat
& $adb shell input tap 980 2580 | Out-Null
Start-Sleep -Milliseconds 900
& $adb shell input text "say%shi" | Out-Null
Start-Sleep -Milliseconds 700
& $adb shell input tap 132 2580 | Out-Null
Write-Output "message sent, waiting for reply..."
Start-Sleep -Seconds 14
$raw = (& $adb shell "uiautomator dump /sdcard/c.xml >/dev/null 2>&1; wc -c < /sdcard/c.xml") -join ''
Write-Output "after send len=$($raw.Trim())"
# now cycle with content present
for ($i = 1; $i -le 14; $i++) {
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
    $r = (& $adb shell "uiautomator dump /sdcard/c.xml >/dev/null 2>&1; wc -c < /sdcard/c.xml") -join ''
    $t = $r.Trim()
    Write-Output "cycle $i mode=$mode len=$t"
    if ($t -match '^\d+$' -and [int]$t -lt 6000) {
        Write-Output "*** COLLAPSED cycle $i mode=$mode len=$t ***"
        break
    }
}
Write-Output "CONTENT_TEST_DONE"
