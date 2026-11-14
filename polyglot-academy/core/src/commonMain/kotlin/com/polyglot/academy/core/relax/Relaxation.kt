package com.polyglot.academy.core.relax

import com.polyglot.academy.core.model.Gloss

enum class BreathPhaseKind { INHALE, HOLD, EXHALE, HOLD_EMPTY }

data class BreathPhase(val kind: BreathPhaseKind, val seconds: Int)

data class BreathingPattern(
    val id: String,
    val title: Gloss,
    val description: Gloss,
    val phases: List<BreathPhase>,
    /** Purpose shown to the learner; never claims learning effects. */
    val purpose: Gloss,
) {
    val cycleSeconds: Int get() = phases.sumOf { it.seconds }
}

data class BreathingFrame(val phase: BreathPhase, val phaseIndex: Int, val secondsIntoPhase: Double, val cycle: Int, val finished: Boolean) {
    /** 0..1 progress within the current phase, for animating the guide circle. */
    val phaseProgress: Double get() = (secondsIntoPhase / phase.seconds).coerceIn(0.0, 1.0)
}

object Relaxation {
    val patterns = listOf(
        BreathingPattern(
            "box", Gloss("Box breathing", "التنفس المربّع"),
            Gloss("Breathe in, hold, out and hold for four seconds each.", "شهيق ثم حبس ثم زفير ثم حبس، أربع ثوانٍ لكل منها."),
            listOf(BreathPhase(BreathPhaseKind.INHALE, 4), BreathPhase(BreathPhaseKind.HOLD, 4), BreathPhase(BreathPhaseKind.EXHALE, 4), BreathPhase(BreathPhaseKind.HOLD_EMPTY, 4)),
            Gloss("A steady rhythm to settle before you practise.", "إيقاع ثابت لتهدأ قبل التمرين."),
        ),
        BreathingPattern(
            "calm", Gloss("Longer exhale", "زفير أطول"),
            Gloss("Breathe in for four seconds and out for six.", "شهيق لأربع ثوانٍ وزفير لست ثوانٍ."),
            listOf(BreathPhase(BreathPhaseKind.INHALE, 4), BreathPhase(BreathPhaseKind.EXHALE, 6)),
            Gloss("Many people find a slower exhale calming before speaking.", "يجد كثيرون أن الزفير الأبطأ مهدّئ قبل التحدث."),
        ),
        BreathingPattern(
            "focus", Gloss("Focus reset", "إعادة التركيز"),
            Gloss("Three gentle breaths: in for three, out for three.", "ثلاثة أنفاس هادئة: شهيق لثلاث وزفير لثلاث."),
            listOf(BreathPhase(BreathPhaseKind.INHALE, 3), BreathPhase(BreathPhaseKind.EXHALE, 3)),
            Gloss("A short pause between activities.", "استراحة قصيرة بين الأنشطة."),
        ),
    )

    /** Evidence boundary shown with every relaxation session. */
    val disclaimer = Gloss(
        "Relaxation can help you feel comfortable and focused. It does not teach vocabulary by itself — learning happens when you practise and recall.",
        "قد يساعدك الاسترخاء على الشعور بالراحة والتركيز، لكنه لا يعلّمك المفردات بحد ذاته؛ فالتعلّم يحدث عندما تتدرّب وتسترجع.",
    )

    fun frameAt(pattern: BreathingPattern, elapsedSeconds: Double, totalCycles: Int): BreathingFrame {
        val cycleLen = pattern.cycleSeconds.toDouble()
        val cycle = (elapsedSeconds / cycleLen).toInt()
        if (cycle >= totalCycles) return BreathingFrame(pattern.phases.last(), pattern.phases.lastIndex, pattern.phases.last().seconds.toDouble(), totalCycles - 1, true)
        var t = elapsedSeconds - cycle * cycleLen
        pattern.phases.forEachIndexed { i, p ->
            if (t < p.seconds) return BreathingFrame(p, i, t, cycle, false)
            t -= p.seconds
        }
        return BreathingFrame(pattern.phases.last(), pattern.phases.lastIndex, 0.0, cycle, false)
    }
}
