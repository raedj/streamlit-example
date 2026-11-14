package com.polyglot.academy.core.content

import com.polyglot.academy.core.model.CefrLevel.B1
import com.polyglot.academy.core.model.CefrLevel.B2
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.GrammarTopic
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Lesson
import com.polyglot.academy.core.model.PartOfSpeech.ADJECTIVE
import com.polyglot.academy.core.model.PartOfSpeech.ADVERB
import com.polyglot.academy.core.model.PartOfSpeech.CONJUNCTION
import com.polyglot.academy.core.model.PartOfSpeech.MEASURE_WORD
import com.polyglot.academy.core.model.PartOfSpeech.NOUN
import com.polyglot.academy.core.model.PartOfSpeech.PARTICLE
import com.polyglot.academy.core.model.PartOfSpeech.PHRASE
import com.polyglot.academy.core.model.PartOfSpeech.PREPOSITION
import com.polyglot.academy.core.model.PartOfSpeech.VERB
import com.polyglot.academy.core.model.Skill

/**
 * Three B1 and three B2 grammar lessons each for English and Mandarin, added 2026-11-09 because the course
 * thinned out after A2. Written by AI, not yet reviewed by native speakers.
 */
internal object UpperLessons {
    private val EN = LanguageCode.ENGLISH
    private val ZH = LanguageCode.MANDARIN

    // ---------------- English ----------------

    fun englishVocab(vb: VocabBuilder) = with(vb) {
        // Telling a story (B1)
        v("suddenly", "suddenly", "quickly and unexpectedly", "فجأة", ADVERB, B1, "stories", "/ˈsʌdənli/",
            ex = "Suddenly the lights went out.", exEn = "Suddenly the lights went out.", exAr = "فجأة انطفأت الأضواء.")
        v("happen", "happen", "to take place", "يحدث", VERB, B1, "stories", "/ˈhæpən/",
            ex = "What happened after the party?", exEn = "What happened after the party?", exAr = "ماذا حدث بعد الحفلة؟")
        v("while", "while", "during the time that", "بينما", CONJUNCTION, B1, "stories", "/waɪl/",
            ex = "I was cooking while she was reading.", exEn = "I was cooking while she was reading.", exAr = "كنت أطبخ بينما كانت تقرأ.")
        v("notice", "notice", "to see or become aware of something", "يلاحظ", VERB, B1, "stories", "/ˈnoʊtɪs/")
        v("accident", "accident", "something bad that happens by chance", "حادث", NOUN, B1, "stories", "/ˈæksɪdənt/",
            ex = "Nobody was hurt in the accident.", exEn = "Nobody was hurt in the accident.", exAr = "لم يُصب أحد في الحادث.")
        v("remember", "remember", "to keep something in your mind", "يتذكّر", VERB, B1, "stories", "/rɪˈmembər/")
        // Advice and rules (B1)
        v("advice", "advice", "an opinion about what someone should do", "نصيحة", NOUN, B1, "advice", "/ədˈvaɪs/",
            ex = "Can I give you some advice?", exEn = "Can I give you some advice?", exAr = "هل يمكنني أن أقدّم لك نصيحة؟")
        v("should", "should", "used to say what is a good idea", "ينبغي", VERB, B1, "advice", "/ʃʊd/",
            ex = "You should see a doctor.", exEn = "You should see a doctor.", exAr = "ينبغي أن تراجع طبيبًا.")
        v("must", "must", "used to say something is necessary", "يجب", VERB, B1, "advice", "/mʌst/")
        v("have_to", "have to", "to be necessary because of a rule or situation", "يتعيّن عليه، مضطر إلى", PHRASE, B1, "advice",
            ex = "I have to work on Saturday.", exEn = "I have to work on Saturday.", exAr = "يتعيّن عليّ أن أعمل يوم السبت.")
        v("allowed_to", "be allowed to", "to have permission to do something", "يُسمح له بـ", PHRASE, B1, "advice")
        v("rule", "rule", "something that says what you can or cannot do", "قاعدة، قانون", NOUN, B1, "advice", "/ruːl/")
        v("recommend", "recommend", "to say that something is good or suitable", "يوصي بـ", VERB, B1, "advice", "/ˌrekəˈmend/")
        // Describing people and places (B1)
        v("neighbour", "neighbour", "a person who lives near you", "جار", NOUN, B1, "people", "/ˈneɪbər/",
            ex = "My neighbour has a big dog.", exEn = "My neighbour has a big dog.", exAr = "لدى جاري كلب كبير.")
        v("colleague", "colleague", "a person you work with", "زميل", NOUN, B1, "people", "/ˈkɒliːɡ/")
        v("reliable", "reliable", "someone you can trust to do things well", "يُعتمد عليه", ADJECTIVE, B1, "people", "/rɪˈlaɪəbəl/",
            ex = "She is a very reliable colleague.", exEn = "She is a very reliable colleague.", exAr = "إنها زميلة يُعتمد عليها كثيرًا.")
        v("honest", "honest", "telling the truth", "صادق، أمين", ADJECTIVE, B1, "people", "/ˈɒnɪst/")
        v("describe", "describe", "to say what someone or something is like", "يصف", VERB, B1, "people", "/dɪˈskraɪb/")
        v("area", "area", "a part of a town or country", "منطقة", NOUN, B1, "people", "/ˈeəriə/")
        // The passive (B2)
        v("invent", "invent", "to create something new for the first time", "يخترع", VERB, B2, "science", "/ɪnˈvent/",
            ex = "The telephone was invented in the 1870s.", exEn = "The telephone was invented in the 1870s.", exAr = "اختُرع الهاتف في سبعينيات القرن التاسع عشر.")
        v("research", "research", "careful study to find new facts", "بحث (علمي)", NOUN, B2, "science", "/rɪˈsɜːrtʃ/")
        v("factory", "factory", "a building where goods are made", "مصنع", NOUN, B2, "science", "/ˈfæktəri/",
            ex = "These cars are made in a factory near Lyon.", exEn = "These cars are made in a factory near Lyon.", exAr = "تُصنع هذه السيارات في مصنع قرب ليون.")
        v("produce", "produce", "to make or grow something", "ينتج", VERB, B2, "science", "/prəˈdjuːs/")
        v("publish", "publish", "to print or put something online for the public", "ينشر", VERB, B2, "science", "/ˈpʌblɪʃ/")
        v("discover", "discover", "to find something for the first time", "يكتشف", VERB, B2, "science", "/dɪˈskʌvər/")
        // Reported speech (B2)
        v("explain", "explain", "to make something clear", "يشرح", VERB, B2, "speech", "/ɪkˈspleɪn/",
            ex = "She explained that the train was late.", exEn = "She explained that the train was late.", exAr = "شرحت أن القطار كان متأخرًا.")
        v("complain", "complain", "to say you are unhappy about something", "يشتكي", VERB, B2, "speech", "/kəmˈpleɪn/")
        v("promise", "promise", "to say you will certainly do something", "يَعِد", VERB, B2, "speech", "/ˈprɒmɪs/")
        v("admit", "admit", "to agree that something bad is true", "يعترف", VERB, B2, "speech", "/ədˈmɪt/")
        v("claim", "claim", "to say something is true without proof", "يزعم", VERB, B2, "speech", "/kleɪm/")
        v("mention", "mention", "to speak about something briefly", "يذكر (عَرَضًا)", VERB, B2, "speech", "/ˈmenʃən/")
        // Regrets (B2)
        v("wish", "wish", "to want something that is not true now", "يتمنّى", VERB, B2, "regrets", "/wɪʃ/",
            ex = "I wish I had more free time.", exEn = "I wish I had more free time.", exAr = "أتمنّى لو كان لديّ وقت فراغ أكثر.")
        v("if_only", "if only", "used to say you strongly wish something", "ليت", PHRASE, B2, "regrets")
        v("mistake", "mistake", "something done wrongly", "خطأ", NOUN, B2, "regrets", "/mɪˈsteɪk/")
        v("otherwise", "otherwise", "if not; or else", "وإلا", ADVERB, B2, "regrets", "/ˈʌðərwaɪz/")
        v("lucky", "lucky", "having good things happen by chance", "محظوظ", ADJECTIVE, B2, "regrets", "/ˈlʌki/")
    }

    fun englishGrammar(): List<GrammarTopic> = listOf(
        grammar("past_continuous", EN, B1, g("Past continuous and past simple", "الماضي المستمر والماضي البسيط"),
            g("Use was/were + -ing for an action in progress at a moment in the past (I was cooking at eight). Use the past simple for a shorter action that interrupts it: I was cooking when the phone rang. Use 'while' with two actions in progress at the same time.",
                "استخدم was/were + ‎-ing لفعل كان مستمرًا في لحظة من الماضي (I was cooking at eight). واستخدم الماضي البسيط لفعل أقصر قطعه: I was cooking when the phone rang. واستخدم while لفعلين مستمرين في الوقت نفسه."),
            listOf(ex("I was walking home when it started to rain.", "I was walking home when it started to rain.", "كنت أمشي إلى البيت عندما بدأ المطر."),
                ex("While we were eating, the lights went out.", "While we were eating, the lights went out.", "بينما كنا نأكل، انطفأت الأضواء."),
                ex("I cooking dinner when the phone rang.", "Use 'was cooking'", "استخدم was cooking", wrong = true)),
            listOf(g("Verbs like know and want are rarely used with -ing: ✗ I was knowing → ✓ I knew.", "أفعال مثل know وwant نادرًا ما تأتي مع ‎-ing: ✗ I was knowing ← ✓ I knew."))),
        grammar("modals", EN, B1, g("should, must and have to", "should وmust وhave to"),
            g("'Should' gives advice (You should rest). 'Must' and 'have to' mean something is necessary; 'have to' is common for rules from outside (I have to wear a uniform). 'Mustn't' means it is not allowed, but 'don't have to' means it is not necessary.",
                "should للنصيحة (You should rest). وmust وhave to تعنيان أن الأمر ضروري، وhave to شائعة للقواعد المفروضة من الخارج (I have to wear a uniform). وmustn't تعني أن الأمر ممنوع، أما don't have to فتعني أنه غير ضروري."),
            listOf(ex("You should drink more water.", "You should drink more water.", "ينبغي أن تشرب ماء أكثر."),
                ex("You don't have to come, but you can.", "You don't have to come, but you can.", "لست مضطرًا إلى المجيء، لكن يمكنك ذلك."),
                ex("You must to wear a helmet.", "No 'to' after must", "لا تأتي to بعد must", wrong = true)),
            listOf(g("Mixing up mustn't and don't have to: 'You mustn't pay' means paying is forbidden.", "الخلط بين mustn't وdon't have to: عبارة You mustn't pay تعني أن الدفع ممنوع."))),
        grammar("relative_clauses", EN, B1, g("Relative clauses: who, which, that, where", "جمل الصلة: who وwhich وthat وwhere"),
            g("Use 'who' for people, 'which' for things and 'where' for places. 'That' can replace who or which in defining clauses: The man who/that lives next door is a doctor. You can leave out who/which/that when it is the object: The book (that) I read was great.",
                "استخدم who للأشخاص، وwhich للأشياء، وwhere للأماكن. ويمكن أن تحلّ that محل who أو which في جمل التعريف: The man who/that lives next door is a doctor. ويمكن حذف who/which/that حين تكون مفعولًا به: The book (that) I read was great."),
            listOf(ex("She is the colleague who helped me.", "She is the colleague who helped me.", "إنها الزميلة التي ساعدتني."),
                ex("This is the café where we first met.", "This is the café where we first met.", "هذا هو المقهى الذي التقينا فيه أول مرة."),
                ex("The man which lives here is a doctor.", "Use 'who' for people", "استخدم who للأشخاص", wrong = true)),
            listOf(g("Repeating the object: ✗ the book that I read it → ✓ the book that I read.", "تكرار المفعول به: ✗ the book that I read it ← ✓ the book that I read."))),
        grammar("passive", EN, B2, g("The passive voice", "المبني للمجهول"),
            g("Form the passive with be + past participle: Paper is made from wood; The bridge was built in 1990. Use it when the action matters more than who did it, or when we don't know who did it. Add 'by' to name the doer: It was painted by a local artist.",
                "يُصاغ المبني للمجهول بـ be + التصريف الثالث: Paper is made from wood؛ The bridge was built in 1990. ويُستخدم حين يهمّ الفعل أكثر من فاعله، أو حين لا نعرف الفاعل. وأضف by لذكر الفاعل: It was painted by a local artist."),
            listOf(ex("This bread is baked every morning.", "This bread is baked every morning.", "يُخبز هذا الخبز كل صباح."),
                ex("The results were published last week.", "The results were published last week.", "نُشرت النتائج الأسبوع الماضي."),
                ex("The museum was build in 1900.", "Use the past participle: built", "استخدم التصريف الثالث: built", wrong = true)),
            listOf(g("Forgetting 'be': ✗ The letter sent yesterday → ✓ The letter was sent yesterday.", "نسيان be: ✗ The letter sent yesterday ← ✓ The letter was sent yesterday."))),
        grammar("reported_speech", EN, B2, g("Reported speech", "الكلام المنقول"),
            g("When we report what someone said, the verb usually moves one step back in time: \"I'm tired\" → She said (that) she was tired; \"I will call\" → He said he would call. Use 'say' without a person and 'tell' with a person: He told me he was busy.",
                "عند نقل كلام شخص يتراجع زمن الفعل عادةً خطوة إلى الماضي: \"I'm tired\" ← She said (that) she was tired؛ و\"I will call\" ← He said he would call. واستخدم say دون ذكر المخاطَب، وtell مع ذكره: He told me he was busy."),
            listOf(ex("She said that she was hungry.", "She said that she was hungry.", "قالت إنها جائعة."),
                ex("He told me he would be late.", "He told me he would be late.", "أخبرني أنه سيتأخر."),
                ex("He said me that he was busy.", "Use 'told me' or 'said that'", "استخدم told me أو said that", wrong = true)),
            listOf(g("Reported questions use statement order: ✗ She asked where was the station → ✓ She asked where the station was.", "الأسئلة المنقولة تأخذ ترتيب الجملة الخبرية: ✗ She asked where was the station ← ✓ She asked where the station was."))),
        grammar("third_conditional", EN, B2, g("Third conditional and wishes about the past", "الشرط الثالث والتمنّي في الماضي"),
            g("Use the third conditional for imagined past situations that did not happen: If + had + past participle, would have + past participle (If I had left earlier, I would have caught the train). Use 'I wish' / 'If only' + had + past participle for regrets: I wish I had studied more.",
                "استخدم الشرط الثالث لمواقف متخيَّلة في الماضي لم تحدث: If + had + التصريف الثالث، ثم would have + التصريف الثالث (If I had left earlier, I would have caught the train). واستخدم I wish أو If only + had + التصريف الثالث للتعبير عن الندم: I wish I had studied more."),
            listOf(ex("If you had told me, I would have helped.", "If you had told me, I would have helped.", "لو أخبرتني لساعدتك."),
                ex("I wish I hadn't said that.", "I wish I hadn't said that.", "ليتني لم أقل ذلك."),
                ex("If I would have known, I would have come.", "Use 'had known' in the if-clause", "استخدم had known في جملة الشرط", wrong = true)),
            listOf(g("✗ If I had went → ✓ If I had gone (use the past participle).", "✗ If I had went ← ✓ If I had gone (استخدم التصريف الثالث)."))),
    )

    fun englishLessons(f: LessonFactory): List<Lesson> = listOf(
        f.lesson("stories_past", B1, "b1_skills", g("Telling a story", "سرد قصة"), g("Describe what was happening when something else happened.", "صِف ما كان يحدث عندما وقع شيء آخر."),
            Skill.SPEAKING, listOf("suddenly", "happen", "while", "notice", "accident", "remember"), "past_continuous",
            extra = listOf(
                fill("pc1", EN, "I ___ (read) when the phone rang.", listOf("was reading")),
                choice("pc2", EN, "While they ___ TV, someone knocked on the door.", listOf("were watching", "watched", "are watching"), 0),
                order("pc3", EN, listOf("She", "was", "sleeping", "when", "I", "arrived"), g("She was sleeping when I arrived.", "كانت نائمة عندما وصلت.")),
                writing("pc4", EN, g("Write about something surprising that happened to you (40+ words). Say what you were doing when it happened.", "اكتب عن أمر مفاجئ حدث لك (40 كلمة على الأقل). قل ماذا كنت تفعل عندما حدث."), 40,
                    listOf("was", "when", "suddenly")),
            ), prerequisites = listOf("plans")),
        f.lesson("advice_rules", B1, "b1_skills", g("Advice and rules", "النصائح والقواعد"), g("Give advice and talk about what is necessary or allowed.", "قدّم النصيحة وتحدّث عمّا هو ضروري أو مسموح."),
            Skill.SPEAKING, listOf("advice", "should", "must", "have_to", "allowed_to", "rule", "recommend"), "modals",
            extra = listOf(
                choice("mo1", EN, "It's free, so you ___ pay.", listOf("don't have to", "mustn't", "should"), 0,
                    explanation = g("'Don't have to' = it isn't necessary.", "don't have to تعني: ليس ضروريًا.")),
                fill("mo2", EN, "You look tired. You ___ go to bed early.", listOf("should")),
                choice("mo3", EN, "You ___ smoke here. It's against the rules.", listOf("mustn't", "don't have to", "should"), 0),
                translate("mo4", EN, g("You should ask your teacher.", "ينبغي أن تسأل معلّمك."), listOf("You should ask your teacher")),
            ), prerequisites = listOf("stories_past")),
        f.lesson("describing_people", B1, "b1_skills", g("Describing people and places", "وصف الأشخاص والأماكن"), g("Add details with who, which, that and where.", "أضف التفاصيل باستخدام who وwhich وthat وwhere."),
            Skill.WRITING, listOf("neighbour", "colleague", "reliable", "honest", "describe", "area"), "relative_clauses",
            extra = listOf(
                fill("rc1", EN, "The woman ___ lives next door is a nurse.", listOf("who", "that")),
                choice("rc2", EN, "This is the town ___ I grew up.", listOf("where", "which", "who"), 0),
                fill("rc3", EN, "I lost the book ___ you gave me.", listOf("that", "which"), g("A thing.", "شيء.")),
                writing("rc4", EN, g("Describe a person you admire (40+ words). Use who, which or where at least twice.", "صِف شخصًا تعجب به (40 كلمة على الأقل). استخدم who أو which أو where مرتين على الأقل."), 40,
                    listOf("who", "which", "where")),
            ), prerequisites = listOf("advice_rules")),
        f.lesson("passive", B2, "b2_skills", g("How things are made", "كيف تُصنع الأشياء"), g("Use the passive to talk about processes, inventions and news.", "استخدم المبني للمجهول للحديث عن العمليات والاختراعات والأخبار."),
            Skill.READING, listOf("invent", "research", "factory", "produce", "publish", "discover"), "passive",
            extra = listOf(
                fill("pa1", EN, "English ___ (speak) in many countries.", listOf("is spoken")),
                fill("pa2", EN, "The bridge ___ (build) in 1990.", listOf("was built")),
                choice("pa3", EN, "The new results ___ next month.", listOf("will be published", "will publish", "are publishing"), 0),
                translate("pa4", EN, g("The letter was sent yesterday.", "أُرسلت الرسالة أمس."), listOf("The letter was sent yesterday")),
            ), prerequisites = listOf("conditionals", "describing_people")),
        f.lesson("reported_speech", B2, "b2_skills", g("What people said", "ما قاله الناس"), g("Report statements, promises and complaints.", "انقل الأقوال والوعود والشكاوى."),
            Skill.LISTENING, listOf("explain", "complain", "promise", "admit", "claim", "mention"), "reported_speech",
            extra = listOf(
                fill("rs1", EN, "\"I am tired.\" → She said that she ___ tired.", listOf("was")),
                fill("rs2", EN, "\"I will help you.\" → He promised that he ___ help me.", listOf("would")),
                choice("rs3", EN, "He ___ me that the meeting was cancelled.", listOf("told", "said", "spoke"), 0),
                choice("rs4", EN, "She asked me where ___.", listOf("the station was", "was the station", "is the station was"), 0),
            ), prerequisites = listOf("passive")),
        f.lesson("regrets", B2, "b2_skills", g("Regrets and missed chances", "الندم والفرص الضائعة"), g("Imagine a different past with the third conditional and 'I wish'.", "تخيّل ماضيًا مختلفًا بالشرط الثالث وI wish."),
            Skill.WRITING, listOf("wish", "if_only", "mistake", "otherwise", "lucky", "regret", "opportunity"), "third_conditional",
            extra = listOf(
                fill("tc1", EN, "If I had known, I would ___ come. (have)", listOf("have")),
                fill("tc2", EN, "If she ___ (study) harder, she would have passed.", listOf("had studied")),
                choice("tc3", EN, "I wish I ___ that. I'm sorry.", listOf("hadn't said", "didn't say", "wouldn't say"), 0),
                writing("tc4", EN, g("Write about a decision you regret (40+ words). What would have happened if you had chosen differently?", "اكتب عن قرار تندم عليه (40 كلمة على الأقل). ماذا كان سيحدث لو اخترت غير ذلك؟"), 40,
                    listOf("If I had", "would have", "I wish")),
            ), prerequisites = listOf("reported_speech")),
    )

    fun englishUnits(): List<CourseUnit> = listOf(
        CourseUnit("en_u_b1_skills", EN, B1, g("Stories, advice and descriptions", "القصص والنصائح والوصف"), g("Past continuous, modal verbs and relative clauses", "الماضي المستمر والأفعال الناقصة وجمل الصلة"),
            listOf("en_l_stories_past", "en_l_advice_rules", "en_l_describing_people")),
        CourseUnit("en_u_b2_skills", EN, B2, g("Processes, reports and regrets", "العمليات والنقل والندم"), g("The passive, reported speech and the third conditional", "المبني للمجهول والكلام المنقول والشرط الثالث"),
            listOf("en_l_passive", "en_l_reported_speech", "en_l_regrets")),
    )

    // ---------------- Mandarin ----------------

    fun mandarinVocab(vb: VocabBuilder) = with(vb) {
        // 过 for experiences (B1)
        v("guo", "过", "(marks something you have experienced)", "(أداة تدل على تجربة سابقة)", PARTICLE, B1, "experience", "guo", trad = "過",
            ex = "我去过长城。", exEn = "I have been to the Great Wall.", exAr = "سبق أن زرتُ سور الصين العظيم.")
        v("changcheng", "长城", "the Great Wall", "سور الصين العظيم", NOUN, B1, "experience", "Chángchéng", trad = "長城")
        v("conglai", "从来", "all along; (with 没 or 不) never", "دائمًا؛ (مع النفي) قطّ", ADVERB, B1, "experience", "cónglái", trad = "從來",
            ex = "我从来没吃过烤鸭。", exEn = "I have never eaten roast duck.", exAr = "لم آكل البط المشوي قطّ.")
        v("ci", "次", "time, occurrence (measure word)", "مرة (كلمة عدّ)", MEASURE_WORD, B1, "experience", "cì")
        v("lvyou", "旅游", "to travel (for pleasure)", "يسافر للسياحة", VERB, B1, "experience", "lǚyóu", trad = "旅遊")
        v("pa", "爬", "to climb", "يتسلّق", VERB, B1, "experience", "pá")
        v("kaoya", "烤鸭", "roast duck", "بط مشوي", NOUN, B1, "experience", "kǎoyā", trad = "烤鴨")
        // 得 for how well (B1)
        v("de_complement", "得", "(links a verb to how well it is done)", "(أداة تربط الفعل بكيفية أدائه)", PARTICLE, B1, "skills", "de",
            ex = "他跑得很快。", exEn = "He runs very fast.", exAr = "هو يركض بسرعة كبيرة.")
        v("pao", "跑", "to run", "يركض", VERB, B1, "skills", "pǎo")
        v("kuai", "快", "fast", "سريع", ADJECTIVE, B1, "skills", "kuài")
        v("man", "慢", "slow", "بطيء", ADJECTIVE, B1, "skills", "màn")
        v("liuli", "流利", "fluent", "طَلِق (في الكلام)", ADJECTIVE, B1, "skills", "liúlì",
            ex = "她中文说得很流利。", exEn = "She speaks Chinese very fluently.", exAr = "هي تتحدث الصينية بطلاقة.")
        v("renzhen", "认真", "serious, conscientious", "جادّ، مجتهد", ADJECTIVE, B1, "skills", "rènzhēn", trad = "認真")
        v("changge", "唱歌", "to sing", "يغنّي", VERB, B1, "skills", "chàng gē")
        // 虽然…但是… and 如果…就… (B1)
        v("suiran", "虽然", "although", "مع أنّ", CONJUNCTION, B1, "linking", "suīrán", trad = "雖然",
            ex = "虽然很累，但是我很高兴。", exEn = "Although I'm tired, I'm very happy.", exAr = "مع أنني متعب، فأنا سعيد جدًا.")
        v("danshi", "但是", "but", "لكن", CONJUNCTION, B1, "linking", "dànshì")
        v("ruguo", "如果", "if", "إذا", CONJUNCTION, B1, "linking", "rúguǒ",
            ex = "如果明天下雨，我们就不去了。", exEn = "If it rains tomorrow, we won't go.", exAr = "إذا أمطرت غدًا فلن نذهب.")
        v("jiu", "就", "then (links a result to a condition)", "فـ، إذن (يربط النتيجة بالشرط)", ADVERB, B1, "linking", "jiù")
        v("haishi", "还是", "still, nevertheless", "مع ذلك، ما زال", ADVERB, B1, "linking", "háishi", trad = "還是")
        v("lei", "累", "tired", "متعب", ADJECTIVE, B1, "linking", "lèi")
        v("jianchi", "坚持", "to keep on, to persist", "يثابر، يواصل", VERB, B1, "linking", "jiānchí", trad = "堅持")
        // 把 (B2)
        v("ba", "把", "(moves the object before the verb to show what is done to it)", "(أداة تقدّم المفعول به على الفعل لبيان ما يُفعل به)", PREPOSITION, B2, "ba", "bǎ",
            ex = "请把门关上。", exEn = "Please close the door.", exAr = "من فضلك أغلق الباب.")
        v("fang", "放", "to put, to place", "يضع", VERB, B2, "ba", "fàng")
        v("guan", "关", "to close; to turn off", "يغلق؛ يطفئ", VERB, B2, "ba", "guān", trad = "關")
        v("dakai", "打开", "to open; to turn on", "يفتح؛ يشغّل", VERB, B2, "ba", "dǎkāi", trad = "打開")
        v("chuanghu", "窗户", "window", "نافذة", NOUN, B2, "ba", "chuānghu", trad = "窗戶")
        v("zuoye", "作业", "homework", "واجب منزلي", NOUN, B2, "ba", "zuòyè", trad = "作業")
        v("nongdiu", "弄丢", "to lose (by accident)", "يُضيّع", VERB, B2, "ba", "nòngdiū", trad = "弄丟")
        // 被 (B2)
        v("bei_passive", "被", "(marks the passive: done by someone)", "(أداة المبني للمجهول)", PREPOSITION, B2, "passive", "bèi",
            ex = "我的钱包被偷了。", exEn = "My wallet was stolen.", exAr = "سُرقت محفظتي.")
        v("tou", "偷", "to steal", "يسرق", VERB, B2, "passive", "tōu")
        v("piping", "批评", "to criticise", "ينتقد، يوبّخ", VERB, B2, "passive", "pīpíng", trad = "批評")
        v("qianbao", "钱包", "wallet, purse", "محفظة", NOUN, B2, "passive", "qiánbāo", trad = "錢包")
        v("pian", "骗", "to cheat, to deceive", "يخدع، يحتال على", VERB, B2, "passive", "piàn", trad = "騙")
        v("zhuang", "撞", "to hit, to bump into", "يصدم", VERB, B2, "passive", "zhuàng")
        // 不但…而且…, 越来越, 连…都 (B2)
        v("budan", "不但", "not only", "ليس فقط", CONJUNCTION, B2, "linking2", "búdàn",
            ex = "他不但会说中文，而且会写汉字。", exEn = "He can not only speak Chinese but also write characters.", exAr = "هو لا يتحدث الصينية فحسب، بل يكتب الحروف الصينية أيضًا.")
        v("erqie", "而且", "and also, moreover", "بل، وأيضًا", CONJUNCTION, B2, "linking2", "érqiě")
        v("yuelaiyue", "越来越", "more and more", "أكثر فأكثر", ADVERB, B2, "linking2", "yuè lái yuè", trad = "越來越")
        v("lian", "连", "even (连…都/也)", "حتى (连…都/也)", PREPOSITION, B2, "linking2", "lián", trad = "連")
        v("shenzhi", "甚至", "even, to the extent that", "بل حتى", ADVERB, B2, "linking2", "shènzhì")
        v("huanjing", "环境", "environment", "البيئة", NOUN, B2, "linking2", "huánjìng", trad = "環境")
        v("wuran", "污染", "pollution; to pollute", "تلوّث؛ يلوّث", NOUN, B2, "linking2", "wūrǎn")
    }

    fun mandarinGrammar(): List<GrammarTopic> = listOf(
        grammar("guo", ZH, B1, g("过 for experiences", "‏过 للتجارب السابقة"),
            g("Put 过 after a verb to say you have had an experience at some time: 我去过长城 (I have been to the Great Wall). To say you never have, use 没(有) + verb + 过, often with 从来: 我从来没吃过烤鸭. Count the times with 次: 我去过两次.",
                "ضع 过 بعد الفعل للدلالة على أنك مررت بالتجربة يومًا ما: 我去过长城 (سبق أن زرت سور الصين). وللنفي استخدم 没(有) + الفعل + 过، وغالبًا مع 从来: 我从来没吃过烤鸭. وعُدّ المرات بـ 次: 我去过两次."),
            listOf(ex("你去过中国吗？", "Have you ever been to China?", "هل سبق أن زرت الصين؟"),
                ex("我爬过两次长城。", "I have climbed the Great Wall twice.", "تسلّقت سور الصين مرتين."),
                ex("我没去过了北京。", "No 了 with 没…过", "لا يأتي 了 مع 没…过", wrong = true)),
            listOf(g("Using 不 instead of 没: ✗ 我不去过 → ✓ 我没去过.", "استخدام 不 بدل 没: ✗ 我不去过 ← ✓ 我没去过."))),
        grammar("de_complement", ZH, B1, g("Verb + 得 + how well", "الفعل + 得 + كيفية الأداء"),
            g("To say how well or how fast something is done, use verb + 得 + adjective: 他跑得很快. If there is an object, say the verb twice or put the object first: 她说中文说得很好 / 她中文说得很好. Negate the adjective, not the verb: 我唱得不好.",
                "لتقول كيف يؤدَّى الفعل أو بأي سرعة، استخدم الفعل + 得 + الصفة: 他跑得很快. وإذا كان هناك مفعول به فكرّر الفعل أو قدّم المفعول به: 她说中文说得很好 / 她中文说得很好. وانفِ الصفة لا الفعل: 我唱得不好."),
            listOf(ex("你说得很对。", "What you say is quite right.", "كلامك صحيح تمامًا."),
                ex("他汉字写得很认真。", "He writes characters very carefully.", "هو يكتب الحروف الصينية بعناية كبيرة."),
                ex("他说中文得很好。", "Repeat the verb: 说中文说得", "كرّر الفعل: 说中文说得", wrong = true)),
            listOf(g("Mixing up 的, 得 and 地: after a verb, use 得 for how well.", "الخلط بين 的 و得 و地: بعد الفعل استخدم 得 لبيان كيفية الأداء."))),
        grammar("suiran", ZH, B1, g("虽然…但是… and 如果…就…", "‏虽然…但是… و如果…就…"),
            g("Mandarin pairs linking words: 虽然 (although) … 但是/可是 (but) …: 虽然很贵，但是很好. 如果 (if) … 就 (then) …: 如果你有时间，就来吧. 就 goes after the subject of the second part, before the verb. 还是 means 'still': 虽然很累，他还是去了.",
                "تستخدم الصينية أدوات الربط أزواجًا: 虽然 (مع أنّ) … 但是/可是 (لكن) …: 虽然很贵，但是很好. و如果 (إذا) … 就 (فـ) …: 如果你有时间，就来吧. ويأتي 就 بعد فاعل الجزء الثاني وقبل الفعل. و还是 تعني «مع ذلك»: 虽然很累，他还是去了."),
            listOf(ex("虽然下雨了，但是我们还是去了公园。", "Although it rained, we still went to the park.", "مع أنها أمطرت، ذهبنا إلى الحديقة مع ذلك."),
                ex("如果你不舒服，就在家休息吧。", "If you don't feel well, rest at home.", "إذا كنت متوعكًا فاسترح في البيت."),
                ex("如果有时间，就我们去。", "就 goes after the subject: 我们就去", "يأتي 就 بعد الفاعل: 我们就去", wrong = true)),
            listOf(g("English uses only 'although'; in Mandarin keeping both 虽然 and 但是 is normal.", "تكتفي الإنجليزية بـ although، أما في الصينية فاستخدام 虽然 و但是 معًا أمر طبيعي."))),
        grammar("ba", ZH, B2, g("The 把 construction", "تركيب 把"),
            g("把 brings the object before the verb to say what is done to it: subject + 把 + object + verb + something more (a result, direction or 了): 我把作业做完了 (I finished the homework). The verb cannot stand alone after 把: say 把门关上, not 把门关. The object must be something specific.",
                "تقدّم 把 المفعول به على الفعل لبيان ما يُفعل به: الفاعل + 把 + المفعول به + الفعل + تكملة (نتيجة أو اتجاه أو 了): 我把作业做完了 (أنهيت الواجب). ولا يأتي الفعل وحده بعد 把: قل 把门关上 لا 把门关. ويجب أن يكون المفعول به محدّدًا."),
            listOf(ex("请把窗户打开。", "Please open the window.", "من فضلك افتح النافذة."),
                ex("我把钥匙弄丢了。", "I've lost my keys.", "أضعت مفاتيحي."),
                ex("我把这本书看。", "The verb needs something after it: 看完了", "يحتاج الفعل إلى تكملة: 看完了", wrong = true)),
            listOf(g("Putting 没 after 把: ✗ 我把作业没做完 → ✓ 我没把作业做完.", "وضع 没 بعد 把: ✗ 我把作业没做完 ← ✓ 我没把作业做完."))),
        grammar("bei", ZH, B2, g("The 被 passive", "المبني للمجهول بـ 被"),
            g("Use 被 for something that happens to the subject, often something unwelcome: 我的钱包被偷了 (My wallet was stolen). You can name who did it after 被: 他被老师批评了. Like 把, the verb needs something after it, such as 了 or a result.",
                "استخدم 被 لما يقع على الفاعل، وغالبًا ما يكون أمرًا غير مرغوب فيه: 我的钱包被偷了 (سُرقت محفظتي). ويمكن ذكر من قام بالفعل بعد 被: 他被老师批评了. وكما في 把، يحتاج الفعل إلى تكملة مثل 了 أو نتيجة."),
            listOf(ex("他的自行车被人撞坏了。", "His bike was damaged when someone ran into it.", "تضرّرت دراجته حين صدمها أحدهم."),
                ex("我被他骗了。", "I was tricked by him.", "خدعني."),
                ex("我的钱包被偷。", "Add 了: 被偷了", "أضف 了: 被偷了", wrong = true)),
            listOf(g("Overusing 被: many English passives are active in Mandarin, e.g. 这本书写得很好 'This book is well written'.", "الإكثار من 被: كثير من صيغ المجهول في الإنجليزية تأتي معلومة في الصينية، مثل 这本书写得很好 «هذا الكتاب مكتوب جيدًا»."))),
        grammar("budan", ZH, B2, g("不但…而且…, 越来越 and 连…都", "‏不但…而且… و越来越 و连…都"),
            g("不但 A，而且 B means 'not only A but also B': 他不但聪明，而且很努力. 越来越 + adjective means 'more and more': 天气越来越冷. 连…都/也 means 'even': 他连饭都没吃 (He didn't even eat).",
                "‏不但 A，而且 B تعني «ليس A فحسب، بل B أيضًا»: 他不但聪明，而且很努力. و越来越 + صفة تعني «أكثر فأكثر»: 天气越来越冷. و连…都/也 تعني «حتى»: 他连饭都没吃 (لم يأكل حتى)."),
            listOf(ex("这里的环境不但干净，而且很安静。", "The environment here is not only clean but also very quiet.", "البيئة هنا ليست نظيفة فحسب، بل هادئة جدًا أيضًا."),
                ex("城市的污染越来越严重。", "Pollution in the city is getting more and more serious.", "يزداد التلوث في المدينة سوءًا."),
                ex("他越来越很忙。", "No 很 after 越来越", "لا يأتي 很 بعد 越来越", wrong = true)),
            listOf(g("Forgetting 都/也 after 连: ✗ 他连水没喝 → ✓ 他连水都没喝.", "نسيان 都/也 بعد 连: ✗ 他连水没喝 ← ✓ 他连水都没喝."))),
    )

    fun mandarinLessons(f: LessonFactory): List<Lesson> = listOf(
        f.lesson("experiences", B1, "b1_skills", g("Have you ever…?", "هل سبق أن…؟"), g("Talk about experiences with 过 and 从来没.", "تحدّث عن تجاربك باستخدام 过 و从来没."),
            Skill.SPEAKING, listOf("guo", "changcheng", "conglai", "ci", "lvyou", "pa", "kaoya"), "guo",
            extra = listOf(
                fill("guo1", ZH, "我去___北京两次。", listOf("过")),
                choice("guo2", ZH, "I have never eaten roast duck:", listOf("我从来没吃过烤鸭。", "我从来不吃过烤鸭。", "我从来没吃了烤鸭。"), 0),
                order("guo3", ZH, listOf("你", "爬过", "长城", "吗"), g("Have you ever climbed the Great Wall?", "هل سبق أن تسلّقت سور الصين؟"), separator = ""),
                writing("guo4", ZH, g("Write about a place you have been to and something you have never done (use 过 and 从来没).", "اكتب عن مكان زرته وشيء لم تفعله قطّ (استخدم 过 و从来没)."), 15, listOf("过", "从来没", "次")),
            ), prerequisites = listOf("compare")),
        f.lesson("how_well", B1, "b1_skills", g("How well do you do it?", "ما مدى إتقانك؟"), g("Describe how well, fast or carefully something is done with 得.", "صِف إتقان الفعل أو سرعته أو العناية به باستخدام 得."),
            Skill.SPEAKING, listOf("de_complement", "pao", "kuai", "man", "liuli", "renzhen", "changge"), "de_complement",
            extra = listOf(
                fill("de1", ZH, "他跑___很快。", listOf("得")),
                choice("de2", ZH, "She speaks Chinese very fluently:", listOf("她说中文说得很流利。", "她说中文得很流利。", "她很流利说中文得。"), 0),
                choice("de3", ZH, "I don't sing well:", listOf("我唱得不好。", "我不唱得好。", "我唱不得好。"), 0),
                order("de4", ZH, listOf("他", "写", "得", "很", "认真"), g("He writes very carefully.", "هو يكتب بعناية كبيرة."), separator = ""),
            ), prerequisites = listOf("experiences")),
        f.lesson("although_if", B1, "b1_skills", g("Although… and if…", "مع أنّ… وإذا…"), g("Link ideas with 虽然…但是… and 如果…就….", "اربط الأفكار باستخدام 虽然…但是… و如果…就…."),
            Skill.WRITING, listOf("suiran", "danshi", "ruguo", "jiu", "haishi", "lei", "jianchi"), "suiran",
            extra = listOf(
                fill("sr1", ZH, "虽然很累，___我很高兴。", listOf("但是", "可是")),
                fill("sr2", ZH, "如果明天不下雨，我们___去公园。", listOf("就")),
                choice("sr3", ZH, "Although it's hard, he still keeps at it:", listOf("虽然很难，他还是坚持。", "虽然很难，所以他坚持。", "因为很难，但是他坚持。"), 0),
                writing("sr4", ZH, g("Write two sentences about learning Chinese, one with 虽然…但是… and one with 如果…就….", "اكتب جملتين عن تعلّم الصينية، واحدة بـ 虽然…但是… وأخرى بـ 如果…就…."), 15, listOf("虽然", "但是", "如果", "就")),
            ), prerequisites = listOf("how_well")),
        f.lesson("ba", B2, "b2_skills", g("Getting things done with 把", "إنجاز الأشياء بـ 把"), g("Say what you do to something with the 把 construction.", "قل ماذا تفعل بشيء ما باستخدام تركيب 把."),
            Skill.GRAMMAR, listOf("ba", "fang", "guan", "dakai", "chuanghu", "zuoye", "nongdiu"), "ba",
            extra = listOf(
                fill("ba1", ZH, "请___窗户打开。", listOf("把")),
                choice("ba2", ZH, "I finished my homework:", listOf("我把作业做完了。", "我把作业做。", "我作业把做完了。"), 0),
                choice("ba3", ZH, "I haven't finished my homework:", listOf("我没把作业做完。", "我把作业没做完。", "我把没作业做完。"), 0),
                order("ba4", ZH, listOf("他", "把", "书", "放在", "桌子上", "了"), g("He put the book on the table.", "وضع الكتاب على الطاولة."), separator = ""),
            ), prerequisites = listOf("although_if")),
        f.lesson("bei", B2, "b2_skills", g("Things that happened to you: 被", "ما وقع عليك: 被"), g("Talk about unwelcome events with the 被 passive.", "تحدّث عن الأحداث غير المرغوب فيها بصيغة 被."),
            Skill.LISTENING, listOf("bei_passive", "tou", "piping", "qianbao", "pian", "zhuang"), "bei",
            extra = listOf(
                fill("bei1", ZH, "我的钱包___偷了。", listOf("被")),
                choice("bei2", ZH, "He was criticised by the teacher:", listOf("他被老师批评了。", "他把老师批评了。", "老师被他批评了。"), 0),
                dictation("bei3", ZH, "我的钱包被偷了", listOf("我的钱包被偷了。")),
                translate("bei4", ZH, g("I was tricked.", "خُدعتُ."), listOf("我被骗了", "我被骗了。")),
            ), prerequisites = listOf("ba")),
        f.lesson("not_only", B2, "b2_skills", g("Not only…, more and more…", "ليس فقط… وأكثر فأكثر…"), g("Build longer arguments with 不但…而且…, 越来越 and 连…都.", "ابنِ حججًا أطول باستخدام 不但…而且… و越来越 و连…都."),
            Skill.WRITING, listOf("budan", "erqie", "yuelaiyue", "lian", "shenzhi", "huanjing", "wuran"), "budan",
            extra = listOf(
                fill("bd1", ZH, "他不但会说中文，___会写汉字。", listOf("而且", "并且", "还", "也")),
                fill("bd2", ZH, "天气越来___冷。", listOf("越")),
                choice("bd3", ZH, "He didn't even drink water:", listOf("他连水都没喝。", "他连水没喝。", "他都连水没喝。"), 0),
                writing("bd4", ZH, g("Write about how your city has changed (use 越来越 and 不但…而且…).", "اكتب عن تغيّر مدينتك (استخدم 越来越 و不但…而且…)."), 20, listOf("越来越", "不但", "而且")),
            ), prerequisites = listOf("bei")),
    )

    fun mandarinUnits(): List<CourseUnit> = listOf(
        CourseUnit("zh_u_b1_skills", ZH, B1, g("Experiences, skills and linking ideas", "التجارب والمهارات وربط الأفكار"), g("过, 得 and 虽然…但是… / 如果…就…", "‏过 و得 و虽然…但是… / 如果…就…"),
            listOf("zh_l_experiences", "zh_l_how_well", "zh_l_although_if")),
        CourseUnit("zh_u_b2_skills", ZH, B2, g("把, 被 and longer arguments", "‏把 و被 والحجج الأطول"), g("The 把 and 被 constructions, 不但…而且… and 越来越", "تركيبا 把 و被، و不但…而且… و越来越"),
            listOf("zh_l_ba", "zh_l_bei", "zh_l_not_only")),
    )
}
