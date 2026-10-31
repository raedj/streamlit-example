package com.polyglot.academy.core.content

import com.polyglot.academy.core.assessment.AssessmentItem
import com.polyglot.academy.core.model.CefrLevel.A1
import com.polyglot.academy.core.model.CefrLevel.A2
import com.polyglot.academy.core.model.CefrLevel.B1
import com.polyglot.academy.core.model.CefrLevel.B2
import com.polyglot.academy.core.model.CefrLevel.C1
import com.polyglot.academy.core.model.CefrLevel
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

internal object EnglishCurriculum {
    private val L = LanguageCode.ENGLISH

    fun build(): LanguageCurriculum {
        val vb = VocabBuilder(L)
        with(vb) {
            // Greetings & introductions
            v("hello", "hello", "a friendly greeting", "مرحبًا", PHRASE, A1, "greetings", "/həˈloʊ/",
                ex = "Hello, my name is Sara.", exEn = "Hello, my name is Sara.", exAr = "مرحبًا، اسمي سارة.")
            v("good_morning", "good morning", "greeting used before noon", "صباح الخير", PHRASE, A1, "greetings", "/ɡʊd ˈmɔːrnɪŋ/",
                ex = "Good morning! How are you?", exEn = "Good morning! How are you?", exAr = "صباح الخير! كيف حالك؟")
            v("thank_you", "thank you", "words to show gratitude", "شكرًا لك", PHRASE, A1, "greetings", "/ˈθæŋk juː/",
                ex = "Thank you for your help.", exEn = "Thank you for your help.", exAr = "شكرًا لك على مساعدتك.")
            v("please", "please", "a polite word used when asking", "من فضلك", ADVERB, A1, "greetings", "/pliːz/",
                ex = "A glass of water, please.", exEn = "A glass of water, please.", exAr = "كوب ماء من فضلك.")
            v("sorry", "sorry", "used to apologise", "آسف", ADJECTIVE, A1, "greetings", "/ˈsɑːri/",
                ex = "Sorry, I'm late.", exEn = "Sorry, I'm late.", exAr = "آسف، لقد تأخرت.")
            v("goodbye", "goodbye", "said when leaving", "مع السلامة", PHRASE, A1, "greetings", "/ɡʊdˈbaɪ/",
                ex = "Goodbye, see you tomorrow.", exEn = "Goodbye, see you tomorrow.", exAr = "مع السلامة، أراك غدًا.")
            v("name", "name", "the word you are called by", "اسم", NOUN, A1, "greetings", "/neɪm/",
                ex = "What is your name?", exEn = "What is your name?", exAr = "ما اسمك؟")
            v("nice_to_meet", "nice to meet you", "said when meeting someone for the first time", "تشرّفت بمعرفتك", PHRASE, A1, "greetings", "/naɪs tə miːt juː/",
                ex = "I'm Omar. Nice to meet you.", exEn = "I'm Omar. Nice to meet you.", exAr = "أنا عمر. تشرّفت بمعرفتك.")
            // People & family
            v("family", "family", "parents, children, brothers and sisters", "عائلة", NOUN, A1, "people", "/ˈfæməli/",
                ex = "My family lives in Amman.", exEn = "My family lives in Amman.", exAr = "تعيش عائلتي في عمّان.")
            v("mother", "mother", "female parent", "أم", NOUN, A1, "people", "/ˈmʌðər/")
            v("father", "father", "male parent", "أب", NOUN, A1, "people", "/ˈfɑːðər/")
            v("brother", "brother", "a male sibling", "أخ", NOUN, A1, "people", "/ˈbrʌðər/")
            v("sister", "sister", "a female sibling", "أخت", NOUN, A1, "people", "/ˈsɪstər/")
            v("friend", "friend", "a person you like and know well", "صديق", NOUN, A1, "people", "/frend/",
                ex = "She is my best friend.", exEn = "She is my best friend.", exAr = "هي أعزّ صديقاتي.")
            // Food & café
            v("water", "water", "the clear liquid we drink", "ماء", NOUN, A1, "food", "/ˈwɔːtər/")
            v("coffee", "coffee", "a hot brown drink", "قهوة", NOUN, A1, "food", "/ˈkɔːfi/",
                ex = "I'd like a coffee, please.", exEn = "I'd like a coffee, please.", exAr = "أريد قهوة من فضلك.")
            v("tea", "tea", "a hot drink made from leaves", "شاي", NOUN, A1, "food", "/tiː/")
            v("bread", "bread", "food made from flour and baked", "خبز", NOUN, A1, "food", "/bred/")
            v("menu", "menu", "a list of food in a restaurant", "قائمة الطعام", NOUN, A2, "food", "/ˈmenjuː/",
                ex = "Can I see the menu, please?", exEn = "Can I see the menu, please?", exAr = "هل يمكنني رؤية قائمة الطعام من فضلك؟")
            v("bill", "the bill", "the paper showing how much to pay in a restaurant", "الحساب (الفاتورة)", NOUN, A2, "food", "/bɪl/",
                ex = "Could we have the bill, please?", exEn = "Could we have the bill, please?", exAr = "هل يمكننا الحصول على الحساب من فضلك؟")
            v("hungry", "hungry", "wanting to eat", "جائع", ADJECTIVE, A1, "food", "/ˈhʌŋɡri/")
            // Numbers & time
            v("today", "today", "this day", "اليوم", ADVERB, A1, "time", "/təˈdeɪ/")
            v("tomorrow", "tomorrow", "the day after today", "غدًا", ADVERB, A1, "time", "/təˈmɑːroʊ/")
            v("yesterday", "yesterday", "the day before today", "أمس", ADVERB, A1, "time", "/ˈjestərdeɪ/")
            v("morning", "morning", "the early part of the day", "صباح", NOUN, A1, "time", "/ˈmɔːrnɪŋ/")
            v("evening", "evening", "the end of the day, before night", "مساء", NOUN, A1, "time", "/ˈiːvnɪŋ/")
            v("week", "week", "seven days", "أسبوع", NOUN, A1, "time", "/wiːk/")
            // Travel & directions
            v("station", "station", "a place where trains or buses stop", "محطة", NOUN, A2, "travel", "/ˈsteɪʃən/",
                ex = "Where is the train station?", exEn = "Where is the train station?", exAr = "أين محطة القطار؟")
            v("ticket", "ticket", "a paper that lets you travel or enter", "تذكرة", NOUN, A2, "travel", "/ˈtɪkɪt/",
                ex = "One ticket to London, please.", exEn = "One ticket to London, please.", exAr = "تذكرة واحدة إلى لندن من فضلك.")
            v("left", "left", "the opposite of right (direction)", "يسار", NOUN, A1, "travel", "/left/")
            v("right", "right", "the opposite of left (direction)", "يمين", NOUN, A1, "travel", "/raɪt/")
            v("straight_on", "straight on", "forward without turning", "إلى الأمام مباشرة", PHRASE, A2, "travel", "/streɪt ɒn/",
                ex = "Go straight on and turn left.", exEn = "Go straight on and turn left.", exAr = "امشِ إلى الأمام مباشرة ثم انعطف يسارًا.")
            v("airport", "airport", "a place where planes take off and land", "مطار", NOUN, A2, "travel", "/ˈerpɔːrt/")
            v("hotel", "hotel", "a building where you pay to stay", "فندق", NOUN, A1, "travel", "/hoʊˈtel/")
            // Daily routine
            v("wake_up", "wake up", "to stop sleeping", "يستيقظ", VERB, A1, "routine", "/weɪk ʌp/",
                ex = "I wake up at seven.", exEn = "I wake up at seven.", exAr = "أستيقظ في السابعة.")
            v("work_v", "work", "to do a job", "يعمل", VERB, A1, "routine", "/wɜːrk/")
            v("study", "study", "to learn about a subject", "يدرس", VERB, A1, "routine", "/ˈstʌdi/",
                ex = "I study English every day.", exEn = "I study English every day.", exAr = "أدرس الإنجليزية كل يوم.")
            v("usually", "usually", "most of the time", "عادةً", ADVERB, A2, "routine", "/ˈjuːʒuəli/",
                ex = "I usually drink tea in the morning.", exEn = "I usually drink tea in the morning.", exAr = "عادةً أشرب الشاي في الصباح.")
            v("busy", "busy", "having a lot to do", "مشغول", ADJECTIVE, A2, "routine", "/ˈbɪzi/")
            // Feelings & opinions
            v("happy", "happy", "feeling good and pleased", "سعيد", ADJECTIVE, A1, "feelings", "/ˈhæpi/")
            v("tired", "tired", "needing rest or sleep", "متعب", ADJECTIVE, A1, "feelings", "/ˈtaɪərd/")
            v("worried", "worried", "unhappy because you think something bad may happen", "قلق", ADJECTIVE, A2, "feelings", "/ˈwɜːrid/")
            v("i_think", "I think", "used to give your opinion", "أعتقد / في رأيي", PHRASE, A2, "feelings", "/aɪ θɪŋk/",
                ex = "I think this book is interesting.", exEn = "I think this book is interesting.", exAr = "أعتقد أن هذا الكتاب ممتع.")
            v("although", "although", "despite the fact that", "على الرغم من أن", ADVERB, B1, "feelings", "/ɔːlˈðoʊ/",
                ex = "Although it was raining, we went out.", exEn = "Although it was raining, we went out.", exAr = "على الرغم من أن المطر كان يهطل، خرجنا.")
            // Work & study (B1)
            v("meeting", "meeting", "when people come together to discuss something", "اجتماع", NOUN, A2, "work", "/ˈmiːtɪŋ/",
                ex = "The meeting starts at ten.", exEn = "The meeting starts at ten.", exAr = "يبدأ الاجتماع في العاشرة.")
            v("deadline", "deadline", "the time by which something must be finished", "الموعد النهائي", NOUN, B1, "work", "/ˈdedlaɪn/",
                ex = "The deadline for the report is Friday.", exEn = "The deadline for the report is Friday.", exAr = "الموعد النهائي للتقرير هو الجمعة.")
            v("experience", "experience", "knowledge or skill you get from doing something", "خبرة", NOUN, B1, "work", "/ɪkˈspɪriəns/")
            v("improve", "improve", "to become or make better", "يحسّن / يتحسّن", VERB, B1, "work", "/ɪmˈpruːv/",
                ex = "I want to improve my speaking.", exEn = "I want to improve my speaking.", exAr = "أريد أن أحسّن مهارة التحدث لديّ.")
            v("schedule", "schedule", "a plan of when things will happen", "جدول زمني", NOUN, B1, "work", "/ˈskedʒuːl/")
            v("achieve", "achieve", "to succeed in doing something after effort", "يحقّق", VERB, B2, "work", "/əˈtʃiːv/",
                ex = "She achieved her goal of running a marathon.", exEn = "She achieved her goal of running a marathon.", exAr = "حقّقت هدفها بالركض في ماراثون.")
            // Shopping
            v("how_much", "how much", "used to ask the price", "بكم / كم السعر", PHRASE, A1, "shopping", "/haʊ mʌtʃ/",
                ex = "How much is this shirt?", exEn = "How much is this shirt?", exAr = "بكم هذا القميص؟")
            v("cheap", "cheap", "not costing much money", "رخيص", ADJECTIVE, A1, "shopping", "/tʃiːp/")
            v("expensive", "expensive", "costing a lot of money", "غالٍ", ADJECTIVE, A1, "shopping", "/ɪkˈspensɪv/")
            v("size", "size", "how big or small something is", "مقاس / حجم", NOUN, A2, "shopping", "/saɪz/")

            // Next steps: plans and conditions
            v("plan", "plan", "something you intend to do", "خطة", NOUN, B1, "plans", "/plæn/",
                ex = "What are your plans for the summer?", exEn = "What are your plans for the summer?", exAr = "ما خططك للصيف؟")
            v("going_to", "be going to", "used for plans you have already decided", "ينوي / سوف (لخطة مقرّرة)", PHRASE, A2, "plans")
            v("probably", "probably", "very likely", "على الأرجح", ADVERB, B1, "plans", "/ˈprɒbəbli/")
            v("abroad", "abroad", "in or to another country", "في الخارج", ADVERB, B1, "plans", "/əˈbrɔːd/")
            v("save_money", "save (money)", "to keep money instead of spending it", "يدّخر (المال)", VERB, B1, "plans")
            v("decide", "decide", "to make a choice", "يقرّر", VERB, B1, "plans", "/dɪˈsaɪd/")
            v("unless", "unless", "except if", "ما لم / إلا إذا", PHRASE, B2, "conditions", "/ənˈles/",
                ex = "I won't go unless you come too.", exEn = "I won't go unless you come too.", exAr = "لن أذهب إلا إذا أتيت أنت أيضًا.")
            v("afford", "afford", "to have enough money for something", "يقدر على تحمّل تكلفة", VERB, B2, "conditions", "/əˈfɔːd/")
            v("opportunity", "opportunity", "a chance to do something", "فرصة", NOUN, B1, "conditions", "/ˌɒpəˈtjuːnəti/")
            v("in_case", "in case", "because something might happen", "تحسّبًا لـ", PHRASE, B2, "conditions",
                ex = "Take an umbrella in case it rains.", exEn = "Take an umbrella in case it rains.", exAr = "خذ مظلة تحسّبًا للمطر.")
            v("regret", "regret", "to feel sorry about something you did", "يندم", VERB, B2, "conditions", "/rɪˈɡret/")
            // Daily life and travel
            v("ev_excuse_me", "Excuse me", "a polite way to get someone's attention", "عفوًا (لجذب الانتباه بأدب)", PHRASE, A1, "essentials", ex = "Excuse me, is this seat free?", exEn = "Excuse me, is this seat free?", exAr = "عفوًا، هل هذا المقعد شاغر؟")
            v("ev_dont_understand", "I don't understand.", "used when you can't follow what was said", "لا أفهم.", PHRASE, A1, "essentials")
            v("ev_speak_slowly", "Could you speak more slowly, please?", "a polite request to slow down", "هل يمكنك التحدث ببطء أكثر من فضلك؟", PHRASE, A1, "essentials")
            v("ev_say_again", "Could you say that again?", "asking someone to repeat", "هل يمكنك أن تعيد ذلك؟", PHRASE, A1, "essentials")
            v("ev_toilets", "Where are the toilets?", "asking where the toilets are", "أين دورات المياه؟", PHRASE, A1, "essentials")
            v("ev_help_me", "Can you help me?", "asking for help", "هل يمكنك مساعدتي؟", PHRASE, A1, "essentials")
            v("ev_dont_know", "I don't know.", "saying you have no information", "لا أعرف.", PHRASE, A1, "essentials")
            v("ev_no_problem", "No problem.", "a friendly reply meaning 'it's fine'", "لا مشكلة.", PHRASE, A1, "essentials")
            v("ev_help", "Help!", "shouted when you are in danger", "النجدة!", PHRASE, A1, "emergency")
            v("ev_police", "police", "the people who keep public order", "الشرطة", NOUN, A1, "emergency")
            v("ev_ambulance", "Call an ambulance!", "asking someone to call medical help", "اتصل بالإسعاف!", PHRASE, A2, "emergency", ex = "Please call an ambulance, he's hurt!", exEn = "Please call an ambulance, he's hurt!", exAr = "اتصل بالإسعاف من فضلك، إنه مصاب!")
            v("ev_lost_passport", "I've lost my passport.", "reporting a lost passport", "أضعتُ جواز سفري.", PHRASE, A2, "emergency")
            v("ev_stolen", "My bag has been stolen.", "reporting a theft", "سُرقت حقيبتي.", PHRASE, A2, "emergency")
            v("ev_embassy", "embassy", "your country's official office abroad", "السفارة", NOUN, A2, "emergency")
            v("ev_hospital", "hospital", "a place where sick people are treated", "المستشفى", NOUN, A1, "emergency")
            v("ev_emergency_number", "emergency number", "the phone number for police, fire or ambulance", "رقم الطوارئ", NOUN, A2, "emergency")
            v("ev_table_for", "A table for two, please.", "asking for a table", "طاولة لشخصين من فضلك.", PHRASE, A1, "restaurant", ex = "Good evening. A table for two, please.", exEn = "Good evening. A table for two, please.", exAr = "مساء الخير. طاولة لشخصين من فضلك.")
            v("ev_recommend", "What do you recommend?", "asking for a suggestion", "بماذا تنصحني؟", PHRASE, A2, "restaurant")
            v("ev_allergic", "I'm allergic to nuts.", "saying a food makes you ill", "عندي حساسية من المكسرات.", PHRASE, A2, "restaurant")
            v("ev_vegetarian", "vegetarian", "not eating meat or fish", "نباتي", ADJECTIVE, A2, "restaurant")
            v("ev_starter", "starter", "a small first dish", "المقبّلات", NOUN, A2, "restaurant")
            v("ev_main_course", "main course", "the biggest dish of a meal", "الطبق الرئيسي", NOUN, A2, "restaurant")
            v("ev_dessert", "dessert", "sweet food at the end of a meal", "الحلوى", NOUN, A1, "restaurant")
            v("ev_tip", "tip", "extra money you give for good service", "البقشيش", NOUN, A2, "restaurant")
            v("ev_reservation", "reservation", "an arrangement to keep a room or table for you", "حجز", NOUN, A2, "hotel", ex = "I have a reservation under the name Hassan.", exEn = "I have a reservation under the name Hassan.", exAr = "لديّ حجز باسم حسن.")
            v("ev_check_in", "check in", "to arrive and register at a hotel", "تسجيل الوصول", VERB, A2, "hotel")
            v("ev_check_out", "check out", "to pay and leave a hotel", "تسجيل المغادرة", VERB, A2, "hotel")
            v("ev_single_room", "single room", "a room for one person", "غرفة لشخص واحد", NOUN, A2, "hotel")
            v("ev_double_room", "double room", "a room for two people", "غرفة مزدوجة", NOUN, A2, "hotel")
            v("ev_key", "key", "a small object that opens a door", "مفتاح", NOUN, A1, "hotel")
            v("ev_breakfast_included", "Is breakfast included?", "asking if breakfast is in the price", "هل الفطور مشمول؟", PHRASE, A2, "hotel")
            v("ev_wifi_password", "What's the Wi-Fi password?", "asking how to connect to the internet", "ما كلمة مرور الواي فاي؟", PHRASE, A2, "hotel")
            v("ev_platform", "platform", "where you get on a train", "الرصيف", NOUN, A2, "transport", ex = "The train leaves from platform four.", exEn = "The train leaves from platform four.", exAr = "يغادر القطار من الرصيف رقم أربعة.")
            v("ev_return_ticket", "return ticket", "a ticket to go and come back", "تذكرة ذهاب وعودة", NOUN, A2, "transport")
            v("ev_single_ticket", "single ticket", "a ticket for one direction", "تذكرة ذهاب فقط", NOUN, A2, "transport")
            v("ev_departure", "departure", "the time something leaves", "المغادرة", NOUN, A2, "transport")
            v("ev_delayed", "delayed", "later than planned", "متأخر", ADJECTIVE, A2, "transport")
            v("ev_boarding_pass", "boarding pass", "the card you need to get on a plane", "بطاقة الصعود إلى الطائرة", NOUN, A2, "transport")
            v("ev_luggage", "luggage", "the bags you travel with", "الأمتعة", NOUN, A2, "transport")
            v("ev_bus_stop", "bus stop", "where a bus stops for passengers", "موقف الحافلات", NOUN, A1, "transport")
            v("ev_museum", "museum", "a building where art or history is shown", "متحف", NOUN, A1, "sightseeing", ex = "What time does the museum open?", exEn = "What time does the museum open?", exAr = "متى يفتح المتحف؟")
            v("ev_opening_hours", "opening hours", "the times a place is open", "مواعيد العمل", NOUN, A2, "sightseeing")
            v("ev_entrance_fee", "entrance fee", "money you pay to go in", "رسم الدخول", NOUN, A2, "sightseeing")
            v("ev_guided_tour", "guided tour", "a visit with a guide who explains", "جولة مع مرشد", NOUN, A2, "sightseeing")
            v("ev_map", "map", "a drawing of an area that shows the way", "خريطة", NOUN, A1, "sightseeing")
            v("ev_take_photo", "Could you take a photo of us?", "asking someone to take your picture", "هل يمكنك أن تلتقط لنا صورة؟", PHRASE, A2, "sightseeing")
            v("ev_old_town", "old town", "the historic centre of a city", "المدينة القديمة", NOUN, A2, "sightseeing")
            v("ev_souvenir", "souvenir", "something you buy to remember a trip", "تذكار", NOUN, A2, "sightseeing")
        }
        val pro = professionalVocabulary()
        val vocabulary = vb.items + pro
        val grammar = grammarTopics()
        val f = LessonFactory(L, vocabulary)

        val lessons = listOf(
            f.lesson("greetings", A1, "basics", g("Greetings", "التحيات"), g("Say hello, thank people and introduce yourself.", "ألقِ التحية واشكر الآخرين وعرّف بنفسك."),
                Skill.SPEAKING, listOf("hello", "good_morning", "thank_you", "please", "sorry", "goodbye"), "to_be",
                extra = listOf(
                    fill("g1", L, "My name ___ Sara.", listOf("is"), g("Use a form of 'to be'.", "استخدم صيغة من فعل 'to be'.")),
                    fill("g2", L, "I ___ from Egypt.", listOf("am", "'m"), g("I + am", "I + am")),
                    order("g3", L, listOf("Nice", "to", "meet", "you"), g("Nice to meet you", "تشرّفت بمعرفتك")),
                ), minutes = 6),
            f.lesson("introductions", A1, "basics", g("Introducing yourself", "التعريف بالنفس"), g("Talk about your name, family and friends.", "تحدّث عن اسمك وعائلتك وأصدقائك."),
                Skill.VOCABULARY, listOf("name", "nice_to_meet", "family", "mother", "father", "brother", "sister", "friend"), "to_be",
                extra = listOf(
                    fill("i1", L, "They ___ my brothers.", listOf("are", "'re")),
                    choice("i2", L, "She ___ my sister.", listOf("is", "are", "am"), 0),
                    translate("i3", L, g("This is my mother.", "هذه أمي."), listOf("This is my mother", "This is my mom", "This is my mum")),
                ), prerequisites = listOf("greetings")),
            f.lesson("cafe", A1, "everyday", g("At the café", "في المقهى"), g("Order drinks and food politely.", "اطلب المشروبات والطعام بأدب."),
                Skill.SPEAKING, listOf("water", "coffee", "tea", "bread", "menu", "bill", "hungry", "please"), "would_like",
                extra = listOf(
                    fill("c1", L, "I'd ___ a tea, please.", listOf("like"), g("I would like = I'd like", "I would like = I'd like")),
                    order("c2", L, listOf("Could", "we", "have", "the", "bill", "please?"), g("Could we have the bill, please?", "هل يمكننا الحصول على الحساب من فضلك؟")),
                    dictation("c3", L, "Can I see the menu, please?", listOf("Can I see the menu please")),
                ), prerequisites = listOf("introductions")),
            f.lesson("time", A1, "everyday", g("Days and time", "الأيام والوقت"), g("Talk about today, tomorrow and your week.", "تحدّث عن اليوم والغد وأسبوعك."),
                Skill.VOCABULARY, listOf("today", "tomorrow", "yesterday", "morning", "evening", "week"), "present_simple",
                extra = listOf(
                    fill("t1", L, "I ___ (study) in the evening.", listOf("study")),
                    fill("t2", L, "She ___ (work) every morning.", listOf("works"), g("he/she/it + verb + s", "he/she/it + الفعل + s")),
                ), prerequisites = listOf("cafe")),
            f.lesson("routine", A2, "everyday", g("Daily routine", "الروتين اليومي"), g("Describe what you usually do.", "صِف ما تفعله عادةً."),
                Skill.WRITING, listOf("wake_up", "work_v", "study", "usually", "busy", "tired"), "present_simple",
                extra = listOf(
                    fill("r1", L, "He usually ___ up at six.", listOf("wakes"), g("he + wakes", "he + wakes")),
                    order("r2", L, listOf("I", "usually", "drink", "tea"), g("I usually drink tea", "عادةً أشرب الشاي")),
                    writing("r3", L, g("Describe your typical morning in 3–5 sentences.", "صِف صباحك المعتاد في 3 إلى 5 جمل."), 25, listOf("usually", "wake up", "breakfast")),
                ), prerequisites = listOf("time")),
            f.lesson("travel", A2, "travel", g("Getting around", "التنقّل"), g("Buy tickets and ask for directions.", "اشترِ التذاكر واسأل عن الاتجاهات."),
                Skill.LISTENING, listOf("station", "ticket", "left", "right", "straight_on", "airport", "hotel"), "imperatives",
                extra = listOf(
                    fill("tr1", L, "___ left at the bank.", listOf("Turn", "turn")),
                    dictation("tr2", L, "Go straight on and turn left.", listOf("Go straight on and turn left")),
                    choice("tr3", L, "Excuse me, ___ is the station?", listOf("where", "who", "when"), 0),
                ), prerequisites = listOf("routine")),
            f.lesson("shopping", A2, "travel", g("Shopping", "التسوّق"), g("Ask about prices and sizes.", "اسأل عن الأسعار والمقاسات."),
                Skill.SPEAKING, listOf("how_much", "cheap", "expensive", "size"), "comparatives",
                extra = listOf(
                    fill("s1", L, "This shirt is ___ (cheap) than that one.", listOf("cheaper")),
                    fill("s2", L, "This bag is more ___ than the black one.", listOf("expensive")),
                ), prerequisites = listOf("travel")),
            f.lesson("opinions", B1, "work", g("Opinions and feelings", "الآراء والمشاعر"), g("Give opinions and explain how you feel.", "عبّر عن رأيك واشرح مشاعرك."),
                Skill.SPEAKING, listOf("happy", "worried", "i_think", "although", "improve"), "present_perfect",
                extra = listOf(
                    fill("o1", L, "I have ___ (finish) my homework.", listOf("finished")),
                    choice("o2", L, "She ___ to Paris twice.", listOf("has been", "have been", "is been"), 0,
                        explanation = g("he/she/it + has + past participle", "he/she/it + has + التصريف الثالث")),
                    translate("o3", L, g("I think it is a good idea.", "أعتقد أنها فكرة جيدة."), listOf("I think it is a good idea", "I think it's a good idea")),
                ), prerequisites = listOf("shopping")),
            f.lesson("work_life", B1, "work", g("Work and deadlines", "العمل والمواعيد النهائية"), g("Talk about meetings, schedules and goals.", "تحدّث عن الاجتماعات والجداول والأهداف."),
                Skill.READING, listOf("meeting", "deadline", "experience", "schedule", "achieve"), "present_perfect",
                extra = listOf(
                    fill("w1", L, "We have ___ (meet) the deadline.", listOf("met")),
                    writing("w2", L, g("Write an email to a colleague asking to move a meeting.", "اكتب رسالة لزميل تطلب فيها تأجيل اجتماع."), 40, listOf("meeting", "schedule", "could we")),
                ), prerequisites = listOf("opinions")),
            f.lesson("plans", B1, "next", g("Plans and predictions", "الخطط والتوقّعات"), g("Talk about the future with will and going to.", "تحدّث عن المستقبل باستخدام will وgoing to."),
                Skill.SPEAKING, listOf("plan", "going_to", "probably", "abroad", "save_money", "decide"), "future",
                extra = listOf(
                    fill("pl1", L, "I'm ___ to visit my aunt next week.", listOf("going")),
                    choice("pl2", L, "Look at those clouds! It ___ rain.", listOf("is going to", "would", "was"), 0),
                    fill("pl3", L, "A: The phone's ringing. B: I ___ get it!", listOf("will")),
                    writing("pl4", L, g("Write about your plans for next year (40+ words).", "اكتب عن خططك للعام القادم (40 كلمة على الأقل)."), 40, listOf("I'm going to", "I will", "probably")),
                ), prerequisites = listOf("work_life")),
            f.lesson("conditionals", B2, "next", g("If… : real and imagined", "لو وإذا: الواقعي والمتخيَّل"), g("Use first and second conditionals.", "استخدم الجمل الشرطية الأولى والثانية."),
                Skill.GRAMMAR, listOf("unless", "afford", "opportunity", "in_case", "regret", "decide"), "conditionals",
                extra = listOf(
                    fill("co1", L, "If it rains tomorrow, we ___ stay at home.", listOf("will")),
                    fill("co2", L, "If I ___ more money, I would travel more. (have)", listOf("had")),
                    choice("co3", L, "If I were you, I ___ accept the offer.", listOf("will", "would", "am"), 1),
                    translate("co4", L, g("If I had time, I would learn Chinese.", "لو كان لديّ وقت لتعلّمت الصينية."),
                        listOf("If I had time, I would learn Chinese", "If I had time I would learn Chinese", "If I had the time, I would learn Chinese")),
                ), prerequisites = listOf("plans")),
            f.lesson("ev_essentials", A1, "survival", g("Most useful phrases", "العبارات الأكثر استخدامًا"), g("Phrases you will use every day and when travelling.", "عبارات تستخدمها كل يوم وفي السفر."),
                Skill.SPEAKING, listOf("ev_excuse_me", "ev_dont_understand", "ev_speak_slowly", "ev_say_again", "ev_toilets", "ev_help_me", "ev_dont_know", "ev_no_problem"),
                extra = listOf(
                    fill("ev1", L, "Could you speak more ___, please?", listOf("slowly")),
                    choice("ev2", L, "You didn't hear the question. You say…", listOf("Could you say that again?", "See you later.", "No problem."), 0, Skill.FUNCTIONAL),
                )),
            f.lesson("ev_emergency", A2, "survival", g("Emergencies and help", "الطوارئ وطلب المساعدة"), g("Ask for help, report a loss and find the police or a hospital.", "اطلب المساعدة وأبلغ عن فقدان شيء واعثر على الشرطة أو المستشفى."),
                Skill.SPEAKING, listOf("ev_help", "ev_police", "ev_ambulance", "ev_lost_passport", "ev_stolen", "ev_embassy", "ev_hospital", "ev_emergency_number"),
                extra = listOf(
                    fill("ev3", L, "I've ___ my passport.", listOf("lost")),
                    choice("ev4", L, "Someone took your bag. You say…", listOf("My bag has been stolen.", "My bag is delicious.", "I'm allergic to bags."), 0, Skill.FUNCTIONAL),
                )),
            f.lesson("ev_restaurant", A2, "tourism", g("At the restaurant", "في المطعم"), g("Ask for a table, order a meal, mention allergies and pay.", "اطلب طاولة واطلب وجبة واذكر الحساسية وادفع."),
                Skill.SPEAKING, listOf("ev_table_for", "ev_recommend", "ev_allergic", "ev_vegetarian", "ev_starter", "ev_main_course", "ev_dessert", "ev_tip"),
                extra = listOf(
                    fill("ev5", L, "A table ___ two, please.", listOf("for")),
                    order("ev6", L, listOf("What", "do", "you", "recommend?"), g("What do you recommend?", "بماذا تنصحني؟")),
                )),
            f.lesson("ev_hotel", A2, "tourism", g("At the hotel", "في الفندق"), g("Check in, ask about breakfast and Wi-Fi, check out.", "سجّل وصولك واسأل عن الفطور والواي فاي وسجّل المغادرة."),
                Skill.LISTENING, listOf("ev_reservation", "ev_check_in", "ev_check_out", "ev_single_room", "ev_double_room", "ev_key", "ev_breakfast_included", "ev_wifi_password"),
                extra = listOf(
                    fill("ev7", L, "Is breakfast ___?", listOf("included")),
                    dictation("ev8", L, "I have a reservation for two nights.", listOf("I have a reservation for two nights")),
                )),
            f.lesson("ev_transport", A2, "tourism", g("Trains, buses and flights", "القطارات والحافلات والطائرات"), g("Buy tickets, find your platform and handle delays.", "اشترِ التذاكر واعثر على رصيفك وتعامل مع التأخير."),
                Skill.LISTENING, listOf("ev_platform", "ev_return_ticket", "ev_single_ticket", "ev_departure", "ev_delayed", "ev_boarding_pass", "ev_luggage", "ev_bus_stop"),
                extra = listOf(
                    choice("ev9", L, "A ticket to go and come back is a…", listOf("return ticket", "single ticket", "boarding pass"), 0, Skill.VOCABULARY),
                    dictation("ev10", L, "The train to London is delayed.", listOf("The train to London is delayed")),
                )),
            f.lesson("ev_sightseeing", A2, "tourism", g("Sightseeing", "السياحة والمعالم"), g("Museums, tickets, tours and photos.", "المتاحف والتذاكر والجولات والصور."),
                Skill.READING, listOf("ev_museum", "ev_opening_hours", "ev_entrance_fee", "ev_guided_tour", "ev_map", "ev_take_photo", "ev_old_town", "ev_souvenir"),
                extra = listOf(
                    fill("ev11", L, "What time does the museum ___?", listOf("open")),
                    choice("ev12", L, "The money you pay to go into a museum is the…", listOf("entrance fee", "tip", "deposit"), 0, Skill.VOCABULARY),
                )),
        )

        val units = listOf(
            CourseUnit("en_u_basics", L, A1, g("First steps", "الخطوات الأولى"), g("Greetings and introductions", "التحيات والتعارف"), listOf("en_l_greetings", "en_l_introductions")),
            CourseUnit("en_u_everyday", L, A1, g("Everyday life", "الحياة اليومية"), g("Food, time and routines", "الطعام والوقت والروتين"), listOf("en_l_cafe", "en_l_time", "en_l_routine")),
            CourseUnit("en_u_travel", L, A2, g("Out and about", "في الخارج"), g("Travel, directions and shopping", "السفر والاتجاهات والتسوق"), listOf("en_l_travel", "en_l_shopping")),
            CourseUnit("en_u_work", L, B1, g("Opinions and work", "الآراء والعمل"), g("Express ideas and talk about work", "التعبير عن الأفكار والحديث عن العمل"), listOf("en_l_opinions", "en_l_work_life")),
            CourseUnit("en_u_next", L, B1, g("Next steps", "الخطوة التالية"), g("Plans, predictions and conditions", "الخطط والتوقّعات والشروط"), listOf("en_l_plans", "en_l_conditionals")),
            CourseUnit("en_u_survival", L, A1, g("Essential phrases", "العبارات الأساسية"), g("Everyday phrases and emergencies", "العبارات اليومية والطوارئ"), listOf("en_l_ev_essentials", "en_l_ev_emergency")),
            CourseUnit("en_u_tourism", L, A2, g("Travel and tourism", "السفر والسياحة"), g("Restaurants, hotels, transport and sightseeing", "المطاعم والفنادق والمواصلات والمعالم"), listOf("en_l_ev_restaurant", "en_l_ev_hotel", "en_l_ev_transport", "en_l_ev_sightseeing")),
        ) + professionalUnits(f).first

        val proLessons = professionalUnits(f).second

        return LanguageCurriculum(
            language = L,
            vocabulary = vocabulary,
            grammar = grammar,
            units = units,
            lessons = lessons + proLessons,
            readings = readings(),
            scenarios = scenarios() + professionalScenarios(),
            assessment = assessment(),
            tracks = tracks,
            dictations = listOf(
                dictation("d1", L, "I usually drink coffee in the morning.", listOf("I usually drink coffee in the morning")),
                dictation("d2", L, "Where is the nearest station?", listOf("Where is the nearest station")),
                dictation("d3", L, "My sister lives in a small town.", listOf("My sister lives in a small town")),
                dictation("d4", L, "We have finished the report.", listOf("We've finished the report")),
                dictation("d5", L, "Although it was late, she kept working.", listOf("Although it was late she kept working"), difficulty = 4),
            ),
            writingPrompts = listOf(
                writing("wp1", L, g("Introduce yourself: name, city, family and hobbies.", "عرّف بنفسك: الاسم والمدينة والعائلة والهوايات."), 30, listOf("my name is", "I live in", "I like")),
                writing("wp2", L, g("Describe your last holiday.", "صِف عطلتك الأخيرة."), 50, listOf("went", "visited", "it was")),
                writing("wp3", L, g("Do you prefer studying alone or in a group? Explain why.", "هل تفضّل الدراسة وحدك أم في مجموعة؟ اشرح السبب."), 60, listOf("I think", "although", "because")),
            ),
        )
    }

    private fun grammarTopics() = listOf(
        grammar("to_be", L, A1, g("The verb 'to be'", "فعل الكينونة 'to be'"),
            g("Use am with I, is with he/she/it, and are with you/we/they. Arabic often omits 'to be' in the present, but English needs it.",
                "استخدم am مع I، وis مع he/she/it، وare مع you/we/they. كثيرًا ما تُحذف 'الكينونة' في العربية في المضارع، لكن الإنجليزية تحتاجها."),
            listOf(ex("I am a student.", "I am a student.", "أنا طالب."), ex("She is a doctor.", "She is a doctor.", "هي طبيبة."),
                ex("They are friends.", "They are friends.", "هم أصدقاء."), ex("She doctor.", "Missing 'is'", "ينقصها 'is'", wrong = true)),
            listOf(g("Leaving out 'is': ✗ He happy → ✓ He is happy.", "حذف 'is': ✗ He happy ← ✓ He is happy."),
                g("Using 'is' with I: ✗ I is → ✓ I am.", "استخدام 'is' مع I: ✗ I is ← ✓ I am."))),
        grammar("would_like", L, A1, g("Polite requests: I'd like / Could I", "الطلب بأدب: I'd like / Could I"),
            g("'I'd like…' (I would like) is more polite than 'I want…'. Use 'Could I/we have…?' to ask for something.",
                "عبارة I'd like (أي I would like) أكثر تهذيبًا من I want. واستخدم Could I/we have…? للطلب."),
            listOf(ex("I'd like a coffee, please.", "I'd like a coffee, please.", "أريد قهوة من فضلك."),
                ex("Could I have the menu?", "Could I have the menu?", "هل يمكنني الحصول على القائمة؟")),
            listOf(g("'Give me a coffee' can sound rude; prefer 'Could I have a coffee?'.", "قد تبدو عبارة Give me a coffee فظّة؛ الأفضل Could I have a coffee?"))),
        grammar("present_simple", L, A1, g("Present simple", "المضارع البسيط"),
            g("Use the present simple for habits and facts. Add -s/-es to the verb after he, she or it.",
                "يُستخدم المضارع البسيط للعادات والحقائق. أضف ‎-s أو ‎-es للفعل بعد he وshe وit."),
            listOf(ex("I work in an office.", "I work in an office.", "أعمل في مكتب."), ex("He works at night.", "He works at night.", "يعمل ليلًا."),
                ex("She work here.", "Missing -s", "ينقصها ‎-s", wrong = true)),
            listOf(g("Forgetting -s: ✗ She like tea → ✓ She likes tea.", "نسيان ‎-s: ✗ She like tea ← ✓ She likes tea."),
                g("Using 'is' with another verb: ✗ I am work → ✓ I work.", "استخدام is مع فعل آخر: ✗ I am work ← ✓ I work."))),
        grammar("imperatives", L, A2, g("Imperatives for directions", "صيغة الأمر للاتجاهات"),
            g("Give directions with the base verb: Go, Turn, Take. Add 'please' or 'Could you…' to be polite.",
                "أعطِ الاتجاهات بصيغة الفعل الأساسية: Go وTurn وTake. أضف please أو Could you… للتهذيب."),
            listOf(ex("Turn right at the bank.", "Turn right at the bank.", "انعطف يمينًا عند البنك."), ex("Take the second street on the left.", "Take the second street on the left.", "خذ الشارع الثاني على اليسار.")),
            listOf(g("Adding 'to': ✗ To turn left → ✓ Turn left.", "إضافة to: ✗ To turn left ← ✓ Turn left."))),
        grammar("comparatives", L, A2, g("Comparatives", "صيغ المقارنة"),
            g("Short adjectives add -er (cheap → cheaper). Long adjectives use 'more' (expensive → more expensive). Use 'than' to compare.",
                "الصفات القصيرة تأخذ ‎-er ‏(cheap ← cheaper)، والطويلة تُسبق بـ more ‏(more expensive). واستخدم than للمقارنة."),
            listOf(ex("This bag is cheaper than that one.", "This bag is cheaper than that one.", "هذه الحقيبة أرخص من تلك."),
                ex("This phone is more expensive.", "This phone is more expensive.", "هذا الهاتف أغلى."),
                ex("more cheaper", "Double comparative", "مقارنة مزدوجة", wrong = true)),
            listOf(g("Double comparison: ✗ more cheaper → ✓ cheaper.", "مقارنة مزدوجة: ✗ more cheaper ← ✓ cheaper."))),
        grammar("present_perfect", L, B1, g("Present perfect", "المضارع التام"),
            g("have/has + past participle connects the past to now: experiences (I have been to Rome) and results (I have finished).",
                "have/has + التصريف الثالث يربط الماضي بالحاضر: للتجارب (I have been to Rome) وللنتائج (I have finished)."),
            listOf(ex("I have visited Cairo twice.", "I have visited Cairo twice.", "زرت القاهرة مرتين."),
                ex("She has finished the report.", "She has finished the report.", "أنهت التقرير."),
                ex("I have seen him yesterday.", "Use past simple with 'yesterday'", "استخدم الماضي البسيط مع yesterday", wrong = true)),
            listOf(g("Using it with a finished time: ✗ I have seen him yesterday → ✓ I saw him yesterday.", "استخدامه مع زمن منتهٍ: ✗ I have seen him yesterday ← ✓ I saw him yesterday."))),
        grammar("future", L, B1, g("Future: will and going to", "المستقبل: will وgoing to"),
            g("Use 'be going to' for plans you have already made (I'm going to visit Rome) and for predictions from what you can see (Look at the clouds — it's going to rain). Use 'will' for decisions made now, promises and general predictions (I'll call you later).",
                "استخدم be going to للخطط المقرّرة مسبقًا (I'm going to visit Rome) وللتوقّع مما تراه (Look at the clouds — it's going to rain). واستخدم will للقرار اللحظي والوعود والتوقّعات العامة (I'll call you later)."),
            listOf(ex("We're going to move next month.", "We're moving next month (already planned).", "سننتقل الشهر القادم (أمر مقرّر)."),
                ex("I'll help you with that.", "A decision or offer made now.", "قرار أو عرض في اللحظة نفسها."),
                ex("I will going to travel.", "Use either 'will travel' or 'am going to travel'", "استخدم will travel أو am going to travel", wrong = true)),
            listOf(g("✗ I will to go → ✓ I will go (no 'to' after will).", "✗ I will to go ← ✓ I will go (لا تأتي to بعد will)."))),
        grammar("conditionals", L, B2, g("First and second conditionals", "الجمل الشرطية الأولى والثانية"),
            g("First conditional for real possibilities: If + present, will + verb (If it rains, we'll stay in). Second conditional for imaginary or unlikely situations: If + past, would + verb (If I had more time, I would learn Chinese). 'Unless' means 'if not'.",
                "الشرط الأول للاحتمالات الواقعية: If + مضارع، ثم will + فعل (If it rains, we'll stay in). والشرط الثاني للمواقف المتخيَّلة أو المستبعدة: If + ماضٍ، ثم would + فعل (If I had more time, I would learn Chinese). وunless تعني 'إذا لم'."),
            listOf(ex("If you study, you'll pass.", "A real possibility.", "احتمال واقعي."),
                ex("If I were rich, I would travel the world.", "An imaginary situation.", "موقف متخيَّل."),
                ex("If it will rain, we stay home.", "No 'will' in the if-clause", "لا تُستخدم will في جملة الشرط", wrong = true)),
            listOf(g("✗ If I would have money → ✓ If I had money.", "✗ If I would have money ← ✓ If I had money."))),
    )

    private fun readings() = listOf(
        ReadingPassage(
            "en_r_morning", L, A1, "Sara's morning", g("Sara's morning", "صباح سارة"),
            listOf("Sara wakes up at seven.", "She drinks tea and eats bread.", "Then she walks to the station.", "She works in a hotel.", "She is busy, but she is happy."),
            listOf(g("Sara wakes up at seven.", "تستيقظ سارة في السابعة."), g("She drinks tea and eats bread.", "تشرب الشاي وتأكل الخبز."),
                g("Then she walks to the station.", "ثم تمشي إلى المحطة."), g("She works in a hotel.", "تعمل في فندق."), g("She is busy, but she is happy.", "هي مشغولة لكنها سعيدة.")),
            listOf("en_wake_up", "en_tea", "en_bread", "en_station", "en_hotel", "en_busy", "en_happy"),
            listOf(
                Exercise.MultipleChoice("en_r_morning_q1", Skill.READING, Instr.choose, "What time does Sara wake up?", listOf("At six", "At seven", "At eight"), 1),
                Exercise.MultipleChoice("en_r_morning_q2", Skill.READING, Instr.choose, "Where does Sara work?", listOf("In a station", "In a hotel", "In a café"), 1),
                Exercise.MultipleChoice("en_r_morning_q3", Skill.READING, Instr.choose, "How does Sara feel?", listOf("Tired and sad", "Busy but happy", "Worried"), 1),
            ),
        ),
        ReadingPassage(
            "en_r_project", L, B1, "A new project", g("A new project", "مشروع جديد"),
            listOf(
                "Last month, Omar's team started a new project.",
                "The schedule is tight, and the deadline is in two weeks.",
                "Although Omar is worried, he thinks the team can achieve its goal.",
                "He has organised a short meeting every morning.",
                "The meetings have helped everyone improve their communication.",
            ),
            listOf(
                g("Last month, Omar's team started a new project.", "في الشهر الماضي، بدأ فريق عمر مشروعًا جديدًا."),
                g("The schedule is tight, and the deadline is in two weeks.", "الجدول الزمني ضيّق، والموعد النهائي بعد أسبوعين."),
                g("Although Omar is worried, he thinks the team can achieve its goal.", "على الرغم من قلق عمر، يعتقد أن الفريق يستطيع تحقيق هدفه."),
                g("He has organised a short meeting every morning.", "نظّم اجتماعًا قصيرًا كل صباح."),
                g("The meetings have helped everyone improve their communication.", "ساعدت الاجتماعات الجميع على تحسين تواصلهم."),
            ),
            listOf("en_schedule", "en_deadline", "en_worried", "en_achieve", "en_meeting", "en_improve"),
            listOf(
                Exercise.MultipleChoice("en_r_project_q1", Skill.READING, Instr.choose, "When is the deadline?", listOf("Tomorrow", "In two weeks", "Next month"), 1),
                Exercise.MultipleChoice("en_r_project_q2", Skill.READING, Instr.choose, "How does Omar feel about the project?", listOf("Worried but hopeful", "Bored", "Angry"), 0),
                Exercise.MultipleChoice("en_r_project_q3", Skill.READING, Instr.choose, "What effect have the meetings had?", listOf("They wasted time", "They improved communication", "They moved the deadline"), 1),
            ),
        ),
    )

    private fun scenarios() = listOf(
        RolePlayScenario(
            "en_s_cafe", L, A1, g("Ordering at a café", "الطلب في مقهى"), g("A busy café in the city centre.", "مقهى مزدحم في وسط المدينة."),
            g("Order a drink and something to eat, then ask for the bill.", "اطلب مشروبًا وشيئًا للأكل، ثم اطلب الحساب."),
            "Hi there! What can I get you today?", g("Hi there! What can I get you today?", "أهلًا! ماذا أحضر لك اليوم؟"),
            listOf(
                ScenarioBeat(listOf("coffee", "tea", "water", "juice", "latte"), g("Order a drink, e.g. 'I'd like a coffee, please.'", "اطلب مشروبًا، مثلًا: I'd like a coffee, please."),
                    "I'd like a coffee, please.", "Sure. Would you like anything to eat?", g("Sure. Would you like anything to eat?", "بالتأكيد. هل تريد شيئًا للأكل؟")),
                ScenarioBeat(listOf("yes", "sandwich", "cake", "bread", "croissant", "no"), g("Say what you'd like to eat, or 'No, thank you.'", "قل ما تريد أكله، أو No, thank you."),
                    "Yes, a sandwich, please.", "Great. Is that for here or to take away?", g("Great. Is that for here or to take away?", "رائع. هل هو هنا أم سفري؟")),
                ScenarioBeat(listOf("here", "take away", "takeaway", "to go"), g("Answer 'For here, please' or 'To take away, please.'", "أجب For here, please أو To take away, please."),
                    "For here, please.", "No problem. Anything else?", g("No problem. Anything else?", "لا مشكلة. هل تريد شيئًا آخر؟")),
                ScenarioBeat(listOf("bill", "pay", "how much", "check"), g("Ask for the bill: 'Could I have the bill, please?'", "اطلب الحساب: Could I have the bill, please?"),
                    "Could I have the bill, please?", "Of course. That's six pounds fifty.", g("Of course. That's six pounds fifty.", "بالطبع. المبلغ ستة جنيهات وخمسون بنسًا.")),
            ),
            listOf("I'd like…, please.", "Could I have…?", "For here, please.", "How much is it?"),
        ),
        RolePlayScenario(
            "en_s_directions", L, A2, g("Asking for directions", "السؤال عن الاتجاهات"), g("On a street, you are looking for the station.", "في الشارع، تبحث عن المحطة."),
            g("Find out how to get to the station and how long it takes.", "اعرف كيف تصل إلى المحطة وكم يستغرق ذلك."),
            "Hello! You look lost. Can I help you?", g("Hello! You look lost. Can I help you?", "مرحبًا! تبدو تائهًا. هل أستطيع مساعدتك؟"),
            listOf(
                ScenarioBeat(listOf("station", "where", "train"), g("Ask where the station is.", "اسأل أين المحطة."), "Yes, please. Where is the train station?",
                    "Go straight on, then take the second street on the left.", g("Go straight on, then take the second street on the left.", "امشِ إلى الأمام مباشرة، ثم خذ الشارع الثاني على اليسار.")),
                ScenarioBeat(listOf("far", "how long", "minutes", "walk"), g("Ask how far it is.", "اسأل كم تبعد."), "Is it far?",
                    "Not really. It's about a ten-minute walk.", g("Not really. It's about a ten-minute walk.", "ليس كثيرًا. حوالي عشر دقائق سيرًا.")),
                ScenarioBeat(listOf("thank", "thanks"), g("Thank the person.", "اشكر الشخص."), "Thank you very much!",
                    "You're welcome. Have a good trip!", g("You're welcome. Have a good trip!", "عفوًا. رحلة سعيدة!")),
            ),
            listOf("Where is…?", "Is it far?", "How long does it take?", "Thank you very much."),
        ),
        RolePlayScenario(
            "en_s_smalltalk", L, A1, g("Meeting someone new", "التعرّف على شخص جديد"), g("At a language exchange evening.", "في أمسية لتبادل اللغات."),
            g("Introduce yourself and ask two questions.", "عرّف بنفسك واطرح سؤالين."),
            "Hi! I'm Emma. What's your name?", g("Hi! I'm Emma. What's your name?", "مرحبًا! أنا إيما. ما اسمك؟"),
            listOf(
                ScenarioBeat(listOf("my name", "i'm", "i am", "name is"), g("Say your name: 'My name is…'", "قل اسمك: My name is…"), "My name is Omar.",
                    "Nice to meet you! Where are you from?", g("Nice to meet you! Where are you from?", "تشرّفت بمعرفتك! من أين أنت؟")),
                ScenarioBeat(listOf("from", "i live", "i'm from"), g("Say where you are from: 'I'm from…'", "قل من أين أنت: I'm from…"), "I'm from Jordan.",
                    "Lovely! What do you do?", g("Lovely! What do you do?", "جميل! ماذا تعمل؟")),
                ScenarioBeat(listOf("student", "work", "engineer", "teacher", "i am a", "i'm a", "job"), g("Say your job or what you study.", "قل عملك أو ماذا تدرس."), "I'm a student. I study engineering.",
                    "That's interesting! Why are you learning English?", g("That's interesting! Why are you learning English?", "هذا مثير للاهتمام! لماذا تتعلم الإنجليزية؟")),
                ScenarioBeat(listOf("because", "for", "work", "travel", "study", "want"), g("Give a reason with 'because…'", "اذكر سببًا باستخدام because…"), "Because I want to travel.",
                    "Great reason. Your English is getting better already!", g("Great reason. Your English is getting better already!", "سبب رائع. لغتك الإنجليزية تتحسن بالفعل!")),
            ),
            listOf("My name is…", "I'm from…", "I'm a…", "because…"),
        ),
        RolePlayScenario(
            "en_s_hotel", L, A2, g("Checking in at a hotel", "تسجيل الوصول في فندق"), g("A hotel reception in Edinburgh.", "مكتب استقبال فندق في إدنبرة."),
            g("Check in, show your passport and ask for the Wi-Fi password.", "سجّل وصولك وأظهر جواز سفرك واسأل عن كلمة مرور الواي فاي."),
            "Good evening, welcome to the Riverside Hotel. How can I help you?", g("Good evening, welcome to the Riverside Hotel. How can I help you?", "مساء الخير، أهلًا بك في فندق ريفرسايد. كيف أساعدك؟"),
            listOf(
                ScenarioBeat(listOf("reservation", "booked", "booking", "name"), g("Say you have a reservation and give your name.", "قل إن لديك حجزًا واذكر اسمك."),
                    "I have a reservation under the name Hassan.", "Yes, a double room for two nights. Could I see your passport, please?", g("Yes, a double room for two nights. Could I see your passport, please?", "نعم، غرفة مزدوجة لليلتين. هل أرى جواز سفرك من فضلك؟")),
                ScenarioBeat(listOf("here", "sure", "passport", "of course"), g("Hand over your passport: 'Here you are.'", "قدّم جواز سفرك: Here you are."),
                    "Sure, here you are.", "Thank you. Breakfast is from seven to ten.", g("Thank you. Breakfast is from seven to ten.", "شكرًا. الفطور من السابعة إلى العاشرة.")),
                ScenarioBeat(listOf("wifi", "wi-fi", "password", "internet"), g("Ask for the Wi-Fi password.", "اسأل عن كلمة مرور الواي فاي."),
                    "What's the Wi-Fi password?", "It's on your key card. Enjoy your stay!", g("It's on your key card. Enjoy your stay!", "إنها على بطاقة المفتاح. إقامة سعيدة!")),
            ),
            listOf("I have a reservation under the name…", "Here you are.", "What's the Wi-Fi password?", "Is breakfast included?"),
        ),
        RolePlayScenario(
            "en_s_restaurant", L, A2, g("Dinner at a restaurant", "العشاء في مطعم"), g("A busy restaurant in London.", "مطعم مزدحم في لندن."),
            g("Get a table, mention an allergy and order a main course.", "احصل على طاولة واذكر حساسيتك واطلب طبقًا رئيسيًا."),
            "Hello! Do you have a reservation?", g("Hello! Do you have a reservation?", "مرحبًا! هل لديكم حجز؟"),
            listOf(
                ScenarioBeat(listOf("table", "two", "no"), g("Say no and ask for a table for two.", "قل لا واطلب طاولة لشخصين."),
                    "No, but could we have a table for two?", "Of course. Here's the menu. Any allergies?", g("Of course. Here's the menu. Any allergies?", "بالتأكيد. هذه القائمة. هل لديكم حساسية من شيء؟")),
                ScenarioBeat(listOf("allergic", "allergy", "nuts", "no", "vegetarian"), g("Mention an allergy: 'I'm allergic to nuts.'", "اذكر حساسيتك: I'm allergic to nuts."),
                    "I'm allergic to nuts.", "No problem, I'll tell the chef. What would you like?", g("No problem, I'll tell the chef. What would you like?", "لا مشكلة، سأخبر الطاهي. ماذا تطلب؟")),
                ScenarioBeat(listOf("have", "like", "please", "fish", "chicken"), g("Order a main course: 'I'll have the fish, please.'", "اطلب طبقًا رئيسيًا: I'll have the fish, please."),
                    "I'll have the grilled fish, please.", "Great choice. It'll be ready in fifteen minutes.", g("Great choice. It'll be ready in fifteen minutes.", "اختيار ممتاز. سيكون جاهزًا خلال خمس عشرة دقيقة.")),
            ),
            listOf("A table for two, please.", "I'm allergic to…", "I'll have…", "Could we have the bill, please?"),
        ),
        RolePlayScenario(
            "en_s_station", L, A2, g("Buying a train ticket", "شراء تذكرة قطار"), g("A ticket office at a London station.", "شبّاك التذاكر في محطة بلندن."),
            g("Buy a return ticket and find out the platform.", "اشترِ تذكرة ذهاب وعودة واعرف رقم الرصيف."),
            "Hi there, where are you travelling to?", g("Hi there, where are you travelling to?", "مرحبًا، إلى أين تسافر؟"),
            listOf(
                ScenarioBeat(listOf("ticket", "return", "single", "oxford", "to"), g("Ask for a return ticket to Oxford.", "اطلب تذكرة ذهاب وعودة إلى أكسفورد."),
                    "A return ticket to Oxford, please.", "That's twenty-four pounds. The next train leaves at a quarter past ten.", g("That's twenty-four pounds. The next train leaves at a quarter past ten.", "المبلغ أربعة وعشرون جنيهًا. يغادر القطار التالي في العاشرة والربع.")),
                ScenarioBeat(listOf("platform", "which", "where"), g("Ask which platform it leaves from.", "اسأل من أي رصيف يغادر."),
                    "Which platform does it leave from?", "Platform three. It's running about ten minutes late.", g("Platform three. It's running about ten minutes late.", "الرصيف رقم ثلاثة. إنه متأخر نحو عشر دقائق.")),
                ScenarioBeat(listOf("thanks", "thank"), g("Thank them.", "اشكره."),
                    "Thanks for your help.", "You're welcome. Have a good trip!", g("You're welcome. Have a good trip!", "عفوًا. رحلة سعيدة!")),
            ),
            listOf("A return ticket to…, please.", "Which platform…?", "Is it on time?"),
        ),
    )

    private fun assessment(): List<AssessmentItem> {
        fun item(id: String, level: CefrLevel, skill: Skill, prompt: String, options: List<String>, correct: Int) =
            AssessmentItem("en_a_$id", L, level, skill, Exercise.MultipleChoice("en_a_$id", skill, Instr.choose, prompt, options, correct))
        return listOf(
            item("a1_1", A1, Skill.GRAMMAR, "I ___ a student.", listOf("am", "is", "are", "be"), 0),
            item("a1_2", A1, Skill.VOCABULARY, "The opposite of 'cheap' is…", listOf("small", "expensive", "happy", "new"), 1),
            item("a1_3", A1, Skill.READING, "'Good morning!' is usually said…", listOf("at night", "before noon", "when leaving", "when eating"), 1),
            item("a2_1", A2, Skill.GRAMMAR, "She ___ to work by bus every day.", listOf("go", "goes", "going", "is go"), 1),
            item("a2_2", A2, Skill.GRAMMAR, "This hotel is ___ than the other one.", listOf("more cheap", "cheaper", "cheapest", "more cheaper"), 1),
            item("a2_3", A2, Skill.VOCABULARY, "Could we have the ___, please? We'd like to pay.", listOf("menu", "bill", "ticket", "size"), 1),
            item("b1_1", B1, Skill.GRAMMAR, "I ___ to Spain three times.", listOf("have been", "was", "am", "had go"), 0),
            item("b1_2", B1, Skill.VOCABULARY, "We must finish the report before the ___ on Friday.", listOf("deadline", "headline", "schedule", "meeting"), 0),
            item("b1_3", B1, Skill.GRAMMAR, "___ it was raining, we went for a walk.", listOf("Because", "Although", "So", "Unless"), 1),
            item("b2_1", B2, Skill.GRAMMAR, "If I ___ more time, I would learn another language.", listOf("have", "had", "will have", "would have"), 1),
            item("b2_2", B2, Skill.VOCABULARY, "The new policy had a significant ___ on sales.", listOf("affect", "effect", "effort", "afford"), 1),
            item("b2_3", B2, Skill.GRAMMAR, "The report ___ by the manager yesterday.", listOf("was approved", "approved", "has approved", "is approving"), 0),
            item("c1_1", C1, Skill.GRAMMAR, "Hardly ___ the meeting started when the fire alarm went off.", listOf("had", "has", "did", "was"), 0),
            item("c1_2", C1, Skill.VOCABULARY, "Her argument was so ___ that nobody could disagree.", listOf("compelling", "compelled", "complying", "compiling"), 0),
            item("c1_3", C1, Skill.READING, "'The results were, to say the least, underwhelming.' The writer is…", listOf("very impressed", "disappointed", "confused", "angry"), 1),
        )
    }

    // ---------------- Professional tracks (optional) ----------------

    private val financeDisclaimer = g(
        "This track teaches English vocabulary used in this field. It is not financial, legal or professional advice.",
        "يعلّم هذا المسار المفردات الإنجليزية المستخدمة في هذا المجال، ولا يُعدّ استشارة مالية أو قانونية أو مهنية.",
    )

    val tracks = listOf(
        LearningTrack("business", L, g("Business English", "الإنجليزية للأعمال"), g("Meetings, emails, presentations and negotiation.", "الاجتماعات والرسائل والعروض والتفاوض."), A2, null),
        LearningTrack("finance", L, g("Banking and finance", "البنوك والتمويل"), g("Accounts, loans, interest and financial statements.", "الحسابات والقروض والفوائد والقوائم المالية."), B1, financeDisclaimer),
        LearningTrack("credit_risk", L, g("Credit and risk", "الائتمان والمخاطر"), g("Credit assessment, collateral, default and NPL management terminology.", "مصطلحات تقييم الائتمان والضمانات والتعثر وإدارة القروض المتعثرة."), B2, financeDisclaimer),
        LearningTrack("legal", L, g("Legal English", "الإنجليزية القانونية"), g("Contracts, clauses, obligations and liability.", "العقود والبنود والالتزامات والمسؤولية."), B2, financeDisclaimer),
        LearningTrack("career", L, g("Interviews and reports", "المقابلات والتقارير"), g("Job interviews, report writing and professional emails.", "مقابلات العمل وكتابة التقارير والرسائل المهنية."), A2, null),
    )

    private fun professionalVocabulary(): List<com.polyglot.academy.core.model.VocabularyItem> {
        val vb = VocabBuilder(L)
        val s = VocabularySource.PROFESSIONAL_TRACK
        with(vb) {
            // Business
            v("agenda", "agenda", "a list of topics to discuss at a meeting", "جدول الأعمال", NOUN, B1, "business", "/əˈdʒendə/",
                ex = "Let's move to the next item on the agenda.", exEn = "Let's move to the next item on the agenda.", exAr = "لننتقل إلى البند التالي في جدول الأعمال.", tags = setOf("business"), source = s)
            v("follow_up", "follow up", "to take further action after something", "يتابع", VERB, B1, "business", "/ˈfɑːloʊ ʌp/",
                ex = "I'll follow up with an email.", exEn = "I'll follow up with an email.", exAr = "سأتابع برسالة إلكترونية.", tags = setOf("business"), source = s)
            v("stakeholder", "stakeholder", "a person or group affected by a project", "صاحب مصلحة", NOUN, B2, "business", "/ˈsteɪkhoʊldər/", tags = setOf("business"), source = s)
            v("proposal", "proposal", "a formal suggestion or plan", "مقترح / عرض", NOUN, B1, "business", "/prəˈpoʊzəl/", tags = setOf("business"), source = s)
            v("compromise", "compromise", "an agreement where each side gives up something", "حلّ وسط", NOUN, B2, "business", "/ˈkɑːmprəmaɪz/",
                ex = "We reached a compromise on the price.", exEn = "We reached a compromise on the price.", exAr = "توصّلنا إلى حلّ وسط بشأن السعر.", tags = setOf("business"), source = s)
            v("attached", "please find attached", "used in emails when sending a file", "تجدون مرفقًا", PHRASE, B1, "business", tags = setOf("business"), source = s)
            // Finance
            v("account", "account", "an arrangement to keep money in a bank", "حساب", NOUN, A2, "finance", "/əˈkaʊnt/", tags = setOf("finance"), source = s)
            v("loan", "loan", "money borrowed that must be paid back", "قرض", NOUN, B1, "finance", "/loʊn/",
                ex = "The bank approved the loan.", exEn = "The bank approved the loan.", exAr = "وافق البنك على القرض.", tags = setOf("finance"), source = s)
            v("interest_rate", "interest rate", "the percentage charged for borrowing money", "سعر الفائدة", NOUN, B1, "finance", tags = setOf("finance"), source = s)
            v("balance_sheet", "balance sheet", "a statement of assets, liabilities and equity", "الميزانية العمومية", NOUN, B2, "finance", tags = setOf("finance"), source = s)
            v("liquidity", "liquidity", "how easily assets can be turned into cash", "السيولة", NOUN, B2, "finance", "/lɪˈkwɪdəti/", tags = setOf("finance"), source = s)
            v("repayment", "repayment", "paying back money that was borrowed", "سداد", NOUN, B1, "finance", tags = setOf("finance"), source = s)
            // Credit & risk / NPL
            v("collateral", "collateral", "an asset pledged to secure a loan", "ضمان (رهن)", NOUN, B2, "credit_risk", "/kəˈlætərəl/",
                ex = "The loan is secured by collateral.", exEn = "The loan is secured by collateral.", exAr = "القرض مضمون برهن.", tags = setOf("credit_risk"), source = s)
            v("default", "default", "failure to repay a debt as agreed", "تعثّر (عن السداد)", NOUN, B2, "credit_risk", "/dɪˈfɔːlt/", tags = setOf("credit_risk"), source = s)
            v("npl", "non-performing loan", "a loan where payments are significantly overdue (commonly 90+ days)", "قرض متعثّر (غير منتج)", NOUN, C1, "credit_risk",
                ex = "The bank reduced its non-performing loans last year.", exEn = "The bank reduced its non-performing loans last year.", exAr = "خفّض البنك قروضه المتعثرة العام الماضي.", tags = setOf("credit_risk"), source = s)
            v("restructuring", "restructuring", "changing the terms of a loan to help repayment", "إعادة الهيكلة", NOUN, C1, "credit_risk", tags = setOf("credit_risk"), source = s)
            v("write_off", "write-off", "removing a debt from the books as unrecoverable", "شطب (الدَّين)", NOUN, C1, "credit_risk", tags = setOf("credit_risk"), source = s)
            v("creditworthiness", "creditworthiness", "how likely a borrower is to repay", "الجدارة الائتمانية", NOUN, C1, "credit_risk", tags = setOf("credit_risk"), source = s)
            v("provision", "provision", "money set aside to cover expected losses", "مخصّص (للخسائر)", NOUN, C1, "credit_risk", tags = setOf("credit_risk"), source = s)
            // Legal
            v("contract", "contract", "a legal agreement between parties", "عقد", NOUN, B1, "legal", "/ˈkɑːntrækt/", tags = setOf("legal"), source = s)
            v("clause", "clause", "a section of a contract", "بند", NOUN, B2, "legal", "/klɔːz/",
                ex = "Clause 5 covers termination.", exEn = "Clause 5 covers termination.", exAr = "يغطي البند 5 إنهاء العقد.", tags = setOf("legal"), source = s)
            v("liability", "liability", "legal responsibility for something", "المسؤولية (القانونية)", NOUN, C1, "legal", tags = setOf("legal"), source = s)
            v("party", "party", "a person or organisation in an agreement", "طرف", NOUN, B2, "legal", tags = setOf("legal"), source = s)
            v("breach", "breach", "breaking the terms of an agreement", "إخلال (بالعقد)", NOUN, C1, "legal", tags = setOf("legal"), source = s)
            // Career
            v("strength", "strength", "something you are good at", "نقطة قوة", NOUN, B1, "career", tags = setOf("career"), source = s)
            v("responsible_for", "responsible for", "having the duty of doing something", "مسؤول عن", PHRASE, B1, "career",
                ex = "I was responsible for a team of five.", exEn = "I was responsible for a team of five.", exAr = "كنت مسؤولًا عن فريق من خمسة أشخاص.", tags = setOf("career"), source = s)
            v("summary", "executive summary", "a short overview at the start of a report", "الملخّص التنفيذي", NOUN, B2, "career", tags = setOf("career"), source = s)
            v("recommendation", "recommendation", "a suggestion about the best action", "توصية", NOUN, B1, "career", tags = setOf("career"), source = s)
        }
        return vb.items
    }

    private fun professionalUnits(f: LessonFactory): Pair<List<CourseUnit>, List<com.polyglot.academy.core.model.Lesson>> {
        val lessons = listOf(
            f.lesson("biz_meetings", B1, "pro_business", g("Running a meeting", "إدارة اجتماع"), g("Agendas, proposals and follow-ups.", "جداول الأعمال والمقترحات والمتابعة."),
                Skill.SPEAKING, listOf("agenda", "proposal", "follow_up", "stakeholder", "compromise", "attached"),
                extra = listOf(
                    fill("bm1", L, "Please find ___ the latest report.", listOf("attached")),
                    choice("bm2", L, "Which is more polite in a meeting?", listOf("You are wrong.", "I see your point, but I'm not sure I agree.", "No."), 1, Skill.FUNCTIONAL),
                )),
            f.lesson("fin_basics", B1, "pro_finance", g("Banking basics", "أساسيات البنوك"), g("Accounts, loans and interest.", "الحسابات والقروض والفوائد."),
                Skill.VOCABULARY, listOf("account", "loan", "interest_rate", "repayment", "balance_sheet", "liquidity"),
                extra = listOf(fill("fb1", L, "The ___ rate on this loan is 5%.", listOf("interest")))),
            f.lesson("credit", B2, "pro_credit", g("Credit risk and NPLs", "مخاطر الائتمان والقروض المتعثرة"), g("Collateral, default, restructuring and provisions.", "الضمانات والتعثّر وإعادة الهيكلة والمخصّصات."),
                Skill.READING, listOf("collateral", "default", "npl", "restructuring", "write_off", "creditworthiness", "provision"),
                extra = listOf(
                    choice("cr1", L, "A loan with payments 90+ days overdue is usually called…", listOf("a performing loan", "a non-performing loan", "a bridge loan"), 1, Skill.VOCABULARY),
                    fill("cr2", L, "The borrower offered his house as ___.", listOf("collateral")),
                )),
            f.lesson("legal_contracts", B2, "pro_legal", g("Contract language", "لغة العقود"), g("Parties, clauses, liability and breach.", "الأطراف والبنود والمسؤولية والإخلال."),
                Skill.READING, listOf("contract", "clause", "party", "liability", "breach"),
                extra = listOf(fill("lc1", L, "Both ___ must sign the contract.", listOf("parties")))),
            f.lesson("career_interview", B1, "pro_career", g("Interviews and reports", "المقابلات والتقارير"), g("Talk about strengths and write recommendations.", "تحدّث عن نقاط قوتك واكتب التوصيات."),
                Skill.WRITING, listOf("strength", "responsible_for", "summary", "recommendation", "experience"),
                extra = listOf(writing("ci1", L, g("Answer: 'Tell me about yourself.' (60+ words)", "أجب عن سؤال: حدّثني عن نفسك. (60 كلمة على الأقل)"), 60, listOf("responsible for", "experience", "strength")))),
        )
        val units = listOf(
            CourseUnit("en_u_pro_business", L, B1, g("Business English", "الإنجليزية للأعمال"), g("Meetings and emails", "الاجتماعات والرسائل"), listOf("en_l_biz_meetings"), trackId = "business"),
            CourseUnit("en_u_pro_finance", L, B1, g("Banking and finance", "البنوك والتمويل"), g("Core banking vocabulary", "مفردات البنوك الأساسية"), listOf("en_l_fin_basics"), trackId = "finance"),
            CourseUnit("en_u_pro_credit", L, B2, g("Credit and risk", "الائتمان والمخاطر"), g("Credit risk and NPL terminology", "مصطلحات مخاطر الائتمان والقروض المتعثرة"), listOf("en_l_credit"), trackId = "credit_risk"),
            CourseUnit("en_u_pro_legal", L, B2, g("Legal English", "الإنجليزية القانونية"), g("Contracts", "العقود"), listOf("en_l_legal_contracts"), trackId = "legal"),
            CourseUnit("en_u_pro_career", L, B1, g("Interviews and reports", "المقابلات والتقارير"), g("Career communication", "التواصل المهني"), listOf("en_l_career_interview"), trackId = "career"),
        )
        return units to lessons
    }

    private fun professionalScenarios() = listOf(
        RolePlayScenario(
            "en_s_negotiation", L, B2, g("Negotiating a delivery date", "التفاوض على موعد التسليم"), g("A call with a supplier.", "مكالمة مع مورّد."),
            g("Agree on a new delivery date and a small discount.", "اتفق على موعد تسليم جديد وخصم بسيط."),
            "Thanks for calling. I'm afraid we can't deliver until the end of the month.",
            g("Thanks for calling. I'm afraid we can't deliver until the end of the month.", "شكرًا على اتصالك. أخشى أننا لا نستطيع التسليم قبل نهاية الشهر."),
            listOf(
                ScenarioBeat(listOf("earlier", "sooner", "possible", "could you", "need"), g("Ask politely if an earlier date is possible.", "اسأل بأدب إن كان موعد أبكر ممكنًا."),
                    "Is there any way you could deliver earlier? We need it by the 20th.", "We could manage the 22nd if you accept partial delivery.",
                    g("We could manage the 22nd if you accept partial delivery.", "يمكننا تدبّر يوم 22 إذا قبلتم تسليمًا جزئيًا.")),
                ScenarioBeat(listOf("discount", "price", "reduce", "compensation"), g("Ask for a discount for the delay.", "اطلب خصمًا بسبب التأخير."),
                    "That works, but given the delay, could you offer a small discount?", "We can offer three percent.", g("We can offer three percent.", "يمكننا تقديم ثلاثة بالمئة.")),
                ScenarioBeat(listOf("agree", "deal", "accept", "fine", "ok", "okay"), g("Accept and confirm next steps.", "وافق وأكّد الخطوات التالية."),
                    "Agreed. I'll send you a confirmation email today.", "Perfect. I'll follow up with the paperwork.", g("Perfect. I'll follow up with the paperwork.", "ممتاز. سأتابع بإرسال المستندات.")),
            ),
            listOf("Is there any way you could…?", "Given the delay…", "I'll send you a confirmation."),
            trackId = "business",
        ),
        RolePlayScenario(
            "en_s_loan_review", L, C1, g("Reviewing a problem loan", "مراجعة قرض متعثّر"), g("A credit committee meeting (language practice only).", "اجتماع لجنة ائتمان (للتدرّب اللغوي فقط)."),
            g("Explain the situation and propose a restructuring.", "اشرح الوضع واقترح إعادة هيكلة."),
            "Let's start with the account that's ninety days past due. What's the situation?",
            g("Let's start with the account that's ninety days past due. What's the situation?", "لنبدأ بالحساب المتأخر تسعين يومًا. ما الوضع؟"),
            listOf(
                ScenarioBeat(listOf("cash flow", "revenue", "sales", "payments", "difficult"), g("Describe why the borrower is behind on payments.", "صِف سبب تأخّر المقترض في السداد."),
                    "The borrower's cash flow dropped after they lost a major client.", "Is the loan secured?", g("Is the loan secured?", "هل القرض مضمون؟")),
                ScenarioBeat(listOf("collateral", "secured", "property", "guarantee"), g("Explain the collateral.", "اشرح الضمانات."),
                    "Yes, it's secured by commercial property worth about twice the outstanding balance.", "What do you recommend?", g("What do you recommend?", "ماذا توصي؟")),
                ScenarioBeat(listOf("restructur", "extend", "reschedul", "recommend", "propose"), g("Propose a restructuring.", "اقترح إعادة هيكلة."),
                    "I recommend restructuring the loan by extending the term by twelve months.", "Reasonable. Please prepare the proposal for next week.",
                    g("Reasonable. Please prepare the proposal for next week.", "معقول. يرجى إعداد المقترح للأسبوع القادم.")),
            ),
            listOf("past due", "secured by", "I recommend…", "extend the term"),
            trackId = "credit_risk",
        ),
    )
}
