package com.polyglot.academy.ui.i18n

import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode

/**
 * Interface strings for one interface language. Placeholders are {0}, {1}…; plural entries hold
 * CLDR forms separated by '|': English "one|other", Arabic "zero|one|two|few|many|other".
 */
class Localizer(val language: LanguageCode) {
    private val table: Map<K, String> = when (language) {
        LanguageCode.ARABIC -> ArabicStrings.table
        else -> EnglishStrings.table
    }
    val isRtl: Boolean get() = language == LanguageCode.ARABIC

    operator fun get(key: K): String = table[key] ?: EnglishStrings.table[key] ?: key.name

    fun f(key: K, vararg args: Any): String = format(get(key), args.map { it.toString() })

    /** Plural-aware format; {0} in the chosen form is replaced by [n]. */
    fun plural(key: K, n: Int, vararg extra: Any): String {
        val forms = get(key).split('|')
        val form = when (language) {
            LanguageCode.ARABIC -> forms.getOrElse(arabicPluralIndex(n)) { forms.last() }
            else -> if (n == 1) forms.first() else forms.getOrElse(1) { forms.first() }
        }
        return format(form, listOf(n.toString()) + extra.map { it.toString() })
    }

    fun gloss(g: Gloss): String = g.forLanguage(language)

    companion object {
        fun format(template: String, args: List<String>): String {
            var out = template
            args.forEachIndexed { i, a -> out = out.replace("{$i}", a) }
            return out
        }

        /** CLDR Arabic categories: 0 zero, 1 one, 2 two, 3–10 few, 11–99 many, otherwise other. */
        fun arabicPluralIndex(n: Int): Int {
            val mod100 = n % 100
            return when {
                n == 0 -> 0
                n == 1 -> 1
                n == 2 -> 2
                mod100 in 3..10 -> 3
                mod100 in 11..99 -> 4
                else -> 5
            }
        }

        val placeholder = Regex("\\{(\\d+)\\}")
    }
}

