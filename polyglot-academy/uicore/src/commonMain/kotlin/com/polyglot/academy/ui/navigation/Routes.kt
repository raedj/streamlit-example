package com.polyglot.academy.ui.navigation

import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.model.LanguageCode

enum class Tab { HOME, LEARN, PRACTICE, TUTORS, ME }

enum class SettingsSection { APPEARANCE, LANGUAGES, LEARNING, SPEECH, ACCESSIBILITY, PRIVACY, PROVIDERS, LOCAL_MODELS, REMINDERS, DATA, PERMISSIONS, HELP, ABOUT }

/** Every destination. Routes are plain data so the back stack can be saved across process death. */
sealed interface Route {
    data class TabRoot(val tab: Tab) : Route
    data class Courses(val trackId: String? = null) : Route
    data class LessonOverview(val lessonId: String) : Route
    data class LessonPlayer(val lessonId: String, val restart: Boolean = false) : Route
    data object Review : Route
    data object Mistakes : Route
    data object Grammar : Route
    data class GrammarTopic(val topicId: String) : Route
    data object Reading : Route
    data class ReadingPassage(val passageId: String) : Route
    data object Listening : Route
    data object Writing : Route
    data object Pronunciation : Route
    data object Tones : Route
    data object Characters : Route
    data object Relax : Route
    data object Vocabulary : Route
    data object Library : Route
    data class Material(val materialId: String) : Route
    data object Search : Route
    data object Plan : Route
    data object Progress : Route
    data class Assessment(val language: LanguageCode? = null, val fromOnboarding: Boolean = false) : Route
    data class TutorDetail(val tutorId: String) : Route
    data class Conversation(val tutorId: String, val scenarioId: String? = null, val offline: Boolean = false) : Route
    data object Scenarios : Route
    data object Settings : Route
    data class SettingsPage(val section: SettingsSection) : Route
    data class ProviderDetail(val provider: ProviderId) : Route
    data object History : Route

    companion object {
        fun encode(r: Route): String = when (r) {
            is TabRoot -> "tab|${r.tab}"
            is Courses -> "courses|${r.trackId.orEmpty()}"
            is LessonOverview -> "lesson|${r.lessonId}"
            is LessonPlayer -> "play|${r.lessonId}|${r.restart}"
            Review -> "review"; Mistakes -> "mistakes"; Grammar -> "grammar"
            is GrammarTopic -> "grammarTopic|${r.topicId}"
            Reading -> "reading"
            is ReadingPassage -> "passage|${r.passageId}"
            Listening -> "listening"; Writing -> "writing"; Pronunciation -> "pronunciation"; Tones -> "tones"; Characters -> "characters"; Relax -> "relax"
            Vocabulary -> "vocab"; Library -> "library"
            is Material -> "material|${r.materialId}"
            Search -> "search"; Plan -> "plan"; Progress -> "progress"
            is Assessment -> "assess|${r.language?.name.orEmpty()}|${r.fromOnboarding}"
            is TutorDetail -> "tutor|${r.tutorId}"
            is Conversation -> "conv|${r.tutorId}|${r.scenarioId.orEmpty()}|${r.offline}"
            Scenarios -> "scenarios"; Settings -> "settings"
            is SettingsPage -> "settingsPage|${r.section}"
            is ProviderDetail -> "provider|${r.provider}"
            History -> "history"
        }

        fun decode(s: String): Route? = runCatching {
            val p = s.split('|')
            fun arg(i: Int) = p.getOrElse(i) { "" }
            when (p[0]) {
                "tab" -> TabRoot(Tab.valueOf(arg(1)))
                "courses" -> Courses(arg(1).ifEmpty { null })
                "lesson" -> LessonOverview(arg(1))
                "play" -> LessonPlayer(arg(1), arg(2) == "true")
                "review" -> Review; "mistakes" -> Mistakes; "grammar" -> Grammar
                "grammarTopic" -> GrammarTopic(arg(1))
                "reading" -> Reading
                "passage" -> ReadingPassage(arg(1))
                "listening" -> Listening; "writing" -> Writing; "pronunciation" -> Pronunciation; "tones" -> Tones; "characters" -> Characters; "relax" -> Relax
                "vocab" -> Vocabulary; "library" -> Library
                "material" -> Material(arg(1))
                "search" -> Search; "plan" -> Plan; "progress" -> Progress
                "assess" -> Assessment(arg(1).ifEmpty { null }?.let(LanguageCode::valueOf), arg(2) == "true")
                "tutor" -> TutorDetail(arg(1))
                "conv" -> Conversation(arg(1), arg(2).ifEmpty { null }, arg(3) == "true")
                "scenarios" -> Scenarios; "settings" -> Settings
                "settingsPage" -> SettingsPage(SettingsSection.valueOf(arg(1)))
                "provider" -> ProviderDetail(ProviderId.valueOf(arg(1)))
                "history" -> History
                else -> null
            }
        }.getOrNull()
    }
}
