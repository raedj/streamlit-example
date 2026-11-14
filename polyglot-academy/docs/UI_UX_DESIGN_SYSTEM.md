# UI/UX Design System

Source: `uicore/.../designsystem/PaletteTokens.kt`, `ui/.../designsystem/{Tokens,Typography,Theme,Components,Illustrations,Charts}.kt`.

## Principles
Calm, friendly, uncluttered. One primary action per card. Real data only — empty states explain
what will appear instead of showing placeholders as if they were statistics. Status is never
conveyed by colour alone (icon + text). Content width is capped at 720 dp on large screens.

## Colour tokens (light / dark)
| Token | Light | Dark | Use |
|---|---|---|---|
| primary / onPrimary | #0B6E6A / #FFFFFF | #5ED6CC / #00302E | Primary actions, progress |
| secondary | #4F4FC2 | #B2B2FF | Secondary emphasis, AI/processing |
| accent | #B45309 | #FFB960 | Streaks, achievements |
| background / surface / card | #F6F7F9 / #FFFFFF / #FFFFFF | #0E1316 / #151B1F / #1A2125 | Layers |
| textPrimary / textSecondary / textMuted | #151A21 / #424B57 / #5F6874 | #E9EEF1 / #BCC6CC / #97A2A9 | Text hierarchy |
| success / warning / error / info | #116A31 / #9A4A00 / #B42318 / #1D4ED8 | #6EE7A0 / #FFC266 / #FF9C92 / #93B4FF | Feedback (always with containers) |
| chart series | teal, indigo, amber, rose, green, brown | lighter equivalents | Charts |

High-contrast mode strengthens secondary/muted text and borders. All text pairs are tested at
≥ 4.5:1 (WCAG AA) in `ContrastTest` for light, dark and both high-contrast variants.

## Typography
Platform default family (Roboto + Noto Sans Arabic / Noto Sans CJK fallback on Android).
Scale: display 32, headline 26/23, title 21/17/15, body 16/14.5/13, label 15/13/12 sp. Line
heights ≈ 1.45× with `LineHeightStyle.Trim.None` so Arabic diacritics and pinyin tone marks are
not clipped. Learning styles: term 28 sp, hanzi 36 sp (line 52), pinyin 16 sp, sentence 19 sp,
statistics 30/20 sp. "Larger text" multiplies the scale by 1.15 on top of system font scaling.
Target-language text gets a `LocaleList` hint (e.g. `zh-CN`) for correct glyph selection.

## Spacing, shape, elevation
Spacing 2/4/8/12/16/24/32/48 dp; gutter 16 dp; touch target 48 dp. Radii 8/12/16/24 dp and pill.
Cards are flat with a 1 dp divider border (elevation 0) for a calm look in both themes.

## Components
ScreenScaffold, ContentColumn, PrimaryButton (loading state), SecondaryButton, QuietButton,
PolyCard (clickable with role/label), SectionHeader (heading semantics), StatTile, ProgressRing
(progress semantics), LabeledProgress, Banner (info/success/warning/error, live region),
LevelBadge, Pill, ChoiceCard (radio/checkbox semantics), ChipGroup, SettingsRow, SwitchRow
(switch role), AppTextField (secret mode), StateMessage (empty/error), LoadingPlaceholder
(static under reduced motion), ConfirmDialog, AudioButton, MicButton (idle/listening/processing/
unavailable, pulse + icon + state description), LearningText (locale + reading line),
FeedbackPanel (assertive live region), IconBadge, TokenChip/MatchCell (exercise-specific).

## Charts
BarChart (zero-based axis, value labels, RTL mirroring), LineChart (fixed 0–100 % axis),
CalendarHeatmap, StackedBar with numeric legend, SkillBars (used instead of a radar chart).
Every chart takes a text `description` exposed to screen readers listing the values.

## Motion
Short fades/slides (≤ 220 ms) for navigation, animated progress, avatar speaking/listening
states, breathing guide. With reduced motion every duration becomes 0 and infinite animations
are replaced by static states.

## RTL
Arabic interface sets `LayoutDirection.Rtl` app-wide; icons use AutoMirrored variants where
direction matters; charts mirror time order.
