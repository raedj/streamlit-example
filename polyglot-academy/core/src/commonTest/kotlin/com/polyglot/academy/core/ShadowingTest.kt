package com.polyglot.academy.core

import com.polyglot.academy.core.speaking.Shadowing
import com.polyglot.academy.core.speaking.Shadowing.Pace
import kotlin.math.PI
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShadowingTest {
    private val rate = Shadowing.SAMPLE_RATE

    /** [silenceBefore] ms of silence, [toneMs] ms of a 220 Hz tone at [amp], then [silenceAfter] ms of silence. */
    private fun pcm(silenceBefore: Int, toneMs: Int, silenceAfter: Int, amp: Double = 0.3): ByteArray {
        val n = (silenceBefore + toneMs + silenceAfter) * rate / 1000
        val start = silenceBefore * rate / 1000
        val end = start + toneMs * rate / 1000
        val out = ByteArray(n * 2)
        for (i in 0 until n) {
            val v = if (i in start until end) (sin(2 * PI * 220 * i / rate) * amp * 32767).toInt() else 0
            out[2 * i] = v.toByte(); out[2 * i + 1] = (v shr 8).toByte()
        }
        return out
    }

    @Test fun pausesBeforeAndAfterAreNotCounted() {
        val ms = Shadowing.voicedMillis(pcm(700, 1500, 900))
        assertTrue(ms in 1460..1540, "$ms")
    }

    @Test fun silenceAndAClickAreNothingHeard() {
        assertEquals(Pace.NOTHING_HEARD, Shadowing.check(pcm(1000, 0, 1000), 1500).pace)
        assertEquals(Pace.NOTHING_HEARD, Shadowing.check(pcm(500, 100, 500), 1500).pace)
        // A very quiet recording is below the silence level.
        assertEquals(Pace.NOTHING_HEARD, Shadowing.check(pcm(200, 1500, 200, amp = 0.01), 1500).pace)
        assertEquals(Pace.NOTHING_HEARD, Shadowing.check(ByteArray(0), 1500).pace)
    }

    @Test fun paceIsComparedWithTheModel() {
        assertEquals(Pace.CLOSE, Shadowing.check(pcm(300, 1600, 300), 1500).pace)
        assertEquals(Pace.MUCH_SLOWER, Shadowing.check(pcm(300, 2600, 300), 1500).pace)
        assertEquals(Pace.MUCH_FASTER, Shadowing.check(pcm(300, 800, 300), 1500).pace)
    }

    @Test fun withoutAModelTimeOnlyHearingIsChecked() {
        val r = Shadowing.check(pcm(300, 3000, 300), null)
        assertEquals(Pace.CLOSE, r.pace)
        assertNull(r.ratio)
    }
}
