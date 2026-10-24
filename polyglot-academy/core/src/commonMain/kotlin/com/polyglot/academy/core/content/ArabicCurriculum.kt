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
        )
        val units = listOf(
            CourseUnit("ar_u_basics", L, PRE_A1, g("First steps", "الخطوات الأولى"), g("Greetings and pronouns", "التحيات والضمائر"), listOf("ar_l_greetings", "ar_l_people")),
            CourseUnit("ar_u_everyday", L, A1, g("Everyday Arabic", "العربية اليومية"), g("Café, places and routines", "المقهى والأماكن والروتين"), listOf("ar_l_cafe", "ar_l_places", "ar_l_routine")),
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
            dictations = listOf(dictation("d1", L, "أنا طالب", listOf("انا طالب")), dictation("d2", L, "أين المحطة؟", listOf("اين المحطة"))),
            writingPrompts = listOf(writing("wp1", L, g("Introduce yourself in Arabic (name, job or study).", "عرّف بنفسك بالعربية."), 5, listOf("اسمي", "أنا"))),
        )
    }
}
