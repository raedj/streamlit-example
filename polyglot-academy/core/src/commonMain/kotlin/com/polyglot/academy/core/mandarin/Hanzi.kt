package com.polyglot.academy.core.mandarin

import com.polyglot.academy.core.model.Gloss

enum class ComponentRole { MEANING, SOUND, OTHER }

data class HanziComponent(val form: String, val meaning: Gloss, val role: ComponentRole)

/** Facts about one simplified character: stroke count and a learner-friendly breakdown into parts. */
data class HanziEntry(val character: Char, val strokes: Int, val components: List<HanziComponent>, val hint: Gloss?)

/**
 * Stroke counts and component breakdowns for the characters used in the built-in Mandarin course.
 * Authored for this app (not copied from a stroke-order database). Breakdowns are the common
 * teaching analysis, simplified for learners; "sound" parts hint at pronunciation only roughly.
 */
object HanziData {
    private fun g(en: String, ar: String) = Gloss(en, ar)

    private val parts: Map<String, Gloss> = mapOf(
        "亻" to g("person", "شخص"), "人" to g("person", "شخص"), "女" to g("woman", "امرأة"), "子" to g("child", "طفل"),
        "讠" to g("speech", "كلام"), "宀" to g("roof", "سقف"), "口" to g("mouth", "فم"), "日" to g("sun, day", "شمس، يوم"),
        "月" to g("moon, flesh", "قمر، لحم"), "木" to g("tree, wood", "شجرة، خشب"), "艹" to g("grass", "عشب"), "饣" to g("food", "طعام"),
        "钅" to g("metal", "معدن"), "土" to g("earth", "تراب"), "囗" to g("enclosure", "إحاطة"), "彳" to g("step", "خطوة"),
        "辶" to g("walking", "مشي"), "灬" to g("fire (dots)", "نار (نقاط)"), "马" to g("horse", "حصان"), "也" to g("also", "أيضًا"),
        "占" to g("to occupy", "يشغل"), "青" to g("blue-green", "أزرق مخضر"), "王" to g("king, jade", "ملك، يشم"), "见" to g("to see", "يرى"),
        "大" to g("big", "كبير"), "夕" to g("evening", "مساء"), "小" to g("small", "صغير"), "立" to g("to stand", "يقف"),
        "广" to g("shelter", "مأوى"), "父" to g("father", "أب"), "巴" to g("(sound part) bā", "(جزء صوتي) bā"), "又" to g("hand, again", "يد، مرة أخرى"),
        "寸" to g("inch", "بوصة"), "走" to g("to walk", "يمشي"), "己" to g("self", "الذات"), "白" to g("white", "أبيض"),
        "勺" to g("ladle", "مغرفة"), "不" to g("not", "لا"), "且" to g("(sound part) qiě", "(جزء صوتي) qiě"), "更" to g("more", "أكثر"),
        "艮" to g("(sound part) gèn", "(جزء صوتي) gèn"), "乍" to g("(sound part) zhà", "(جزء صوتي) zhà"), "反" to g("reverse", "عكس"),
        "乞" to g("to beg", "يتسوّل"), "曷" to g("(sound part) hé", "(جزء صوتي) hé"), "射" to g("to shoot", "يرمي"), "各" to g("each", "كل"),
        "那" to g("that", "ذلك"), "失" to g("to lose", "يفقد"), "关" to g("to close", "يغلق"), "云" to g("cloud", "سحابة"),
        "八" to g("eight", "ثمانية"), "厶" to g("private", "خاص"), "玉" to g("jade", "يشم"), "覀" to g("cover", "غطاء"),
        "冖" to g("cover", "غطاء"), "⺍" to g("(top dots)", "(نقاط علوية)"), "耂" to g("old (top)", "كبير السن (أعلى)"), "匕" to g("spoon", "ملعقة"),
        "尔" to g("you (old form)", "أنت (صيغة قديمة)"), "丩" to g("(sound part) jiū", "(جزء صوتي) jiū"), "一" to g("one", "واحد"),
        "丨" to g("line", "خط"), "丿" to g("stroke", "ضربة"),
    )

    private val roleOf = mapOf('*' to ComponentRole.SOUND, '~' to ComponentRole.OTHER)

    /** Components are space-separated; a leading * marks a sound part, ~ a structural part. */
    private fun h(c: Char, strokes: Int, components: String = "", hint: Gloss? = null): HanziEntry {
        val list = components.split(' ').filter { it.isNotBlank() }.map { raw ->
            val role = roleOf[raw[0]] ?: ComponentRole.MEANING
            val form = raw.trimStart('*', '~')
            HanziComponent(form, parts[form] ?: error("unknown component $form"), role)
        }
        return HanziEntry(c, strokes, list, hint)
    }

    val entries: Map<Char, HanziEntry> = listOf(
        h('一', 1), h('二', 2), h('三', 3), h('十', 2), h('人', 2), h('四', 5), h('五', 4),
        h('你', 7, "亻 *尔", g("A person (亻) you talk to: 'you'.", "شخص (亻) تخاطبه: 'أنت'.")),
        h('好', 6, "女 子", g("A woman (女) with a child (子): good.", "امرأة (女) مع طفل (子): جيد.")),
        h('谢', 12, "讠 *射", g("Speech (讠) on the left: words of thanks.", "الكلام (讠) على اليسار: كلمات الشكر.")),
        h('不', 4), h('客', 9, "宀 ~各"), h('气', 4),
        h('对', 5, "又 寸"), h('起', 10, "走 *己"), h('再', 6), h('见', 4),
        h('叫', 5, "口 *丩", g("A mouth (口) calling out.", "فم (口) ينادي.")),
        h('名', 6, "夕 口", g("In the evening (夕) you call out (口) your name.", "في المساء (夕) تنادي (口) باسمك.")),
        h('字', 6, "宀 子", g("A child (子) under a roof (宀) learning characters.", "طفل (子) تحت سقف (宀) يتعلّم الحروف.")),
        h('我', 7), h('他', 5, "亻 *也"), h('她', 6, "女 *也", g("Like 他, but with 女 (woman): 'she'.", "مثل 他 لكن مع 女 (امرأة): 'هي'.")),
        h('朋', 8, "月 月"), h('友', 4, "~一 ~丿 又"), h('老', 6, "耂 匕"), h('师', 6), h('学', 8, "⺍ 冖 子"), h('生', 5),
        h('妈', 6, "女 *马", g("Woman (女) + 马 (mǎ) for the sound: mā, 'mum'.", "امرأة (女) + 马 (mǎ) للصوت: mā، 'أم'.")),
        h('爸', 8, "父 *巴", g("Father (父) + 巴 (bā) for the sound: bà.", "أب (父) + 巴 (bā) للصوت: bà.")),
        h('是', 9, "日 ~一"), h('个', 3, "人 丨"), h('多', 6, "夕 夕"), h('少', 4, "小 丿"),
        h('茶', 9, "艹 人 木", g("Grass (艹), a person (人) and a tree (木): tea leaves.", "عشب (艹) وشخص (人) وشجرة (木): أوراق الشاي.")),
        h('水', 4), h('米', 6),
        h('饭', 7, "饣 *反", g("Food (饣) + 反 (fǎn) for the sound: fàn, 'rice, meal'.", "طعام (饣) + 反 (fǎn) للصوت: fàn، 'أرز، وجبة'.")),
        h('喝', 12, "口 *曷", g("Mouth (口) + 曷 (hé): hē, 'to drink'.", "فم (口) + 曷 (hé): hē، 'يشرب'.")),
        h('吃', 6, "口 *乞", g("Mouth (口) on the left: 'to eat'.", "فم (口) على اليسار: 'يأكل'.")),
        h('要', 9, "覀 女"), h('杯', 8, "木 *不", g("Wood (木): cups were once wooden.", "خشب (木): كانت الأكواب خشبية قديمًا.")),
        h('中', 4, "口 丨", g("A line (丨) through the middle of a box: 'middle'.", "خط (丨) يقسم المربع من الوسط: 'وسط'.")),
        h('国', 8, "囗 玉", g("Jade (玉) inside borders (囗): a country.", "يشم (玉) داخل حدود (囗): دولة.")),
        h('哪', 9, "口 *那"), h('里', 7), h('去', 5, "土 厶"), h('在', 6), h('商', 11),
        h('店', 8, "广 *占", g("A shelter (广) + 占 for the sound: diàn, 'shop'.", "مأوى (广) + 占 للصوت: diàn، 'متجر'.")),
        h('火', 4), h('车', 4),
        h('站', 10, "立 *占", g("Stand (立) + 占 (zhàn): zhàn, 'to stand; station'.", "يقف (立) + 占 (zhàn): zhàn، 'يقف؛ محطة'.")),
        h('地', 6, "土 *也", g("Earth (土) on the left: 'ground, place'.", "تراب (土) على اليسار: 'أرض، مكان'.")),
        h('铁', 10, "钅 *失", g("Metal (钅): iron, as in 地铁 (metro).", "معدن (钅): حديد، كما في 地铁 (مترو).")),
        h('今', 4), h('天', 4, "一 大", g("A line (一) above a big person (大): the sky, the day.", "خط (一) فوق شخص كبير (大): السماء، اليوم.")),
        h('明', 8, "日 月", g("Sun (日) and moon (月): bright, and 明天 'tomorrow'.", "شمس (日) وقمر (月): مشرق، و明天 'غدًا'.")),
        h('现', 8, "王 *见"), h('点', 9, "*占 灬"), h('习', 3), h('工', 3), h('作', 7, "亻 *乍"),
        h('吗', 6, "口 *马", g("Mouth (口) + 马 (mǎ): the question word ma.", "فم (口) + 马 (mǎ): أداة الاستفهام ma.")),
        h('很', 9, "彳 *艮"), h('贵', 9), h('便', 9, "亻 更"), h('宜', 8, "宀 *且"), h('因', 6, "囗 大"), h('为', 4), h('所', 8), h('以', 4),
        h('请', 10, "讠 *青", g("Speech (讠) + 青 (qīng): qǐng, 'please'.", "كلام (讠) + 青 (qīng): qǐng، 'من فضلك'.")),
        h('的', 8, "白 勺"), h('会', 6, "人 云"), h('公', 4, "八 厶"), h('发', 5), h('送', 9, "辶 关"),
    ).associateBy { it.character }

    fun of(c: Char): HanziEntry? = entries[c]
}
