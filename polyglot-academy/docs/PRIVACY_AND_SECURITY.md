# Privacy and Security

## What is stored on the device
Profile, settings, progress, attempts, mistakes, review states/logs, conversations and turns,
saved phrases, user vocabulary, imported text, notes, bookmarks, provider configuration (not
keys) — in the app's private SQLite database. API keys: Android Keystore AES-256-GCM, ciphertext
in a private SharedPreferences file.

## What may leave the device
Only text in categories the learner allowed, only to the provider they configured, only when
cloud AI is on and the current consent version is accepted. The device's speech recognizer may
process audio according to its own settings; the app reports whether recognition is on-device
when Android exposes it.

Raw microphone audio leaves the device only for two optional features, and only when cloud AI is
on **and** the "Voice recordings" category (off by default) is allowed:
- **Detailed pronunciation scoring**: one recording (at most 20 s) per check, sent to the
  learner's own Azure AI Speech resource with the reference sentence. Not stored by the app.
- **Live voice calls**: audio streams to OpenAI Realtime during the call; only the text
  transcript is stored. Each learner sentence is also checked as text (CONVERSATION_TEXT).

**Sync** (optional) uploads one file to the learner's own WebDAV server. It is encrypted on the
device (PBKDF2-HMAC-SHA256, 210,000 iterations → AES-256-GCM, fresh salt and nonce per upload)
with a passphrase that never leaves the device; the server sees only ciphertext. Device settings
(privacy choices, theme, voice services) and all keys stay on each device.

**AI-written stories** send only the chosen topic, level and a few known words (LESSON_CONTENT).

## Controls
Cloud AI master switch (off by default) + consent dialog; per-category switches (sensitive
categories off by default); per-item consent for imported material; local-network AI switch;
cloud fallback switch (off by default); "Voice recordings" category (off by default) for pronunciation scoring and live calls; sync connection with disconnect; backup opt-in; export (no secrets); delete
conversations / imported material / everything (incl. keys and reminder).

## Security measures
- No secrets in source code; keys entered by the learner at runtime.
- `SecretRedactor` strips key-like strings; error messages never include keys or user content (tested).
- HTTPS only (network security config); cleartext allowed only for 127.0.0.1/localhost/10.0.2.2 (own local server).
- No `followRedirects` for provider calls; timeouts on every request.
- Reading a photo or scanned PDF with AI sends its images to the chosen vision provider only after a confirmation dialog, only when cloud AI is enabled (the global switch is checked first), and is tagged IMPORTED_MATERIAL. Files are kept in memory for this, never stored.
- Imported PDFs are parsed on the device; their bytes are never sent anywhere, and only the extracted text is stored.
- Backup rules exclude the secrets file and the raw database; optional backup uses a secret-free export.
- Restore (`SqlStore.importJson`) checks the format and version, runs in one transaction (a bad file changes nothing),
  never reads or writes secrets, and always switches cloud AI off and resets the accepted consent version,
  so a file from another device cannot silently re-enable cloud processing.
- Permissions requested just in time (microphone, notifications); file access via the system picker (no storage permission).
- No analytics or crash reporting SDKs; all metrics stay local.
- Speech-service key, WebDAV password and sync passphrase are kept in the secure store like API keys, never exported and removed by "delete everything" and "disconnect".
- Azure region and realtime model ids are validated (letters/digits only) before they are put into a URL; WebDAV requires HTTPS (plain HTTP only to localhost, as in the network security config).
- Sync downloads keep the device's own settings, so a synced file cannot switch on cloud AI or voice upload on another device.

## Privacy tests (automated, passing)
Cloud off by default; sensitive categories blocked; per-item consent; outdated consent blocks;
router never uses denied providers; no silent cloud fallback; offline excludes network
providers; no request is made when cloud is disabled (integration test); errors don't leak keys;
redaction; export excludes conversations/notes unless chosen and never contains keys; delete-all
removes data, keys and reminder; pronunciation scoring and live calls refuse to send audio without
the voice category; the speech key is never exported; sync file is ciphertext only, wrong
passphrase and tampering are rejected, device settings survive a download.

## Not yet done
Penetration testing; review of Android Keystore behaviour on devices without StrongBox;
dependency vulnerability scan (no scanner available offline); verifying third-party speech
service data handling per device vendor.
