# Google Play Data safety: draft answers

Draft for the owner to check before filling in Play Console → App content → Data safety. It follows
what the code does today (see PRIVACY_AND_SECURITY.md). It is not legal advice, and Google's form
changes, so read each question as it is asked when you fill it in.

## The key point
The app itself collects nothing: there is no server, no account, no analytics or advertising SDK.
Data leaves the phone only when the learner turns on an optional feature and points it at a service
they chose: their own AI provider key, their own Azure Speech resource, or their own WebDAV server.
In Play's terms that is data **shared with a third party at the user's request**, and it must be
declared, because the app sends it.

## Answers

| Question | Draft answer | Why |
|---|---|---|
| Does the app collect or share any of the required user data types? | Yes | Optional cloud features send learner text and, if allowed, voice to the provider the learner set up |
| Is all collected data encrypted in transit? | Yes | Every cloud request uses HTTPS; WebDAV sync requires HTTPS and the file is also encrypted on the phone |
| Do you provide a way for users to request deletion? | Yes | Everything is on the phone: Settings → Your data → delete; uninstalling removes it. Data already sent to a provider follows that provider's policy |

Data types to declare, all **shared, optional, at the user's request, not used for advertising**:

| Play data type | When it leaves the phone | Recipient |
|---|---|---|
| Messages → Other in-app messages | Tutor conversations, writing corrections, AI stories (cloud AI on) | The AI provider the learner chose |
| Audio → Voice or sound recordings | Pronunciation scoring, live voice calls (cloud AI on and "Voice recordings" allowed; off by default) | The learner's Azure Speech resource; OpenAI Realtime |
| Photos and videos → Photos | Reading a photo with a cloud model (on request, per item) | The AI provider the learner chose |
| Files and docs | A page of an imported document sent for cloud reading (on request) | The AI provider the learner chose |
| App activity → Other user-generated content | Encrypted sync file (progress, vocabulary, notes) | The learner's own WebDAV server; it sees only ciphertext |

Not collected and not shared: location, contacts, identifiers, financial data, health, app
interaction analytics, crash logs. The opt-in usage log for trials (Settings → Your data) never
leaves the phone; the tester exports and sends the file themselves, so it is not collection by the
app. Re-check this answer if the log is ever sent automatically.

Model downloads (Qwen chat models, Whisper, OCR data) are requests to Hugging Face and GitHub that
carry no learner data.

## Before submitting
- Host the privacy policy at a public URL (the in-app "How your data is handled" page is the
  source) and put the same URL in the store listing.
- The app uses `RECORD_AUDIO`, `POST_NOTIFICATIONS` and network access; the store listing should say
  why (speaking practice, reminders, optional cloud AI and sync).
- Content is AI-written and not reviewed by native speakers (K-23); say so in the description.
