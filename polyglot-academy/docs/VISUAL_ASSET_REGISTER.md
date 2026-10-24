# Visual Asset Register

All assets are original and drawn in code (no bitmaps, no third-party artwork, no generated
images). Source: `ui/.../designsystem/Illustrations.kt` unless noted.

| Asset | Purpose | Scaling | Accessibility | Theme | Source |
|---|---|---|---|---|---|
| `TutorAvatar` ×10 (from `AvatarSpec`) | Tutor portraits; idle/listening ring/thinking dots/speaking mouth | Vector, any size (56–120 dp used) | `TUTOR_AVATAR_DESC` content description | Uses accent + theme surface | Original, this repo |
| `HeroIllustration` WELCOME | Onboarding/new learner: speech bubbles "Aa ع 中 Ñ" | Vector | Generic "Illustration" description | Theme colours | Original |
| REVIEW_DONE / SUCCESS | Star + arc for completion states | Vector | ″ | ″ | Original |
| EMPTY_LIBRARY | Stacked documents | Vector | ″ | ″ | Original |
| PRIVACY | Shield with lock | Vector | ″ | ″ | Original |
| OFFLINE / ERROR | Cloud with slash / alert disc | Vector | ″ | ″ | Original |
| MICROPHONE | Voice intro | Vector | ″ | ″ | Original |
| PLAN / ANALYTICS_EMPTY / RELAX | Plan grid, faded bars, concentric circles | Vector | ″ | ″ | Original |
| `AchievementBadge` | Medal with glyph | Vector | Achievement name | Accent / disabled | Original |
| Launcher icon `res/drawable/ic_launcher.xml` | App icon (two speech bubbles) | Vector drawable | n/a | Fixed brand colours | Original |
| Material Icons (filled/auto-mirrored) | Action icons | Vector | Content descriptions on meaningful icons | Tinted by theme | Apache-2.0 (Google), via Compose |

Not yet provided: adaptive-icon foreground/background layers and Play Store artwork; vocabulary
images; cultural-context illustrations; stroke-order animations for Chinese characters.
