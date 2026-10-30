package com.polyglot.academy.core.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Injected time source so scheduling logic is deterministic in tests. */
fun interface TimeSource {
    fun nowMillis(): Long

    companion object {
        @OptIn(ExperimentalTime::class)
        val System = TimeSource { Clock.System.now().toEpochMilliseconds() }
    }
}

class FixedTimeSource(var current: Long) : TimeSource {
    override fun nowMillis(): Long = current
    fun advanceDays(days: Double) { current += (days * MILLIS_PER_DAY).toLong() }
    fun advanceMinutes(minutes: Long) { current += minutes * 60_000L }
}

const val MILLIS_PER_DAY = 86_400_000L
const val MILLIS_PER_MINUTE = 60_000L

@OptIn(ExperimentalTime::class)
fun Long.toLocalDate(zone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(zone).date

fun interface IdGenerator {
    fun newId(prefix: String): String

    companion object {
        val Random = IdGenerator { prefix ->
            val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
            prefix + "_" + (1..16).map { chars[kotlin.random.Random.nextInt(chars.length)] }.joinToString("")
        }
    }
}

class SequentialIdGenerator : IdGenerator {
    private var n = 0
    override fun newId(prefix: String): String = "${prefix}_${++n}"
}

/** Deterministic shuffle used by exercise players so a resumed lesson shows the same order. */
fun <T> List<T>.seededShuffle(seed: String): List<T> = shuffled(Random(seed.hashCode()))
