package com.polyglot.academy.core.speaking

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.RolePlayScenario

/**
 * One speaking task on the path from first words to fluent conversation. Missions are language
 * independent: the AI tutor plays them in the learner's target language. Where a built-in
 * role-play exists for a language, it is used when no AI tutor is available.
 */
data class SpeakingMission(
    val id: String,
    val level: CefrLevel,
    val title: Gloss,
    /** What the learner can do after the mission, written for learners (not official CEFR wording). */
    val canDo: Gloss,
    val tasks: List<Gloss>,
    /** Minimum learner turns for the mission to count. */
    val minTurns: Int,
    /** Minimum median words per learner turn (Mandarin: estimated words, see [SpeechUnits]). */
    val minWordsPerTurn: Int,
    /** Instructions for the AI tutor, in English. */
    val brief: String,
    val offlineScenarioIds: List<String> = emptyList(),
) {
    val conversationKey: String get() = "$KEY_PREFIX$id"

    fun offlineScenario(language: LanguageCode): RolePlayScenario? =
        Curriculum.of(language).scenarios.firstOrNull { it.id in offlineScenarioIds }

    companion object {
        const val KEY_PREFIX = "mission:"
    }
}

object SpeakingPath {
    private fun g(en: String, ar: String) = Gloss(en, ar)

    private fun m(
        id: String, level: CefrLevel, title: Gloss, canDo: Gloss, tasks: List<Gloss>, brief: String, offline: List<String> = emptyList(),
    ): SpeakingMission {
        val (turns, words) = when (level) {
            CefrLevel.PRE_A1, CefrLevel.A1 -> 4 to 3
            CefrLevel.A2 -> 5 to 5
            CefrLevel.B1 -> 6 to 8
            CefrLevel.B2 -> 7 to 12
            else -> 8 to 15
        }
        return SpeakingMission(id, level, title, canDo, tasks, turns, words, brief, offline)
    }

    val missions: List<SpeakingMission> = listOf(
        // ---------------- A1: survival and personal basics
        m("a1_intro", CefrLevel.A1, g("Introduce yourself", "عرّف بنفسك"),
            g("I can say who I am, where I'm from and what I do.", "أستطيع أن أقول من أنا ومن أين أنا وماذا أعمل."),
            listOf(g("Say your name", "قل اسمك"), g("Say where you are from", "قل من أين أنت"), g("Say what you do (job or studies)", "قل ماذا تعمل أو تدرس")),
            "First meeting with a new classmate. Ask the learner their name, where they are from and what they do, one question at a time.",
            listOf("en_s_smalltalk", "zh_s_meet")),
        m("a1_cafe", CefrLevel.A1, g("Order food and drink", "اطلب طعامًا وشرابًا"),
            g("I can order something simple and ask the price.", "أستطيع طلب شيء بسيط والسؤال عن السعر."),
            listOf(g("Order a drink", "اطلب مشروبًا"), g("Order something to eat", "اطلب شيئًا للأكل"), g("Ask how much it costs and say thank you", "اسأل عن السعر واشكر")),
            "You are a friendly server in a café. Take the learner's order and tell them the price when asked.",
            listOf("en_s_cafe", "es_s_cafe", "fr_s_boulangerie", "ar_s_cafe", "zh_s_tea")),
        m("a1_family", CefrLevel.A1, g("Talk about your family", "تحدّث عن عائلتك"),
            g("I can name my family members and describe one of them.", "أستطيع ذكر أفراد عائلتي ووصف أحدهم."),
            listOf(g("Say who is in your family", "قل من في عائلتك"), g("Describe one person (age, job or character)", "صِف شخصًا واحدًا (العمر أو العمل أو الطباع)"), g("Ask the tutor about their family", "اسأل المعلّم عن عائلته")),
            "Chat about families. Share a little about your own (invented, clearly fictional) family so the learner can ask you questions."),
        m("a1_day", CefrLevel.A1, g("Your typical day", "يومك المعتاد"),
            g("I can describe my daily routine with times.", "أستطيع وصف روتيني اليومي مع الأوقات."),
            listOf(g("Say when you get up", "قل متى تستيقظ"), g("Say two things you do every day", "اذكر شيئين تفعلهما كل يوم"), g("Say what you do in the evening", "قل ماذا تفعل في المساء")),
            "Ask the learner about their normal day: morning, daytime and evening. Ask about times."),
        m("a1_shopping", CefrLevel.A1, g("Buy something in a shop", "اشترِ شيئًا من متجر"),
            g("I can ask for an item, its price and another size or colour.", "أستطيع طلب سلعة والسؤال عن سعرها وعن مقاس أو لون آخر."),
            listOf(g("Say what you are looking for", "قل ما تبحث عنه"), g("Ask the price", "اسأل عن السعر"), g("Ask for another size or colour", "اطلب مقاسًا أو لونًا آخر")),
            "You are a shop assistant in a clothes shop. Help the learner find something."),
        m("a1_likes", CefrLevel.A1, g("Likes and dislikes", "ما تحبه وما لا تحبه"),
            g("I can say what I like and don't like and ask others.", "أستطيع قول ما أحب وما لا أحب وسؤال الآخرين."),
            listOf(g("Say three things you like", "اذكر ثلاثة أشياء تحبها"), g("Say one thing you don't like", "اذكر شيئًا لا تحبه"), g("Ask the tutor what they like", "اسأل المعلّم عمّا يحب")),
            "Talk about food, music and free time. Share your own simple preferences when asked."),

        // ---------------- A2: everyday situations, simple past and future
        m("a2_directions", CefrLevel.A2, g("Find your way", "اعرف طريقك"),
            g("I can ask for and understand simple directions.", "أستطيع السؤال عن الاتجاهات البسيطة وفهمها."),
            listOf(g("Ask where a place is", "اسأل أين يقع مكان"), g("Check how far it is", "تأكّد من المسافة"), g("Repeat the directions back", "أعد ذكر الاتجاهات")),
            "You are a passer-by in a city. The learner is looking for the station or a museum. Give clear directions.",
            listOf("en_s_directions", "es_s_direcciones")),
        m("a2_doctor", CefrLevel.A2, g("At the doctor's", "عند الطبيب"),
            g("I can explain a simple health problem and understand advice.", "أستطيع شرح مشكلة صحية بسيطة وفهم النصيحة."),
            listOf(g("Say what hurts", "قل ما يؤلمك"), g("Say since when", "قل منذ متى"), g("Ask a question about the advice", "اسأل سؤالًا عن النصيحة")),
            "You are a doctor in a role-play for LANGUAGE PRACTICE ONLY. Give only generic advice like rest and drinking water, never real medical advice or diagnoses.",
            listOf("es_s_medico", "fr_s_medecin", "ar_s_doctor")),
        m("a2_weekend", CefrLevel.A2, g("Your last weekend", "عطلة نهاية الأسبوع الماضية"),
            g("I can say what I did, using the past.", "أستطيع قول ما فعلته باستخدام الماضي."),
            listOf(g("Say where you went", "قل إلى أين ذهبت"), g("Say who you were with", "قل مع من كنت"), g("Say if you enjoyed it and why", "قل هل استمتعت ولماذا")),
            "Ask about the learner's last weekend. Focus on past-tense forms; recast past-tense errors."),
        m("a2_plans", CefrLevel.A2, g("Make plans with a friend", "رتّب موعدًا مع صديق"),
            g("I can suggest an activity and agree on a time and place.", "أستطيع اقتراح نشاط والاتفاق على وقت ومكان."),
            listOf(g("Suggest something to do", "اقترح نشاطًا"), g("Agree on a day and time", "اتفق على يوم ووقت"), g("Agree where to meet", "اتفق على مكان اللقاء")),
            "You are the learner's friend. Be a little busy so you have to negotiate a time (but agree in the end)."),
        m("a2_hotel", CefrLevel.A2, g("Sort out a hotel problem", "حلّ مشكلة في الفندق"),
            g("I can explain a problem politely and ask for a solution.", "أستطيع شرح مشكلة بأدب وطلب حل."),
            listOf(g("Say who you are and your room", "عرّف بنفسك ورقم غرفتك"), g("Explain the problem", "اشرح المشكلة"), g("Agree on a solution", "اتفق على حل")),
            "You are a hotel receptionist. The learner has a problem with their room (noise, no hot water, wrong room). Be helpful."),
        m("a2_home", CefrLevel.A2, g("Where you live", "المكان الذي تعيش فيه"),
            g("I can describe my home and my neighbourhood.", "أستطيع وصف بيتي وحيّي."),
            listOf(g("Describe your home", "صِف بيتك"), g("Say what is near it", "قل ما القريب منه"), g("Say what you like or would change", "قل ما يعجبك أو ما تودّ تغييره")),
            "Ask the learner to describe their home and area. Ask follow-up questions about details."),

        // ---------------- B1: stories, opinions, problems
        m("b1_story", CefrLevel.B1, g("Tell a story", "احكِ قصة"),
            g("I can tell a story in order and say how I felt.", "أستطيع سرد قصة بالترتيب وقول ما شعرت به."),
            listOf(g("Set the scene (when and where)", "حدّد الزمان والمكان"), g("Tell what happened in order", "احكِ ما حدث بالترتيب"), g("Say how it ended and how you felt", "قل كيف انتهت وما شعرت به")),
            "Ask the learner to tell you about a memorable trip or a surprising experience. Use follow-up questions (and then? why? how did you feel?). Encourage linking words like first, then, after that, finally."),
        m("b1_opinion", CefrLevel.B1, g("Give and support an opinion", "عبّر عن رأيك وادعمه"),
            g("I can give my opinion with a reason and an example.", "أستطيع إبداء رأيي مع سبب ومثال."),
            listOf(g("Give your opinion clearly", "أبدِ رأيك بوضوح"), g("Give a reason", "اذكر سببًا"), g("Give an example from your life", "اذكر مثالًا من حياتك")),
            "Discuss whether smartphones make life better. Ask for reasons and examples; politely mention one opposite view."),
        m("b1_interview", CefrLevel.B1, g("A job interview", "مقابلة عمل"),
            g("I can talk about my experience and strengths in an interview.", "أستطيع الحديث عن خبرتي ونقاط قوتي في مقابلة."),
            listOf(g("Describe your experience", "صِف خبرتك"), g("Name a strength with an example", "اذكر نقطة قوة مع مثال"), g("Ask the interviewer a question", "اطرح سؤالًا على من يجري المقابلة")),
            "You are a friendly interviewer for a job the learner chooses. Ask typical interview questions one at a time."),
        m("b1_problem", CefrLevel.B1, g("Solve a problem", "حلّ مشكلة"),
            g("I can explain an unexpected problem and work out a solution.", "أستطيع شرح مشكلة غير متوقعة والوصول إلى حل."),
            listOf(g("Explain what happened", "اشرح ما حدث"), g("Describe the lost item or the issue in detail", "صِف الشيء المفقود أو المشكلة بالتفصيل"), g("Agree on next steps", "اتفق على الخطوات التالية")),
            "You work at an airport service desk. The learner's luggage is lost. Ask for details and explain the process."),
        m("b1_meeting", CefrLevel.B1, g("A work meeting", "اجتماع عمل"),
            g("I can discuss a plan, a budget and a deadline at work.", "أستطيع مناقشة خطة وميزانية وموعد نهائي في العمل."),
            listOf(g("Explain your proposal", "اشرح مقترحك"), g("Agree on the budget or deadline", "اتفق على الميزانية أو الموعد"), g("Confirm the next step", "أكّد الخطوة التالية")),
            "You are a client in a meeting about a small project. Ask about cost and timing.",
            listOf("es_s_reunion", "fr_s_reunion", "ar_s_meeting", "zh_s_client")),
        m("b1_culture", CefrLevel.B1, g("Customs and traditions", "العادات والتقاليد"),
            g("I can describe a tradition and compare it with another culture.", "أستطيع وصف عادة ومقارنتها بثقافة أخرى."),
            listOf(g("Describe a celebration from your culture", "صِف احتفالًا من ثقافتك"), g("Explain what people do and why", "اشرح ما يفعله الناس ولماذا"), g("Compare it with another country", "قارنه ببلد آخر")),
            "Ask about a festival or custom the learner knows well. Be curious and respectful; do not state facts about cultures you are unsure of."),

        // ---------------- B2: argument, hypotheses, formal and professional talk
        m("b2_debate", CefrLevel.B2, g("Debate pros and cons", "ناقش الإيجابيات والسلبيات"),
            g("I can argue both sides of an issue and reach a conclusion.", "أستطيع عرض جانبي قضية والوصول إلى خلاصة."),
            listOf(g("Give two advantages", "اذكر ميزتين"), g("Give two disadvantages", "اذكر عيبين"), g("Conclude with your own position", "اختم برأيك")),
            "Debate whether working from home is better than working in an office. Challenge weak arguments politely and ask for evidence from experience."),
        m("b2_negotiate", CefrLevel.B2, g("Negotiate a deal", "تفاوض على اتفاق"),
            g("I can negotiate politely and reach a compromise.", "أستطيع التفاوض بأدب والوصول إلى حل وسط."),
            listOf(g("State what you need", "اذكر ما تحتاجه"), g("Make a counter-offer", "قدّم عرضًا مقابلًا"), g("Agree and summarise the deal", "وافق ولخّص الاتفاق")),
            "You are a supplier. The learner wants a better price or earlier delivery. Don't give in immediately; make them justify requests.",
            listOf("en_s_negotiation")),
        m("b2_hypothetical", CefrLevel.B2, g("What would you do if…?", "ماذا كنت ستفعل لو…؟"),
            g("I can talk about imaginary situations and their consequences.", "أستطيع الحديث عن مواقف متخيَّلة ونتائجها."),
            listOf(g("Answer a 'what if' question", "أجب عن سؤال 'ماذا لو'"), g("Explain the consequences", "اشرح النتائج"), g("Ask the tutor a 'what if' question", "اطرح على المعلّم سؤال 'ماذا لو'")),
            "Ask imaginative questions (if you could live anywhere, if you won a large prize). Target conditional/hypothetical structures and recast errors in them."),
        m("b2_issue", CefrLevel.B2, g("Discuss a social issue", "ناقش قضية اجتماعية"),
            g("I can explain causes and effects of an issue I care about.", "أستطيع شرح أسباب ونتائج قضية تهمّني."),
            listOf(g("Choose and explain an issue", "اختر قضية واشرحها"), g("Explain causes and effects", "اشرح الأسباب والنتائج"), g("Suggest a realistic solution", "اقترح حلًا واقعيًا")),
            "Let the learner choose a social or environmental issue. Do not introduce news facts or statistics yourself; ask questions and help them express ideas precisely."),
        m("b2_complaint", CefrLevel.B2, g("Make a formal complaint", "قدّم شكوى رسمية"),
            g("I can complain formally, keep a polite tone and insist on a solution.", "أستطيع تقديم شكوى رسمية بأسلوب مهذب والإصرار على حل."),
            listOf(g("Explain the problem formally", "اشرح المشكلة بأسلوب رسمي"), g("Say what you expect", "قل ما تتوقعه"), g("Respond to a first refusal politely", "ردّ بأدب على الرفض الأول")),
            "You are a customer-service agent on the phone. At first offer only a small gesture, so the learner must insist politely."),
        m("b2_pitch", CefrLevel.B2, g("Present an idea", "اعرض فكرة"),
            g("I can present an idea clearly and answer questions about it.", "أستطيع عرض فكرة بوضوح والإجابة عن الأسئلة حولها."),
            listOf(g("Present the idea in a few sentences", "اعرض الفكرة في بضع جمل"), g("Explain who it helps and how", "اشرح من تفيد وكيف"), g("Answer two critical questions", "أجب عن سؤالين نقديين")),
            "The learner pitches a small business or community idea to you. Ask two or three critical but fair questions."),

        // ---------------- C1: nuance, persuasion, mediation, register
        m("c1_abstract", CefrLevel.C1, g("Discuss abstract ideas", "ناقش أفكارًا مجرّدة"),
            g("I can discuss abstract topics with nuance and qualification.", "أستطيع مناقشة مواضيع مجرّدة بدقة وتحفّظ."),
            listOf(g("Define what the idea means to you", "عرّف ما تعنيه الفكرة لك"), g("Qualify your view (it depends…, to some extent…)", "قيّد رأيك (يعتمد على… إلى حدٍّ ما…)"), g("Respond to a counter-example", "ردّ على مثال معاكس")),
            "Discuss what 'success' or 'a good life' means. Push for nuance, hedging and precise vocabulary; offer counter-examples."),
        m("c1_persuade", CefrLevel.C1, g("Persuade a sceptic", "أقنع شخصًا متشككًا"),
            g("I can build a persuasive case and handle objections.", "أستطيع بناء حجة مقنعة والتعامل مع الاعتراضات."),
            listOf(g("State your case convincingly", "اعرض حجتك بإقناع"), g("Handle two objections", "تعامل مع اعتراضين"), g("Find common ground", "جِد أرضية مشتركة")),
            "Play a sceptical colleague. The learner tries to persuade you to adopt a new way of working. Raise realistic objections and only agree to well-argued points."),
        m("c1_mediate", CefrLevel.C1, g("Mediate between two views", "توسّط بين رأيين"),
            g("I can summarise opposing views fairly and propose a compromise.", "أستطيع تلخيص الآراء المتعارضة بإنصاف واقتراح حل وسط."),
            listOf(g("Summarise each side fairly", "لخّص كل جانب بإنصاف"), g("Identify what both sides share", "حدّد ما يشترك فيه الطرفان"), g("Propose a compromise", "اقترح حلًا وسطًا")),
            "Describe a disagreement between two neighbours (or colleagues) about a shared space. The learner mediates; you react as one side."),
        m("c1_explain", CefrLevel.C1, g("Explain something complex simply", "اشرح شيئًا معقدًا ببساطة"),
            g("I can explain a complex topic from my field to a non-expert.", "أستطيع شرح موضوع معقد من مجالي لغير المتخصص."),
            listOf(g("Explain the main idea simply", "اشرح الفكرة الرئيسة ببساطة"), g("Use an analogy or example", "استخدم تشبيهًا أو مثالًا"), g("Check understanding and clarify", "تأكّد من الفهم ووضّح")),
            "Play a curious non-expert. Ask the learner to explain something from their work or studies; ask naive questions when anything is unclear."),
        m("c1_informal", CefrLevel.C1, g("Informal chat, idioms and humour", "دردشة غير رسمية وتعابير ودعابة"),
            g("I can switch to an informal register, tell an anecdote and use idioms naturally.", "أستطيع الانتقال إلى أسلوب غير رسمي ورواية طرفة واستخدام التعابير بطبيعية."),
            listOf(g("Tell a funny or embarrassing anecdote", "احكِ طرفة أو موقفًا محرجًا"), g("Use at least two idioms naturally", "استخدم تعبيرين اصطلاحيين على الأقل بطبيعية"), g("React to the tutor's story", "تفاعل مع قصة المعلّم")),
            "Chat like a close friend over coffee. Use natural informal language and idioms, explain any idiom the learner does not know, and gently point out register mismatches."),
        m("c1_professional", CefrLevel.C1, g("A high-stakes professional discussion", "نقاش مهني حاسم"),
            g("I can argue precisely in a professional setting under pressure.", "أستطيع النقاش بدقة في سياق مهني تحت الضغط."),
            listOf(g("Present the situation and the risks", "اعرض الوضع والمخاطر"), g("Recommend a course of action", "أوصِ بإجراء"), g("Defend your recommendation to tough questions", "دافع عن توصيتك أمام أسئلة صعبة")),
            "Play a demanding committee member in the learner's field. Ask probing questions; this is language practice, not professional advice.",
            listOf("en_s_loan_review")),
    )

    val levels: List<CefrLevel> = listOf(CefrLevel.A1, CefrLevel.A2, CefrLevel.B1, CefrLevel.B2, CefrLevel.C1)

    fun of(id: String): SpeakingMission? = missions.firstOrNull { it.id == id }

    /** Resolves a conversation key such as "mission:b1_story". */
    fun fromConversationKey(key: String?): SpeakingMission? =
        key?.takeIf { it.startsWith(SpeakingMission.KEY_PREFIX) }?.let { of(it.removePrefix(SpeakingMission.KEY_PREFIX)) }

    /** Level-specific coaching for the AI tutor that pushes towards fluency. */
    fun coaching(level: CefrLevel): String = when {
        level <= CefrLevel.A1 -> "Ask one simple question at a time. Accept short answers, then model a slightly longer answer the learner could copy."
        level <= CefrLevel.A2 -> "Ask simple follow-up questions. Encourage answers of a full sentence or two and model linking words like 'and', 'but', 'because'."
        level <= CefrLevel.B1 -> "Ask open questions and follow-ups (why? how? what happened next?). Encourage connected answers of several sentences."
        level <= CefrLevel.B2 -> "Challenge the learner's points politely, ask for examples and counter-arguments, and push for precise vocabulary instead of general words."
        else -> "Speak naturally, as to an educated adult. Push for nuance, hedging, idiomatic expressions and appropriate register; point out where a more precise or more natural phrase exists."
    }
}
