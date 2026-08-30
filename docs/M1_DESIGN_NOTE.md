# M1 Design Note — Skeleton & Providers

## Goal
Scaffold the Android project with Clean Architecture modules, Hilt DI, secure key
storage, four AI providers (Anthropic, OpenAI, DeepSeek, Gemini), a Settings screen
for key management, and a plain streaming chat screen that verifies each provider
end-to-end. GitHub Models kept as a non-blocking stub.

## Module structure

```
braining/
├── app/                          # Entry point, navigation, DI wiring, first-run
├── core-ui/                      # Compose design system, Material 3, RTL/Arabic
├── core-domain/                  # Pure Kotlin: entities, use-cases, interfaces
├── core-data/                    # Repositories, Room, DataStore, Keystore store
├── ai-providers/                 # One AiProvider impl per vendor
├── feature-settings/             # Keys, provider toggles, verify
├── feature-chat/                 # Plain streaming chat screen (M1 verification)
├── build-logic/                  # Convention plugins (shared Gradle config)
```

## Key interfaces (core-domain)

```kotlin
// One per AI vendor. Adding a provider = one isolated file in ai-providers/.
interface AiProvider {
    val id: ProviderId
    val displayName: String
    val capabilities: ProviderCapabilities
    fun complete(request: AiRequest): Flow<AiChunk>
}

data class ProviderCapabilities(
    val streaming: Boolean = true,
    val maxContextTokens: Int,
    val costTier: CostTier,       // FREE, LOW, MEDIUM, HIGH
)

sealed class AiChunk {
    data class Token(val text: String) : AiChunk()
    data class Done(val usage: TokenUsage?) : AiChunk()
    data class Error(val message: String) : AiChunk()
}

data class AiRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val systemPrompt: String? = null,
    val maxTokens: Int = 4096,
    val temperature: Double = 0.7,
)
```

## Data flow (M1)

```
User types message
  → ChatViewModel sends AiRequest to selected AiProvider
    → Provider makes SSE POST to vendor API (Ktor client)
      → Tokens streamed back via Flow<AiChunk>
        → ChatViewModel updates UI state token-by-token
          → ChatScreen renders streaming response
```

## Provider implementations (ai-providers)

| Provider | Base URL | Auth header | Default model |
|---|---|---|---|
| Anthropic | api.anthropic.com | x-api-key + anthropic-version | claude-sonnet-4-20250514 |
| OpenAI | api.openai.com | Authorization: Bearer | gpt-4o |
| DeepSeek | api.deepseek.com | Authorization: Bearer | deepseek-chat |
| Gemini | generativelanguage.googleapis.com | URL key param | gemini-2.0-flash |
| GitHub Models | (stub only) | — | — |

All use SSE streaming via Ktor client.

## Secure key storage (core-data)

- Android Keystore generates/holds an AES-256 key (hardware-backed on supported devices).
- Encrypted API keys stored in DataStore Preferences (encrypted bytes).
- `KeyStore` interface in core-domain, `EncryptedKeyStore` impl in core-data.

## Settings screen (feature-settings)

- List of providers with: toggle (enabled/disabled), API key input (masked),
  model selector dropdown, "Verify" button (sends a tiny completions call),
  status indicator (green check / red X / grey off).
- Google Gemini shown first with "Free tier — recommended to start" label.
- GitHub Models shown last with "Optional — requires legacy access" label.

## String resources

- `values/strings.xml` — English (secondary)
- `values-ar/strings.xml` — Arabic (default, primary)
- All user-facing strings use `R.string.*` — zero hardcoded text.

## Signing config

- Keystore generated via `keytool`, stored outside version control.
- `keystore.properties` gitignored, read in `build.gradle.kts`.
- `signingConfigs.release` wired to `buildTypes.release`.

## Assumptions

1. Ktor client is the HTTP layer (already in spec). Kotlinx Serialization for JSON.
2. Gemini uses the REST API with API key as URL query parameter (simpler than OAuth for BYOK).
3. GitHub Models stub: a toggle in Settings that says "Not configured" — no implementation.
4. No Room yet (M5). No voice (M2). No clarify (M3). M1 is purely text chat + providers.
