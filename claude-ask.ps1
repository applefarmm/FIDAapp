# ========================
# Configuration
# ========================
$API_KEY = "your-api-key"                    # Your actual API key
$BASE_URL = "https://your-third-party-proxy.com/v1"  # ✅ Your 3rd-party base URL
$MODEL = "claude-3-sonnet-20240229"         # Model name (adjust if needed)
$TIMEOUT = 30                               # Timeout in seconds

# ========================
# Function: Call API via Custom Base URL
# ========================
function Invoke-Claude {
    param(
        [string]$Prompt,
        [int]$MaxTokens = 1024
    )

    $body = @{
        model = $MODEL
        max_tokens = $MaxTokens
        temperature = 0.3
        messages = @(
            @{
                role = "user"
                content = $Prompt
            }
        )
    } | ConvertTo-Json -Depth 10

    $headers = @{
        "x-api-key" = $API_KEY
        "anthropic-version" = "2023-06-01"
        "Content-Type" = "application/json"
    }

    try {
        $response = Invoke-RestMethod -Uri "$BASE_URL/messages" `
            -Method Post `
            -Body $body `
            -Headers $headers `
            -TimeoutSec $TIMEOUT `
            -ErrorAction Stop

        return $response.content[0].text
    }
    catch {
        Write-Error "API Error: $($_.Exception.Message)"
        Write-Error "Status: $($_.Exception.Response.StatusCode)"
        Write-Error "Response: $($_.ErrorDetails.Message)"
        return $null
    }
}

# ========================
# Step 1: Architecture Summary
# ========================
$archPrompt = @"
Analyze the `domain`, `data`, and `presentation` packages. Describe the architecture pattern used (e.g., Clean Architecture). Include:
- Responsibilities of each layer
- Key files and classes
- How dependencies are managed (e.g., Koin)
- Any use of Coroutines, Flow, or Repository Pattern
"@

Write-Host "🔍 Analyzing architecture..."
$architecture = Invoke-Claude -Prompt $archPrompt -MaxTokens 1024
if ($architecture) {
    Write-Host "`n--- Architecture Summary ---`n$architecture`n"
} else {
    Write-Error "❌ Failed to get architecture summary."
}

# ========================
# Step 2: Database Design
# ========================
$dbPrompt = @"
Analyze the Room database setup. List all entities, relationships, and key DAOs. Include:
- Entity names and fields
- Foreign key relationships
- Sample `@Query` or `@Insert` method with explanation
- Migration strategy (if any)
- Indexing and constraints used
"@

Write-Host "🔍 Analyzing database design..."
$database = Invoke-Claude -Prompt $dbPrompt -MaxTokens 1024
if ($database) {
    Write-Host "`n--- Database Design ---`n$database`n"
} else {
    Write-Error "❌ Failed to get database design."
}

# ========================
# Step 3: Final Report
# ========================
$reportPrompt = @"
Now combine the two sections above into a single, formal university project report using the academic template. Include:
- Abstract
- Introduction
- System Architecture
- Database Design
- Implementation
- Testing Strategy
- Conclusion
- References
- Appendices (e.g., project structure, ERD)

Use formal academic tone. Be technical, clear and detailed.
"@

Write-Host "🔍 Generating full report..."
$fullReport = Invoke-Claude -Prompt $reportPrompt -MaxTokens 2048
if ($fullReport) {
    Write-Host "`n--- Full Report ---`n$fullReport`n"
    $fullReport | Out-File -FilePath "academic_report.md" -Encoding UTF8
    Write-Host "✅ Report saved to 'academic_report.md'"
} else {
    Write-Error "❌ Failed to generate full report."
}
