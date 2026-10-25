package com.polyglot.academy.ui.i18n

import com.polyglot.academy.core.ai.AiCapability
import com.polyglot.academy.core.learning.RecommendationReason
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CorrectionIntensity
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.LearningGoal
import com.polyglot.academy.core.model.LearningStyle
import com.polyglot.academy.core.model.MaterialStatus
import com.polyglot.academy.core.model.MaterialType
import com.polyglot.academy.core.model.ProviderLocation
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.model.ThemeMode
import com.polyglot.academy.core.service.OfflineReason
import com.polyglot.academy.core.speech.SpeechErrorKind
import com.polyglot.academy.core.tutor.CorrectionStrategy
import com.polyglot.academy.core.tutor.TeachingStyle
import com.polyglot.academy.core.util.MILLIS_PER_DAY
import com.polyglot.academy.core.util.MILLIS_PER_MINUTE

/** Maps domain enums to localized labels. Every enum value has a key (checked by LabelsTest). */
object Labels {
    fun language(s: Localizer, code: LanguageCode) = Languages.of(code).displayName(s.language)
    fun level(s: Localizer, l: CefrLevel) = s[K.valueOf("LEVEL_${l.name}")]
    fun goal(s: Localizer, g: LearningGoal) = s[K.valueOf("GOAL_${g.name}")]
    fun style(s: Localizer, st: LearningStyle) = s[K.valueOf("STYLE_${st.name}")]
    fun styleDesc(s: Localizer, st: LearningStyle) = s[K.valueOf("STYLE_${st.name}_DESC")]
    fun skill(s: Localizer, sk: Skill) = s[K.valueOf("SKILL_${sk.name}")]
    fun correction(s: Localizer, c: CorrectionIntensity) = s[K.valueOf("CORRECTION_${c.name}")]
    fun correctionDesc(s: Localizer, c: CorrectionIntensity) = s[K.valueOf("CORRECTION_${c.name}_DESC")]
    fun theme(s: Localizer, t: ThemeMode) = s[K.valueOf("THEME_${t.name}")]
    fun severity(s: Localizer, e: ErrorSeverity) = s[K.valueOf("SEVERITY_${e.name}")]
    fun errorCategory(s: Localizer, category: String) =
        s[K.valueOf("ERR_CAT_" + (category.takeIf { it in com.polyglot.academy.core.tutor.CoachProtocol.errorCategories } ?: "other").uppercase())]
    fun register(s: Localizer, r: com.polyglot.academy.core.model.Register) = s[K.valueOf("REGISTER_${r.name}")]
    fun coachTrack(s: Localizer, t: com.polyglot.academy.core.model.CoachTrack, language: LanguageCode) =
        if (t == com.polyglot.academy.core.model.CoachTrack.EXAM) s.f(K.COACH_TRACK_EXAM, com.polyglot.academy.core.tutor.CoachProtocol.examName(language).replace("an Arabic proficiency exam", s[K.COACH_EXAM_ARABIC]).replace(" or ", " / "))
        else s[K.valueOf("COACH_TRACK_${t.name}")]
    fun coachTitle(s: Localizer, t: com.polyglot.academy.core.tutor.CoachTitle) = s[K.valueOf("COACH_TITLE_${t.name}")]
    fun speechError(s: Localizer, e: SpeechErrorKind) = s[K.valueOf("SPEECH_ERR_${e.name}")]
    fun offlineReason(s: Localizer, r: OfflineReason) = s[K.valueOf("OFFLINE_REASON_${r.name}")]
    fun reason(s: Localizer, r: RecommendationReason) = s[K.valueOf("REASON_${r.name}")]
    fun teachingStyle(s: Localizer, t: TeachingStyle) = s[K.valueOf("TUTOR_STYLE_${t.name}")]
    fun correctionStrategy(s: Localizer, c: CorrectionStrategy) = s[K.valueOf("TUTOR_CORR_${c.name}")]
    fun category(s: Localizer, c: DataCategory) = s[K.valueOf("CAT_${c.name}")]
    fun categoryDesc(s: Localizer, c: DataCategory) = s[K.valueOf("CAT_${c.name}_SUB")]
    fun materialType(s: Localizer, t: MaterialType) = s[K.valueOf("MTYPE_${t.name}")]
    fun materialStatus(s: Localizer, t: MaterialStatus) = s[K.valueOf("MSTATUS_${t.name}")]
    fun session(s: Localizer, k: SessionKind) = s[K.valueOf("SESSION_${k.name}")]
    fun capability(s: Localizer, c: AiCapability) = s[K.valueOf("CAP_${c.name}")]
    fun location(s: Localizer, l: ProviderLocation) = s[K.valueOf("PROV_LOC_${l.name}")]
    fun achievement(s: Localizer, id: String) = runCatching { s[K.valueOf("ACH_$id")] }.getOrDefault(id)
    fun day(s: Localizer, isoDay: Int) = s[K.valueOf("DAY_$isoDay")]
    fun dayInitial(s: Localizer, isoDay: Int) = s[K.valueOf("DAY_INITIAL_$isoDay")]

    /** Human duration for review intervals: "5 min", "3 days", "2 months". */
    fun duration(s: Localizer, millis: Long): String {
        val minutes = (millis / MILLIS_PER_MINUTE).toInt()
        return when {
            minutes < 1 -> s[K.NOW]
            minutes < 60 -> s.plural(K.MINUTES, minutes)
            minutes < 60 * 24 -> s.plural(K.HOURS, minutes / 60)
            millis < 45 * MILLIS_PER_DAY -> s.plural(K.DAYS, (millis / MILLIS_PER_DAY).toInt().coerceAtLeast(1))
            else -> s.plural(K.MONTHS, (millis / (30 * MILLIS_PER_DAY)).toInt())
        }
    }

    fun clockTime(seconds: Int): String {
        val m = seconds / 60; val sec = seconds % 60
        return "$m:${sec.toString().padStart(2, '0')}"
    }
}
