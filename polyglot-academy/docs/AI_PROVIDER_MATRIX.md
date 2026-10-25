# AI Provider Matrix

| Provider | Adapter | Endpoint / auth | Default model (editable) | Location | Capabilities used | Verified |
|---|---|---|---|---|---|---|
| OpenAI | `OpenAiCompatibleProvider` | `POST {base}/chat/completions`, `Authorization: Bearer` | `gpt-4.1-mini` | Cloud | chat, feedback, translation | Mock HTTP tests only |
| Azure OpenAI (Microsoft) | `AzureOpenAiProvider` | `{endpoint}/openai/deployments/{deployment}/chat/completions?api-version=2024-10-21`, `api-key` header | deployment name | Cloud | chat, feedback | Mock HTTP tests only |
| Google Gemini | `GeminiProvider` | `POST {base}/models/{model}:generateContent`, `x-goog-api-key` (never in URL) | `gemini-2.5-flash` | Cloud | chat, feedback, translation | Mock HTTP tests only |
| Anthropic Claude | `AnthropicProvider` | `POST {base}/messages`, `x-api-key`, `anthropic-version: 2023-06-01` | `claude-haiku-5-5` | Cloud | chat, feedback, translation | Mock HTTP tests only |
| DeepSeek | `OpenAiCompatibleProvider` | `https://api.deepseek.com/chat/completions` | `deepseek-chat` | Cloud | chat, feedback | Mock HTTP tests only |
| Local server (Ollama, llama.cpp server, LM Studio) | `OpenAiCompatibleProvider` | `{base}/chat/completions`, optional key | `llama3.2` | Learner's own server | chat, feedback | Mock HTTP tests only |
| On-device | — | — | — | Device | — | No runtime bundled (honest "not available") |

**No provider has been tested against its live API** (no keys; cloud hosts are outside this
environment's allow-list). Default model names should be checked against each vendor's current
catalogue before release; the learner can change the model in Settings.

Notes
- Consumer subscriptions (ChatGPT Plus, Gemini Advanced, Microsoft Copilot) are not API access;
  the app says so. "Copilot" is only supported through the documented Azure OpenAI API.
- No undocumented endpoints, no scraping.
- Speech uses the device's `SpeechRecognizer` / `TextToSpeech`, not the AI provider.
- Usage counters (requests, approximate tokens) are local and approximate; billing is the
  provider's dashboard.
- Routing, retries and fallback rules: SR-AI-03, SR-PV-03.

## Vision (text recognition)
Photos and scanned PDF pages can be read by vision-capable providers (OpenAI, Gemini, Anthropic) on explicit request. Images are sent as `image_url` data URLs (OpenAI), `inlineData` parts (Gemini) or base64 `image` blocks (Anthropic); request shapes are covered by `AdapterTest.imagesUseEachVendorsDocumentedShape`. Not verified against live endpoints (no keys in this environment).

## Voice services (optional, learner's own keys)
| Service | Used for | Endpoint / auth | Data | Verified |
|---|---|---|---|---|
| Azure AI Speech — pronunciation assessment | Scores per word and phoneme, fluency, completeness, prosody (English) | `POST https://{region}.stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1?language=…&format=detailed`, `Ocp-Apim-Subscription-Key`, `Pronunciation-Assessment` header (base64 JSON: reference text, HundredMark, Phoneme granularity, miscue); key check via `…/sts/v1.0/issueToken` | 16 kHz mono WAV, ≤ 20 s (VOICE_AUDIO) | Mock HTTP tests only |
| OpenAI Realtime | Live voice calls (speech-to-speech, server voice detection, barge-in) | `wss://api.openai.com/v1/realtime?model=gpt-realtime`, `Authorization: Bearer` (the OpenAI key from AI providers); `session.update` with 24 kHz PCM in/out, input transcription | streamed PCM (VOICE_AUDIO) + transcripts | Fake WebSocket tests only; parser accepts current and beta event names |

Locales for pronunciation: en-US/en-GB/en-AU, es-ES/es-MX, fr-FR/fr-CA, zh-CN, ar-SA/ar-EG
(the course variant is used when supported). Check Microsoft's current language list before
release. Realtime model ids and voices are editable in Settings → Voice services.

## Story writing
AI-written graded stories use the normal chat providers (STRUCTURED_FEEDBACK capability, JSON
reply). The reply is validated (sentences, translations, glossary terms present in the text,
well-formed questions) and rejected if incomplete.
