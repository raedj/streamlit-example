package com.polyglot.academy.core.content

import com.polyglot.academy.core.assessment.AssessmentItem
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CefrLevel.A1
import com.polyglot.academy.core.model.CefrLevel.A2
import com.polyglot.academy.core.model.CefrLevel.B1
import com.polyglot.academy.core.model.CefrLevel.B2
import com.polyglot.academy.core.model.CefrLevel.C1
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

            // Banking and finance (professional track)
            v("fin_hisab", "حساب مصرفي", "bank account", "حساب في البنك", NOUN, A2, "finance", translit = "ḥisāb maṣrifī", gender = MASCULINE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_qard", "قرض", "loan", "مال يُستدان ويُردّ", NOUN, B1, "finance", translit = "qarḍ", gender = MASCULINE,
                ex = "وافق البنك على القرض.", exEn = "The bank approved the loan.", exAr = "وافق البنك على القرض.", tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_faida", "سعر الفائدة", "interest rate", "نسبة الفائدة على المال", NOUN, B1, "finance", translit = "siʿr al-fāʾida", gender = MASCULINE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_qist", "قسط", "instalment", "جزء من الدين يُدفع دوريًا", NOUN, B1, "finance", translit = "qisṭ", gender = MASCULINE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_daman", "ضمان", "collateral, guarantee", "ما يُقدَّم لتوثيق الدين", NOUN, B2, "finance", translit = "ḍamān", gender = MASCULINE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_taathur", "تعثّر عن السداد", "default (failure to repay)", "العجز عن دفع الدين", PHRASE, B2, "finance", translit = "taʿaththur ʿan as-sadād", tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_mizaniyya_umumiyya", "الميزانية العمومية", "balance sheet", "قائمة الأصول والخصوم", NOUN, B2, "finance", translit = "al-mīzāniyya al-ʿumūmiyya", gender = FEMININE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("fin_suyula", "السيولة", "liquidity", "توفر النقد", NOUN, B2, "finance", translit = "as-suyūla", gender = FEMININE, tags = setOf("finance"), source = VocabularySource.PROFESSIONAL_TRACK)
            // Credit and risk (professional track)
            v("cr_mukhatir", "مخاطر الائتمان", "credit risk", "احتمال ألّا يسدّد المقترض دينه", NOUN, B2, "credit_risk", translit = "makhāṭir al-iʾtimān", gender = FEMININE, ex = "يقيّم البنك مخاطر الائتمان لكل عميل.", exEn = "The bank assesses each customer's credit risk.", exAr = "يقيّم البنك مخاطر الائتمان لكل عميل.", tags = setOf("credit_risk"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("cr_qard_mutaaththir", "قرض متعثّر", "non-performing loan", "قرض تأخّر سداده طويلًا (عادة 90 يومًا أو أكثر)", NOUN, B2, "credit_risk", translit = "qarḍ mutaʿaththir", gender = MASCULINE, tags = setOf("credit_risk"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("cr_haykala", "إعادة الهيكلة", "restructuring", "تغيير شروط القرض لتسهيل سداده", NOUN, C1, "credit_risk", translit = "iʿādat al-haykala", gender = FEMININE, tags = setOf("credit_risk"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("cr_shatb", "شطب الدين", "write-off", "حذف دين لا يُتوقّع تحصيله من الدفاتر", NOUN, C1, "credit_risk", translit = "shaṭb ad-dayn", gender = MASCULINE, tags = setOf("credit_risk"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("cr_jadara", "الجدارة الائتمانية", "creditworthiness", "مدى قدرة المقترض على السداد", NOUN, C1, "credit_risk", translit = "al-jadāra al-iʾtimāniyya", gender = FEMININE, tags = setOf("credit_risk"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("cr_kafil", "كفيل", "guarantor", "من يتعهّد بالسداد إذا عجز المقترض", NOUN, B2, "credit_risk", translit = "kafīl", gender = MASCULINE, tags = setOf("credit_risk"), source = VocabularySource.PROFESSIONAL_TRACK)
            // Contracts (professional track)
            v("lg_aqd", "عقد", "contract", "اتفاق ملزم بين طرفين أو أكثر", NOUN, B1, "legal", translit = "ʿaqd", gender = MASCULINE, ex = "وقّع الطرفان العقد أمس.", exEn = "Both parties signed the contract yesterday.", exAr = "وقّع الطرفان العقد أمس.", tags = setOf("legal"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("lg_band", "بند", "clause", "جزء من العقد", NOUN, B2, "legal", translit = "band", gender = MASCULINE, tags = setOf("legal"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("lg_taraf", "طرف", "party (to an agreement)", "شخص أو جهة في الاتفاق", NOUN, B2, "legal", translit = "ṭaraf", gender = MASCULINE, tags = setOf("legal"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("lg_masuliyya", "المسؤولية القانونية", "legal liability", "التبعة القانونية عن فعل أو ضرر", NOUN, C1, "legal", translit = "al-masʾūliyya al-qānūniyya", gender = FEMININE, tags = setOf("legal"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("lg_ikhlal", "الإخلال بالعقد", "breach of contract", "عدم الوفاء بشروط العقد", PHRASE, C1, "legal", translit = "al-ikhlāl bil-ʿaqd", tags = setOf("legal"), source = VocabularySource.PROFESSIONAL_TRACK)
            // Daily life and travel
            v("ev_law_samaht", "لو سمحت", "excuse me / please (to get attention)", "عبارة لطلب الانتباه بأدب", PHRASE, A1, "essentials", translit = "law samaḥt", ex = "لو سمحت، هل هذا المقعد شاغر؟", exEn = "Excuse me, is this seat free?", exAr = "لو سمحت، هل هذا المقعد شاغر؟")
            v("ev_la_afham", "لا أفهم", "I don't understand", "عبارة عند عدم الفهم", PHRASE, A1, "essentials", translit = "lā afham")
            v("ev_bibut", "تكلّم ببطء من فضلك", "speak slowly, please", "طلب الإبطاء في الكلام", PHRASE, A1, "essentials", translit = "takallam bi-buṭʾ min faḍlik")
            v("ev_karrir", "هل يمكنك أن تكرّر؟", "Could you repeat that?", "طلب الإعادة", PHRASE, A1, "essentials", translit = "hal yumkinuka an tukarrir?")
            v("ev_dawrat_miyah", "أين دورة المياه؟", "Where is the toilet?", "سؤال عن مكان الحمّام", PHRASE, A1, "essentials", translit = "ayna dawrat al-miyāh?")
            v("ev_musaada", "هل يمكنك مساعدتي؟", "Can you help me?", "طلب المساعدة", PHRASE, A1, "essentials", translit = "hal yumkinuka musāʿadatī?")
            v("ev_la_aarif", "لا أعرف", "I don't know", "عبارة عند عدم المعرفة", PHRASE, A1, "essentials", translit = "lā aʿrif")
            v("ev_la_mushkila", "لا مشكلة", "no problem", "عبارة تعني لا بأس", PHRASE, A1, "essentials", translit = "lā mushkila")
            v("ev_najda", "النجدة!", "Help!", "صيحة طلب العون", PHRASE, A1, "emergency", translit = "an-najda!")
            v("ev_shurta", "الشرطة", "the police", "جهاز حفظ الأمن", NOUN, A1, "emergency", translit = "ash-shurṭa")
            v("ev_isaaf", "اتصل بالإسعاف!", "Call an ambulance!", "طلب سيارة الإسعاف", PHRASE, A2, "emergency", translit = "ittaṣil bil-isʿāf!", ex = "اتصل بالإسعاف من فضلك!", exEn = "Call an ambulance, please!", exAr = "اتصل بالإسعاف من فضلك!")
            v("ev_jawaz", "أضعتُ جواز سفري", "I've lost my passport", "الإبلاغ عن فقد الجواز", PHRASE, A2, "emergency", translit = "aḍaʿtu jawāza safarī")
            v("ev_suriqa", "سُرقت حقيبتي", "My bag has been stolen", "الإبلاغ عن سرقة", PHRASE, A2, "emergency", translit = "suriqat ḥaqībatī")
            v("ev_sifara", "السفارة", "the embassy", "مكتب الدولة في الخارج", NOUN, A2, "emergency", translit = "as-sifāra")
            v("ev_mustashfa", "المستشفى", "the hospital", "مكان علاج المرضى", NOUN, A1, "emergency", translit = "al-mustashfā")
            v("ev_raqm_tawari", "رقم الطوارئ", "emergency number", "رقم الاتصال عند الخطر", NOUN, A2, "emergency", translit = "raqm aṭ-ṭawāriʾ")
            v("ev_tawila", "طاولة لشخصين من فضلك", "a table for two, please", "طلب طاولة", PHRASE, A1, "restaurant", translit = "ṭāwila li-shakhṣayn min faḍlik", ex = "مساء الخير، طاولة لشخصين من فضلك.", exEn = "Good evening, a table for two, please.", exAr = "مساء الخير، طاولة لشخصين من فضلك.")
            v("ev_tansah", "بماذا تنصحني؟", "What do you recommend?", "طلب اقتراح", PHRASE, A2, "restaurant", translit = "bi-mādhā tanṣaḥunī?")
            v("ev_hassasiyya", "عندي حساسية من…", "I'm allergic to…", "الإخبار بحساسية من طعام", PHRASE, A2, "restaurant", translit = "ʿindī ḥassāsiyya min…")
            v("ev_nabati", "نباتي", "vegetarian", "لا يأكل اللحم", ADJECTIVE, A2, "restaurant", translit = "nabātī")
            v("ev_qaimat_taam", "قائمة الطعام", "the menu", "قائمة الأطباق", NOUN, A1, "restaurant", translit = "qāʾimat aṭ-ṭaʿām")
            v("ev_tabaq_raisi", "الطبق الرئيسي", "main course", "الطبق الأساسي في الوجبة", NOUN, A2, "restaurant", translit = "aṭ-ṭabaq ar-raʾīsī")
            v("ev_halwa", "الحلوى", "dessert", "طعام حلو بعد الوجبة", NOUN, A1, "restaurant", translit = "al-ḥalwā")
            v("ev_hisab", "الحساب من فضلك", "the bill, please", "طلب الفاتورة", PHRASE, A1, "restaurant", translit = "al-ḥisāb min faḍlik")
            v("ev_hajz", "حجز", "reservation", "ترتيب مسبق لغرفة أو طاولة", NOUN, A2, "hotel", translit = "ḥajz", ex = "عندي حجز باسم حسن.", exEn = "I have a reservation under the name Hassan.", exAr = "عندي حجز باسم حسن.")
            v("ev_ghurfa_fardiyya", "غرفة لشخص واحد", "single room", "غرفة لفرد", NOUN, A2, "hotel", translit = "ghurfa li-shakhṣ wāḥid")
            v("ev_ghurfa_muzdawaja", "غرفة مزدوجة", "double room", "غرفة لشخصين", NOUN, A2, "hotel", translit = "ghurfa muzdawaja")
            v("ev_miftah", "مفتاح", "key", "أداة فتح الباب", NOUN, A1, "hotel", translit = "miftāḥ")
            v("ev_futur", "هل الفطور مشمول؟", "Is breakfast included?", "سؤال عن الفطور في السعر", PHRASE, A2, "hotel", translit = "hal al-fuṭūr mashmūl?")
            v("ev_kalimat_murur", "ما كلمة مرور الواي فاي؟", "What's the Wi-Fi password?", "سؤال عن الاتصال بالإنترنت", PHRASE, A2, "hotel", translit = "mā kalimat murūr al-wāy fāy?")
            v("ev_istiqbal", "الاستقبال", "reception", "مكتب استقبال النزلاء", NOUN, A2, "hotel", translit = "al-istiqbāl")
            v("ev_mughadara", "موعد المغادرة", "check-out time", "وقت ترك الفندق", NOUN, A2, "hotel", translit = "mawʿid al-mughādara")
            v("ev_rasif", "الرصيف", "the platform", "مكان ركوب القطار", NOUN, A2, "transport", translit = "ar-raṣīf", ex = "يغادر القطار من الرصيف رقم أربعة.", exEn = "The train leaves from platform four.", exAr = "يغادر القطار من الرصيف رقم أربعة.")
            v("ev_dhahab_awda", "تذكرة ذهاب وعودة", "a return ticket", "تذكرة للاتجاهين", NOUN, A2, "transport", translit = "tadhkarat dhahāb wa-ʿawda")
            v("ev_dhahab_faqat", "تذكرة ذهاب فقط", "a single (one-way) ticket", "تذكرة لاتجاه واحد", NOUN, A2, "transport", translit = "tadhkarat dhahāb faqaṭ")
            v("ev_qitar", "القطار", "the train", "وسيلة نقل على سكة", NOUN, A1, "transport", translit = "al-qiṭār")
            v("ev_taakhir", "تأخير", "delay", "وصول بعد الموعد", NOUN, A2, "transport", translit = "taʾkhīr")
            v("ev_bitaqat_suud", "بطاقة الصعود إلى الطائرة", "boarding pass", "بطاقة ركوب الطائرة", NOUN, A2, "transport", translit = "biṭāqat aṣ-ṣuʿūd ilā ṭ-ṭāʾira")
            v("ev_amtia", "الأمتعة", "luggage", "حقائب السفر", NOUN, A2, "transport", translit = "al-amtiʿa")
            v("ev_mataar", "المطار", "the airport", "مكان هبوط الطائرات وإقلاعها", NOUN, A1, "transport", translit = "al-maṭār")
            v("ev_mathaf", "المتحف", "the museum", "مكان عرض الآثار والفنون", NOUN, A1, "sightseeing", translit = "al-matḥaf", ex = "متى يفتح المتحف؟", exEn = "What time does the museum open?", exAr = "متى يفتح المتحف؟")
            v("ev_mawaid_amal", "مواعيد العمل", "opening hours", "أوقات الفتح", NOUN, A2, "sightseeing", translit = "mawāʿīd al-ʿamal")
            v("ev_rasm_dukhul", "رسم الدخول", "entrance fee", "ثمن الدخول", NOUN, A2, "sightseeing", translit = "rasm ad-dukhūl")
            v("ev_jawla", "جولة سياحية", "sightseeing tour", "زيارة لأماكن سياحية", NOUN, A2, "sightseeing", translit = "jawla siyāḥiyya")
            v("ev_kharita", "خريطة", "map", "رسم يبيّن الطرق والأماكن", NOUN, A1, "sightseeing", translit = "kharīṭa")
            v("ev_sura", "هل يمكنك أن تلتقط لنا صورة؟", "Could you take a photo of us?", "طلب التصوير", PHRASE, A2, "sightseeing", translit = "hal yumkinuka an taltaqiṭa lanā ṣūra?")
            v("ev_madina_qadima", "المدينة القديمة", "the old town", "الحيّ التاريخي", NOUN, A2, "sightseeing", translit = "al-madīna al-qadīma")
            v("ev_tidhkar", "تذكار", "souvenir", "شيء يُشترى للذكرى", NOUN, A2, "sightseeing", translit = "tidhkār")
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
            f.lesson("finance", B1, "pro_finance", g("Banking basics", "أساسيات البنوك"), g("Accounts, loans, interest and instalments.", "الحسابات والقروض والفوائد والأقساط."),
                Skill.VOCABULARY, listOf("fin_hisab", "fin_qard", "fin_faida", "fin_qist", "fin_daman", "fin_taathur", "fin_mizaniyya_umumiyya", "fin_suyula"),
                extra = listOf(
                    choice("fi1", L, "The bank approved the loan: وافق البنك على ___", listOf("القرض", "القسط", "الحساب"), 0),
                    order("fi2", L, listOf("دفعتُ", "القسط", "الشهري"), g("I paid the monthly instalment.", "دفعتُ القسط الشهري.")),
                    choice("fi3", L, "Interest rate:", listOf("سعر الفائدة", "سعر الصرف", "سعر السوق"), 0, Skill.VOCABULARY),
                )),
            f.lesson("credit", B2, "pro_credit", g("Credit and risk", "الائتمان والمخاطر"), g("Credit risk, problem loans, write-offs and guarantors.", "مخاطر الائتمان والقروض المتعثرة وشطب الديون والكفلاء."),
                Skill.VOCABULARY, listOf("cr_mukhatir", "cr_qard_mutaaththir", "cr_haykala", "cr_shatb", "cr_jadara", "cr_kafil"),
                extra = listOf(choice("cr1", L, "A loan unpaid for 90+ days: قرض ___", listOf("متعثّر", "مضمون", "جديد"), 0, Skill.VOCABULARY), choice("cr2", L, "Creditworthiness:", listOf("الجدارة الائتمانية", "السيولة", "الميزانية العمومية"), 0, Skill.VOCABULARY))),
            f.lesson("legal", B2, "pro_legal", g("Contract language", "لغة العقود"), g("Contracts, clauses, parties, liability and breach.", "العقود والبنود والأطراف والمسؤولية والإخلال."),
                Skill.VOCABULARY, listOf("lg_aqd", "lg_band", "lg_taraf", "lg_masuliyya", "lg_ikhlal"),
                extra = listOf(choice("lg1", L, "وقّع الطرفان ___ أمس.", listOf("العقد", "القسط", "الكفيل"), 0, Skill.VOCABULARY), choice("lg2", L, "A section of a contract:", listOf("بند", "طرف", "ضمان"), 0, Skill.VOCABULARY))),
            f.lesson("ev_essentials", A1, "survival", g("Most useful phrases", "العبارات الأكثر استخدامًا"), g("Phrases you will use every day and when travelling.", "عبارات تستخدمها كل يوم وفي السفر."),
                Skill.SPEAKING, listOf("ev_law_samaht", "ev_la_afham", "ev_bibut", "ev_karrir", "ev_dawrat_miyah", "ev_musaada", "ev_la_aarif", "ev_la_mushkila"),
                extra = listOf(
                    choice("ev1", L, "You don't understand. You say…", listOf("لا أفهم", "لا مشكلة", "إلى اللقاء"), 0, Skill.FUNCTIONAL),
                    order("ev2", L, listOf("تكلّم", "ببطء", "من", "فضلك"), g("Speak slowly, please.", "تكلّم ببطء من فضلك.")),
                )),
            f.lesson("ev_emergency", A2, "survival", g("Emergencies and help", "الطوارئ وطلب المساعدة"), g("Ask for help, report a loss and find the police or a hospital.", "اطلب المساعدة وأبلغ عن فقدان شيء واعثر على الشرطة أو المستشفى."),
                Skill.SPEAKING, listOf("ev_najda", "ev_shurta", "ev_isaaf", "ev_jawaz", "ev_suriqa", "ev_sifara", "ev_mustashfa", "ev_raqm_tawari"),
                extra = listOf(
                    choice("ev3", L, "Call an ambulance!", listOf("اتصل بالإسعاف!", "اتصل بالفندق!", "اتصل بالمطعم!"), 0),
                    order("ev4", L, listOf("أضعتُ", "جواز", "سفري"), g("I've lost my passport.", "أضعتُ جواز سفري.")),
                )),
            f.lesson("ev_restaurant", A2, "tourism", g("At the restaurant", "في المطعم"), g("Ask for a table, order a meal, mention allergies and pay.", "اطلب طاولة واطلب وجبة واذكر الحساسية وادفع."),
                Skill.SPEAKING, listOf("ev_tawila", "ev_tansah", "ev_hassasiyya", "ev_nabati", "ev_qaimat_taam", "ev_tabaq_raisi", "ev_halwa", "ev_hisab"),
                extra = listOf(
                    order("ev5", L, listOf("طاولة", "لشخصين", "من", "فضلك"), g("A table for two, please.", "طاولة لشخصين من فضلك.")),
                    choice("ev6", L, "The bill, please:", listOf("الحساب من فضلك", "القائمة من فضلك", "الماء من فضلك"), 0),
                )),
            f.lesson("ev_hotel", A2, "tourism", g("At the hotel", "في الفندق"), g("Check in, ask about breakfast and Wi-Fi, check out.", "سجّل وصولك واسأل عن الفطور والواي فاي وسجّل المغادرة."),
                Skill.LISTENING, listOf("ev_hajz", "ev_ghurfa_fardiyya", "ev_ghurfa_muzdawaja", "ev_miftah", "ev_futur", "ev_kalimat_murur", "ev_istiqbal", "ev_mughadara"),
                extra = listOf(
                    choice("ev7", L, "Is breakfast included?", listOf("هل الفطور مشمول؟", "هل الغرفة كبيرة؟", "أين المفتاح؟"), 0),
                    dictation("ev8", L, "عندي حجز باسم حسن", listOf("عندي حجز باسم حسن.")),
                )),
            f.lesson("ev_transport", A2, "tourism", g("Trains, buses and flights", "القطارات والحافلات والطائرات"), g("Buy tickets, find your platform and handle delays.", "اشترِ التذاكر واعثر على رصيفك وتعامل مع التأخير."),
                Skill.LISTENING, listOf("ev_rasif", "ev_dhahab_awda", "ev_dhahab_faqat", "ev_qitar", "ev_taakhir", "ev_bitaqat_suud", "ev_amtia", "ev_mataar"),
                extra = listOf(
                    choice("ev9", L, "A return ticket:", listOf("تذكرة ذهاب وعودة", "تذكرة ذهاب فقط", "بطاقة الصعود إلى الطائرة"), 0, Skill.VOCABULARY),
                    dictation("ev10", L, "القطار متأخر", listOf("القطار متأخر.")),
                )),
            f.lesson("ev_sightseeing", A2, "tourism", g("Sightseeing", "السياحة والمعالم"), g("Museums, tickets, tours and photos.", "المتاحف والتذاكر والجولات والصور."),
                Skill.READING, listOf("ev_mathaf", "ev_mawaid_amal", "ev_rasm_dukhul", "ev_jawla", "ev_kharita", "ev_sura", "ev_madina_qadima", "ev_tidhkar"),
                extra = listOf(
                    choice("ev11", L, "What time does the museum open? ___ يفتح المتحف؟", listOf("متى", "أين", "كم"), 0),
                    choice("ev12", L, "Entrance fee:", listOf("رسم الدخول", "مواعيد العمل", "المدينة القديمة"), 0, Skill.VOCABULARY),
                )),
        )
        val units = listOf(
            CourseUnit("ar_u_basics", L, PRE_A1, g("First steps", "الخطوات الأولى"), g("Greetings and pronouns", "التحيات والضمائر"), listOf("ar_l_greetings", "ar_l_people")),
            CourseUnit("ar_u_everyday", L, A1, g("Everyday Arabic", "العربية اليومية"), g("Café, places and routines", "المقهى والأماكن والروتين"), listOf("ar_l_cafe", "ar_l_places", "ar_l_routine")),

            CourseUnit("ar_u_home", L, A2, g("Home and health", "البيت والصحة"), g("Your home, the weather and the doctor", "بيتك والطقس والطبيب"), listOf("ar_l_home", "ar_l_health")),
            CourseUnit("ar_u_pro_business", L, B1, g("Arabic for work", "العربية للعمل"), g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), listOf("ar_l_work"), trackId = "business"),
            CourseUnit("ar_u_next", L, B1, g("Next steps", "الخطوة التالية"), g("The past and the future", "الماضي والمستقبل"), listOf("ar_l_past", "ar_l_future")),
            CourseUnit("ar_u_pro_finance", L, B1, g("Banking and finance", "البنوك والتمويل"), g("Core banking vocabulary", "مفردات البنوك الأساسية"), listOf("ar_l_finance"), trackId = "finance"),
            CourseUnit("ar_u_pro_credit", L, B2, g("Credit and risk", "الائتمان والمخاطر"), g("Credit risk and problem loans", "مخاطر الائتمان والقروض المتعثرة"), listOf("ar_l_credit"), trackId = "credit_risk"),
            CourseUnit("ar_u_pro_legal", L, B2, g("Legal Arabic", "العربية القانونية"), g("Contracts", "العقود"), listOf("ar_l_legal"), trackId = "legal"),
            CourseUnit("ar_u_survival", L, A1, g("Essential phrases", "العبارات الأساسية"), g("Everyday phrases and emergencies", "العبارات اليومية والطوارئ"), listOf("ar_l_ev_essentials", "ar_l_ev_emergency")),
            CourseUnit("ar_u_tourism", L, A2, g("Travel and tourism", "السفر والسياحة"), g("Restaurants, hotels, transport and sightseeing", "المطاعم والفنادق والمواصلات والمعالم"), listOf("ar_l_ev_restaurant", "ar_l_ev_hotel", "ar_l_ev_transport", "ar_l_ev_sightseeing")),
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
            RolePlayScenario(
                "ar_s_hotel", L, A2, g("Checking in at a hotel", "تسجيل الوصول في فندق"), g("A hotel reception in Marrakesh.", "مكتب استقبال فندق في مراكش."),
                g("Check in, show your passport and ask for the Wi-Fi password.", "سجّل وصولك وأظهر جواز سفرك واسأل عن كلمة مرور الواي فاي."),
                "مساء الخير، أهلًا بك. كيف أستطيع مساعدتك؟ (masāʾ al-khayr, ahlan bik. kayfa astaṭīʿu musāʿadatak?)", g("Good evening, welcome. How can I help you?", "مساء الخير، أهلًا بك. كيف أستطيع مساعدتك؟"),
                listOf(
                    ScenarioBeat(listOf("حجز", "باسم", "غرفة"), g("Say: عندي حجز باسم… (ʿindī ḥajz bi-ism…)", "قل: عندي حجز باسم…"),
                        "عندي حجز باسم حسن.", "نعم، غرفة مزدوجة لليلتين. جواز السفر من فضلك. (naʿam, ghurfa muzdawaja li-laylatayn. jawāz as-safar min faḍlik.)", g("Yes, a double room for two nights. Your passport, please.", "نعم، غرفة مزدوجة لليلتين. جواز السفر من فضلك.")),
                    ScenarioBeat(listOf("تفضل", "تفضّل", "جواز", "نعم"), g("Hand it over: تفضّل (tafaḍḍal).", "قدّمه: تفضّل."),
                        "تفضّل.", "شكرًا. الفطور من السابعة إلى العاشرة. (shukran. al-fuṭūr min as-sābiʿa ilā l-ʿāshira.)", g("Thank you. Breakfast is from seven to ten.", "شكرًا. الفطور من السابعة إلى العاشرة.")),
                    ScenarioBeat(listOf("واي", "فاي", "كلمة", "مرور", "الإنترنت"), g("Ask for the Wi-Fi password.", "اسأل عن كلمة مرور الواي فاي."),
                        "ما كلمة مرور الواي فاي؟", "مكتوبة على بطاقة الغرفة. إقامة سعيدة! (maktūba ʿalā biṭāqat al-ghurfa. iqāma saʿīda!)", g("It's written on the room card. Enjoy your stay!", "مكتوبة على بطاقة الغرفة. إقامة سعيدة!")),
                ),
                listOf("عندي حجز باسم…", "تفضّل", "هل الفطور مشمول؟"),
            ),
            RolePlayScenario(
                "ar_s_restaurant", L, A2, g("Dinner at a restaurant", "العشاء في مطعم"), g("A restaurant in Beirut.", "مطعم في بيروت."),
                g("Get a table, mention an allergy and order a main course.", "احصل على طاولة واذكر حساسيتك واطلب طبقًا رئيسيًا."),
                "أهلًا وسهلًا! هل عندكم حجز؟ (ahlan wa-sahlan! hal ʿindakum ḥajz?)", g("Welcome! Do you have a reservation?", "أهلًا وسهلًا! هل عندكم حجز؟"),
                listOf(
                    ScenarioBeat(listOf("طاولة", "لشخصين", "لا"), g("Say: لا، طاولة لشخصين من فضلك.", "قل: لا، طاولة لشخصين من فضلك."),
                        "لا، طاولة لشخصين من فضلك.", "تفضّلوا. هذه قائمة الطعام. هل عندكم حساسية من شيء؟ (tafaḍḍalū. hādhihi qāʾimat aṭ-ṭaʿām. hal ʿindakum ḥassāsiyya min shayʾ?)", g("Please come in. Here is the menu. Are you allergic to anything?", "تفضّلوا. هذه قائمة الطعام. هل عندكم حساسية من شيء؟")),
                    ScenarioBeat(listOf("حساسية", "لا"), g("Say: عندي حساسية من المكسرات.", "قل: عندي حساسية من المكسرات."),
                        "عندي حساسية من المكسرات.", "حسنًا، سأخبر الطاهي. ماذا تطلبون؟ (ḥasanan, sa-ukhbiru ṭ-ṭāhī. mādhā taṭlubūn?)", g("All right, I'll tell the chef. What would you like?", "حسنًا، سأخبر الطاهي. ماذا تطلبون؟")),
                    ScenarioBeat(listOf("أريد", "اريد", "سمك", "دجاج", "لحم"), g("Order: أريد السمك من فضلك.", "اطلب: أريد السمك من فضلك."),
                        "أريد السمك من فضلك.", "اختيار ممتاز. (ikhtiyār mumtāz.)", g("Excellent choice.", "اختيار ممتاز.")),
                ),
                listOf("طاولة لشخصين من فضلك", "عندي حساسية من…", "أريد…", "الحساب من فضلك"),
            ),
            RolePlayScenario(
                "ar_s_airport", L, A2, g("Checking in at the airport", "تسجيل الدخول في المطار"), g("A check-in desk at Dubai airport.", "مكتب تسجيل في مطار دبي."),
                g("Hand over your documents, check your luggage and ask about boarding time.", "سلّم وثائقك وسجّل أمتعتك واسأل عن موعد الصعود."),
                "صباح الخير. جواز السفر والتذكرة من فضلك. (ṣabāḥ al-khayr. jawāz as-safar wa-t-tadhkara min faḍlik.)", g("Good morning. Your passport and ticket, please.", "صباح الخير. جواز السفر والتذكرة من فضلك."),
                listOf(
                    ScenarioBeat(listOf("تفضل", "تفضّل", "نعم", "هذا"), g("Hand them over: تفضّل.", "قدّمها: تفضّل."),
                        "تفضّل.", "هل عندك أمتعة؟ (hal ʿindaka amtiʿa?)", g("Do you have any luggage?", "هل عندك أمتعة؟")),
                    ScenarioBeat(listOf("حقيبة", "أمتعة", "امتعة", "نعم", "لا"), g("Say: نعم، حقيبة واحدة.", "قل: نعم، حقيبة واحدة."),
                        "نعم، حقيبة واحدة.", "هذه بطاقة الصعود. البوابة رقم اثني عشر. (hādhihi biṭāqat aṣ-ṣuʿūd. al-bawwāba raqm ithnay ʿashar.)", g("Here is your boarding pass. Gate number twelve.", "هذه بطاقة الصعود. البوابة رقم اثني عشر.")),
                    ScenarioBeat(listOf("متى", "موعد", "الصعود"), g("Ask: متى موعد الصعود؟", "اسأل: متى موعد الصعود؟"),
                        "متى موعد الصعود؟", "في الساعة العاشرة والنصف. رحلة سعيدة! (fī s-sāʿa al-ʿāshira wa-n-niṣf. riḥla saʿīda!)", g("At half past ten. Have a good flight!", "في الساعة العاشرة والنصف. رحلة سعيدة!")),
                ),
                listOf("تفضّل", "حقيبة واحدة", "متى موعد الصعود؟"),
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
            tracks = listOf(LearningTrack("business", L, g("Arabic for work", "العربية للعمل"), g("Meetings, formal emails, budgets and deadlines in Modern Standard Arabic.", "الاجتماعات والرسائل الرسمية والميزانيات والمواعيد النهائية بالعربية الفصحى."), A2, null),
                LearningTrack("finance", L, g("Arabic for banking and finance", "العربية للبنوك والتمويل"), g("Accounts, loans, interest, collateral and financial statements in Modern Standard Arabic.", "الحسابات والقروض والفوائد والضمانات والقوائم المالية بالعربية الفصحى."), B1,
                    g("This track teaches Arabic vocabulary used in this field. It is not financial, legal or professional advice.", "يعلّم هذا المسار مفردات العربية المستخدمة في هذا المجال، ولا يُعدّ استشارة مالية أو قانونية أو مهنية.")),
                LearningTrack("credit_risk", L, g("Credit and risk", "الائتمان والمخاطر"), g("Credit risk, problem loans, restructuring and guarantors.", "مخاطر الائتمان والقروض المتعثرة وإعادة الهيكلة والكفلاء."), B2,
                    g("This track teaches Arabic vocabulary used in this field. It is not financial, legal or professional advice.", "يعلّم هذا المسار مفردات العربية المستخدمة في هذا المجال، ولا يُعدّ استشارة مالية أو قانونية أو مهنية.")),
                LearningTrack("legal", L, g("Legal Arabic", "العربية القانونية"), g("Contracts, clauses, parties and liability.", "العقود والبنود والأطراف والمسؤولية."), B2,
                    g("This track teaches Arabic vocabulary used in this field. It is not financial, legal or professional advice.", "يعلّم هذا المسار مفردات العربية المستخدمة في هذا المجال، ولا يُعدّ استشارة مالية أو قانونية أو مهنية."))),
            dictations = listOf(dictation("d1", L, "أنا طالب", listOf("انا طالب")), dictation("d2", L, "أين المحطة؟", listOf("اين المحطة"))),
            writingPrompts = listOf(writing("wp1", L, g("Introduce yourself in Arabic (name, job or study).", "عرّف بنفسك بالعربية."), 5, listOf("اسمي", "أنا"))),
        )
    }
}
