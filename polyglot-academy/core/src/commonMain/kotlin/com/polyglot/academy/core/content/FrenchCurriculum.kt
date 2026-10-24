package com.polyglot.academy.core.content

import com.polyglot.academy.core.assessment.AssessmentItem
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CefrLevel.A1
import com.polyglot.academy.core.model.CefrLevel.A2
import com.polyglot.academy.core.model.CefrLevel.B1
import com.polyglot.academy.core.model.CefrLevel.B2
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gender.FEMININE
import com.polyglot.academy.core.model.Gender.MASCULINE
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.PartOfSpeech.ADJECTIVE
import com.polyglot.academy.core.model.PartOfSpeech.ADVERB
import com.polyglot.academy.core.model.PartOfSpeech.NOUN
import com.polyglot.academy.core.model.PartOfSpeech.PHRASE
import com.polyglot.academy.core.model.PartOfSpeech.VERB
import com.polyglot.academy.core.model.ReadingPassage
import com.polyglot.academy.core.model.RolePlayScenario
import com.polyglot.academy.core.model.ScenarioBeat
import com.polyglot.academy.core.model.Skill

internal object FrenchCurriculum {
    private val L = LanguageCode.FRENCH

    fun build(): LanguageCurriculum {
        val vb = VocabBuilder(L)
        with(vb) {
            v("bonjour", "bonjour", "hello, good morning", "مرحبًا / صباح الخير", PHRASE, A1, "greetings", "/bɔ̃.ʒuʁ/",
                ex = "Bonjour, je m'appelle Karim.", exEn = "Hello, my name is Karim.", exAr = "مرحبًا، اسمي كريم.")
            v("bonsoir", "bonsoir", "good evening", "مساء الخير", PHRASE, A1, "greetings", "/bɔ̃.swaʁ/")
            v("merci", "merci", "thank you", "شكرًا", PHRASE, A1, "greetings", "/mɛʁ.si/",
                ex = "Merci beaucoup !", exEn = "Thank you very much!", exAr = "شكرًا جزيلًا!")
            v("svp", "s'il vous plaît", "please (formal)", "من فضلك (رسمي)", PHRASE, A1, "greetings", "/sil vu plɛ/")
            v("pardon", "pardon", "sorry, excuse me", "عذرًا", PHRASE, A1, "greetings", "/paʁ.dɔ̃/")
            v("au_revoir", "au revoir", "goodbye", "إلى اللقاء", PHRASE, A1, "greetings", "/o ʁə.vwaʁ/")
            v("je_mappelle", "je m'appelle", "my name is", "اسمي", PHRASE, A1, "greetings", "/ʒə ma.pɛl/")
            v("enchante", "enchanté / enchantée", "nice to meet you (m./f.)", "تشرّفت بمعرفتك (مذكر/مؤنث)", ADJECTIVE, A1, "greetings", "/ɑ̃.ʃɑ̃.te/")
            v("famille", "la famille", "family", "العائلة", NOUN, A1, "people", "/fa.mij/", gender = FEMININE)
            v("mere", "la mère", "mother", "الأم", NOUN, A1, "people", "/mɛʁ/", gender = FEMININE)
            v("pere", "le père", "father", "الأب", NOUN, A1, "people", "/pɛʁ/", gender = MASCULINE)
            v("frere", "le frère", "brother", "الأخ", NOUN, A1, "people", "/fʁɛʁ/", gender = MASCULINE)
            v("soeur", "la sœur", "sister", "الأخت", NOUN, A1, "people", "/sœʁ/", gender = FEMININE)
            v("ami", "l'ami / l'amie", "friend", "الصديق / الصديقة", NOUN, A1, "people", "/a.mi/",
                ex = "C'est mon meilleur ami.", exEn = "He's my best friend.", exAr = "إنه أعزّ أصدقائي.")
            v("eau", "l'eau", "water", "الماء", NOUN, A1, "food", "/o/", gender = FEMININE)
            v("cafe", "le café", "coffee; café", "القهوة؛ المقهى", NOUN, A1, "food", "/ka.fe/", gender = MASCULINE)
            v("pain", "le pain", "bread", "الخبز", NOUN, A1, "food", "/pɛ̃/", gender = MASCULINE)
            v("addition", "l'addition", "the bill (restaurant)", "الحساب (في المطعم)", NOUN, A2, "food", "/a.di.sjɔ̃/", gender = FEMININE,
                ex = "L'addition, s'il vous plaît.", exEn = "The bill, please.", exAr = "الحساب من فضلك.")
            v("voudrais", "je voudrais", "I would like", "أودّ", PHRASE, A1, "food", "/ʒə vu.dʁɛ/",
                ex = "Je voudrais un café, s'il vous plaît.", exEn = "I'd like a coffee, please.", exAr = "أودّ قهوة من فضلك.")
            v("delicieux", "délicieux / délicieuse", "delicious", "لذيذ", ADJECTIVE, A1, "food", "/de.li.sjø/")
            v("aujourdhui", "aujourd'hui", "today", "اليوم", ADVERB, A1, "time", "/o.ʒuʁ.dɥi/")
            v("demain", "demain", "tomorrow", "غدًا", ADVERB, A1, "time", "/də.mɛ̃/",
                ex = "À demain !", exEn = "See you tomorrow!", exAr = "إلى الغد!")
            v("hier", "hier", "yesterday", "أمس", ADVERB, A1, "time", "/jɛʁ/")
            v("semaine", "la semaine", "week", "الأسبوع", NOUN, A1, "time", "/sə.mɛn/", gender = FEMININE)
            v("gare", "la gare", "train station", "محطة القطار", NOUN, A2, "travel", "/ɡaʁ/", gender = FEMININE,
                ex = "Où est la gare ?", exEn = "Where is the station?", exAr = "أين محطة القطار؟")
            v("billet", "le billet", "ticket", "التذكرة", NOUN, A2, "travel", "/bi.jɛ/", gender = MASCULINE)
            v("gauche", "à gauche", "on the left", "على اليسار", PHRASE, A1, "travel", "/a ɡoʃ/")
            v("droite", "à droite", "on the right", "على اليمين", PHRASE, A1, "travel", "/a dʁwat/")
            v("tout_droit", "tout droit", "straight on", "إلى الأمام مباشرة", PHRASE, A2, "travel", "/tu dʁwa/")
            v("se_lever", "se lever", "to get up", "ينهض", VERB, A2, "routine", "/sə lə.ve/",
                ex = "Je me lève à sept heures.", exEn = "I get up at seven.", exAr = "أنهض في السابعة.")
            v("travailler", "travailler", "to work", "يعمل", VERB, A1, "routine", "/tʁa.va.je/")
            v("etudier", "étudier", "to study", "يدرس", VERB, A1, "routine", "/e.ty.dje/")
            v("dhabitude", "d'habitude", "usually", "عادةً", ADVERB, A2, "routine", "/da.bi.tyd/")
            v("fatigue", "fatigué / fatiguée", "tired", "متعب / متعبة", ADJECTIVE, A1, "feelings", "/fa.ti.ɡe/")
            v("content", "content / contente", "happy, pleased", "مسرور / مسرورة", ADJECTIVE, A1, "feelings", "/kɔ̃.tɑ̃/")
            v("je_pense", "je pense que", "I think that", "أعتقد أنّ", PHRASE, B1, "feelings", "/ʒə pɑ̃s kə/",
                ex = "Je pense que c'est une bonne idée.", exEn = "I think it's a good idea.", exAr = "أعتقد أنها فكرة جيدة.")
            v("bien_que", "bien que", "although (+ subjunctive)", "على الرغم من أنّ", PHRASE, B2, "feelings", "/bjɛ̃ kə/")
            v("combien", "c'est combien ?", "how much is it?", "بكم؟", PHRASE, A1, "shopping", "/sɛ kɔ̃.bjɛ̃/")
            v("cher", "cher / chère", "expensive; dear", "غالٍ؛ عزيز", ADJECTIVE, A1, "shopping", "/ʃɛʁ/")
            v("pas_cher", "pas cher", "cheap (lit. not expensive)", "رخيص (حرفيًا: ليس غاليًا)", PHRASE, A1, "shopping", "/pa ʃɛʁ/")
        }
        val vocabulary = vb.items
        val f = LessonFactory(L, vocabulary)

        val grammar = listOf(
            grammar("etre_avoir", L, A1, g("Être and avoir", "‏être و avoir"),
                g("Être (to be): je suis, tu es, il/elle est, nous sommes, vous êtes, ils sont. Avoir (to have): j'ai, tu as, il a… French uses avoir for age: J'ai 20 ans.",
                    "‏être (يكون): je suis وtu es وil/elle est وnous sommes وvous êtes وils sont. ‏avoir (يملك): j'ai وtu as وil a… وتُستخدم avoir للعمر: J'ai 20 ans."),
                listOf(ex("Je suis étudiant.", "I am a student.", "أنا طالب."), ex("J'ai vingt ans.", "I am twenty.", "عمري عشرون عامًا."),
                    ex("Je suis vingt ans.", "Age uses avoir", "العمر يُعبَّر عنه بـ avoir", wrong = true)),
                listOf(g("✗ Je suis 25 ans → ✓ J'ai 25 ans.", "✗ Je suis 25 ans ← ✓ J'ai 25 ans."))),
            grammar("gender", L, A1, g("Gender, articles and elision", "الجنس وأدوات التعريف والإدغام"),
                g("Le (m.), la (f.), les (pl.). Before a vowel or silent h, le/la become l': l'ami, l'eau. Adjectives agree: un ami content, une amie contente.",
                    "‏le للمذكر وla للمؤنث وles للجمع. وقبل حرف علّة أو h صامتة تصبح l': ‏l'ami وl'eau. وتتطابق الصفات: un ami content وune amie contente."),
                listOf(ex("la gare", "the station", "المحطة"), ex("l'eau", "the water", "الماء"), ex("le eau", "Missing elision", "ينقصها الإدغام", wrong = true)),
                listOf(g("✗ le ami → ✓ l'ami.", "✗ le ami ← ✓ l'ami."))),
            grammar("present_er", L, A1, g("Present tense of -er verbs", "مضارع أفعال ‎-er"),
                g("travailler → je travaille, tu travailles, il travaille, nous travaillons, vous travaillez, ils travaillent. The -e, -es and -ent endings are silent.",
                    "travailler ← je travaille وtu travailles وil travaille وnous travaillons وvous travaillez وils travaillent. النهايات ‎-e و‎-es و‎-ent لا تُنطق."),
                listOf(ex("Je travaille à Lyon.", "I work in Lyon.", "أعمل في ليون."), ex("Ils travaillent beaucoup.", "They work a lot.", "يعملون كثيرًا.")),
                listOf(g("Pronouncing the silent -ent: travaillent sounds like travaille.", "نطق ‎-ent الصامتة: travaillent تُنطق مثل travaille."))),
            grammar("politeness", L, A2, g("Polite requests with je voudrais", "الطلب المهذب بـ je voudrais"),
                g("Je voudrais… is the polite form of je veux. Add s'il vous plaît (formal) or s'il te plaît (informal).",
                    "‏Je voudrais… هي الصيغة المهذبة لـ je veux. وأضف s'il vous plaît (رسمي) أو s'il te plaît (غير رسمي)."),
                listOf(ex("Je voudrais l'addition, s'il vous plaît.", "I'd like the bill, please.", "أودّ الحساب من فضلك.")),
                listOf(g("'Je veux' can sound demanding to a waiter.", "قد تبدو je veux متطلّبة عند مخاطبة النادل."))),
            grammar("passe_compose", L, B1, g("Passé composé", "الماضي المركّب (passé composé)"),
                g("avoir/être + past participle: j'ai mangé, je suis allé(e). Verbs of movement (aller, venir, partir…) and reflexive verbs use être.",
                    "‏avoir أو être + اسم المفعول: j'ai mangé وje suis allé(e). أفعال الحركة (aller وvenir وpartir…) والأفعال الانعكاسية تُستخدم مع être."),
                listOf(ex("Hier, j'ai travaillé.", "Yesterday I worked.", "أمس عملت."), ex("Elle est partie à huit heures.", "She left at eight.", "غادرت في الثامنة."),
                    ex("J'ai allé au cinéma.", "aller uses être", "الفعل aller يُستخدم مع être", wrong = true)),
                listOf(g("✗ J'ai allé → ✓ Je suis allé(e).", "✗ J'ai allé ← ✓ Je suis allé(e)."))),
        )

        val lessons = listOf(
            f.lesson("salutations", A1, "basics", g("Greetings", "التحيات"), g("Say hello and introduce yourself.", "ألقِ التحية وعرّف بنفسك."),
                Skill.SPEAKING, listOf("bonjour", "bonsoir", "merci", "svp", "pardon", "au_revoir", "je_mappelle", "enchante"), "etre_avoir",
                extra = listOf(
                    fill("s1", L, "Je ___ étudiante.", listOf("suis")),
                    fill("s2", L, "J'___ vingt-deux ans.", listOf("ai")),
                ), minutes = 6),
            f.lesson("famille", A1, "basics", g("Family", "العائلة"), g("Talk about your family with le, la and l'.", "تحدّث عن عائلتك باستخدام le وla وl'."),
                Skill.VOCABULARY, listOf("famille", "mere", "pere", "frere", "soeur", "ami"), "gender",
                extra = listOf(choice("f1", L, "___ amie de Paul s'appelle Léa.", listOf("L'", "La", "Le"), 0, explanation = g("Elision before a vowel.", "إدغام قبل حرف علّة."))),
                prerequisites = listOf("salutations")),
            f.lesson("cafe", A1, "everyday", g("At the café", "في المقهى"), g("Order politely with je voudrais.", "اطلب بأدب باستخدام je voudrais."),
                Skill.SPEAKING, listOf("eau", "cafe", "pain", "addition", "voudrais", "delicieux"), "politeness",
                extra = listOf(
                    order("c1", L, listOf("Je", "voudrais", "un", "café,", "s'il", "vous", "plaît"), g("I'd like a coffee, please.", "أودّ قهوة من فضلك.")),
                    dictation("c2", L, "L'addition, s'il vous plaît.", listOf("L'addition s'il vous plaît")),
                ), prerequisites = listOf("famille")),
            f.lesson("routine", A2, "everyday", g("Daily routine", "الروتين اليومي"), g("Describe your day with -er verbs.", "صِف يومك بأفعال ‎-er."),
                Skill.WRITING, listOf("se_lever", "travailler", "etudier", "dhabitude", "aujourdhui", "demain", "semaine"), "present_er",
                extra = listOf(
                    fill("r1", L, "D'habitude, je ___ (travailler) le matin.", listOf("travaille")),
                    fill("r2", L, "Nous ___ (étudier) le français.", listOf("étudions")),
                ), prerequisites = listOf("cafe")),
            f.lesson("voyage", A2, "travel", g("Getting around", "التنقّل"), g("Ask for directions and buy tickets.", "اسأل عن الاتجاهات واشترِ التذاكر."),
                Skill.LISTENING, listOf("gare", "billet", "gauche", "droite", "tout_droit"), "gender",
                extra = listOf(dictation("v1", L, "Allez tout droit, puis tournez à gauche.", listOf("Allez tout droit puis tournez à gauche"))),
                prerequisites = listOf("routine")),
            f.lesson("achats", A2, "travel", g("Shopping", "التسوّق"), g("Ask prices and give opinions.", "اسأل عن الأسعار وعبّر عن رأيك."),
                Skill.SPEAKING, listOf("combien", "cher", "pas_cher", "content", "fatigue"), "gender",
                extra = listOf(fill("a1", L, "Cette robe est trop ___ (cher).", listOf("chère"))),
                prerequisites = listOf("voyage")),
            f.lesson("passe", B1, "travel", g("Talking about the past", "الحديث عن الماضي"), g("Use the passé composé and give opinions.", "استخدم الماضي المركّب وعبّر عن آرائك."),
                Skill.SPEAKING, listOf("hier", "je_pense", "bien_que", "content"), "passe_compose",
                extra = listOf(
                    fill("p1", L, "Hier, je ___ allée au cinéma.", listOf("suis")),
                    fill("p2", L, "Nous ___ mangé au restaurant.", listOf("avons")),
                ), prerequisites = listOf("achats")),
        )
        val units = listOf(
            CourseUnit("fr_u_basics", L, A1, g("First steps", "الخطوات الأولى"), g("Greetings and family", "التحيات والعائلة"), listOf("fr_l_salutations", "fr_l_famille")),
            CourseUnit("fr_u_everyday", L, A1, g("Everyday life", "الحياة اليومية"), g("Café and routines", "المقهى والروتين"), listOf("fr_l_cafe", "fr_l_routine")),
            CourseUnit("fr_u_travel", L, A2, g("Out and about", "في الخارج"), g("Travel, shopping and the past", "السفر والتسوق والماضي"), listOf("fr_l_voyage", "fr_l_achats", "fr_l_passe")),
        )
        val readings = listOf(
            ReadingPassage(
                "fr_r_karim", L, A1, "La journée de Karim", g("Karim's day", "يوم كريم"),
                listOf("Karim se lève à sept heures.", "Il prend un café et du pain.", "Il travaille dans une banque.", "Le soir, il étudie l'anglais.", "Il est fatigué, mais content."),
                listOf(g("Karim gets up at seven.", "ينهض كريم في السابعة."), g("He has a coffee and some bread.", "يتناول قهوة وبعض الخبز."),
                    g("He works in a bank.", "يعمل في بنك."), g("In the evening he studies English.", "في المساء يدرس الإنجليزية."), g("He is tired, but happy.", "هو متعب لكنه مسرور.")),
                listOf("fr_se_lever", "fr_cafe", "fr_pain", "fr_travailler", "fr_etudier", "fr_fatigue", "fr_content"),
                listOf(
                    Exercise.MultipleChoice("fr_r_karim_q1", Skill.READING, Instr.choose, "Où travaille Karim ?", listOf("Dans une gare", "Dans une banque", "Dans un café"), 1),
                    Exercise.MultipleChoice("fr_r_karim_q2", Skill.READING, Instr.choose, "Qu'est-ce qu'il étudie le soir ?", listOf("L'anglais", "Le français", "L'espagnol"), 0),
                ),
            ),
        )
        val scenarios = listOf(
            RolePlayScenario(
                "fr_s_boulangerie", L, A1, g("At the bakery", "في المخبز"), g("A bakery in Paris in the morning.", "مخبز في باريس صباحًا."),
                g("Buy bread and a pastry and pay.", "اشترِ خبزًا ومعجّنة وادفع."),
                "Bonjour ! Qu'est-ce que je vous sers ?", g("Hello! What can I get you?", "مرحبًا! ماذا أقدّم لك؟"),
                listOf(
                    ScenarioBeat(listOf("baguette", "pain", "voudrais", "croissant"), g("Say: 'Je voudrais une baguette, s'il vous plaît.'", "قل: Je voudrais une baguette, s'il vous plaît."),
                        "Je voudrais une baguette, s'il vous plaît.", "Très bien. Et avec ceci ?", g("Very good. Anything else?", "جيد جدًا. وماذا أيضًا؟")),
                    ScenarioBeat(listOf("croissant", "pain au chocolat", "c'est tout", "tout", "rien"), g("Add a croissant or say 'C'est tout, merci.'", "أضف كرواسون أو قل C'est tout, merci."),
                        "Un croissant, s'il vous plaît.", "Voilà. Ça fera deux euros quarante.", g("Here you are. That'll be two euros forty.", "تفضّل. المبلغ يوروان وأربعون سنتًا.")),
                    ScenarioBeat(listOf("merci", "voilà", "au revoir"), g("Pay and say thank you.", "ادفع وقل شكرًا."),
                        "Voilà. Merci, au revoir !", "Merci à vous, bonne journée !", g("Thank you, have a nice day!", "شكرًا لك، يومًا سعيدًا!")),
                ),
                listOf("Je voudrais…", "C'est tout, merci.", "Ça fait combien ?"),
            ),
        )
        fun item(id: String, level: CefrLevel, skill: Skill, prompt: String, options: List<String>, correct: Int) =
            AssessmentItem("fr_a_$id", L, level, skill, Exercise.MultipleChoice("fr_a_$id", skill, Instr.choose, prompt, options, correct))
        val assessment = listOf(
            item("a1_1", A1, Skill.GRAMMAR, "Je ___ français.", listOf("suis", "ai", "est"), 0),
            item("a1_2", A1, Skill.GRAMMAR, "J'___ trente ans.", listOf("suis", "ai", "es"), 1),
            item("a1_3", A1, Skill.VOCABULARY, "'Merci' veut dire…", listOf("please", "thank you", "sorry"), 1),
            item("a2_1", A2, Skill.GRAMMAR, "Nous ___ (parler) français.", listOf("parlons", "parlez", "parlent"), 0),
            item("a2_2", A2, Skill.GRAMMAR, "___ eau est froide.", listOf("Le", "La", "L'"), 2),
            item("a2_3", A2, Skill.VOCABULARY, "Au restaurant, pour payer on demande…", listOf("la carte", "l'addition", "le billet"), 1),
            item("b1_1", B1, Skill.GRAMMAR, "Hier, elle ___ allée à Paris.", listOf("a", "est", "était"), 1),
            item("b1_2", B1, Skill.GRAMMAR, "Quand j'étais petit, je ___ au parc tous les jours.", listOf("suis allé", "allais", "irai"), 1),
            item("b1_3", B1, Skill.GRAMMAR, "Je ne ___ jamais allé en Chine.", listOf("suis", "ai", "était"), 0),
            item("b2_1", B2, Skill.GRAMMAR, "Il faut que tu ___ tes devoirs.", listOf("fais", "fasses", "feras"), 1),
            item("b2_2", B2, Skill.GRAMMAR, "Si j'avais le temps, je ___ plus.", listOf("lis", "lirais", "lirai"), 1),
            item("b2_3", B2, Skill.GRAMMAR, "Bien qu'il ___ tard, il continue.", listOf("est", "soit", "sera"), 1),
        )
        return LanguageCurriculum(L, vocabulary, grammar, units, lessons, readings, scenarios, assessment,
            dictations = listOf(
                dictation("d1", L, "Je m'appelle Karim.", listOf("Je m'appelle Karim")),
                dictation("d2", L, "Où est la gare ?", listOf("Où est la gare")),
                dictation("d3", L, "D'habitude, je travaille le matin.", listOf("D'habitude je travaille le matin")),
            ),
            writingPrompts = listOf(writing("wp1", L, g("Describe your weekend in French.", "صِف عطلة نهاية الأسبوع بالفرنسية."), 30, listOf("samedi", "dimanche", "j'ai"))),
        )
    }
}
