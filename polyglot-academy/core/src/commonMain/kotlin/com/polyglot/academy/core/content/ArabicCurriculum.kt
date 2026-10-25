package com.polyglot.academy.core.content

import com.polyglot.academy.core.assessment.AssessmentItem
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CefrLevel.A1
import com.polyglot.academy.core.model.CefrLevel.A2
import com.polyglot.academy.core.model.CefrLevel.B1
import com.polyglot.academy.core.model.CefrLevel.PRE_A1
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gender.FEMININE
import com.polyglot.academy.core.model.Gender.MASCULINE
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.VocabularySource
import com.polyglot.academy.core.model.LearningTrack
import com.polyglot.academy.core.model.PartOfSpeech.ADJECTIVE
import com.polyglot.academy.core.model.PartOfSpeech.NOUN
import com.polyglot.academy.core.model.PartOfSpeech.PHRASE
import com.polyglot.academy.core.model.PartOfSpeech.PRONOUN
import com.polyglot.academy.core.model.PartOfSpeech.VERB
import com.polyglot.academy.core.model.ReadingPassage
import com.polyglot.academy.core.model.RolePlayScenario
import com.polyglot.academy.core.model.ScenarioBeat
import com.polyglot.academy.core.model.Skill

/**
 * Modern Standard Arabic for learners whose explanation language is English. The Arabic gloss
 * field holds a short Arabic definition so the same content can be shown to Arabic-interface users.
 */
internal object ArabicCurriculum {
    private val L = LanguageCode.ARABIC

    fun build(): LanguageCurriculum {
        val vb = VocabBuilder(L)
        with(vb) {
            v("marhaban", "مرحبًا", "hello", "تحية", PHRASE, PRE_A1, "greetings", translit = "marḥaban",
                ex = "مرحبًا، اسمي سارة.", exEn = "Hello, my name is Sara.", exAr = "مرحبًا، اسمي سارة.")
            v("salam", "السلام عليكم", "peace be upon you (common greeting)", "تحية الإسلام", PHRASE, PRE_A1, "greetings", translit = "as-salāmu ʿalaykum")
            v("shukran", "شكرًا", "thank you", "كلمة شكر", PHRASE, PRE_A1, "greetings", translit = "shukran")
            v("afwan", "عفوًا", "you're welcome; excuse me", "ردّ على الشكر", PHRASE, PRE_A1, "greetings", translit = "ʿafwan")
            v("maa_salama", "مع السلامة", "goodbye", "تحية الوداع", PHRASE, PRE_A1, "greetings", translit = "maʿa s-salāma")
            v("ismi", "اسمي", "my name (is)", "اسمي", PHRASE, PRE_A1, "greetings", translit = "ismī",
                ex = "اسمي عمر.", exEn = "My name is Omar.", exAr = "اسمي عمر.")
            v("kayfa_haluk", "كيف حالك؟", "how are you?", "سؤال عن الحال", PHRASE, A1, "greetings", translit = "kayfa ḥāluk(i)")
            v("ana", "أنا", "I", "ضمير المتكلم", PRONOUN, PRE_A1, "people", translit = "anā")
            v("anta", "أنتَ", "you (masculine)", "ضمير المخاطب", PRONOUN, PRE_A1, "people", translit = "anta")
            v("anti", "أنتِ", "you (feminine)", "ضمير المخاطبة", PRONOUN, PRE_A1, "people", translit = "anti")
            v("huwa", "هو", "he", "ضمير الغائب", PRONOUN, PRE_A1, "people", translit = "huwa")
            v("hiya", "هي", "she", "ضمير الغائبة", PRONOUN, PRE_A1, "people", translit = "hiya")
            v("talib", "طالب", "student (m.)", "طالب", NOUN, A1, "people", translit = "ṭālib", gender = MASCULINE,
                ex = "أنا طالب.", exEn = "I am a student.", exAr = "أنا طالب.")
            v("taliba", "طالبة", "student (f.)", "طالبة", NOUN, A1, "people", translit = "ṭāliba", gender = FEMININE)
            v("mudarris", "مدرّس", "teacher (m.)", "مدرّس", NOUN, A1, "people", translit = "mudarris", gender = MASCULINE)
            v("sadiq", "صديق", "friend (m.)", "صديق", NOUN, A1, "people", translit = "ṣadīq", gender = MASCULINE)
            v("usra", "أسرة", "family", "عائلة", NOUN, A1, "people", translit = "usra", gender = FEMININE)
            v("bayt", "بيت", "house, home", "منزل", NOUN, A1, "places", translit = "bayt", gender = MASCULINE)
            v("madina", "مدينة", "city", "مدينة", NOUN, A1, "places", translit = "madīna", gender = FEMININE)
            v("madrasa", "مدرسة", "school", "مدرسة", NOUN, A1, "places", translit = "madrasa", gender = FEMININE)
            v("ayna", "أين", "where", "أداة استفهام عن المكان", PRONOUN, A1, "places", translit = "ayna",
                ex = "أين المحطة؟", exEn = "Where is the station?", exAr = "أين المحطة؟")
            v("mahatta", "محطة", "station", "محطة", NOUN, A2, "places", translit = "maḥaṭṭa", gender = FEMININE)
            v("maa", "ماء", "water", "ماء", NOUN, A1, "food", translit = "māʾ", gender = MASCULINE)
            v("qahwa", "قهوة", "coffee", "قهوة", NOUN, A1, "food", translit = "qahwa", gender = FEMININE,
                ex = "أريد قهوة من فضلك.", exEn = "I want coffee, please.", exAr = "أريد قهوة من فضلك.")
            v("shay", "شاي", "tea", "شاي", NOUN, A1, "food", translit = "shāy", gender = MASCULINE)
            v("khubz", "خبز", "bread", "خبز", NOUN, A1, "food", translit = "khubz", gender = MASCULINE)
            v("urid", "أريد", "I want", "أرغب في", VERB, A1, "food", translit = "urīd")
            v("min_fadlak", "من فضلك", "please", "عبارة طلب مهذبة", PHRASE, A1, "food", translit = "min faḍlik")
            v("kabir", "كبير", "big", "كبير", ADJECTIVE, A1, "descriptions", translit = "kabīr")
            v("saghir", "صغير", "small", "صغير", ADJECTIVE, A1, "descriptions", translit = "ṣaghīr")
            v("jamil", "جميل", "beautiful", "جميل", ADJECTIVE, A1, "descriptions", translit = "jamīl")
            v("jadid", "جديد", "new", "جديد", ADJECTIVE, A1, "descriptions", translit = "jadīd")
            v("yadrus", "يدرس", "he studies", "يتعلّم", VERB, A2, "routine", translit = "yadrus",
                ex = "يدرس أخي في الجامعة.", exEn = "My brother studies at university.", exAr = "يدرس أخي في الجامعة.")
            v("yamal", "يعمل", "he works", "يشتغل", VERB, A2, "routine", translit = "yaʿmal")
            v("alyawm", "اليوم", "today", "هذا اليوم", NOUN, A1, "routine", translit = "al-yawm")
            v("ghadan", "غدًا", "tomorrow", "اليوم التالي", NOUN, A1, "routine", translit = "ghadan")

            // Home and weather
            v("ghurfa", "غرفة", "room", "حجرة في البيت", NOUN, A1, "home", translit = "ghurfa", gender = FEMININE,
                ex = "غرفتي صغيرة وجميلة.", exEn = "My room is small and beautiful.", exAr = "غرفتي صغيرة وجميلة.")
            v("matbakh", "مطبخ", "kitchen", "مكان الطبخ", NOUN, A1, "home", translit = "maṭbakh", gender = MASCULINE)
            v("uhibb", "أحبّ", "I love, I like", "أميل إلى الشيء", VERB, A1, "home", translit = "uḥibb",
                ex = "أحبّ الشاي في الصباح.", exEn = "I like tea in the morning.", exAr = "أحبّ الشاي في الصباح.")
            v("taqs", "الطقس", "the weather", "حالة الجو", NOUN, A1, "weather", translit = "aṭ-ṭaqs", gender = MASCULINE)
            v("harr", "حارّ", "hot", "شديد الحرارة", ADJECTIVE, A1, "weather", translit = "ḥārr")
            v("barid", "بارد", "cold", "قليل الحرارة", ADJECTIVE, A1, "weather", translit = "bārid",
                ex = "الطقس بارد اليوم.", exEn = "The weather is cold today.", exAr = "الطقس بارد اليوم.")
            v("matar", "مطر", "rain", "ماء ينزل من السحاب", NOUN, A1, "weather", translit = "maṭar", gender = MASCULINE)
            v("shita", "الشتاء", "winter", "فصل البرد", NOUN, A1, "weather", translit = "ash-shitāʾ", gender = MASCULINE)
            // Health
            v("tabib", "طبيب / طبيبة", "doctor (m./f.)", "من يعالج المرضى", NOUN, A1, "health", translit = "ṭabīb / ṭabība")
            v("yulimuni", "يؤلمني…", "my … hurts", "أشعر بألم في…", PHRASE, A2, "health", translit = "yuʾlimunī",
                ex = "يؤلمني رأسي.", exEn = "My head hurts.", exAr = "يؤلمني رأسي.")
            v("ras", "رأس", "head", "أعلى الجسم", NOUN, A1, "health", translit = "raʾs", gender = MASCULINE)
            v("marid", "مريض / مريضة", "ill, sick (m./f.)", "غير صحيح الجسم", ADJECTIVE, A1, "health", translit = "marīḍ / marīḍa")
            v("saydaliyya", "صيدلية", "pharmacy", "مكان بيع الدواء", NOUN, A1, "health", translit = "ṣaydaliyya", gender = FEMININE)
            v("dawa", "دواء", "medicine", "ما يُعالَج به المريض", NOUN, A2, "health", translit = "dawāʾ", gender = MASCULINE)
            v("mawid", "موعد", "appointment", "وقت متّفق عليه", NOUN, A2, "health", translit = "mawʿid", gender = MASCULINE,
                ex = "أريد موعدًا مع الطبيب.", exEn = "I want an appointment with the doctor.", exAr = "أريد موعدًا مع الطبيب.")
            // Work (professional track)
            v("ijtima", "اجتماع", "meeting", "لقاء للعمل", NOUN, A2, "business", translit = "ijtimāʿ", gender = MASCULINE,
                ex = "يبدأ الاجتماع في الساعة العاشرة.", exEn = "The meeting starts at ten o'clock.", exAr = "يبدأ الاجتماع في الساعة العاشرة.", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("sharika", "شركة", "company", "مؤسسة تجارية", NOUN, A2, "business", translit = "sharika", gender = FEMININE, tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("amil", "عميل / عميلة", "client (m./f.)", "من يتعامل مع الشركة", NOUN, A2, "business", translit = "ʿamīl / ʿamīla", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("mizaniyya", "ميزانية", "budget", "المال المخصّص لعمل", NOUN, B1, "business", translit = "mīzāniyya", gender = FEMININE, tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("mawid_nihai", "الموعد النهائي", "deadline", "آخر وقت للتسليم", NOUN, B1, "business", translit = "al-mawʿid an-nihāʾī", gender = MASCULINE, tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("iqtirah", "اقتراح", "proposal, suggestion", "رأي يُعرض للنقاش", NOUN, B1, "business", translit = "iqtirāḥ", gender = MASCULINE, tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("murfaq", "تجدون مرفقًا…", "please find attached… (formal email)", "عبارة لإرسال ملف", PHRASE, B1, "business", translit = "tajidūna murfaqan", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("tahiyya", "تحية طيبة وبعد،", "formal letter opening (≈ 'Dear…')", "افتتاحية الرسائل الرسمية", PHRASE, B1, "business", translit = "taḥiyya ṭayyiba wa-baʿd", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)

            // Next steps: past and future
            v("dhahabtu", "ذهبتُ", "I went", "فعل ماضٍ للمتكلم من ذهب", VERB, A2, "past", translit = "dhahabtu",
                ex = "ذهبتُ إلى السوق أمس.", exEn = "I went to the market yesterday.", exAr = "ذهبتُ إلى السوق أمس.")
            v("darastu", "درستُ", "I studied", "فعل ماضٍ للمتكلم من درس", VERB, A2, "past", translit = "darastu")
            v("katabtu", "كتبتُ", "I wrote", "فعل ماضٍ للمتكلم من كتب", VERB, A2, "past", translit = "katabtu")
            v("ams", "أمس", "yesterday", "اليوم الذي قبل اليوم", NOUN, A1, "past", translit = "ams")
            v("usbu_madi", "الأسبوع الماضي", "last week", "الأسبوع الذي مضى", PHRASE, A2, "past", translit = "al-usbūʿ al-māḍī")
            v("suq", "السوق", "the market", "مكان البيع والشراء", NOUN, A1, "past", translit = "as-sūq", gender = MASCULINE)
            v("risala", "رسالة", "letter, message", "كلام مكتوب يُرسَل", NOUN, A2, "past", translit = "risāla", gender = FEMININE)
            v("sawfa", "سـ / سوف", "will (future marker before a present verb)", "حرفا الاستقبال", PHRASE, B1, "future", translit = "sa- / sawfa",
                ex = "سأسافر الشهر القادم.", exEn = "I will travel next month.", exAr = "سأسافر الشهر القادم.")
            v("yusafir", "يسافر", "he travels", "ينتقل من بلد إلى آخر", VERB, A2, "future", translit = "yusāfir")
            v("shahr_qadim", "الشهر القادم", "next month", "الشهر الذي يأتي", PHRASE, A2, "future", translit = "ash-shahr al-qādim")
            v("utla", "عطلة", "holiday", "وقت الراحة من العمل", NOUN, A2, "future", translit = "ʿuṭla", gender = FEMININE)
            v("khutta", "خطة", "plan", "ما يُعَدّ لعمل في المستقبل", NOUN, B1, "future", translit = "khiṭṭa", gender = FEMININE)
            v("inshallah", "إن شاء الله", "God willing (common when speaking of plans)", "عبارة تقال عن المستقبل", PHRASE, A2, "future", translit = "in shāʾa llāh")
            v("lan", "لن", "will not (+ subjunctive verb)", "حرف نفي المستقبل", PHRASE, B1, "future", translit = "lan")
        }
        val vocabulary = vb.items
        val f = LessonFactory(L, vocabulary)
        val grammar = listOf(
            grammar("nominal", L, PRE_A1, g("Nominal sentences (no 'to be')", "الجملة الاسمية"),
                g("In present-tense Arabic, 'is/am/are' is not written: أنا طالب = I (am) a student. The sentence is subject + predicate.",
                    "في الجملة الاسمية لا يُذكر فعل الكينونة في المضارع: أنا طالب. وتتكون من مبتدأ وخبر."),
                listOf(ex("أنا طالبة.", "I am a student (f.).", "أنا طالبة."), ex("البيت كبير.", "The house is big.", "البيت كبير.")),
                listOf(g("Don't add a word for 'is' in the present.", "لا تُضف كلمة بمعنى 'يكون' في المضارع."))),
            grammar("gender", L, A1, g("Gender: the tāʾ marbūṭa (ة)", "التذكير والتأنيث: التاء المربوطة"),
                g("Most feminine nouns and adjectives end in ة: طالب → طالبة. Adjectives agree with the noun: مدينة جميلة, بيت جميل.",
                    "تنتهي معظم الأسماء والصفات المؤنثة بالتاء المربوطة: طالب ← طالبة، وتتبع الصفة الموصوف: مدينة جميلة، بيت جميل."),
                listOf(ex("مدينة كبيرة", "a big city", "مدينة كبيرة"), ex("بيت صغير", "a small house", "بيت صغير"), ex("مدينة كبير", "Adjective must be feminine", "يجب تأنيث الصفة", wrong = true)),
                listOf(g("Forgetting agreement: ✗ مدينة جميل → ✓ مدينة جميلة.", "عدم المطابقة: ✗ مدينة جميل ← ✓ مدينة جميلة."))),
            grammar("definite", L, A1, g("The definite article ال", "أداة التعريف (ال)"),
                g("ال makes a noun definite: بيت (a house) → البيت (the house). When the adjective is also definite it describes the noun (البيت الكبير = the big house); when it is indefinite it forms a sentence (البيت كبير = the house is big).",
                    "تعرّف «ال» الاسم: بيت ← البيت. إذا عُرّفت الصفة أيضًا كانت نعتًا (البيت الكبير)، وإذا نُكّرت كوّنت جملة (البيت كبير)."),
                listOf(ex("البيت الكبير", "the big house", "البيت الكبير"), ex("البيت كبير.", "The house is big.", "البيت كبير.")),
                listOf(g("Mixing up 'the big house' and 'the house is big'.", "الخلط بين النعت والخبر."))),

            grammar("idafa", L, A2, g("Possession: the iḍāfa", "الإضافة"),
                g("Put two nouns side by side to show possession. The first noun never takes ال; the second is definite: بيت الطالب (the student's house), غرفة المدرّس (the teacher's room).",
                    "تُضمّ كلمتان للدلالة على الملكية: الأولى (المضاف) لا تأخذ (ال)، والثانية (المضاف إليه) معرّفة: بيت الطالب، غرفة المدرّس."),
                listOf(ex("بيت الطالب", "the student's house", "بيت الطالب"), ex("موعد الطبيب", "the doctor's appointment", "موعد الطبيب"),
                    ex("البيت الطالب", "The first noun takes no ال", "المضاف لا يأخذ (ال)", wrong = true)),
                listOf(g("✗ الغرفة المدرّس → ✓ غرفة المدرّس.", "✗ الغرفة المدرّس ← ✓ غرفة المدرّس."))),
            grammar("past", L, B1, g("The past tense (al-māḍī)", "الفعل الماضي"),
                g("Past verbs show the person with an ending: ذهبتُ (I went), ذهبتَ (you, m.), ذهبتِ (you, f.), ذهبَ (he went), ذهبتْ (she went), ذهبنا (we went). Time words like أمس and الأسبوع الماضي often go with it.",
                    "يدلّ آخر الفعل الماضي على الفاعل: ذهبتُ، ذهبتَ، ذهبتِ، ذهبَ، ذهبتْ، ذهبنا. ويأتي غالبًا مع كلمات زمنية مثل أمس والأسبوع الماضي."),
                listOf(ex("درستُ العربية أمس.", "I studied Arabic yesterday.", "درستُ العربية أمس."),
                    ex("كتبتْ سارة رسالة.", "Sara wrote a letter.", "كتبتْ سارة رسالة."),
                    ex("هي كتبَ رسالة.", "With هي use كتبتْ", "مع هي نقول كتبتْ", wrong = true)),
                listOf(g("Forgetting the feminine ending: ✗ هي ذهبَ → ✓ هي ذهبتْ.", "نسيان تاء التأنيث: ✗ هي ذهبَ ← ✓ هي ذهبتْ."))),
            grammar("future", L, B1, g("The future with سـ and سوف", "المستقبل بـ (سـ) و(سوف)"),
                g("Put سـ (joined to the verb) or سوف (a separate word) before a present-tense verb: سأدرس / سوف أدرس (I will study). To say 'will not', use لن with the verb: لن أسافر.",
                    "ضع (سـ) متصلة بالفعل أو (سوف) منفصلة قبل الفعل المضارع: سأدرس / سوف أدرس. ولنفي المستقبل استخدم (لن) قبل الفعل: لن أسافر."),
                listOf(ex("سنسافر الشهر القادم.", "We will travel next month.", "سنسافر الشهر القادم."),
                    ex("لن أعمل غدًا.", "I won't work tomorrow.", "لن أعمل غدًا."),
                    ex("سوف ذهبتُ.", "سوف goes with a present verb: سوف أذهب", "تأتي سوف مع المضارع: سوف أذهب", wrong = true)),
                listOf(g("Don't combine them: ✗ سوف سأدرس → ✓ سوف أدرس or سأدرس.", "لا تجمع بينهما: ✗ سوف سأدرس ← ✓ سوف أدرس أو سأدرس."))),
        )
        val lessons = listOf(
            f.lesson("greetings", PRE_A1, "basics", g("Greetings", "التحيات"), g("Greet people and introduce yourself.", "التحية والتعريف بالنفس."),
                Skill.SPEAKING, listOf("marhaban", "salam", "shukran", "afwan", "maa_salama", "ismi", "kayfa_haluk"), "nominal",
                extra = listOf(choice("g1", L, "Someone says شكرًا. You reply…", listOf("عفوًا", "مرحبًا", "مع السلامة"), 0, Skill.FUNCTIONAL)), minutes = 6),
            f.lesson("people", PRE_A1, "basics", g("I, you, he, she", "الضمائر"), g("Use pronouns and nominal sentences.", "استخدام الضمائر والجملة الاسمية."),
                Skill.GRAMMAR, listOf("ana", "anta", "anti", "huwa", "hiya", "talib", "taliba", "mudarris"), "nominal",
                extra = listOf(
                    order("p1", L, listOf("أنا", "طالب"), g("I am a student.", "أنا طالب.")),
                    choice("p2", L, "She is a student: هي ___", listOf("طالبة", "طالب", "مدرّس"), 0),
                ), prerequisites = listOf("greetings")),
            f.lesson("cafe", A1, "everyday", g("At the café", "في المقهى"), g("Order drinks politely.", "اطلب المشروبات بأدب."),
                Skill.SPEAKING, listOf("maa", "qahwa", "shay", "khubz", "urid", "min_fadlak"), "definite",
                extra = listOf(order("c1", L, listOf("أريد", "قهوة", "من", "فضلك"), g("I want coffee, please.", "أريد قهوة من فضلك."))), prerequisites = listOf("people")),
            f.lesson("places", A1, "everyday", g("Places and descriptions", "الأماكن والأوصاف"), g("Describe places with adjectives that agree.", "صِف الأماكن بصفات مطابقة."),
                Skill.GRAMMAR, listOf("bayt", "madina", "madrasa", "ayna", "kabir", "saghir", "jamil", "jadid"), "gender",
                extra = listOf(
                    choice("pl1", L, "a beautiful city: مدينة ___", listOf("جميلة", "جميل", "الجميل"), 0),
                    choice("pl2", L, "The house is big:", listOf("البيت كبير", "البيت الكبير", "بيت الكبير"), 0),
                ), prerequisites = listOf("cafe")),
            f.lesson("routine", A2, "everyday", g("Work and study", "العمل والدراسة"), g("Talk about what people do.", "تحدّث عمّا يفعله الناس."),
                Skill.READING, listOf("yadrus", "yamal", "alyawm", "ghadan", "usra", "sadiq", "mahatta"), "gender",
                prerequisites = listOf("places")),

            f.lesson("home", A2, "home", g("Home and weather", "البيت والطقس"), g("Describe your home and the weather.", "صِف بيتك والطقس."),
                Skill.VOCABULARY, listOf("ghurfa", "matbakh", "uhibb", "taqs", "harr", "barid", "matar", "shita"), "idafa",
                extra = listOf(
                    choice("ho1", L, "In winter the weather is cold: في الشتاء الطقس ___", listOf("بارد", "حارّ", "مطر"), 0),
                    order("ho2", L, listOf("غرفة", "الطالب", "كبيرة"), g("The student's room is big.", "غرفة الطالب كبيرة.")),
                    choice("ho3", L, "the teacher's house:", listOf("بيت المدرّس", "البيت المدرّس", "بيت مدرّس ال"), 0),
                ), prerequisites = listOf("routine")),
            f.lesson("health", A2, "home", g("At the doctor's", "عند الطبيب"), g("Say what hurts and ask for an appointment.", "قل ما يؤلمك واطلب موعدًا."),
                Skill.SPEAKING, listOf("tabib", "yulimuni", "ras", "marid", "saydaliyya", "dawa", "mawid"), "idafa",
                extra = listOf(
                    choice("h1", L, "My head hurts:", listOf("يؤلمني رأسي", "رأسي طبيب", "أريد رأس"), 0),
                    order("h2", L, listOf("أريد", "موعدًا", "مع", "الطبيب"), g("I want an appointment with the doctor.", "أريد موعدًا مع الطبيب.")),
                    choice("h3", L, "Where do you buy medicine?", listOf("في الصيدلية", "في المطبخ", "في المدرسة"), 0, Skill.VOCABULARY),
                ), prerequisites = listOf("home")),
            f.lesson("work", B1, "pro_business", g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), g("Budgets, deadlines and formal openings.", "الميزانيات والمواعيد النهائية والافتتاحيات الرسمية."),
                Skill.WRITING, listOf("ijtima", "sharika", "amil", "mizaniyya", "mawid_nihai", "iqtirah", "murfaq", "tahiyya"), "idafa",
                extra = listOf(
                    choice("w1", L, "A formal email opening:", listOf("تحية طيبة وبعد،", "أهلًا يا صديقي!", "مع السلامة"), 0, Skill.FUNCTIONAL),
                    order("w2", L, listOf("تجدون", "مرفقًا", "الاقتراح"), g("Please find the proposal attached.", "تجدون مرفقًا الاقتراح.")),
                    choice("w3", L, "the company's budget:", listOf("ميزانية الشركة", "الميزانية الشركة", "ميزانية شركة ال"), 0),
                    writing("w4", L, g("Write a short email to a client proposing a meeting.", "اكتب رسالة قصيرة إلى عميل تقترح فيها اجتماعًا."), 15,
                        listOf("تحية طيبة وبعد", "اجتماع", "اقتراح")),
                )),
            f.lesson("past", B1, "next", g("What happened", "ماذا حدث"), g("Talk about the past with al-māḍī.", "تحدّث عن الماضي بالفعل الماضي."),
                Skill.GRAMMAR, listOf("dhahabtu", "darastu", "katabtu", "ams", "usbu_madi", "suq", "risala"), "past",
                extra = listOf(
                    choice("pa1", L, "I went to the market yesterday: ___ إلى السوق أمس.", listOf("ذهبتُ", "أذهب", "سأذهب"), 0),
                    choice("pa2", L, "She wrote a letter: هي ___ رسالة.", listOf("كتبتْ", "كتبَ", "يكتب"), 0),
                    order("pa3", L, listOf("درستُ", "العربية", "أمس"), g("I studied Arabic yesterday.", "درستُ العربية أمس.")),
                ), prerequisites = listOf("health")),
            f.lesson("future", B1, "next", g("Plans and the future", "الخطط والمستقبل"), g("Use سـ, سوف and لن for the future.", "استخدم سـ وسوف ولن للمستقبل."),
                Skill.SPEAKING, listOf("sawfa", "yusafir", "shahr_qadim", "utla", "khutta", "inshallah", "lan"), "future",
                extra = listOf(
                    choice("fu1", L, "I will travel next month: ___ الشهر القادم.", listOf("سأسافر", "سافرتُ", "سافرنا"), 0),
                    order("fu2", L, listOf("سوف", "أدرس", "غدًا"), g("I will study tomorrow.", "سوف أدرس غدًا.")),
                    choice("fu3", L, "I will not travel: ___ أسافر.", listOf("لن", "لم", "ما"), 0),
                    writing("fu4", L, g("Write two sentences about your plans (use سـ or سوف).", "اكتب جملتين عن خططك (استخدم سـ أو سوف)."), 6, listOf("سأ", "سوف", "غدًا")),
                ), prerequisites = listOf("past")),
        )
        val units = listOf(
            CourseUnit("ar_u_basics", L, PRE_A1, g("First steps", "الخطوات الأولى"), g("Greetings and pronouns", "التحيات والضمائر"), listOf("ar_l_greetings", "ar_l_people")),
            CourseUnit("ar_u_everyday", L, A1, g("Everyday Arabic", "العربية اليومية"), g("Café, places and routines", "المقهى والأماكن والروتين"), listOf("ar_l_cafe", "ar_l_places", "ar_l_routine")),

            CourseUnit("ar_u_home", L, A2, g("Home and health", "البيت والصحة"), g("Your home, the weather and the doctor", "بيتك والطقس والطبيب"), listOf("ar_l_home", "ar_l_health")),
            CourseUnit("ar_u_pro_business", L, B1, g("Arabic for work", "العربية للعمل"), g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), listOf("ar_l_work"), trackId = "business"),
            CourseUnit("ar_u_next", L, B1, g("Next steps", "الخطوة التالية"), g("The past and the future", "الماضي والمستقبل"), listOf("ar_l_past", "ar_l_future")),
        )
        val readings = listOf(
            ReadingPassage(
                "ar_r_omar", L, A1, "عمر وأسرته", g("Omar and his family", "عمر وأسرته"),
                listOf("اسمي عمر.", "أنا طالب في مدرسة جديدة.", "بيتنا صغير وجميل.", "أشرب الشاي مع أسرتي كل يوم."),
                listOf(g("My name is Omar.", "اسمي عمر."), g("I am a student at a new school.", "أنا طالب في مدرسة جديدة."),
                    g("Our house is small and beautiful.", "بيتنا صغير وجميل."), g("I drink tea with my family every day.", "أشرب الشاي مع أسرتي كل يوم.")),
                listOf("ar_ismi", "ar_talib", "ar_madrasa", "ar_jadid", "ar_bayt", "ar_saghir", "ar_jamil", "ar_shay", "ar_usra"),
                listOf(
                    Exercise.MultipleChoice("ar_r_omar_q1", Skill.READING, Instr.choose, "عمر…", listOf("مدرّس", "طالب", "طبيب"), 1,
                        promptGloss = g("Omar is…", "عمر…"), optionGlosses = listOf(g("a teacher", "مدرّس"), g("a student", "طالب"), g("a doctor", "طبيب"))),
                    Exercise.MultipleChoice("ar_r_omar_q2", Skill.READING, Instr.choose, "البيت…", listOf("كبير", "صغير وجميل", "جديد"), 1,
                        promptGloss = g("The house is…", "البيت…"), optionGlosses = listOf(g("big", "كبير"), g("small and beautiful", "صغير وجميل"), g("new", "جديد"))),
                ),
            ),
        )
        val scenarios = listOf(
            RolePlayScenario(
                "ar_s_cafe", L, A1, g("At a café", "في المقهى"), g("A café in Amman.", "مقهى في عمّان."),
                g("Greet the waiter and order a drink.", "حيِّ النادل واطلب مشروبًا."),
                "أهلًا وسهلًا! ماذا تريد؟ (ahlan wa sahlan! mādhā turīd?)", g("Welcome! What would you like?", "أهلًا وسهلًا! ماذا تريد؟"),
                listOf(
                    ScenarioBeat(listOf("قهوة", "شاي", "ماء", "أريد", "اريد"), g("Say: أريد قهوة من فضلك (urīd qahwa min faḍlik).", "قل: أريد قهوة من فضلك."),
                        "أريد قهوة من فضلك.", "تفضّل. هل تريد شيئًا آخر؟ (tafaḍḍal. hal turīd shayʾan ākhar?)", g("Here you are. Anything else?", "تفضّل. هل تريد شيئًا آخر؟")),
                    ScenarioBeat(listOf("لا", "شكرا", "شكرًا", "نعم", "خبز"), g("Say: لا، شكرًا (lā, shukran) — No, thank you.", "قل: لا، شكرًا."),
                        "لا، شكرًا.", "عفوًا! (ʿafwan!)", g("You're welcome!", "عفوًا!")),
                ),
                listOf("أريد…", "من فضلك", "شكرًا"),
            ),

            RolePlayScenario(
                "ar_s_doctor", L, A2, g("At the doctor's", "عند الطبيب"), g("A clinic in Cairo. Language practice only — not medical advice.", "عيادة في القاهرة. للتدرّب اللغوي فقط، وليس استشارة طبية."),
                g("Say what hurts and ask where the pharmacy is.", "قل ما يؤلمك واسأل أين الصيدلية."),
                "أهلًا، تفضّل بالجلوس. ممّ تشكو؟ (ahlan, tafaḍḍal bil-julūs. mimma tashkū?)", g("Hello, please sit down. What's the problem?", "أهلًا، تفضّل بالجلوس. ممّ تشكو؟"),
                listOf(
                    ScenarioBeat(listOf("يؤلمني", "رأسي", "رأس", "مريض", "مريضة", "ألم"), g("Say: يؤلمني رأسي (yuʾlimunī raʾsī) — my head hurts.", "قل: يؤلمني رأسي."),
                        "يؤلمني رأسي.", "منذ متى؟ (mundhu matā?)", g("Since when?", "منذ متى؟")),
                    ScenarioBeat(listOf("أمس", "امس", "منذ", "يوم", "يومين"), g("Say: منذ أمس (mundhu ams) — since yesterday.", "قل: منذ أمس."),
                        "منذ أمس.", "خذ هذا الدواء واسترح. (khudh hādhā d-dawāʾ wa-istariḥ.)", g("Take this medicine and rest.", "خذ هذا الدواء واسترح.")),
                    ScenarioBeat(listOf("أين", "اين", "صيدلية", "الصيدلية"), g("Ask: أين الصيدلية؟ (ayna ṣ-ṣaydaliyya?)", "اسأل: أين الصيدلية؟"),
                        "أين الصيدلية؟", "الصيدلية بجانب العيادة. (aṣ-ṣaydaliyya bi-jānib al-ʿiyāda.)", g("The pharmacy is next to the clinic.", "الصيدلية بجانب العيادة.")),
                ),
                listOf("يؤلمني…", "منذ أمس", "أين الصيدلية؟"),
            ),
            RolePlayScenario(
                "ar_s_meeting", L, B1, g("A meeting with a client", "اجتماع مع عميل"), g("An office in Dubai.", "مكتب في دبي."),
                g("Discuss the budget and the deadline, then promise a follow-up email.", "ناقش الميزانية والموعد النهائي، ثم وعد برسالة متابعة."),
                "أهلًا وسهلًا. شكرًا على الاقتراح. ما الميزانية؟ (mā l-mīzāniyya?)", g("Welcome. Thank you for the proposal. What is the budget?", "أهلًا وسهلًا. شكرًا على الاقتراح. ما الميزانية؟"),
                listOf(
                    ScenarioBeat(listOf("ميزانية", "الميزانية", "دولار", "درهم", "ألف", "الف"), g("State the budget: الميزانية خمسة آلاف دولار.", "اذكر الميزانية: الميزانية خمسة آلاف دولار."),
                        "الميزانية خمسة آلاف دولار.", "جيد. وما الموعد النهائي؟", g("Good. And what is the deadline?", "جيد. وما الموعد النهائي؟")),
                    ScenarioBeat(listOf("أسابيع", "اسابيع", "شهر", "الموعد", "أسبوع", "اسبوع"), g("Give a deadline: بعد ستة أسابيع (after six weeks).", "حدّد موعدًا: بعد ستة أسابيع."),
                        "نستطيع التسليم بعد ستة أسابيع.", "هذا مناسب لنا.", g("That suits us.", "هذا مناسب لنا.")),
                    ScenarioBeat(listOf("رسالة", "بريد", "أرسل", "ارسل", "مرفق"), g("Say you will send an email today.", "قل إنك سترسل رسالة اليوم."),
                        "سأرسل لكم رسالة بالتفاصيل اليوم.", "شكرًا جزيلًا.", g("Thank you very much.", "شكرًا جزيلًا.")),
                ),
                listOf("الميزانية…", "نستطيع التسليم بعد…", "سأرسل لكم…"),
                trackId = "business",
            ),
        )
        fun item(id: String, level: CefrLevel, skill: Skill, prompt: String, options: List<String>, correct: Int, en: String) =
            AssessmentItem("ar_a_$id", L, level, skill, Exercise.MultipleChoice("ar_a_$id", skill, Instr.choose, prompt, options, correct, promptGloss = g(en, prompt)))
        val assessment = listOf(
            item("p1", PRE_A1, Skill.VOCABULARY, "شكرًا", listOf("thank you", "hello", "goodbye"), 0, "What does شكرًا mean?"),
            item("p2", PRE_A1, Skill.VOCABULARY, "أنا", listOf("you", "I", "she"), 1, "What does أنا mean?"),
            item("p3", PRE_A1, Skill.READING, "مع السلامة", listOf("goodbye", "please", "sorry"), 0, "What does مع السلامة mean?"),
            item("a1_1", A1, Skill.GRAMMAR, "هي ___", listOf("طالب", "طالبة", "مدرّس"), 1, "She is a student: هي ___"),
            item("a1_2", A1, Skill.GRAMMAR, "مدينة ___", listOf("جميل", "جميلة", "الجميل"), 1, "a beautiful city: مدينة ___"),
            item("a1_3", A1, Skill.GRAMMAR, "The house is big.", listOf("البيت الكبير", "البيت كبير", "بيت الكبير"), 1, "The house is big."),
            item("a2_1", A2, Skill.GRAMMAR, "___ المحطة؟", listOf("أين", "ماذا", "من"), 0, "Where is the station?"),
            item("a2_2", A2, Skill.VOCABULARY, "يدرس", listOf("he works", "he studies", "he drinks"), 1, "What does يدرس mean?"),
            item("a2_3", A2, Skill.GRAMMAR, "نحن ___ في شركة.", listOf("نعمل", "يعمل", "تعمل"), 0, "We work in a company: نحن ___ في شركة."),
            item("b1_1", B1, Skill.GRAMMAR, "ذهبتُ إلى السوق ___.", listOf("أمس", "غدًا", "الآن"), 0, "I went to the market ___."),
            item("b1_2", B1, Skill.GRAMMAR, "الطلاب ___ في المكتبة.", listOf("يدرسون", "يدرس", "تدرس"), 0, "The students study in the library."),
        )
        return LanguageCurriculum(L, vocabulary, grammar, units, lessons, readings, scenarios, assessment,
            tracks = listOf(LearningTrack("business", L, g("Arabic for work", "العربية للعمل"), g("Meetings, formal emails, budgets and deadlines in Modern Standard Arabic.", "الاجتماعات والرسائل الرسمية والميزانيات والمواعيد النهائية بالعربية الفصحى."), A2, null)),
            dictations = listOf(dictation("d1", L, "أنا طالب", listOf("انا طالب")), dictation("d2", L, "أين المحطة؟", listOf("اين المحطة"))),
            writingPrompts = listOf(writing("wp1", L, g("Introduce yourself in Arabic (name, job or study).", "عرّف بنفسك بالعربية."), 5, listOf("اسمي", "أنا"))),
        )
    }
}
