package com.polyglot.academy.core.service

import com.polyglot.academy.core.model.LanguageCode

/**
 * Which parts of the app a build shows. The full app is the default; the trial build for the first testers
 * (Android with -Ppolyglot.trial=true) teaches English, Mandarin and Malay and asks fewer setup questions, so
 * testers see what is being tested and nothing else (UI first-run audit, 2026-11-06; owner's scope, 2026-11-07).
 */
data class AppEdition(
    /** Languages a learner can pick or add; null means every language the app has. */
    val learnableLanguages: Set<LanguageCode>? = null,
    /** Onboarding asks only for the app language, level, daily time and privacy; the rest keeps its defaults. */
    val shortOnboarding: Boolean = false,
) {
    fun canLearn(code: LanguageCode): Boolean = learnableLanguages?.contains(code) ?: true

    /** True when there is only one language to learn, so pickers and switchers have nothing to offer. */
    val singleLanguage: Boolean get() = learnableLanguages?.size == 1

    companion object {
        val FULL = AppEdition()
        val TRIAL = AppEdition(learnableLanguages = setOf(LanguageCode.ENGLISH, LanguageCode.MANDARIN, LanguageCode.MALAY), shortOnboarding = true)
    }
}
