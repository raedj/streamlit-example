package com.polyglot.academy.core.content

import com.polyglot.academy.core.assessment.AssessmentItem
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CefrLevel.A1
import com.polyglot.academy.core.model.CefrLevel.A2
import com.polyglot.academy.core.model.CefrLevel.B1
import com.polyglot.academy.core.model.CefrLevel.B2
import com.polyglot.academy.core.model.CefrLevel.C1
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

/** Malay starter course (Bahasa Melayu as used in Malaysia). Original content; not yet reviewed by a native speaker. */
internal object MalayCurriculum {
    private val L = LanguageCode.MALAY
    private val biz = setOf("business")
    private val fin = setOf("finance")
    private val pro = VocabularySource.PROFESSIONAL_TRACK

    fun build(): LanguageCurriculum {
        val vb = VocabBuilder(L)
        with(vb) {
            v("helo", "helo", "hello", "مرحبًا", PHRASE, A1, "greetings", "/helo/",
                ex = "Helo, nama saya Aminah.", exEn = "Hello, my name is Aminah.", exAr = "مرحبًا، اسمي أمينة.")
            v("selamat_pagi", "selamat pagi", "good morning", "صباح الخير", PHRASE, A1, "greetings", "/səlamat paɡi/")
            v("selamat_malam", "selamat malam", "good evening, good night", "مساء الخير، تصبح على خير", PHRASE, A1, "greetings", "/səlamat malam/")
            v("terima_kasih", "terima kasih", "thank you", "شكرًا", PHRASE, A1, "greetings", "/tərima kasih/",
                ex = "Terima kasih banyak!", exEn = "Thank you very much!", exAr = "شكرًا جزيلًا!")
            v("tolong_please", "tolong …", "please (asking someone to do something)", "من فضلك (عند الطلب)", PHRASE, A1, "greetings", "/toloŋ/")
            v("maaf", "maaf", "sorry", "آسف", PHRASE, A1, "greetings", "/maʔaf/")
            v("jumpa_lagi", "jumpa lagi", "see you, goodbye", "إلى اللقاء", PHRASE, A1, "greetings", "/dʒumpa laɡi/")
            v("nama_saya", "nama saya …", "my name is…", "اسمي…", PHRASE, A1, "greetings", "/nama saja/")
            v("berkenalan", "gembira berkenalan", "nice to meet you", "تشرّفت بمعرفتك", PHRASE, A1, "greetings", "/ɡəmbira bərkənalan/")
            v("keluarga", "keluarga", "family", "العائلة", NOUN, A1, "people", "/kəluarɡa/")
            v("ibu", "ibu", "mother", "الأم", NOUN, A1, "people", "/ibu/",
                ex = "Ibu saya seorang guru.", exEn = "My mother is a teacher.", exAr = "أمي معلّمة.")
            v("bapa", "bapa", "father", "الأب", NOUN, A1, "people", "/bapa/")
            v("abang", "abang", "older brother", "الأخ الأكبر", NOUN, A1, "people", "/abaŋ/")
            v("kakak", "kakak", "older sister", "الأخت الكبرى", NOUN, A1, "people", "/kakaʔ/")
            v("adik", "adik", "younger brother or sister", "الأخ أو الأخت الأصغر", NOUN, A1, "people", "/adeʔ/")
            v("kawan", "kawan", "friend", "الصديق", NOUN, A1, "people", "/kawan/",
                ex = "Dia kawan baik saya.", exEn = "He/She is my good friend.", exAr = "إنه صديقي العزيز.")
            v("air", "air", "water", "الماء", NOUN, A1, "food", "/aer/")
            v("teh", "teh", "tea", "الشاي", NOUN, A1, "food", "/teh/")
            v("kopi", "kopi", "coffee", "القهوة", NOUN, A1, "food", "/kopi/")
            v("roti", "roti", "bread", "الخبز", NOUN, A1, "food", "/roti/")
            v("bil", "bil", "the bill (restaurant)", "الحساب (في المطعم)", NOUN, A2, "food", "/bil/",
                ex = "Minta bil, ya.", exEn = "The bill, please.", exAr = "الحساب من فضلك.")
            v("boleh_dapat", "boleh saya dapat …?", "can I have…?", "هل يمكنني أن أحصل على…؟", PHRASE, A1, "food",
                ex = "Boleh saya dapat secawan teh?", exEn = "Can I have a cup of tea?", exAr = "هل يمكنني أن أحصل على كوب شاي؟")
            v("sedap", "sedap", "delicious", "لذيذ", ADJECTIVE, A1, "food", "/sədap/")
            v("hari_ini", "hari ini", "today", "اليوم", ADVERB, A1, "time", "/hari ini/")
            v("esok", "esok", "tomorrow", "غدًا", ADVERB, A1, "time", "/esoʔ/",
                ex = "Jumpa esok!", exEn = "See you tomorrow!", exAr = "نلتقي غدًا!")
            v("semalam", "semalam", "yesterday", "أمس", ADVERB, A1, "time", "/səmalam/")
            v("minggu", "minggu", "week", "الأسبوع", NOUN, A1, "time", "/miŋɡu/")
            v("stesen", "stesen", "station", "المحطة", NOUN, A2, "travel", "/stesen/",
                ex = "Di mana stesen?", exEn = "Where is the station?", exAr = "أين المحطة؟")
            v("tiket", "tiket", "ticket", "التذكرة", NOUN, A2, "travel", "/tiket/")
            v("kiri", "kiri", "left", "اليسار", ADVERB, A1, "travel", "/kiri/")
            v("kanan", "kanan", "right", "اليمين", ADVERB, A1, "travel", "/kanan/")
            v("terus", "jalan terus", "go straight on", "امشِ إلى الأمام مباشرة", PHRASE, A2, "travel", "/dʒalan tərus/")
            v("bangun", "bangun", "to get up, to wake up", "ينهض، يستيقظ", VERB, A2, "routine", "/baŋun/",
                ex = "Saya bangun pukul tujuh setiap pagi.", exEn = "I get up at seven every morning.", exAr = "أنهض في السابعة كل صباح.")
            v("bekerja", "bekerja", "to work", "يعمل", VERB, A1, "routine", "/bəkərdʒa/")
            v("belajar", "belajar", "to study, to learn", "يدرس، يتعلّم", VERB, A1, "routine", "/bəladʒar/")
            v("biasanya", "biasanya", "usually", "عادةً", ADVERB, A2, "routine", "/biasaɲa/")
            v("penat", "penat", "tired", "متعب", ADJECTIVE, A1, "feelings", "/pənat/")
            v("gembira", "gembira", "happy", "سعيد", ADJECTIVE, A1, "feelings", "/ɡəmbira/")
            v("pendapat", "pada pendapat saya", "in my opinion", "في رأيي", PHRASE, B1, "feelings",
                ex = "Pada pendapat saya, ini idea yang baik.", exEn = "In my opinion, this is a good idea.", exAr = "في رأيي هذه فكرة جيدة.")
            v("walaupun", "walaupun", "although", "على الرغم من أنّ", PHRASE, B2, "feelings", "/walaupun/")
            v("berapa", "berapa harganya?", "how much is it?", "بكم؟", PHRASE, A1, "shopping", "/bərapa harɡaɲa/")
            v("mahal", "mahal", "expensive", "غالٍ", ADJECTIVE, A1, "shopping", "/mahal/")
            v("murah", "murah", "cheap", "رخيص", ADJECTIVE, A1, "shopping", "/murah/")

            // Home and weather
            v("rumah", "rumah", "house, home", "البيت", NOUN, A1, "home",
                ex = "Rumah saya kecil tetapi cantik.", exEn = "My house is small but pretty.", exAr = "بيتي صغير لكنه جميل.")
            v("dapur", "dapur", "kitchen", "المطبخ", NOUN, A1, "home")
            v("bilik_tidur", "bilik tidur", "bedroom", "غرفة النوم", NOUN, A1, "home")
            v("suka", "saya suka …", "I like…", "أحبّ…", PHRASE, A1, "home",
                ex = "Saya sangat suka hari yang cerah.", exEn = "I really like sunny days.", exAr = "أحبّ الأيام المشمسة كثيرًا.")
            v("cuaca", "cuaca", "weather", "الطقس", NOUN, A2, "weather")
            v("panas", "cuaca panas", "it's hot", "الجو حار", PHRASE, A1, "weather")
            v("sejuk", "cuaca sejuk", "it's cool, it's cold", "الجو بارد", PHRASE, A1, "weather",
                ex = "Pada waktu malam, cuaca agak sejuk.", exEn = "At night it's rather cool.", exAr = "في الليل يكون الجو باردًا نوعًا ما.")
            v("hujan", "sedang hujan", "it's raining", "إنها تمطر", PHRASE, A1, "weather")
            // Health
            v("doktor", "doktor", "doctor", "الطبيب", NOUN, A1, "health")
            v("sakit", "… saya sakit", "my … hurts", "يؤلمني…", PHRASE, A2, "health",
                ex = "Kepala saya sakit.", exEn = "I have a headache.", exAr = "رأسي يؤلمني.")
            v("kepala", "kepala", "head", "الرأس", NOUN, A1, "health")
            v("demam", "demam", "fever; to have a fever", "الحمّى", NOUN, A2, "health")
            v("farmasi", "farmasi", "pharmacy", "الصيدلية", NOUN, A1, "health")
            v("temu_janji", "temu janji", "appointment", "الموعد", NOUN, A2, "health",
                ex = "Saya mahu buat temu janji dengan doktor.", exEn = "I'd like to make an appointment with the doctor.", exAr = "أودّ حجز موعد مع الطبيب.")
            v("tidak_sihat", "tidak sihat", "unwell, ill", "مريض، متوعّك", ADJECTIVE, A1, "health")
            // Work (professional track)
            v("mesyuarat", "mesyuarat", "meeting", "الاجتماع", NOUN, A2, "business",
                ex = "Mesyuarat bermula pukul sepuluh.", exEn = "The meeting starts at ten.", exAr = "يبدأ الاجتماع في العاشرة.", tags = biz, source = pro)
            v("syarikat", "syarikat", "company", "الشركة", NOUN, A2, "business", tags = biz, source = pro)
            v("pelanggan", "pelanggan", "client, customer", "العميل", NOUN, A2, "business", tags = biz, source = pro)
            v("sebut_harga", "sebut harga", "quote, quotation", "عرض السعر", NOUN, B1, "business", tags = biz, source = pro)
            v("tarikh_akhir", "tarikh akhir", "deadline", "الموعد النهائي", NOUN, B1, "business",
                ex = "Tarikh akhir sangat dekat.", exEn = "The deadline is very close.", exAr = "الموعد النهائي قريب جدًا.", tags = biz, source = pro)
            v("cadangan", "cadangan", "proposal, suggestion", "المقترح", NOUN, B1, "business", tags = biz, source = pro)
            v("dilampirkan", "… dilampirkan", "… is attached (formal email)", "… مرفق", PHRASE, B1, "business", tags = biz, source = pro)
            v("tuan_puan", "Tuan/Puan,", "Dear Sir/Madam, (formal letter opening)", "حضرة السيد/السيدة، (افتتاحية رسمية)", PHRASE, B1, "business", tags = biz, source = pro)

            // Next steps: plans and wishes
            v("rancangan", "rancangan", "plan", "الخطة", NOUN, B1, "plans")
            v("depan", "minggu depan / tahun depan", "next week / next year", "الأسبوع القادم / العام القادم", PHRASE, A2, "plans",
                ex = "Tahun depan saya akan berpindah ke Pulau Pinang.", exEn = "Next year I'm going to move to Penang.", exAr = "في العام القادم سأنتقل إلى بينانغ.")
            v("melancong", "melancong", "to travel (for pleasure)", "يسافر للسياحة", VERB, A2, "plans")
            v("menabung", "menabung", "to save money", "يدّخر المال", VERB, B1, "plans")
            v("mungkin", "mungkin", "maybe, perhaps", "ربما", ADVERB, A2, "plans")
            v("cuti", "cuti", "holiday, leave", "العطلة", NOUN, A2, "plans")
            v("perlu", "perlu / mesti", "need to / must", "يحتاج إلى / يجب", PHRASE, B2, "wishes",
                ex = "Awak perlu berehat.", exEn = "You need to rest.", exAr = "تحتاج إلى أن ترتاح.")
            v("mahu_awak", "saya mahu awak …", "I want you to…", "أريدك أن…", PHRASE, B2, "wishes")
            v("harap", "saya harap", "I hope", "آمل", PHRASE, B1, "wishes")
            v("nasib", "nasib", "luck, fate", "الحظ", NOUN, B1, "wishes")
            v("berjaya", "berjaya", "to succeed; successful", "ينجح؛ ناجح", VERB, B1, "wishes")
            v("peperiksaan", "peperiksaan", "exam", "الامتحان", NOUN, A2, "wishes")

            // Banking and finance (professional track)
            v("fin_akaun", "akaun bank", "bank account", "الحساب المصرفي", NOUN, A2, "finance", tags = fin, source = pro)
            v("fin_pinjaman", "pinjaman", "loan", "القرض", NOUN, B1, "finance",
                ex = "Bank meluluskan pinjaman itu.", exEn = "The bank approved the loan.", exAr = "وافق البنك على القرض.", tags = fin, source = pro)
            v("fin_faedah", "kadar faedah", "interest rate", "سعر الفائدة", NOUN, B1, "finance", tags = fin, source = pro)
            v("fin_ansuran", "ansuran", "instalment", "القسط", NOUN, B1, "finance", tags = fin, source = pro)
            v("fin_cagaran", "cagaran", "collateral", "الضمان", NOUN, B2, "finance", tags = fin, source = pro)
            v("fin_ingkar", "ingkar bayar", "default (failure to pay)", "التعثّر عن السداد", NOUN, B2, "finance", tags = fin, source = pro)
            v("fin_kunci_kira", "kunci kira-kira", "balance sheet", "الميزانية العمومية", NOUN, B2, "finance", tags = fin, source = pro)
            v("fin_kecairan", "kecairan", "liquidity", "السيولة", NOUN, B2, "finance", tags = fin, source = pro)
            // Credit and risk (professional track)
            v("cr_risiko_kredit", "risiko kredit", "credit risk", "مخاطر الائتمان", NOUN, B2, "credit_risk", ex = "Bank menilai risiko kredit setiap pelanggan.", exEn = "The bank assesses each customer's credit risk.", exAr = "يقيّم البنك مخاطر الائتمان لكل عميل.", tags = setOf("credit_risk"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("cr_penstrukturan", "penstrukturan semula", "restructuring", "إعادة الهيكلة", NOUN, B2, "credit_risk", tags = setOf("credit_risk"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("cr_hapus_kira", "hapus kira", "write-off", "شطب الدين", NOUN, C1, "credit_risk", tags = setOf("credit_risk"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("cr_kelayakan", "kelayakan kredit", "creditworthiness", "الجدارة الائتمانية", NOUN, C1, "credit_risk", tags = setOf("credit_risk"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("cr_peruntukan", "peruntukan kerugian", "loan-loss provision", "مخصّص الخسائر", NOUN, C1, "credit_risk", tags = setOf("credit_risk"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("cr_penjamin", "penjamin", "guarantor", "الكفيل", NOUN, B2, "credit_risk", tags = setOf("credit_risk"), source = VocabularySource.PROFESSIONAL_TRACK)
            // Contracts (professional track)
            v("lg_kontrak", "kontrak", "contract", "العقد", NOUN, B1, "legal", ex = "Kedua-dua pihak menandatangani kontrak itu semalam.", exEn = "Both parties signed the contract yesterday.", exAr = "وقّع الطرفان العقد أمس.", tags = setOf("legal"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("lg_fasal", "fasal", "clause", "البند", NOUN, B2, "legal", tags = setOf("legal"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("lg_pihak", "pihak", "party (to an agreement)", "الطرف", NOUN, B2, "legal", tags = setOf("legal"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("lg_liabiliti", "liabiliti", "legal liability", "المسؤولية القانونية", NOUN, C1, "legal", tags = setOf("legal"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("lg_pelanggaran", "pelanggaran kontrak", "breach of contract", "الإخلال بالعقد", NOUN, C1, "legal", tags = setOf("legal"), source = VocabularySource.PROFESSIONAL_TRACK)
            // Daily life and travel
            v("ev_maafkan", "maafkan saya", "excuse me", "عفوًا", PHRASE, A1, "essentials", ex = "Maafkan saya, tempat ini kosong?", exEn = "Excuse me, is this seat free?", exAr = "عفوًا، هل هذا المقعد شاغر؟")
            v("ev_tak_faham", "saya tak faham", "I don't understand", "لا أفهم", PHRASE, A1, "essentials")
            v("ev_perlahan", "Boleh cakap perlahan sikit?", "Could you speak a bit more slowly?", "هل يمكنك التحدث ببطء أكثر قليلًا؟", PHRASE, A1, "essentials")
            v("ev_ulang", "Boleh ulang sekali lagi?", "Could you repeat that?", "هل يمكنك أن تعيد؟", PHRASE, A1, "essentials")
            v("ev_tandas", "Di mana tandas?", "Where is the toilet?", "أين دورة المياه؟", PHRASE, A1, "essentials")
            v("ev_bantu", "Boleh tolong saya?", "Can you help me?", "هل يمكنك مساعدتي؟", PHRASE, A1, "essentials")
            v("ev_tak_tahu", "saya tak tahu", "I don't know", "لا أعرف", PHRASE, A1, "essentials")
            v("ev_sama_sama", "sama-sama", "you're welcome", "عفوًا (ردًّا على الشكر)", PHRASE, A1, "essentials")
            v("ev_tolong", "Tolong!", "Help!", "النجدة!", PHRASE, A1, "emergency")
            v("ev_polis", "polis", "the police", "الشرطة", NOUN, A1, "emergency")
            v("ev_ambulans", "Panggil ambulans!", "Call an ambulance!", "اتصلوا بالإسعاف!", PHRASE, A2, "emergency", ex = "Tolong panggil ambulans sekarang!", exEn = "Please call an ambulance now!", exAr = "من فضلكم اتصلوا بالإسعاف الآن!")
            v("ev_pasport", "Pasport saya hilang.", "I've lost my passport.", "أضعتُ جواز سفري.", PHRASE, A2, "emergency")
            v("ev_dicuri", "Beg saya dicuri.", "My bag has been stolen.", "سُرقت حقيبتي.", PHRASE, A2, "emergency")
            v("ev_kedutaan", "kedutaan", "the embassy", "السفارة", NOUN, A2, "emergency")
            v("ev_hospital", "hospital", "hospital", "المستشفى", NOUN, A1, "emergency")
            v("ev_kecemasan", "nombor kecemasan", "emergency number", "رقم الطوارئ", NOUN, A2, "emergency")
            v("ev_meja", "meja untuk dua orang", "a table for two", "طاولة لشخصين", PHRASE, A1, "restaurant", ex = "Selamat malam, meja untuk dua orang, ya.", exEn = "Good evening, a table for two, please.", exAr = "مساء الخير، طاولة لشخصين من فضلك.")
            v("ev_cadangkan", "Apa yang awak cadangkan?", "What do you recommend?", "بماذا تنصحني؟", PHRASE, A2, "restaurant")
            v("ev_alah", "saya alah kepada …", "I'm allergic to…", "عندي حساسية من…", PHRASE, A2, "restaurant")
            v("ev_vegetarian", "vegetarian", "vegetarian", "نباتي", ADJECTIVE, A2, "restaurant")
            v("ev_pembuka_selera", "pembuka selera", "starter", "المقبّلات", NOUN, A2, "restaurant")
            v("ev_hidangan_utama", "hidangan utama", "main course", "الطبق الرئيسي", NOUN, A2, "restaurant")
            v("ev_pencuci_mulut", "pencuci mulut", "dessert", "الحلوى", NOUN, A1, "restaurant")
            v("ev_tip", "tip", "tip", "البقشيش", NOUN, A2, "restaurant")
            v("ev_tempahan", "tempahan", "reservation, booking", "الحجز", NOUN, A2, "hotel", ex = "Saya ada tempahan atas nama Hasan.", exEn = "I have a reservation under the name Hasan.", exAr = "لديّ حجز باسم حسن.")
            v("ev_bilik_bujang", "bilik bujang", "single room", "غرفة لشخص واحد", NOUN, A2, "hotel")
            v("ev_bilik_kelamin", "bilik kelamin", "double room", "غرفة مزدوجة", NOUN, A2, "hotel")
            v("ev_kunci", "kunci", "key", "المفتاح", NOUN, A1, "hotel")
            v("ev_sarapan", "Sarapan termasuk?", "Is breakfast included?", "هل الفطور مشمول؟", PHRASE, A2, "hotel")
            v("ev_wifi", "Apa kata laluan Wi-Fi?", "What's the Wi-Fi password?", "ما كلمة مرور الواي فاي؟", PHRASE, A2, "hotel")
            v("ev_kaunter", "kaunter penyambut tetamu", "reception desk", "مكتب الاستقبال", NOUN, A2, "hotel")
            v("ev_daftar_keluar", "daftar keluar", "to check out", "تسجيل المغادرة", VERB, A2, "hotel")
            v("ev_platform", "platform", "platform", "الرصيف", NOUN, A2, "transport", ex = "Kereta api bertolak dari platform tiga.", exEn = "The train leaves from platform three.", exAr = "يغادر القطار من الرصيف رقم ثلاثة.")
            v("ev_pergi_balik", "tiket pergi balik", "a return ticket", "تذكرة ذهاب وعودة", NOUN, A2, "transport")
            v("ev_sehala", "tiket sehala", "a single (one-way) ticket", "تذكرة ذهاب فقط", NOUN, A2, "transport")
            v("ev_bertolak", "waktu bertolak", "departure time", "موعد المغادرة", NOUN, A2, "transport")
            v("ev_kelewatan", "kelewatan", "delay", "التأخير", NOUN, A2, "transport")
            v("ev_pas_masuk", "pas masuk", "boarding pass", "بطاقة الصعود إلى الطائرة", NOUN, A2, "transport")
            v("ev_bagasi", "bagasi", "luggage", "الأمتعة", NOUN, A2, "transport")
            v("ev_perhentian_bas", "perhentian bas", "bus stop", "موقف الحافلات", NOUN, A1, "transport")
            v("ev_muzium", "muzium", "museum", "المتحف", NOUN, A1, "sightseeing", ex = "Muzium buka pukul berapa?", exEn = "What time does the museum open?", exAr = "متى يفتح المتحف؟")
            v("ev_waktu_buka", "waktu buka", "opening hours", "مواعيد العمل", NOUN, A2, "sightseeing")
            v("ev_tiket_masuk", "tiket masuk", "entrance ticket", "تذكرة الدخول", NOUN, A2, "sightseeing")
            v("ev_lawatan_berpandu", "lawatan berpandu", "guided tour", "جولة مع مرشد", NOUN, A2, "sightseeing")
            v("ev_peta", "peta", "map", "الخريطة", NOUN, A1, "sightseeing")
            v("ev_gambar", "Boleh tolong ambil gambar kami?", "Could you take a photo of us?", "هل يمكنك أن تلتقط لنا صورة؟", PHRASE, A2, "sightseeing")
            v("ev_bandar_lama", "bandar lama", "old town", "المدينة القديمة", NOUN, A2, "sightseeing")
            v("ev_cenderamata", "cenderamata", "souvenir", "تذكار", NOUN, A2, "sightseeing")
        }
        val vocabulary = vb.items
        val f = LessonFactory(L, vocabulary)

        val grammar = listOf(
            grammar("no_to_be", L, A1, g("No verb 'to be'", "لا يوجد فعل للكينونة"),
                g("Malay has no verb 'to be' before adjectives or places: Saya penat (I am tired), Dia di rumah (she is at home). Before a noun you may add ialah or adalah in formal writing, but everyday speech simply says Saya pelajar (I am a student).",
                    "لا يوجد في الملايوية فعل للكينونة قبل الصفات أو الأماكن: ‏Saya penat (أنا متعب) وDia di rumah (هي في البيت). وقبل الاسم يمكن إضافة ialah أو adalah في الكتابة الرسمية، أما في الكلام اليومي فيُقال ببساطة Saya pelajar (أنا طالب)."),
                listOf(ex("Saya pelajar.", "I am a student.", "أنا طالب."), ex("Kami gembira.", "We are happy.", "نحن سعداء."),
                    ex("Saya adalah penat.", "No 'adalah' before an adjective: Saya penat", "لا تُستخدم adalah قبل الصفة: Saya penat", wrong = true)),
                listOf(g("✗ Saya ada penat → ✓ Saya penat ('ada' means 'to have / there is').", "✗ Saya ada penat ← ✓ Saya penat (ada تعني «يملك / يوجد»)."))),
            grammar("plural", L, A1, g("Plurals: reduplication and numbers", "الجمع: التكرار والأعداد"),
                g("Nouns do not change for number. To stress 'many different', repeat the noun: buku-buku (books). After a number or a quantity word, do not repeat it: dua buku (two books), banyak buku (many books).",
                    "لا تتغيّر الأسماء بحسب العدد. وللتأكيد على الكثرة يُكرَّر الاسم: ‏buku-buku (كتب). وبعد العدد أو كلمات الكمية لا يُكرَّر: ‏dua buku (كتابان) وbanyak buku (كتب كثيرة)."),
                listOf(ex("Ada tiga kucing di luar.", "There are three cats outside.", "توجد ثلاث قطط في الخارج."), ex("Buku-buku itu baru.", "Those books are new.", "تلك الكتب جديدة."),
                    ex("banyak buku-buku", "No repetition after banyak: banyak buku", "لا تكرار بعد banyak: banyak buku", wrong = true)),
                listOf(g("✗ dua orang-orang → ✓ dua orang.", "✗ dua orang-orang ← ✓ dua orang."))),
            grammar("di_ke_dari", L, A1, g("Where? di, ke, dari", "أين؟ di وke وdari"),
                g("di = at, in (location): di rumah. ke = to (direction): pergi ke pasar. dari = from: datang dari Johor. Note that di as a separate word is a preposition; joined to a verb (dibeli) it marks the passive.",
                    "‏di = في (المكان): di rumah. وke = إلى (الاتجاه): pergi ke pasar. وdari = من: datang dari Johor. لاحظ أن di المنفصلة حرف جر، أما المتصلة بالفعل (dibeli) فعلامة للمبني للمجهول."),
                listOf(ex("Ibu ada di rumah.", "Mum is at home.", "أمي في البيت."), ex("Saya pergi ke pasar.", "I'm going to the market.", "أذهب إلى السوق."),
                    ex("Saya pergi di pasar.", "Movement takes ke: pergi ke pasar", "الحركة تأخذ ke: pergi ke pasar", wrong = true)),
                listOf(g("✗ dirumah → ✓ di rumah (the preposition is written apart).", "✗ dirumah ← ✓ di rumah (يُكتب حرف الجر منفصلًا)."))),
            grammar("aspect", L, A1, g("Time words: sedang, sudah, belum, akan", "كلمات الزمن: sedang وsudah وbelum وakan"),
                g("Verbs never change for tense. Small words before the verb give the time: sedang (in progress), sudah / telah (already, done), belum (not yet), akan (will).",
                    "لا يتغيّر الفعل بحسب الزمن، بل تُحدّد الزمنَ كلماتٌ صغيرة قبله: ‏sedang (يحدث الآن) وsudah / telah (حدث وانتهى) وbelum (لم يحدث بعد) وakan (سوف)."),
                listOf(ex("Saya sedang makan.", "I'm eating (right now).", "أنا آكل الآن."), ex("Dia sudah pergi.", "He has already left.", "لقد غادر بالفعل.")),
                listOf(g("Not yet is belum, not tidak sudah: Saya belum makan.", "«لم … بعد» تُقال belum لا tidak sudah: Saya belum makan."))),
            grammar("politeness", L, A2, g("Polite requests", "الطلب المهذب"),
                g("Ask with 'Boleh saya dapat …?' (can I have …?) or 'Minta …' (may I have …). Use tolong + verb to ask someone to do something: Tolong bawa bil. Address strangers as encik (Mr), puan (Mrs/Ms) or cik.",
                    "اطلب بصيغة 'Boleh saya dapat …?' (هل يمكنني أن أحصل على…؟) أو 'Minta …'. واستخدم tolong + الفعل لتطلب من شخص أن يفعل شيئًا: Tolong bawa bil. وخاطب الغرباء بـ encik (السيد) أو puan (السيدة) أو cik."),
                listOf(ex("Boleh saya dapat secawan kopi?", "Can I have a cup of coffee?", "هل يمكنني أن أحصل على كوب قهوة؟"), ex("Tolong bawa bil, encik.", "Please bring the bill, sir.", "أحضر الحساب من فضلك يا سيدي.")),
                listOf(g("'Bagi kopi!' (give coffee!) sounds abrupt to a waiter.", "عبارة «Bagi kopi!» (أعطني قهوة!) تبدو جافة مع النادل."))),
            grammar("past", L, B1, g("Talking about the past", "الحديث عن الماضي"),
                g("Use a time word (semalam, tadi, minggu lepas) or sudah / telah; the verb stays the same: Semalam saya pergi ke pawagam. To say 'have never / not yet', use belum pernah or belum: Saya belum pernah ke Sabah.",
                    "استخدم كلمة زمن (semalam وtadi وminggu lepas) أو sudah / telah؛ ويبقى الفعل كما هو: ‏Semalam saya pergi ke pawagam. وللتعبير عن «لم … قط / بعد» استخدم belum pernah أو belum: ‏Saya belum pernah ke Sabah."),
                listOf(ex("Semalam kami makan di restoran.", "Yesterday we ate at a restaurant.", "أمس أكلنا في مطعم."), ex("Saya sudah membaca buku itu.", "I have already read that book.", "لقد قرأت ذلك الكتاب."),
                    ex("Saya tidak sudah makan.", "Say 'Saya belum makan'", "قل Saya belum makan", wrong = true)),
                listOf(g("✗ Semalam saya akan pergi → ✓ Semalam saya pergi.", "✗ Semalam saya akan pergi ← ✓ Semalam saya pergi."))),
            grammar("cuaca", L, A1, g("Talking about the weather", "الحديث عن الطقس"),
                g("Describe the weather with cuaca + adjective: Cuaca panas, Cuaca sejuk, Cuaca baik. For rain: Hari ini hujan, or Sedang hujan (it's raining now).",
                    "صِف الطقس بـ cuaca + صفة: ‏Cuaca panas وCuaca sejuk وCuaca baik. وللمطر: ‏Hari ini hujan، أو Sedang hujan (إنها تمطر الآن)."),
                listOf(ex("Hari ini cuaca sangat panas.", "It's very hot today.", "الجو حار جدًا اليوم."), ex("Sejak pagi sedang hujan.", "It's been raining since the morning.", "إنها تمطر منذ الصباح."),
                    ex("Hari ini ada panas.", "Say 'cuaca panas'", "قل cuaca panas", wrong = true)),
                listOf(g("sejuk covers both 'cool' and 'cold'; in a tropical country it often means pleasantly cool.", "‏sejuk تعني «لطيف البرودة» و«بارد» معًا، وفي البلد الاستوائي تعني غالبًا البرودة اللطيفة."))),
            grammar("register", L, A2, g("Formal and informal 'you'", "المخاطبة الرسمية وغير الرسمية"),
                g("With friends use awak or kamu. With clients, officials and people you don't know, use anda, a title (encik, puan, tuan) or the person's name. Saya (I) is safe everywhere; aku is only for close friends.",
                    "مع الأصدقاء استخدم awak أو kamu. ومع العملاء والموظفين ومن لا تعرفهم استخدم anda أو لقبًا (encik وpuan وtuan) أو اسم الشخص. والضمير saya (أنا) مناسب في كل المواقف، أما aku فللأصدقاء المقرّبين فقط."),
                listOf(ex("Bolehkah tuan mengesahkan tarikh itu?", "Could you (sir) confirm the date?", "هل يمكنكم يا سيدي تأكيد الموعد؟"),
                    ex("Aku nak jumpa bos kau.", "Too informal at work: Saya ingin berjumpa dengan pengurus anda.", "غير رسمي جدًا في العمل: Saya ingin berjumpa dengan pengurus anda.", wrong = true)),
                listOf(g("Using aku or kau with a client sounds rude.", "استخدام aku أو kau مع عميل يبدو فظًّا."))),
            grammar("future", L, B1, g("The future with akan", "المستقبل مع akan"),
                g("Put akan before the verb: Saya akan pergi (I will go), Esok akan hujan (it will rain tomorrow). In speech, nak or mahu often expresses plans: Saya nak melancong ke Jepun.",
                    "ضع akan قبل الفعل: ‏Saya akan pergi (سأذهب) وEsok akan hujan (ستمطر غدًا). وفي الكلام اليومي تعبّر nak أو mahu غالبًا عن الخطط: Saya nak melancong ke Jepun."),
                listOf(ex("Malam ini kami akan makan di rumah.", "Tonight we will eat at home.", "الليلة سنأكل في البيت."),
                    ex("Tahun depan saya akan tinggal di Melaka.", "Next year I will live in Melaka.", "في العام القادم سأعيش في ملقا."),
                    ex("Saya akan pergi semalam.", "akan cannot go with semalam (yesterday)", "لا تجتمع akan مع semalam (أمس)", wrong = true)),
                listOf(g("akan is often dropped when a time word already shows the future: Esok saya kerja.", "كثيرًا ما تُحذف akan إذا دلّت كلمة الزمن على المستقبل: Esok saya kerja."))),
            grammar("perlu_supaya", L, B2, g("Need to, want someone to, so that", "الحاجة والطلب من الآخرين والغاية"),
                g("perlu (need to), mesti (must) and harus (should) go before the verb: Awak perlu berehat. To want someone else to act, put the person after mahu: Saya mahu awak datang (I want you to come). supaya or agar means 'so that': Dia belajar supaya lulus.",
                    "تأتي perlu (يحتاج إلى) وmesti (يجب) وharus (ينبغي) قبل الفعل: ‏Awak perlu berehat. وللطلب من شخص آخر يوضع الشخص بعد mahu: ‏Saya mahu awak datang (أريدك أن تأتي). وsupaya أو agar بمعنى «لكي»: Dia belajar supaya lulus."),
                listOf(ex("Awak perlu minum lebih banyak air.", "You need to drink more water.", "تحتاج إلى شرب المزيد من الماء."),
                    ex("Saya mahu awak berjaya.", "I want you to succeed.", "أريدك أن تنجح."),
                    ex("Saya mahu bahawa awak datang.", "No 'bahawa' after mahu: Saya mahu awak datang", "لا تُستخدم bahawa بعد mahu: Saya mahu awak datang", wrong = true)),
                listOf(g("walaupun (although) and supaya (so that) are easy to mix up: Walaupun hujan, kami keluar.", "يسهل الخلط بين walaupun (رغم أنّ) وsupaya (لكي): Walaupun hujan, kami keluar."))),
            grammar("passive_di", L, B2, g("The passive with di-", "المبني للمجهول بـ di-"),
                g("Active verbs often start with meN-: Bank meluluskan pinjaman (the bank approves the loan). Replace meN- with di- for the passive and put the doer after oleh: Pinjaman diluluskan oleh bank. The passive is very common in formal Malay.",
                    "تبدأ الأفعال المبنية للمعلوم غالبًا بـ meN-: ‏Bank meluluskan pinjaman (يوافق البنك على القرض). وللمبني للمجهول تُستبدل meN- بـ di- ويأتي الفاعل بعد oleh: ‏Pinjaman diluluskan oleh bank. والمبني للمجهول شائع جدًا في الملايوية الرسمية."),
                listOf(ex("Laporan itu ditulis oleh pengurus.", "The report was written by the manager.", "كُتب التقرير من قِبل المدير."),
                    ex("Pinjaman itu meluluskan oleh bank.", "Use the passive form: diluluskan", "استخدم صيغة المجهول: diluluskan", wrong = true)),
                listOf(g("di- joined to the verb is the passive; di as a separate word means 'at': dibeli vs di kedai.", "‏di- المتصلة بالفعل للمجهول، وdi المنفصلة بمعنى «في»: dibeli مقابل di kedai."))),
        )

        val lessons = listOf(
            f.lesson("salam", A1, "basics", g("Greetings", "التحيات"), g("Say hello and introduce yourself.", "ألقِ التحية وعرّف بنفسك."),
                Skill.SPEAKING, listOf("helo", "selamat_pagi", "selamat_malam", "terima_kasih", "tolong_please", "maaf", "jumpa_lagi", "nama_saya", "berkenalan"), "no_to_be",
                extra = listOf(
                    fill("s1", L, "Nama ___ Aminah.", listOf("saya")),
                    fill("s2", L, "___ berkenalan.", listOf("Gembira")),
                ), minutes = 6),
            f.lesson("keluarga", A1, "basics", g("Family", "العائلة"), g("Talk about your family and count people and things.", "تحدّث عن عائلتك وعُدّ الأشخاص والأشياء."),
                Skill.VOCABULARY, listOf("keluarga", "ibu", "bapa", "abang", "kakak", "adik", "kawan"), "plural",
                extra = listOf(choice("k1", L, "Saya ada dua ___. (siblings)", listOf("adik", "adik-adik", "adiknya-adik"), 0,
                    explanation = g("After a number, the noun is not repeated.", "بعد العدد لا يُكرَّر الاسم."))),
                prerequisites = listOf("salam")),
            f.lesson("kafe", A1, "everyday", g("At the café", "في المقهى"), g("Order politely with 'Boleh saya dapat …?'.", "اطلب بأدب باستخدام 'Boleh saya dapat …?'."),
                Skill.SPEAKING, listOf("air", "teh", "kopi", "roti", "bil", "boleh_dapat", "sedap"), "politeness",
                extra = listOf(
                    order("c1", L, listOf("Boleh", "saya", "dapat", "secawan", "teh?"), g("Can I have a cup of tea?", "هل يمكنني أن أحصل على كوب شاي؟")),
                    dictation("c2", L, "Minta bil, ya.", listOf("Minta bil ya")),
                ), prerequisites = listOf("keluarga")),
            f.lesson("rutin", A2, "everyday", g("Daily routine", "الروتين اليومي"), g("Describe your day with sedang, sudah and biasanya.", "صِف يومك باستخدام sedang وsudah وbiasanya."),
                Skill.WRITING, listOf("bangun", "bekerja", "belajar", "biasanya", "hari_ini", "esok", "minggu"), "aspect",
                extra = listOf(
                    fill("r1", L, "Saya ___ pukul tujuh setiap pagi. (to get up)", listOf("bangun")),
                    fill("r2", L, "Kami ___ belajar bahasa Melayu sekarang. (in progress)", listOf("sedang")),
                ), prerequisites = listOf("kafe")),
            f.lesson("perjalanan", A2, "travel", g("Getting around", "التنقّل"), g("Ask for directions with di, ke and dari.", "اسأل عن الاتجاهات باستخدام di وke وdari."),
                Skill.LISTENING, listOf("stesen", "tiket", "kiri", "kanan", "terus"), "di_ke_dari",
                extra = listOf(
                    fill("p1", L, "Saya pergi ___ pasar. (to)", listOf("ke")),
                    dictation("p2", L, "Jalan terus, kemudian belok kiri.", listOf("Jalan terus kemudian belok kiri")),
                ), prerequisites = listOf("rutin")),
            f.lesson("membeli", A2, "travel", g("Shopping", "التسوّق"), g("Ask prices and give opinions.", "اسأل عن الأسعار وعبّر عن رأيك."),
                Skill.SPEAKING, listOf("berapa", "mahal", "murah", "gembira", "penat"), "plural",
                extra = listOf(fill("m1", L, "Baju ini sangat ___. (expensive)", listOf("mahal"))),
                prerequisites = listOf("perjalanan")),
            f.lesson("masa_lalu", B1, "travel", g("Talking about the past", "الحديث عن الماضي"), g("Use time words, sudah and belum.", "استخدم كلمات الزمن وsudah وbelum."),
                Skill.SPEAKING, listOf("semalam", "pendapat", "walaupun", "gembira"), "past",
                extra = listOf(
                    fill("ml1", L, "Saya ___ makan, terima kasih. (already)", listOf("sudah", "telah")),
                    fill("ml2", L, "Saya ___ pernah ke Sabah. (not yet)", listOf("belum")),
                ), prerequisites = listOf("membeli")),
            f.lesson("rumah_cuaca", A2, "home", g("Home and weather", "البيت والطقس"), g("Describe your home and talk about the weather.", "صِف بيتك وتحدّث عن الطقس."),
                Skill.VOCABULARY, listOf("rumah", "dapur", "bilik_tidur", "suka", "cuaca", "panas", "sejuk", "hujan"), "cuaca",
                extra = listOf(
                    fill("rc1", L, "Hari ini cuaca sangat ___. (hot)", listOf("panas")),
                    fill("rc2", L, "Bawa payung, sedang ___!", listOf("hujan")),
                    order("rc3", L, listOf("Saya", "sangat", "suka", "rumah", "saya"), g("I really like my house.", "أحبّ بيتي كثيرًا.")),
                ), prerequisites = listOf("masa_lalu")),
            f.lesson("kesihatan", A2, "home", g("At the doctor's", "عند الطبيب"), g("Say what hurts and make an appointment.", "قل ما يؤلمك واحجز موعدًا."),
                Skill.SPEAKING, listOf("doktor", "sakit", "kepala", "demam", "farmasi", "temu_janji", "tidak_sihat"), "register",
                extra = listOf(
                    fill("ks1", L, "Kepala saya ___.", listOf("sakit")),
                    fill("ks2", L, "Saya mahu buat ___ dengan doktor.", listOf("temu janji")),
                    dictation("ks3", L, "Saya demam sejak semalam.", listOf("Saya demam sejak semalam")),
                ), prerequisites = listOf("rumah_cuaca")),
            f.lesson("kerja", B1, "pro_business", g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), g("Quotes, deadlines and polite, formal requests.", "عروض الأسعار والمواعيد النهائية والطلبات الرسمية المهذبة."),
                Skill.WRITING, listOf("mesyuarat", "syarikat", "pelanggan", "sebut_harga", "tarikh_akhir", "cadangan", "dilampirkan", "tuan_puan"), "register",
                extra = listOf(
                    fill("kj1", L, "Sebut harga ___ bersama e-mel ini.", listOf("dilampirkan")),
                    choice("kj2", L, "Which is the right way to start a formal email?", listOf("Hai bro!", "Tuan/Puan,", "Apa khabar, kawan?"), 1, Skill.FUNCTIONAL),
                    fill("kj3", L, "Bolehkah ___ mengesahkan tarikh mesyuarat? (formal 'you')", listOf("tuan", "puan", "encik", "anda")),
                    writing("kj4", L, g("Write a short email to a client proposing a meeting next week.", "اكتب رسالة قصيرة إلى عميل تقترح فيها اجتماعًا الأسبوع القادم."), 40,
                        listOf("Tuan/Puan", "mesyuarat", "cadangan", "Sekian, terima kasih")),
                )),
            f.lesson("rancangan", B1, "next", g("Plans for the future", "خطط المستقبل"), g("Talk about plans with akan.", "تحدّث عن الخطط باستخدام akan."),
                Skill.SPEAKING, listOf("rancangan", "depan", "melancong", "menabung", "mungkin", "cuti"), "future",
                extra = listOf(
                    fill("rn1", L, "Tahun depan saya ___ melancong ke Jepun. (will)", listOf("akan")),
                    fill("rn2", L, "Esok ___ hujan. (will)", listOf("akan")),
                    order("rn3", L, listOf("Kami", "akan", "menabung", "untuk", "cuti"), g("We'll save money for the holiday.", "سنوفّر المال للعطلة.")),
                    writing("rn4", L, g("Write about your plans for next year (30+ words).", "اكتب عن خططك للعام القادم (30 كلمة على الأقل)."), 30, listOf("tahun depan", "akan", "mungkin")),
                ), prerequisites = listOf("kesihatan")),
            f.lesson("harapan", B2, "next", g("Needs and wishes", "الضرورات والأمنيات"), g("Use perlu, 'saya mahu awak …' and supaya.", "استخدم perlu و'saya mahu awak …' وsupaya."),
                Skill.GRAMMAR, listOf("perlu", "mahu_awak", "harap", "nasib", "berjaya", "peperiksaan", "walaupun"), "perlu_supaya",
                extra = listOf(
                    fill("h1", L, "Awak ___ berehat lebih banyak. (need to)", listOf("perlu", "mesti", "harus")),
                    fill("h2", L, "Dia belajar bersungguh-sungguh ___ lulus peperiksaan. (so that)", listOf("supaya", "agar")),
                    choice("h3", L, "Saya harap awak ___ dalam peperiksaan.", listOf("berjaya", "kejayaan", "berjayanya"), 0,
                        explanation = g("After harap, use the verb itself.", "بعد harap يأتي الفعل نفسه.")),
                    translate("h4", L, g("I want you to succeed.", "أريدك أن تنجح."), listOf("Saya mahu awak berjaya", "Saya mahu kamu berjaya", "Saya mahu anda berjaya")),
                ), prerequisites = listOf("rancangan")),
            f.lesson("kewangan", B1, "pro_finance", g("Banking basics", "أساسيات البنوك"), g("Accounts, loans, interest and the formal passive.", "الحسابات والقروض والفوائد والمبني للمجهول الرسمي."),
                Skill.VOCABULARY, listOf("fin_akaun", "fin_pinjaman", "fin_faedah", "fin_ansuran", "fin_cagaran", "fin_ingkar", "fin_kunci_kira", "fin_kecairan"), "passive_di",
                extra = listOf(
                    fill("kw1", L, "Bank memberi ___ kepada syarikat itu.", listOf("pinjaman")),
                    fill("kw2", L, "Kadar ___ ialah lima peratus.", listOf("faedah")),
                    choice("kw3", L, "Pinjaman itu ___ oleh bank.", listOf("diluluskan", "meluluskan", "lulus"), 0,
                        explanation = g("Passive: di- + verb, doer after oleh.", "المبني للمجهول: di- + الفعل، والفاعل بعد oleh.")),
                    choice("kw4", L, "Which document shows assets and liabilities?", listOf("kunci kira-kira", "sebut harga", "resit"), 0, Skill.VOCABULARY),
                )),
            f.lesson("credit", B2, "pro_credit", g("Credit and risk", "الائتمان والمخاطر"), g("Credit risk, problem loans, write-offs and guarantors.", "مخاطر الائتمان والقروض المتعثرة وشطب الديون والكفلاء."),
                Skill.VOCABULARY, listOf("cr_risiko_kredit", "cr_penstrukturan", "cr_hapus_kira", "cr_kelayakan", "cr_peruntukan", "cr_penjamin"),
                extra = listOf(fill("cr1", L, "Bank menilai ___ kredit setiap pelanggan.", listOf("risiko")), choice("cr2", L, "Who pays if the borrower cannot?", listOf("penjamin", "juruwang", "peguam"), 0, Skill.VOCABULARY))),
            f.lesson("legal", B2, "pro_legal", g("Contract language", "لغة العقود"), g("Contracts, clauses, parties, liability and breach.", "العقود والبنود والأطراف والمسؤولية والإخلال."),
                Skill.VOCABULARY, listOf("lg_kontrak", "lg_fasal", "lg_pihak", "lg_liabiliti", "lg_pelanggaran"),
                extra = listOf(fill("lg1", L, "Kedua-dua ___ menandatangani kontrak itu.", listOf("pihak")), choice("lg2", L, "A section of a contract:", listOf("fasal", "ansuran", "cagaran"), 0, Skill.VOCABULARY))),
            f.lesson("ev_essentials", A1, "survival", g("Most useful phrases", "العبارات الأكثر استخدامًا"), g("Phrases you will use every day and when travelling.", "عبارات تستخدمها كل يوم وفي السفر."),
                Skill.SPEAKING, listOf("ev_maafkan", "ev_tak_faham", "ev_perlahan", "ev_ulang", "ev_tandas", "ev_bantu", "ev_tak_tahu", "ev_sama_sama"),
                extra = listOf(
                    fill("ev1", L, "Boleh cakap ___ sikit?", listOf("perlahan")),
                    choice("ev2", L, "Someone thanks you. You reply…", listOf("Sama-sama.", "Saya tak tahu.", "Maafkan saya."), 0, Skill.FUNCTIONAL),
                )),
            f.lesson("ev_emergency", A2, "survival", g("Emergencies and help", "الطوارئ وطلب المساعدة"), g("Ask for help, report a loss and find the police or a hospital.", "اطلب المساعدة وأبلغ عن فقدان شيء واعثر على الشرطة أو المستشفى."),
                Skill.SPEAKING, listOf("ev_tolong", "ev_polis", "ev_ambulans", "ev_pasport", "ev_dicuri", "ev_kedutaan", "ev_hospital", "ev_kecemasan"),
                extra = listOf(
                    fill("ev3", L, "Pasport saya ___. (lost)", listOf("hilang")),
                    choice("ev4", L, "Your bag was stolen:", listOf("Beg saya dicuri.", "Saya suka beg saya.", "Beg saya mahal."), 0, Skill.FUNCTIONAL),
                )),
            f.lesson("ev_restaurant", A2, "tourism", g("At the restaurant", "في المطعم"), g("Ask for a table, order a meal, mention allergies and pay.", "اطلب طاولة واطلب وجبة واذكر الحساسية وادفع."),
                Skill.SPEAKING, listOf("ev_meja", "ev_cadangkan", "ev_alah", "ev_vegetarian", "ev_pembuka_selera", "ev_hidangan_utama", "ev_pencuci_mulut", "ev_tip"),
                extra = listOf(
                    fill("ev5", L, "Meja untuk dua ___, ya.", listOf("orang")),
                    choice("ev6", L, "'Pencuci mulut' on a menu is…", listOf("dessert", "a starter", "the tip"), 0, Skill.VOCABULARY),
                )),
            f.lesson("ev_hotel", A2, "tourism", g("At the hotel", "في الفندق"), g("Check in, ask about breakfast and Wi-Fi, check out.", "سجّل وصولك واسأل عن الفطور والواي فاي وسجّل المغادرة."),
                Skill.LISTENING, listOf("ev_tempahan", "ev_bilik_bujang", "ev_bilik_kelamin", "ev_kunci", "ev_sarapan", "ev_wifi", "ev_kaunter", "ev_daftar_keluar"),
                extra = listOf(
                    fill("ev7", L, "Sarapan ___?", listOf("termasuk")),
                    dictation("ev8", L, "Saya ada tempahan untuk dua malam.", listOf("Saya ada tempahan untuk dua malam")),
                )),
            f.lesson("ev_transport", A2, "tourism", g("Trains, buses and flights", "القطارات والحافلات والطائرات"), g("Buy tickets, find your platform and handle delays.", "اشترِ التذاكر واعثر على رصيفك وتعامل مع التأخير."),
                Skill.LISTENING, listOf("ev_platform", "ev_pergi_balik", "ev_sehala", "ev_bertolak", "ev_kelewatan", "ev_pas_masuk", "ev_bagasi", "ev_perhentian_bas"),
                extra = listOf(
                    choice("ev9", L, "A return ticket is…", listOf("tiket pergi balik", "tiket sehala", "platform"), 0, Skill.VOCABULARY),
                    dictation("ev10", L, "Kereta api lewat setengah jam.", listOf("Kereta api lewat setengah jam")),
                )),
            f.lesson("ev_sightseeing", A2, "tourism", g("Sightseeing", "السياحة والمعالم"), g("Museums, tickets, tours and photos.", "المتاحف والتذاكر والجولات والصور."),
                Skill.READING, listOf("ev_muzium", "ev_waktu_buka", "ev_tiket_masuk", "ev_lawatan_berpandu", "ev_peta", "ev_gambar", "ev_bandar_lama", "ev_cenderamata"),
                extra = listOf(
                    fill("ev11", L, "Muzium ___ pukul berapa? (opens)", listOf("buka")),
                    choice("ev12", L, "A guided tour is…", listOf("lawatan berpandu", "bandar lama", "cenderamata"), 0, Skill.VOCABULARY),
                )),
        )
        val units = listOf(
            CourseUnit("ms_u_basics", L, A1, g("First steps", "الخطوات الأولى"), g("Greetings and family", "التحيات والعائلة"), listOf("ms_l_salam", "ms_l_keluarga")),
            CourseUnit("ms_u_everyday", L, A1, g("Everyday life", "الحياة اليومية"), g("Café and routines", "المقهى والروتين"), listOf("ms_l_kafe", "ms_l_rutin")),
            CourseUnit("ms_u_travel", L, A2, g("Out and about", "في الخارج"), g("Directions, shopping and the past", "الاتجاهات والتسوق والماضي"), listOf("ms_l_perjalanan", "ms_l_membeli", "ms_l_masa_lalu")),
            CourseUnit("ms_u_home", L, A2, g("Home and health", "البيت والصحة"), g("Your home, the weather and the doctor", "بيتك والطقس والطبيب"), listOf("ms_l_rumah_cuaca", "ms_l_kesihatan")),
            CourseUnit("ms_u_pro_business", L, B1, g("Malay for work", "الملايوية للعمل"), g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), listOf("ms_l_kerja"), trackId = "business"),
            CourseUnit("ms_u_next", L, B1, g("Next steps", "الخطوة التالية"), g("Plans, needs and wishes", "الخطط والضرورات والأمنيات"), listOf("ms_l_rancangan", "ms_l_harapan")),
            CourseUnit("ms_u_pro_finance", L, B1, g("Banking and finance", "البنوك والتمويل"), g("Core banking vocabulary", "مفردات البنوك الأساسية"), listOf("ms_l_kewangan"), trackId = "finance"),
            CourseUnit("ms_u_pro_credit", L, B2, g("Credit and risk", "الائتمان والمخاطر"), g("Credit risk and problem loans", "مخاطر الائتمان والقروض المتعثرة"), listOf("ms_l_credit"), trackId = "credit_risk"),
            CourseUnit("ms_u_pro_legal", L, B2, g("Legal Malay", "الملايوية القانونية"), g("Contracts", "العقود"), listOf("ms_l_legal"), trackId = "legal"),
            CourseUnit("ms_u_survival", L, A1, g("Essential phrases", "العبارات الأساسية"), g("Everyday phrases and emergencies", "العبارات اليومية والطوارئ"), listOf("ms_l_ev_essentials", "ms_l_ev_emergency")),
            CourseUnit("ms_u_tourism", L, A2, g("Travel and tourism", "السفر والسياحة"), g("Restaurants, hotels, transport and sightseeing", "المطاعم والفنادق والمواصلات والمعالم"), listOf("ms_l_ev_restaurant", "ms_l_ev_hotel", "ms_l_ev_transport", "ms_l_ev_sightseeing")),
        )
        val readings = listOf(
            ReadingPassage(
                "ms_r_aminah", L, A1, "Hari Aminah", g("Aminah's day", "يوم أمينة"),
                listOf("Aminah bangun pukul tujuh pagi.", "Dia minum teh dan makan roti.", "Dia bekerja di sebuah bank.", "Pada waktu petang, dia belajar bahasa Inggeris.", "Dia penat tetapi gembira."),
                listOf(g("Aminah gets up at seven in the morning.", "تنهض أمينة في السابعة صباحًا."), g("She drinks tea and eats bread.", "تشرب الشاي وتأكل الخبز."),
                    g("She works in a bank.", "تعمل في بنك."), g("In the evening she studies English.", "في المساء تدرس الإنجليزية."), g("She is tired but happy.", "هي متعبة لكنها سعيدة.")),
                listOf("ms_bangun", "ms_teh", "ms_roti", "ms_bekerja", "ms_belajar", "ms_penat", "ms_gembira"),
                listOf(
                    Exercise.MultipleChoice("ms_r_aminah_q1", Skill.READING, Instr.choose, "Di mana Aminah bekerja?", listOf("Di stesen", "Di bank", "Di kafe"), 1),
                    Exercise.MultipleChoice("ms_r_aminah_q2", Skill.READING, Instr.choose, "Apa yang dia belajar pada waktu petang?", listOf("Bahasa Inggeris", "Bahasa Arab", "Bahasa Perancis"), 0),
                ),
            ),
        )
        val scenarios = listOf(
            RolePlayScenario(
                "ms_s_warung", L, A1, g("At a food stall", "عند كشك الطعام"), g("A busy food stall in Kuala Lumpur at breakfast time.", "كشك طعام مزدحم في كوالالمبور وقت الفطور."),
                g("Order nasi lemak and a drink and pay.", "اطلب ناسي ليماك ومشروبًا وادفع."),
                "Ya, nak makan apa?", g("Yes, what would you like to eat?", "نعم، ماذا تريد أن تأكل؟"),
                listOf(
                    ScenarioBeat(listOf("nasi lemak", "nak", "boleh", "satu"), g("Say: 'Satu nasi lemak, ya.'", "قل: Satu nasi lemak, ya."),
                        "Satu nasi lemak, ya.", "Boleh. Minum apa?", g("Sure. What would you like to drink?", "حسنًا. ماذا تشرب؟")),
                    ScenarioBeat(listOf("teh tarik", "teh", "air", "kopi", "tak"), g("Order a drink: 'Teh tarik satu.'", "اطلب مشروبًا: Teh tarik satu."),
                        "Teh tarik satu.", "Semuanya sembilan ringgit.", g("That's nine ringgit altogether.", "المجموع تسعة رينغيت.")),
                    ScenarioBeat(listOf("ini", "terima kasih"), g("Pay and say thank you.", "ادفع وقل شكرًا."),
                        "Ini duitnya. Terima kasih!", "Sama-sama, datang lagi!", g("You're welcome, come again!", "عفوًا، عُد مرة أخرى!")),
                ),
                listOf("Satu nasi lemak, ya.", "Teh tarik satu.", "Berapa semuanya?"),
            ),
            RolePlayScenario(
                "ms_s_klinik", L, A2, g("At the doctor's", "عند الطبيب"), g("A clinic in Johor Bahru. Language practice only — not medical advice.", "عيادة في جوهور بارو. للتدرّب اللغوي فقط، وليس استشارة طبية."),
                g("Explain what hurts, say since when, and ask about the pharmacy.", "اشرح ما يؤلمك ومنذ متى، واسأل عن الصيدلية."),
                "Selamat pagi, sila duduk. Apa masalahnya?", g("Good morning, please sit down. What's the problem?", "صباح الخير، تفضّل بالجلوس. ما المشكلة؟"),
                listOf(
                    ScenarioBeat(listOf("sakit", "kepala", "demam"), g("Say what hurts: 'Kepala saya sakit.'", "قل ما يؤلمك: Kepala saya sakit."),
                        "Kepala saya sakit dan saya demam.", "Sejak bila?", g("Since when?", "منذ متى؟")),
                    ScenarioBeat(listOf("sejak", "semalam", "hari", "minggu"), g("Say since when: 'Sejak semalam.'", "قل منذ متى: Sejak semalam."),
                        "Sejak semalam.", "Baiklah. Rehat dan minum banyak air.", g("All right. Rest and drink plenty of water.", "حسنًا. استرح واشرب الكثير من الماء.")),
                    ScenarioBeat(listOf("farmasi", "mana"), g("Ask where the pharmacy is.", "اسأل أين الصيدلية."),
                        "Di mana farmasi yang paling dekat?", "Betul-betul di sebelah, di kanan.", g("Right next door, on the right.", "بجانبنا مباشرة، على اليمين.")),
                ),
                listOf("… saya sakit.", "Saya demam.", "Sejak bila?", "Di mana farmasi?"),
            ),
            RolePlayScenario(
                "ms_s_mesyuarat", L, B1, g("Planning a project with a client", "التخطيط لمشروع مع عميل"), g("A video call with a client in Penang.", "مكالمة فيديو مع عميل في بينانغ."),
                g("Agree on the price, the delivery date and the next step.", "اتفق على السعر وموعد التسليم والخطوة التالية."),
                "Selamat pagi. Terima kasih atas cadangan anda. Boleh kita bincang harganya?", g("Good morning. Thank you for your proposal. Can we discuss the price?", "صباح الخير. شكرًا على مقترحكم. هل نناقش السعر؟"),
                listOf(
                    ScenarioBeat(listOf("harga", "ringgit", "dolar", "sebut harga", "jumlah"), g("Explain the price.", "اشرح السعر."),
                        "Boleh. Jumlah harganya lima ribu dolar.", "Baik. Bila tarikh penghantaran?", g("Good. When is the delivery date?", "جيد. متى موعد التسليم؟")),
                    ScenarioBeat(listOf("minggu", "bulan", "tarikh", "hantar"), g("Give a date: 'dalam masa enam minggu'.", "حدّد موعدًا: dalam masa enam minggu."),
                        "Kami boleh hantar dalam masa enam minggu.", "Itu sesuai untuk kami.", g("That suits us.", "يناسبنا ذلك.")),
                    ScenarioBeat(listOf("e-mel", "emel", "email", "ringkasan", "kontrak"), g("Say you will send a summary by email.", "قل إنك سترسل ملخصًا بالبريد الإلكتروني."),
                        "Saya akan hantar ringkasan melalui e-mel hari ini.", "Terima kasih banyak. Jumpa lagi.", g("Thank you very much. Speak soon.", "شكرًا جزيلًا. إلى اللقاء.")),
                ),
                listOf("Jumlah harganya …", "Kami boleh hantar dalam masa …", "Saya akan hantar …"),
                trackId = "business",
            ),
            RolePlayScenario(
                "ms_s_hotel", L, A2, g("Checking in at a hotel", "تسجيل الوصول في فندق"), g("A hotel reception in Melaka.", "مكتب استقبال فندق في ملقا."),
                g("Check in, show your passport and ask for the Wi-Fi password.", "سجّل وصولك وأظهر جواز سفرك واسأل عن كلمة مرور الواي فاي."),
                "Selamat petang, selamat datang. Boleh saya bantu?", g("Good afternoon, welcome. Can I help you?", "مساء الخير، أهلًا بك. هل أساعدك؟"),
                listOf(
                    ScenarioBeat(listOf("tempahan", "nama", "bilik"), g("Say: 'Saya ada tempahan atas nama …'", "قل: Saya ada tempahan atas nama …"),
                        "Saya ada tempahan atas nama Hasan.", "Ya, bilik kelamin untuk dua malam. Boleh saya lihat pasport?", g("Yes, a double room for two nights. May I see your passport?", "نعم، غرفة مزدوجة لليلتين. هل يمكنني رؤية جواز سفرك؟")),
                    ScenarioBeat(listOf("ini", "pasport"), g("Hand it over: 'Ini pasport saya.'", "قدّمه: Ini pasport saya."),
                        "Ini pasport saya.", "Terima kasih. Sarapan dari pukul tujuh hingga sepuluh.", g("Thank you. Breakfast is from seven to ten.", "شكرًا. الفطور من السابعة إلى العاشرة.")),
                    ScenarioBeat(listOf("wi-fi", "wifi", "kata laluan", "internet"), g("Ask for the Wi-Fi password.", "اسأل عن كلمة مرور الواي فاي."),
                        "Apa kata laluan Wi-Fi?", "Ada pada kad anda. Selamat menginap!", g("It's on your card. Enjoy your stay!", "إنها على بطاقتك. إقامة سعيدة!")),
                ),
                listOf("Saya ada tempahan atas nama …", "Ini pasport saya.", "Sarapan termasuk?"),
            ),
            RolePlayScenario(
                "ms_s_restoran", L, A2, g("Dinner at a restaurant", "العشاء في مطعم"), g("A seafood restaurant in Kota Kinabalu.", "مطعم مأكولات بحرية في كوتا كينابالو."),
                g("Get a table, mention an allergy and order a main course.", "احصل على طاولة واذكر حساسيتك واطلب طبقًا رئيسيًا."),
                "Selamat datang! Ada tempahan?", g("Welcome! Do you have a booking?", "أهلًا وسهلًا! هل لديكم حجز؟"),
                listOf(
                    ScenarioBeat(listOf("meja", "dua", "tak", "tidak"), g("Say no and ask for a table for two.", "قل لا واطلب طاولة لشخصين."),
                        "Tidak, ada meja untuk dua orang?", "Ada. Ini menunya. Ada alahan?", g("Yes. Here's the menu. Any allergies?", "نعم. هذه القائمة. هل لديكم حساسية؟")),
                    ScenarioBeat(listOf("alah", "alahan", "kacang", "tidak"), g("Mention an allergy: 'Saya alah kepada kacang.'", "اذكر حساسيتك: Saya alah kepada kacang."),
                        "Saya alah kepada kacang.", "Baiklah, saya beritahu tukang masak. Nak makan apa?", g("All right, I'll tell the cook. What would you like?", "حسنًا، سأُخبر الطاهي. ماذا تريد أن تأكل؟")),
                    ScenarioBeat(listOf("hidangan utama", "ikan", "ayam", "nak", "mahu"), g("Order: 'Untuk hidangan utama, saya nak ikan bakar.'", "اطلب: Untuk hidangan utama, saya nak ikan bakar."),
                        "Untuk hidangan utama, saya nak ikan bakar.", "Baik, sekejap ya.", g("Fine, just a moment.", "حسنًا، لحظة من فضلك.")),
                ),
                listOf("Meja untuk dua orang, ya.", "Saya alah kepada …", "Saya nak …", "Minta bil, ya."),
            ),
            RolePlayScenario(
                "ms_s_stesen", L, A2, g("Buying a train ticket", "شراء تذكرة قطار"), g("The ticket counter at KL Sentral.", "شبّاك التذاكر في محطة كوالالمبور المركزية."),
                g("Buy a return ticket and find out the platform.", "اشترِ تذكرة ذهاب وعودة واعرف رقم الرصيف."),
                "Ya, boleh saya bantu?", g("Yes, can I help you?", "نعم، هل أساعدك؟"),
                listOf(
                    ScenarioBeat(listOf("tiket", "pergi balik", "ipoh", "butterworth"), g("Ask for a return ticket to Ipoh.", "اطلب تذكرة ذهاب وعودة إلى إيبوه."),
                        "Satu tiket pergi balik ke Ipoh.", "Baik. Kereta api seterusnya bertolak pukul sepuluh suku.", g("Fine. The next train leaves at a quarter past ten.", "حسنًا. يغادر القطار التالي في العاشرة والربع.")),
                    ScenarioBeat(listOf("platform", "mana"), g("Ask which platform: 'Dari platform mana?'", "اسأل عن الرصيف: Dari platform mana?"),
                        "Dari platform mana?", "Platform tiga. Lewat lebih kurang sepuluh minit.", g("Platform three. It's about ten minutes late.", "الرصيف الثالث. متأخر نحو عشر دقائق.")),
                    ScenarioBeat(listOf("terima kasih"), g("Thank them.", "اشكره."),
                        "Terima kasih banyak.", "Sama-sama. Selamat jalan!", g("You're welcome. Have a good trip!", "عفوًا. رحلة سعيدة!")),
                ),
                listOf("Satu tiket pergi balik ke …", "Dari platform mana?", "Kereta api tepat pada masanya?"),
            ),
        )
        fun item(id: String, level: CefrLevel, skill: Skill, prompt: String, options: List<String>, correct: Int) =
            AssessmentItem("ms_a_$id", L, level, skill, Exercise.MultipleChoice("ms_a_$id", skill, Instr.choose, prompt, options, correct))
        val assessment = listOf(
            item("a1_1", A1, Skill.GRAMMAR, "Which sentence is correct? (I am tired)", listOf("Saya penat.", "Saya adalah penat.", "Saya ada penat."), 0),
            item("a1_2", A1, Skill.VOCABULARY, "'Terima kasih' means…", listOf("please", "thank you", "sorry"), 1),
            item("a1_3", A1, Skill.GRAMMAR, "tiga ___ (three cats)", listOf("kucing", "kucing-kucing", "kucingan"), 0),
            item("a2_1", A2, Skill.GRAMMAR, "Saya ___ makan sekarang. (in progress)", listOf("sedang", "sudah", "akan"), 0),
            item("a2_2", A2, Skill.GRAMMAR, "Dia pergi ___ sekolah.", listOf("ke", "di", "dari"), 0),
            item("a2_3", A2, Skill.VOCABULARY, "Di restoran, untuk membayar anda minta ___.", listOf("menu", "bil", "tiket"), 1),
            item("b1_1", B1, Skill.GRAMMAR, "Saya ___ makan, terima kasih. (already)", listOf("sudah", "akan", "sedang"), 0),
            item("b1_2", B1, Skill.GRAMMAR, "Esok kami ___ pergi ke Melaka.", listOf("akan", "sudah", "telah"), 0),
            item("b1_3", B1, Skill.GRAMMAR, "Saya ___ pernah ke Sabah. (never yet)", listOf("belum", "bukan", "jangan"), 0),
            item("b2_1", B2, Skill.GRAMMAR, "Pinjaman itu ___ oleh bank.", listOf("diluluskan", "meluluskan", "lulus"), 0),
            item("b2_2", B2, Skill.GRAMMAR, "Dia belajar bersungguh-sungguh ___ lulus peperiksaan.", listOf("supaya", "walaupun", "kerana"), 0),
            item("b2_3", B2, Skill.GRAMMAR, "___ hujan, kami tetap keluar.", listOf("Walaupun", "Supaya", "Kerana"), 0),
        )
        return LanguageCurriculum(L, vocabulary, grammar, units, lessons, readings, scenarios, assessment,
            tracks = listOf(LearningTrack("business", L, g("Malay for work", "الملايوية للعمل"), g("Meetings, formal emails, quotes and deadlines.", "الاجتماعات والرسائل الرسمية وعروض الأسعار والمواعيد النهائية."), A2, null),
                LearningTrack("finance", L, g("Malay for banking and finance", "الملايوية للبنوك والتمويل"), g("Accounts, loans, interest, collateral and financial statements.", "الحسابات والقروض والفوائد والضمانات والقوائم المالية."), B1,
                    g("This track teaches Malay vocabulary used in this field. It is not financial, legal or professional advice.", "يعلّم هذا المسار مفردات الملايوية المستخدمة في هذا المجال، ولا يُعدّ استشارة مالية أو قانونية أو مهنية.")),
                LearningTrack("credit_risk", L, g("Credit and risk", "الائتمان والمخاطر"), g("Credit risk, problem loans, restructuring and guarantors.", "مخاطر الائتمان والقروض المتعثرة وإعادة الهيكلة والكفلاء."), B2,
                    g("This track teaches Malay vocabulary used in this field. It is not financial, legal or professional advice.", "يعلّم هذا المسار مفردات الملايوية المستخدمة في هذا المجال، ولا يُعدّ استشارة مالية أو قانونية أو مهنية.")),
                LearningTrack("legal", L, g("Legal Malay", "الملايوية القانونية"), g("Contracts, clauses, parties and liability.", "العقود والبنود والأطراف والمسؤولية."), B2,
                    g("This track teaches Malay vocabulary used in this field. It is not financial, legal or professional advice.", "يعلّم هذا المسار مفردات الملايوية المستخدمة في هذا المجال، ولا يُعدّ استشارة مالية أو قانونية أو مهنية."))),
            dictations = listOf(
                dictation("d1", L, "Nama saya Aminah.", listOf("Nama saya Aminah")),
                dictation("d2", L, "Di mana stesen?", listOf("Di mana stesen")),
                dictation("d3", L, "Biasanya saya bekerja pada waktu pagi.", listOf("Biasanya saya bekerja pada waktu pagi")),
            ),
            writingPrompts = listOf(writing("wp1", L, g("Describe your weekend in Malay.", "صِف عطلة نهاية الأسبوع بالملايوية."), 30, listOf("Sabtu", "Ahad", "saya"))),
        )
    }
}
