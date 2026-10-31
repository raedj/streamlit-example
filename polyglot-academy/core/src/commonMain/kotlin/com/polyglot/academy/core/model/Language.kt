package com.polyglot.academy.core.model

import kotlinx.serialization.Serializable

/** Writing direction of a script. */
enum class TextDirection { LTR, RTL }

/**
 * Languages known to the product. New languages are added here plus a [LanguageProfile]
 * and a curriculum module; no other code should branch on specific languages except
 * language-specific workflows (e.g. Mandarin tones), which check [LanguageProfile.features].
 */
@Serializable
enum class LanguageCode(val tag: String) {
    ARABIC("ar"),
    ENGLISH("en"),
    MANDARIN("zh"),
    SPANISH("es"),
    FRENCH("fr"),
    TURKISH("tr"),
    URDU("ur"),
    MALAY("ms");

    companion object {
        fun fromTag(tag: String): LanguageCode? = entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) }
    }
}

/** Optional per-language learning features that switch on dedicated workflows. */
enum class LanguageFeature {
    TRANSLITERATION,
    TONES,
    CHARACTERS,
    SIMPLIFIED_TRADITIONAL,
    GRAMMATICAL_GENDER,
    REGIONAL_VARIANTS,
    RIGHT_TO_LEFT_SCRIPT,
}

@Serializable
data class RegionalVariant(val id: String, val localeTag: String, val englishName: String, val arabicName: String)

/** Static configuration for a learning (and/or interface) language. */
data class LanguageProfile(
    val code: LanguageCode,
    val englishName: String,
    val arabicName: String,
    val nativeName: String,
    val direction: TextDirection,
    val features: Set<LanguageFeature>,
    val variants: List<RegionalVariant>,
    val defaultVariantId: String,
    /** Default text-to-speech rate where 1.0 is the engine default. */
    val defaultSpeechRate: Float,
    val supportedLevels: List<CefrLevel>,
    /** True when an interface translation (UI strings) exists for this language. */
    val hasInterfaceTranslation: Boolean,
) {
    fun displayName(interfaceLanguage: LanguageCode): String = when (interfaceLanguage) {
        LanguageCode.ARABIC -> arabicName
        else -> englishName
    }

    fun has(feature: LanguageFeature) = feature in features
    val defaultVariant: RegionalVariant get() = variants.first { it.id == defaultVariantId }
}

object Languages {
    private val allLevels = CefrLevel.entries.toList()

    val ARABIC = LanguageProfile(
        code = LanguageCode.ARABIC,
        englishName = "Arabic",
        arabicName = "العربية",
        nativeName = "العربية",
        direction = TextDirection.RTL,
        features = setOf(LanguageFeature.RIGHT_TO_LEFT_SCRIPT, LanguageFeature.TRANSLITERATION, LanguageFeature.GRAMMATICAL_GENDER),
        variants = listOf(RegionalVariant("ar-msa", "ar", "Modern Standard Arabic", "العربية الفصحى")),
        defaultVariantId = "ar-msa",
        defaultSpeechRate = 0.9f,
        supportedLevels = allLevels,
        hasInterfaceTranslation = true,
    )

    val ENGLISH = LanguageProfile(
        code = LanguageCode.ENGLISH,
        englishName = "English",
        arabicName = "الإنجليزية",
        nativeName = "English",
        direction = TextDirection.LTR,
        features = setOf(LanguageFeature.REGIONAL_VARIANTS),
        variants = listOf(
            RegionalVariant("en-us", "en-US", "American English", "الإنجليزية الأمريكية"),
            RegionalVariant("en-gb", "en-GB", "British English", "الإنجليزية البريطانية"),
            RegionalVariant("en-au", "en-AU", "Australian English", "الإنجليزية الأسترالية"),
        ),
        defaultVariantId = "en-us",
        defaultSpeechRate = 1.0f,
        supportedLevels = allLevels,
        hasInterfaceTranslation = true,
    )

    val MANDARIN = LanguageProfile(
        code = LanguageCode.MANDARIN,
        englishName = "Mandarin Chinese",
        arabicName = "الصينية الماندرين",
        nativeName = "中文（普通话）",
        direction = TextDirection.LTR,
        features = setOf(
            LanguageFeature.TRANSLITERATION,
            LanguageFeature.TONES,
            LanguageFeature.CHARACTERS,
            LanguageFeature.SIMPLIFIED_TRADITIONAL,
            LanguageFeature.REGIONAL_VARIANTS,
        ),
        variants = listOf(
            RegionalVariant("zh-cn", "zh-CN", "Mainland (Simplified)", "البر الرئيسي (مبسطة)"),
            RegionalVariant("zh-tw", "zh-TW", "Taiwan (Traditional)", "تايوان (تقليدية)"),
        ),
        defaultVariantId = "zh-cn",
        defaultSpeechRate = 0.85f,
        supportedLevels = allLevels,
        hasInterfaceTranslation = false,
    )

    val SPANISH = LanguageProfile(
        code = LanguageCode.SPANISH,
        englishName = "Spanish",
        arabicName = "الإسبانية",
        nativeName = "Español",
        direction = TextDirection.LTR,
        features = setOf(LanguageFeature.GRAMMATICAL_GENDER, LanguageFeature.REGIONAL_VARIANTS),
        variants = listOf(
            RegionalVariant("es-es", "es-ES", "Spain", "إسبانيا"),
            RegionalVariant("es-mx", "es-MX", "Mexico", "المكسيك"),
            RegionalVariant("es-419", "es-419", "Latin America", "أمريكا اللاتينية"),
        ),
        defaultVariantId = "es-es",
        defaultSpeechRate = 0.95f,
        supportedLevels = allLevels,
        hasInterfaceTranslation = false,
    )

    val FRENCH = LanguageProfile(
        code = LanguageCode.FRENCH,
        englishName = "French",
        arabicName = "الفرنسية",
        nativeName = "Français",
        direction = TextDirection.LTR,
        features = setOf(LanguageFeature.GRAMMATICAL_GENDER, LanguageFeature.REGIONAL_VARIANTS),
        variants = listOf(
            RegionalVariant("fr-fr", "fr-FR", "France", "فرنسا"),
            RegionalVariant("fr-ca", "fr-CA", "Canada (Québec)", "كندا (كيبيك)"),
        ),
        defaultVariantId = "fr-fr",
        defaultSpeechRate = 0.95f,
        supportedLevels = allLevels,
        hasInterfaceTranslation = false,
    )

    val TURKISH = LanguageProfile(
        code = LanguageCode.TURKISH,
        englishName = "Turkish",
        arabicName = "التركية",
        nativeName = "Türkçe",
        direction = TextDirection.LTR,
        features = emptySet(),
        variants = listOf(RegionalVariant("tr-tr", "tr-TR", "Turkey", "تركيا")),
        defaultVariantId = "tr-tr",
        defaultSpeechRate = 0.95f,
        supportedLevels = allLevels,
        hasInterfaceTranslation = false,
    )

    val URDU = LanguageProfile(
        code = LanguageCode.URDU,
        englishName = "Urdu",
        arabicName = "الأردية",
        nativeName = "اردو",
        direction = TextDirection.RTL,
        features = setOf(LanguageFeature.RIGHT_TO_LEFT_SCRIPT, LanguageFeature.TRANSLITERATION, LanguageFeature.GRAMMATICAL_GENDER),
        variants = listOf(RegionalVariant("ur-pk", "ur-PK", "Pakistan", "باكستان")),
        defaultVariantId = "ur-pk",
        defaultSpeechRate = 0.9f,
        supportedLevels = allLevels,
        hasInterfaceTranslation = false,
    )

    val MALAY = LanguageProfile(
        code = LanguageCode.MALAY,
        englishName = "Malay",
        arabicName = "الملايوية",
        nativeName = "Bahasa Melayu",
        direction = TextDirection.LTR,
        features = emptySet(),
        variants = listOf(RegionalVariant("ms-my", "ms-MY", "Malaysia", "ماليزيا")),
        defaultVariantId = "ms-my",
        defaultSpeechRate = 0.95f,
        supportedLevels = allLevels,
        hasInterfaceTranslation = false,
    )

    val all: List<LanguageProfile> = listOf(ARABIC, ENGLISH, MANDARIN, SPANISH, FRENCH, TURKISH, URDU, MALAY)

    fun of(code: LanguageCode): LanguageProfile = all.first { it.code == code }

    /** Languages with a full interface translation. */
    val interfaceLanguages: List<LanguageProfile> get() = all.filter { it.hasInterfaceTranslation }
}
