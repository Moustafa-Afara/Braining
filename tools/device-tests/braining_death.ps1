$adb = "C:\Users\ASUS\AppData\Local\Android\Sdk\platform-tools-2\adb.exe"
for ($i = 1; $i -le 6; $i++) {
    # make sure we are in the app and on chat
    & $adb shell am start -n com.braining.app/.MainActivity | Out-Null
    Start-Sleep -Milliseconds 1500
    $pidBefore = (& $adb shell pidof com.braining.app) -join ''
    # background it
    & $adb shell input keyevent 3 | Out-Null
    Start-Sleep -Milliseconds 1500
    # simulate MIUI reclaiming the backgrounded process
    & $adb shell am kill com.braining.app | Out-Null
    Start-Sleep -Milliseconds 1500
    $pidKilled = (& $adb shell pidof com.braining.app) -join ''
    # return the way the user would: relaunch the existing task
    & $adb shell am start -n com.braining.app/.MainActivity | Out-Null
    Start-Sleep -Milliseconds 4000
    $pidAfter = (& $adb shell pidof com.braining.app) -join ''
    $raw = (& $adb shell "uiautomator dump /sdcard/d.xml >/dev/null 2>&1; wc -c < /sdcard/d.xml") -join ''
    $t = $raw.Trim()
    Write-Output "run $i  pid before=$pidBefore afterKill='$pidKilled' new=$pidAfter  len=$t"
    if ($t -match '^\d+$' -and [int]$t -lt 6000) {
        Write-Output "*** BLANK AFTER PROCESS DEATH RESTORE at run $i (len=$t) ***"
        break
    }
}
Write-Output "DEATH_TEST_DONE"
