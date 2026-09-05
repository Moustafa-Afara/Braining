@echo off
"C:\Users\ASUS\AppData\Local\Android\Sdk\platform-tools-2\adb.exe" logcat -v threadtime libsensor-displayalgo:S SDM:S libsensor-parseRGB:S Dpps:S sensors:S sensors-hal:S BufferQueueProducer:S *:V >> "C:\Dev\Braining\log_black.txt"
