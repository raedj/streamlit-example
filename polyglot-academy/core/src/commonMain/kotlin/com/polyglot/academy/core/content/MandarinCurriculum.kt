package com.polyglot.academy.core.content

import com.polyglot.academy.core.assessment.AssessmentItem
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CefrLevel.A1
import com.polyglot.academy.core.model.CefrLevel.A2
import com.polyglot.academy.core.model.CefrLevel.B1
import com.polyglot.academy.core.model.CefrLevel.B2
import com.polyglot.academy.core.model.CefrLevel.PRE_A1
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.VocabularySource
import com.polyglot.academy.core.model.LearningTrack
import com.polyglot.academy.core.model.PartOfSpeech.ADJECTIVE
import com.polyglot.academy.core.model.PartOfSpeech.ADVERB
import com.polyglot.academy.core.model.PartOfSpeech.MEASURE_WORD
import com.polyglot.academy.core.model.PartOfSpeech.NOUN
import com.polyglot.academy.core.model.PartOfSpeech.NUMERAL
import com.polyglot.academy.core.model.PartOfSpeech.PARTICLE
import com.polyglot.academy.core.model.PartOfSpeech.PHRASE
import com.polyglot.academy.core.model.PartOfSpeech.PRONOUN
import com.polyglot.academy.core.model.PartOfSpeech.VERB
import com.polyglot.academy.core.model.ReadingPassage
import com.polyglot.academy.core.model.RolePlayScenario
import com.polyglot.academy.core.model.ScenarioBeat
import com.polyglot.academy.core.model.Skill

/**
 * Mandarin content. Terms are simplified characters; [com.polyglot.academy.core.model.VocabularyItem.traditional]
 * holds the traditional form where it differs; pronunciation is Hanyu Pinyin with tone marks.
 */
internal object MandarinCurriculum {
    private val L = LanguageCode.MANDARIN

    fun build(): LanguageCurriculum {
        val vb = VocabBuilder(L)
        with(vb) {
            // Greetings
            v("nihao", "你好", "hello", "مرحبًا", PHRASE, PRE_A1, "greetings", "nǐ hǎo",
                ex = "你好，我叫明明。", exEn = "Hello, my name is Mingming.", exAr = "مرحبًا، اسمي مينغ مينغ.")
            v("xiexie", "谢谢", "thank you", "شكرًا", PHRASE, PRE_A1, "greetings", "xièxie", trad = "謝謝",
                ex = "谢谢你！", exEn = "Thank you!", exAr = "شكرًا لك!")
            v("bukeqi", "不客气", "you're welcome", "عفوًا", PHRASE, PRE_A1, "greetings", "bú kèqi", trad = "不客氣")
            v("duibuqi", "对不起", "sorry", "آسف", PHRASE, PRE_A1, "greetings", "duìbuqǐ", trad = "對不起")
            v("zaijian", "再见", "goodbye", "مع السلامة", PHRASE, PRE_A1, "greetings", "zàijiàn", trad = "再見")
            v("jiao", "叫", "to be called", "يُدعى / اسمه", VERB, A1, "greetings", "jiào",
                ex = "你叫什么名字？", exEn = "What is your name?", exAr = "ما اسمك؟")
            v("mingzi", "名字", "name", "اسم", NOUN, A1, "greetings", "míngzi")
            // Pronouns & people
            v("wo", "我", "I, me", "أنا", PRONOUN, PRE_A1, "people", "wǒ")
            v("ni", "你", "you", "أنت", PRONOUN, PRE_A1, "people", "nǐ")
            v("ta_m", "他", "he, him", "هو", PRONOUN, PRE_A1, "people", "tā")
            v("ta_f", "她", "she, her", "هي", PRONOUN, PRE_A1, "people", "tā")
            v("pengyou", "朋友", "friend", "صديق", NOUN, A1, "people", "péngyou")
            v("laoshi", "老师", "teacher", "معلّم", NOUN, A1, "people", "lǎoshī", trad = "老師")
            v("xuesheng", "学生", "student", "طالب", NOUN, A1, "people", "xuésheng", trad = "學生",
                ex = "我是学生。", exEn = "I am a student.", exAr = "أنا طالب.")
            v("mama", "妈妈", "mum, mother", "أم", NOUN, PRE_A1, "people", "māma", trad = "媽媽")
            v("baba", "爸爸", "dad, father", "أب", NOUN, PRE_A1, "people", "bàba")
            v("shi", "是", "to be (is/am/are)", "يكون (هو/هي)", VERB, PRE_A1, "people", "shì")
            v("ren", "人", "person", "شخص", NOUN, PRE_A1, "people", "rén")
            // Numbers
            v("yi", "一", "one", "واحد", NUMERAL, PRE_A1, "numbers", "yī")
            v("er", "二", "two", "اثنان", NUMERAL, PRE_A1, "numbers", "èr")
            v("san", "三", "three", "ثلاثة", NUMERAL, PRE_A1, "numbers", "sān")
            v("si", "四", "four", "أربعة", NUMERAL, PRE_A1, "numbers", "sì")
            v("wu", "五", "five", "خمسة", NUMERAL, PRE_A1, "numbers", "wǔ")
            v("shi_ten", "十", "ten", "عشرة", NUMERAL, PRE_A1, "numbers", "shí")
            v("ge", "个", "general measure word", "مُميِّز عام (كلمة عدّ)", MEASURE_WORD, A1, "numbers", "gè", trad = "個",
                ex = "三个人", exEn = "three people", exAr = "ثلاثة أشخاص")
            v("duoshao", "多少", "how many / how much", "كم", PRONOUN, A1, "numbers", "duōshao")
            // Food & drink
            v("cha", "茶", "tea", "شاي", NOUN, A1, "food", "chá")
            v("shui", "水", "water", "ماء", NOUN, A1, "food", "shuǐ")
            v("kafei", "咖啡", "coffee", "قهوة", NOUN, A1, "food", "kāfēi")
            v("mifan", "米饭", "cooked rice", "أرز مطبوخ", NOUN, A1, "food", "mǐfàn", trad = "米飯")
            v("he", "喝", "to drink", "يشرب", VERB, A1, "food", "hē",
                ex = "我喝茶。", exEn = "I drink tea.", exAr = "أنا أشرب الشاي.")
            v("chi", "吃", "to eat", "يأكل", VERB, A1, "food", "chī")
            v("yao", "要", "to want (to order)", "يريد", VERB, A1, "food", "yào",
                ex = "我要一杯咖啡。", exEn = "I want a cup of coffee.", exAr = "أريد كوبًا من القهوة.")
            v("bei", "杯", "cup (measure word)", "كوب (كلمة عدّ)", MEASURE_WORD, A1, "food", "bēi")
            v("hao_chi", "好吃", "tasty, delicious", "لذيذ", ADJECTIVE, A1, "food", "hǎochī")
            // Places & travel
            v("zhongguo", "中国", "China", "الصين", NOUN, A1, "places", "Zhōngguó", trad = "中國")
            v("nali", "哪里", "where", "أين", PRONOUN, A1, "places", "nǎlǐ", trad = "哪裡",
                ex = "你是哪里人？", exEn = "Where are you from?", exAr = "من أين أنت؟")
            v("qu", "去", "to go", "يذهب", VERB, A1, "places", "qù")
            v("zai", "在", "to be at / in", "في / يوجد في", VERB, A1, "places", "zài",
                ex = "我在北京。", exEn = "I am in Beijing.", exAr = "أنا في بكين.")
            v("shangdian", "商店", "shop", "متجر", NOUN, A1, "places", "shāngdiàn")
            v("huochezhan", "火车站", "train station", "محطة القطار", NOUN, A2, "places", "huǒchēzhàn", trad = "火車站")
            v("ditie", "地铁", "subway, metro", "مترو الأنفاق", NOUN, A2, "places", "dìtiě", trad = "地鐵")
            // Time & daily life
            v("jintian", "今天", "today", "اليوم", NOUN, A1, "time", "jīntiān")
            v("mingtian", "明天", "tomorrow", "غدًا", NOUN, A1, "time", "míngtiān")
            v("xianzai", "现在", "now", "الآن", NOUN, A1, "time", "xiànzài", trad = "現在")
            v("dian", "点", "o'clock", "الساعة (للوقت)", MEASURE_WORD, A1, "time", "diǎn", trad = "點",
                ex = "现在三点。", exEn = "It is three o'clock now.", exAr = "الساعة الآن الثالثة.")
            v("xuexi", "学习", "to study", "يدرس", VERB, A1, "time", "xuéxí", trad = "學習",
                ex = "我每天学习中文。", exEn = "I study Chinese every day.", exAr = "أدرس الصينية كل يوم.")
            v("gongzuo", "工作", "to work; job", "يعمل؛ عمل", VERB, A1, "time", "gōngzuò")
            // Particles & descriptions
            v("ma", "吗", "question particle (yes/no)", "أداة استفهام (نعم/لا)", PARTICLE, PRE_A1, "grammar", "ma", trad = "嗎")
            v("bu", "不", "not", "لا / ليس", ADVERB, PRE_A1, "grammar", "bù")
            v("hen", "很", "very", "جدًا", ADVERB, A1, "grammar", "hěn")
            v("hao", "好", "good", "جيد", ADJECTIVE, PRE_A1, "grammar", "hǎo")
            v("gui", "贵", "expensive", "غالٍ", ADJECTIVE, A2, "shopping", "guì", trad = "貴",
                ex = "这个太贵了。", exEn = "This one is too expensive.", exAr = "هذا غالٍ جدًا.")
            v("pianyi", "便宜", "cheap", "رخيص", ADJECTIVE, A2, "shopping", "piányi")
            v("yinwei", "因为", "because", "لأنّ", PHRASE, B1, "grammar", "yīnwèi", trad = "因為")
            v("suoyi", "所以", "so, therefore", "لذلك", PHRASE, B1, "grammar", "suǒyǐ")

            // Work (professional track)
            v("huiyi", "会议", "meeting", "الاجتماع", NOUN, A2, "business", "huìyì", trad = "會議",
                ex = "会议九点开始。", exEn = "The meeting starts at nine.", exAr = "يبدأ الاجتماع في التاسعة.", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("gongsi", "公司", "company", "الشركة", NOUN, A2, "business", "gōngsī", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("kehu", "客户", "client, customer", "العميل", NOUN, A2, "business", "kèhù", trad = "客戶", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("yusuan", "预算", "budget", "الميزانية", NOUN, B1, "business", "yùsuàn", trad = "預算", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("jiezhi_riqi", "截止日期", "deadline", "الموعد النهائي", NOUN, B1, "business", "jiézhǐ rìqī", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fangan", "方案", "proposal, plan", "المقترح، الخطة", NOUN, B1, "business", "fāng'àn", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fujian", "附件", "attachment", "المرفق", NOUN, B1, "business", "fùjiàn",
                ex = "请看附件。", exEn = "Please see the attachment.", exAr = "يرجى الاطلاع على المرفق.", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fasong", "发送", "to send", "يرسل", VERB, B1, "business", "fāsòng", trad = "發送", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("zunjing", "尊敬的…", "Dear… (formal letter opening)", "حضرة… المحترم", PHRASE, B1, "business", "zūnjìng de", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("qing", "请", "please (polite request)", "من فضلك (طلب مهذب)", VERB, A2, "business", "qǐng", trad = "請", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)

            // Next steps: completed actions and comparisons
            v("le", "了", "(marks a completed action or a change)", "(أداة تدل على اكتمال الفعل أو التغيّر)", PARTICLE, B1, "aspect", "le",
                ex = "我买了一本书。", exEn = "I bought a book.", exAr = "اشتريتُ كتابًا.")
            v("mai", "买", "to buy", "يشتري", VERB, A1, "aspect", "mǎi", trad = "買")
            v("yijing", "已经", "already", "بالفعل، سبق أن", ADVERB, B1, "aspect", "yǐjīng", trad = "已經")
            v("zuotian", "昨天", "yesterday", "أمس", NOUN, A1, "aspect", "zuótiān")
            v("kan", "看", "to look; to read; to watch", "ينظر، يقرأ، يشاهد", VERB, A1, "aspect", "kàn")
            v("shu", "书", "book", "كتاب", NOUN, A1, "aspect", "shū", trad = "書")
            v("dianying", "电影", "film, movie", "فيلم", NOUN, A2, "aspect", "diànyǐng", trad = "電影")
            v("meiyou", "没有", "did not; have not (negates completed actions)", "لم (نفي الفعل المكتمل)", ADVERB, A2, "aspect", "méiyǒu", trad = "沒有")
            v("bi", "比", "than (in comparisons)", "من (في المقارنة)", PARTICLE, B1, "compare", "bǐ",
                ex = "我比他高。", exEn = "I am taller than him.", exAr = "أنا أطول منه.")
            v("geng", "更", "even more", "أكثر (للمقارنة)", ADVERB, B1, "compare", "gèng")
            v("gao", "高", "tall, high", "طويل، مرتفع", ADJECTIVE, A1, "compare", "gāo")
            v("da", "大", "big; older (in age)", "كبير؛ أكبر سنًا", ADJECTIVE, A1, "compare", "dà")
            v("xiao", "小", "small; younger", "صغير؛ أصغر سنًا", ADJECTIVE, A1, "compare", "xiǎo")
            v("yiyang", "一样", "the same", "مثل، نفس الشيء", ADJECTIVE, B1, "compare", "yíyàng", trad = "一樣")
            v("leng", "冷", "cold", "بارد", ADJECTIVE, A1, "compare", "lěng")
            v("re", "热", "hot", "حار", ADJECTIVE, A1, "compare", "rè", trad = "熱")
        }
        val vocabulary = vb.items
        val f = LessonFactory(L, vocabulary)

        val lessons = listOf(
            f.lesson("tones_intro", PRE_A1, "sounds", g("The four tones", "النغمات الأربع"),
                g("Hear and tell apart the four tones plus the neutral tone.", "استمع إلى النغمات الأربع والنغمة المحايدة وميّز بينها."),
                Skill.PRONUNCIATION, listOf("ma", "wo", "ni", "hao", "shi", "bu"),
                extra = listOf(
                    Exercise.ToneChoice("zh_x_tone_ma1", Skill.PRONUNCIATION, Instr.tone, "ma", 1, "妈", g("mum (first character of 妈妈)", "أم (الحرف الأول من 妈妈)")),
                    Exercise.ToneChoice("zh_x_tone_ma2", Skill.PRONUNCIATION, Instr.tone, "ma", 2, "麻", g("hemp; numb", "قنّب؛ خَدَر")),
                    Exercise.ToneChoice("zh_x_tone_ma3", Skill.PRONUNCIATION, Instr.tone, "ma", 3, "马", g("horse", "حصان")),
                    Exercise.ToneChoice("zh_x_tone_ma4", Skill.PRONUNCIATION, Instr.tone, "ma", 4, "骂", g("to scold", "يوبّخ")),
                    choice("tone_sandhi", L, "你好 is written nǐ hǎo. How is 你 actually pronounced?", listOf("ní (2nd tone)", "nǐ (3rd tone)", "nì (4th tone)"), 0, Skill.PRONUNCIATION,
                        g("Two 3rd tones in a row: the first becomes a 2nd tone.", "عند تتابع نغمتين ثالثتين تتحول الأولى إلى نغمة ثانية.")),
                ), minutes = 7),
            f.lesson("greetings", PRE_A1, "sounds", g("Greetings", "التحيات"), g("Say hello, thank you and goodbye.", "قل مرحبًا وشكرًا ومع السلامة."),
                Skill.SPEAKING, listOf("nihao", "xiexie", "bukeqi", "duibuqi", "zaijian"),
                extra = listOf(choice("gr1", L, "Someone says 谢谢. You reply…", listOf("不客气", "对不起", "再见"), 0, Skill.FUNCTIONAL)),
                prerequisites = listOf("tones_intro")),
            f.lesson("intro", A1, "people", g("Who are you?", "من أنت؟"), g("Use 是 and 叫 to introduce people.", "استخدم 是 و叫 للتعريف بالأشخاص."),
                Skill.GRAMMAR, listOf("wo", "ni", "ta_m", "ta_f", "shi", "jiao", "mingzi", "xuesheng", "laoshi"), "shi",
                extra = listOf(
                    order("in1", L, listOf("我", "是", "学生"), g("I am a student.", "أنا طالب."), separator = ""),
                    order("in2", L, listOf("你", "叫", "什么", "名字"), g("What is your name?", "ما اسمك؟"), separator = ""),
                    fill("in3", L, "她___老师。", listOf("是"), g("Use 是 to link two nouns.", "استخدم 是 للربط بين اسمين.")),
                ), prerequisites = listOf("greetings")),
            f.lesson("questions", A1, "people", g("Yes/no questions with 吗", "أسئلة نعم/لا مع 吗"), g("Turn statements into questions and say 'not'.", "حوّل الجمل إلى أسئلة واستخدم النفي."),
                Skill.GRAMMAR, listOf("ma", "bu", "hen", "hao", "pengyou", "ren"), "ma",
                extra = listOf(
                    fill("q1", L, "你是学生___？", listOf("吗")),
                    fill("q2", L, "我___是老师。", listOf("不"), g("不 goes before the verb.", "تأتي 不 قبل الفعل.")),
                    order("q3", L, listOf("你", "好", "吗"), g("How are you?", "كيف حالك؟"), separator = ""),
                ), prerequisites = listOf("intro")),
            f.lesson("numbers", A1, "daily", g("Numbers and measure words", "الأعداد وكلمات العدّ"), g("Count things with 个 and 杯.", "عُدّ الأشياء باستخدام 个 و杯."),
                Skill.VOCABULARY, listOf("yi", "er", "san", "si", "wu", "shi_ten", "ge", "duoshao"), "measure",
                extra = listOf(
                    fill("n1", L, "三___朋友", listOf("个")),
                    choice("n2", L, "How do you say 'two cups of tea'?", listOf("两杯茶", "二杯茶", "两个茶"), 0, explanation = g("Before measure words, 'two' is 两 (liǎng), not 二.", "قبل كلمات العدّ تُستخدم 两 للعدد اثنين وليس 二.")),
                ), prerequisites = listOf("questions")),
            f.lesson("food", A1, "daily", g("Ordering food and drinks", "طلب الطعام والمشروبات"), g("Use 要, 喝 and 吃 to order.", "استخدم 要 و喝 و吃 للطلب."),
                Skill.SPEAKING, listOf("cha", "shui", "kafei", "mifan", "he", "chi", "yao", "bei", "hao_chi"), "measure",
                extra = listOf(
                    order("f1", L, listOf("我", "要", "一", "杯", "咖啡"), g("I want a cup of coffee.", "أريد كوبًا من القهوة."), separator = ""),
                    dictation("f2", L, "我喝茶", listOf("我喝茶。")),
                ), prerequisites = listOf("numbers")),
            f.lesson("places", A2, "travel", g("Where is it?", "أين يقع؟"), g("Ask where places are and say where you're going.", "اسأل عن أماكن الأشياء وقل إلى أين تذهب."),
                Skill.LISTENING, listOf("nali", "qu", "zai", "shangdian", "huochezhan", "ditie", "zhongguo"), "zai",
                extra = listOf(
                    fill("p1", L, "火车站___哪里？", listOf("在")),
                    order("p2", L, listOf("我", "去", "商店"), g("I'm going to the shop.", "أنا ذاهب إلى المتجر."), separator = ""),
                ), prerequisites = listOf("food")),
            f.lesson("time_daily", A2, "travel", g("Time and daily life", "الوقت والحياة اليومية"), g("Say what time it is and what you do each day.", "قل كم الساعة وماذا تفعل كل يوم."),
                Skill.READING, listOf("jintian", "mingtian", "xianzai", "dian", "xuexi", "gongzuo", "gui", "pianyi"), "time_order",
                extra = listOf(
                    order("t1", L, listOf("我", "明天", "去", "北京"), g("I'm going to Beijing tomorrow.", "سأذهب إلى بكين غدًا."), separator = ""),
                    choice("t2", L, "Where does the time word usually go?", listOf("Before the verb", "At the very end", "After the object"), 0,
                        explanation = g("Time words come before the verb: 我明天去.", "تأتي كلمات الزمن قبل الفعل: 我明天去.")),
                ), prerequisites = listOf("places")),
            f.lesson("reasons", B1, "travel", g("Giving reasons", "ذكر الأسباب"), g("Link ideas with 因为…所以….", "اربط الأفكار باستخدام 因为…所以…."),
                Skill.WRITING, listOf("yinwei", "suoyi", "xuexi", "gongzuo", "hen"), "yinwei",
                extra = listOf(
                    fill("r1", L, "因为下雨，___我不去。", listOf("所以")),
                    writing("r2", L, g("Write two sentences explaining why you study Chinese (use 因为…所以…).", "اكتب جملتين تشرح فيهما سبب دراستك للصينية (استخدم 因为…所以…)."), 10, listOf("因为", "所以", "学习")),
                ), prerequisites = listOf("time_daily")),

            f.lesson("work", B1, "pro_business", g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), g("Budgets, deadlines and polite requests.", "الميزانيات والمواعيد النهائية والطلبات المهذبة."),
                Skill.WRITING, listOf("huiyi", "gongsi", "kehu", "yusuan", "jiezhi_riqi", "fangan", "fujian", "fasong", "zunjing", "qing"), "qing",
                extra = listOf(
                    fill("w1", L, "___问，会议几点开始？", listOf("请")),
                    order("w2", L, listOf("我们", "明天", "开会"), g("We have a meeting tomorrow.", "لدينا اجتماع غدًا."), separator = ""),
                    choice("w3", L, "Which is the right way to start a formal email?", listOf("尊敬的王先生：", "嗨，老王！", "再见！"), 0, Skill.FUNCTIONAL),
                    writing("w4", L, g("Write a short email to a client proposing a meeting (use 尊敬的 and 附件).", "اكتب رسالة قصيرة إلى عميل تقترح فيها اجتماعًا (استخدم 尊敬的 و附件)."), 15,
                        listOf("尊敬的", "会议", "附件")),
                )),
            f.lesson("completed", B1, "next", g("What you did", "ما فعلته"), g("Talk about completed actions with 了 and 没有.", "تحدّث عن الأفعال المكتملة باستخدام 了 و没有."),
                Skill.GRAMMAR, listOf("le", "mai", "yijing", "zuotian", "kan", "shu", "dianying", "meiyou"), "le",
                extra = listOf(
                    fill("le1", L, "我昨天看___一个电影。", listOf("了")),
                    choice("le2", L, "I didn't buy a book:", listOf("我没买书。", "我不买了书。", "我没买了书。"), 0),
                    order("le3", L, listOf("我", "已经", "吃", "了"), g("I have already eaten.", "لقد أكلتُ بالفعل."), separator = ""),
                ), prerequisites = listOf("reasons")),
            f.lesson("compare", B1, "next", g("Comparing things", "المقارنة"), g("Compare people and things with 比 and 一样.", "قارن بين الأشخاص والأشياء باستخدام 比 و一样."),
                Skill.SPEAKING, listOf("bi", "geng", "gao", "da", "xiao", "yiyang", "leng", "re"), "bi",
                extra = listOf(
                    choice("bi1", L, "I am taller than him:", listOf("我比他高。", "我比他很高。", "我高比他。"), 0),
                    fill("bi2", L, "今天___昨天冷。", listOf("比")),
                    order("bi3", L, listOf("北京", "比", "上海", "冷"), g("Beijing is colder than Shanghai.", "بكين أبرد من شنغهاي."), separator = ""),
                ), prerequisites = listOf("completed")),
        )

        val grammar = listOf(
            grammar("shi", L, A1, g("是 (shì) — 'to be' with nouns", "‏是 — فعل الكينونة مع الأسماء"),
                g("Use 是 to link a subject with a noun: 我是学生. Do not use 是 before adjectives — use 很 instead: 我很好.",
                    "يُستخدم 是 لربط المبتدأ باسم: 我是学生. لا تستخدم 是 قبل الصفات، بل استخدم 很: 我很好."),
                listOf(ex("我是老师。", "I am a teacher.", "أنا معلّم."), ex("她很好。", "She is well.", "هي بخير."), ex("我是好。", "Wrong: 是 before an adjective", "خطأ: 是 قبل صفة", wrong = true)),
                listOf(g("✗ 我是很忙 → ✓ 我很忙 (I'm busy).", "✗ 我是很忙 ← ✓ 我很忙 (أنا مشغول)."))),
            grammar("ma", L, A1, g("Questions with 吗 and negation with 不", "الاستفهام بـ 吗 والنفي بـ 不"),
                g("Add 吗 to the end of a statement to make a yes/no question. Put 不 before a verb or adjective to negate it.",
                    "أضف 吗 في نهاية الجملة لتصبح سؤال نعم/لا. وضع 不 قبل الفعل أو الصفة للنفي."),
                listOf(ex("你是学生吗？", "Are you a student?", "هل أنت طالب؟"), ex("我不是老师。", "I am not a teacher.", "لست معلّمًا.")),
                listOf(g("Don't add 吗 to questions that already have a question word: ✗ 你叫什么名字吗？", "لا تضف 吗 إلى سؤال فيه أداة استفهام: ✗ 你叫什么名字吗؟"))),
            grammar("measure", L, A1, g("Measure words", "كلمات العدّ (المُميِّزات)"),
                g("Between a number and a noun you need a measure word: 三个人, 一杯茶. 个 is the most general one. 'Two' before a measure word is 两.",
                    "بين العدد والاسم تحتاج كلمة عدّ: 三个人 و一杯茶. و个 هي الأعمّ. ويُقال 两 للعدد اثنين قبل كلمة العدّ."),
                listOf(ex("一个朋友", "one friend", "صديق واحد"), ex("两杯咖啡", "two cups of coffee", "كوبان من القهوة"), ex("三人", "Missing measure word (in everyday speech)", "تنقصها كلمة العدّ (في الكلام اليومي)", wrong = true)),
                listOf(g("✗ 二个人 → ✓ 两个人.", "✗ 二个人 ← ✓ 两个人."))),
            grammar("zai", L, A2, g("在 for location", "‏在 للمكان"),
                g("Subject + 在 + place says where someone or something is: 我在家. Ask with 在哪里.",
                    "المبتدأ + 在 + المكان يحدد مكان الشخص أو الشيء: 我在家. واسأل بـ 在哪里."),
                listOf(ex("商店在哪里？", "Where is the shop?", "أين المتجر؟"), ex("我在火车站。", "I'm at the train station.", "أنا في محطة القطار.")),
                listOf(g("✗ 我是在家 is unnecessary in simple statements; say 我在家.", "‏✗ 我是在家 غير ضروري في الجمل البسيطة؛ قل 我在家."))),
            grammar("time_order", L, A2, g("Word order with time", "ترتيب الكلمات مع الزمن"),
                g("Time words come before the verb, usually right after the subject: 我今天工作. Mandarin verbs do not change for tense.",
                    "تأتي كلمات الزمن قبل الفعل، غالبًا بعد المبتدأ مباشرة: 我今天工作. ولا يتغير الفعل الصيني بحسب الزمن."),
                listOf(ex("我明天去北京。", "I'm going to Beijing tomorrow.", "سأذهب إلى بكين غدًا."), ex("我去北京明天。", "Time word in the wrong place", "كلمة الزمن في غير موضعها", wrong = true)),
                listOf(g("Putting the time at the end, as in English.", "وضع الزمن في آخر الجملة كما في الإنجليزية."))),
            grammar("yinwei", L, B1, g("因为…所以… (because… so…)", "‏因为…所以… (لأنّ… لذلك…)"),
                g("Mandarin often uses both 因为 (because) and 所以 (so) in the same sentence: 因为很贵，所以我不买.",
                    "تستخدم الصينية غالبًا 因为 و所以 معًا في الجملة نفسها: 因为很贵，所以我不买."),
                listOf(ex("因为我喜欢中国，所以我学习中文。", "Because I like China, I study Chinese.", "لأنني أحب الصين، أدرس الصينية.")),
                listOf(g("Using both is normal in Mandarin even though English uses only one.", "استخدام الاثنين معًا أمر طبيعي في الصينية رغم أن الإنجليزية تكتفي بواحدة."))),

            grammar("qing", L, A2, g("Polite requests: 请 and 能不能", "الطلب المهذب: 请 و能不能"),
                g("Put 请 (please) before the verb for polite requests: 请发送附件. Ask permission or a favour with 能不能 + verb: 您能不能明天发给我？ Use 您 instead of 你 with clients.",
                    "ضع 请 قبل الفعل للطلب المهذب: 请发送附件. واطلب إذنًا أو خدمة بـ 能不能 + الفعل: 您能不能明天发给我？ واستخدم 您 بدل 你 مع العملاء."),
                listOf(ex("请您看一下附件。", "Please take a look at the attachment.", "يرجى الاطلاع على المرفق."),
                    ex("您能不能下午开会？", "Could you meet in the afternoon?", "هل يمكنكم الاجتماع بعد الظهر؟"),
                    ex("发送请附件。", "请 goes before the verb", "يأتي 请 قبل الفعل", wrong = true)),
                listOf(g("Using 你 with a new client can sound too casual; 您 is safer.", "استخدام 你 مع عميل جديد قد يبدو غير رسمي، و您 أنسب."))),
            grammar("le", L, B1, g("了 for completed actions", "‏了 للفعل المكتمل"),
                g("Put 了 after a verb to show the action is completed: 我买了一本书 (I bought a book). To say it did not happen, use 没(有) before the verb and drop 了: 我没买书.",
                    "ضع 了 بعد الفعل للدلالة على اكتماله: 我买了一本书 (اشتريتُ كتابًا). وللنفي استخدم 没(有) قبل الفعل واحذف 了: 我没买书."),
                listOf(ex("我昨天看了一个电影。", "I watched a film yesterday.", "شاهدتُ فيلمًا أمس."),
                    ex("他已经吃了。", "He has already eaten.", "لقد أكل بالفعل."),
                    ex("我没买了书。", "No 了 after 没", "لا يأتي 了 مع 没", wrong = true)),
                listOf(g("Using 不 for the past: ✗ 我昨天不去 → ✓ 我昨天没去.", "استخدام 不 للماضي: ✗ 我昨天不去 ← ✓ 我昨天没去."))),
            grammar("bi", L, B1, g("Comparisons with 比", "المقارنة بـ 比"),
                g("A 比 B + adjective: 我比他高 (I am taller than him). Don't put 很 before the adjective; use 更 (even more) or add an amount: 今天比昨天更冷, 他比我大两岁. For 'the same', use A 跟 B 一样 + adjective.",
                    "‏A 比 B + صفة: 我比他高 (أنا أطول منه). لا تضع 很 قبل الصفة، بل استخدم 更 أو أضف مقدارًا: 今天比昨天更冷، 他比我大两岁. وللمساواة استخدم A 跟 B 一样 + صفة."),
                listOf(ex("北京比上海冷。", "Beijing is colder than Shanghai.", "بكين أبرد من شنغهاي."),
                    ex("我跟我哥哥一样高。", "I am as tall as my older brother.", "أنا بطول أخي الأكبر."),
                    ex("我比他很高。", "No 很 in a 比 sentence", "لا يأتي 很 في جملة 比", wrong = true)),
                listOf(g("✗ 我高比他 → ✓ 我比他高 (比 B comes before the adjective).", "✗ 我高比他 ← ✓ 我比他高 (يأتي 比 B قبل الصفة)."))),
        )

        val units = listOf(
            CourseUnit("zh_u_sounds", L, PRE_A1, g("Sounds and greetings", "الأصوات والتحيات"), g("Tones, pinyin and first words", "النغمات والبينيين والكلمات الأولى"), listOf("zh_l_tones_intro", "zh_l_greetings")),
            CourseUnit("zh_u_people", L, A1, g("People", "الأشخاص"), g("Introductions and questions", "التعارف والأسئلة"), listOf("zh_l_intro", "zh_l_questions")),
            CourseUnit("zh_u_daily", L, A1, g("Numbers and food", "الأعداد والطعام"), g("Counting and ordering", "العدّ والطلب"), listOf("zh_l_numbers", "zh_l_food")),
            CourseUnit("zh_u_travel", L, A2, g("Getting around", "التنقّل"), g("Places, time and reasons", "الأماكن والوقت والأسباب"), listOf("zh_l_places", "zh_l_time_daily", "zh_l_reasons")),

            CourseUnit("zh_u_pro_business", L, B1, g("Chinese for work", "الصينية للعمل"), g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), listOf("zh_l_work"), trackId = "business"),
            CourseUnit("zh_u_next", L, B1, g("Next steps", "الخطوة التالية"), g("Completed actions and comparisons", "الأفعال المكتملة والمقارنة"), listOf("zh_l_completed", "zh_l_compare")),
        )

        val readings = listOf(
            ReadingPassage(
                "zh_r_mingming", L, A1, "我的朋友明明", g("My friend Mingming", "صديقي مينغ مينغ"),
                listOf("我有一个朋友。", "他叫明明。", "他是学生。", "他很喜欢喝茶。", "明天我们去商店。"),
                listOf(g("I have a friend.", "لديّ صديق."), g("His name is Mingming.", "اسمه مينغ مينغ."), g("He is a student.", "هو طالب."),
                    g("He really likes drinking tea.", "هو يحب شرب الشاي كثيرًا."), g("Tomorrow we are going to the shop.", "غدًا سنذهب إلى المتجر.")),
                listOf("zh_pengyou", "zh_jiao", "zh_xuesheng", "zh_he", "zh_cha", "zh_mingtian", "zh_shangdian"),
                listOf(
                    Exercise.MultipleChoice("zh_r_mingming_q1", Skill.READING, Instr.choose, "明明是谁？", listOf("老师", "学生", "妈妈"), 1,
                        promptGloss = g("Who is Mingming?", "من هو مينغ مينغ؟"), optionGlosses = listOf(g("a teacher", "معلّم"), g("a student", "طالب"), g("a mother", "أم"))),
                    Exercise.MultipleChoice("zh_r_mingming_q2", Skill.READING, Instr.choose, "明明喜欢喝什么？", listOf("咖啡", "水", "茶"), 2,
                        promptGloss = g("What does Mingming like to drink?", "ماذا يحب مينغ مينغ أن يشرب؟"), optionGlosses = listOf(g("coffee", "قهوة"), g("water", "ماء"), g("tea", "شاي"))),
                    Exercise.MultipleChoice("zh_r_mingming_q3", Skill.READING, Instr.choose, "他们明天去哪里？", listOf("商店", "火车站", "中国"), 0,
                        promptGloss = g("Where are they going tomorrow?", "إلى أين سيذهبون غدًا؟"), optionGlosses = listOf(g("the shop", "المتجر"), g("the train station", "محطة القطار"), g("China", "الصين"))),
                ),
            ),
        )

        val scenarios = listOf(
            RolePlayScenario(
                "zh_s_tea", L, A1, g("Ordering at a tea shop", "الطلب في محل شاي"), g("A small tea shop in Beijing.", "محل شاي صغير في بكين."),
                g("Order a drink and ask the price.", "اطلب مشروبًا واسأل عن السعر."),
                "你好！你要喝什么？(Nǐ hǎo! Nǐ yào hē shénme?)", g("Hello! What would you like to drink?", "مرحبًا! ماذا تريد أن تشرب؟"),
                listOf(
                    ScenarioBeat(listOf("茶", "咖啡", "水", "要"), g("Say what you want: 我要一杯茶。(Wǒ yào yì bēi chá.)", "قل ما تريد: 我要一杯茶。"),
                        "我要一杯茶。", "好的。还要别的吗？(Hǎo de. Hái yào bié de ma?)", g("OK. Anything else?", "حسنًا. هل تريد شيئًا آخر؟")),
                    ScenarioBeat(listOf("不要", "不", "谢谢", "米饭", "要"), g("Say 不要，谢谢。(Bú yào, xièxie.) — No, thanks.", "قل 不要，谢谢。أي: لا، شكرًا."),
                        "不要，谢谢。", "好。(Hǎo.)", g("Fine.", "حسنًا.")),
                    ScenarioBeat(listOf("多少", "钱"), g("Ask the price: 多少钱？(Duōshao qián?)", "اسأل عن السعر: 多少钱？"),
                        "多少钱？", "十五块。(Shíwǔ kuài.)", g("Fifteen yuan.", "خمسة عشر يوانًا.")),
                    ScenarioBeat(listOf("谢谢", "再见"), g("Thank them and say goodbye.", "اشكرهم وودّعهم."),
                        "谢谢，再见！", "不客气，再见！(Bú kèqi, zàijiàn!)", g("You're welcome, goodbye!", "عفوًا، مع السلامة!")),
                ),
                listOf("我要…", "多少钱？", "谢谢", "再见"),
            ),
            RolePlayScenario(
                "zh_s_meet", L, PRE_A1, g("Meeting a classmate", "التعرّف على زميل"), g("First day of a Chinese class.", "اليوم الأول في صف اللغة الصينية."),
                g("Say hello, give your name and say you are a student.", "ألقِ التحية وقل اسمك وأنك طالب."),
                "你好！我叫明明。你叫什么名字？(Nǐ hǎo! Wǒ jiào Míngming. Nǐ jiào shénme míngzi?)", g("Hi! I'm Mingming. What's your name?", "مرحبًا! أنا مينغ مينغ. ما اسمك؟"),
                listOf(
                    ScenarioBeat(listOf("我叫", "叫"), g("Say 我叫… (Wǒ jiào…) and your name.", "قل 我叫… ثم اسمك."),
                        "我叫萨拉。", "很高兴认识你！你是学生吗？(Hěn gāoxìng rènshi nǐ! Nǐ shì xuésheng ma?)", g("Nice to meet you! Are you a student?", "سعيد بمعرفتك! هل أنت طالب؟")),
                    ScenarioBeat(listOf("是", "学生", "不是"), g("Answer 是，我是学生。(Shì, wǒ shì xuésheng.)", "أجب: 是，我是学生。"),
                        "是，我是学生。", "我也是学生！(Wǒ yě shì xuésheng!)", g("I'm a student too!", "أنا طالب أيضًا!")),
                ),
                listOf("我叫…", "我是学生", "很高兴认识你"),
            ),

            RolePlayScenario(
                "zh_s_client", L, B1, g("Planning a project with a client", "التخطيط لمشروع مع عميل"), g("A video call with a client in Shanghai.", "مكالمة فيديو مع عميل في شنغهاي."),
                g("Agree on the budget and the deadline, then promise an email.", "اتفق على الميزانية والموعد النهائي، ثم عِد برسالة."),
                "您好！谢谢您的方案。我们先谈谈预算吧？(Nín hǎo! Xièxie nín de fāng'àn. Wǒmen xiān tántan yùsuàn ba?)",
                g("Hello! Thank you for the proposal. Shall we talk about the budget first?", "مرحبًا! شكرًا على المقترح. هل نتحدث عن الميزانية أولًا؟"),
                listOf(
                    ScenarioBeat(listOf("预算", "万", "元", "块", "美元"), g("State the budget: 预算是五万元。(Yùsuàn shì wǔ wàn yuán.)", "اذكر الميزانية: 预算是五万元。"),
                        "预算是五万元。", "好的。什么时候能完成？(Hǎo de. Shénme shíhou néng wánchéng?)", g("OK. When can it be finished?", "حسنًا. متى يمكن إنجازه؟")),
                    ScenarioBeat(listOf("星期", "月", "周", "以后"), g("Give a deadline: 六个星期以后。(Liù ge xīngqī yǐhòu.)", "حدّد موعدًا: 六个星期以后。"),
                        "六个星期以后能完成。", "没问题。(Méi wèntí.)", g("No problem.", "لا مشكلة.")),
                    ScenarioBeat(listOf("邮件", "发", "附件"), g("Say you'll send an email today: 我今天给您发邮件。", "قل إنك سترسل رسالة اليوم: 我今天给您发邮件。"),
                        "我今天给您发邮件。", "谢谢，再见！(Xièxie, zàijiàn!)", g("Thank you, goodbye!", "شكرًا، مع السلامة!")),
                ),
                listOf("预算是…", "…以后能完成", "我给您发邮件", "请看附件"),
                trackId = "business",
            ),
        )

        fun item(id: String, level: CefrLevel, skill: Skill, prompt: String, options: List<String>, correct: Int, promptEn: String, promptAr: String) =
            AssessmentItem("zh_a_$id", L, level, skill, Exercise.MultipleChoice("zh_a_$id", skill, Instr.choose, prompt, options, correct, promptGloss = g(promptEn, promptAr)))

        val assessment = listOf(
            item("p1", PRE_A1, Skill.VOCABULARY, "你好", listOf("hello", "goodbye", "thank you"), 0, "What does 你好 mean?", "ما معنى 你好؟"),
            item("p2", PRE_A1, Skill.PRONUNCIATION, "谢谢", listOf("xièxie", "zàijiàn", "nǐ hǎo"), 0, "Which pinyin matches 谢谢?", "أي بينيين يطابق 谢谢؟"),
            item("p3", PRE_A1, Skill.VOCABULARY, "三", listOf("2", "3", "10"), 1, "Which number is 三?", "أي عدد هو 三؟"),
            item("a1_1", A1, Skill.GRAMMAR, "你是学生___？", listOf("吗", "不", "很"), 0, "Complete the question.", "أكمل السؤال."),
            item("a1_2", A1, Skill.GRAMMAR, "我___好。", listOf("是", "很", "吗"), 1, "Complete: 'I am well.'", "أكمل: أنا بخير."),
            item("a1_3", A1, Skill.GRAMMAR, "三___人", listOf("个", "杯", "点"), 0, "Choose the measure word.", "اختر كلمة العدّ."),
            item("a2_1", A2, Skill.GRAMMAR, "火车站___哪里？", listOf("是", "在", "去"), 1, "Complete: 'Where is the train station?'", "أكمل: أين محطة القطار؟"),
            item("a2_2", A2, Skill.GRAMMAR, "Which sentence is correct?", listOf("我去北京明天。", "我明天去北京。", "明天北京我去。"), 1, "Which sentence is correct?", "أي جملة صحيحة؟"),
            item("a2_3", A2, Skill.VOCABULARY, "这个太___了，我不买。", listOf("便宜", "贵", "好吃"), 1, "'This is too ___, I won't buy it.'", "هذا ___ جدًا، لن أشتريه."),
            item("b1_1", B1, Skill.GRAMMAR, "因为下雨，___我们不去公园。", listOf("所以", "但是", "和"), 0, "Complete the sentence.", "أكمل الجملة."),
            item("b1_2", B1, Skill.GRAMMAR, "我___过长城。", listOf("去", "在", "是"), 0, "'I have been to the Great Wall.'", "لقد زرت سور الصين العظيم."),
            item("b1_3", B1, Skill.GRAMMAR, "他说中文说___很好。", listOf("的", "得", "地"), 1, "Choose the correct particle.", "اختر الأداة الصحيحة."),
            item("b2_1", B2, Skill.GRAMMAR, "我把作业___完了。", listOf("做", "是", "在"), 0, "Complete the 把 sentence.", "أكمل جملة 把."),
            item("b2_2", B2, Skill.VOCABULARY, "虽然很累，___他还是去上班了。", listOf("但是", "所以", "因为"), 0, "'Although tired, he still went to work.'", "رغم تعبه، ذهب إلى العمل."),
        )

        return LanguageCurriculum(
            language = L, vocabulary = vocabulary, grammar = grammar, units = units, lessons = lessons, readings = readings,
            scenarios = scenarios, assessment = assessment,
            tracks = listOf(LearningTrack("business", L, g("Chinese for work", "الصينية للعمل"), g("Meetings, formal emails, budgets and deadlines.", "الاجتماعات والرسائل الرسمية والميزانيات والمواعيد النهائية."), A2, null)),
            dictations = listOf(
                dictation("d1", L, "我是学生", listOf("我是学生。")),
                dictation("d2", L, "你好吗", listOf("你好吗？")),
                dictation("d3", L, "我要一杯咖啡", listOf("我要一杯咖啡。")),
            ),
            writingPrompts = listOf(
                writing("wp1", L, g("Introduce yourself in Chinese: name and whether you are a student.", "عرّف بنفسك بالصينية: اسمك وهل أنت طالب."), 5, listOf("我叫", "我是")),
            ),
        )
    }
}
