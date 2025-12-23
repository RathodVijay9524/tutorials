$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    source_code = "public class Main {`n    public static void main(String[] args) {`n        System.out.println(`"Hello World`");`n    }`n}"
    language_id = 62
    cpu_time_limit = 5
    memory_limit = 256000
} | ConvertTo-Json

Write-Host "Sending request to Judge0..."
Write-Host "Body: $body"

try {
    $response = Invoke-RestMethod -Uri "http://localhost:2358/submissions?wait=true" -Method Post -Headers $headers -Body $body
    Write-Host "`nSuccess! Response:"
    $response | ConvertTo-Json -Depth 5
} catch {
    Write-Host "`nError:"
    Write-Host $_.Exception.Message
    if ($_.ErrorDetails) {
        Write-Host "Details: $($_.ErrorDetails.Message)"
    }
}
