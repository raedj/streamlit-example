# Known Issues and Limitations

| ID | Severity | Issue | Impact / workaround |
|---|---|---|---|
| K-01 | High (process) | Android module never compiled; compile errors are possible | Build with Google Maven and fix before any release |
| K-02 | High (process) | UI never executed; layout, clipping, RTL mirroring and TalkBack order unverified | Run `:ui:desktopTest`, capture screenshots, test on devices |
| K-03 | Medium | Default AI model ids may be outdated by the time of release | Learner can edit the model; verify against vendor catalogues |
| K-04 | Medium | PDFs with real text are extracted on device (pure Kotlin; Flate/ASCII streams, object streams, ToUnicode/CID fonts, form XObjects). Not supported: scanned PDFs (OCR), encrypted PDFs, LZW-compressed content, image OCR, audio/video transcription — each is reported, not guessed. Arabic/Hebrew lines are ordered by glyph position (visual → reading order, ActualText honoured); other lines keep content-stream order, so multi-column text and tables can come out in the wrong order (the UI asks the learner to check) | Paste text or import .txt/.srt/.vtt for unsupported files |
| K-05 | Medium | No on-device LLM runtime; on-device provider is always unavailable | Use a local OpenAI-compatible server or a cloud provider |
| K-06 | Low | Restore implemented (Settings → Your data); the restored reminder is re-scheduled (asking for notification permission if it is on). Version 1 exports restore profile, settings, lessons, reviews, vocabulary and mistakes only; reduced v1 phrase/assessment/note records are skipped and counted | Fixed 2026-10-25 (partial for v1 files) |
| K-07 | Low | Built-in content is still modest (roughly 55–90 words and 8–14 lessons per language). Every language now has at least one professional track; Spanish, French, Mandarin and Arabic have only a single "for work" lesson and role-play, at B1 | Partly addressed 2026-10-25; content packs can be added without engine changes |
| K-08 | Low | Pronunciation feedback is recognizer text similarity, not phonetic/tone analysis (disclosed in UI) | — |
| K-09 | Low | Chinese stroke order, handwriting practice and character components not implemented | — |
| K-10 | Low | Avatars are level 2 (2D animated); no lip-sync | — |
| K-11 | Low | Reminder notification text is fixed (no per-language variety beyond EN/AR) | — |
| K-12 | Low | Charts are custom Canvas drawings without zoom/tooltips | Values are labelled and described for screen readers |
| K-13 | Low | Professional-track role-plays exist for all five languages, but finance, credit-risk and legal tracks are English only | — |
| K-14 | Low | Compose Multiplatform 1.7.3 / Kotlin 2.1.21 chosen for offline resolution; newer versions exist | Upgrade once Google Maven is reachable |
