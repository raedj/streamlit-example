package com.polyglot.academy.core.service

import com.polyglot.academy.core.assessment.DiagnosticSession
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LanguageEnrollment
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.repo.ProfileRepository
import com.polyglot.academy.core.repo.ProgressRepository
import com.polyglot.academy.core.util.IdGenerator
import com.polyglot.academy.core.util.TimeSource

class AssessmentService(
    private val profiles: ProfileRepository,
    private val progress: ProgressRepository,
    private val achievements: AchievementService,
    private val clock: TimeSource,
    private val ids: IdGenerator,
) {
    fun start(language: LanguageCode, selfReported: CefrLevel?): DiagnosticSession =
        DiagnosticSession(language, Curriculum.of(language).assessment, selfReported ?: CefrLevel.A1)

    suspend fun finish(session: DiagnosticSession, language: LanguageCode): AssessmentResult {
        val result = session.result(ids.newId("assess"), clock.nowMillis())
        progress.saveAssessment(result)
        achievements.evaluateAssessment()
        return result
    }

    /** The learner may accept the estimate or choose a different starting level. */
    suspend fun applyStartingLevel(language: LanguageCode, level: CefrLevel, fromAssessment: Boolean) {
        profiles.updateProfile { p ->
            val existing = p.enrollment(language)
            val enrollment = existing?.copy(startingLevel = level, levelFromAssessment = fromAssessment, targetLevel = maxOf(existing.targetLevel, level.next()))
                ?: LanguageEnrollment(language, Languages.of(language).defaultVariantId, level, fromAssessment)
            p.copy(learningLanguages = p.learningLanguages.filter { it.language != language } + enrollment)
        }
    }
}
