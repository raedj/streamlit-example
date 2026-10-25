package com.polyglot.academy.core.content

import com.polyglot.academy.core.assessment.AssessmentItem
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CefrLevel.A1
import com.polyglot.academy.core.model.CefrLevel.A2
import com.polyglot.academy.core.model.CefrLevel.B1
import com.polyglot.academy.core.model.CefrLevel.B2
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.Exercise
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

/** Turkish starter course (Istanbul Turkish). Original content; not yet reviewed by a native speaker. */
internal object TurkishCurriculum {
    private val L = LanguageCode.TURKISH
    private val biz = setOf("business")
    private val fin = setOf("finance")
    private val pro = VocabularySource.PROFESSIONAL_TRACK

    fun build(): LanguageCurriculum {
        val vb = VocabBuilder(L)
        with(vb) {
            v("merhaba", "merhaba", "hello", "مرحبًا", PHRASE, A1, "greetings", "/meɾhaba/",
                ex = "Merhaba, benim adım Kerem.", exEn = "Hello, my name is Kerem.", exAr = "مرحبًا، اسمي كرم.")
            v("gunaydin", "günaydın", "good morning", "صباح الخير", PHRASE, A1, "greetings", "/ɟynajdɯn/")
            v("iyi_aksamlar", "iyi akşamlar", "good evening", "مساء الخير", PHRASE, A1, "greetings", "/iji akʃamlaɾ/")
            v("tesekkurler", "teşekkür ederim", "thank you", "شكرًا", PHRASE, A1, "greetings", "/teʃecːyɾ edeɾim/",
                ex = "Çok teşekkür ederim!", exEn = "Thank you very much!", exAr = "شكرًا جزيلًا!")
            v("lutfen", "lütfen", "please", "من فضلك", ADVERB, A1, "greetings", "/lytfen/")
            v("ozur", "özür dilerim", "I'm sorry", "أنا آسف", PHRASE, A1, "greetings", "/øzyɾ dileɾim/")
            v("gorusuruz", "görüşürüz", "see you, goodbye", "إلى اللقاء", PHRASE, A1, "greetings", "/ɟøɾyʃyɾyz/")
            v("benim_adim", "benim adım…", "my name is…", "اسمي…", PHRASE, A1, "greetings", "/benim adɯm/")
            v("memnun_oldum", "memnun oldum", "nice to meet you", "تشرّفت بمعرفتك", PHRASE, A1, "greetings", "/memnun oldum/")
            v("aile", "aile", "family", "العائلة", NOUN, A1, "people", "/aːile/")
            v("anne", "anne", "mother", "الأم", NOUN, A1, "people", "/anːe/",
                ex = "Annem öğretmen.", exEn = "My mother is a teacher.", exAr = "أمي معلّمة.")
            v("baba", "baba", "father", "الأب", NOUN, A1, "people", "/baba/")
            v("kardes", "kardeş", "brother or sister (sibling)", "الأخ أو الأخت", NOUN, A1, "people", "/kaɾdeʃ/")
            v("abla", "abla", "older sister", "الأخت الكبرى", NOUN, A1, "people", "/abla/")
            v("arkadas", "arkadaş", "friend", "الصديق", NOUN, A1, "people", "/aɾkadaʃ/",
                ex = "O benim en iyi arkadaşım.", exEn = "He/She is my best friend.", exAr = "إنه أعزّ أصدقائي.")
            v("su", "su", "water", "الماء", NOUN, A1, "food", "/su/")
            v("cay", "çay", "tea", "الشاي", NOUN, A1, "food", "/tʃaj/")
            v("kahve", "kahve", "coffee", "القهوة", NOUN, A1, "food", "/kahve/")
            v("ekmek", "ekmek", "bread", "الخبز", NOUN, A1, "food", "/ecmec/")
            v("hesap", "hesap", "the bill (restaurant); account", "الحساب", NOUN, A2, "food", "/hesap/",
                ex = "Hesap, lütfen.", exEn = "The bill, please.", exAr = "الحساب من فضلك.")
            v("alabilir_miyim", "… alabilir miyim?", "can I have…?", "هل يمكنني أن آخذ…؟", PHRASE, A1, "food",
                ex = "Bir çay alabilir miyim?", exEn = "Can I have a tea?", exAr = "هل يمكنني أن آخذ كوب شاي؟")
            v("lezzetli", "lezzetli", "delicious", "لذيذ", ADJECTIVE, A1, "food", "/lezːetli/")
            v("bugun", "bugün", "today", "اليوم", ADVERB, A1, "time", "/buɟyn/")
            v("yarin", "yarın", "tomorrow", "غدًا", ADVERB, A1, "time", "/jaɾɯn/",
                ex = "Yarın görüşürüz!", exEn = "See you tomorrow!", exAr = "نلتقي غدًا!")
            v("dun", "dün", "yesterday", "أمس", ADVERB, A1, "time", "/dyn/")
            v("hafta", "hafta", "week", "الأسبوع", NOUN, A1, "time", "/hafta/")
            v("istasyon", "istasyon", "station", "المحطة", NOUN, A2, "travel", "/istasjon/",
                ex = "İstasyon nerede?", exEn = "Where is the station?", exAr = "أين المحطة؟")
            v("bilet", "bilet", "ticket", "التذكرة", NOUN, A2, "travel", "/bilet/")
            v("sola", "sola", "to the left", "إلى اليسار", ADVERB, A1, "travel", "/sola/")
            v("saga", "sağa", "to the right", "إلى اليمين", ADVERB, A1, "travel", "/saːa/")
            v("duz", "düz", "straight on", "إلى الأمام مباشرة", ADVERB, A2, "travel", "/dyz/")
            v("kalkmak", "kalkmak", "to get up; to depart", "ينهض؛ يغادر (القطار)", VERB, A2, "routine", "/kalkmak/",
                ex = "Her sabah yedide kalkıyorum.", exEn = "I get up at seven every morning.", exAr = "أنهض كل صباح في السابعة.")
            v("calismak", "çalışmak", "to work; to study", "يعمل؛ يذاكر", VERB, A1, "routine", "/tʃalɯʃmak/")
            v("okumak", "okumak", "to read; to study (at school)", "يقرأ؛ يدرس", VERB, A1, "routine", "/okumak/")
            v("genellikle", "genellikle", "usually", "عادةً", ADVERB, A2, "routine", "/ɟenelːikle/")
            v("yorgun", "yorgun", "tired", "متعب", ADJECTIVE, A1, "feelings", "/joɾɡun/")
            v("mutlu", "mutlu", "happy", "سعيد", ADJECTIVE, A1, "feelings", "/mutlu/")
            v("bence", "bence", "in my opinion", "في رأيي", ADVERB, B1, "feelings", "/bendʒe/",
                ex = "Bence bu iyi bir fikir.", exEn = "In my opinion this is a good idea.", exAr = "في رأيي هذه فكرة جيدة.")
            v("ragmen", "-e rağmen", "despite, although", "على الرغم من", PHRASE, B2, "feelings", "/ɾaːmen/")
            v("ne_kadar", "ne kadar?", "how much is it?", "بكم؟", PHRASE, A1, "shopping", "/ne kadaɾ/")
            v("pahali", "pahalı", "expensive", "غالٍ", ADJECTIVE, A1, "shopping", "/pahalɯ/")
            v("ucuz", "ucuz", "cheap", "رخيص", ADJECTIVE, A1, "shopping", "/udʒuz/")

            // Home and weather
            v("ev", "ev", "house, home", "البيت", NOUN, A1, "home", "/ev/",
                ex = "Evim küçük ama güzel.", exEn = "My house is small but nice.", exAr = "بيتي صغير لكنه جميل.")
            v("mutfak", "mutfak", "kitchen", "المطبخ", NOUN, A1, "home")
            v("yatak_odasi", "yatak odası", "bedroom", "غرفة النوم", NOUN, A1, "home")
            v("seviyorum", "seviyorum", "I love, I like", "أحبّ", VERB, A1, "home",
                ex = "Güneşli günleri çok seviyorum.", exEn = "I really love sunny days.", exAr = "أحبّ الأيام المشمسة كثيرًا.")
            v("hava_durumu", "hava durumu", "weather forecast", "النشرة الجوية", NOUN, A2, "weather")
            v("hava_sicak", "hava sıcak", "it's hot", "الجو حار", PHRASE, A1, "weather")
            v("hava_soguk", "hava soğuk", "it's cold", "الجو بارد", PHRASE, A1, "weather",
                ex = "Ocak ayında hava çok soğuk.", exEn = "In January it's very cold.", exAr = "في يناير يكون الجو باردًا جدًا.")
            v("yagmur_yagiyor", "yağmur yağıyor", "it's raining", "إنها تمطر", PHRASE, A1, "weather")
            // Health
            v("doktor", "doktor", "doctor", "الطبيب", NOUN, A1, "health")
            v("agriyor", "… ağrıyor", "my … hurts", "يؤلمني…", PHRASE, A2, "health",
                ex = "Başım ağrıyor.", exEn = "I have a headache.", exAr = "رأسي يؤلمني.")
            v("bas", "baş", "head", "الرأس", NOUN, A1, "health")
            v("ates", "ateş", "fever; fire", "الحمّى؛ النار", NOUN, A2, "health")
            v("eczane", "eczane", "pharmacy", "الصيدلية", NOUN, A1, "health")
            v("randevu", "randevu", "appointment", "الموعد", NOUN, A2, "health",
                ex = "Doktordan randevu almak istiyorum.", exEn = "I'd like to make an appointment with the doctor.", exAr = "أودّ حجز موعد مع الطبيب.")
            v("hasta", "hasta", "ill, sick; patient", "مريض", ADJECTIVE, A1, "health")
            // Work (professional track)
            v("toplanti", "toplantı", "meeting", "الاجتماع", NOUN, A2, "business",
                ex = "Toplantı saat onda başlıyor.", exEn = "The meeting starts at ten.", exAr = "يبدأ الاجتماع في العاشرة.", tags = biz, source = pro)
            v("sirket", "şirket", "company", "الشركة", NOUN, A2, "business", tags = biz, source = pro)
            v("musteri", "müşteri", "client, customer", "العميل", NOUN, A2, "business", tags = biz, source = pro)
            v("teklif", "teklif", "offer, quote, proposal", "العرض، عرض السعر", NOUN, B1, "business", tags = biz, source = pro)
            v("son_tarih", "son tarih", "deadline", "الموعد النهائي", NOUN, B1, "business",
                ex = "Son tarih çok yakın.", exEn = "The deadline is very close.", exAr = "الموعد النهائي قريب جدًا.", tags = biz, source = pro)
            v("sunum", "sunum", "presentation", "العرض التقديمي", NOUN, B1, "business", tags = biz, source = pro)
            v("ekte", "ekte … bulabilirsiniz", "please find attached… (formal email)", "تجدون مرفقًا…", PHRASE, B1, "business", tags = biz, source = pro)
            v("sayin", "Sayın …", "Dear … (formal letter opening)", "حضرة… (افتتاحية رسمية)", PHRASE, B1, "business", tags = biz, source = pro)

            // Next steps: plans and wishes
            v("plan", "plan", "plan", "الخطة", NOUN, B1, "plans")
            v("gelecek_hafta", "gelecek hafta / gelecek yıl", "next week / next year", "الأسبوع القادم / العام القادم", PHRASE, A2, "plans",
                ex = "Gelecek yıl Ankara'ya taşınacağım.", exEn = "Next year I'm going to move to Ankara.", exAr = "في العام القادم سأنتقل إلى أنقرة.")
            v("seyahat_etmek", "seyahat etmek", "to travel", "يسافر", VERB, A2, "plans")
            v("biriktirmek", "para biriktirmek", "to save money", "يدّخر المال", VERB, B1, "plans")
            v("belki", "belki", "maybe, perhaps", "ربما", ADVERB, A2, "plans")
            v("tatil", "tatil", "holiday, vacation", "العطلة", NOUN, A2, "plans")
            v("meli", "-meli / -malı", "must, should", "يجب أن", PHRASE, B2, "wishes",
                ex = "Daha çok dinlenmelisin.", exEn = "You should rest more.", exAr = "يجب أن ترتاح أكثر.")
            v("mesini_istiyorum", "-mesini istiyorum", "I want (someone) to…", "أريد أن (يفعل شخص آخر)…", PHRASE, B2, "wishes")
            v("umarim", "umarım", "I hope", "آمل", ADVERB, B1, "wishes")
            v("sans", "şans", "luck", "الحظ", NOUN, B1, "wishes")
            v("basarmak", "başarmak", "to succeed", "ينجح", VERB, B1, "wishes")
            v("sinav", "sınav", "exam", "الامتحان", NOUN, A2, "wishes")

            // Banking and finance (professional track)
            v("fin_hesap", "banka hesabı", "bank account", "الحساب المصرفي", NOUN, A2, "finance", tags = fin, source = pro)
            v("fin_kredi", "kredi", "loan, credit", "القرض", NOUN, B1, "finance",
                ex = "Banka krediyi onayladı.", exEn = "The bank approved the loan.", exAr = "وافق البنك على القرض.", tags = fin, source = pro)
            v("fin_faiz", "faiz oranı", "interest rate", "سعر الفائدة", NOUN, B1, "finance", tags = fin, source = pro)
            v("fin_taksit", "taksit", "instalment", "القسط", NOUN, B1, "finance", tags = fin, source = pro)
            v("fin_teminat", "teminat", "collateral, guarantee", "الضمان", NOUN, B2, "finance", tags = fin, source = pro)
            v("fin_temerrut", "temerrüt", "default (failure to pay)", "التعثّر عن السداد", NOUN, B2, "finance", tags = fin, source = pro)
            v("fin_bilanco", "bilanço", "balance sheet", "الميزانية العمومية", NOUN, B2, "finance", tags = fin, source = pro)
            v("fin_likidite", "likidite", "liquidity", "السيولة", NOUN, B2, "finance", tags = fin, source = pro)
            // Daily life and travel
            v("ev_affedersiniz", "affedersiniz", "excuse me", "عفوًا", PHRASE, A1, "essentials", ex = "Affedersiniz, bu yer boş mu?", exEn = "Excuse me, is this seat free?", exAr = "عفوًا، هل هذا المقعد شاغر؟")
            v("ev_anlamiyorum", "anlamıyorum", "I don't understand", "لا أفهم", PHRASE, A1, "essentials")
            v("ev_yavas", "Daha yavaş konuşabilir misiniz?", "Could you speak more slowly?", "هل يمكنك التحدث ببطء أكثر؟", PHRASE, A1, "essentials")
            v("ev_tekrar", "Tekrar eder misiniz?", "Could you repeat that?", "هل يمكنك أن تعيد؟", PHRASE, A1, "essentials")
            v("ev_tuvalet", "Tuvalet nerede?", "Where is the toilet?", "أين دورة المياه؟", PHRASE, A1, "essentials")
            v("ev_yardim", "Bana yardım edebilir misiniz?", "Can you help me?", "هل يمكنك مساعدتي؟", PHRASE, A1, "essentials")
            v("ev_bilmiyorum", "bilmiyorum", "I don't know", "لا أعرف", PHRASE, A1, "essentials")
            v("ev_rica", "rica ederim", "you're welcome", "عفوًا (ردًّا على الشكر)", PHRASE, A1, "essentials")
            v("ev_imdat", "İmdat!", "Help!", "النجدة!", PHRASE, A1, "emergency")
            v("ev_polis", "polis", "the police", "الشرطة", NOUN, A1, "emergency")
            v("ev_ambulans", "Ambulans çağırın!", "Call an ambulance!", "اتصلوا بالإسعاف!", PHRASE, A2, "emergency", ex = "Lütfen hemen ambulans çağırın!", exEn = "Please call an ambulance right away!", exAr = "من فضلكم اتصلوا بالإسعاف فورًا!")
            v("ev_pasaport", "Pasaportumu kaybettim.", "I've lost my passport.", "أضعتُ جواز سفري.", PHRASE, A2, "emergency")
            v("ev_calindi", "Çantam çalındı.", "My bag has been stolen.", "سُرقت حقيبتي.", PHRASE, A2, "emergency")
            v("ev_buyukelcilik", "büyükelçilik", "the embassy", "السفارة", NOUN, A2, "emergency")
            v("ev_hastane", "hastane", "hospital", "المستشفى", NOUN, A1, "emergency")
            v("ev_acil", "acil durum numarası", "emergency number", "رقم الطوارئ", NOUN, A2, "emergency")
            v("ev_masa", "iki kişilik bir masa, lütfen", "a table for two, please", "طاولة لشخصين من فضلك", PHRASE, A1, "restaurant", ex = "İyi akşamlar, iki kişilik bir masa, lütfen.", exEn = "Good evening, a table for two, please.", exAr = "مساء الخير، طاولة لشخصين من فضلك.")
            v("ev_oneri", "Ne önerirsiniz?", "What do you recommend?", "بماذا تنصحني؟", PHRASE, A2, "restaurant")
            v("ev_alerji", "… alerjim var", "I'm allergic to…", "عندي حساسية من…", PHRASE, A2, "restaurant")
            v("ev_vejetaryen", "vejetaryen", "vegetarian", "نباتي", ADJECTIVE, A2, "restaurant")
            v("ev_meze", "meze", "small starter dishes (meze)", "المقبّلات (المازة)", NOUN, A2, "restaurant")
            v("ev_ana_yemek", "ana yemek", "main course", "الطبق الرئيسي", NOUN, A2, "restaurant")
            v("ev_tatli", "tatlı", "dessert; sweet", "الحلوى؛ حلو", NOUN, A1, "restaurant")
            v("ev_bahsis", "bahşiş", "tip", "البقشيش", NOUN, A2, "restaurant")
            v("ev_rezervasyon", "rezervasyon", "reservation", "الحجز", NOUN, A2, "hotel", ex = "Hasan adına bir rezervasyonum var.", exEn = "I have a reservation under the name Hasan.", exAr = "لديّ حجز باسم حسن.")
            v("ev_tek_kisilik", "tek kişilik oda", "single room", "غرفة لشخص واحد", NOUN, A2, "hotel")
            v("ev_cift_kisilik", "çift kişilik oda", "double room", "غرفة مزدوجة", NOUN, A2, "hotel")
            v("ev_anahtar", "anahtar", "key", "المفتاح", NOUN, A1, "hotel")
            v("ev_kahvalti", "Kahvaltı dahil mi?", "Is breakfast included?", "هل الفطور مشمول؟", PHRASE, A2, "hotel")
            v("ev_wifi", "Wi-Fi şifresi ne?", "What's the Wi-Fi password?", "ما كلمة مرور الواي فاي؟", PHRASE, A2, "hotel")
            v("ev_resepsiyon", "resepsiyon", "reception", "الاستقبال", NOUN, A2, "hotel")
            v("ev_cikis", "çıkış yapmak", "to check out", "تسجيل المغادرة", VERB, A2, "hotel")
            v("ev_peron", "peron", "platform", "الرصيف", NOUN, A2, "transport", ex = "Tren üçüncü perondan kalkıyor.", exEn = "The train leaves from platform three.", exAr = "يغادر القطار من الرصيف الثالث.")
            v("ev_gidis_donus", "gidiş-dönüş bilet", "a return ticket", "تذكرة ذهاب وعودة", NOUN, A2, "transport")
            v("ev_tek_yon", "tek yön bilet", "a single (one-way) ticket", "تذكرة ذهاب فقط", NOUN, A2, "transport")
            v("ev_kalkis", "kalkış", "departure", "المغادرة", NOUN, A2, "transport")
            v("ev_rotar", "rötar", "delay (of a train, bus or plane)", "التأخير", NOUN, A2, "transport")
            v("ev_binis", "biniş kartı", "boarding pass", "بطاقة الصعود إلى الطائرة", NOUN, A2, "transport")
            v("ev_bagaj", "bagaj", "luggage", "الأمتعة", NOUN, A2, "transport")
            v("ev_durak", "otobüs durağı", "bus stop", "موقف الحافلات", NOUN, A1, "transport")
            v("ev_muze", "müze", "museum", "المتحف", NOUN, A1, "sightseeing", ex = "Müze saat kaçta açılıyor?", exEn = "What time does the museum open?", exAr = "متى يفتح المتحف؟")
            v("ev_ziyaret_saatleri", "ziyaret saatleri", "opening hours (for visitors)", "مواعيد الزيارة", NOUN, A2, "sightseeing")
            v("ev_giris_bileti", "giriş bileti", "entrance ticket", "تذكرة الدخول", NOUN, A2, "sightseeing")
            v("ev_rehberli_tur", "rehberli tur", "guided tour", "جولة مع مرشد", NOUN, A2, "sightseeing")
            v("ev_harita", "harita", "map", "الخريطة", NOUN, A1, "sightseeing")
            v("ev_fotograf", "Fotoğrafımızı çekebilir misiniz?", "Could you take a photo of us?", "هل يمكنك أن تلتقط لنا صورة؟", PHRASE, A2, "sightseeing")
            v("ev_eski_sehir", "eski şehir", "old town", "المدينة القديمة", NOUN, A2, "sightseeing")
            v("ev_hediyelik", "hediyelik eşya", "souvenir", "تذكار", NOUN, A2, "sightseeing")
        }
        val vocabulary = vb.items
        val f = LessonFactory(L, vocabulary)

        val grammar = listOf(
            grammar("ben_im", L, A1, g("'To be': personal endings", "الكينونة: لواحق الضمائر"),
                g("Turkish has no separate verb 'to be'. A personal ending is added to the noun or adjective: öğrenciyim (I am a student), öğrencisin (you are), öğrenci (he/she is), öğrenciyiz (we are). Age uses the same pattern: yirmi yaşındayım (I am twenty).",
                    "لا يوجد في التركية فعل مستقل للكينونة، بل تُضاف لاحقة الضمير إلى الاسم أو الصفة: ‏öğrenciyim (أنا طالب) وöğrencisin (أنت طالب) وöğrenci (هو طالب) وöğrenciyiz (نحن طلاب). وكذلك العمر: ‏yirmi yaşındayım (عمري عشرون عامًا)."),
                listOf(ex("Ben öğrenciyim.", "I am a student.", "أنا طالب."), ex("Yirmi yaşındayım.", "I am twenty.", "عمري عشرون عامًا."),
                    ex("Ben öğrenci var.", "'var' means 'there is'; use the ending: öğrenciyim", "‏var تعني «يوجد»؛ استخدم اللاحقة: öğrenciyim", wrong = true)),
                listOf(g("✗ Ben öğrenci → ✓ Ben öğrenciyim (the ending is needed).", "✗ Ben öğrenci ← ✓ Ben öğrenciyim (اللاحقة ضرورية)."))),
            grammar("vowel_harmony", L, A1, g("Vowel harmony and the plural -ler / -lar", "انسجام الحروف الصوتية والجمع ‎-ler / ‎-lar"),
                g("Suffixes change their vowel to match the last vowel of the word. After e, i, ö, ü the plural is -ler (evler, günler); after a, ı, o, u it is -lar (kitaplar, okullar).",
                    "تتغيّر حركة اللاحقة لتنسجم مع آخر حرف صوتي في الكلمة. بعد e وi وö وü يكون الجمع ‎-ler (evler وgünler)، وبعد a وı وo وu يكون ‎-lar (kitaplar وokullar)."),
                listOf(ex("evler", "houses", "بيوت"), ex("kitaplar", "books", "كتب"), ex("kitapler", "After 'a' the plural is -lar", "بعد a يكون الجمع ‎-lar", wrong = true)),
                listOf(g("✗ okuller → ✓ okullar (last vowel u).", "✗ okuller ← ✓ okullar (آخر حرف صوتي u)."))),
            grammar("locative", L, A1, g("Where? The locative -de / -da", "أين؟ لاحقة المكان ‎-de / ‎-da"),
                g("Add -de or -da (by vowel harmony) for 'in, at, on': evde (at home), okulda (at school). After ç, f, h, k, p, s, ş, t it becomes -te / -ta: sokakta (in the street). Proper names take an apostrophe: İstanbul'da.",
                    "أضف ‎-de أو ‎-da (حسب الانسجام) بمعنى «في»: ‏evde (في البيت) وokulda (في المدرسة). وبعد ç وf وh وk وp وs وş وt تصبح ‎-te / ‎-ta: ‏sokakta (في الشارع). وتأخذ أسماء الأعلام فاصلة علوية: İstanbul'da."),
                listOf(ex("Annem evde.", "My mother is at home.", "أمي في البيت."), ex("Ali İstanbul'da çalışıyor.", "Ali works in Istanbul.", "يعمل علي في إسطنبول."),
                    ex("sokakda", "After k use -ta: sokakta", "بعد k تُستخدم ‎-ta: sokakta", wrong = true)),
                listOf(g("✗ İstanbulda → ✓ İstanbul'da (apostrophe with names).", "✗ İstanbulda ← ✓ İstanbul'da (فاصلة علوية مع الأعلام)."))),
            grammar("present_iyor", L, A1, g("The present continuous -iyor", "المضارع المستمر ‎-iyor"),
                g("Verb stem + -iyor (-ıyor, -uyor, -üyor by harmony) + personal ending: geliyorum (I am coming), çalışıyorsun (you are working), okuyor (he/she is reading). It is used for now and for routines.",
                    "جذر الفعل + ‎-iyor (أو ‎-ıyor و‎-uyor و‎-üyor حسب الانسجام) + لاحقة الضمير: ‏geliyorum (أنا آتٍ) وçalışıyorsun (أنت تعمل) وokuyor (هو يقرأ). ويُستخدم للحاضر وللعادات."),
                listOf(ex("Şimdi kahve içiyorum.", "I'm drinking coffee now.", "أشرب القهوة الآن."), ex("Her gün Türkçe çalışıyoruz.", "We study Turkish every day.", "ندرس التركية كل يوم.")),
                listOf(g("Verbs ending in a vowel drop it: oku- + -iyor → okuyor; bekle- → bekliyor.", "الأفعال المنتهية بحرف صوتي يُحذف حرفها: oku- + -iyor ← okuyor وbekle- ← bekliyor."))),
            grammar("politeness", L, A2, g("Polite requests", "الطلب المهذب"),
                g("Ask with '… alabilir miyim?' (can I have …?) or '… rica edebilir miyim?'. Add lütfen. To a stranger or a waiter use siz: Hesabı getirebilir misiniz?",
                    "اطلب بصيغة '… alabilir miyim?' (هل يمكنني أن آخذ…؟) أو '… rica edebilir miyim?' وأضف lütfen. ومع الغرباء أو النادل استخدم siz: ‏Hesabı getirebilir misiniz?"),
                listOf(ex("Bir çay alabilir miyim?", "Can I have a tea?", "هل يمكنني أن آخذ كوب شاي؟"), ex("Hesabı getirebilir misiniz?", "Could you bring the bill?", "هل يمكنك إحضار الحساب؟")),
                listOf(g("'Çay ver!' (give tea!) sounds rude to a waiter.", "عبارة «Çay ver!» (أعطني شايًا!) تبدو فظّة مع النادل."))),
            grammar("past_di", L, B1, g("The past tense -di", "الماضي ‎-di"),
                g("Verb stem + -di (-dı, -du, -dü) + ending: geldim, geldin, geldi, geldik. After a voiceless consonant (ç, f, h, k, p, s, ş, t) d becomes t: gittim, çalıştım, yaptık.",
                    "جذر الفعل + ‎-di (أو ‎-dı و‎-du و‎-dü) + اللاحقة: ‏geldim وgeldin وgeldi وgeldik. وبعد حرف ساكن مهموس (ç وf وh وk وp وs وş وt) تصبح d حرف t: ‏gittim وçalıştım وyaptık."),
                listOf(ex("Dün sinemaya gittim.", "Yesterday I went to the cinema.", "أمس ذهبت إلى السينما."), ex("Akşam yemeği evde yedik.", "We had dinner at home.", "تناولنا العشاء في البيت."),
                    ex("Dün çalışdım.", "After ş use -tı: çalıştım", "بعد ş تُستخدم ‎-tı: çalıştım", wrong = true)),
                listOf(g("✗ yapdım → ✓ yaptım.", "✗ yapdım ← ✓ yaptım."))),
            grammar("hava", L, A1, g("Talking about the weather", "الحديث عن الطقس"),
                g("Use hava (the weather/air) with an adjective: Hava sıcak, Hava soğuk, Hava güzel. Rain and snow are 'falling': Yağmur yağıyor, Kar yağıyor.",
                    "استخدم hava (الجو) مع صفة: ‏Hava sıcak وHava soğuk وHava güzel. أما المطر والثلج فهما «يهطلان»: ‏Yağmur yağıyor وKar yağıyor."),
                listOf(ex("Bugün hava çok güzel.", "The weather is very nice today.", "الجو جميل جدًا اليوم."), ex("Sabahtan beri yağmur yağıyor.", "It's been raining since the morning.", "إنها تمطر منذ الصباح."),
                    ex("Bugün sıcak var.", "Say 'hava sıcak'", "قل hava sıcak", wrong = true)),
                listOf(g("✗ Yağmur var (for 'it's raining') → ✓ Yağmur yağıyor.", "✗ Yağmur var (بمعنى «إنها تمطر») ← ✓ Yağmur yağıyor."))),
            grammar("siz", L, A2, g("Formal 'you': siz", "المخاطبة الرسمية: siz"),
                g("Use siz with clients, officials and people you don't know, even for one person. Verbs take -siniz: Gelebilir misiniz? Titles follow the first name: Ayşe Hanım, Mehmet Bey.",
                    "استخدم siz مع العملاء والموظفين ومن لا تعرفهم، حتى لو كان شخصًا واحدًا. ويأخذ الفعل اللاحقة ‎-siniz: ‏Gelebilir misiniz? وتأتي الألقاب بعد الاسم الأول: Ayşe Hanım وMehmet Bey."),
                listOf(ex("Tarihi onaylayabilir misiniz?", "Could you confirm the date?", "هل يمكنكم تأكيد الموعد؟"),
                    ex("Bey Mehmet, merhaba.", "The title comes after the name: Mehmet Bey", "يأتي اللقب بعد الاسم: Mehmet Bey", wrong = true)),
                listOf(g("Switching from siz to sen with a client can seem too familiar.", "الانتقال من siz إلى sen مع عميل قد يبدو تبسّطًا زائدًا."))),
            grammar("future", L, B1, g("The future -ecek / -acak", "المستقبل ‎-ecek / ‎-acak"),
                g("Verb stem + -ecek or -acak + ending: geleceğim (I will come), yapacaksın (you will do), yağacak (it will rain). Before a vowel the k softens to ğ: gideceğim.",
                    "جذر الفعل + ‎-ecek أو ‎-acak + اللاحقة: ‏geleceğim (سآتي) وyapacaksın (ستفعل) وyağacak (ستمطر). وقبل حرف صوتي يلين k فيصبح ğ: ‏gideceğim."),
                listOf(ex("Akşam evde yemek yiyeceğiz.", "We're going to eat at home this evening.", "سنتناول الطعام في البيت هذا المساء."),
                    ex("Gelecek yıl Ankara'da yaşayacağım.", "Next year I'll live in Ankara.", "في العام القادم سأعيش في أنقرة."),
                    ex("Yarın gidecekim.", "k becomes ğ before a vowel: gideceğim", "يصبح k حرف ğ قبل حرف صوتي: gideceğim", wrong = true)),
                listOf(g("Verbs ending in a vowel add y: oku- → okuyacağım; bekle- → bekleyeceğim.", "الأفعال المنتهية بحرف صوتي تضيف y: oku- ← okuyacağım وbekle- ← bekleyeceğim."))),
            grammar("necessity", L, B2, g("Must and want someone to: -meli, -mesini istiyorum", "الوجوب والطلب من الآخرين: ‎-meli و‎-mesini istiyorum"),
                g("-meli / -malı + ending expresses 'must, should': gitmeliyim (I must go), dinlenmelisin (you should rest). '-mesi gerekiyor' is a softer 'need to': Gitmem gerekiyor. To want someone else to do something, use the verbal noun with a possessive: Gelmeni istiyorum (I want you to come).",
                    "‏-meli / ‎-malı مع اللاحقة تعبّر عن الوجوب: ‏gitmeliyim (يجب أن أذهب) وdinlenmelisin (يجب أن ترتاح). و‎-mesi gerekiyor صيغة ألطف للحاجة: ‏Gitmem gerekiyor. وللطلب من شخص آخر يُستخدم المصدر مع لاحقة الملكية: ‏Gelmeni istiyorum (أريدك أن تأتي)."),
                listOf(ex("Daha çok su içmelisin.", "You should drink more water.", "يجب أن تشرب ماءً أكثر."),
                    ex("Sınavı geçmeni istiyorum.", "I want you to pass the exam.", "أريدك أن تنجح في الامتحان."),
                    ex("Sen gelmek istiyorum.", "Use the possessive form: Gelmeni istiyorum", "استخدم صيغة الملكية: Gelmeni istiyorum", wrong = true)),
                listOf(g("✗ Senin gelmek istiyorum → ✓ Gelmeni istiyorum.", "✗ Senin gelmek istiyorum ← ✓ Gelmeni istiyorum."))),
        )

        val lessons = listOf(
            f.lesson("selamlasma", A1, "basics", g("Greetings", "التحيات"), g("Say hello and introduce yourself.", "ألقِ التحية وعرّف بنفسك."),
                Skill.SPEAKING, listOf("merhaba", "gunaydin", "iyi_aksamlar", "tesekkurler", "lutfen", "ozur", "gorusuruz", "benim_adim", "memnun_oldum"), "ben_im",
                extra = listOf(
                    fill("s1", L, "___ adım Ayşe.", listOf("Benim")),
                    fill("s2", L, "Ben ___. (öğrenci, I am a student)", listOf("öğrenciyim")),
                ), minutes = 6),
            f.lesson("aile", A1, "basics", g("Family", "العائلة"), g("Talk about your family and make plurals.", "تحدّث عن عائلتك وكوّن الجمع."),
                Skill.VOCABULARY, listOf("aile", "anne", "baba", "kardes", "abla", "arkadas"), "vowel_harmony",
                extra = listOf(choice("a1", L, "kitap → plural", listOf("kitaplar", "kitapler", "kitaplır"), 0,
                    explanation = g("The last vowel is a, so the plural is -lar.", "آخر حرف صوتي a، لذا يكون الجمع ‎-lar."))),
                prerequisites = listOf("selamlasma")),
            f.lesson("kafe", A1, "everyday", g("At the café", "في المقهى"), g("Order politely with '… alabilir miyim?'.", "اطلب بأدب باستخدام '… alabilir miyim?'."),
                Skill.SPEAKING, listOf("su", "cay", "kahve", "ekmek", "hesap", "alabilir_miyim", "lezzetli"), "politeness",
                extra = listOf(
                    order("k1", L, listOf("Bir", "çay", "alabilir", "miyim?"), g("Can I have a tea?", "هل يمكنني أن آخذ كوب شاي؟")),
                    dictation("k2", L, "Hesap, lütfen.", listOf("Hesap lütfen")),
                ), prerequisites = listOf("aile")),
            f.lesson("gunluk", A2, "everyday", g("Daily routine", "الروتين اليومي"), g("Describe your day with the present continuous.", "صِف يومك بالمضارع المستمر."),
                Skill.WRITING, listOf("kalkmak", "calismak", "okumak", "genellikle", "bugun", "yarin", "hafta"), "present_iyor",
                extra = listOf(
                    fill("g1", L, "Her sabah yedide ___. (kalkmak, I)", listOf("kalkıyorum")),
                    fill("g2", L, "Biz Türkçe ___. (öğrenmek, we)", listOf("öğreniyoruz")),
                ), prerequisites = listOf("kafe")),
            f.lesson("yolculuk", A2, "travel", g("Getting around", "التنقّل"), g("Ask for directions and say where things are.", "اسأل عن الاتجاهات وقل أين توجد الأشياء."),
                Skill.LISTENING, listOf("istasyon", "bilet", "sola", "saga", "duz"), "locative",
                extra = listOf(
                    fill("y1", L, "Annem ___. (ev, at home)", listOf("evde")),
                    dictation("y2", L, "Düz gidin, sonra sola dönün.", listOf("Düz gidin sonra sola dönün")),
                ), prerequisites = listOf("gunluk")),
            f.lesson("alisveris", A2, "travel", g("Shopping", "التسوّق"), g("Ask prices and give opinions.", "اسأل عن الأسعار وعبّر عن رأيك."),
                Skill.SPEAKING, listOf("ne_kadar", "pahali", "ucuz", "mutlu", "yorgun"), "vowel_harmony",
                extra = listOf(fill("al1", L, "Bu elbise çok ___. (expensive)", listOf("pahalı"))),
                prerequisites = listOf("yolculuk")),
            f.lesson("gecmis", B1, "travel", g("Talking about the past", "الحديث عن الماضي"), g("Use the -di past and give opinions.", "استخدم الماضي ‎-di وعبّر عن آرائك."),
                Skill.SPEAKING, listOf("dun", "bence", "ragmen", "mutlu"), "past_di",
                extra = listOf(
                    fill("ge1", L, "Dün sinemaya ___. (gitmek, I)", listOf("gittim")),
                    fill("ge2", L, "Biz restoranda yemek ___. (yemek, we)", listOf("yedik")),
                ), prerequisites = listOf("alisveris")),
            f.lesson("ev_hava", A2, "home", g("Home and weather", "البيت والطقس"), g("Describe your home and talk about the weather.", "صِف بيتك وتحدّث عن الطقس."),
                Skill.VOCABULARY, listOf("ev", "mutfak", "yatak_odasi", "seviyorum", "hava_durumu", "hava_sicak", "hava_soguk", "yagmur_yagiyor"), "hava",
                extra = listOf(
                    fill("h1", L, "Kışın hava çok ___.", listOf("soğuk")),
                    fill("h2", L, "Şemsiyeni al, yağmur ___!", listOf("yağıyor")),
                    order("h3", L, listOf("Evimi", "çok", "seviyorum"), g("I really love my home.", "أحبّ بيتي كثيرًا.")),
                ), prerequisites = listOf("gecmis")),
            f.lesson("saglik", A2, "home", g("At the doctor's", "عند الطبيب"), g("Say what hurts and make an appointment.", "قل ما يؤلمك واحجز موعدًا."),
                Skill.SPEAKING, listOf("doktor", "agriyor", "bas", "ates", "eczane", "randevu", "hasta"), "siz",
                extra = listOf(
                    fill("sa1", L, "Başım ___.", listOf("ağrıyor")),
                    fill("sa2", L, "Doktordan ___ almak istiyorum.", listOf("randevu")),
                    dictation("sa3", L, "Dünden beri ateşim var.", listOf("Dünden beri ateşim var")),
                ), prerequisites = listOf("ev_hava")),
            f.lesson("is", B1, "pro_business", g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), g("Quotes, deadlines and polite requests with siz.", "عروض الأسعار والمواعيد النهائية والطلبات المهذبة بصيغة siz."),
                Skill.WRITING, listOf("toplanti", "sirket", "musteri", "teklif", "son_tarih", "sunum", "ekte", "sayin"), "siz",
                extra = listOf(
                    fill("i1", L, "Teklifi ___ bulabilirsiniz.", listOf("ekte")),
                    choice("i2", L, "Which is the right way to start a formal email?", listOf("Selam!", "Sayın Yılmaz,", "Naber?"), 1, Skill.FUNCTIONAL),
                    fill("i3", L, "Toplantı tarihini onaylayabilir ___?", listOf("misiniz")),
                    writing("i4", L, g("Write a short email to a client proposing a meeting next week.", "اكتب رسالة قصيرة إلى عميل تقترح فيها اجتماعًا الأسبوع القادم."), 40,
                        listOf("Sayın", "toplantı", "teklif", "Saygılarımla")),
                )),
            f.lesson("planlar", B1, "next", g("Plans for the future", "خطط المستقبل"), g("Talk about plans with the future -ecek / -acak.", "تحدّث عن الخطط بالمستقبل ‎-ecek / ‎-acak."),
                Skill.SPEAKING, listOf("plan", "gelecek_hafta", "seyahat_etmek", "biriktirmek", "belki", "tatil"), "future",
                extra = listOf(
                    fill("p1", L, "Gelecek yıl Kanada'ya ___. (gitmek, I)", listOf("gideceğim")),
                    fill("p2", L, "Yarın yağmur ___. (yağmak)", listOf("yağacak")),
                    order("p3", L, listOf("Tatil", "için", "para", "biriktireceğiz"), g("We're going to save money for the holiday.", "سنوفّر المال للعطلة.")),
                    writing("p4", L, g("Write about your plans for next year (30+ words).", "اكتب عن خططك للعام القادم (30 كلمة على الأقل)."), 30, listOf("gelecek yıl", "belki", "-ecek")),
                ), prerequisites = listOf("saglik")),
            f.lesson("istekler", B2, "next", g("Needs and wishes", "الضرورات والأمنيات"), g("Use -meli and '-mesini istiyorum'.", "استخدم ‎-meli و‎-mesini istiyorum."),
                Skill.GRAMMAR, listOf("meli", "mesini_istiyorum", "umarim", "sans", "basarmak", "sinav"), "necessity",
                extra = listOf(
                    fill("is1", L, "Ödevini ___. (yapmak, you must)", listOf("yapmalısın")),
                    fill("is2", L, "Yarın erken ___ gerekiyor. (kalkmak, I need to)", listOf("kalkmam")),
                    choice("is3", L, "Umarım sınavı ___.", listOf("geçersin", "geçmek", "geçti mi"), 0,
                        explanation = g("After umarım, use a finite verb (here the aorist).", "بعد umarım يأتي فعل مصرّف (هنا المضارع البسيط).")),
                    translate("is4", L, g("I want you to succeed.", "أريدك أن تنجح."), listOf("Başarmanı istiyorum", "Başarılı olmanı istiyorum")),
                ), prerequisites = listOf("planlar")),
            f.lesson("finans", B1, "pro_finance", g("Banking basics", "أساسيات البنوك"), g("Accounts, loans, interest and instalments.", "الحسابات والقروض والفوائد والأقساط."),
                Skill.VOCABULARY, listOf("fin_hesap", "fin_kredi", "fin_faiz", "fin_taksit", "fin_teminat", "fin_temerrut", "fin_bilanco", "fin_likidite"),
                extra = listOf(
                    fill("fi1", L, "Banka şirkete ___ verdi.", listOf("kredi")),
                    fill("fi2", L, "Faiz ___ yüzde beş.", listOf("oranı")),
                    choice("fi3", L, "Which document shows assets and liabilities?", listOf("bilanço", "teklif", "fiş"), 0, Skill.VOCABULARY),
                )),
            f.lesson("ev_essentials", A1, "survival", g("Most useful phrases", "العبارات الأكثر استخدامًا"), g("Phrases you will use every day and when travelling.", "عبارات تستخدمها كل يوم وفي السفر."),
                Skill.SPEAKING, listOf("ev_affedersiniz", "ev_anlamiyorum", "ev_yavas", "ev_tekrar", "ev_tuvalet", "ev_yardim", "ev_bilmiyorum", "ev_rica"),
                extra = listOf(
                    fill("ev1", L, "Daha ___ konuşabilir misiniz?", listOf("yavaş")),
                    choice("ev2", L, "Someone thanks you. You reply…", listOf("Rica ederim.", "Bilmiyorum.", "Affedersiniz."), 0, Skill.FUNCTIONAL),
                )),
            f.lesson("ev_emergency", A2, "survival", g("Emergencies and help", "الطوارئ وطلب المساعدة"), g("Ask for help, report a loss and find the police or a hospital.", "اطلب المساعدة وأبلغ عن فقدان شيء واعثر على الشرطة أو المستشفى."),
                Skill.SPEAKING, listOf("ev_imdat", "ev_polis", "ev_ambulans", "ev_pasaport", "ev_calindi", "ev_buyukelcilik", "ev_hastane", "ev_acil"),
                extra = listOf(
                    fill("ev3", L, "Pasaportumu ___. (kaybetmek, I lost)", listOf("kaybettim")),
                    choice("ev4", L, "Your bag was stolen:", listOf("Çantam çalındı.", "Çantamı seviyorum.", "Çantam pahalı."), 0, Skill.FUNCTIONAL),
                )),
            f.lesson("ev_restaurant", A2, "tourism", g("At the restaurant", "في المطعم"), g("Ask for a table, order a meal, mention allergies and pay.", "اطلب طاولة واطلب وجبة واذكر الحساسية وادفع."),
                Skill.SPEAKING, listOf("ev_masa", "ev_oneri", "ev_alerji", "ev_vejetaryen", "ev_meze", "ev_ana_yemek", "ev_tatli", "ev_bahsis"),
                extra = listOf(
                    fill("ev5", L, "İki ___ bir masa, lütfen.", listOf("kişilik")),
                    choice("ev6", L, "'Meze' on a Turkish menu are…", listOf("small starter dishes", "desserts", "drinks"), 0, Skill.VOCABULARY),
                )),
            f.lesson("ev_hotel", A2, "tourism", g("At the hotel", "في الفندق"), g("Check in, ask about breakfast and Wi-Fi, check out.", "سجّل وصولك واسأل عن الفطور والواي فاي وسجّل المغادرة."),
                Skill.LISTENING, listOf("ev_rezervasyon", "ev_tek_kisilik", "ev_cift_kisilik", "ev_anahtar", "ev_kahvalti", "ev_wifi", "ev_resepsiyon", "ev_cikis"),
                extra = listOf(
                    fill("ev7", L, "Kahvaltı ___ mi?", listOf("dahil")),
                    dictation("ev8", L, "İki gece için rezervasyonum var.", listOf("İki gece için rezervasyonum var")),
                )),
            f.lesson("ev_transport", A2, "tourism", g("Trains, buses and flights", "القطارات والحافلات والطائرات"), g("Buy tickets, find your platform and handle delays.", "اشترِ التذاكر واعثر على رصيفك وتعامل مع التأخير."),
                Skill.LISTENING, listOf("ev_peron", "ev_gidis_donus", "ev_tek_yon", "ev_kalkis", "ev_rotar", "ev_binis", "ev_bagaj", "ev_durak"),
                extra = listOf(
                    choice("ev9", L, "A return ticket is…", listOf("gidiş-dönüş bilet", "tek yön bilet", "peron"), 0, Skill.VOCABULARY),
                    dictation("ev10", L, "Tren yarım saat rötarlı.", listOf("Tren yarım saat rötarlı")),
                )),
            f.lesson("ev_sightseeing", A2, "tourism", g("Sightseeing", "السياحة والمعالم"), g("Museums, tickets, tours and photos.", "المتاحف والتذاكر والجولات والصور."),
                Skill.READING, listOf("ev_muze", "ev_ziyaret_saatleri", "ev_giris_bileti", "ev_rehberli_tur", "ev_harita", "ev_fotograf", "ev_eski_sehir", "ev_hediyelik"),
                extra = listOf(
                    fill("ev11", L, "Müze saat kaçta ___? (açılmak)", listOf("açılıyor")),
                    choice("ev12", L, "A guided tour is…", listOf("rehberli tur", "eski şehir", "hediyelik eşya"), 0, Skill.VOCABULARY),
                )),
        )
        val units = listOf(
            CourseUnit("tr_u_basics", L, A1, g("First steps", "الخطوات الأولى"), g("Greetings and family", "التحيات والعائلة"), listOf("tr_l_selamlasma", "tr_l_aile")),
            CourseUnit("tr_u_everyday", L, A1, g("Everyday life", "الحياة اليومية"), g("Café and routines", "المقهى والروتين"), listOf("tr_l_kafe", "tr_l_gunluk")),
            CourseUnit("tr_u_travel", L, A2, g("Out and about", "في الخارج"), g("Directions, shopping and the past", "الاتجاهات والتسوق والماضي"), listOf("tr_l_yolculuk", "tr_l_alisveris", "tr_l_gecmis")),
            CourseUnit("tr_u_home", L, A2, g("Home and health", "البيت والصحة"), g("Your home, the weather and the doctor", "بيتك والطقس والطبيب"), listOf("tr_l_ev_hava", "tr_l_saglik")),
            CourseUnit("tr_u_pro_business", L, B1, g("Turkish for work", "التركية للعمل"), g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), listOf("tr_l_is"), trackId = "business"),
            CourseUnit("tr_u_next", L, B1, g("Next steps", "الخطوة التالية"), g("Plans, needs and wishes", "الخطط والضرورات والأمنيات"), listOf("tr_l_planlar", "tr_l_istekler")),
            CourseUnit("tr_u_pro_finance", L, B1, g("Banking and finance", "البنوك والتمويل"), g("Core banking vocabulary", "مفردات البنوك الأساسية"), listOf("tr_l_finans"), trackId = "finance"),
            CourseUnit("tr_u_survival", L, A1, g("Essential phrases", "العبارات الأساسية"), g("Everyday phrases and emergencies", "العبارات اليومية والطوارئ"), listOf("tr_l_ev_essentials", "tr_l_ev_emergency")),
            CourseUnit("tr_u_tourism", L, A2, g("Travel and tourism", "السفر والسياحة"), g("Restaurants, hotels, transport and sightseeing", "المطاعم والفنادق والمواصلات والمعالم"), listOf("tr_l_ev_restaurant", "tr_l_ev_hotel", "tr_l_ev_transport", "tr_l_ev_sightseeing")),
        )
        val readings = listOf(
            ReadingPassage(
                "tr_r_ayse", L, A1, "Ayşe'nin günü", g("Ayşe's day", "يوم عائشة"),
                listOf("Ayşe sabah yedide kalkıyor.", "Çay içiyor ve ekmek yiyor.", "Bir bankada çalışıyor.", "Akşamları İngilizce çalışıyor.", "Yorgun ama mutlu."),
                listOf(g("Ayşe gets up at seven in the morning.", "تنهض عائشة في السابعة صباحًا."), g("She drinks tea and eats bread.", "تشرب الشاي وتأكل الخبز."),
                    g("She works in a bank.", "تعمل في بنك."), g("In the evenings she studies English.", "في المساء تدرس الإنجليزية."), g("She is tired but happy.", "هي متعبة لكنها سعيدة.")),
                listOf("tr_kalkmak", "tr_cay", "tr_ekmek", "tr_calismak", "tr_yorgun", "tr_mutlu"),
                listOf(
                    Exercise.MultipleChoice("tr_r_ayse_q1", Skill.READING, Instr.choose, "Ayşe nerede çalışıyor?", listOf("Bir istasyonda", "Bir bankada", "Bir kafede"), 1),
                    Exercise.MultipleChoice("tr_r_ayse_q2", Skill.READING, Instr.choose, "Akşamları ne çalışıyor?", listOf("İngilizce", "Almanca", "Fransızca"), 0),
                ),
            ),
        )
        val scenarios = listOf(
            RolePlayScenario(
                "tr_s_simit", L, A1, g("At the simit stall", "عند بائع السميت"), g("A simit stall by the ferry in Istanbul, early morning.", "كشك سميت قرب العبّارة في إسطنبول صباحًا."),
                g("Buy a simit and a tea and pay.", "اشترِ سميتًا وكوب شاي وادفع."),
                "Buyurun, ne istersiniz?", g("Here you are, what would you like?", "تفضّل، ماذا تريد؟"),
                listOf(
                    ScenarioBeat(listOf("simit", "istiyorum", "alabilir", "lütfen"), g("Say: 'Bir simit, lütfen.'", "قل: Bir simit, lütfen."),
                        "Bir simit, lütfen.", "Tabii. Başka bir şey?", g("Sure. Anything else?", "بالتأكيد. شيء آخر؟")),
                    ScenarioBeat(listOf("çay", "cay", "bu kadar", "yok", "teşekkür"), g("Add a tea or say 'Bu kadar, teşekkürler.'", "أضف كوب شاي أو قل Bu kadar, teşekkürler."),
                        "Bir çay da, lütfen.", "Toplam otuz lira.", g("That's thirty lira in total.", "المجموع ثلاثون ليرة.")),
                    ScenarioBeat(listOf("buyurun", "teşekkür", "iyi günler"), g("Pay and say thank you.", "ادفع وقل شكرًا."),
                        "Buyurun. Teşekkürler, iyi günler!", "Size de iyi günler!", g("Have a nice day too!", "ولك يوم سعيد أيضًا!")),
                ),
                listOf("Bir simit, lütfen.", "Bu kadar, teşekkürler.", "Ne kadar?"),
            ),
            RolePlayScenario(
                "tr_s_doktor", L, A2, g("At the doctor's", "عند الطبيب"), g("A family doctor's practice in Ankara. Language practice only — not medical advice.", "عيادة طبيب أسرة في أنقرة. للتدرّب اللغوي فقط، وليس استشارة طبية."),
                g("Explain what hurts, say since when, and ask about the pharmacy.", "اشرح ما يؤلمك ومنذ متى، واسأل عن الصيدلية."),
                "Merhaba, buyurun oturun. Şikâyetiniz ne?", g("Hello, please sit down. What's the problem?", "مرحبًا، تفضّل بالجلوس. ما شكواك؟"),
                listOf(
                    ScenarioBeat(listOf("ağrıyor", "agriyor", "baş", "ateş", "ates", "hasta"), g("Say what hurts: 'Başım ağrıyor.'", "قل ما يؤلمك: Başım ağrıyor."),
                        "Başım ağrıyor ve ateşim var.", "Ne zamandan beri?", g("Since when?", "منذ متى؟")),
                    ScenarioBeat(listOf("beri", "dün", "dun", "gün", "hafta"), g("Say since when: 'Dünden beri.'", "قل منذ متى: Dünden beri."),
                        "Dün akşamdan beri.", "Tamam. Dinlenin ve bol su için.", g("All right. Rest and drink plenty of water.", "حسنًا. استرح واشرب الكثير من الماء.")),
                    ScenarioBeat(listOf("eczane", "nerede"), g("Ask where the pharmacy is.", "اسأل أين الصيدلية."),
                        "En yakın eczane nerede?", "Hemen yanda, sağda.", g("Right next door, on the right.", "بجانبنا مباشرة، على اليمين.")),
                ),
                listOf("… ağrıyor.", "Ateşim var.", "Ne zamandan beri?", "Eczane nerede?"),
            ),
            RolePlayScenario(
                "tr_s_toplanti", L, B1, g("Planning a project with a client", "التخطيط لمشروع مع عميل"), g("A video call with a client in İzmir.", "مكالمة فيديو مع عميل في إزمير."),
                g("Agree on the price, the delivery date and the next step.", "اتفق على السعر وموعد التسليم والخطوة التالية."),
                "Merhaba. Teklifiniz için teşekkürler. Fiyatı konuşalım mı?", g("Hello. Thank you for your offer. Shall we discuss the price?", "مرحبًا. شكرًا على عرضكم. هل نناقش السعر؟"),
                listOf(
                    ScenarioBeat(listOf("teklif", "fiyat", "lira", "dolar", "euro", "toplam"), g("Explain the price.", "اشرح السعر."),
                        "Tabii. Toplam fiyat beş bin dolar.", "Peki, teslim tarihi ne zaman?", g("All right, and when is the delivery date?", "حسنًا، ومتى موعد التسليم؟")),
                    ScenarioBeat(listOf("hafta", "ay", "tarih", "teslim", "cuma"), g("Give a date: 'altı hafta içinde'.", "حدّد موعدًا: altı hafta içinde."),
                        "Altı hafta içinde teslim edebiliriz.", "Bu bize uyar.", g("That works for us.", "يناسبنا ذلك.")),
                    ScenarioBeat(listOf("e-posta", "eposta", "mail", "gönder", "gonder", "özet", "sözleşme"), g("Say you will send a summary by email.", "قل إنك سترسل ملخصًا بالبريد الإلكتروني."),
                        "Bugün size e-postayla bir özet gönderiyorum.", "Çok teşekkürler. Görüşmek üzere.", g("Thank you very much. Speak soon.", "شكرًا جزيلًا. إلى اللقاء قريبًا.")),
                ),
                listOf("Toplam fiyat…", "… içinde teslim edebiliriz.", "Size … gönderiyorum."),
                trackId = "business",
            ),
            RolePlayScenario(
                "tr_s_hotel", L, A2, g("Checking in at a hotel", "تسجيل الوصول في فندق"), g("A hotel reception in Antalya.", "مكتب استقبال فندق في أنطاليا."),
                g("Check in, show your passport and ask for the Wi-Fi password.", "سجّل وصولك وأظهر جواز سفرك واسأل عن كلمة مرور الواي فاي."),
                "İyi akşamlar, hoş geldiniz. Nasıl yardımcı olabilirim?", g("Good evening, welcome. How can I help?", "مساء الخير، أهلًا بك. كيف أساعدك؟"),
                listOf(
                    ScenarioBeat(listOf("rezervasyon", "adına", "adina", "oda"), g("Say: '… adına bir rezervasyonum var.'", "قل: … adına bir rezervasyonum var."),
                        "Hasan adına bir rezervasyonum var.", "Evet, iki gece için çift kişilik oda. Pasaportunuz, lütfen?", g("Yes, a double room for two nights. Your passport, please?", "نعم، غرفة مزدوجة لليلتين. جواز سفرك من فضلك؟")),
                    ScenarioBeat(listOf("buyurun", "pasaport"), g("Hand it over: 'Buyurun, pasaportum.'", "قدّمه: Buyurun, pasaportum."),
                        "Buyurun, pasaportum.", "Teşekkürler. Kahvaltı yediden ona kadar.", g("Thank you. Breakfast is from seven to ten.", "شكرًا. الفطور من السابعة إلى العاشرة.")),
                    ScenarioBeat(listOf("wi-fi", "wifi", "şifre", "sifre", "internet"), g("Ask for the Wi-Fi password.", "اسأل عن كلمة مرور الواي فاي."),
                        "Wi-Fi şifresi ne?", "Kartınızın üzerinde yazıyor. İyi konaklamalar!", g("It's written on your card. Enjoy your stay!", "إنها مكتوبة على بطاقتك. إقامة سعيدة!")),
                ),
                listOf("… adına bir rezervasyonum var.", "Buyurun, pasaportum.", "Kahvaltı dahil mi?"),
            ),
            RolePlayScenario(
                "tr_s_restoran", L, A2, g("Dinner at a restaurant", "العشاء في مطعم"), g("A fish restaurant by the Bosphorus.", "مطعم سمك على البوسفور."),
                g("Get a table, mention an allergy and order a main course.", "احصل على طاولة واذكر حساسيتك واطلب طبقًا رئيسيًا."),
                "Merhaba! Rezervasyonunuz var mı?", g("Hello! Do you have a reservation?", "مرحبًا! هل لديكم حجز؟"),
                listOf(
                    ScenarioBeat(listOf("masa", "iki", "hayır", "hayir", "yok"), g("Say no and ask for a table for two.", "قل لا واطلب طاولة لشخصين."),
                        "Hayır, iki kişilik bir masanız var mı?", "Tabii. Buyurun menü. Alerjiniz var mı?", g("Of course. Here's the menu. Any allergies?", "بالتأكيد. تفضّل القائمة. هل لديك حساسية؟")),
                    ScenarioBeat(listOf("alerji", "yok", "fıstık", "fistik"), g("Mention an allergy: 'Fıstığa alerjim var.'", "اذكر حساسيتك: Fıstığa alerjim var."),
                        "Fıstığa alerjim var.", "Tamam, şefe söylerim. Ne alırsınız?", g("All right, I'll tell the chef. What will you have?", "حسنًا، سأُخبر الطاهي. ماذا تطلب؟")),
                    ScenarioBeat(listOf("ana yemek", "balık", "balik", "tavuk", "istiyorum", "alayım", "alayim"), g("Order: 'Ana yemek olarak balık alayım.'", "اطلب: Ana yemek olarak balık alayım."),
                        "Ana yemek olarak balık alayım, lütfen.", "Çok güzel, hemen getiriyorum.", g("Very good, I'll bring it right away.", "جيد جدًا، سأحضره فورًا.")),
                ),
                listOf("İki kişilik bir masa, lütfen.", "… alerjim var.", "… alayım.", "Hesap, lütfen."),
            ),
            RolePlayScenario(
                "tr_s_gar", L, A2, g("Buying a train ticket", "شراء تذكرة قطار"), g("The ticket office at Ankara station.", "شبّاك التذاكر في محطة أنقرة."),
                g("Buy a return ticket and find out the platform.", "اشترِ تذكرة ذهاب وعودة واعرف رقم الرصيف."),
                "Merhaba, buyurun.", g("Hello, how can I help?", "مرحبًا، تفضّل."),
                listOf(
                    ScenarioBeat(listOf("gidiş-dönüş", "gidis", "bilet", "istanbul", "konya"), g("Ask for a return ticket to Konya.", "اطلب تذكرة ذهاب وعودة إلى قونية."),
                        "Konya'ya bir gidiş-dönüş bilet, lütfen.", "Tabii. Bir sonraki tren saat onu çeyrek geçe kalkıyor.", g("Sure. The next train leaves at a quarter past ten.", "بالتأكيد. يغادر القطار التالي في العاشرة والربع.")),
                    ScenarioBeat(listOf("peron", "hangi"), g("Ask which platform: 'Hangi perondan kalkıyor?'", "اسأل عن الرصيف: Hangi perondan kalkıyor?"),
                        "Hangi perondan kalkıyor?", "Üçüncü perondan. Yaklaşık on dakika rötarlı.", g("From platform three. It's about ten minutes late.", "من الرصيف الثالث. إنه متأخر نحو عشر دقائق.")),
                    ScenarioBeat(listOf("teşekkür", "tesekkur", "sağ ol", "sag ol"), g("Thank them.", "اشكره."),
                        "Çok teşekkürler.", "Rica ederim. İyi yolculuklar!", g("You're welcome. Have a good trip!", "عفوًا. رحلة سعيدة!")),
                ),
                listOf("… bir gidiş-dönüş bilet, lütfen.", "Hangi perondan kalkıyor?", "Tren zamanında mı?"),
            ),
        )
        fun item(id: String, level: CefrLevel, skill: Skill, prompt: String, options: List<String>, correct: Int) =
            AssessmentItem("tr_a_$id", L, level, skill, Exercise.MultipleChoice("tr_a_$id", skill, Instr.choose, prompt, options, correct))
        val assessment = listOf(
            item("a1_1", A1, Skill.GRAMMAR, "Ben ___. (I am a teacher)", listOf("öğretmenim", "öğretmensin", "öğretmenler"), 0),
            item("a1_2", A1, Skill.VOCABULARY, "'Teşekkür ederim' means…", listOf("please", "thank you", "sorry"), 1),
            item("a1_3", A1, Skill.GRAMMAR, "ev → plural", listOf("evlar", "evler", "evlır"), 1),
            item("a2_1", A2, Skill.GRAMMAR, "Şu anda kitap ___. (I am reading)", listOf("okuyorum", "okudum", "okuyacak"), 0),
            item("a2_2", A2, Skill.GRAMMAR, "Ali şimdi ___. (at school)", listOf("okulda", "okula", "okuldan"), 0),
            item("a2_3", A2, Skill.VOCABULARY, "Restoranda ödemek için ___ istersiniz.", listOf("menüyü", "hesabı", "bileti"), 1),
            item("b1_1", B1, Skill.GRAMMAR, "Dün akşam sinemaya ___.", listOf("gidiyorum", "gittim", "gideceğim"), 1),
            item("b1_2", B1, Skill.GRAMMAR, "Yarın Ankara'ya ___.", listOf("gittim", "gideceğim", "gidiyordum"), 1),
            item("b1_3", B1, Skill.GRAMMAR, "Bu kitabı daha önce hiç ___.", listOf("okumadım", "okumuyorum", "okumayacağım"), 0),
            item("b2_1", B2, Skill.GRAMMAR, "Sağlığın için daha çok spor ___.", listOf("yapmalısın", "yaptın", "yapıyordun"), 0),
            item("b2_2", B2, Skill.GRAMMAR, "Zamanım olsaydı daha çok kitap ___.", listOf("okurdum", "okuyorum", "okudum"), 0),
            item("b2_3", B2, Skill.GRAMMAR, "Yağmur yağmasına ___ dışarı çıktık.", listOf("rağmen", "göre", "kadar"), 0),
        )
        return LanguageCurriculum(L, vocabulary, grammar, units, lessons, readings, scenarios, assessment,
            tracks = listOf(LearningTrack("business", L, g("Turkish for work", "التركية للعمل"), g("Meetings, formal emails, quotes and deadlines.", "الاجتماعات والرسائل الرسمية وعروض الأسعار والمواعيد النهائية."), A2, null),
                LearningTrack("finance", L, g("Turkish for banking and finance", "التركية للبنوك والتمويل"), g("Accounts, loans, interest, collateral and financial statements.", "الحسابات والقروض والفوائد والضمانات والقوائم المالية."), B1,
                    g("This track teaches Turkish vocabulary used in this field. It is not financial, legal or professional advice.", "يعلّم هذا المسار مفردات التركية المستخدمة في هذا المجال، ولا يُعدّ استشارة مالية أو قانونية أو مهنية."))),
            dictations = listOf(
                dictation("d1", L, "Benim adım Kerem.", listOf("Benim adım Kerem")),
                dictation("d2", L, "İstasyon nerede?", listOf("İstasyon nerede")),
                dictation("d3", L, "Genellikle sabahları çalışıyorum.", listOf("Genellikle sabahları çalışıyorum")),
            ),
            writingPrompts = listOf(writing("wp1", L, g("Describe your weekend in Turkish.", "صِف عطلة نهاية الأسبوع بالتركية."), 30, listOf("cumartesi", "pazar", "-di"))),
        )
    }
}
