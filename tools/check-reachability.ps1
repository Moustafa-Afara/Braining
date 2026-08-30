# check-reachability.ps1
# ------------------------------------------------------------------------------
# يقيس أي مزوّدي الذكاء الاصطناعي يمكن الوصول إليهم من شبكتك الحالية.
#
# الفكرة: لا نحتاج مفتاح API إطلاقاً. أي رد HTTP — حتى 401 أو 400 — يثبت أن
# الاتصال وصل إلى الخادم ولم يُحجب. الفشل الوحيد المهم هو انقطاع الاتصال نفسه
# (timeout / connection refused / DNS)، وهذا ما يعني الحجب.
#
# شغّله مرتين:
#   1) والـ VPN مُطفأ   ->  .\check-reachability.ps1 -Label "بدون VPN"
#   2) والـ VPN يعمل    ->  .\check-reachability.ps1 -Label "مع VPN"
# ------------------------------------------------------------------------------

param(
    [string]$Label = "غير محدد",
    [int]$TimeoutSec = 12
)

$targets = @(
    @{ Name = "DeepSeek";    Url = "https://api.deepseek.com/v1/chat/completions" }
    @{ Name = "OpenRouter";  Url = "https://openrouter.ai/api/v1/chat/completions" }
    @{ Name = "Groq";        Url = "https://api.groq.com/openai/v1/chat/completions" }
    @{ Name = "Mistral";     Url = "https://api.mistral.ai/v1/chat/completions" }
    @{ Name = "Together";    Url = "https://api.together.xyz/v1/chat/completions" }
    @{ Name = "Cerebras";    Url = "https://api.cerebras.ai/v1/chat/completions" }
    @{ Name = "Cloudflare";  Url = "https://api.cloudflare.com/client/v4/user/tokens/verify" }
    @{ Name = "Gemini";      Url = "https://generativelanguage.googleapis.com/v1beta/models" }
    @{ Name = "Anthropic";   Url = "https://api.anthropic.com/v1/messages" }
    @{ Name = "OpenAI";      Url = "https://api.openai.com/v1/chat/completions" }
)

function Test-Provider {
    param([string]$Name, [string]$Url, [int]$Timeout)

    $code = $null
    $err  = $null
    $sw   = [System.Diagnostics.Stopwatch]::StartNew()

    try {
        $resp = Invoke-WebRequest -Uri $Url -Method POST -Body '{}' `
                    -ContentType 'application/json' -TimeoutSec $Timeout `
                    -UseBasicParsing -ErrorAction Stop
        $code = [int]$resp.StatusCode
    }
    catch {
        $response = $_.Exception.Response
        if ($null -ne $response) {
            try { $code = [int]$response.StatusCode } catch { $code = $null }
        }
        if ($null -eq $code) { $err = $_.Exception.Message }
    }

    $sw.Stop()
    $ms = [int]$sw.Elapsed.TotalMilliseconds

    if ($null -ne $code) {
        $status  = "متاح"
        $detail  = "HTTP $code"
    }
    else {
        $status  = "محجوب"
        if ($err.Length -gt 70) { $err = $err.Substring(0, 70) + "..." }
        $detail  = $err
    }

    [PSCustomObject]@{
        'المزوّد'  = $Name
        'الحالة'   = $status
        'التفصيل'  = $detail
        'زمن(ms)'  = $ms
    }
}

Write-Host ""
Write-Host "=== فحص الوصول للمزوّدين — الحالة: $Label ===" -ForegroundColor Cyan
Write-Host "(أي رمز HTTP يعني وصولاً ناجحاً. 401 ممتاز — يعني أن الخادم ردّ وطلب مفتاحاً.)"
Write-Host ""

$results = foreach ($t in $targets) {
    Write-Host ("  ... " + $t.Name) -NoNewline
    $r = Test-Provider -Name $t.Name -Url $t.Url -Timeout $TimeoutSec
    if ($r.'الحالة' -eq 'متاح') {
        Write-Host "  متاح" -ForegroundColor Green
    } else {
        Write-Host "  محجوب" -ForegroundColor Red
    }
    $r
}

Write-Host ""
$results | Format-Table -AutoSize

$reachable = ($results | Where-Object { $_.'الحالة' -eq 'متاح' }).Count
Write-Host ""
Write-Host "الخلاصة: $reachable من أصل $($targets.Count) مزوّداً متاح في حالة [$Label]." -ForegroundColor Yellow
Write-Host ""

$outFile = Join-Path $PSScriptRoot ("reachability-" + ($Label -replace '\s','_') + ".txt")
$results | Format-Table -AutoSize | Out-File -FilePath $outFile -Encoding UTF8
Write-Host "حُفظت النتيجة في: $outFile"
Write-Host ""
