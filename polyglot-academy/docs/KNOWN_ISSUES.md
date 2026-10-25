# Known Issues and Limitations

| ID | Severity | Issue | Impact / workaround |
|---|---|---|---|
| K-01 | High (process) | Android module never compiled; compile errors are possible | Build with Google Maven and fix before any release |
| K-02 | High (process) | UI never executed; layout, clipping, RTL mirroring and TalkBack order unverified | Run `:ui:desktopTest`, capture screenshots, test on devices |
| K-03 | Medium | Default AI model ids may be outdated by the time of release | Learner can edit the model; verify against vendor catalogues |
| K-04 | Medium | PDFs with real text are extracted on device (pure Kotlin). Photos and scanned PDFs can be read with a vision AI provider (OpenAI, Gemini, Claude) on explicit request; this sends the images to that provider and needs cloud AI allowed. Scanned PDFs need page rendering, which exists on Android (PdfRenderer) but not in the desktop harness. No on-device OCR; no audio/video transcription; encrypted PDFs and LZW content unsupported. Arabic/Hebrew lines are ordered by glyph position; multi-column layouts can come out in the wrong order | Paste text or import .txt/.srt/.vtt for unsupported files |
| K-05 | Medium | No on-device LLM runtime; on-device provider is always unavailable | Use a local OpenAI-compatible server or a cloud provider |
| K-06 | Low | Restore implemented (Settings → Your data); the restored reminder is re-scheduled (asking for notification permission if it is on). Version 1 exports restore profile, settings, lessons, reviews, vocabulary and mistakes only; reduced v1 phrase/assessment/note records are skipped and counted | Fixed 2026-10-25 (partial for v1 files) |
| K-07 | Low | Built-in content is still modest (73–93 words and 10–16 lessons per language). Every language now reaches B1, with B2 grammar lessons in English, Spanish and French (conditionals, subjunctive); there is no C1 general content, and Spanish/French/Mandarin/Arabic professional tracks have one lesson each | Partly addressed 2026-10-25; content packs can be added without engine changes |
| K-08 | Low | Pronunciation feedback is recognizer text similarity, not phonetic/tone analysis (disclosed in UI) | — |
| K-09 | Low | Character writing practice exists (trace/from memory, shape-overlap check against the font glyph, stroke counts and components for 98 course characters). Stroke order and direction are not checked, and there is no animated stroke-order demo (no licence-compatible stroke data bundled); traditional characters have no breakdowns | Partly addressed 2026-10-25 |
| K-10 | Low | Avatars are 2D. While the tutor speaks, mouth shapes follow the spelling of the word the TTS engine reports (Android range events); this is an approximation, not phoneme-accurate lip-sync. Engines without progress events fall back to a simple rhythm | — |
| K-11 | Low | Reminder notification text is fixed (no per-language variety beyond EN/AR) | — |
| K-12 | Low | Bar and line charts support tap-to-inspect (tooltip with label and value); there is no pinch-zoom | Values are labelled and described for screen readers |
| K-13 | Low | Professional-track role-plays exist for all five languages, but finance, credit-risk and legal tracks are English only | — |
| K-14 | Low | Compose Multiplatform 1.7.3 / Kotlin 2.1.21 chosen for offline resolution; newer versions exist | Upgrade once Google Maven is reachable |
| K-15 | Medium | Speaking path (30 missions, A1–C1): missions without a built-in role-play need an AI tutor; pass criteria (turns, median turn length, tutor confirmation) are practice targets, not a certified level. Speaking rate uses recognizer timing and is approximate; Mandarin word counts are estimated from characters | — |
