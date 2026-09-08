# Security — do not commit secrets

This is a **public** repository. Never commit credentials or signing material.

## Must stay private (gitignored)

| Item | Notes |
|------|--------|
| `keystore.properties` | Passwords + alias |
| `*.jks` / `*.keystore` | Signing keystores |
| `signing-secret/` | Local signing pack |
| `signing-secret.zip` | Zip backup of signing pack |
| `signing-key-base64.txt` | Base64 keystore for CI |
| `local.properties` | SDK paths (machine-specific) |
| `.env` / credential files | Any API keys or tokens |

Signing secrets for CI belong only in **GitHub Actions repository secrets**:
`SIGNING_KEY`, `KEY_STORE_PASSWORD`, `ALIAS`, `KEY_PASSWORD`.

Local release builds: copy `keystore.properties.example` to `keystore.properties` (gitignored) and fill in values. Keep `mutual-transfer-keystore.jks` on your machine only.

Machine-specific Gradle settings (SDK paths, corporate SSL truststores) belong in
**`~/.gradle/gradle.properties`** or `local.properties` — never in the committed `gradle.properties`.

## If something sensitive was pushed

1. Rotate passwords / create a new upload key if a keystore leaked (coordinate with Play App Signing).
2. Remove the file from git history if needed (`git filter-repo` / BFG) — deleting in a later commit is not enough.
3. Revoke any exposed tokens immediately.
