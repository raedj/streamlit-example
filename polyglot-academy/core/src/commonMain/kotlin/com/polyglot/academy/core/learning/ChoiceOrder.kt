package com.polyglot.academy.core.learning

import kotlin.random.Random

/**
 * The order in which a choice question shows its options. The content lists the right answer first in most
 * hand-written questions, so a learner who always tapped the first option passed the placement test at B1
 * (found on the emulator, 2026-11-07). The order is mixed per question id: it does not change when the screen
 * redraws or the question comes back, and the content itself, with its correct index, stays as written.
 */
object ChoiceOrder {
    /** Indices into the question's options, in display order. */
    fun of(questionId: String, optionCount: Int): List<Int> = (0 until optionCount).shuffled(Random(questionId.hashCode()))
}
