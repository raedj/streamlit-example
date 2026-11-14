package com.polyglot.academy.core.arabic

import com.polyglot.academy.core.mandarin.InkPoint
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode

/** Where a letter's dots or small sign sit, relative to its main stroke. */
enum class MarkPosition { ABOVE, BELOW, INSIDE }

/**
 * One isolated letter in a simple Naskh hand, drawn for this app (not traced from a font): the body
 * strokes in writing order, then the dots or small sign. Coordinates are 0..1 in the writing box,
 * y downwards. The templates have not been reviewed by a teacher yet (see docs/CONTENT_SOURCES.md).
 */
data class LetterTemplate(
    val letter: Char,
    val name: Gloss,
    val body: List<List<InkPoint>>,
    /** The model's dots or small-sign strokes, drawn after the body. */
    val marks: List<List<InkPoint>> = emptyList(),
    val dots: Int = 0,
    /** A small sign instead of dots (Urdu ٹ ڈ ڑ, the mark inside Arabic ك). */
    val hasSign: Boolean = false,
    /** Another accepted place for the dots (the dot of ج is written inside the bowl or under it). */
    val alsoAt: MarkPosition? = null,
)

object ArabicLetters {
    /** A smooth stroke through the given points (Catmull–Rom), sampled for drawing and scoring. */
    private fun curve(vararg p: Pair<Double, Double>): List<InkPoint> {
        if (p.size == 2) return (0..12).map { t -> InkPoint((p[0].first + (p[1].first - p[0].first) * t / 12).toFloat(), (p[0].second + (p[1].second - p[0].second) * t / 12).toFloat()) }
        val pts = listOf(p.first()) + p.toList() + listOf(p.last())
        return buildList {
            for (i in 1 until pts.size - 2) for (s in 0 until 8) {
                val t = s / 8.0
                fun c(a: Double, b: Double, c: Double, d: Double) = 0.5 * (2 * b + (-a + c) * t + (2 * a - 5 * b + 4 * c - d) * t * t + (-a + 3 * b - 3 * c + d) * t * t * t)
                add(InkPoint(c(pts[i - 1].first, pts[i].first, pts[i + 1].first, pts[i + 2].first).toFloat(), c(pts[i - 1].second, pts[i].second, pts[i + 1].second, pts[i + 2].second).toFloat()))
            }
            add(InkPoint(p.last().first.toFloat(), p.last().second.toFloat()))
        }
    }

    private fun dot(x: Double, y: Double) = listOf(InkPoint(x.toFloat(), y.toFloat()), InkPoint((x + 0.01).toFloat(), (y + 0.005).toFloat()))
    private fun g(en: String, ar: String) = Gloss(en, ar)

    // Shared bodies.
    private val baBowl = curve(0.85 to 0.45, 0.82 to 0.58, 0.65 to 0.62, 0.35 to 0.62, 0.18 to 0.58, 0.15 to 0.45)
    private val nunBowl = curve(0.72 to 0.38, 0.75 to 0.55, 0.62 to 0.74, 0.4 to 0.76, 0.27 to 0.62, 0.28 to 0.42)
    private val jim = curve(0.3 to 0.32, 0.5 to 0.3, 0.68 to 0.3, 0.45 to 0.42, 0.3 to 0.55, 0.3 to 0.75, 0.5 to 0.86, 0.75 to 0.82)
    private val dal = curve(0.42 to 0.3, 0.55 to 0.45, 0.6 to 0.6, 0.35 to 0.62)
    private val ra = curve(0.62 to 0.4, 0.6 to 0.55, 0.5 to 0.7, 0.32 to 0.8)
    private val sin = curve(0.92 to 0.45, 0.88 to 0.56, 0.82 to 0.47, 0.77 to 0.56, 0.71 to 0.47, 0.66 to 0.56, 0.6 to 0.62, 0.55 to 0.72, 0.38 to 0.78, 0.2 to 0.72, 0.15 to 0.55)
    private val sad = curve(0.6 to 0.58, 0.7 to 0.45, 0.85 to 0.42, 0.9 to 0.52, 0.8 to 0.6, 0.6 to 0.6, 0.5 to 0.72, 0.35 to 0.78, 0.2 to 0.72, 0.15 to 0.55)
    private val taLoop = curve(0.3 to 0.62, 0.45 to 0.48, 0.65 to 0.45, 0.78 to 0.55, 0.7 to 0.62, 0.3 to 0.62)
    private val taStem = curve(0.42 to 0.15, 0.42 to 0.6)
    private val ain = curve(0.68 to 0.3, 0.55 to 0.24, 0.45 to 0.3, 0.5 to 0.4, 0.68 to 0.45, 0.48 to 0.5, 0.35 to 0.62, 0.38 to 0.8, 0.55 to 0.87, 0.75 to 0.82)
    private val yaBody = curve(0.65 to 0.32, 0.5 to 0.42, 0.6 to 0.52, 0.78 to 0.58, 0.72 to 0.72, 0.48 to 0.8, 0.25 to 0.74, 0.18 to 0.6)
    private val keheh = curve(0.55 to 0.15, 0.7 to 0.35, 0.82 to 0.55, 0.6 to 0.62, 0.3 to 0.62, 0.18 to 0.5)
    // The small ط written over ٹ ڈ ڑ: a little loop and its stem.
    private fun smallTa(x: Double, y: Double) = listOf(
        curve(x - 0.06 to y + 0.05, x to y, x + 0.05 to y + 0.03, x - 0.06 to y + 0.05),
        curve(x - 0.03 to y - 0.07, x - 0.03 to y + 0.05),
    )

    private fun three(x: Double, y: Double, up: Boolean) = listOf(dot(x - 0.07, y), dot(x + 0.07, y), dot(x, if (up) y - 0.09 else y + 0.09))
    private fun two(x: Double, y: Double) = listOf(dot(x - 0.07, y), dot(x + 0.07, y))

    private val arabic: List<LetterTemplate> = listOf(
        LetterTemplate('ا', g("alif", "ألف"), listOf(curve(0.5 to 0.12, 0.5 to 0.85))),
        LetterTemplate('ب', g("baa", "باء"), listOf(baBowl), listOf(dot(0.5, 0.75)), dots = 1),
        LetterTemplate('ت', g("taa", "تاء"), listOf(baBowl), two(0.5, 0.42), dots = 2),
        LetterTemplate('ث', g("thaa", "ثاء"), listOf(baBowl), three(0.5, 0.42, up = true), dots = 3),
        LetterTemplate('ج', g("jiim", "جيم"), listOf(jim), listOf(dot(0.52, 0.6)), dots = 1, alsoAt = MarkPosition.BELOW),
        LetterTemplate('ح', g("haa", "حاء"), listOf(jim)),
        LetterTemplate('خ', g("khaa", "خاء"), listOf(jim), listOf(dot(0.5, 0.18)), dots = 1),
        LetterTemplate('د', g("daal", "دال"), listOf(dal)),
        LetterTemplate('ذ', g("dhaal", "ذال"), listOf(dal), listOf(dot(0.45, 0.16)), dots = 1),
        LetterTemplate('ر', g("raa", "راء"), listOf(ra)),
        LetterTemplate('ز', g("zaay", "زاي"), listOf(ra), listOf(dot(0.6, 0.25)), dots = 1),
        LetterTemplate('س', g("siin", "سين"), listOf(sin)),
        LetterTemplate('ش', g("shiin", "شين"), listOf(sin), three(0.77, 0.32, up = true), dots = 3),
        LetterTemplate('ص', g("saad", "صاد"), listOf(sad)),
        LetterTemplate('ض', g("daad", "ضاد"), listOf(sad), listOf(dot(0.75, 0.28)), dots = 1),
        LetterTemplate('ط', g("taa (heavy)", "طاء"), listOf(taLoop, taStem)),
        LetterTemplate('ظ', g("dhaa (heavy)", "ظاء"), listOf(taLoop, taStem), listOf(dot(0.62, 0.32)), dots = 1),
        LetterTemplate('ع', g("ayn", "عين"), listOf(ain)),
        LetterTemplate('غ', g("ghayn", "غين"), listOf(ain), listOf(dot(0.55, 0.12)), dots = 1),
        LetterTemplate('ف', g("faa", "فاء"), listOf(curve(0.78 to 0.5, 0.72 to 0.42, 0.8 to 0.36, 0.86 to 0.45, 0.8 to 0.56, 0.6 to 0.6, 0.35 to 0.62, 0.18 to 0.58, 0.15 to 0.45)),
            listOf(dot(0.8, 0.24)), dots = 1),
        LetterTemplate('ق', g("qaaf", "قاف"), listOf(curve(0.7 to 0.45, 0.64 to 0.38, 0.71 to 0.31, 0.77 to 0.4, 0.72 to 0.52, 0.7 to 0.68, 0.52 to 0.8, 0.33 to 0.76, 0.27 to 0.6, 0.3 to 0.48)),
            two(0.72, 0.18), dots = 2),
        LetterTemplate('ك', g("kaaf", "كاف"), listOf(curve(0.78 to 0.15, 0.78 to 0.4, 0.78 to 0.6, 0.5 to 0.62, 0.22 to 0.6, 0.18 to 0.48)),
            listOf(curve(0.51 to 0.38, 0.45 to 0.41, 0.51 to 0.44)), hasSign = true),
        LetterTemplate('ل', g("laam", "لام"), listOf(curve(0.72 to 0.12, 0.72 to 0.4, 0.7 to 0.62, 0.58 to 0.76, 0.38 to 0.78, 0.25 to 0.68, 0.24 to 0.55))),
        LetterTemplate('م', g("miim", "ميم"), listOf(curve(0.62 to 0.48, 0.52 to 0.4, 0.42 to 0.48, 0.5 to 0.56, 0.6 to 0.52, 0.58 to 0.6, 0.56 to 0.75, 0.55 to 0.9))),
        LetterTemplate('ن', g("nuun", "نون"), listOf(nunBowl), listOf(dot(0.5, 0.48)), dots = 1),
        LetterTemplate('ه', g("haa (round)", "هاء"), listOf(curve(0.5 to 0.35, 0.68 to 0.5, 0.6 to 0.68, 0.4 to 0.68, 0.32 to 0.5, 0.5 to 0.35))),
        LetterTemplate('و', g("waaw", "واو"), listOf(curve(0.62 to 0.5, 0.55 to 0.42, 0.62 to 0.35, 0.7 to 0.45, 0.65 to 0.6, 0.52 to 0.75, 0.32 to 0.82))),
        LetterTemplate('ي', g("yaa", "ياء"), listOf(yaBody), two(0.49, 0.92), dots = 2),
    )

    private val urduExtra: List<LetterTemplate> = listOf(
        LetterTemplate('پ', g("pe", "پے (باء بثلاث نقاط)"), listOf(baBowl), three(0.5, 0.74, up = false), dots = 3),
        LetterTemplate('ٹ', g("ṭe", "ٹے (تاء بطاء صغيرة)"), listOf(baBowl), smallTa(0.5, 0.33), hasSign = true),
        LetterTemplate('چ', g("che", "چے (جيم بثلاث نقاط)"), listOf(jim), three(0.52, 0.58, up = false), dots = 3, alsoAt = MarkPosition.BELOW),
        LetterTemplate('ڈ', g("ḍāl", "ڈال (دال بطاء صغيرة)"), listOf(dal), smallTa(0.48, 0.16), hasSign = true),
        LetterTemplate('ڑ', g("ṛe", "ڑے (راء بطاء صغيرة)"), listOf(ra), smallTa(0.6, 0.24), hasSign = true),
        LetterTemplate('ژ', g("zhe", "ژے (زاي بثلاث نقاط)"), listOf(ra), three(0.6, 0.27, up = true), dots = 3),
        LetterTemplate('ک', g("kāf", "کاف الأردية"), listOf(keheh)),
        LetterTemplate('گ', g("gāf", "گاف"), listOf(keheh, curve(0.66 to 0.04, 0.8 to 0.25))),
        LetterTemplate('ں', g("nūn ghunna", "نون غنّة (بلا نقطة)"), listOf(nunBowl)),
        LetterTemplate('ھ', g("do-chashmi he", "هاء بعينين"), listOf(curve(0.5 to 0.35, 0.72 to 0.5, 0.6 to 0.66, 0.4 to 0.66, 0.28 to 0.5, 0.42 to 0.4, 0.52 to 0.52, 0.6 to 0.45, 0.5 to 0.35))),
        LetterTemplate('ہ', g("gol he", "هاء مستديرة"), listOf(curve(0.5 to 0.32, 0.64 to 0.46, 0.62 to 0.62, 0.46 to 0.68, 0.35 to 0.58, 0.4 to 0.46, 0.55 to 0.44))),
        LetterTemplate('ی', g("choṭī ye", "ياء صغيرة (بلا نقاط)"), listOf(yaBody)),
        LetterTemplate('ے', g("baṛī ye", "ياء كبيرة"), listOf(curve(0.75 to 0.32, 0.6 to 0.45, 0.72 to 0.55, 0.5 to 0.62, 0.25 to 0.62, 0.1 to 0.55))),
    )

    private val byLetter: Map<Char, LetterTemplate> = (arabic + urduExtra).associateBy { it.letter }

    fun of(letter: Char): LetterTemplate? = byLetter[letter]

    /** The isolated letters a learner of [language] writes, in alphabetical order; empty for other languages. */
    fun alphabet(language: LanguageCode): List<LetterTemplate> = when (language) {
        LanguageCode.ARABIC -> arabic
        LanguageCode.URDU -> "ابپتٹثجچحخدڈذرڑزژسشصضطظعغفقکگلمنںوہھیے".map { byLetter.getValue(it) }
        else -> emptyList()
    }

    fun supports(language: LanguageCode) = language == LanguageCode.ARABIC || language == LanguageCode.URDU
}
