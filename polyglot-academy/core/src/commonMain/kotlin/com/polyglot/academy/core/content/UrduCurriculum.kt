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
import com.polyglot.academy.core.model.VocabularySource

/**
 * Urdu starter course (standard Urdu as used in Pakistan), in Urdu script with a simple romanization.
 * Original content; not yet reviewed by a native speaker.
 */
internal object UrduCurriculum {
    private val L = LanguageCode.URDU
    private val biz = setOf("business")
    private val fin = setOf("finance")
    private val pro = VocabularySource.PROFESSIONAL_TRACK

    fun build(): LanguageCurriculum {
        val vb = VocabBuilder(L)
        with(vb) {
            v("salam", "السلام علیکم", "hello (peace be upon you)", "السلام عليكم", PHRASE, A1, "greetings", translit = "assalām-o-ʿalaikum",
                ex = "السلام علیکم، میرا نام عمران ہے۔", exEn = "Hello, my name is Imran.", exAr = "السلام عليكم، اسمي عمران.")
            v("subah_bakhair", "صبح بخیر", "good morning", "صباح الخير", PHRASE, A1, "greetings", translit = "subah bakhair")
            v("shab_bakhair", "شب بخیر", "good night", "تصبح على خير", PHRASE, A1, "greetings", translit = "shab bakhair")
            v("shukriya", "شکریہ", "thank you", "شكرًا", PHRASE, A1, "greetings", translit = "shukriya",
                ex = "بہت شکریہ!", exEn = "Thank you very much!", exAr = "شكرًا جزيلًا!")
            v("meharbani", "مہربانی کر کے", "please", "من فضلك", PHRASE, A1, "greetings", translit = "mehrbānī kar ke")
            v("maaf", "معاف کیجیے", "sorry, excuse me", "عذرًا", PHRASE, A1, "greetings", translit = "muʿāf kījiye")
            v("khuda_hafiz", "خدا حافظ", "goodbye", "مع السلامة", PHRASE, A1, "greetings", translit = "khudā hāfiz")
            v("mera_naam", "میرا نام … ہے", "my name is…", "اسمي…", PHRASE, A1, "greetings", translit = "merā nām … hai")
            v("mil_kar_khushi", "آپ سے مل کر خوشی ہوئی", "nice to meet you", "تشرّفت بمعرفتك", PHRASE, A1, "greetings", translit = "āp se mil kar khushī huī")
            v("khandan", "خاندان", "family", "العائلة", NOUN, A1, "people", translit = "khāndān", gender = MASCULINE)
            v("ammi", "امی", "mum, mother", "الأم", NOUN, A1, "people", translit = "ammī", gender = FEMININE,
                ex = "میری امی استانی ہیں۔", exEn = "My mother is a teacher.", exAr = "أمي معلّمة.")
            v("abbu", "ابو", "dad, father", "الأب", NOUN, A1, "people", translit = "abbū", gender = MASCULINE)
            v("bhai", "بھائی", "brother", "الأخ", NOUN, A1, "people", translit = "bhāī", gender = MASCULINE)
            v("behan", "بہن", "sister", "الأخت", NOUN, A1, "people", translit = "behan", gender = FEMININE)
            v("dost", "دوست", "friend", "الصديق", NOUN, A1, "people", translit = "dost",
                ex = "وہ میرا سب سے اچھا دوست ہے۔", exEn = "He is my best friend.", exAr = "إنه أعزّ أصدقائي.")
            v("pani", "پانی", "water", "الماء", NOUN, A1, "food", translit = "pānī", gender = MASCULINE)
            v("chai", "چائے", "tea", "الشاي", NOUN, A1, "food", translit = "chāy", gender = FEMININE)
            v("coffee", "کافی", "coffee", "القهوة", NOUN, A1, "food", translit = "kāfī", gender = FEMININE)
            v("roti", "روٹی", "bread, flatbread", "الخبز", NOUN, A1, "food", translit = "roṭī", gender = FEMININE)
            v("bill", "بل", "the bill (restaurant)", "الحساب (في المطعم)", NOUN, A2, "food", translit = "bil", gender = MASCULINE,
                ex = "براہِ کرم بل لے آئیں۔", exEn = "Please bring the bill.", exAr = "أحضر الحساب من فضلك.")
            v("chahiye", "مجھے … چاہیے", "I would like…, I need…", "أريد…، أحتاج إلى…", PHRASE, A1, "food", translit = "mujhe … chāhiye",
                ex = "مجھے ایک چائے چاہیے۔", exEn = "I'd like a tea.", exAr = "أريد كوب شاي.")
            v("mazedar", "مزیدار", "delicious", "لذيذ", ADJECTIVE, A1, "food", translit = "mazedār")
            v("aaj", "آج", "today", "اليوم", ADVERB, A1, "time", translit = "āj")
            v("kal", "کل", "tomorrow; yesterday (from context)", "غدًا؛ أمس (حسب السياق)", ADVERB, A1, "time", translit = "kal",
                ex = "کل ملیں گے!", exEn = "See you tomorrow!", exAr = "نلتقي غدًا!")
            v("abhi", "ابھی", "now, right now", "الآن", ADVERB, A1, "time", translit = "abhī")
            v("hafta", "ہفتہ", "week; Saturday", "الأسبوع؛ السبت", NOUN, A1, "time", translit = "hafta", gender = MASCULINE)
            v("station", "اسٹیشن", "station", "المحطة", NOUN, A2, "travel", translit = "isṭeshan",
                ex = "اسٹیشن کہاں ہے؟", exEn = "Where is the station?", exAr = "أين المحطة؟")
            v("ticket", "ٹکٹ", "ticket", "التذكرة", NOUN, A2, "travel", translit = "ṭikaṭ")
            v("bayen", "بائیں", "left", "اليسار", ADVERB, A1, "travel", translit = "bāeṇ")
            v("dayen", "دائیں", "right", "اليمين", ADVERB, A1, "travel", translit = "dāeṇ")
            v("seedha", "سیدھا", "straight on", "إلى الأمام مباشرة", ADVERB, A2, "travel", translit = "sīdhā")
            v("uthna", "اٹھنا", "to get up", "ينهض", VERB, A2, "routine", translit = "uṭhnā",
                ex = "میں روز صبح سات بجے اٹھتا ہوں۔", exEn = "I get up at seven every morning.", exAr = "أنهض كل صباح في السابعة.")
            v("kaam_karna", "کام کرنا", "to work", "يعمل", VERB, A1, "routine", translit = "kām karnā")
            v("parhna", "پڑھنا", "to read; to study", "يقرأ؛ يدرس", VERB, A1, "routine", translit = "paṛhnā")
            v("aam_taur", "عام طور پر", "usually", "عادةً", ADVERB, A2, "routine", translit = "ʿām taur par")
            v("thaka", "تھکا ہوا / تھکی ہوئی", "tired (m./f.)", "متعب / متعبة", ADJECTIVE, A1, "feelings", translit = "thakā huā / thakī huī")
            v("khush", "خوش", "happy", "سعيد", ADJECTIVE, A1, "feelings", translit = "khush")
            v("mere_khayal", "میرے خیال میں", "in my opinion", "في رأيي", PHRASE, B1, "feelings", translit = "mere khayāl meṇ",
                ex = "میرے خیال میں یہ اچھا خیال ہے۔", exEn = "In my opinion this is a good idea.", exAr = "في رأيي هذه فكرة جيدة.")
            v("agarche", "اگرچہ", "although", "على الرغم من أنّ", PHRASE, B2, "feelings", translit = "agarche")
            v("kitne_ka", "یہ کتنے کا ہے؟", "how much is it?", "بكم هذا؟", PHRASE, A1, "shopping", translit = "yeh kitne kā hai?")
            v("mehnga", "مہنگا", "expensive", "غالٍ", ADJECTIVE, A1, "shopping", translit = "mahaṅgā")
            v("sasta", "سستا", "cheap", "رخيص", ADJECTIVE, A1, "shopping", translit = "sastā")

            // Home and weather
            v("ghar", "گھر", "house, home", "البيت", NOUN, A1, "home", translit = "ghar", gender = MASCULINE,
                ex = "میرا گھر چھوٹا مگر خوبصورت ہے۔", exEn = "My house is small but beautiful.", exAr = "بيتي صغير لكنه جميل.")
            v("bawarchi_khana", "باورچی خانہ", "kitchen", "المطبخ", NOUN, A1, "home", translit = "bāwarchī khāna", gender = MASCULINE)
            v("sone_ka_kamra", "سونے کا کمرہ", "bedroom", "غرفة النوم", NOUN, A1, "home", translit = "sone kā kamra", gender = MASCULINE)
            v("pasand", "مجھے … پسند ہے", "I like…", "أحبّ…", PHRASE, A1, "home", translit = "mujhe … pasand hai",
                ex = "مجھے دھوپ والے دن بہت پسند ہیں۔", exEn = "I really like sunny days.", exAr = "أحبّ الأيام المشمسة كثيرًا.")
            v("mausam", "موسم", "weather", "الطقس", NOUN, A2, "weather", translit = "mausam", gender = MASCULINE)
            v("garmi", "گرمی ہے", "it's hot", "الجو حار", PHRASE, A1, "weather", translit = "garmī hai")
            v("sardi", "سردی ہے", "it's cold", "الجو بارد", PHRASE, A1, "weather", translit = "sardī hai",
                ex = "جنوری میں بہت سردی ہوتی ہے۔", exEn = "It's very cold in January.", exAr = "في يناير يكون الجو باردًا جدًا.")
            v("barish", "بارش ہو رہی ہے", "it's raining", "إنها تمطر", PHRASE, A1, "weather", translit = "bārish ho rahī hai")
            // Health
            v("doctor", "ڈاکٹر", "doctor", "الطبيب", NOUN, A1, "health", translit = "ḍākṭar")
            v("dard", "میرے … میں درد ہے", "my … hurts", "يؤلمني…", PHRASE, A2, "health", translit = "mere … meṇ dard hai",
                ex = "میرے سر میں درد ہے۔", exEn = "I have a headache.", exAr = "رأسي يؤلمني.")
            v("sar", "سر", "head", "الرأس", NOUN, A1, "health", translit = "sar", gender = MASCULINE)
            v("bukhar", "بخار", "fever", "الحمّى", NOUN, A2, "health", translit = "bukhār", gender = MASCULINE)
            v("medical_store", "میڈیکل اسٹور", "pharmacy", "الصيدلية", NOUN, A1, "health", translit = "meḍikal isṭor")
            v("appointment", "اپوائنٹمنٹ", "appointment", "الموعد", NOUN, A2, "health", translit = "apāinṭmanṭ", gender = FEMININE,
                ex = "مجھے ڈاکٹر سے اپوائنٹمنٹ لینی ہے۔", exEn = "I need to make an appointment with the doctor.", exAr = "أحتاج إلى حجز موعد مع الطبيب.")
            v("bimar", "بیمار", "ill, sick", "مريض", ADJECTIVE, A1, "health", translit = "bīmār")
            // Work (professional track)
            v("meeting", "میٹنگ", "meeting", "الاجتماع", NOUN, A2, "business", translit = "mīṭing", gender = FEMININE,
                ex = "میٹنگ دس بجے شروع ہوتی ہے۔", exEn = "The meeting starts at ten.", exAr = "يبدأ الاجتماع في العاشرة.", tags = biz, source = pro)
            v("company", "کمپنی", "company", "الشركة", NOUN, A2, "business", translit = "kampanī", gender = FEMININE, tags = biz, source = pro)
            v("client", "کلائنٹ / گاہک", "client, customer", "العميل", NOUN, A2, "business", translit = "klāinṭ / gāhak", tags = biz, source = pro)
            v("quotation", "کوٹیشن", "quote, estimate", "عرض السعر", NOUN, B1, "business", translit = "koṭeshan", tags = biz, source = pro)
            v("akhri_tareekh", "آخری تاریخ", "deadline", "الموعد النهائي", NOUN, B1, "business", translit = "ākhrī tārīkh", gender = FEMININE,
                ex = "آخری تاریخ بہت قریب ہے۔", exEn = "The deadline is very close.", exAr = "الموعد النهائي قريب جدًا.", tags = biz, source = pro)
            v("tajweez", "تجویز", "proposal", "المقترح", NOUN, B1, "business", translit = "tajvīz", gender = FEMININE, tags = biz, source = pro)
            v("munsalik", "… منسلک ہے", "… is attached (formal email)", "… مرفق", PHRASE, B1, "business", translit = "… munsalik hai", tags = biz, source = pro)
            v("muhtaram", "محترم جناب،", "Dear Sir, (formal letter opening)", "حضرة السيد، (افتتاحية رسمية)", PHRASE, B1, "business", translit = "muhtaram janāb", tags = biz, source = pro)

            // Next steps: plans and wishes
            v("mansooba", "منصوبہ", "plan", "الخطة", NOUN, B1, "plans", translit = "mansūba", gender = MASCULINE)
            v("agle_hafte", "اگلے ہفتے / اگلے سال", "next week / next year", "الأسبوع القادم / العام القادم", PHRASE, A2, "plans", translit = "agle hafte / agle sāl",
                ex = "اگلے سال میں لاہور جاؤں گا۔", exEn = "Next year I'll go to Lahore.", exAr = "في العام القادم سأذهب إلى لاهور.")
            v("safar_karna", "سفر کرنا", "to travel", "يسافر", VERB, A2, "plans", translit = "safar karnā")
            v("paise_bachana", "پیسے بچانا", "to save money", "يدّخر المال", VERB, B1, "plans", translit = "paise bachānā")
            v("shayad", "شاید", "maybe, perhaps", "ربما", ADVERB, A2, "plans", translit = "shāyad")
            v("chhuttiyan", "چھٹیاں", "holidays", "العطلة", NOUN, A2, "plans", translit = "chhuṭṭiyāṇ", gender = FEMININE)
            v("chahiye_should", "… کو … چاہیے", "should (someone should do…)", "ينبغي أن…", PHRASE, B2, "wishes", translit = "… ko … chāhiye",
                ex = "آپ کو آرام کرنا چاہیے۔", exEn = "You should rest.", exAr = "ينبغي أن ترتاح.")
            v("chahta_hoon_ke", "میں چاہتا ہوں کہ …", "I want (someone) to…", "أريد أن (يفعل شخص آخر)…", PHRASE, B2, "wishes", translit = "maiṅ chāhtā hūṇ ke …")
            v("umeed", "امید ہے", "I hope", "آمل", PHRASE, B1, "wishes", translit = "umīd hai")
            v("qismat", "قسمت", "luck", "الحظ", NOUN, B1, "wishes", translit = "qismat", gender = FEMININE)
            v("kamyab", "کامیاب ہونا", "to succeed", "ينجح", VERB, B1, "wishes", translit = "kāmyāb honā")
            v("imtihan", "امتحان", "exam", "الامتحان", NOUN, A2, "wishes", translit = "imtihān", gender = MASCULINE)

            // Banking and finance (professional track)
            v("fin_account", "بینک اکاؤنٹ", "bank account", "الحساب المصرفي", NOUN, A2, "finance", translit = "baink akāunṭ", tags = fin, source = pro)
            v("fin_qarz", "قرض", "loan", "القرض", NOUN, B1, "finance", translit = "qarz", gender = MASCULINE,
                ex = "بینک نے قرض منظور کر لیا۔", exEn = "The bank approved the loan.", exAr = "وافق البنك على القرض.", tags = fin, source = pro)
            v("fin_sood", "شرحِ سود", "interest rate", "سعر الفائدة", NOUN, B1, "finance", translit = "sharh-e sūd", tags = fin, source = pro)
            v("fin_qist", "قسط", "instalment", "القسط", NOUN, B1, "finance", translit = "qist", gender = FEMININE, tags = fin, source = pro)
            v("fin_zamanat", "ضمانت", "collateral, guarantee", "الضمان", NOUN, B2, "finance", translit = "zamānat", gender = FEMININE, tags = fin, source = pro)
            v("fin_nadehandagi", "نادہندگی", "default (failure to pay)", "التعثّر عن السداد", NOUN, B2, "finance", translit = "nādehandagī", tags = fin, source = pro)
            v("fin_balance_sheet", "بیلنس شیٹ", "balance sheet", "الميزانية العمومية", NOUN, B2, "finance", translit = "bailans shīṭ", tags = fin, source = pro)
            v("fin_liquidity", "لیکویڈیٹی", "liquidity", "السيولة", NOUN, B2, "finance", translit = "likwiḍiṭī", tags = fin, source = pro)
            // Daily life and travel
            v("ev_suniye", "سنیے", "excuse me (to get attention)", "لو سمحت", PHRASE, A1, "essentials", translit = "suniye", ex = "سنیے، کیا یہ سیٹ خالی ہے؟", exEn = "Excuse me, is this seat free?", exAr = "لو سمحت، هل هذا المقعد شاغر؟")
            v("ev_samjha_nahin", "میں سمجھا نہیں / سمجھی نہیں", "I didn't understand (m./f.)", "لم أفهم", PHRASE, A1, "essentials", translit = "maiṅ samjhā nahīṇ / samjhī nahīṇ")
            v("ev_ahista", "کیا آپ آہستہ بول سکتے ہیں؟", "Could you speak more slowly?", "هل يمكنك التحدث ببطء أكثر؟", PHRASE, A1, "essentials", translit = "kyā āp āhista bol sakte haiṇ?")
            v("ev_dobara", "کیا آپ دوبارہ کہہ سکتے ہیں؟", "Could you say that again?", "هل يمكنك أن تعيد؟", PHRASE, A1, "essentials", translit = "kyā āp dobāra keh sakte haiṇ?")
            v("ev_washroom", "واش روم کہاں ہے؟", "Where is the toilet?", "أين دورة المياه؟", PHRASE, A1, "essentials", translit = "wāsh rūm kahāṇ hai?")
            v("ev_madad", "کیا آپ میری مدد کر سکتے ہیں؟", "Can you help me?", "هل يمكنك مساعدتي؟", PHRASE, A1, "essentials", translit = "kyā āp merī madad kar sakte haiṇ?")
            v("ev_maloom", "مجھے نہیں معلوم", "I don't know", "لا أعرف", PHRASE, A1, "essentials", translit = "mujhe nahīṇ maʿlūm")
            v("ev_koi_baat", "کوئی بات نہیں", "you're welcome; no problem", "عفوًا؛ لا مشكلة", PHRASE, A1, "essentials", translit = "koī bāt nahīṇ")
            v("ev_bachao", "بچاؤ!", "Help!", "النجدة!", PHRASE, A1, "emergency", translit = "bachāo!")
            v("ev_police", "پولیس", "the police", "الشرطة", NOUN, A1, "emergency", translit = "polīs")
            v("ev_ambulance", "ایمبولینس بلائیں!", "Call an ambulance!", "اتصلوا بالإسعاف!", PHRASE, A2, "emergency", translit = "aimbulens bulāeṇ!", ex = "جلدی سے ایمبولینس بلائیں!", exEn = "Call an ambulance, quickly!", exAr = "اتصلوا بالإسعاف بسرعة!")
            v("ev_passport", "میرا پاسپورٹ گم ہو گیا ہے۔", "I've lost my passport.", "أضعتُ جواز سفري.", PHRASE, A2, "emergency", translit = "merā pāsporṭ gum ho gayā hai")
            v("ev_chori", "میرا بیگ چوری ہو گیا ہے۔", "My bag has been stolen.", "سُرقت حقيبتي.", PHRASE, A2, "emergency", translit = "merā baig chorī ho gayā hai")
            v("ev_sifarat_khana", "سفارت خانہ", "the embassy", "السفارة", NOUN, A2, "emergency", translit = "sifārat khāna")
            v("ev_haspatal", "ہسپتال", "hospital", "المستشفى", NOUN, A1, "emergency", translit = "haspatāl")
            v("ev_emergency_number", "ایمرجنسی نمبر", "emergency number", "رقم الطوارئ", NOUN, A2, "emergency", translit = "emarjansī nambar")
            v("ev_mez", "دو لوگوں کے لیے میز", "a table for two", "طاولة لشخصين", PHRASE, A1, "restaurant", translit = "do logoṇ ke liye mez", ex = "کیا دو لوگوں کے لیے میز مل سکتی ہے؟", exEn = "Can we have a table for two?", exAr = "هل يمكن أن نحصل على طاولة لشخصين؟")
            v("ev_recommend", "آپ کیا تجویز کریں گے؟", "What do you recommend?", "بماذا تنصحني؟", PHRASE, A2, "restaurant", translit = "āp kyā tajvīz kareṅge?")
            v("ev_allergy", "مجھے … سے الرجی ہے", "I'm allergic to…", "عندي حساسية من…", PHRASE, A2, "restaurant", translit = "mujhe … se alarjī hai")
            v("ev_sabzi_khor", "سبزی خور", "vegetarian", "نباتي", ADJECTIVE, A2, "restaurant", translit = "sabzī khor")
            v("ev_starter", "اسٹارٹر", "starter", "المقبّلات", NOUN, A2, "restaurant", translit = "isṭārṭar")
            v("ev_main_course", "مین کورس", "main course", "الطبق الرئيسي", NOUN, A2, "restaurant", translit = "main kors")
            v("ev_meetha", "میٹھا", "dessert; sweet", "الحلوى؛ حلو", NOUN, A1, "restaurant", translit = "mīṭhā")
            v("ev_tip", "ٹپ", "tip", "البقشيش", NOUN, A2, "restaurant", translit = "ṭip")
            v("ev_booking", "بکنگ", "booking, reservation", "الحجز", NOUN, A2, "hotel", translit = "buking", ex = "میری بکنگ حسن کے نام سے ہے۔", exEn = "My booking is under the name Hasan.", exAr = "حجزي باسم حسن.")
            v("ev_single_room", "سنگل روم", "single room", "غرفة لشخص واحد", NOUN, A2, "hotel", translit = "singal rūm")
            v("ev_double_room", "ڈبل روم", "double room", "غرفة مزدوجة", NOUN, A2, "hotel", translit = "ḍabal rūm")
            v("ev_chabi", "چابی", "key", "المفتاح", NOUN, A1, "hotel", translit = "chābī")
            v("ev_nashta", "کیا ناشتہ شامل ہے؟", "Is breakfast included?", "هل الفطور مشمول؟", PHRASE, A2, "hotel", translit = "kyā nāshta shāmil hai?")
            v("ev_wifi", "وائی فائی کا پاس ورڈ کیا ہے؟", "What's the Wi-Fi password?", "ما كلمة مرور الواي فاي؟", PHRASE, A2, "hotel", translit = "wāī fāī kā pās warḍ kyā hai?")
            v("ev_reception", "ریسیپشن", "reception", "الاستقبال", NOUN, A2, "hotel", translit = "risepshan")
            v("ev_checkout", "چیک آؤٹ کرنا", "to check out", "تسجيل المغادرة", VERB, A2, "hotel", translit = "chek āuṭ karnā")
            v("ev_platform", "پلیٹ فارم", "platform", "الرصيف", NOUN, A2, "transport", translit = "pleṭ fārm", ex = "ٹرین پلیٹ فارم نمبر تین سے جائے گی۔", exEn = "The train will leave from platform three.", exAr = "سيغادر القطار من الرصيف رقم ثلاثة.")
            v("ev_aana_jana", "آنے جانے کا ٹکٹ", "a return ticket", "تذكرة ذهاب وعودة", NOUN, A2, "transport", translit = "āne jāne kā ṭikaṭ")
            v("ev_yak_tarfa", "یک طرفہ ٹکٹ", "a single (one-way) ticket", "تذكرة ذهاب فقط", NOUN, A2, "transport", translit = "yak-tarfa ṭikaṭ")
            v("ev_rawangi", "روانگی", "departure", "المغادرة", NOUN, A2, "transport", translit = "rawāngī")
            v("ev_takheer", "تاخیر", "delay", "التأخير", NOUN, A2, "transport", translit = "tākhīr")
            v("ev_boarding_pass", "بورڈنگ پاس", "boarding pass", "بطاقة الصعود إلى الطائرة", NOUN, A2, "transport", translit = "borḍing pās")
            v("ev_saman", "سامان", "luggage", "الأمتعة", NOUN, A2, "transport", translit = "sāmān")
            v("ev_bus_stop", "بس اسٹاپ", "bus stop", "موقف الحافلات", NOUN, A1, "transport", translit = "bas isṭāp")
            v("ev_ajaib_ghar", "عجائب گھر", "museum", "المتحف", NOUN, A1, "sightseeing", translit = "ʿajāib ghar", ex = "عجائب گھر کب کھلتا ہے؟", exEn = "When does the museum open?", exAr = "متى يفتح المتحف؟")
            v("ev_auqat", "کھلنے کے اوقات", "opening hours", "مواعيد العمل", NOUN, A2, "sightseeing", translit = "khulne ke auqāt")
            v("ev_dakhla_ticket", "داخلہ ٹکٹ", "entrance ticket", "تذكرة الدخول", NOUN, A2, "sightseeing", translit = "dākhla ṭikaṭ")
            v("ev_guide_sair", "گائیڈ کے ساتھ سیر", "guided tour", "جولة مع مرشد", NOUN, A2, "sightseeing", translit = "gāiḍ ke sāth sair")
            v("ev_naqsha", "نقشہ", "map", "الخريطة", NOUN, A1, "sightseeing", translit = "naqsha")
            v("ev_tasveer", "کیا آپ ہماری تصویر کھینچ سکتے ہیں؟", "Could you take a photo of us?", "هل يمكنك أن تلتقط لنا صورة؟", PHRASE, A2, "sightseeing", translit = "kyā āp hamārī tasvīr khēṅch sakte haiṇ?")
            v("ev_purana_shehar", "پرانا شہر", "old town", "المدينة القديمة", NOUN, A2, "sightseeing", translit = "purānā shehar")
            v("ev_yadgari_tohfa", "یادگاری تحفہ", "souvenir", "تذكار", NOUN, A2, "sightseeing", translit = "yādgārī tohfa")
        }
        val vocabulary = vb.items
        val f = LessonFactory(L, vocabulary)

        val grammar = listOf(
            grammar("hona", L, A1, g("'To be': ہوں، ہے، ہیں", "الكينونة: ہوں وہے وہیں"),
                g("The verb 'to be' comes at the end of the sentence: میں … ہوں (I am), تم … ہو (you are, informal), وہ … ہے (he/she is), ہم / آپ / وہ … ہیں (we / you (polite) / they are).",
                    "يأتي فعل الكينونة في آخر الجملة: ‏میں … ہوں (أنا) وتم … ہو (أنت، غير رسمي) ووہ … ہے (هو/هي) وہم / آپ / وہ … ہیں (نحن / حضرتك / هم)."),
                listOf(ex("میں طالب علم ہوں۔", "I am a student.", "أنا طالب."), ex("آپ کہاں سے ہیں؟", "Where are you from?", "من أين حضرتك؟"),
                    ex("میں طالب علم ہے۔", "With میں use ہوں", "مع میں تُستخدم ہوں", wrong = true)),
                listOf(g("✗ میں خوش ہے ← ✓ میں خوش ہوں۔", "✗ میں خوش ہے ← ✓ میں خوش ہوں۔"))),
            grammar("gender", L, A1, g("Gender and کا / کی / کے", "الجنس وأدوات الإضافة کا / کی / کے"),
                g("Every noun is masculine or feminine. 'My' and 'of' agree with the thing possessed: میرا بھائی (my brother), میری بہن (my sister), میرے دوست (my friends). The same goes for کا / کی / کے: علی کا گھر، علی کی گاڑی۔",
                    "كل اسم في الأردية مذكر أو مؤنث، وتتطابق «ملكي» وأداة الإضافة مع المملوك: ‏میرا بھائی (أخي) ومیری بہن (أختي) ومیرے دوست (أصدقائي). وكذلك کا / کی / کے: ‏علی کا گھر وعلی کی گاڑی."),
                listOf(ex("میری بہن ڈاکٹر ہے۔", "My sister is a doctor.", "أختي طبيبة."), ex("یہ علی کا گھر ہے۔", "This is Ali's house.", "هذا بيت علي."),
                    ex("میرا بہن", "بہن is feminine: میری بہن", "بہن مؤنثة: میری بہن", wrong = true)),
                listOf(g("The possessive agrees with the possessed noun, not with the owner.", "تتطابق أداة الملكية مع المملوك لا مع المالك."))),
            grammar("postpositions", L, A1, g("Postpositions: میں، پر، سے", "حروف الجر المتأخرة: میں وپر وسے"),
                g("Urdu puts 'prepositions' after the noun: گھر میں (in the house), میز پر (on the table), لاہور سے (from Lahore). Masculine nouns ending in ا change to ے before them: کمرہ → کمرے میں.",
                    "تأتي حروف الجر في الأردية بعد الاسم: ‏گھر میں (في البيت) ومیز پر (على الطاولة) ولاہور سے (من لاهور). والأسماء المذكرة المنتهية بـ ا/ہ تتحوّل إلى ے قبلها: کمرہ ← کمرے میں."),
                listOf(ex("امی گھر میں ہیں۔", "Mum is at home.", "أمي في البيت."), ex("کتاب میز پر ہے۔", "The book is on the table.", "الكتاب على الطاولة."),
                    ex("کمرہ میں", "Use the changed form: کمرے میں", "استخدم الصيغة المتغيّرة: کمرے میں", wrong = true)),
                listOf(g("✗ میں لاہور سے آیا ہوں is fine; ✗ سے لاہور is wrong: the postposition follows the noun.", "‏«لاہور سے» صحيحة، أما «سے لاہور» فخطأ: حرف الجر يأتي بعد الاسم."))),
            grammar("present_habitual", L, A1, g("The present habitual: کرتا ہوں / کرتی ہوں", "المضارع المعتاد: کرتا ہوں / کرتی ہوں"),
                g("Verb stem + تا (m.) / تی (f.) / تے (m. pl.) + the 'to be' form: میں کام کرتا ہوں (I work, man speaking), میں کام کرتی ہوں (woman speaking), ہم پڑھتے ہیں (we study). It is used for routines and facts.",
                    "جذر الفعل + تا (مذكر) / تی (مؤنث) / تے (جمع مذكر) + فعل الكينونة: ‏میں کام کرتا ہوں (أعمل، بصوت رجل) ومیں کام کرتی ہوں (بصوت امرأة) وہم پڑھتے ہیں (ندرس). ويُستخدم للعادات والحقائق."),
                listOf(ex("میں روز چائے پیتا ہوں۔", "I drink tea every day. (man)", "أشرب الشاي كل يوم. (رجل)"), ex("وہ بینک میں کام کرتی ہے۔", "She works in a bank.", "هي تعمل في بنك.")),
                listOf(g("The verb shows the speaker's gender: a woman says کرتی ہوں, not کرتا ہوں.", "يُظهر الفعل جنس المتكلم: تقول المرأة کرتی ہوں لا کرتا ہوں."))),
            grammar("politeness", L, A2, g("Polite requests with آپ", "الطلب المهذب مع آپ"),
                g("With آپ, polite commands end in -یے / -ئیں: بیٹھیے (please sit), لائیں (please bring), دیجیے (please give). To ask for something: مجھے … چاہیے (I would like …). Add براہِ کرم or مہربانی کر کے for 'please'.",
                    "مع آپ تنتهي الأوامر المهذبة بـ ‎-یے / ‎-ئیں: ‏بیٹھیے (تفضّل بالجلوس) ولائیں (أحضِر من فضلك) ودیجیے (أعطِ من فضلك). وللطلب: ‏مجھے … چاہیے (أريد …). وأضف براہِ کرم أو مہربانی کر کے بمعنى «من فضلك»."),
                listOf(ex("مجھے ایک چائے چاہیے۔", "I'd like a tea.", "أريد كوب شاي."), ex("براہِ کرم بل لے آئیں۔", "Please bring the bill.", "أحضر الحساب من فضلك.")),
                listOf(g("'چائے دو!' (give tea!) sounds rude to a waiter.", "عبارة «چائے دو!» (أعطني شايًا!) تبدو فظّة مع النادل."))),
            grammar("past_ne", L, B1, g("The past tense and نے", "الماضي واستخدام نے"),
                g("With transitive verbs in the simple past, the subject takes نے and the verb agrees with the object: میں نے کھانا کھایا (I ate food), میں نے چائے پی (I drank tea, چائے is feminine). Intransitive verbs take no نے and agree with the subject: میں گیا / میں گئی (I went).",
                    "مع الأفعال المتعدية في الماضي البسيط يأخذ الفاعل نے ويتطابق الفعل مع المفعول به: ‏میں نے کھانا کھایا (أكلت الطعام) ومیں نے چائے پی (شربت الشاي، والشاي مؤنث). أما الأفعال اللازمة فلا تأخذ نے وتتطابق مع الفاعل: ‏میں گیا / میں گئی (ذهبت)."),
                listOf(ex("کل میں نے فلم دیکھی۔", "Yesterday I watched a film.", "أمس شاهدت فيلمًا."), ex("ہم بازار گئے۔", "We went to the market.", "ذهبنا إلى السوق."),
                    ex("میں نے گیا۔", "جانا is intransitive: میں گیا", "جانا فعل لازم: میں گیا", wrong = true)),
                listOf(g("✗ میں نے چائے پیا ← ✓ میں نے چائے پی (چائے is feminine).", "✗ میں نے چائے پیا ← ✓ میں نے چائے پی (الشاي مؤنث)."))),
            grammar("mausam", L, A1, g("Talking about the weather", "الحديث عن الطقس"),
                g("Heat and cold are nouns with ہے: گرمی ہے (it's hot), سردی ہے (it's cold). Rain 'is happening': بارش ہو رہی ہے. For a general description use موسم: آج موسم اچھا ہے.",
                    "الحر والبرد اسمان مع ہے: ‏گرمی ہے (الجو حار) وسردی ہے (الجو بارد). والمطر «يحدث»: ‏بارش ہو رہی ہے. وللوصف العام يُستخدم موسم: آج موسم اچھا ہے."),
                listOf(ex("آج بہت گرمی ہے۔", "It's very hot today.", "الجو حار جدًا اليوم."), ex("صبح سے بارش ہو رہی ہے۔", "It's been raining since the morning.", "إنها تمطر منذ الصباح."),
                    ex("آج گرم ہے۔", "For the weather say گرمی ہے", "للطقس قل گرمی ہے", wrong = true)),
                listOf(g("گرم is an adjective (hot tea: گرم چائے); for the weather use the noun گرمی.", "‏گرم صفة (شاي ساخن: گرم چائے)؛ أما للطقس فيُستخدم الاسم گرمی."))),
            grammar("aap", L, A2, g("Formal 'you': آپ", "المخاطبة الرسمية: آپ"),
                g("Use آپ with clients, officials, elders and people you don't know. آپ always takes plural verb forms, even for one person: آپ کیسے ہیں؟ (how are you?), کیا آپ آ سکتے ہیں؟ (can you come?).",
                    "استخدم آپ مع العملاء والموظفين وكبار السن ومن لا تعرفهم. ويأخذ آپ دائمًا صيغة الجمع حتى لشخص واحد: ‏آپ کیسے ہیں؟ (كيف حالك؟) وکیا آپ آ سکتے ہیں؟ (هل يمكنك أن تأتي؟)."),
                listOf(ex("کیا آپ تاریخ کی تصدیق کر سکتے ہیں؟", "Could you confirm the date?", "هل يمكنكم تأكيد الموعد؟"),
                    ex("آپ کیسا ہے؟", "آپ takes plural forms: آپ کیسے ہیں؟", "آپ يأخذ صيغة الجمع: آپ کیسے ہیں؟", wrong = true)),
                listOf(g("Using تم with a client or an older person can sound disrespectful.", "استخدام تم مع عميل أو شخص أكبر سنًا قد يبدو قلة احترام."))),
            grammar("future", L, B1, g("The future: جاؤں گا / جاؤں گی", "المستقبل: جاؤں گا / جاؤں گی"),
                g("Verb stem + the subjunctive ending + گا (m.) / گی (f.) / گے (m. pl.): میں جاؤں گا / جاؤں گی (I will go), وہ آئے گا (he will come), ہم کریں گے (we will do), بارش ہو گی (it will rain).",
                    "جذر الفعل + لاحقة الالتزامي + گا (مذكر) / گی (مؤنث) / گے (جمع): ‏میں جاؤں گا / جاؤں گی (سأذهب) ووہ آئے گا (سيأتي) وہم کریں گے (سنفعل) وبارش ہو گی (ستمطر)."),
                listOf(ex("شام کو ہم گھر پر کھانا کھائیں گے۔", "This evening we'll eat at home.", "سنتناول الطعام في البيت هذا المساء."),
                    ex("اگلے سال میں لاہور میں رہوں گی۔", "Next year I'll live in Lahore. (woman)", "في العام القادم سأسكن في لاهور. (امرأة)"),
                    ex("میں کل جاتا گا۔", "Use the subjunctive stem: جاؤں گا", "استخدم جذر الالتزامي: جاؤں گا", wrong = true)),
                listOf(g("گا / گی / گے agree with the subject's gender and number.", "‏گا / گی / گے تتطابق مع جنس الفاعل وعدده."))),
            grammar("chahiye_subjunctive", L, B2, g("Should and wanting someone to: چاہیے، کہ + subjunctive", "الوجوب والطلب من الآخرين: چاہیے وکہ + الالتزامي"),
                g("'Should' is the person + کو + infinitive + چاہیے: آپ کو آرام کرنا چاہیے (you should rest). To want someone else to do something, use کہ and the subjunctive: میں چاہتا ہوں کہ تم آؤ (I want you to come). امید ہے کہ … usually takes the future.",
                    "الوجوب يُعبَّر عنه بالشخص + کو + المصدر + چاہیے: ‏آپ کو آرام کرنا چاہیے (ينبغي أن ترتاح). وللطلب من شخص آخر تُستخدم کہ مع الالتزامي: ‏میں چاہتا ہوں کہ تم آؤ (أريدك أن تأتي). وبعد امید ہے کہ … يأتي المستقبل عادةً."),
                listOf(ex("آپ کو زیادہ پانی پینا چاہیے۔", "You should drink more water.", "ينبغي أن تشرب ماءً أكثر."),
                    ex("میں چاہتی ہوں کہ تم امتحان میں کامیاب ہو۔", "I want you to pass the exam. (woman speaking)", "أريدك أن تنجح في الامتحان. (امرأة)"),
                    ex("میں چاہتا ہوں کہ تم آتے ہو۔", "Use the subjunctive after کہ: تم آؤ", "استخدم الالتزامي بعد کہ: تم آؤ", wrong = true)),
                listOf(g("✗ میں چاہتا ہوں تم آنا ← ✓ میں چاہتا ہوں کہ تم آؤ۔", "✗ میں چاہتا ہوں تم آنا ← ✓ میں چاہتا ہوں کہ تم آؤ۔"))),
        )

        val lessons = listOf(
            f.lesson("salam", A1, "basics", g("Greetings", "التحيات"), g("Say hello and introduce yourself.", "ألقِ التحية وعرّف بنفسك."),
                Skill.SPEAKING, listOf("salam", "subah_bakhair", "shab_bakhair", "shukriya", "meharbani", "maaf", "khuda_hafiz", "mera_naam", "mil_kar_khushi"), "hona",
                extra = listOf(
                    fill("s1", L, "میرا نام عمران ___۔", listOf("ہے")),
                    fill("s2", L, "میں طالب علم ___۔", listOf("ہوں")),
                ), minutes = 6),
            f.lesson("khandan", A1, "basics", g("Family", "العائلة"), g("Talk about your family with میرا / میری / میرے.", "تحدّث عن عائلتك باستخدام میرا / میری / میرے."),
                Skill.VOCABULARY, listOf("khandan", "ammi", "abbu", "bhai", "behan", "dost"), "gender",
                extra = listOf(choice("k1", L, "___ بہن ڈاکٹر ہے۔", listOf("میری", "میرا", "میرے"), 0,
                    explanation = g("بہن is feminine, so میری.", "‏بہن مؤنثة، لذا میری."))),
                prerequisites = listOf("salam")),
            f.lesson("chai_khana", A1, "everyday", g("At the café", "في المقهى"), g("Order politely with مجھے … چاہیے.", "اطلب بأدب باستخدام مجھے … چاہیے."),
                Skill.SPEAKING, listOf("pani", "chai", "coffee", "roti", "bill", "chahiye", "mazedar"), "politeness",
                extra = listOf(
                    order("c1", L, listOf("مجھے", "ایک", "چائے", "چاہیے"), g("I'd like a tea.", "أريد كوب شاي.")),
                    dictation("c2", L, "براہِ کرم بل لے آئیں۔", listOf("براہ کرم بل لے آئیں")),
                ), prerequisites = listOf("khandan")),
            f.lesson("rozmarra", A2, "everyday", g("Daily routine", "الروتين اليومي"), g("Describe your day with the present habitual.", "صِف يومك بالمضارع المعتاد."),
                Skill.WRITING, listOf("uthna", "kaam_karna", "parhna", "aam_taur", "aaj", "kal", "abhi", "hafta"), "present_habitual",
                extra = listOf(
                    fill("r1", L, "میں روز صبح سات بجے ___ ہوں۔ (اٹھنا)", listOf("اٹھتا", "اٹھتی")),
                    fill("r2", L, "ہم اردو ___ ہیں۔ (پڑھنا)", listOf("پڑھتے")),
                ), prerequisites = listOf("chai_khana")),
            f.lesson("safar", A2, "travel", g("Getting around", "التنقّل"), g("Ask for directions and say where things are.", "اسأل عن الاتجاهات وقل أين توجد الأشياء."),
                Skill.LISTENING, listOf("station", "ticket", "bayen", "dayen", "seedha"), "postpositions",
                extra = listOf(
                    fill("sf1", L, "امی گھر ___ ہیں۔ (in)", listOf("میں")),
                    dictation("sf2", L, "سیدھے جائیں، پھر بائیں مڑیں۔", listOf("سیدھے جائیں پھر بائیں مڑیں")),
                ), prerequisites = listOf("rozmarra")),
            f.lesson("kharidari", A2, "travel", g("Shopping", "التسوّق"), g("Ask prices and give opinions.", "اسأل عن الأسعار وعبّر عن رأيك."),
                Skill.SPEAKING, listOf("kitne_ka", "mehnga", "sasta", "khush", "thaka"), "gender",
                extra = listOf(fill("kh1", L, "یہ جوتے بہت ___ ہیں۔ (مہنگا)", listOf("مہنگے"))),
                prerequisites = listOf("safar")),
            f.lesson("maazi", B1, "travel", g("Talking about the past", "الحديث عن الماضي"), g("Use the past tense with and without نے.", "استخدم الماضي مع نے وبدونها."),
                Skill.SPEAKING, listOf("kal", "mere_khayal", "agarche", "khush"), "past_ne",
                extra = listOf(
                    fill("m1", L, "کل میں نے فلم ___۔ (دیکھنا)", listOf("دیکھی")),
                    fill("m2", L, "کل ہم بازار ___۔ (جانا)", listOf("گئے")),
                ), prerequisites = listOf("kharidari")),
            f.lesson("ghar_mausam", A2, "home", g("Home and weather", "البيت والطقس"), g("Describe your home and talk about the weather.", "صِف بيتك وتحدّث عن الطقس."),
                Skill.VOCABULARY, listOf("ghar", "bawarchi_khana", "sone_ka_kamra", "pasand", "mausam", "garmi", "sardi", "barish"), "mausam",
                extra = listOf(
                    fill("g1", L, "جنوری میں بہت ___ ہوتی ہے۔", listOf("سردی")),
                    fill("g2", L, "چھتری لے لو، بارش ہو ___ ہے!", listOf("رہی")),
                    order("g3", L, listOf("مجھے", "اپنا", "گھر", "بہت", "پسند", "ہے"), g("I really like my home.", "أحبّ بيتي كثيرًا.")),
                ), prerequisites = listOf("maazi")),
            f.lesson("sehat", A2, "home", g("At the doctor's", "عند الطبيب"), g("Say what hurts and make an appointment.", "قل ما يؤلمك واحجز موعدًا."),
                Skill.SPEAKING, listOf("doctor", "dard", "sar", "bukhar", "medical_store", "appointment", "bimar"), "aap",
                extra = listOf(
                    fill("se1", L, "میرے سر میں ___ ہے۔", listOf("درد")),
                    fill("se2", L, "مجھے ڈاکٹر سے ___ لینی ہے۔", listOf("اپوائنٹمنٹ")),
                    dictation("se3", L, "مجھے کل سے بخار ہے۔", listOf("مجھے کل سے بخار ہے")),
                ), prerequisites = listOf("ghar_mausam")),
            f.lesson("kaam", B1, "pro_business", g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), g("Quotes, deadlines and polite requests with آپ.", "عروض الأسعار والمواعيد النهائية والطلبات المهذبة بصيغة آپ."),
                Skill.WRITING, listOf("meeting", "company", "client", "quotation", "akhri_tareekh", "tajweez", "munsalik", "muhtaram"), "aap",
                extra = listOf(
                    fill("ka1", L, "کوٹیشن ___ ہے۔ (attached)", listOf("منسلک")),
                    choice("ka2", L, "Which is the right way to start a formal email?", listOf("ہیلو یار!", "محترم جناب،", "کیا حال ہے؟"), 1, Skill.FUNCTIONAL),
                    fill("ka3", L, "کیا آپ میٹنگ کی تاریخ کی تصدیق کر ___ ہیں؟", listOf("سکتے")),
                    writing("ka4", L, g("Write a short email to a client proposing a meeting next week.", "اكتب رسالة قصيرة إلى عميل تقترح فيها اجتماعًا الأسبوع القادم."), 40,
                        listOf("محترم", "میٹنگ", "تجویز", "شکریہ")),
                )),
            f.lesson("mansoobay", B1, "next", g("Plans for the future", "خطط المستقبل"), g("Talk about plans with the future tense.", "تحدّث عن الخطط بصيغة المستقبل."),
                Skill.SPEAKING, listOf("mansooba", "agle_hafte", "safar_karna", "paise_bachana", "shayad", "chhuttiyan"), "future",
                extra = listOf(
                    fill("mn1", L, "اگلے سال میں کینیڈا ___۔ (جانا)", listOf("جاؤں گا", "جاؤں گی")),
                    fill("mn2", L, "کل بارش ___۔ (ہونا)", listOf("ہو گی", "ہوگی")),
                    order("mn3", L, listOf("ہم", "چھٹیوں", "کے", "لیے", "پیسے", "بچائیں", "گے"), g("We'll save money for the holidays.", "سنوفّر المال للعطلة.")),
                    writing("mn4", L, g("Write about your plans for next year (30+ words).", "اكتب عن خططك للعام القادم (30 كلمة على الأقل)."), 30, listOf("اگلے سال", "شاید", "گا")),
                ), prerequisites = listOf("sehat")),
            f.lesson("khwahishein", B2, "next", g("Needs and wishes", "الضرورات والأمنيات"), g("Use چاہیے and کہ with the subjunctive.", "استخدم چاہیے وکہ مع الالتزامي."),
                Skill.GRAMMAR, listOf("chahiye_should", "chahta_hoon_ke", "umeed", "qismat", "kamyab", "imtihan"), "chahiye_subjunctive",
                extra = listOf(
                    fill("kw1", L, "آپ کو زیادہ پانی پینا ___۔", listOf("چاہیے")),
                    fill("kw2", L, "میں چاہتا ہوں کہ تم کل ___۔ (آنا)", listOf("آؤ")),
                    choice("kw3", L, "امید ہے آپ امتحان میں ___۔", listOf("کامیاب ہوں گے", "کامیاب ہونا", "کامیاب تھے"), 0,
                        explanation = g("After امید ہے, the future is usual.", "بعد امید ہے يأتي المستقبل عادةً.")),
                    translate("kw4", L, g("I want you to succeed.", "أريدك أن تنجح."),
                        listOf("میں چاہتا ہوں کہ تم کامیاب ہو", "میں چاہتی ہوں کہ تم کامیاب ہو", "میں چاہتا ہوں کہ آپ کامیاب ہوں", "میں چاہتی ہوں کہ آپ کامیاب ہوں")),
                ), prerequisites = listOf("mansoobay")),
            f.lesson("finance", B1, "pro_finance", g("Banking basics", "أساسيات البنوك"), g("Accounts, loans, interest and instalments.", "الحسابات والقروض والفوائد والأقساط."),
                Skill.VOCABULARY, listOf("fin_account", "fin_qarz", "fin_sood", "fin_qist", "fin_zamanat", "fin_nadehandagi", "fin_balance_sheet", "fin_liquidity"),
                extra = listOf(
                    fill("fi1", L, "بینک نے کمپنی کو ___ دیا۔", listOf("قرض")),
                    fill("fi2", L, "شرحِ ___ پانچ فیصد ہے۔", listOf("سود")),
                    choice("fi3", L, "Which document shows assets and liabilities?", listOf("بیلنس شیٹ", "کوٹیشن", "رسید"), 0, Skill.VOCABULARY),
                )),
            f.lesson("ev_essentials", A1, "survival", g("Most useful phrases", "العبارات الأكثر استخدامًا"), g("Phrases you will use every day and when travelling.", "عبارات تستخدمها كل يوم وفي السفر."),
                Skill.SPEAKING, listOf("ev_suniye", "ev_samjha_nahin", "ev_ahista", "ev_dobara", "ev_washroom", "ev_madad", "ev_maloom", "ev_koi_baat"),
                extra = listOf(
                    fill("ev1", L, "کیا آپ ___ بول سکتے ہیں؟", listOf("آہستہ")),
                    choice("ev2", L, "Someone thanks you. You reply…", listOf("کوئی بات نہیں۔", "مجھے نہیں معلوم۔", "سنیے۔"), 0, Skill.FUNCTIONAL),
                )),
            f.lesson("ev_emergency", A2, "survival", g("Emergencies and help", "الطوارئ وطلب المساعدة"), g("Ask for help, report a loss and find the police or a hospital.", "اطلب المساعدة وأبلغ عن فقدان شيء واعثر على الشرطة أو المستشفى."),
                Skill.SPEAKING, listOf("ev_bachao", "ev_police", "ev_ambulance", "ev_passport", "ev_chori", "ev_sifarat_khana", "ev_haspatal", "ev_emergency_number"),
                extra = listOf(
                    fill("ev3", L, "میرا پاسپورٹ گم ہو ___ ہے۔", listOf("گیا")),
                    choice("ev4", L, "Your bag was stolen:", listOf("میرا بیگ چوری ہو گیا ہے۔", "مجھے بیگ پسند ہے۔", "بیگ مہنگا ہے۔"), 0, Skill.FUNCTIONAL),
                )),
            f.lesson("ev_restaurant", A2, "tourism", g("At the restaurant", "في المطعم"), g("Ask for a table, order a meal, mention allergies and pay.", "اطلب طاولة واطلب وجبة واذكر الحساسية وادفع."),
                Skill.SPEAKING, listOf("ev_mez", "ev_recommend", "ev_allergy", "ev_sabzi_khor", "ev_starter", "ev_main_course", "ev_meetha", "ev_tip"),
                extra = listOf(
                    fill("ev5", L, "کیا دو لوگوں کے ___ میز مل سکتی ہے؟", listOf("لیے")),
                    choice("ev6", L, "'میٹھا' at the end of a meal is…", listOf("dessert", "a starter", "the tip"), 0, Skill.VOCABULARY),
                )),
            f.lesson("ev_hotel", A2, "tourism", g("At the hotel", "في الفندق"), g("Check in, ask about breakfast and Wi-Fi, check out.", "سجّل وصولك واسأل عن الفطور والواي فاي وسجّل المغادرة."),
                Skill.LISTENING, listOf("ev_booking", "ev_single_room", "ev_double_room", "ev_chabi", "ev_nashta", "ev_wifi", "ev_reception", "ev_checkout"),
                extra = listOf(
                    fill("ev7", L, "کیا ناشتہ ___ ہے؟", listOf("شامل")),
                    dictation("ev8", L, "میری دو راتوں کی بکنگ ہے۔", listOf("میری دو راتوں کی بکنگ ہے")),
                )),
            f.lesson("ev_transport", A2, "tourism", g("Trains, buses and flights", "القطارات والحافلات والطائرات"), g("Buy tickets, find your platform and handle delays.", "اشترِ التذاكر واعثر على رصيفك وتعامل مع التأخير."),
                Skill.LISTENING, listOf("ev_platform", "ev_aana_jana", "ev_yak_tarfa", "ev_rawangi", "ev_takheer", "ev_boarding_pass", "ev_saman", "ev_bus_stop"),
                extra = listOf(
                    choice("ev9", L, "A return ticket is…", listOf("آنے جانے کا ٹکٹ", "یک طرفہ ٹکٹ", "پلیٹ فارم"), 0, Skill.VOCABULARY),
                    dictation("ev10", L, "ٹرین آدھا گھنٹہ لیٹ ہے۔", listOf("ٹرین آدھا گھنٹہ لیٹ ہے")),
                )),
            f.lesson("ev_sightseeing", A2, "tourism", g("Sightseeing", "السياحة والمعالم"), g("Museums, tickets, tours and photos.", "المتاحف والتذاكر والجولات والصور."),
                Skill.READING, listOf("ev_ajaib_ghar", "ev_auqat", "ev_dakhla_ticket", "ev_guide_sair", "ev_naqsha", "ev_tasveer", "ev_purana_shehar", "ev_yadgari_tohfa"),
                extra = listOf(
                    fill("ev11", L, "عجائب گھر کب ___ ہے؟ (کھلنا)", listOf("کھلتا")),
                    choice("ev12", L, "A guided tour is…", listOf("گائیڈ کے ساتھ سیر", "پرانا شہر", "یادگاری تحفہ"), 0, Skill.VOCABULARY),
                )),
        )
        val units = listOf(
            CourseUnit("ur_u_basics", L, A1, g("First steps", "الخطوات الأولى"), g("Greetings and family", "التحيات والعائلة"), listOf("ur_l_salam", "ur_l_khandan")),
            CourseUnit("ur_u_everyday", L, A1, g("Everyday life", "الحياة اليومية"), g("Café and routines", "المقهى والروتين"), listOf("ur_l_chai_khana", "ur_l_rozmarra")),
            CourseUnit("ur_u_travel", L, A2, g("Out and about", "في الخارج"), g("Directions, shopping and the past", "الاتجاهات والتسوق والماضي"), listOf("ur_l_safar", "ur_l_kharidari", "ur_l_maazi")),
            CourseUnit("ur_u_home", L, A2, g("Home and health", "البيت والصحة"), g("Your home, the weather and the doctor", "بيتك والطقس والطبيب"), listOf("ur_l_ghar_mausam", "ur_l_sehat")),
            CourseUnit("ur_u_pro_business", L, B1, g("Urdu for work", "الأردية للعمل"), g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), listOf("ur_l_kaam"), trackId = "business"),
            CourseUnit("ur_u_next", L, B1, g("Next steps", "الخطوة التالية"), g("Plans, needs and wishes", "الخطط والضرورات والأمنيات"), listOf("ur_l_mansoobay", "ur_l_khwahishein")),
            CourseUnit("ur_u_pro_finance", L, B1, g("Banking and finance", "البنوك والتمويل"), g("Core banking vocabulary", "مفردات البنوك الأساسية"), listOf("ur_l_finance"), trackId = "finance"),
            CourseUnit("ur_u_survival", L, A1, g("Essential phrases", "العبارات الأساسية"), g("Everyday phrases and emergencies", "العبارات اليومية والطوارئ"), listOf("ur_l_ev_essentials", "ur_l_ev_emergency")),
            CourseUnit("ur_u_tourism", L, A2, g("Travel and tourism", "السفر والسياحة"), g("Restaurants, hotels, transport and sightseeing", "المطاعم والفنادق والمواصلات والمعالم"), listOf("ur_l_ev_restaurant", "ur_l_ev_hotel", "ur_l_ev_transport", "ur_l_ev_sightseeing")),
        )
        val readings = listOf(
            ReadingPassage(
                "ur_r_sana", L, A1, "ثنا کا دن", g("Sana's day", "يوم ثناء"),
                listOf("ثنا صبح سات بجے اٹھتی ہے۔", "وہ چائے پیتی ہے اور روٹی کھاتی ہے۔", "وہ ایک بینک میں کام کرتی ہے۔", "شام کو وہ انگریزی پڑھتی ہے۔", "وہ تھکی ہوئی ہے مگر خوش ہے۔"),
                listOf(g("Sana gets up at seven in the morning.", "تنهض ثناء في السابعة صباحًا."), g("She drinks tea and eats bread.", "تشرب الشاي وتأكل الخبز."),
                    g("She works in a bank.", "تعمل في بنك."), g("In the evening she studies English.", "في المساء تدرس الإنجليزية."), g("She is tired but happy.", "هي متعبة لكنها سعيدة.")),
                listOf("ur_uthna", "ur_chai", "ur_roti", "ur_kaam_karna", "ur_parhna", "ur_khush"),
                listOf(
                    Exercise.MultipleChoice("ur_r_sana_q1", Skill.READING, Instr.choose, "ثنا کہاں کام کرتی ہے؟", listOf("اسٹیشن پر", "بینک میں", "کیفے میں"), 1),
                    Exercise.MultipleChoice("ur_r_sana_q2", Skill.READING, Instr.choose, "شام کو وہ کیا پڑھتی ہے؟", listOf("انگریزی", "عربی", "فرانسیسی"), 0),
                ),
            ),
        )
        val scenarios = listOf(
            RolePlayScenario(
                "ur_s_chai", L, A1, g("At a tea stall", "عند كشك الشاي"), g("A tea stall in Lahore in the morning.", "كشك شاي في لاهور صباحًا."),
                g("Order a tea and a samosa and pay.", "اطلب كوب شاي وسمبوسة وادفع."),
                "جی، کیا چاہیے؟", g("Yes, what would you like?", "نعم، ماذا تريد؟"),
                listOf(
                    ScenarioBeat(listOf("چائے", "چاہیے", "دیں", "پلیز"), g("Say: 'ایک چائے دے دیں۔'", "قل: ایک چائے دے دیں۔"),
                        "ایک چائے دے دیں، پلیز۔", "ضرور۔ اور کچھ؟", g("Sure. Anything else?", "بالتأكيد. شيء آخر؟")),
                    ScenarioBeat(listOf("سموسہ", "بس", "نہیں", "شکریہ"), g("Add a samosa or say 'بس، شکریہ۔'", "أضف سمبوسة أو قل بس، شکریہ۔"),
                        "اور ایک سموسہ بھی۔", "کل ستر روپے ہوئے۔", g("That's seventy rupees in total.", "المجموع سبعون روبية.")),
                    ScenarioBeat(listOf("یہ لیں", "شکریہ", "خدا حافظ"), g("Pay and say thank you.", "ادفع وقل شكرًا."),
                        "یہ لیں۔ شکریہ!", "شکریہ، پھر آئیے گا!", g("Thank you, come again!", "شكرًا، عُد مرة أخرى!")),
                ),
                listOf("ایک چائے دے دیں۔", "بس، شکریہ۔", "کتنے پیسے ہوئے؟"),
            ),
            RolePlayScenario(
                "ur_s_doctor", L, A2, g("At the doctor's", "عند الطبيب"), g("A clinic in Karachi. Language practice only — not medical advice.", "عيادة في كراتشي. للتدرّب اللغوي فقط، وليس استشارة طبية."),
                g("Explain what hurts, say since when, and ask about the pharmacy.", "اشرح ما يؤلمك ومنذ متى، واسأل عن الصيدلية."),
                "السلام علیکم، تشریف رکھیے۔ کیا تکلیف ہے؟", g("Hello, please sit down. What's the problem?", "السلام عليكم، تفضّل بالجلوس. ما الذي يزعجك؟"),
                listOf(
                    ScenarioBeat(listOf("درد", "سر", "بخار", "بیمار"), g("Say what hurts: 'میرے سر میں درد ہے۔'", "قل ما يؤلمك: میرے سر میں درد ہے۔"),
                        "میرے سر میں درد ہے اور بخار بھی ہے۔", "کب سے؟", g("Since when?", "منذ متى؟")),
                    ScenarioBeat(listOf("سے", "کل", "دن", "ہفتے"), g("Say since when: 'کل رات سے۔'", "قل منذ متى: کل رات سے۔"),
                        "کل رات سے۔", "ٹھیک ہے۔ آرام کیجیے اور پانی زیادہ پیجیے۔", g("All right. Rest and drink plenty of water.", "حسنًا. استرح واشرب الكثير من الماء.")),
                    ScenarioBeat(listOf("میڈیکل", "فارمیسی", "کہاں"), g("Ask where the pharmacy is.", "اسأل أين الصيدلية."),
                        "قریب ترین میڈیکل اسٹور کہاں ہے؟", "بالکل ساتھ، دائیں طرف۔", g("Right next door, on the right.", "بجانبنا مباشرة، على اليمين.")),
                ),
                listOf("میرے … میں درد ہے۔", "مجھے بخار ہے۔", "کب سے؟", "میڈیکل اسٹور کہاں ہے؟"),
            ),
            RolePlayScenario(
                "ur_s_meeting", L, B1, g("Planning a project with a client", "التخطيط لمشروع مع عميل"), g("A video call with a client in Islamabad.", "مكالمة فيديو مع عميل في إسلام آباد."),
                g("Agree on the price, the delivery date and the next step.", "اتفق على السعر وموعد التسليم والخطوة التالية."),
                "السلام علیکم۔ آپ کی تجویز کا شکریہ۔ کیا ہم قیمت پر بات کریں؟", g("Hello. Thank you for your proposal. Shall we discuss the price?", "السلام عليكم. شكرًا على مقترحكم. هل نناقش السعر؟"),
                listOf(
                    ScenarioBeat(listOf("قیمت", "ڈالر", "روپے", "کوٹیشن", "کل"), g("Explain the price.", "اشرح السعر."),
                        "جی ضرور۔ کل قیمت پانچ ہزار ڈالر ہے۔", "ٹھیک ہے، اور ڈیلیوری کب تک ہو گی؟", g("All right, and when will the delivery be?", "حسنًا، ومتى سيكون التسليم؟")),
                    ScenarioBeat(listOf("ہفتے", "ہفتوں", "مہینے", "تاریخ", "ڈیلیوری"), g("Give a date: 'چھ ہفتوں میں'.", "حدّد موعدًا: چھ ہفتوں میں."),
                        "ہم چھ ہفتوں میں ڈیلیوری کر سکتے ہیں۔", "یہ ہمارے لیے ٹھیک ہے۔", g("That works for us.", "يناسبنا ذلك.")),
                    ScenarioBeat(listOf("ای میل", "بھیج", "خلاصہ", "معاہدہ"), g("Say you will send a summary by email.", "قل إنك سترسل ملخصًا بالبريد الإلكتروني."),
                        "میں آج آپ کو ای میل پر خلاصہ بھیج دوں گا۔", "بہت شکریہ۔ پھر بات ہو گی۔", g("Thank you very much. Speak soon.", "شكرًا جزيلًا. نتحدث لاحقًا.")),
                ),
                listOf("کل قیمت … ہے۔", "ہم … میں ڈیلیوری کر سکتے ہیں۔", "میں آپ کو … بھیج دوں گا۔"),
                trackId = "business",
            ),
            RolePlayScenario(
                "ur_s_hotel", L, A2, g("Checking in at a hotel", "تسجيل الوصول في فندق"), g("A hotel reception in Lahore.", "مكتب استقبال فندق في لاهور."),
                g("Check in, show your passport and ask for the Wi-Fi password.", "سجّل وصولك وأظهر جواز سفرك واسأل عن كلمة مرور الواي فاي."),
                "خوش آمدید! میں آپ کی کیا مدد کر سکتا ہوں؟", g("Welcome! How can I help you?", "أهلًا وسهلًا! كيف أساعدك؟"),
                listOf(
                    ScenarioBeat(listOf("بکنگ", "نام", "کمرہ"), g("Say: 'میری بکنگ … کے نام سے ہے۔'", "قل: میری بکنگ … کے نام سے ہے۔"),
                        "میری بکنگ حسن کے نام سے ہے۔", "جی، دو راتوں کے لیے ڈبل روم۔ اپنا پاسپورٹ دکھا دیں؟", g("Yes, a double room for two nights. Could you show your passport?", "نعم، غرفة مزدوجة لليلتين. هل تريني جواز سفرك؟")),
                    ScenarioBeat(listOf("یہ لیں", "پاسپورٹ", "جی"), g("Hand it over: 'جی، یہ لیں۔'", "قدّمه: جی، یہ لیں۔"),
                        "جی، یہ لیں۔", "شکریہ۔ ناشتہ سات سے دس بجے تک ہے۔", g("Thank you. Breakfast is from seven to ten.", "شكرًا. الفطور من السابعة إلى العاشرة.")),
                    ScenarioBeat(listOf("وائی فائی", "پاس ورڈ", "انٹرنیٹ"), g("Ask for the Wi-Fi password.", "اسأل عن كلمة مرور الواي فاي."),
                        "وائی فائی کا پاس ورڈ کیا ہے؟", "آپ کے کارڈ پر لکھا ہے۔ آپ کا قیام خوشگوار ہو!", g("It's written on your card. Enjoy your stay!", "إنها مكتوبة على بطاقتك. إقامة سعيدة!")),
                ),
                listOf("میری بکنگ … کے نام سے ہے۔", "جی، یہ لیں۔", "کیا ناشتہ شامل ہے؟"),
            ),
            RolePlayScenario(
                "ur_s_restaurant", L, A2, g("Dinner at a restaurant", "العشاء في مطعم"), g("A family restaurant in Karachi.", "مطعم عائلي في كراتشي."),
                g("Get a table, mention an allergy and order a main course.", "احصل على طاولة واذكر حساسيتك واطلب طبقًا رئيسيًا."),
                "خوش آمدید! کیا آپ نے بکنگ کی ہے؟", g("Welcome! Have you booked?", "أهلًا وسهلًا! هل حجزتم؟"),
                listOf(
                    ScenarioBeat(listOf("میز", "دو", "نہیں"), g("Say no and ask for a table for two.", "قل لا واطلب طاولة لشخصين."),
                        "نہیں، کیا دو لوگوں کے لیے میز مل سکتی ہے؟", "جی ضرور۔ یہ مینو ہے۔ کسی چیز سے الرجی ہے؟", g("Of course. Here's the menu. Are you allergic to anything?", "بالتأكيد. هذه القائمة. هل لديك حساسية من شيء؟")),
                    ScenarioBeat(listOf("الرجی", "نہیں", "مونگ پھلی"), g("Mention an allergy: 'مجھے مونگ پھلی سے الرجی ہے۔'", "اذكر حساسيتك: مجھے مونگ پھلی سے الرجی ہے۔"),
                        "مجھے مونگ پھلی سے الرجی ہے۔", "ٹھیک ہے، میں شیف کو بتا دوں گا۔ آپ کیا لیں گے؟", g("All right, I'll tell the chef. What will you have?", "حسنًا، سأُخبر الطاهي. ماذا تطلب؟")),
                    ScenarioBeat(listOf("مین کورس", "بریانی", "چکن", "مچھلی", "چاہیے", "لوں گا", "لوں گی"), g("Order: 'مین کورس میں مجھے چکن بریانی چاہیے۔'", "اطلب: مین کورس میں مجھے چکن بریانی چاہیے۔"),
                        "مین کورس میں مجھے چکن بریانی چاہیے۔", "بہت اچھا، ابھی لاتا ہوں۔", g("Very good, I'll bring it right away.", "جيد جدًا، سأحضره فورًا.")),
                ),
                listOf("دو لوگوں کے لیے میز مل سکتی ہے؟", "مجھے … سے الرجی ہے۔", "مجھے … چاہیے۔", "براہِ کرم بل لے آئیں۔"),
            ),
            RolePlayScenario(
                "ur_s_station", L, A2, g("Buying a train ticket", "شراء تذكرة قطار"), g("The ticket office at Karachi Cantonment station.", "شبّاك التذاكر في محطة كراتشي كانت."),
                g("Buy a return ticket and find out the platform.", "اشترِ تذكرة ذهاب وعودة واعرف رقم الرصيف."),
                "جی، فرمائیے؟", g("Yes, how can I help?", "نعم، تفضّل؟"),
                listOf(
                    ScenarioBeat(listOf("ٹکٹ", "آنے جانے", "لاہور", "حیدرآباد"), g("Ask for a return ticket to Lahore.", "اطلب تذكرة ذهاب وعودة إلى لاهور."),
                        "لاہور کے لیے آنے جانے کا ٹکٹ چاہیے۔", "جی۔ اگلی ٹرین سوا دس بجے روانہ ہو گی۔", g("Sure. The next train leaves at a quarter past ten.", "حسنًا. سيغادر القطار التالي في العاشرة والربع.")),
                    ScenarioBeat(listOf("پلیٹ فارم", "کون سا", "کس"), g("Ask which platform: 'کس پلیٹ فارم سے جائے گی؟'", "اسأل عن الرصيف: کس پلیٹ فارم سے جائے گی؟"),
                        "کس پلیٹ فارم سے جائے گی؟", "پلیٹ فارم نمبر تین سے۔ تقریباً دس منٹ لیٹ ہے۔", g("From platform three. It's about ten minutes late.", "من الرصيف رقم ثلاثة. إنه متأخر نحو عشر دقائق.")),
                    ScenarioBeat(listOf("شکریہ", "مہربانی"), g("Thank them.", "اشكره."),
                        "بہت شکریہ۔", "کوئی بات نہیں۔ آپ کا سفر اچھا ہو!", g("You're welcome. Have a good trip!", "عفوًا. رحلة سعيدة!")),
                ),
                listOf("… کے لیے آنے جانے کا ٹکٹ چاہیے۔", "کس پلیٹ فارم سے جائے گی؟", "کیا ٹرین وقت پر ہے؟"),
            ),
        )
        fun item(id: String, level: CefrLevel, skill: Skill, prompt: String, options: List<String>, correct: Int) =
            AssessmentItem("ur_a_$id", L, level, skill, Exercise.MultipleChoice("ur_a_$id", skill, Instr.choose, prompt, options, correct))
        val assessment = listOf(
            item("a1_1", A1, Skill.GRAMMAR, "میں طالب علم ___۔", listOf("ہوں", "ہے", "ہیں"), 0),
            item("a1_2", A1, Skill.VOCABULARY, "'شکریہ' means…", listOf("please", "thank you", "sorry"), 1),
            item("a1_3", A1, Skill.GRAMMAR, "___ بہن ڈاکٹر ہے۔", listOf("میرا", "میری", "میرے"), 1),
            item("a2_1", A2, Skill.GRAMMAR, "وہ لڑکی روز اسکول ___ ہے۔", listOf("جاتی", "جاتا", "جاتے"), 0),
            item("a2_2", A2, Skill.GRAMMAR, "کتاب میز ___ ہے۔ (on)", listOf("پر", "سے", "کو"), 0),
            item("a2_3", A2, Skill.VOCABULARY, "ریسٹورنٹ میں ادائیگی کے لیے آپ ___ مانگتے ہیں۔", listOf("مینو", "بل", "ٹکٹ"), 1),
            item("b1_1", B1, Skill.GRAMMAR, "کل میں نے کھانا ___۔", listOf("کھایا", "کھاتا ہوں", "کھاؤں گا"), 0),
            item("b1_2", B1, Skill.GRAMMAR, "اگلے ہفتے ہم لاہور ___۔", listOf("گئے", "جائیں گے", "جاتے تھے"), 1),
            item("b1_3", B1, Skill.GRAMMAR, "علی ___ خط لکھا۔", listOf("نے", "کو", "سے"), 0),
            item("b2_1", B2, Skill.GRAMMAR, "آپ کو زیادہ آرام کرنا ___۔", listOf("چاہیے", "چاہتا", "چاہا"), 0),
            item("b2_2", B2, Skill.GRAMMAR, "میں چاہتی ہوں کہ تم وقت پر ___۔", listOf("آؤ", "آتے ہو", "آئے تھے"), 0),
            item("b2_3", B2, Skill.GRAMMAR, "___ بارش ہو رہی تھی، ہم باہر گئے۔", listOf("اگرچہ", "کیونکہ", "جب تک"), 0),
        )
        return LanguageCurriculum(L, vocabulary, grammar, units, lessons, readings, scenarios, assessment,
            tracks = listOf(LearningTrack("business", L, g("Urdu for work", "الأردية للعمل"), g("Meetings, formal emails, quotes and deadlines.", "الاجتماعات والرسائل الرسمية وعروض الأسعار والمواعيد النهائية."), A2, null),
                LearningTrack("finance", L, g("Urdu for banking and finance", "الأردية للبنوك والتمويل"), g("Accounts, loans, interest, collateral and financial statements.", "الحسابات والقروض والفوائد والضمانات والقوائم المالية."), B1,
                    g("This track teaches Urdu vocabulary used in this field. It is not financial, legal or professional advice.", "يعلّم هذا المسار مفردات الأردية المستخدمة في هذا المجال، ولا يُعدّ استشارة مالية أو قانونية أو مهنية."))),
            dictations = listOf(
                dictation("d1", L, "میرا نام عمران ہے۔", listOf("میرا نام عمران ہے")),
                dictation("d2", L, "اسٹیشن کہاں ہے؟", listOf("اسٹیشن کہاں ہے")),
                dictation("d3", L, "میں عام طور پر صبح کام کرتا ہوں۔", listOf("میں عام طور پر صبح کام کرتا ہوں", "میں عام طور پر صبح کام کرتی ہوں")),
            ),
            writingPrompts = listOf(writing("wp1", L, g("Describe your weekend in Urdu.", "صِف عطلة نهاية الأسبوع بالأردية."), 30, listOf("ہفتہ", "اتوار", "میں نے"))),
        )
    }
}
