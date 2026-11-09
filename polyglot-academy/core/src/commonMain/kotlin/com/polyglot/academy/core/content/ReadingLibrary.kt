package com.polyglot.academy.core.content

import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CefrLevel.A1
import com.polyglot.academy.core.model.CefrLevel.A2
import com.polyglot.academy.core.model.CefrLevel.B1
import com.polyglot.academy.core.model.CefrLevel.B2
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.GlossaryEntry
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.OpenQuestion
import com.polyglot.academy.core.model.ReadingPassage
import com.polyglot.academy.core.model.Skill

/**
 * More graded readings for English and Mandarin (A1 to B2), and questions answered in the learner's own words
 * for these and the older English and Mandarin readings. Written by AI (2026-11-09), not yet reviewed by native speakers.
 */
internal object ReadingLibrary {
    private class Line(val text: String, val en: String, val ar: String)

    private fun l(text: String, en: String, ar: String) = Line(text, en, ar)
    private fun w(term: String, en: String, ar: String, reading: String? = null) = GlossaryEntry(term, Gloss(en, ar), reading)
    private fun q(id: String, prompt: String, options: List<String>, answer: Int, gloss: Gloss? = null, optionGlosses: List<Gloss>? = null) =
        Exercise.MultipleChoice(id, Skill.READING, Instr.choose, prompt, options, answer, promptGloss = gloss, optionGlosses = optionGlosses)

    /** A question about the text; [points] are words a good answer contains ("a|b" = either). */
    private fun o(id: String, prompt: String, en: String, ar: String, model: String, vararg points: String) =
        OpenQuestion(id, prompt, g(en, ar), model, points.toList())

    private fun retell(id: String, lang: LanguageCode, model: String, vararg points: String) = OpenQuestion(
        id, if (lang == LanguageCode.MANDARIN) "用两三句话说一说这篇短文。" else "Retell the text in two or three sentences.",
        g("Retell the text in two or three sentences.", "أعد سرد النص في جملتين أو ثلاث."), model, points.toList(), summary = true,
    )

    private fun story(
        id: String, lang: LanguageCode, level: CefrLevel, title: String, en: String, ar: String,
        lines: List<Line>, words: List<GlossaryEntry>, qs: List<Exercise.MultipleChoice>, open: List<OpenQuestion>,
    ) = ReadingPassage(id, lang, level, title, g(en, ar), lines.map { it.text }, lines.map { g(it.en, it.ar) }, emptyList(), qs, words, openQuestions = open)

    fun of(lang: LanguageCode): List<ReadingPassage> = when (lang) {
        LanguageCode.ENGLISH -> english
        LanguageCode.MANDARIN -> mandarin
        else -> emptyList()
    }

    /** Adds the open questions written for an older reading that was built without them. */
    fun withOpenQuestions(r: ReadingPassage): ReadingPassage =
        if (r.openQuestions.isNotEmpty()) r else olderReadings[r.id]?.let { r.copy(openQuestions = it) } ?: r

    private val olderReadings: Map<String, List<OpenQuestion>> by lazy {
        val E = LanguageCode.ENGLISH
        val Z = LanguageCode.MANDARIN
        mapOf(
            "en_r_morning" to listOf(
                o("en_r_morning_o1", "What does Sara have for breakfast?", "What does Sara have for breakfast?", "ماذا تتناول سارة في الفطور؟",
                    "She drinks tea and eats bread.", "tea", "bread"),
                retell("en_r_morning_o2", E, "Sara wakes up at seven, has tea and bread and walks to the station. She works in a hotel. She is busy but happy.",
                    "seven", "hotel", "busy|happy"),
            ),
            "en_r_project" to listOf(
                o("en_r_project_o1", "Why does Omar hold a meeting every morning?", "Why does Omar hold a meeting every morning?", "لماذا يعقد عمر اجتماعًا كل صباح؟",
                    "Because the deadline is close and the meetings help the team communicate better.", "deadline|schedule|time", "communicat|team"),
                retell("en_r_project_o2", E, "Omar's team has a new project with a tight deadline. Omar is worried, but short daily meetings have improved the team's communication.",
                    "project", "deadline|schedule", "meeting"),
            ),
            "en_st_market" to listOf(
                o("en_st_market_o1", "What do Lina and her brother buy?", "What do Lina and her brother buy?", "ماذا تشتري لينا وأخوها؟",
                    "They buy apples, tomatoes and fresh bread.", "apple", "tomato", "bread"),
                retell("en_st_market_o2", E, "On Saturday, Lina and her brother go to the market. They buy fruit, vegetables and bread, and then they cook lunch together.",
                    "market", "buy|bought", "lunch|cook"),
            ),
            "en_st_lost_bag" to listOf(
                o("en_st_lost_bag_o1", "What did the woman at the desk write down?", "What did the woman at the desk write down?", "ماذا دوّنت المرأة في المكتب؟",
                    "She wrote down his phone number and the address of his hotel.", "phone|number", "address|hotel"),
                retell("en_st_lost_bag_o2", E, "Karim flew to London, but his bag did not arrive. A kind woman at the information desk helped him, and the next morning the bag came to his hotel.",
                    "London|airport", "bag", "hotel|morning"),
            ),
            "en_st_volunteer" to listOf(
                o("en_st_volunteer_o1", "How did volunteering help Maya's English?", "How did volunteering help Maya's English?", "كيف ساعد التطوع لغة مايا الإنجليزية؟",
                    "She became more confident and learned many new words from the books.", "confident|confidence", "words|vocabulary"),
                retell("en_st_volunteer_o2", E, "Maya was too shy to speak English, so she read stories to children at a library. After a few months she felt more confident and knew more words.",
                    "shy", "library|children|stories", "confident"),
            ),
            "zh_r_mingming" to listOf(
                o("zh_r_mingming_o1", "明天我们去哪里？", "Where are we going tomorrow?", "إلى أين سنذهب غدًا؟", "我们去商店。", "商店"),
                retell("zh_r_mingming_o2", Z, "我有一个朋友，他叫明明。他是学生，很喜欢喝茶。", "明明", "学生", "茶"),
            ),
            "zh_st_shichang" to listOf(
                o("zh_st_shichang_o1", "他们买了什么？", "What did they buy?", "ماذا اشتريا؟", "他们买了苹果、西红柿和面包。", "苹果", "西红柿", "面包"),
                retell("zh_st_shichang_o2", Z, "星期六小丽和哥哥去市场买东西。小丽付了十块钱，然后他们回家一起做午饭。", "市场", "买", "午饭|回家"),
            ),
            "zh_st_xinglixiang" to listOf(
                o("zh_st_xinglixiang_o1", "服务台的小姐写下了什么？", "What did the woman at the service desk write down?", "ماذا دوّنت الموظفة في مكتب الخدمة؟",
                    "她写下了他的电话号码和酒店的地址。", "电话", "地址|酒店"),
                retell("zh_st_xinglixiang_o2", Z, "大卫坐飞机去北京，可是他的行李箱没有来。服务台的小姐帮了他。第二天早上，有人把行李箱送到了酒店。",
                    "行李箱", "服务台|小姐", "第二天|酒店"),
            ),
            "zh_st_zhiyuanzhe" to listOf(
                o("zh_st_zhiyuanzhe_o1", "阿米尔在图书馆做什么？", "What does Amir do at the library?", "ماذا يفعل أمير في المكتبة؟",
                    "他每个星期六给孩子们读故事。", "孩子", "故事"),
                retell("zh_st_zhiyuanzhe_o2", Z, "阿米尔不好意思说中文，所以他去图书馆给孩子们读故事。几个月以后，他有信心了，也学会了很多新词。",
                    "图书馆|故事", "信心", "新词|词"),
            ),
        )
    }

    private val english by lazy {
        val L = LanguageCode.ENGLISH
        listOf(
            story("en_st_new_flat", L, A1, "Our new flat", "Our new flat", "شقتنا الجديدة", listOf(
                l("We have a new flat.", "We have a new flat.", "لدينا شقة جديدة."),
                l("It is near the park.", "It is near the park.", "إنها قرب الحديقة."),
                l("The flat has two bedrooms and a small kitchen.", "The flat has two bedrooms and a small kitchen.", "في الشقة غرفتا نوم ومطبخ صغير."),
                l("My room is blue.", "My room is blue.", "غرفتي زرقاء."),
                l("My sister's room is yellow.", "My sister's room is yellow.", "غرفة أختي صفراء."),
                l("Our cat sleeps on the sofa all day.", "Our cat sleeps on the sofa all day.", "تنام قطتنا على الأريكة طوال اليوم."),
                l("In the evening, we eat dinner on the balcony.", "In the evening, we eat dinner on the balcony.", "في المساء نتناول العشاء على الشرفة."),
                l("I love our new home.", "I love our new home.", "أحب بيتنا الجديد."),
            ), listOf(
                w("new", "new", "جديد"), w("flat", "flat, apartment", "شقة"), w("near", "near", "قرب"), w("park", "park", "حديقة عامة"),
                w("bedrooms", "bedrooms", "غرف نوم"), w("small", "small", "صغير"), w("kitchen", "kitchen", "مطبخ"), w("room", "room", "غرفة"),
                w("blue", "blue", "أزرق"), w("sister", "sister", "أخت"), w("yellow", "yellow", "أصفر"), w("cat", "cat", "قطة"),
                w("sleeps", "sleeps", "تنام"), w("sofa", "sofa", "أريكة"), w("all day", "all day", "طوال اليوم"), w("evening", "evening", "مساء"),
                w("dinner", "dinner", "عشاء"), w("balcony", "balcony", "شرفة"), w("home", "home", "بيت"),
            ), listOf(
                q("en_st_new_flat_q1", "Where is the flat?", listOf("Near the park", "Near the station", "Near the sea"), 0),
                q("en_st_new_flat_q2", "What colour is my room?", listOf("Yellow", "Blue", "Green"), 1),
                q("en_st_new_flat_q3", "Where does the cat sleep?", listOf("On the bed", "On the balcony", "On the sofa"), 2),
            ), listOf(
                o("en_st_new_flat_o1", "Where do they eat dinner in the evening?", "Where do they eat dinner in the evening?", "أين يتناولون العشاء في المساء؟",
                    "They eat dinner on the balcony.", "balcony"),
                retell("en_st_new_flat_o2", L, "A family has a new flat near the park. It has two bedrooms and a small kitchen. They love their new home.",
                    "flat|home|apartment", "park", "bedroom|room|kitchen"),
            )),
            story("en_st_cooking_class", L, A2, "The cooking class", "The cooking class", "درس الطبخ", listOf(
                l("Every Tuesday evening, Maya goes to a cooking class.", "Every Tuesday evening, Maya goes to a cooking class.", "كل مساء ثلاثاء تذهب مايا إلى درس طبخ."),
                l("There are eight students and one teacher, Chef Daniel.", "There are eight students and one teacher, Chef Daniel.", "هناك ثمانية طلاب ومعلّم واحد، الطاهي دانيال."),
                l("Last week they made vegetable soup and fresh bread.", "Last week they made vegetable soup and fresh bread.", "في الأسبوع الماضي صنعوا حساء خضار وخبزًا طازجًا."),
                l("Maya forgot to add salt, so her soup didn't taste very good.", "Maya forgot to add salt, so her soup didn't taste very good.", "نسيت مايا إضافة الملح، فلم يكن طعم حسائها جيدًا."),
                l("Chef Daniel laughed and said, \"Everyone makes mistakes.\"", "Chef Daniel laughed and said, \"Everyone makes mistakes.\"", "ضحك الطاهي دانيال وقال: «الجميع يخطئون»."),
                l("Her bread, however, was the best in the class.", "Her bread, however, was the best in the class.", "لكن خبزها كان الأفضل في الصف."),
                l("She took it home and shared it with her parents.", "She took it home and shared it with her parents.", "أخذته إلى البيت وتقاسمته مع والديها."),
                l("Next week they are going to cook fish.", "Next week they are going to cook fish.", "في الأسبوع القادم سيطبخون السمك."),
            ), listOf(
                w("Tuesday", "Tuesday", "الثلاثاء"), w("cooking class", "cooking class", "درس طبخ"), w("students", "students", "طلاب"), w("teacher", "teacher", "معلّم"),
                w("Chef", "chef, cook", "طاهٍ"), w("vegetable", "vegetable", "خضار"), w("soup", "soup", "حساء"), w("fresh", "fresh", "طازج"),
                w("forgot", "forgot (past of forget)", "نسي"), w("salt", "salt", "ملح"), w("taste", "taste", "يكون طعمه"), w("laughed", "laughed", "ضحك"),
                w("mistakes", "mistakes", "أخطاء"), w("however", "however", "لكن، ومع ذلك"), w("the best", "the best", "الأفضل"), w("shared", "shared", "تقاسم"),
                w("parents", "parents", "الوالدان"), w("fish", "fish", "سمك"), w("Next week", "next week", "الأسبوع القادم"),
            ), listOf(
                q("en_st_cooking_class_q1", "When does Maya go to the class?", listOf("On Tuesday evenings", "On Saturday mornings", "Every day"), 0),
                q("en_st_cooking_class_q2", "What was wrong with her soup?", listOf("It was cold", "It had no salt", "It was too spicy"), 1),
                q("en_st_cooking_class_q3", "What are they going to cook next week?", listOf("Soup", "Bread", "Fish"), 2),
            ), listOf(
                o("en_st_cooking_class_o1", "Why didn't Maya's soup taste good?", "Why didn't Maya's soup taste good?", "لماذا لم يكن طعم حساء مايا جيدًا؟",
                    "Because she forgot to add salt.", "forgot|forget|no", "salt"),
                retell("en_st_cooking_class_o2", L, "Maya goes to a cooking class every Tuesday. Her soup was not good because she forgot the salt, but her bread was the best.",
                    "cooking|cook|class", "soup", "salt", "bread"),
            )),
            story("en_st_broken_phone", L, A2, "The broken phone", "The broken phone", "الهاتف المكسور", listOf(
                l("On Monday morning, Leo dropped his phone and the screen broke.", "On Monday morning, Leo dropped his phone and the screen broke.", "صباح يوم الاثنين أوقع ليو هاتفه فانكسرت الشاشة."),
                l("The repair shop said it would take a week to fix.", "The repair shop said it would take a week to fix.", "قال محل التصليح إن إصلاحه سيستغرق أسبوعًا."),
                l("At first, Leo felt bored and a little lost.", "At first, Leo felt bored and a little lost.", "في البداية شعر ليو بالملل وبشيء من الضياع."),
                l("He couldn't check the news or send messages to his friends.", "He couldn't check the news or send messages to his friends.", "لم يستطع متابعة الأخبار ولا إرسال الرسائل إلى أصدقائه."),
                l("So he started reading a book that he bought last year.", "So he started reading a book that he bought last year.", "فبدأ يقرأ كتابًا اشتراه في العام الماضي."),
                l("In the evenings, he played chess with his grandfather.", "In the evenings, he played chess with his grandfather.", "وفي المساء كان يلعب الشطرنج مع جدّه."),
                l("He also slept better than before.", "He also slept better than before.", "كما نام أفضل من قبل."),
                l("When he got his phone back, he decided to use it less.", "When he got his phone back, he decided to use it less.", "وعندما استعاد هاتفه قرّر أن يستخدمه أقل."),
            ), listOf(
                w("dropped", "dropped", "أوقع"), w("screen", "screen", "شاشة"), w("broke", "broke (past of break)", "انكسر"), w("repair shop", "repair shop", "محل تصليح"),
                w("fix", "fix, repair", "يُصلح"), w("At first", "at first", "في البداية"), w("bored", "bored", "يشعر بالملل"), w("lost", "lost", "ضائع"),
                w("check", "check", "يتفقّد"), w("news", "news", "أخبار"), w("send", "send", "يرسل"), w("messages", "messages", "رسائل"),
                w("started", "started", "بدأ"), w("bought", "bought (past of buy)", "اشترى"), w("chess", "chess", "شطرنج"), w("grandfather", "grandfather", "جدّ"),
                w("slept", "slept (past of sleep)", "نام"), w("better than before", "better than before", "أفضل من قبل"), w("decided", "decided", "قرّر"), w("less", "less", "أقل"),
            ), listOf(
                q("en_st_broken_phone_q1", "What happened to Leo's phone?", listOf("The screen broke", "He lost it", "The battery died"), 0),
                q("en_st_broken_phone_q2", "Who did he play chess with?", listOf("His friend", "His grandfather", "His teacher"), 1),
                q("en_st_broken_phone_q3", "What did he decide at the end?", listOf("To buy a new phone", "To use his phone less", "To stop reading"), 1),
            ), listOf(
                o("en_st_broken_phone_o1", "What did Leo do when he had no phone? Write two things.", "What did Leo do when he had no phone? Write two things.",
                    "ماذا فعل ليو عندما لم يكن معه هاتف؟ اذكر أمرين.", "He read a book and played chess with his grandfather.", "read|book", "chess|grandfather|slept|sleep"),
                retell("en_st_broken_phone_o2", L, "Leo broke his phone and could not use it for a week. He read a book, played chess and slept better. Now he wants to use his phone less.",
                    "phone", "week", "book|read|chess", "less"),
            )),
            story("en_st_garden", L, B1, "The community garden", "The community garden", "حديقة الحي", listOf(
                l("Two years ago, the empty land behind our building was full of rubbish.", "Two years ago, the empty land behind our building was full of rubbish.", "قبل عامين كانت الأرض الفارغة خلف مبنانا مليئة بالقمامة."),
                l("One neighbour, Mrs Patel, suggested turning it into a garden.", "One neighbour, Mrs Patel, suggested turning it into a garden.", "اقترحت إحدى الجارات، السيدة باتيل، تحويلها إلى حديقة."),
                l("Not everyone liked the idea, because they thought it would be too much work.", "Not everyone liked the idea, because they thought it would be too much work.", "لم تعجب الفكرة الجميع، لأنهم ظنّوا أنها ستتطلب عملًا كثيرًا."),
                l("However, about fifteen people agreed to help on Sunday mornings.", "However, about fifteen people agreed to help on Sunday mornings.", "ومع ذلك وافق نحو خمسة عشر شخصًا على المساعدة صباح أيام الأحد."),
                l("They cleaned the land, built wooden boxes and planted tomatoes, beans and herbs.", "They cleaned the land, built wooden boxes and planted tomatoes, beans and herbs.", "نظّفوا الأرض وبنوا صناديق خشبية وزرعوا الطماطم والفاصولياء والأعشاب."),
                l("Neighbours who had never spoken before began to share tools and recipes.", "Neighbours who had never spoken before began to share tools and recipes.", "بدأ جيران لم يتحدثوا من قبل يتبادلون الأدوات ووصفات الطعام."),
                l("Last summer the garden produced so many vegetables that they gave some to a local school.", "Last summer the garden produced so many vegetables that they gave some to a local school.", "في الصيف الماضي أنتجت الحديقة خضارًا كثيرة حتى إنهم أعطوا بعضها لمدرسة قريبة."),
                l("Now even the people who were against the idea come to sit there in the evenings.", "Now even the people who were against the idea come to sit there in the evenings.", "والآن حتى الذين عارضوا الفكرة يأتون للجلوس هناك في المساء."),
            ), listOf(
                w("empty", "empty", "فارغ"), w("land", "land", "أرض"), w("behind", "behind", "خلف"), w("full of", "full of", "مليء بـ"), w("rubbish", "rubbish", "قمامة"),
                w("neighbour", "neighbour", "جار"), w("suggested", "suggested", "اقترح"), w("turning it into", "turning it into", "تحويلها إلى"), w("idea", "idea", "فكرة"),
                w("However", "however", "ومع ذلك"), w("agreed", "agreed", "وافق"), w("cleaned", "cleaned", "نظّف"), w("built", "built (past of build)", "بنى"),
                w("wooden", "wooden", "خشبي"), w("planted", "planted", "زرع"), w("beans", "beans", "فاصولياء"), w("herbs", "herbs", "أعشاب"),
                w("had never spoken", "had never spoken", "لم يتحدثوا قط"), w("share", "share", "يتشارك"), w("tools", "tools", "أدوات"), w("recipes", "recipes", "وصفات طعام"),
                w("produced", "produced", "أنتج"), w("local", "local", "محلي، قريب"), w("against", "against", "ضد، معارض"),
            ), listOf(
                q("en_st_garden_q1", "What was on the land before?", listOf("A car park", "Rubbish", "An old school"), 1),
                q("en_st_garden_q2", "Why did some people dislike the idea?", listOf("They thought it would be too much work", "They wanted a car park", "They didn't like vegetables"), 0),
                q("en_st_garden_q3", "What did they do with some of the vegetables?", listOf("They sold them", "They gave them to a local school", "They threw them away"), 1),
            ), listOf(
                o("en_st_garden_o1", "How did the garden change the neighbours?", "How did the garden change the neighbours?", "كيف غيّرت الحديقة الجيران؟",
                    "Neighbours who had never spoken before started to talk and share tools and recipes.", "neighbour|people", "share|talk|spoke|speak|friend"),
                retell("en_st_garden_o2", L, "The neighbours turned an empty piece of land full of rubbish into a garden. They grew vegetables, got to know each other and gave food to a school.",
                    "land|rubbish", "garden", "neighbour|people", "vegetable|tomato|grew|planted"),
            )),
            story("en_st_working_home", L, B2, "Working from home", "Working from home", "العمل من البيت", listOf(
                l("When Hana's company allowed staff to work from home three days a week, she was delighted.", "When Hana's company allowed staff to work from home three days a week, she was delighted.", "عندما سمحت شركة هناء للموظفين بالعمل من البيت ثلاثة أيام في الأسبوع، سعدت كثيرًا."),
                l("She would no longer have to spend almost two hours a day commuting.", "She would no longer have to spend almost two hours a day commuting.", "لن تضطر بعد الآن إلى قضاء ساعتين تقريبًا كل يوم في التنقّل إلى العمل."),
                l("For the first few weeks, she got far more done than she had in the office.", "For the first few weeks, she got far more done than she had in the office.", "في الأسابيع الأولى أنجزت أكثر بكثير مما كانت تنجزه في المكتب."),
                l("Gradually, though, the line between work and free time began to disappear.", "Gradually, though, the line between work and free time began to disappear.", "لكن الحدّ بين العمل ووقت الفراغ بدأ يختفي تدريجيًا."),
                l("She found herself answering emails late at night and skipping lunch.", "She found herself answering emails late at night and skipping lunch.", "وجدت نفسها تردّ على الرسائل الإلكترونية في وقت متأخر من الليل وتتخطّى الغداء."),
                l("She also missed the casual conversations with colleagues that had often sparked new ideas.", "She also missed the casual conversations with colleagues that had often sparked new ideas.", "كما افتقدت الأحاديث العفوية مع زملائها التي كثيرًا ما كانت تولّد أفكارًا جديدة."),
                l("Eventually, she set herself clear rules: a fixed finishing time, a proper lunch break and a walk after work.", "Eventually, she set herself clear rules: a fixed finishing time, a proper lunch break and a walk after work.", "وفي النهاية وضعت لنفسها قواعد واضحة: موعد ثابت لانتهاء العمل، واستراحة غداء حقيقية، ونزهة مشيًا بعد العمل."),
                l("She now believes that working from home is neither a dream nor a trap; it depends on how you organise it.", "She now believes that working from home is neither a dream nor a trap; it depends on how you organise it.", "وهي ترى الآن أن العمل من البيت ليس حلمًا ولا فخًّا، بل يتوقف على طريقة تنظيمه."),
            ), listOf(
                w("allowed", "allowed", "سمح"), w("staff", "staff, employees", "الموظفون"), w("delighted", "delighted", "مسرور جدًا"), w("no longer", "no longer", "لم يعد"),
                w("commuting", "travelling to and from work", "التنقّل اليومي إلى العمل"), w("got far more done", "got much more work finished", "أنجزت أكثر بكثير"),
                w("Gradually", "gradually", "تدريجيًا"), w("though", "though, however", "لكن"), w("the line between", "the line between", "الحدّ الفاصل بين"),
                w("disappear", "disappear", "يختفي"), w("found herself", "found herself (doing)", "وجدت نفسها"), w("skipping", "skipping, missing out", "تتخطّى"),
                w("missed", "missed (felt sad without)", "افتقدت"), w("casual", "casual, informal", "عفوي"), w("colleagues", "colleagues", "زملاء"),
                w("sparked", "sparked, started", "أطلق، ولّد"), w("Eventually", "eventually, in the end", "في النهاية"), w("set herself", "set herself", "وضعت لنفسها"),
                w("fixed", "fixed", "ثابت"), w("proper", "proper, real", "حقيقي، لائق"), w("neither", "neither", "لا (هذا)"), w("nor", "nor", "ولا"),
                w("trap", "trap", "فخ"), w("depends on", "depends on", "يتوقف على"), w("organise", "organise", "ينظّم"),
            ), listOf(
                q("en_st_working_home_q1", "Why was Hana happy at first?", listOf("She would save time commuting", "She got a pay rise", "She could move house"), 0),
                q("en_st_working_home_q2", "What problem appeared later?", listOf("Her internet was slow", "Work began to take over her free time", "Her manager complained"), 1),
                q("en_st_working_home_q3", "What does Hana think now?", listOf("Working from home is always better", "It depends on how you organise it", "Offices are always better"), 1),
            ), listOf(
                o("en_st_working_home_o1", "What rules did Hana set for herself, and why?", "What rules did Hana set for herself, and why?", "ما القواعد التي وضعتها هناء لنفسها، ولماذا؟",
                    "She set a fixed finishing time, a proper lunch break and a walk after work, because work was taking over her evenings and free time.",
                    "finish|stop|time", "lunch|break", "walk", "free|night|evening|late"),
                retell("en_st_working_home_o2", L, "Hana was happy to work from home because she saved time. Later, work took over her evenings and she missed her colleagues, so she made clear rules.",
                    "home", "commut|time|travel", "colleague|lonely|missed|evening|night", "rule"),
            )),
            story("en_st_painter", L, B2, "The forgotten painter", "The forgotten painter", "الرسامة المنسيّة", listOf(
                l("For decades, a small painting hung unnoticed in the corridor of a village library.", "For decades, a small painting hung unnoticed in the corridor of a village library.", "لعقود عُلّقت لوحة صغيرة في ممرّ مكتبة قرية دون أن يلاحظها أحد."),
                l("Last spring, an art student called Tom stopped to look at it more closely.", "Last spring, an art student called Tom stopped to look at it more closely.", "في الربيع الماضي توقف طالب فنون اسمه توم لينظر إليها عن قرب."),
                l("He noticed a faint signature in the corner that he did not recognise.", "He noticed a faint signature in the corner that he did not recognise.", "لاحظ في الزاوية توقيعًا باهتًا لم يتعرّف عليه."),
                l("Curious, he spent several weekends searching old newspapers and letters in the local archive.", "Curious, he spent several weekends searching old newspapers and letters in the local archive.", "وبدافع الفضول أمضى عدة عطلات أسبوعية يبحث في الصحف والرسائل القديمة في الأرشيف المحلي."),
                l("It turned out that the painter was a woman who had lived in the village a century ago.", "It turned out that the painter was a woman who had lived in the village a century ago.", "وتبيّن أن الرسامة امرأة عاشت في القرية قبل قرن."),
                l("Her work had been ignored, partly because women artists were rarely taken seriously at the time.", "Her work had been ignored, partly because women artists were rarely taken seriously at the time.", "كانت أعمالها قد تُجوهلت، ويعود ذلك جزئيًا إلى أن الفنانات نادرًا ما كنّ يُؤخذن على محمل الجد في ذلك الوقت."),
                l("Tom's discovery led to an exhibition of her paintings, many of which had been kept in attics by local families.", "Tom's discovery led to an exhibition of her paintings, many of which had been kept in attics by local families.", "أدّى اكتشاف توم إلى معرض للوحاتها، وكان كثير منها محفوظًا في علّيات بيوت عائلات المنطقة."),
                l("Thousands of visitors came, and the painting from the corridor now hangs in the city museum.", "Thousands of visitors came, and the painting from the corridor now hangs in the city museum.", "جاء آلاف الزوار، وصارت اللوحة التي كانت في الممرّ معلّقة الآن في متحف المدينة."),
            ), listOf(
                w("decades", "decades", "عقود"), w("painting", "painting", "لوحة"), w("hung", "hung (past of hang)", "عُلّق"), w("unnoticed", "unnoticed", "دون أن يلاحظه أحد"),
                w("corridor", "corridor", "ممرّ"), w("closely", "closely", "عن قرب"), w("noticed", "noticed", "لاحظ"), w("faint", "faint", "باهت"),
                w("signature", "signature", "توقيع"), w("recognise", "recognise", "يتعرّف على"), w("Curious", "curious", "فضولي"), w("archive", "archive", "أرشيف"),
                w("It turned out", "it turned out", "تبيّن أن"), w("century", "century", "قرن"), w("ignored", "ignored", "تُجوهل"), w("partly", "partly", "جزئيًا"),
                w("rarely", "rarely", "نادرًا"), w("taken seriously", "taken seriously", "يُؤخذ على محمل الجد"), w("discovery", "discovery", "اكتشاف"),
                w("led to", "led to", "أدّى إلى"), w("exhibition", "exhibition", "معرض"), w("attics", "attics", "علّيات"), w("visitors", "visitors", "زوار"), w("museum", "museum", "متحف"),
            ), listOf(
                q("en_st_painter_q1", "What did Tom notice on the painting?", listOf("A date", "A faint signature", "A hidden message"), 1),
                q("en_st_painter_q2", "Why had the painter been ignored?", listOf("Her paintings were too small", "Women artists were rarely taken seriously", "She never showed her work"), 1),
                q("en_st_painter_q3", "Where is the painting now?", listOf("In the library", "In the city museum", "In Tom's house"), 1),
            ), listOf(
                o("en_st_painter_o1", "How did Tom find out who the painter was?", "How did Tom find out who the painter was?", "كيف عرف توم من هي الرسامة؟",
                    "He searched old newspapers and letters in the local archive.", "newspaper|letter|archive", "search|look|read"),
                retell("en_st_painter_o2", L, "An art student noticed a signature on an old painting in a library. He discovered it was by a forgotten woman painter, and her work was shown in an exhibition.",
                    "painting", "signature|student", "woman|painter|artist", "exhibition|museum"),
            )),
            story("en_st_car_free", L, B2, "A town without cars?", "A town without cars?", "بلدة بلا سيارات؟", listOf(
                l("Last month our town council proposed banning cars from the old centre at weekends.", "Last month our town council proposed banning cars from the old centre at weekends.", "في الشهر الماضي اقترح مجلس بلدتنا منع السيارات من وسط البلدة القديم في عطلات نهاية الأسبوع."),
                l("Supporters argue that the streets would become safer, quieter and less polluted.", "Supporters argue that the streets would become safer, quieter and less polluted.", "يقول المؤيدون إن الشوارع ستصبح أكثر أمانًا وهدوءًا وأقل تلوثًا."),
                l("Families could walk freely, and cafés could put tables outside.", "Families could walk freely, and cafés could put tables outside.", "وستتمكن العائلات من المشي بحرية، وستتمكن المقاهي من وضع طاولات في الخارج."),
                l("Shop owners, on the other hand, are worried that customers will simply drive to the shopping centre outside town.", "Shop owners, on the other hand, are worried that customers will simply drive to the shopping centre outside town.", "أما أصحاب المتاجر فيخشون أن يقود الزبائن ببساطة إلى المركز التجاري خارج البلدة."),
                l("Elderly residents and people with disabilities have also raised concerns about how they would get around.", "Elderly residents and people with disabilities have also raised concerns about how they would get around.", "كما أبدى السكان المسنّون وذوو الإعاقة قلقهم بشأن كيفية تنقّلهم."),
                l("To address these worries, the council has suggested a free electric bus and more parking at the edge of the centre.", "To address these worries, the council has suggested a free electric bus and more parking at the edge of the centre.", "ولمعالجة هذه المخاوف اقترح المجلس حافلة كهربائية مجانية ومزيدًا من المواقف عند أطراف الوسط."),
                l("A three-month trial will begin in the spring, after which residents will vote.", "A three-month trial will begin in the spring, after which residents will vote.", "وستبدأ في الربيع تجربة مدتها ثلاثة أشهر، يصوّت السكان بعدها."),
                l("Whatever the result, the debate has got people talking about what kind of town they want to live in.", "Whatever the result, the debate has got people talking about what kind of town they want to live in.", "وأيًّا كانت النتيجة، فقد جعل هذا النقاش الناس يتحدثون عن نوع البلدة التي يريدون العيش فيها."),
            ), listOf(
                w("town council", "town council", "مجلس البلدة"), w("proposed", "proposed", "اقترح"), w("banning", "banning", "منع"), w("centre", "centre", "وسط"),
                w("Supporters", "supporters", "المؤيدون"), w("argue", "argue, claim", "يحتجّون، يقولون"), w("safer", "safer", "أكثر أمانًا"), w("polluted", "polluted", "ملوّث"),
                w("freely", "freely", "بحرية"), w("Shop owners", "shop owners", "أصحاب المتاجر"), w("on the other hand", "on the other hand", "في المقابل"),
                w("customers", "customers", "زبائن"), w("shopping centre", "shopping centre", "مركز تجاري"), w("Elderly", "elderly", "مسنّ"), w("residents", "residents", "سكان"),
                w("disabilities", "disabilities", "إعاقات"), w("raised concerns", "raised concerns", "أبدوا قلقهم"), w("get around", "get around, travel about", "يتنقّل"),
                w("address", "address, deal with", "يعالج"), w("electric", "electric", "كهربائي"), w("parking", "parking", "مواقف السيارات"), w("edge", "edge", "طرف، حافة"),
                w("trial", "trial", "تجربة"), w("vote", "vote", "يصوّت"), w("Whatever", "whatever", "أيًّا كان"), w("debate", "debate", "نقاش"),
            ), listOf(
                q("en_st_car_free_q1", "What did the council propose?", listOf("Banning cars from the centre at weekends", "Building a new shopping centre", "Closing the cafés"), 0),
                q("en_st_car_free_q2", "Why are shop owners worried?", listOf("Rents will go up", "Customers may go to the shopping centre instead", "The buses are too slow"), 1),
                q("en_st_car_free_q3", "What will happen in the spring?", listOf("An immediate vote", "New shops will open", "A three-month trial"), 2),
            ), listOf(
                o("en_st_car_free_o1", "What has the council suggested for people who cannot walk far?", "What has the council suggested for people who cannot walk far?",
                    "ماذا اقترح المجلس لمن لا يستطيعون المشي مسافات طويلة؟", "A free electric bus and more parking at the edge of the centre.", "bus", "parking|park"),
                retell("en_st_car_free_o2", L, "The council wants to ban cars from the town centre at weekends. Some people think it will be safer and cleaner, but shop owners and older residents are worried. There will be a trial and then a vote.",
                    "car", "centre|center|town", "worried|worry|concern|against|shop", "trial|vote"),
            )),
        )
    }

    private val mandarin by lazy {
        val L = LanguageCode.MANDARIN
        listOf(
            story("zh_st_wodejia", L, A1, "我的家", "My family", "عائلتي", listOf(
                l("我家有四口人。", "There are four people in my family.", "في عائلتي أربعة أشخاص."),
                l("爸爸、妈妈、妹妹和我。", "Dad, Mum, my younger sister and me.", "أبي وأمي وأختي الصغرى وأنا."),
                l("爸爸是医生，妈妈是老师。", "Dad is a doctor and Mum is a teacher.", "أبي طبيب وأمي معلّمة."),
                l("妹妹六岁，她很可爱。", "My younger sister is six. She is very cute.", "أختي الصغرى عمرها ست سنوات، وهي لطيفة جدًا."),
                l("我们家有一只小猫。", "We have a little cat.", "عندنا قطة صغيرة."),
                l("晚上我们一起吃饭、看电视。", "In the evening we eat and watch TV together.", "في المساء نأكل ونشاهد التلفاز معًا."),
                l("我很爱我的家。", "I love my family very much.", "أحب عائلتي كثيرًا."),
            ), listOf(
                w("家", "home; family", "بيت؛ عائلة", "jiā"), w("口", "(measure word for family members)", "(كلمة عدّ لأفراد العائلة)", "kǒu"), w("人", "person, people", "شخص، ناس", "rén"),
                w("爸爸", "dad", "أب", "bàba"), w("妈妈", "mum", "أم", "māma"), w("妹妹", "younger sister", "الأخت الصغرى", "mèimei"), w("医生", "doctor", "طبيب", "yīshēng"),
                w("老师", "teacher", "معلّم", "lǎoshī"), w("岁", "years old", "سنة (من العمر)", "suì"), w("可爱", "cute, lovely", "لطيف، محبوب", "kě'ài"),
                w("只", "(measure word for animals)", "(كلمة عدّ للحيوانات)", "zhī"), w("小猫", "kitten, little cat", "قطة صغيرة", "xiǎo māo"), w("晚上", "evening", "مساء", "wǎnshang"),
                w("一起", "together", "معًا", "yìqǐ"), w("吃饭", "to eat (a meal)", "يأكل (وجبة)", "chī fàn"), w("看电视", "to watch TV", "يشاهد التلفاز", "kàn diànshì"), w("爱", "to love", "يحب", "ài"),
            ), listOf(
                q("zh_st_wodejia_q1", "他家有几口人？", listOf("三口", "四口", "五口"), 1, g("How many people are in his family?", "كم شخصًا في عائلته؟"),
                    listOf(g("three", "ثلاثة"), g("four", "أربعة"), g("five", "خمسة"))),
                q("zh_st_wodejia_q2", "妈妈做什么工作？", listOf("医生", "老师", "学生"), 1, g("What does Mum do?", "ما عمل الأم؟"),
                    listOf(g("doctor", "طبيبة"), g("teacher", "معلّمة"), g("student", "طالبة"))),
                q("zh_st_wodejia_q3", "晚上他们做什么？", listOf("看电视", "去商店", "去学校"), 0, g("What do they do in the evening?", "ماذا يفعلون في المساء؟"),
                    listOf(g("watch TV", "يشاهدون التلفاز"), g("go to the shop", "يذهبون إلى المتجر"), g("go to school", "يذهبون إلى المدرسة"))),
            ), listOf(
                o("zh_st_wodejia_o1", "妹妹几岁？", "How old is the younger sister?", "كم عمر الأخت الصغرى؟", "她六岁。", "六"),
                retell("zh_st_wodejia_o2", L, "我家有四口人。爸爸是医生，妈妈是老师。我们晚上一起吃饭、看电视。", "四", "医生|老师", "吃饭|电视"),
            )),
            story("zh_st_shengri", L, A2, "朋友的生日", "A friend's birthday", "عيد ميلاد صديق", listOf(
                l("上个星期六是我朋友小王的生日。", "Last Saturday was my friend Xiao Wang's birthday.", "كان السبت الماضي عيد ميلاد صديقي شياو وانغ."),
                l("我想送他一个礼物，可是不知道买什么。", "I wanted to give him a present, but I didn't know what to buy.", "أردت أن أهديه هدية، لكنني لم أعرف ماذا أشتري."),
                l("后来我想起来他很喜欢看书。", "Then I remembered that he really likes reading.", "ثم تذكّرت أنه يحب القراءة كثيرًا."),
                l("所以我去书店给他买了一本小说。", "So I went to a bookshop and bought him a novel.", "فذهبت إلى محل الكتب واشتريت له رواية."),
                l("晚上我们在一家饭馆吃饭，来了十几个朋友。", "In the evening we ate at a restaurant, and more than ten friends came.", "في المساء أكلنا في مطعم، وحضر أكثر من عشرة أصدقاء."),
                l("大家一起唱歌，还吃了一个很大的蛋糕。", "Everyone sang together, and we also ate a very big cake.", "غنّى الجميع معًا، وأكلنا أيضًا كعكة كبيرة جدًا."),
                l("小王看到我的礼物，非常高兴。", "When Xiao Wang saw my present, he was very happy.", "عندما رأى شياو وانغ هديتي فرح كثيرًا."),
                l("他说这是他最喜欢的作家写的书。", "He said it was a book by his favourite writer.", "قال إنه كتاب لكاتبه المفضّل."),
            ), listOf(
                w("上个星期六", "last Saturday", "السبت الماضي", "shàng ge xīngqīliù"), w("生日", "birthday", "عيد ميلاد", "shēngrì"), w("送", "to give (as a present)", "يهدي", "sòng"),
                w("礼物", "present, gift", "هدية", "lǐwù"), w("可是", "but", "لكن", "kěshì"), w("知道", "to know", "يعرف", "zhīdào"), w("后来", "later, afterwards", "بعد ذلك", "hòulái"),
                w("想起来", "to remember, to recall", "يتذكّر", "xiǎng qǐlai"), w("看书", "to read (books)", "يقرأ", "kàn shū"), w("所以", "so, therefore", "لذلك", "suǒyǐ"),
                w("书店", "bookshop", "محل كتب", "shūdiàn"), w("本", "(measure word for books)", "(كلمة عدّ للكتب)", "běn"), w("小说", "novel", "رواية", "xiǎoshuō"),
                w("饭馆", "restaurant", "مطعم", "fànguǎn"), w("十几个", "ten-odd, more than ten", "بضعة عشر", "shí jǐ ge"), w("大家", "everyone", "الجميع", "dàjiā"),
                w("唱歌", "to sing", "يغنّي", "chàng gē"), w("蛋糕", "cake", "كعكة", "dàngāo"), w("非常", "very, extremely", "جدًا", "fēicháng"), w("高兴", "happy", "سعيد", "gāoxìng"),
                w("最", "most", "الأكثر", "zuì"), w("作家", "writer", "كاتب", "zuòjiā"),
            ), listOf(
                q("zh_st_shengri_q1", "我送了小王什么？", listOf("一本小说", "一个蛋糕", "一件衣服"), 0, g("What did I give Xiao Wang?", "ماذا أهديت شياو وانغ؟"),
                    listOf(g("a novel", "رواية"), g("a cake", "كعكة"), g("a piece of clothing", "قطعة ملابس"))),
                q("zh_st_shengri_q2", "他们在哪儿吃饭？", listOf("在家", "在饭馆", "在学校"), 1, g("Where did they eat?", "أين أكلوا؟"),
                    listOf(g("at home", "في البيت"), g("at a restaurant", "في مطعم"), g("at school", "في المدرسة"))),
                q("zh_st_shengri_q3", "来了多少朋友？", listOf("两三个", "十几个", "一百个"), 1, g("How many friends came?", "كم صديقًا حضر؟"),
                    listOf(g("two or three", "اثنان أو ثلاثة"), g("more than ten", "أكثر من عشرة"), g("a hundred", "مئة"))),
            ), listOf(
                o("zh_st_shengri_o1", "为什么我给小王买书？", "Why did I buy Xiao Wang a book?", "لماذا اشتريت لشياو وانغ كتابًا؟", "因为他很喜欢看书。", "喜欢", "书"),
                retell("zh_st_shengri_o2", L, "上个星期六是小王的生日。我送了他一本小说。大家在饭馆吃饭、唱歌，小王很高兴。",
                    "生日", "小说|书|礼物", "饭馆|吃饭|唱歌|蛋糕", "高兴"),
            )),
            story("zh_st_xiayu", L, A2, "下雨天", "A rainy day", "يوم ممطر", listOf(
                l("今天早上我起床的时候，外面在下大雨。", "When I got up this morning, it was raining heavily outside.", "هذا الصباح عندما استيقظت كانت السماء تمطر بغزارة."),
                l("我忘了带雨伞，只好跑到公共汽车站。", "I forgot to bring an umbrella, so I had to run to the bus stop.", "نسيت أن آخذ مظلة، فاضطررت إلى الركض إلى موقف الحافلات."),
                l("等了二十分钟，车还没来。", "I waited twenty minutes and the bus still hadn't come.", "انتظرت عشرين دقيقة ولم تأتِ الحافلة بعد."),
                l("一位老奶奶看见我全身都湿了，就把她的伞借给我。", "An old lady saw that I was wet all over, so she lent me her umbrella.", "رأتني امرأة عجوز مبلّلًا تمامًا، فأعارتني مظلتها."),
                l("她说她家就在附近，不需要伞。", "She said her home was nearby and she didn't need the umbrella.", "قالت إن بيتها قريب ولا تحتاج إلى المظلة."),
                l("我到公司的时候已经迟到了。", "When I got to the office, I was already late.", "عندما وصلت إلى الشركة كنت قد تأخرت."),
                l("下班以后，我去老奶奶家还伞，还给她买了一些水果。", "After work, I went to the old lady's home to return the umbrella, and I also bought her some fruit.", "بعد العمل ذهبت إلى بيت المرأة العجوز لأعيد المظلة، واشتريت لها أيضًا بعض الفاكهة."),
            ), listOf(
                w("起床", "to get up", "ينهض من النوم", "qǐ chuáng"), w("外面", "outside", "في الخارج", "wàimiàn"), w("下大雨", "to rain heavily", "تمطر بغزارة", "xià dà yǔ"),
                w("忘了", "forgot", "نسي", "wàng le"), w("带", "to bring, to take along", "يأخذ معه", "dài"), w("雨伞", "umbrella", "مظلة", "yǔsǎn"),
                w("只好", "to have no choice but to", "اضطر إلى", "zhǐhǎo"), w("公共汽车站", "bus stop", "موقف الحافلات", "gōnggòng qìchē zhàn"),
                w("分钟", "minute", "دقيقة", "fēnzhōng"), w("老奶奶", "old lady, granny", "امرأة عجوز، جدّة", "lǎo nǎinai"), w("全身", "the whole body", "الجسم كله", "quánshēn"),
                w("湿", "wet", "مبلّل", "shī"), w("借给", "to lend to", "يعير", "jiè gěi"), w("附近", "nearby", "قريب، في الجوار", "fùjìn"), w("需要", "to need", "يحتاج إلى", "xūyào"),
                w("公司", "company, office", "شركة", "gōngsī"), w("迟到", "to be late", "يتأخر", "chídào"), w("下班", "to finish work", "ينهي العمل", "xià bān"),
                w("以后", "after", "بعد", "yǐhòu"), w("还伞", "to return the umbrella", "يعيد المظلة", "huán sǎn"), w("水果", "fruit", "فاكهة", "shuǐguǒ"),
            ), listOf(
                q("zh_st_xiayu_q1", "我忘了带什么？", listOf("手机", "雨伞", "钱包"), 1, g("What did I forget to bring?", "ماذا نسيت أن آخذ معي؟"),
                    listOf(g("my phone", "الهاتف"), g("an umbrella", "مظلة"), g("my wallet", "المحفظة"))),
                q("zh_st_xiayu_q2", "谁帮了我？", listOf("一位老奶奶", "一个司机", "我的同事"), 0, g("Who helped me?", "من ساعدني؟"),
                    listOf(g("an old lady", "امرأة عجوز"), g("a driver", "سائق"), g("my colleague", "زميلي"))),
                q("zh_st_xiayu_q3", "下班以后我做了什么？", listOf("去看电影", "回家睡觉", "去还伞"), 2, g("What did I do after work?", "ماذا فعلت بعد العمل؟"),
                    listOf(g("went to see a film", "ذهبت لمشاهدة فيلم"), g("went home to sleep", "عدت إلى البيت لأنام"), g("went to return the umbrella", "ذهبت لأعيد المظلة"))),
            ), listOf(
                o("zh_st_xiayu_o1", "老奶奶为什么说她不需要伞？", "Why did the old lady say she didn't need the umbrella?", "لماذا قالت المرأة العجوز إنها لا تحتاج إلى المظلة؟",
                    "因为她家就在附近。", "附近|近"),
                retell("zh_st_xiayu_o2", L, "下大雨，我忘了带伞。一位老奶奶把伞借给我。下班以后我去还伞，还给她买了水果。", "雨", "伞", "老奶奶", "还|水果"),
            )),
            story("zh_st_banjia", L, B1, "搬到新城市", "Moving to a new city", "الانتقال إلى مدينة جديدة", listOf(
                l("去年我因为工作搬到了广州。", "Last year I moved to Guangzhou for work.", "في العام الماضي انتقلت إلى قوانغتشو بسبب العمل."),
                l("刚来的时候，我一个朋友也没有，周末常常一个人在家。", "When I first arrived, I didn't have a single friend, and at weekends I often stayed home alone.", "عندما وصلت أول مرة لم يكن لديّ صديق واحد، وكثيرًا ما بقيت وحدي في البيت في عطلة نهاية الأسبوع."),
                l("而且广东话我一句也听不懂。", "What's more, I couldn't understand a single sentence of Cantonese.", "فضلًا عن ذلك لم أكن أفهم جملة واحدة من الكانتونية."),
                l("后来一个同事建议我参加公司的篮球队。", "Later a colleague suggested that I join the company basketball team.", "ثم اقترح عليّ زميل أن أنضمّ إلى فريق كرة السلة في الشركة."),
                l("虽然我打得不太好，但是队员们都很热情。", "Although I didn't play very well, the team members were all very friendly.", "مع أنني لم ألعب جيدًا، كان أعضاء الفريق جميعًا ودودين جدًا."),
                l("每次练习以后，我们都一起去吃晚饭，聊工作和生活。", "After every practice we went to dinner together and talked about work and life.", "بعد كل تدريب كنا نذهب معًا لتناول العشاء ونتحدث عن العمل والحياة."),
                l("慢慢地，我不但交了很多朋友，而且越来越喜欢这个城市了。", "Little by little, I not only made many friends but also came to like this city more and more.", "شيئًا فشيئًا لم أكوّن صداقات كثيرة فحسب، بل صرت أحب هذه المدينة أكثر فأكثر."),
                l("现在如果有新同事来，我也会请他们一起打球。", "Now, when new colleagues come, I invite them to play with us too.", "والآن إذا جاء زملاء جدد أدعوهم أيضًا ليلعبوا معنا."),
            ), listOf(
                w("去年", "last year", "العام الماضي", "qùnián"), w("因为", "because (of)", "بسبب، لأن", "yīnwèi"), w("搬", "to move (house)", "ينتقل (في السكن)", "bān"),
                w("广州", "Guangzhou (a city)", "قوانغتشو (مدينة)", "Guǎngzhōu"), w("刚", "just, only just", "للتوّ", "gāng"), w("周末", "weekend", "عطلة نهاية الأسبوع", "zhōumò"),
                w("常常", "often", "كثيرًا ما", "chángcháng"), w("一个人", "alone, by oneself", "وحده", "yí ge rén"), w("而且", "moreover, and also", "فضلًا عن ذلك", "érqiě"),
                w("广东话", "Cantonese", "الكانتونية", "Guǎngdōnghuà"), w("听不懂", "cannot understand (by listening)", "لا يفهم ما يسمع", "tīng bu dǒng"),
                w("同事", "colleague", "زميل", "tóngshì"), w("建议", "to suggest", "يقترح", "jiànyì"), w("参加", "to join, to take part in", "ينضمّ إلى، يشارك في", "cānjiā"),
                w("篮球队", "basketball team", "فريق كرة السلة", "lánqiú duì"), w("虽然", "although", "مع أنّ", "suīrán"), w("但是", "but", "لكن", "dànshì"),
                w("队员", "team member", "عضو الفريق", "duìyuán"), w("热情", "warm, friendly", "ودود، متحمّس", "rèqíng"), w("练习", "practice", "تدريب", "liànxí"),
                w("聊", "to chat", "يدردش", "liáo"), w("生活", "life", "حياة", "shēnghuó"), w("慢慢地", "slowly, little by little", "شيئًا فشيئًا", "mànmàn de"),
                w("不但", "not only", "ليس فقط", "búdàn"), w("交了很多朋友", "made many friends", "كوّن صداقات كثيرة", "jiāo le hěn duō péngyou"),
                w("越来越", "more and more", "أكثر فأكثر", "yuè lái yuè"), w("城市", "city", "مدينة", "chéngshì"), w("如果", "if", "إذا", "rúguǒ"), w("请", "to invite", "يدعو", "qǐng"),
            ), listOf(
                q("zh_st_banjia_q1", "我为什么搬到广州？", listOf("因为工作", "因为学习", "因为家人"), 0, g("Why did I move to Guangzhou?", "لماذا انتقلت إلى قوانغتشو؟"),
                    listOf(g("for work", "بسبب العمل"), g("to study", "للدراسة"), g("because of my family", "بسبب عائلتي"))),
                q("zh_st_banjia_q2", "谁建议我参加篮球队？", listOf("我的老板", "我的邻居", "一个同事"), 2, g("Who suggested I join the basketball team?", "من اقترح عليّ الانضمام إلى فريق كرة السلة؟"),
                    listOf(g("my boss", "مديري"), g("my neighbour", "جاري"), g("a colleague", "زميل"))),
                q("zh_st_banjia_q3", "现在我觉得这个城市怎么样？", listOf("想离开", "越来越喜欢", "还是不喜欢"), 1, g("How do I feel about the city now?", "ما شعوري تجاه المدينة الآن؟"),
                    listOf(g("I want to leave", "أريد المغادرة"), g("I like it more and more", "أحبها أكثر فأكثر"), g("I still don't like it", "ما زلت لا أحبها"))),
            ), listOf(
                o("zh_st_banjia_o1", "参加篮球队以后，我的生活有什么变化？", "How did my life change after I joined the basketball team?", "كيف تغيّرت حياتي بعد انضمامي إلى فريق كرة السلة؟",
                    "我交了很多朋友，也越来越喜欢广州了。", "朋友", "喜欢"),
                retell("zh_st_banjia_o2", L, "我因为工作搬到广州，刚来的时候没有朋友。一个同事建议我参加篮球队，后来我交了很多朋友。", "广州|搬", "朋友", "篮球"),
            )),
            story("zh_st_wanggou", L, B1, "网上买东西", "Shopping online", "التسوق عبر الإنترنت", listOf(
                l("现在很多人喜欢在网上买东西，我妈妈也不例外。", "Nowadays many people like shopping online, and my mum is no exception.", "اليوم يحب كثير من الناس التسوق عبر الإنترنت، وأمي ليست استثناءً."),
                l("上个月她在网上看到一件很漂亮的大衣，价格比商店便宜一半。", "Last month she saw a very nice coat online for half the price it was in the shops.", "في الشهر الماضي رأت على الإنترنت معطفًا جميلًا جدًا بنصف سعره في المتاجر."),
                l("她马上就买了，三天以后快递就送到了。", "She bought it straight away, and three days later the courier delivered it.", "اشترته فورًا، وبعد ثلاثة أيام وصل مع شركة التوصيل."),
                l("可是打开一看，大衣的颜色和照片上的不一样，而且太小了。", "But when she opened it, the colour was different from the photo, and it was too small.", "لكنها عندما فتحته وجدت أن لونه يختلف عن الصورة، وأنه صغير جدًا أيضًا."),
                l("她给卖家打电话，卖家说可以退货，但是她要自己付邮费。", "She phoned the seller, who said she could return it but would have to pay the postage herself.", "اتصلت بالبائع، فقال إنها تستطيع إرجاعه لكن عليها أن تدفع أجرة البريد بنفسها."),
                l("妈妈觉得不太公平，就在网上写了一条评价。", "Mum felt this wasn't really fair, so she wrote a review online.", "شعرت أمي أن ذلك ليس عادلًا، فكتبت تقييمًا على الإنترنت."),
                l("没想到第二天卖家主动联系她，说邮费由他们来付。", "To her surprise, the next day the seller contacted her and said they would pay the postage.", "وعلى غير المتوقع اتصل بها البائع في اليوم التالي من تلقاء نفسه وقال إنهم سيدفعون أجرة البريد."),
                l("从那以后，妈妈买东西以前一定先看别人的评价。", "Since then, Mum always reads other people's reviews before she buys anything.", "ومنذ ذلك الحين تقرأ أمي دائمًا تقييمات الآخرين قبل أن تشتري أي شيء."),
            ), listOf(
                w("网上", "online", "على الإنترنت", "wǎngshàng"), w("不例外", "is no exception", "ليس استثناءً", "bú lìwài"), w("件", "(measure word for clothes)", "(كلمة عدّ للملابس)", "jiàn"),
                w("漂亮", "pretty, nice", "جميل", "piàoliang"), w("大衣", "coat", "معطف", "dàyī"), w("价格", "price", "سعر", "jiàgé"), w("便宜", "cheap", "رخيص", "piányi"),
                w("一半", "half", "نصف", "yíbàn"), w("马上", "at once", "فورًا", "mǎshàng"), w("快递", "courier, express delivery", "شركة التوصيل السريع", "kuàidì"),
                w("送到", "to deliver", "يوصل", "sòngdào"), w("打开", "to open", "يفتح", "dǎkāi"), w("颜色", "colour", "لون", "yánsè"), w("照片", "photo", "صورة", "zhàopiàn"),
                w("不一样", "different", "مختلف", "bù yíyàng"), w("卖家", "seller", "البائع", "màijiā"), w("退货", "to return goods", "يُرجع البضاعة", "tuì huò"),
                w("自己", "oneself", "بنفسه", "zìjǐ"), w("邮费", "postage", "أجرة البريد", "yóufèi"), w("公平", "fair", "عادل", "gōngpíng"), w("评价", "review", "تقييم", "píngjià"),
                w("没想到", "unexpectedly", "على غير المتوقع", "méi xiǎngdào"), w("主动", "on one's own initiative", "من تلقاء نفسه", "zhǔdòng"), w("联系", "to contact", "يتواصل مع", "liánxì"),
                w("从那以后", "since then", "منذ ذلك الحين", "cóng nà yǐhòu"), w("别人", "other people", "الآخرون", "biérén"),
            ), listOf(
                q("zh_st_wanggou_q1", "大衣有什么问题？", listOf("太贵了", "颜色不一样，而且太小", "送得太慢"), 1, g("What was wrong with the coat?", "ما مشكلة المعطف؟"),
                    listOf(g("it was too expensive", "كان غاليًا جدًا"), g("the colour was different and it was too small", "لونه مختلف وهو صغير جدًا"), g("it came too slowly", "وصل متأخرًا جدًا"))),
                q("zh_st_wanggou_q2", "卖家一开始说什么？", listOf("不能退货", "可以退货，但要自己付邮费", "马上寄一件新的"), 1, g("What did the seller say at first?", "ماذا قال البائع في البداية؟"),
                    listOf(g("it can't be returned", "لا يمكن إرجاعه"), g("it can be returned, but she pays the postage", "يمكن إرجاعه، لكن عليها دفع أجرة البريد"), g("they will send a new one at once", "سيرسلون واحدًا جديدًا فورًا"))),
                q("zh_st_wanggou_q3", "现在妈妈买东西以前做什么？", listOf("先看别人的评价", "先去商店看看", "先问朋友"), 0, g("What does Mum do now before buying?", "ماذا تفعل أمي الآن قبل أن تشتري؟"),
                    listOf(g("reads other people's reviews", "تقرأ تقييمات الآخرين"), g("goes to look in a shop", "تذهب لترى في المتجر"), g("asks friends", "تسأل صديقاتها"))),
            ), listOf(
                o("zh_st_wanggou_o1", "妈妈为什么在网上写评价？", "Why did Mum write a review online?", "لماذا كتبت أمي تقييمًا على الإنترنت؟",
                    "因为她觉得自己付邮费不公平。", "公平", "邮费|退货"),
                retell("zh_st_wanggou_o2", L, "妈妈在网上买了一件大衣，可是颜色不一样，也太小。她写了评价以后，卖家给她付了邮费。", "大衣|网上", "颜色|小", "评价", "邮费|退货"),
            )),
            story("zh_st_shouji", L, B2, "离不开手机", "Can't live without my phone", "لا أستغني عن هاتفي", listOf(
                l("据调查，很多年轻人每天看手机的时间超过六个小时。", "According to surveys, many young people spend more than six hours a day on their phones.", "وفقًا لاستطلاعات، يقضي كثير من الشباب أكثر من ست ساعات يوميًا على هواتفهم."),
                l("我曾经也是这样，一睁开眼睛就先看消息。", "I used to be like that too: as soon as I opened my eyes, I checked my messages.", "كنت كذلك أنا أيضًا: ما إن أفتح عينيّ حتى أتفقّد الرسائل."),
                l("后来我发现，自己越来越难集中注意力，看书看不了十分钟就想拿手机。", "Later I found it harder and harder to concentrate; I couldn't read for ten minutes without wanting to pick up my phone.", "ثم اكتشفت أنه صار يصعب عليّ التركيز أكثر فأكثر، فلا أستطيع القراءة عشر دقائق دون أن أرغب في الإمساك بالهاتف."),
                l("于是我决定做一个实验：一个月里，晚上九点以后不用手机。", "So I decided to try an experiment: for one month, no phone after nine in the evening.", "فقررت أن أجري تجربة: طوال شهر، لا هاتف بعد التاسعة مساءً."),
                l("头几天特别难受，总担心错过什么重要的事情。", "The first few days were really hard; I kept worrying that I would miss something important.", "كانت الأيام الأولى صعبة جدًا، وكنت أخشى دائمًا أن يفوتني أمر مهم."),
                l("但是一个星期以后，我睡得比以前好多了，早上也更有精神。", "But after a week I was sleeping much better than before and had more energy in the mornings.", "لكن بعد أسبوع صرت أنام أفضل بكثير من قبل، وصرت أكثر نشاطًا في الصباح."),
                l("事实上，那些“重要”的消息，第二天早上再看也完全来得及。", "In fact, those \"important\" messages could just as well wait until the next morning.", "في الحقيقة، تلك الرسائل «المهمة» كان يمكن قراءتها صباح اليوم التالي دون أي ضرر."),
                l("当然，手机本身没有错，关键在于我们怎么使用它。", "Of course, the phone itself is not to blame; the key is how we use it.", "طبعًا، الهاتف نفسه ليس هو المذنب، فالمهم هو كيف نستخدمه."),
            ), listOf(
                w("据调查", "according to surveys", "وفقًا للاستطلاعات", "jù diàochá"), w("年轻人", "young people", "الشباب", "niánqīngrén"), w("超过", "to exceed, more than", "يتجاوز", "chāoguò"),
                w("小时", "hour", "ساعة", "xiǎoshí"), w("曾经", "once, used to", "سبق أن، كان في الماضي", "céngjīng"), w("睁开眼睛", "to open one's eyes", "يفتح عينيه", "zhēngkāi yǎnjing"),
                w("消息", "message, news", "رسالة، خبر", "xiāoxi"), w("发现", "to discover, to find", "يكتشف", "fāxiàn"), w("集中注意力", "to concentrate", "يركّز انتباهه", "jízhōng zhùyìlì"),
                w("于是", "so, thereupon", "فـ، وعندئذ", "yúshì"), w("决定", "to decide", "يقرّر", "juédìng"), w("实验", "experiment", "تجربة", "shíyàn"),
                w("头几天", "the first few days", "الأيام الأولى", "tóu jǐ tiān"), w("难受", "hard to bear, uncomfortable", "مزعج، صعب الاحتمال", "nánshòu"),
                w("担心", "to worry", "يقلق", "dānxīn"), w("错过", "to miss (a chance)", "يفوته", "cuòguò"), w("重要", "important", "مهم", "zhòngyào"),
                w("精神", "energy, vigour", "نشاط، حيوية", "jīngshen"), w("事实上", "in fact", "في الحقيقة", "shìshí shang"), w("来得及", "there is still time", "لم يفت الأوان", "láidejí"),
                w("当然", "of course", "طبعًا", "dāngrán"), w("本身", "itself", "بحدّ ذاته", "běnshēn"), w("关键在于", "the key lies in", "المهم يكمن في", "guānjiàn zàiyú"), w("使用", "to use", "يستخدم", "shǐyòng"),
            ), listOf(
                q("zh_st_shouji_q1", "我做了什么实验？", listOf("一个月不用手机", "晚上九点以后不用手机", "每天只用一个小时手机"), 1, g("What experiment did I do?", "ما التجربة التي أجريتها؟"),
                    listOf(g("no phone for a month", "لا هاتف لمدة شهر"), g("no phone after 9 p.m.", "لا هاتف بعد التاسعة مساءً"), g("only one hour of phone a day", "ساعة واحدة فقط من الهاتف يوميًا"))),
                q("zh_st_shouji_q2", "一个星期以后有什么变化？", listOf("睡得更好了", "更累了", "朋友更少了"), 0, g("What changed after a week?", "ما الذي تغيّر بعد أسبوع؟"),
                    listOf(g("I slept better", "صرت أنام أفضل"), g("I was more tired", "صرت أكثر تعبًا"), g("I had fewer friends", "صار أصدقائي أقل"))),
                q("zh_st_shouji_q3", "作者认为关键是什么？", listOf("手机太贵", "怎么使用手机", "消息太多"), 1, g("What does the writer think is the key?", "ما الأمر المهم في رأي الكاتب؟"),
                    listOf(g("phones are too expensive", "الهواتف غالية جدًا"), g("how we use our phones", "كيف نستخدم الهاتف"), g("there are too many messages", "الرسائل كثيرة جدًا"))),
            ), listOf(
                o("zh_st_shouji_o1", "实验刚开始的时候，我为什么觉得难受？", "Why did I find it hard when the experiment began?", "لماذا وجدت الأمر صعبًا في بداية التجربة؟",
                    "因为我总担心错过重要的消息。", "担心", "错过|重要"),
                retell("zh_st_shouji_o2", L, "我以前离不开手机，很难集中注意力。我决定晚上九点以后不用手机，后来睡得更好了。我觉得关键是怎么使用手机。",
                    "手机", "九点|晚上|实验", "睡|精神", "使用|怎么用"),
            )),
            story("zh_st_mianshi", L, B2, "一次失败的面试", "A failed interview", "مقابلة عمل فاشلة", listOf(
                l("大学毕业那年，我参加了一家大公司的面试。", "The year I graduated from university, I had an interview with a big company.", "في سنة تخرّجي من الجامعة أجريت مقابلة عمل مع شركة كبيرة."),
                l("为了准备，我在网上查了很多资料，还把常见的问题和答案都背了下来。", "To prepare, I looked up a lot of information online and memorised the common questions and answers.", "للاستعداد بحثت عن معلومات كثيرة على الإنترنت، وحفظت الأسئلة الشائعة وإجاباتها."),
                l("没想到，面试官一个准备好的问题也没问。", "Unexpectedly, the interviewer didn't ask a single one of the questions I had prepared.", "وعلى غير المتوقع لم يسأل المحاوِر سؤالًا واحدًا مما استعددت له."),
                l("他只是让我讲一次失败的经历，以及我从中学到了什么。", "He just asked me to talk about a time I failed and what I learned from it.", "طلب مني فقط أن أتحدث عن تجربة فشل مررت بها، وعمّا تعلّمته منها."),
                l("我紧张得说不出话来，最后只说了几句很空的话。", "I was so nervous that I couldn't speak, and in the end I said only a few empty sentences.", "توترت حتى عجزت عن الكلام، وفي النهاية لم أقل إلا بضع جمل فارغة."),
                l("结果可想而知，我没有被录取。", "You can imagine the result: I didn't get the job.", "والنتيجة متوقعة: لم يتم قبولي."),
                l("不过，这次经历让我明白，背答案不如真正了解自己。", "However, the experience taught me that memorising answers is no match for really knowing yourself.", "لكن هذه التجربة جعلتني أفهم أن حفظ الإجابات لا يغني عن معرفة النفس حقًّا."),
                l("后来每次面试以前，我都会先想想自己的经历和优缺点。", "After that, before every interview, I first thought about my own experiences, strengths and weaknesses.", "بعد ذلك صرت قبل كل مقابلة أفكر أولًا في تجاربي ونقاط قوتي وضعفي."),
                l("半年以后，我终于找到了一份满意的工作。", "Six months later, I finally found a job I was happy with.", "وبعد ستة أشهر وجدت أخيرًا عملًا أرضاني."),
            ), listOf(
                w("毕业", "to graduate", "يتخرّج", "bìyè"), w("参加", "to take part in", "يشارك في", "cānjiā"), w("面试", "interview", "مقابلة عمل", "miànshì"),
                w("为了", "in order to", "من أجل", "wèile"), w("准备", "to prepare", "يستعدّ", "zhǔnbèi"), w("查", "to look up", "يبحث عن", "chá"), w("资料", "information, material", "معلومات، مواد", "zīliào"),
                w("常见", "common", "شائع", "chángjiàn"), w("背了下来", "learned by heart", "حفظ عن ظهر قلب", "bèi le xiàlai"), w("没想到", "unexpectedly", "على غير المتوقع", "méi xiǎngdào"),
                w("面试官", "interviewer", "المحاوِر في المقابلة", "miànshìguān"), w("失败", "failure; to fail", "فشل", "shībài"), w("经历", "experience", "تجربة (مرّ بها)", "jīnglì"),
                w("以及", "as well as, and", "وكذلك", "yǐjí"), w("紧张", "nervous", "متوتر", "jǐnzhāng"), w("说不出话来", "unable to say a word", "عجز عن الكلام", "shuō bu chū huà lái"),
                w("空的话", "empty words", "كلام فارغ", "kōng de huà"), w("结果", "result", "النتيجة", "jiéguǒ"), w("可想而知", "one can imagine", "يمكن تخيّله", "kě xiǎng ér zhī"),
                w("录取", "to accept, to hire", "يقبل (مرشحًا)", "lùqǔ"), w("不过", "however", "لكن", "búguò"), w("明白", "to understand", "يفهم", "míngbai"),
                w("不如", "not as good as", "لا يرقى إلى", "bùrú"), w("真正", "truly, really", "حقًّا", "zhēnzhèng"), w("了解", "to know well, to understand", "يعرف جيدًا", "liǎojiě"),
                w("优缺点", "strengths and weaknesses", "نقاط القوة والضعف", "yōuquēdiǎn"), w("终于", "finally", "أخيرًا", "zhōngyú"), w("份", "(measure word for jobs)", "(كلمة عدّ للوظائف)", "fèn"),
                w("满意", "satisfied", "راضٍ", "mǎnyì"),
            ), listOf(
                q("zh_st_mianshi_q1", "面试官让我讲什么？", listOf("我的大学生活", "一次失败的经历", "我为什么喜欢这家公司"), 1, g("What did the interviewer ask me to talk about?", "عمّ طلب مني المحاوِر أن أتحدث؟"),
                    listOf(g("my university life", "حياتي الجامعية"), g("a time I failed", "تجربة فشل"), g("why I like this company", "لماذا أحب هذه الشركة"))),
                q("zh_st_mianshi_q2", "面试的结果怎么样？", listOf("被录取了", "没有被录取", "还不知道"), 1, g("What was the result of the interview?", "ما نتيجة المقابلة؟"),
                    listOf(g("I got the job", "قُبلت"), g("I didn't get the job", "لم أُقبل"), g("I don't know yet", "لا أعرف بعد"))),
                q("zh_st_mianshi_q3", "我从这次经历学到了什么？", listOf("背答案不如了解自己", "面试不重要", "应该多查资料"), 0, g("What did I learn from this experience?", "ماذا تعلّمت من هذه التجربة؟"),
                    listOf(g("knowing yourself is better than memorising answers", "معرفة النفس أفضل من حفظ الإجابات"), g("interviews don't matter", "المقابلات ليست مهمة"), g("I should look up more information", "عليّ أن أبحث عن معلومات أكثر"))),
            ), listOf(
                o("zh_st_mianshi_o1", "面试的时候我为什么没回答好？", "Why didn't I answer well in the interview?", "لماذا لم أُجب جيدًا في المقابلة؟",
                    "因为面试官没问我准备好的问题，我太紧张了。", "紧张", "准备|问题"),
                retell("zh_st_mianshi_o2", L, "我准备了很多答案，可是面试官让我讲一次失败的经历，我太紧张，没被录取。后来我明白了要先了解自己，半年以后找到了工作。",
                    "面试", "紧张|失败", "录取", "了解|工作"),
            )),
            story("zh_st_laojia", L, B2, "回老家", "Back to my home village", "العودة إلى القرية", listOf(
                l("春节的时候，我带着女儿回了一趟老家。", "At Spring Festival I took my daughter back to my home village.", "في عيد الربيع عدت مع ابنتي في زيارة إلى قريتي."),
                l("我已经差不多十年没回去了，村子的变化大得让我吃惊。", "I hadn't been back for almost ten years, and the village had changed so much it astonished me.", "لم أعد إليها منذ قرابة عشر سنوات، وأدهشني مقدار تغيّر القرية."),
                l("以前的土路变成了宽宽的水泥路，很多人家都盖了新房子。", "The old dirt road had become a wide concrete road, and many families had built new houses.", "صار الطريق الترابي القديم طريقًا إسمنتيًا عريضًا، وبنت عائلات كثيرة بيوتًا جديدة."),
                l("可是村里的年轻人却少了很多，大部分都去城里打工了。", "But there were far fewer young people in the village; most had gone to work in the city.", "لكن عدد الشباب في القرية قلّ كثيرًا، فمعظمهم ذهبوا للعمل في المدينة."),
                l("留下来的主要是老人和孩子。", "Those who stayed were mainly old people and children.", "ومن بقي هم في الغالب كبار السن والأطفال."),
                l("我小时候常去的那条小河还在，只是水比以前浅了。", "The little river I often went to as a child was still there, only the water was shallower than before.", "النهر الصغير الذي كنت أذهب إليه كثيرًا في طفولتي ما زال موجودًا، إلا أن ماءه صار أضحل من قبل."),
                l("女儿在城里长大，第一次看见鸡和鸭在院子里跑来跑去，兴奋得不得了。", "My daughter grew up in the city; seeing chickens and ducks running around a yard for the first time, she was terribly excited.", "ابنتي نشأت في المدينة، وحين رأت الدجاج والبط يركض في الفناء لأول مرة تحمّست كثيرًا."),
                l("离开的时候，她问我：“我们明年还能再来吗？”", "When we left, she asked me, \"Can we come again next year?\"", "وعندما غادرنا سألتني: «هل نستطيع أن نأتي مرة أخرى في العام القادم؟»"),
            ), listOf(
                w("春节", "Spring Festival (Chinese New Year)", "عيد الربيع (رأس السنة الصينية)", "Chūn Jié"), w("带着", "taking along", "آخذًا معه", "dàizhe"),
                w("趟", "(measure word for trips)", "(كلمة عدّ للرحلات)", "tàng"), w("老家", "home village, hometown", "مسقط الرأس", "lǎojiā"), w("差不多", "almost", "تقريبًا", "chàbuduō"),
                w("村子", "village", "قرية", "cūnzi"), w("变化", "change", "تغيّر", "biànhuà"), w("吃惊", "astonished", "مندهش", "chījīng"), w("土路", "dirt road", "طريق ترابي", "tǔlù"),
                w("变成", "to become", "يصبح", "biànchéng"), w("宽", "wide", "عريض", "kuān"), w("水泥路", "concrete road", "طريق إسمنتي", "shuǐní lù"), w("盖", "to build", "يبني", "gài"),
                w("房子", "house", "بيت", "fángzi"), w("却", "but, yet", "لكن", "què"), w("大部分", "most", "معظم", "dà bùfen"), w("打工", "to work (a job away from home)", "يعمل (بعيدًا عن بلده)", "dǎgōng"),
                w("留下来", "to stay behind", "يبقى", "liú xiàlai"), w("主要", "mainly", "في الغالب", "zhǔyào"), w("老人", "old people", "كبار السن", "lǎorén"),
                w("小时候", "in childhood", "في الطفولة", "xiǎoshíhou"), w("小河", "small river", "نهر صغير", "xiǎo hé"), w("浅", "shallow", "ضحل", "qiǎn"), w("长大", "to grow up", "ينشأ، يكبر", "zhǎngdà"),
                w("鸡", "chicken", "دجاج", "jī"), w("鸭", "duck", "بط", "yā"), w("院子", "yard, courtyard", "فناء", "yuànzi"), w("跑来跑去", "to run around", "يركض هنا وهناك", "pǎo lái pǎo qù"),
                w("兴奋", "excited", "متحمّس", "xīngfèn"), w("不得了", "extremely", "للغاية", "bùdéliǎo"), w("离开", "to leave", "يغادر", "líkāi"), w("明年", "next year", "العام القادم", "míngnián"),
            ), listOf(
                q("zh_st_laojia_q1", "我多久没回老家了？", listOf("一年", "差不多十年", "二十年"), 1, g("How long had I been away from my home village?", "منذ متى لم أعد إلى قريتي؟"),
                    listOf(g("one year", "سنة"), g("almost ten years", "قرابة عشر سنوات"), g("twenty years", "عشرون سنة"))),
                q("zh_st_laojia_q2", "村里的年轻人为什么少了？", listOf("去城里打工了", "去国外上学了", "搬到别的村子了"), 0, g("Why were there fewer young people in the village?", "لماذا قلّ الشباب في القرية؟"),
                    listOf(g("they went to work in the city", "ذهبوا للعمل في المدينة"), g("they went abroad to study", "سافروا للدراسة في الخارج"), g("they moved to another village", "انتقلوا إلى قرية أخرى"))),
                q("zh_st_laojia_q3", "女儿看到什么很兴奋？", listOf("小河", "新房子", "鸡和鸭"), 2, g("What made my daughter excited?", "ما الذي جعل ابنتي متحمّسة؟"),
                    listOf(g("the little river", "النهر الصغير"), g("the new houses", "البيوت الجديدة"), g("the chickens and ducks", "الدجاج والبط"))),
            ), listOf(
                o("zh_st_laojia_o1", "村子里有哪些变化？说两个。", "What had changed in the village? Name two things.", "ما الذي تغيّر في القرية؟ اذكر أمرين.",
                    "土路变成了水泥路，很多人盖了新房子，年轻人也少了。", "路", "房子|年轻人"),
                retell("zh_st_laojia_o2", L, "春节我带女儿回了老家。村子变化很大，有了新路和新房子，可是年轻人都去城里打工了。女儿很喜欢那里。",
                    "老家|村子", "路|房子", "年轻人|打工", "女儿"),
            )),
        )
    }
}
