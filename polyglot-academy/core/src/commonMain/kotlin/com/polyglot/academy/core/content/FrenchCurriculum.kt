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
import com.polyglot.academy.core.model.VocabularySource
import com.polyglot.academy.core.model.LearningTrack
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

            // Home and weather
            v("maison", "la maison", "house, home", "البيت", NOUN, A1, "home", gender = FEMININE,
                ex = "Ma maison est petite mais jolie.", exEn = "My house is small but pretty.", exAr = "بيتي صغير لكنه جميل.")
            v("cuisine", "la cuisine", "kitchen; cooking", "المطبخ؛ الطبخ", NOUN, A1, "home", gender = FEMININE)
            v("chambre", "la chambre", "bedroom", "غرفة النوم", NOUN, A1, "home", gender = FEMININE)
            v("j_aime", "j'aime", "I like, I love", "أحبّ", VERB, A1, "home",
                ex = "J'aime beaucoup les jours de soleil.", exEn = "I really like sunny days.", exAr = "أحبّ الأيام المشمسة كثيرًا.")
            v("meteo", "la météo", "weather forecast", "النشرة الجوية", NOUN, A2, "weather", gender = FEMININE)
            v("il_fait_chaud", "il fait chaud", "it's hot", "الجو حار", PHRASE, A1, "weather")
            v("il_fait_froid", "il fait froid", "it's cold", "الجو بارد", PHRASE, A1, "weather",
                ex = "En janvier, il fait très froid.", exEn = "In January it's very cold.", exAr = "في يناير يكون الجو باردًا جدًا.")
            v("il_pleut", "il pleut", "it's raining", "إنها تمطر", PHRASE, A1, "weather")
            // Health
            v("medecin", "le médecin", "doctor", "الطبيب", NOUN, A1, "health", gender = MASCULINE)
            v("j_ai_mal", "j'ai mal à…", "my … hurts", "يؤلمني…", PHRASE, A2, "health",
                ex = "J'ai mal à la tête.", exEn = "I have a headache.", exAr = "رأسي يؤلمني.")
            v("tete", "la tête", "head", "الرأس", NOUN, A1, "health", gender = FEMININE)
            v("fievre", "la fièvre", "fever", "الحمّى", NOUN, A2, "health", gender = FEMININE)
            v("pharmacie", "la pharmacie", "pharmacy", "الصيدلية", NOUN, A1, "health", gender = FEMININE)
            v("rendez_vous", "le rendez-vous", "appointment", "الموعد", NOUN, A2, "health", gender = MASCULINE,
                ex = "Je voudrais prendre rendez-vous avec le médecin.", exEn = "I'd like to make an appointment with the doctor.", exAr = "أودّ حجز موعد مع الطبيب.")
            v("malade", "malade", "ill, sick", "مريض", ADJECTIVE, A1, "health")
            // Work (professional track)
            v("reunion", "la réunion", "meeting", "الاجتماع", NOUN, A2, "business", gender = FEMININE,
                ex = "La réunion commence à dix heures.", exEn = "The meeting starts at ten.", exAr = "يبدأ الاجتماع في العاشرة.", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("entreprise", "l'entreprise", "company", "الشركة", NOUN, A2, "business", gender = FEMININE, tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("client", "le client / la cliente", "client, customer", "العميل / العميلة", NOUN, A2, "business", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("devis", "le devis", "quote, estimate", "عرض السعر", NOUN, B1, "business", gender = MASCULINE, tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("delai", "le délai", "deadline; time allowed", "المهلة", NOUN, B1, "business", gender = MASCULINE,
                ex = "Le délai est très court.", exEn = "The deadline is very tight.", exAr = "المهلة قصيرة جدًا.", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("proposition", "la proposition", "proposal", "المقترح", NOUN, B1, "business", gender = FEMININE, tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("ci_joint", "veuillez trouver ci-joint…", "please find attached… (formal email)", "تجدون مرفقًا…", PHRASE, B1, "business", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("madame_monsieur", "Madame, Monsieur,", "Dear Sir or Madam, (formal letter opening)", "سيدتي، سيدي، (افتتاحية رسمية)", PHRASE, B1, "business", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)

            // Next steps: plans and wishes
            v("projet", "le projet", "plan, project", "المشروع، الخطة", NOUN, B1, "plans", gender = MASCULINE)
            v("aller_inf", "aller + infinitif", "to be going to (do)", "سوف (لخطة قريبة)", PHRASE, A2, "plans",
                ex = "Je vais étudier ce soir.", exEn = "I'm going to study tonight.", exAr = "سأدرس هذا المساء.")
            v("prochain", "l'année prochaine / la semaine prochaine", "next year / next week", "العام القادم / الأسبوع القادم", PHRASE, A2, "plans")
            v("voyager", "voyager", "to travel", "يسافر", VERB, A2, "plans")
            v("economiser", "économiser", "to save (money)", "يدّخر", VERB, B1, "plans")
            v("peut_etre", "peut-être", "maybe, perhaps", "ربما", ADVERB, A2, "plans")
            v("vacances", "les vacances", "holidays (feminine plural)", "العطلة", NOUN, A2, "plans", gender = FEMININE)
            v("il_faut_que", "il faut que…", "it is necessary that… (+ subjunctive)", "يجب أن…", PHRASE, B2, "wishes",
                ex = "Il faut que tu viennes.", exEn = "You have to come.", exAr = "يجب أن تأتي.")
            v("je_veux_que", "je veux que…", "I want (someone) to…", "أريد أن (يفعل شخص آخر)…", PHRASE, B2, "wishes")
            v("j_espere_que", "j'espère que…", "I hope that… (+ indicative)", "آمل أن…", PHRASE, B1, "wishes")
            v("chance", "la chance", "luck", "الحظ", NOUN, B1, "wishes", gender = FEMININE)
            v("reussir", "réussir", "to succeed; to pass (an exam)", "ينجح", VERB, B1, "wishes")
            v("examen", "l'examen", "exam", "الامتحان", NOUN, A2, "wishes", gender = MASCULINE)

            // Banking and finance (professional track)
            v("fin_compte", "le compte bancaire", "bank account", "الحساب المصرفي", NOUN, A2, "finance", gender = MASCULINE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_pret", "le prêt", "loan", "القرض", NOUN, B1, "finance", gender = MASCULINE,
                ex = "La banque a accordé le prêt.", exEn = "The bank granted the loan.", exAr = "منح البنك القرض.", tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_taux", "le taux d'intérêt", "interest rate", "سعر الفائدة", NOUN, B1, "finance", gender = MASCULINE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_mensualite", "la mensualité", "monthly instalment", "القسط الشهري", NOUN, B1, "finance", gender = FEMININE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_garantie", "la garantie", "collateral, guarantee", "الضمان", NOUN, B2, "finance", gender = FEMININE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_defaut", "le défaut de paiement", "default (failure to pay)", "التعثّر عن السداد", NOUN, B2, "finance", gender = MASCULINE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_bilan", "le bilan", "balance sheet", "الميزانية العمومية", NOUN, B2, "finance", gender = MASCULINE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_liquidite", "la liquidité", "liquidity", "السيولة", NOUN, B2, "finance", gender = FEMININE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
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

            grammar("il_fait", L, A1, g("Weather: il fait, il pleut", "الطقس: il fait وil pleut"),
                g("French uses the impersonal il for weather: il fait chaud / froid / beau, il pleut, il neige. The il does not refer to a person.",
                    "تستخدم الفرنسية الضمير il غير الشخصي للطقس: il fait chaud / froid / beau وil pleut وil neige، ولا يشير il إلى شخص."),
                listOf(ex("Il fait beau aujourd'hui.", "The weather is nice today.", "الجو جميل اليوم."), ex("Il pleut depuis ce matin.", "It's been raining since this morning.", "إنها تمطر منذ الصباح."),
                    ex("C'est froid aujourd'hui.", "For the weather say 'il fait froid'", "للطقس قل il fait froid", wrong = true)),
                listOf(g("✗ Il est chaud (about the weather) → ✓ Il fait chaud.", "✗ Il est chaud (عن الطقس) ← ✓ Il fait chaud."))),
            grammar("vous", L, A2, g("Formal 'you': vous", "المخاطبة الرسمية: vous"),
                g("Use vous with clients, officials and people you don't know, even for one person. Verbs take the vous form: Pouvez-vous m'envoyer le devis ?",
                    "استخدم vous مع العملاء والموظفين ومن لا تعرفهم، حتى لو كان شخصًا واحدًا. ويُصرَّف الفعل مع vous: Pouvez-vous m'envoyer le devis ?"),
                listOf(ex("Pourriez-vous confirmer la date ?", "Could you confirm the date?", "هل يمكنكم تأكيد الموعد؟"),
                    ex("Vous peux venir ?", "vous takes 'pouvez'", "vous يأخذ pouvez", wrong = true)),
                listOf(g("Switching from vous to tu with a client can seem too familiar.", "الانتقال من vous إلى tu مع عميل قد يبدو تبسّطًا زائدًا."))),
            grammar("futur", L, B1, g("Talking about the future", "الحديث عن المستقبل"),
                g("The futur proche (aller + infinitive) is for plans and the near future: Je vais voyager. The futur simple (infinitive + -ai, -as, -a, -ons, -ez, -ont) is for predictions and promises: Il pleuvra demain. Je t'appellerai.",
                    "يُستخدم futur proche (aller + المصدر) للخطط والمستقبل القريب: Je vais voyager. ويُستخدم futur simple (المصدر + ‎-ai و‎-as وغيرهما) للتوقّعات والوعود: Il pleuvra demain. Je t'appellerai."),
                listOf(ex("Nous allons dîner à la maison.", "We're going to have dinner at home.", "سنتناول العشاء في البيت."),
                    ex("L'année prochaine, j'habiterai à Paris.", "Next year I'll live in Paris.", "في العام القادم سأسكن في باريس."),
                    ex("Je vais voyage.", "After aller use the infinitive: je vais voyager", "بعد aller يأتي المصدر: je vais voyager", wrong = true)),
                listOf(g("Irregular stems to learn: être → ser-, avoir → aur-, aller → ir-, faire → fer-.", "جذور شاذة يجب حفظها: être ← ser- وavoir ← aur- وaller ← ir- وfaire ← fer-."))),
            grammar("subjonctif", L, B2, g("The subjunctive after il faut que", "صيغة subjonctif بعد il faut que"),
                g("Use the subjunctive after il faut que, je veux que, pour que and bien que: Il faut que tu viennes. Take the ils-form stem and add -e, -es, -e, -ions, -iez, -ent (ils parlent → que je parle). Learn the irregulars: être (sois), avoir (aies), faire (fasse), aller (aille). Espérer que takes the indicative.",
                    "تُستخدم subjonctif بعد il faut que وje veux que وpour que وbien que: Il faut que tu viennes. خذ جذر صيغة ils وأضف ‎-e و‎-es و‎-e و‎-ions و‎-iez و‎-ent (ils parlent ← que je parle). واحفظ الشواذ: être (sois) وavoir (aies) وfaire (fasse) وaller (aille). أما espérer que فيليها الإخبار."),
                listOf(ex("Il faut que nous partions.", "We have to leave.", "يجب أن نغادر."),
                    ex("Je veux que tu sois heureux.", "I want you to be happy.", "أريدك أن تكون سعيدًا."),
                    ex("Il faut que tu fais attention.", "Use the subjunctive: que tu fasses", "استخدم subjonctif: que tu fasses", wrong = true)),
                listOf(g("✗ J'espère qu'il vienne → ✓ J'espère qu'il viendra (indicative).", "✗ J'espère qu'il vienne ← ✓ J'espère qu'il viendra (صيغة الإخبار)."))),
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

            f.lesson("maison", A2, "home", g("Home and weather", "البيت والطقس"), g("Describe your home and talk about the weather.", "صِف بيتك وتحدّث عن الطقس."),
                Skill.VOCABULARY, listOf("maison", "cuisine", "chambre", "j_aime", "meteo", "il_fait_chaud", "il_fait_froid", "il_pleut"), "il_fait",
                extra = listOf(
                    fill("m1", L, "En hiver, il fait ___.", listOf("froid")),
                    fill("m2", L, "Prends ton parapluie, il ___ !", listOf("pleut")),
                    order("m3", L, listOf("J'aime", "beaucoup", "ma", "maison"), g("I really like my house.", "أحبّ بيتي كثيرًا.")),
                ), prerequisites = listOf("passe")),
            f.lesson("sante", A2, "home", g("At the doctor's", "عند الطبيب"), g("Say what hurts and make an appointment.", "قل ما يؤلمك واحجز موعدًا."),
                Skill.SPEAKING, listOf("medecin", "j_ai_mal", "tete", "fievre", "pharmacie", "rendez_vous", "malade"), "vous",
                extra = listOf(
                    fill("sa1", L, "J'ai mal à la ___.", listOf("tête", "tete")),
                    fill("sa2", L, "Je voudrais prendre ___ avec le médecin.", listOf("rendez-vous")),
                    dictation("sa3", L, "J'ai de la fièvre depuis hier.", listOf("J'ai de la fièvre depuis hier")),
                ), prerequisites = listOf("maison")),
            f.lesson("travail", B1, "pro_business", g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), g("Quotes, deadlines and polite requests with vous.", "عروض الأسعار والمواعيد النهائية والطلبات المهذبة بصيغة vous."),
                Skill.WRITING, listOf("reunion", "entreprise", "client", "devis", "delai", "proposition", "ci_joint", "madame_monsieur"), "vous",
                extra = listOf(
                    fill("t1", L, "Veuillez trouver ___ le devis.", listOf("ci-joint")),
                    choice("t2", L, "Which is the right way to start a formal email?", listOf("Salut !", "Madame, Monsieur,", "Coucou !"), 1, Skill.FUNCTIONAL),
                    fill("t3", L, "___-vous confirmer la date de la réunion ? (pouvoir)", listOf("Pouvez", "Pourriez")),
                    writing("t4", L, g("Write a short email to a client proposing a meeting next week.", "اكتب رسالة قصيرة إلى عميل تقترح فيها اجتماعًا الأسبوع القادم."), 40,
                        listOf("Madame, Monsieur", "réunion", "proposition", "Cordialement")),
                )),
            f.lesson("projets", B1, "next", g("Plans for the future", "خطط المستقبل"), g("Talk about plans with aller + infinitive and the futur simple.", "تحدّث عن الخطط باستخدام aller + المصدر والمستقبل البسيط."),
                Skill.SPEAKING, listOf("projet", "aller_inf", "prochain", "voyager", "economiser", "peut_etre", "vacances"), "futur",
                extra = listOf(
                    fill("pr1", L, "L'année prochaine, je ___ voyager au Canada. (aller)", listOf("vais")),
                    fill("pr2", L, "Demain, il ___ (pleuvoir).", listOf("pleuvra")),
                    order("pr3", L, listOf("Nous", "allons", "économiser", "pour", "les", "vacances"), g("We're going to save for the holidays.", "سنوفّر المال للعطلة.")),
                    writing("pr4", L, g("Write about your plans for next year (30+ words).", "اكتب عن خططك للعام القادم (30 كلمة على الأقل)."), 30, listOf("je vais", "l'année prochaine", "peut-être")),
                ), prerequisites = listOf("sante")),
            f.lesson("souhaits", B2, "next", g("Needs and wishes", "الضرورات والأمنيات"), g("Use the subjunctive after il faut que and je veux que.", "استخدم subjonctif بعد il faut que وje veux que."),
                Skill.GRAMMAR, listOf("il_faut_que", "je_veux_que", "j_espere_que", "chance", "reussir", "examen"), "subjonctif",
                extra = listOf(
                    fill("so1", L, "Il faut que tu ___ (faire) tes devoirs.", listOf("fasses")),
                    fill("so2", L, "Je veux que vous ___ (venir) demain.", listOf("veniez")),
                    choice("so3", L, "J'espère qu'il ___ beau demain.", listOf("fasse", "fera", "fait"), 1,
                        explanation = g("Espérer que takes the indicative (here the future).", "يأتي بعد espérer que الفعل في صيغة الإخبار (هنا المستقبل).")),
                    translate("so4", L, g("I want you to succeed.", "أريدك أن تنجح."), listOf("Je veux que tu réussisses", "Je veux que vous réussissiez")),
                ), prerequisites = listOf("projets")),
            f.lesson("finance", B1, "pro_finance", g("Banking basics", "أساسيات البنوك"), g("Accounts, loans, interest and instalments.", "الحسابات والقروض والفوائد والأقساط."),
                Skill.VOCABULARY, listOf("fin_compte", "fin_pret", "fin_taux", "fin_mensualite", "fin_garantie", "fin_defaut", "fin_bilan", "fin_liquidite"),
                extra = listOf(
                    fill("fi1", L, "La banque a accordé un ___ à l'entreprise.", listOf("prêt", "pret")),
                    fill("fi2", L, "Le ___ d'intérêt est de 5 %.", listOf("taux")),
                    choice("fi3", L, "Which document shows assets and liabilities?", listOf("le bilan", "le devis", "le reçu"), 0, Skill.VOCABULARY),
                )),
        )
        val units = listOf(
            CourseUnit("fr_u_basics", L, A1, g("First steps", "الخطوات الأولى"), g("Greetings and family", "التحيات والعائلة"), listOf("fr_l_salutations", "fr_l_famille")),
            CourseUnit("fr_u_everyday", L, A1, g("Everyday life", "الحياة اليومية"), g("Café and routines", "المقهى والروتين"), listOf("fr_l_cafe", "fr_l_routine")),
            CourseUnit("fr_u_travel", L, A2, g("Out and about", "في الخارج"), g("Travel, shopping and the past", "السفر والتسوق والماضي"), listOf("fr_l_voyage", "fr_l_achats", "fr_l_passe")),

            CourseUnit("fr_u_home", L, A2, g("Home and health", "البيت والصحة"), g("Your home, the weather and the doctor", "بيتك والطقس والطبيب"), listOf("fr_l_maison", "fr_l_sante")),
            CourseUnit("fr_u_pro_business", L, B1, g("French for work", "الفرنسية للعمل"), g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), listOf("fr_l_travail"), trackId = "business"),
            CourseUnit("fr_u_next", L, B1, g("Next steps", "الخطوة التالية"), g("Plans, needs and wishes", "الخطط والضرورات والأمنيات"), listOf("fr_l_projets", "fr_l_souhaits")),
            CourseUnit("fr_u_pro_finance", L, B1, g("Banking and finance", "البنوك والتمويل"), g("Core banking vocabulary", "مفردات البنوك الأساسية"), listOf("fr_l_finance"), trackId = "finance"),
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

            RolePlayScenario(
                "fr_s_medecin", L, A2, g("At the doctor's", "عند الطبيب"), g("A GP's surgery in Lyon. Language practice only — not medical advice.", "عيادة طبيب في ليون. للتدرّب اللغوي فقط، وليس استشارة طبية."),
                g("Explain what hurts, say since when, and ask about the pharmacy.", "اشرح ما يؤلمك ومنذ متى، واسأل عن الصيدلية."),
                "Bonjour, asseyez-vous. Qu'est-ce qui ne va pas ?", g("Hello, please sit down. What's wrong?", "مرحبًا، تفضّل بالجلوس. ما المشكلة؟"),
                listOf(
                    ScenarioBeat(listOf("mal", "tête", "tete", "fièvre", "fievre", "malade"), g("Say what hurts: 'J'ai mal à la tête.'", "قل ما يؤلمك: J'ai mal à la tête."),
                        "J'ai mal à la tête et j'ai de la fièvre.", "Depuis quand ?", g("Since when?", "منذ متى؟")),
                    ScenarioBeat(listOf("depuis", "hier", "jours", "semaine"), g("Say since when: 'Depuis hier.'", "قل منذ متى: Depuis hier."),
                        "Depuis hier soir.", "D'accord. Reposez-vous et buvez beaucoup d'eau.", g("All right. Rest and drink plenty of water.", "حسنًا. استرح واشرب الكثير من الماء.")),
                    ScenarioBeat(listOf("pharmacie", "où", "ou", "ordonnance"), g("Ask where the pharmacy is.", "اسأل أين الصيدلية."),
                        "Où est la pharmacie la plus proche ?", "Juste à côté, sur la droite.", g("Right next door, on the right.", "بجانبنا مباشرة، على اليمين.")),
                ),
                listOf("J'ai mal à…", "J'ai de la fièvre.", "Depuis quand ?", "Où est la pharmacie ?"),
            ),
            RolePlayScenario(
                "fr_s_reunion", L, B1, g("Planning a project with a client", "التخطيط لمشروع مع عميل"), g("A video call with a client in Montréal.", "مكالمة فيديو مع عميل في مونتريال."),
                g("Agree on the quote, the deadline and the next step.", "اتفق على عرض السعر والموعد النهائي والخطوة التالية."),
                "Bonjour. Merci pour votre proposition. On parle du devis ?", g("Hello. Thank you for your proposal. Shall we discuss the quote?", "مرحبًا. شكرًا على مقترحكم. هل نناقش عرض السعر؟"),
                listOf(
                    ScenarioBeat(listOf("devis", "prix", "euros", "dollars", "coût", "cout"), g("Explain the quote.", "اشرح عرض السعر."),
                        "Bien sûr. Le devis s'élève à cinq mille dollars.", "Très bien. Et le délai ?", g("Very good. And the deadline?", "جيد جدًا. وماذا عن المهلة؟")),
                    ScenarioBeat(listOf("délai", "delai", "semaines", "mois", "date", "vendredi"), g("Give a deadline: 'dans six semaines'.", "حدّد موعدًا: dans six semaines."),
                        "Nous pouvons livrer dans six semaines.", "Cela nous convient.", g("That works for us.", "يناسبنا ذلك.")),
                    ScenarioBeat(listOf("e-mail", "mail", "courriel", "envoyer", "envoie", "ci-joint", "contrat"), g("Say you will send a summary by email.", "قل إنك سترسل ملخصًا بالبريد الإلكتروني."),
                        "Je vous envoie un résumé par e-mail aujourd'hui.", "Merci beaucoup. À bientôt.", g("Thank you very much. Speak soon.", "شكرًا جزيلًا. إلى اللقاء قريبًا.")),
                ),
                listOf("Le devis s'élève à…", "Nous pouvons livrer dans…", "Je vous envoie…"),
                trackId = "business",
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
            tracks = listOf(LearningTrack("business", L, g("French for work", "الفرنسية للعمل"), g("Meetings, formal emails, quotes and deadlines.", "الاجتماعات والرسائل الرسمية وعروض الأسعار والمواعيد النهائية."), A2, null),
                LearningTrack("finance", L, g("French for banking and finance", "الفرنسية للبنوك والتمويل"), g("Accounts, loans, interest, collateral and financial statements.", "الحسابات والقروض والفوائد والضمانات والقوائم المالية."), B1,
                    g("This track teaches French vocabulary used in this field. It is not financial, legal or professional advice.", "يعلّم هذا المسار مفردات الفرنسية المستخدمة في هذا المجال، ولا يُعدّ استشارة مالية أو قانونية أو مهنية."))),
            dictations = listOf(
                dictation("d1", L, "Je m'appelle Karim.", listOf("Je m'appelle Karim")),
                dictation("d2", L, "Où est la gare ?", listOf("Où est la gare")),
                dictation("d3", L, "D'habitude, je travaille le matin.", listOf("D'habitude je travaille le matin")),
            ),
            writingPrompts = listOf(writing("wp1", L, g("Describe your weekend in French.", "صِف عطلة نهاية الأسبوع بالفرنسية."), 30, listOf("samedi", "dimanche", "j'ai"))),
        )
    }
}
