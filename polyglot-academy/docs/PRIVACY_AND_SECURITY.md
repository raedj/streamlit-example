# Privacy and Security

## What is stored on the device
Profile, settings, progress, attempts, mistakes, review states/logs, conversations and turns,
saved phrases, user vocabulary, imported text, notes, bookmarks, provider configuration (not
keys) — in the app's private SQLite database. API keys: Android Keystore AES-256-GCM, ciphertext
in a private SharedPreferences file.

## What may leave the device
Only text in categories the learner allowed, only to the provider they configured, only when
cloud AI is on and the current consent version is accepted. Raw audio never leaves via this app
(the device's speech recognizer may process audio according to its own settings; the app
reports whether recognition is on-device when Android exposes it).

## Controls
Cloud AI master switch (off by default) + consent dialog; per-category switches (sensitive
categories off by default); per-item consent for imported material; local-network AI switch;
cloud fallback switch (off by default); backup opt-in; export (no secrets); delete
conversations / imported material / everything (incl. keys and reminder).

## Security measures
- No secrets in source code; keys entered by the learner at runtime.
- `SecretRedactor` strips key-like strings; error messages never include keys or user content (tested).
- HTTPS only (network security config); cleartext allowed only for 127.0.0.1/localhost/10.0.2.2 (own local server).
- No `followRedirects` for provider calls; timeouts on every request.
- Backup rules exclude the secrets file and the raw database; optional backup uses a secret-free export.
- Permissions requested just in time (microphone, notifications); file access via the system picker (no storage permission).
- No analytics or crash reporting SDKs; all metrics stay local.

## Privacy tests (automated, passing)
Cloud off by default; sensitive categories blocked; per-item consent; outdated consent blocks;
router never uses denied providers; no silent cloud fallback; offline excludes network
providers; no request is made when cloud is disabled (integration test); errors don't leak keys;
redaction; export excludes conversations/notes unless chosen and never contains keys; delete-all
removes data, keys and reminder.

## Not yet done
Penetration testing; review of Android Keystore behaviour on devices without StrongBox;
dependency vulnerability scan (no scanner available offline); verifying third-party speech
service data handling per device vendor.
