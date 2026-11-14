package com.polyglot.academy.core.content

import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CefrLevel.B2
import com.polyglot.academy.core.model.CefrLevel.C1
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.GlossaryEntry
import com.polyglot.academy.core.model.GrammarTopic
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Lesson
import com.polyglot.academy.core.model.OpenQuestion
import com.polyglot.academy.core.model.PartOfSpeech.ADJECTIVE
import com.polyglot.academy.core.model.PartOfSpeech.ADVERB
import com.polyglot.academy.core.model.PartOfSpeech.CONJUNCTION
import com.polyglot.academy.core.model.PartOfSpeech.NOUN
import com.polyglot.academy.core.model.PartOfSpeech.PHRASE
import com.polyglot.academy.core.model.PartOfSpeech.PREPOSITION
import com.polyglot.academy.core.model.PartOfSpeech.VERB
import com.polyglot.academy.core.model.ReadingPassage
import com.polyglot.academy.core.model.Skill

/**
 * A first C1 unit (four lessons) for English and Mandarin, and longer B2 and C1 readings, added 2026-11-10
 * because the upper levels were too easy and the course stopped at B2. Written by AI, not yet reviewed by
 * native speakers.
 */
internal object AdvancedLessons {
    private val EN = LanguageCode.ENGLISH
    private val ZH = LanguageCode.MANDARIN

    // ---------------- English ----------------

    fun englishVocab(vb: VocabBuilder) = with(vb) {
        // Inversion for emphasis
        v("seldom", "seldom", "rarely; not often", "نادرًا", ADVERB, C1, "emphasis", "/ˈseldəm/",
            ex = "Seldom have I seen such a beautiful sunset.", exEn = "Seldom have I seen such a beautiful sunset.", exAr = "نادرًا ما رأيت غروبًا بهذا الجمال.")
        v("hardly_when", "hardly … when", "used to say one thing happened just after another", "ما إن… حتى", PHRASE, C1, "emphasis",
            ex = "Hardly had we arrived when it started to rain.", exEn = "Hardly had we arrived when it started to rain.", exAr = "ما إن وصلنا حتى بدأ المطر.")
        v("no_sooner", "no sooner … than", "used to say one thing happened immediately after another", "لم يكد… حتى", PHRASE, C1, "emphasis")
        v("under_no_circumstances", "under no circumstances", "never, in any situation", "لا… تحت أي ظرف", PHRASE, C1, "emphasis",
            ex = "Under no circumstances should you share your password.", exEn = "Under no circumstances should you share your password.", exAr = "لا ينبغي أن تشارك كلمة مرورك تحت أي ظرف.")
        v("little_did", "little did … know", "used to say someone had no idea at all", "لم يكن يدري أبدًا", PHRASE, C1, "emphasis",
            ex = "Little did she know that her life was about to change.", exEn = "Little did she know that her life was about to change.", exAr = "لم تكن تدري أبدًا أن حياتها على وشك أن تتغيّر.")
        v("not_until", "not until", "only at a later time, and not before", "لم… إلا عندما", PHRASE, C1, "emphasis")
        // Hedging
        v("arguably", "arguably", "it can be said, though some may disagree", "يمكن القول إنّ", ADVERB, C1, "academic", "/ˈɑːɡjuəbli/",
            ex = "It is arguably the most important invention of the century.", exEn = "It is arguably the most important invention of the century.", exAr = "يمكن القول إنه أهم اختراع في هذا القرن.")
        v("tend_to", "tend to", "to usually do or be something", "يميل إلى", PHRASE, C1, "academic",
            ex = "People tend to spend more money when they feel happy.", exEn = "People tend to spend more money when they feel happy.", exAr = "يميل الناس إلى إنفاق مال أكثر حين يشعرون بالسعادة.")
        v("presumably", "presumably", "probably, as you would expect", "على الأرجح، يُفترض أنّ", ADVERB, C1, "academic", "/prɪˈzjuːməbli/")
        v("evidence", "evidence", "facts that show something is true", "دليل، أدلّة", NOUN, C1, "academic", "/ˈevɪdəns/",
            ex = "There is little evidence that the new diet works.", exEn = "There is little evidence that the new diet works.", exAr = "لا توجد أدلّة تُذكر على أن النظام الغذائي الجديد ينجح.")
        v("to_some_extent", "to some extent", "partly, but not completely", "إلى حدٍّ ما", PHRASE, C1, "academic")
        v("likelihood", "likelihood", "how likely something is to happen", "احتمال، أرجحيّة", NOUN, C1, "academic", "/ˈlaɪklihʊd/")
        v("appear_to", "appear to", "to seem, judging by what we know", "يبدو أنّه", PHRASE, C1, "academic")
        // Cleft sentences
        v("emphasise", "emphasise", "to give special importance to something", "يؤكّد، يشدّد على", VERB, C1, "focus", "/ˈemfəsaɪz/")
        v("crucial", "crucial", "extremely important", "حاسم، بالغ الأهمية", ADJECTIVE, C1, "focus", "/ˈkruːʃəl/",
            ex = "What is crucial is that everyone feels safe at work.", exEn = "What is crucial is that everyone feels safe at work.", exAr = "الأمر الحاسم هو أن يشعر الجميع بالأمان في العمل.")
        v("whereas", "whereas", "while, in contrast", "في حين أنّ", CONJUNCTION, C1, "focus", "/weərˈæz/",
            ex = "I prefer the mountains, whereas my wife loves the sea.", exEn = "I prefer the mountains, whereas my wife loves the sea.", exAr = "أفضّل الجبال، في حين أن زوجتي تحب البحر.")
        v("nevertheless", "nevertheless", "despite what has just been said", "ومع ذلك", ADVERB, C1, "focus", "/ˌnevəðəˈles/")
        v("underlying", "underlying", "real and basic, but not obvious", "كامن، جوهري", ADJECTIVE, C1, "focus", "/ˌʌndəˈlaɪɪŋ/")
        v("the_thing_is", "the thing is", "used to introduce the real problem", "المشكلة أنّ", PHRASE, C1, "focus")
        // Formal and informal register
        v("postpone", "postpone", "to move to a later time (formal for 'put off')", "يؤجّل", VERB, C1, "register", "/pəˈspəʊn/",
            ex = "The meeting has been postponed until next week.", exEn = "The meeting has been postponed until next week.", exAr = "أُجّل الاجتماع إلى الأسبوع القادم.")
        v("inquire", "inquire", "to ask for information (formal)", "يستفسر", VERB, C1, "register", "/ɪnˈkwaɪər/",
            ex = "I am writing to inquire about the position you advertised.", exEn = "I am writing to inquire about the position you advertised.", exAr = "أكتب لأستفسر عن الوظيفة التي أعلنتم عنها.")
        v("assistance", "assistance", "help (formal)", "مساعدة", NOUN, C1, "register", "/əˈsɪstəns/")
        v("regarding", "regarding", "about (formal)", "بخصوص، فيما يتعلّق بـ", PREPOSITION, C1, "register", "/rɪˈɡɑːdɪŋ/")
        v("obtain", "obtain", "to get (formal)", "يحصل على", VERB, C1, "register", "/əbˈteɪn/")
        v("sincerely", "sincerely", "used to end a formal letter", "مع خالص التحية (في ختام الرسائل الرسمية)", ADVERB, C1, "register", "/sɪnˈsɪəli/")
    }

    fun englishGrammar(): List<GrammarTopic> = listOf(
        grammar("inversion", EN, C1, g("Inversion for emphasis", "القلب للتوكيد"),
            g("After a negative or limiting word at the start of a sentence (Never, Seldom, Rarely, Hardly, No sooner, Not until, Under no circumstances, Little), the auxiliary comes before the subject, as in a question: Never have I felt so tired. If there is no auxiliary, add do/does/did: Little did he know. It sounds formal or dramatic, so it is used in writing, speeches and stories more than in conversation.",
                "إذا بدأت الجملة بكلمة نفي أو تقييد (Never وSeldom وRarely وHardly وNo sooner وNot until وUnder no circumstances وLittle) جاء الفعل المساعد قبل الفاعل كما في السؤال: Never have I felt so tired. وإن لم يكن في الجملة فعل مساعد فأضف do/does/did: Little did he know. وهذا الأسلوب رسمي أو مؤثر، فيُستخدم في الكتابة والخطب والقصص أكثر من المحادثة."),
            listOf(ex("No sooner had I sat down than the phone rang.", "No sooner had I sat down than the phone rang.", "لم أكد أجلس حتى رنّ الهاتف."),
                ex("Not until the end of the film did I understand the title.", "Not until the end of the film did I understand the title.", "لم أفهم العنوان إلا في نهاية الفيلم."),
                ex("Never I have seen such a mess.", "Put the auxiliary first: Never have I seen", "قدّم الفعل المساعد: Never have I seen", wrong = true)),
            listOf(g("Inverting after 'Not until' in the wrong clause: ✗ Not until did I leave home I understood → ✓ Not until I left home did I understand.", "القلب في الجزء الخطأ بعد Not until: ✗ Not until did I leave home I understood ← ✓ Not until I left home did I understand."),
                g("Using 'than' after 'Hardly': ✗ Hardly had we arrived than… → ✓ Hardly had we arrived when… ('than' goes with 'No sooner').", "استخدام than بعد Hardly: ✗ Hardly had we arrived than… ← ✓ Hardly had we arrived when… (أما than فتأتي مع No sooner)."))),
        grammar("hedging", EN, C1, g("Hedging: making careful claims", "التحوّط: صياغة الادعاءات بحذر"),
            g("In academic and professional English, writers avoid sounding too certain. They use verbs like seem, appear and tend (Prices appear to be rising), modal verbs (This may/could explain…), adverbs like arguably, presumably and probably, and phrases like to some extent, it could be argued that and there is some evidence that. Compare 'Social media causes loneliness' with 'Heavy use of social media may contribute to loneliness'.",
                "في الإنجليزية الأكاديمية والمهنية يتجنّب الكاتب أن يبدو واثقًا أكثر من اللازم. فيستخدم أفعالًا مثل seem وappear وtend (Prices appear to be rising)، والأفعال الناقصة (This may/could explain…)، وظروفًا مثل arguably وpresumably وprobably، وعبارات مثل to some extent وit could be argued that وthere is some evidence that. قارن بين «Social media causes loneliness» و«Heavy use of social media may contribute to loneliness»."),
            listOf(ex("The results appear to support the theory, at least to some extent.", "The results appear to support the theory, at least to some extent.", "يبدو أن النتائج تؤيّد النظرية، إلى حدٍّ ما على الأقل."),
                ex("It could be argued that the plan was too ambitious from the start.", "It could be argued that the plan was too ambitious from the start.", "يمكن القول إن الخطة كانت طموحة أكثر من اللازم منذ البداية."),
                ex("Children tend spending too much time online.", "tend + to + verb: tend to spend", "tend + to + الفعل: tend to spend", wrong = true)),
            listOf(g("Hedging every sentence: too many 'may possibly perhaps' makes a text weak. Hedge claims, not facts.", "الإفراط في التحوّط: كثرة may possibly perhaps تُضعف النص. تحوّط في الآراء والادعاءات لا في الحقائق."))),
        grammar("cleft", EN, C1, g("Cleft sentences: What… is… / It is… that…", "جمل التركيز: What… is… وIt is… that…"),
            g("To put focus on one part of a sentence, split it in two. 'It is/was + focus + that/who': It was Sara who found the mistake (not someone else). 'What + clause + is/was': What I need is a long holiday. 'All' works the same way for 'the only thing': All I want is some sleep. Speakers also use 'The thing is…' and 'The reason why… is that…'.",
                "لتركيز الانتباه على جزء من الجملة قسّمها إلى جزأين. «It is/was + الجزء المقصود + that/who»: It was Sara who found the mistake (هي لا غيرها). و«What + جملة + is/was»: What I need is a long holiday. وتعمل All بالطريقة نفسها بمعنى «الشيء الوحيد»: All I want is some sleep. ويستخدم المتحدثون أيضًا The thing is… وThe reason why… is that…."),
            listOf(ex("What surprised me most was how friendly everyone was.", "What surprised me most was how friendly everyone was.", "أكثر ما فاجأني هو مدى لطف الجميع."),
                ex("It was only last year that I started taking the subject seriously.", "It was only last year that I started taking the subject seriously.", "لم أبدأ التعامل مع المادة بجدية إلا في العام الماضي."),
                ex("What I need it is more time.", "No 'it' after the What-clause", "لا تأتي it بعد جملة What", wrong = true)),
            listOf(g("'The reason is because…' is common in speech but avoided in formal writing: use 'The reason is that…'.", "عبارة The reason is because… شائعة في الكلام لكنها تُتجنّب في الكتابة الرسمية: استخدم The reason is that…."))),
        grammar("register", EN, C1, g("Formal and informal register", "المستوى الرسمي وغير الرسمي"),
            g("English often has two ways to say the same thing. Everyday English prefers short phrasal verbs (put off, find out, get, ask about), while formal emails, reports and letters prefer single verbs (postpone, discover, obtain, inquire). Formal writing also avoids contractions (I'm → I am), uses 'regarding' rather than 'about' and polite indirect requests: I would be grateful if you could… instead of Can you…?",
                "كثيرًا ما يكون في الإنجليزية طريقتان لقول الشيء نفسه. فالإنجليزية اليومية تفضّل الأفعال المركّبة القصيرة (put off وfind out وget وask about)، أما الرسائل والتقارير الرسمية فتفضّل الأفعال المفردة (postpone وdiscover وobtain وinquire). وتتجنّب الكتابة الرسمية الاختصارات (I'm ← I am)، وتستخدم regarding بدل about، والطلبات المهذّبة غير المباشرة: I would be grateful if you could… بدل Can you…?"),
            listOf(ex("I would be grateful if you could send me further details.", "I would be grateful if you could send me further details.", "سأكون ممتنًا لو أرسلتم إليّ مزيدًا من التفاصيل."),
                ex("We regret to inform you that the event has been postponed.", "We regret to inform you that the event has been postponed.", "نأسف لإبلاغكم بأن الفعالية قد أُجّلت."),
                ex("Dear Sir, I wanna ask about the job.", "Too informal for a letter: I am writing to inquire about the position.", "غير رسمي لرسالة: I am writing to inquire about the position.", wrong = true)),
            listOf(g("Mixing registers: a very formal word in a chatty message sounds strange, and slang in a formal email sounds rude.", "الخلط بين المستويين: الكلمة الرسمية جدًا في رسالة ودّية تبدو غريبة، والعامية في رسالة رسمية تبدو قليلة الاحترام."))),
    )

    fun englishLessons(f: LessonFactory): List<Lesson> = listOf(
        f.lesson("inversion", C1, "c1_skills", g("Never have I…: inversion", "Never have I…: القلب للتوكيد"), g("Add drama and emphasis by putting a negative word first.", "أضف التأثير والتوكيد بتقديم كلمة النفي."),
            Skill.WRITING, listOf("seldom", "hardly_when", "no_sooner", "under_no_circumstances", "little_did", "not_until"), "inversion",
            extra = listOf(
                fill("inv1", EN, "Seldom ___ I read a book that moved me so deeply. (have)", listOf("have")),
                choice("inv2", EN, "No sooner had the concert started ___ the power went off.", listOf("than", "when", "that"), 0,
                    explanation = g("'No sooner' goes with 'than'; 'hardly' goes with 'when'.", "تأتي No sooner مع than، وتأتي hardly مع when.")),
                choice("inv3", EN, "Not until she read the letter again ___ what he really meant.", listOf("did she realise", "she realised", "she did realise"), 0),
                order("inv4", EN, listOf("Under", "no", "circumstances", "should", "you", "leave", "the", "door", "unlocked"),
                    g("Under no circumstances should you leave the door unlocked.", "لا ينبغي أن تترك الباب غير مقفل تحت أي ظرف.")),
                translate("inv5", EN, g("Little did they know that the shop would close the next day.", "لم يكونوا يدرون أبدًا أن المتجر سيُغلق في اليوم التالي."),
                    listOf("Little did they know that the shop would close the next day", "Little did they know the shop would close the next day")),
                writing("inv6", EN, g("Write the opening of a short story (80+ words). Use at least two inversions, e.g. 'Hardly had…', 'Never had…', 'Little did…'.", "اكتب بداية قصة قصيرة (80 كلمة على الأقل). استخدم القلب مرتين على الأقل، مثل Hardly had… وNever had… وLittle did…."), 80,
                    listOf("Hardly had", "Little did", "Never had")),
            ), prerequisites = listOf("regrets")),
        f.lesson("hedging", C1, "c1_skills", g("Careful claims: hedging", "الادعاءات الحذرة: التحوّط"), g("Sound balanced and credible in essays, reports and discussions.", "اكتب بتوازن ومصداقية في المقالات والتقارير والنقاشات."),
            Skill.WRITING, listOf("arguably", "tend_to", "presumably", "evidence", "to_some_extent", "likelihood", "appear_to"), "hedging",
            extra = listOf(
                choice("hd1", EN, "Which sentence is the most careful (hedged) claim?", listOf(
                    "Remote work may, to some extent, reduce stress for some employees.",
                    "Remote work reduces stress.",
                    "Remote work always reduces stress for everyone."), 0, skill = Skill.READING),
                fill("hd2", EN, "Older people ___ to be less comfortable with online banking, although this is changing. (tend)", listOf("tend")),
                choice("hd3", EN, "There is growing ___ that regular exercise improves memory.", listOf("evidence", "evidences", "an evidence"), 0,
                    explanation = g("'Evidence' is uncountable: no -s and no 'an'.", "evidence اسم غير معدود: بلا ‎-s وبلا an.")),
                translate("hd4", EN, g("It could be argued that the price is too high.", "يمكن القول إن السعر مرتفع أكثر من اللازم."),
                    listOf("It could be argued that the price is too high", "It can be argued that the price is too high")),
                writing("hd5", EN, g("Write a paragraph (100+ words) on whether smartphones make people less sociable. Present both sides and hedge your claims.", "اكتب فقرة (100 كلمة على الأقل) عن سؤال: هل تجعل الهواتف الذكية الناس أقل اجتماعية؟ اعرض الرأيين وتحوّط في ادعاءاتك."), 100,
                    listOf("tend to", "arguably", "to some extent", "evidence")),
            ), prerequisites = listOf("inversion")),
        f.lesson("cleft", C1, "c1_skills", g("What I mean is…: cleft sentences", "What I mean is…: جمل التركيز"), g("Put the focus exactly where you want it in speech and writing.", "ضع التركيز حيث تريد تمامًا في الكلام والكتابة."),
            Skill.SPEAKING, listOf("emphasise", "crucial", "whereas", "nevertheless", "underlying", "the_thing_is"), "cleft",
            extra = listOf(
                fill("cl1", EN, "It was my grandmother ___ taught me how to cook. (a person)", listOf("who", "that")),
                choice("cl2", EN, "\"I need a holiday.\" → ___ a holiday.", listOf("What I need is", "What I need it is", "That I need is"), 0),
                order("cl3", EN, listOf("What", "worries", "me", "is", "the", "cost", "of", "the", "project"),
                    g("What worries me is the cost of the project.", "ما يقلقني هو تكلفة المشروع.")),
                choice("cl4", EN, "My brother loves cities, ___ I would rather live in the countryside.", listOf("whereas", "nevertheless", "despite"), 0),
                writing("cl5", EN, g("You disagree with a colleague's plan. Write what you would say (80+ words), using 'What I'm worried about is…', 'The thing is…' and 'It's… that…'.", "أنت لا توافق على خطة زميلك. اكتب ما ستقوله (80 كلمة على الأقل) مستخدمًا What I'm worried about is… وThe thing is… وIt's… that…."), 80,
                    listOf("What", "The thing is", "It's")),
            ), prerequisites = listOf("hedging")),
        f.lesson("register", C1, "c1_skills", g("Formal emails and register", "الرسائل الرسمية ومستوى اللغة"), g("Switch between everyday and formal English.", "انتقل بين الإنجليزية اليومية والرسمية."),
            Skill.WRITING, listOf("postpone", "inquire", "assistance", "regarding", "obtain", "sincerely"), "register",
            extra = listOf(
                choice("rg1", EN, "Formal for 'We've put off the meeting':", listOf("The meeting has been postponed.", "We've put the meeting back a bit.", "The meeting's off for now."), 0),
                choice("rg2", EN, "Formal for 'Can you help me?':", listOf("I would be grateful for your assistance.", "Can you give me a hand?", "Help me out, please."), 0),
                fill("rg3", EN, "I am writing ___ your advertisement in yesterday's newspaper. (formal 'about')", listOf("regarding", "concerning", "with regard to")),
                translate("rg4", EN, g("I would be grateful if you could send me the form.", "سأكون ممتنًا لو أرسلتم إليّ الاستمارة."),
                    listOf("I would be grateful if you could send me the form")),
                writing("rg5", EN, g("Write a formal email (100+ words) asking a university for information about a course: dates, fees and how to apply.", "اكتب رسالة رسمية (100 كلمة على الأقل) تطلب فيها من جامعة معلومات عن مقرّر: المواعيد والرسوم وطريقة التقديم."), 100,
                    listOf("Dear", "inquire", "regarding", "I would be grateful", "Yours sincerely")),
            ), prerequisites = listOf("cleft")),
    )

    fun englishUnits(): List<CourseUnit> = listOf(
        CourseUnit("en_u_c1_skills", EN, C1, g("Emphasis, nuance and register", "التوكيد والدقة ومستوى اللغة"), g("Inversion, hedging, cleft sentences and formal English", "القلب والتحوّط وجمل التركيز والإنجليزية الرسمية"),
            listOf("en_l_inversion", "en_l_hedging", "en_l_cleft", "en_l_register")),
    )

    // ---------------- Mandarin ----------------

    fun mandarinVocab(vb: VocabBuilder) = with(vb) {
        // Formal written connectors
        v("yinci", "因此", "therefore, for this reason", "لذلك، ولهذا", CONJUNCTION, C1, "formal", "yīncǐ",
            ex = "他工作很努力，因此很快就升职了。", exEn = "He works very hard, so he was soon promoted.", exAr = "يعمل بجدّ، ولذلك رُقّي سريعًا.")
        v("raner", "然而", "however", "غير أنّ، إلا أنّ", CONJUNCTION, C1, "formal", "rán'ér")
        v("conger", "从而", "thus, thereby", "وبذلك", CONJUNCTION, C1, "formal", "cóng'ér", trad = "從而")
        v("ciwai", "此外", "in addition, besides", "إضافةً إلى ذلك", CONJUNCTION, C1, "formal", "cǐwài")
        v("jinguan", "尽管", "even though, despite", "على الرغم من أنّ", CONJUNCTION, C1, "formal", "jǐn guǎn", trad = "儘管",
            ex = "尽管遇到了很多困难，他还是完成了任务。", exEn = "Even though he met many difficulties, he still finished the task.", exAr = "على الرغم من أنه واجه صعوبات كثيرة، فقد أنجز المهمة.")
        v("zongzhi", "总之", "in short, all in all", "باختصار، خلاصة القول", CONJUNCTION, C1, "formal", "zǒngzhī", trad = "總之")
        // Conditions and concessions
        v("wulun", "无论", "no matter (what, how, who)", "مهما، بغضّ النظر عن", CONJUNCTION, C1, "conditions", "wúlùn", trad = "無論",
            ex = "无论天气怎么样，他都去跑步。", exEn = "Whatever the weather is like, he goes running.", exAr = "مهما كان الطقس، يذهب للركض.")
        v("jishi", "即使", "even if", "حتى لو", CONJUNCTION, C1, "conditions", "jíshǐ",
            ex = "即使你不同意，我也要试一试。", exEn = "Even if you don't agree, I'm going to give it a try.", exAr = "حتى لو لم توافق، سأجرّب.")
        v("zhiyao", "只要", "as long as", "ما دام، بشرط أن", CONJUNCTION, C1, "conditions", "zhǐyào")
        v("zhiyou", "只有", "only if, only when", "لا… إلا إذا", CONJUNCTION, C1, "conditions", "zhǐyǒu")
        v("buguan", "不管", "no matter (more colloquial than 无论)", "مهما (أقرب إلى العامية من 无论)", CONJUNCTION, C1, "conditions", "bùguǎn")
        v("tiaojian", "条件", "condition, circumstances", "شرط، ظروف", NOUN, C1, "conditions", "tiáojiàn", trad = "條件")
        // Four-character idioms
        v("yijuliangde", "一举两得", "to gain two things with one action", "يضرب عصفورين بحجر", PHRASE, C1, "idioms", "yì jǔ liǎng dé", trad = "一舉兩得",
            ex = "骑车上班又能锻炼又能省钱，真是一举两得。", exEn = "Cycling to work is exercise and saves money: two birds with one stone.", exAr = "ركوب الدراجة إلى العمل رياضة وتوفير للمال، فهو ضرب لعصفورين بحجر.")
        v("bantuerfei", "半途而废", "to give up halfway", "يتخلّى عن الأمر في منتصف الطريق", PHRASE, C1, "idioms", "bàn tú ér fèi", trad = "半途而廢")
        v("ruxiangsuisu", "入乡随俗", "when in a new place, follow its customs", "يتّبع عادات البلد الذي يحلّ فيه", PHRASE, C1, "idioms", "rù xiāng suí sú", trad = "入鄉隨俗")
        v("zixiangmaodun", "自相矛盾", "to contradict oneself", "يناقض نفسه", PHRASE, C1, "idioms", "zì xiāng máo dùn")
        v("juyifansan", "举一反三", "to work out other cases from one example", "يقيس على المثال الواحد غيرَه", PHRASE, C1, "idioms", "jǔ yī fǎn sān", trad = "舉一反三")
        v("sanxinheryi", "三心二意", "half-hearted, unable to focus or decide", "متردّد، مشتّت الذهن", PHRASE, C1, "idioms", "sān xīn èr yì")
        // Rhetorical questions and attitude adverbs
        v("nandao", "难道", "(rhetorical question) surely not…? do you really mean…?", "أَوَ…؟ أيُعقل أنّ…؟ (استفهام إنكاري)", ADVERB, C1, "attitude", "nándào", trad = "難道",
            ex = "难道你不知道明天有考试吗？", exEn = "Don't you know there's an exam tomorrow?", exAr = "ألا تعرف أن هناك امتحانًا غدًا؟")
        v("hebi", "何必", "why bother; there is no need", "ما الداعي إلى", ADVERB, C1, "attitude", "hébì")
        v("jingran", "竟然", "to one's surprise, unexpectedly", "يا للعجب، على غير المتوقَّع", ADVERB, C1, "attitude", "jìngrán",
            ex = "他平时很少学习，这次竟然考了第一名。", exEn = "He hardly ever studies, yet this time he came first, of all things.", exAr = "نادرًا ما يدرس، ومع ذلك حلّ هذه المرة في المركز الأول، يا للعجب.")
        v("guoran", "果然", "just as expected", "كما كان متوقَّعًا فعلًا", ADVERB, C1, "attitude", "guǒrán")
        v("bujin", "不禁", "cannot help (doing)", "لا يملك إلا أن", ADVERB, C1, "attitude", "bùjīn")
        v("fanzheng", "反正", "anyway, in any case", "على أي حال", ADVERB, C1, "attitude", "fǎnzhèng")
    }

    fun mandarinGrammar(): List<GrammarTopic> = listOf(
        grammar("formal_connectors", ZH, C1, g("Written connectors: 因此, 然而, 从而, 此外, 尽管", "أدوات الربط في الكتابة: 因此 و然而 و从而 و此外 و尽管"),
            g("Spoken Mandarin links ideas with 所以, 但是 and 还有. News, essays and reports prefer more formal words: 因此 (therefore), 然而 (however, at the start of a sentence), 从而 (thereby, linking a means to its result), 此外 (in addition) and 尽管…还是/却… (even though). 总之 sums up at the end. Using them in conversation can sound stiff; using 所以 everywhere in an essay sounds too casual.",
                "تربط الصينية المحكية الأفكار بـ 所以 و但是 و还有. أما الأخبار والمقالات والتقارير فتفضّل كلمات أكثر رسمية: 因此 (لذلك)، و然而 (غير أنّ، في أول الجملة)، و从而 (وبذلك، تربط الوسيلة بنتيجتها)، و此外 (إضافةً إلى ذلك)، و尽管…还是/却… (على الرغم من أنّ). و总之 للخلاصة في النهاية. واستخدامها في المحادثة قد يبدو متكلّفًا، أما تكرار 所以 في مقال فيبدو غير رسمي."),
            listOf(ex("城市修了很多地铁，从而减少了交通堵塞。", "The city built many metro lines, thereby reducing traffic jams.", "بنت المدينة خطوط مترو كثيرة، وبذلك خفّفت الازدحام المروري."),
                ex("这个办法很好。然而，它的成本太高了。", "This method is good. However, it costs too much.", "هذه الطريقة جيدة. غير أن تكلفتها مرتفعة جدًا."),
                ex("尽管他很累，因此他还是来了。", "尽管 pairs with 还是/却, not 因此", "‏尽管 تقترن بـ 还是/却 لا بـ 因此", wrong = true)),
            listOf(g("从而 introduces a result brought about by the action in the first part, usually done by the same subject; it does not just mean 'so': ✗ 下雨了，从而我没去 → ✓ 下雨了，所以我没去.", "‏从而 تقدّم نتيجة يُحدثها الفعل المذكور في الجزء الأول، وغالبًا ما يكون الفاعل واحدًا، ولا تعني مجرد «لذلك»: ✗ 下雨了，从而我没去 ← ✓ 下雨了，所以我没去."))),
        grammar("conditions", ZH, C1, g("无论…都, 即使…也, 只要…就, 只有…才", "‏无论…都 و即使…也 و只要…就 و只有…才"),
            g("These pairs are a core of fluent Mandarin. 无论/不管 + question word or A还是B … 都 … means 'no matter': 无论谁来，我都欢迎. 即使 … 也 … means 'even if': 即使很贵，我也要买. 只要 … 就 … means 'as long as' (a sufficient condition): 只要努力，就会进步. 只有 … 才 … means 'only if' (the one necessary condition): 只有多练，才能说好. Mixing 只要 with 才 or 只有 with 就 is a common mistake.",
                "هذه الأزواج من أسس الصينية الطليقة. ‏无论/不管 + أداة استفهام أو A还是B … 都 … تعني «مهما»: 无论谁来，我都欢迎. و即使 … 也 … تعني «حتى لو»: 即使很贵，我也要买. و只要 … 就 … تعني «ما دام» (شرط كافٍ): 只要努力，就会进步. و只有 … 才 … تعني «لا… إلا إذا» (الشرط الوحيد اللازم): 只有多练，才能说好. والخلط بين 只要 و才 أو بين 只有 و就 خطأ شائع."),
            listOf(ex("不管多忙，她每天都给妈妈打电话。", "However busy she is, she calls her mother every day.", "مهما كانت مشغولة، تتصل بأمها كل يوم."),
                ex("只有通过考试，才能拿到毕业证。", "Only by passing the exam can you get the diploma.", "لا يمكنك الحصول على شهادة التخرّج إلا إذا نجحت في الامتحان."),
                ex("无论天气很好，我们都去。", "无论 needs a question word or a choice: 无论天气好不好", "يحتاج 无论 إلى أداة استفهام أو خيارين: 无论天气好不好", wrong = true)),
            listOf(g("Forgetting 都 after 无论: ✗ 无论你说什么，我不听 → ✓ 无论你说什么，我都不听.", "نسيان 都 بعد 无论: ✗ 无论你说什么，我不听 ← ✓ 无论你说什么，我都不听."))),
        grammar("chengyu", ZH, C1, g("Four-character idioms (成语)", "التعابير الرباعية (成语)"),
            g("成语 are fixed four-character expressions, many from old stories. Educated speakers use them often in speech and even more in writing; knowing the common ones is a mark of a high level. Learn each one as a whole with its story, its meaning and how it is used: some act like verbs (他做事总是半途而废), some like adjectives or comments (真是一举两得). Do not change the characters or their order.",
                "‏成语 تعابير ثابتة من أربعة حروف، وكثير منها مأخوذ من قصص قديمة. يستخدمها المتعلّمون كثيرًا في الكلام وأكثر في الكتابة، ومعرفة الشائع منها علامة على مستوى عالٍ. تعلّم كل تعبير كاملًا مع قصته ومعناه وطريقة استخدامه: بعضها يعمل عمل الفعل (他做事总是半途而废)، وبعضها عمل الصفة أو التعليق (真是一举两得). ولا تغيّر حروفها أو ترتيبها."),
            listOf(ex("在国外生活要入乡随俗。", "When you live abroad, you should follow local customs.", "حين تعيش في الخارج عليك أن تتّبع عادات أهل البلد."),
                ex("他说的话前后自相矛盾。", "What he said contradicts itself.", "كلامه يناقض بعضه بعضًا."),
                ex("学习不能半途而停。", "The idiom is fixed: 半途而废", "التعبير ثابت: 半途而废", wrong = true)),
            listOf(g("Overusing idioms in casual chat can sound showy; one well-chosen 成语 is better than three.", "الإكثار من التعابير في الحديث العادي قد يبدو تكلّفًا، وتعبير واحد في مكانه خير من ثلاثة."),
                g("自相矛盾 comes from the story of a man selling a spear (矛) that pierces any shield and a shield (盾) no spear can pierce.", "أصل 自相矛盾 قصة رجل يبيع رمحًا (矛) يخترق كل درع، ودرعًا (盾) لا يخترقه أي رمح."))),
        grammar("rhetorical", ZH, C1, g("Rhetorical questions and attitude: 难道, 何必, 竟然, 果然", "الاستفهام الإنكاري والموقف: 难道 و何必 و竟然 و果然"),
            g("Native speakers show feelings with small adverbs. 难道…吗？ asks a question whose answer is obvious, to show surprise or criticism: 难道你忘了吗？ (Surely you haven't forgotten?). 何必…呢？ means 'why bother': 何必生气呢？ 竟然 marks something surprising: 他竟然走路来了 (He actually walked here!). 果然 marks something that happened just as expected. 反正 means 'anyway, whatever happens'.",
                "يعبّر المتحدثون الأصليون عن مشاعرهم بظروف صغيرة. ‏难道…吗？ سؤال جوابه واضح، للدلالة على الدهشة أو الانتقاد: 难道你忘了吗？ (أيُعقل أنك نسيت؟). و何必…呢？ تعني «ما الداعي إلى»: 何必生气呢？ و竟然 تدل على أمر مفاجئ: 他竟然走路来了 (جاء ماشيًا، يا للعجب!). و果然 تدل على أمر حدث كما كان متوقعًا. و反正 تعني «على أي حال، مهما حدث»."),
            listOf(ex("天气预报说今天有雨，下午果然下起雨来了。", "The forecast said it would rain, and sure enough it started raining in the afternoon.", "قالت النشرة الجوية إنها ستمطر، وفعلًا بدأ المطر بعد الظهر."),
                ex("反正时间还早，我们走着去吧。", "It's still early anyway, so let's walk.", "الوقت ما زال مبكرًا على أي حال، فلنذهب مشيًا."),
                ex("难道你不知道这件事。", "A 难道 question ends with 吗 or a question mark", "ينتهي سؤال 难道 بـ 吗 أو بعلامة استفهام", wrong = true)),
            listOf(g("Confusing 竟然 (surprising) and 果然 (as expected): they mean opposite things.", "الخلط بين 竟然 (مفاجئ) و果然 (كما هو متوقع): معناهما متعاكسان."))),
    )

    fun mandarinLessons(f: LessonFactory): List<Lesson> = listOf(
        f.lesson("formal_connectors", C1, "c1_skills", g("Writing like a newspaper", "الكتابة بأسلوب الصحف"), g("Link ideas formally with 因此, 然而, 从而, 此外 and 尽管.", "اربط الأفكار بأسلوب رسمي باستخدام 因此 و然而 و从而 و此外 و尽管."),
            Skill.WRITING, listOf("yinci", "raner", "conger", "ciwai", "jinguan", "zongzhi"), "formal_connectors",
            extra = listOf(
                fill("fc1", ZH, "他每天坚持练习，___进步得很快。", listOf("因此", "所以")),
                choice("fc2", ZH, "However, the result was not ideal:", listOf("然而，结果并不理想。", "因此，结果并不理想。", "此外，结果并不理想。"), 0),
                choice("fc3", ZH, "公司改进了服务，___吸引了更多顾客。", listOf("从而", "然而", "尽管"), 0,
                    explanation = g("从而 links a means to its result.", "‏从而 تربط الوسيلة بنتيجتها.")),
                order("fc4", ZH, listOf("尽管", "工作", "很忙", "他", "还是", "每天", "学习"), g("Even though work is busy, he still studies every day.", "على الرغم من انشغاله في العمل، ما زال يدرس كل يوم."), separator = ""),
                writing("fc5", ZH, g("Write a short report (80+ characters) on the advantages and disadvantages of online shopping. Use 此外, 然而 and 总之.", "اكتب تقريرًا قصيرًا (80 حرفًا على الأقل) عن مزايا التسوّق عبر الإنترنت وعيوبه. استخدم 此外 و然而 و总之."), 80,
                    listOf("此外", "然而", "总之")),
            ), prerequisites = listOf("not_only")),
        f.lesson("conditions", C1, "c1_skills", g("No matter, even if, only if", "مهما وحتى لو ولا… إلا إذا"), g("Express conditions precisely with 无论…都, 即使…也, 只要…就 and 只有…才.", "عبّر عن الشروط بدقة باستخدام 无论…都 و即使…也 و只要…就 و只有…才."),
            Skill.GRAMMAR, listOf("wulun", "jishi", "zhiyao", "zhiyou", "buguan", "tiaojian"), "conditions",
            extra = listOf(
                fill("cd1", ZH, "无论多忙，他___坚持锻炼。", listOf("都")),
                fill("cd2", ZH, "即使明天下雨，我们___要去。", listOf("也")),
                choice("cd3", ZH, "As long as you keep at it, you will improve:", listOf("只要坚持，就会进步。", "只有坚持，就会进步。", "即使坚持，就会进步。"), 0),
                choice("cd4", ZH, "Only by practising every day can you speak fluently:", listOf("只有每天练习，才能说得流利。", "只要每天练习，才能说得流利。", "只有每天练习，就能说得流利。"), 0),
                translate("cd5", ZH, g("No matter who comes, we welcome them.", "مهما كان القادم، نرحّب به."), listOf("无论谁来，我们都欢迎", "不管谁来，我们都欢迎", "无论谁来我们都欢迎", "不管谁来我们都欢迎")),
            ), prerequisites = listOf("formal_connectors")),
        f.lesson("chengyu", C1, "c1_skills", g("Idioms native speakers use", "تعابير يستخدمها أهل اللغة"), g("Understand and use common four-character idioms (成语).", "افهم التعابير الرباعية الشائعة (成语) واستخدمها."),
            Skill.READING, listOf("yijuliangde", "bantuerfei", "ruxiangsuisu", "zixiangmaodun", "juyifansan", "sanxinheryi"), "chengyu",
            extra = listOf(
                choice("cy1", ZH, "他学了三个月钢琴就不学了，真是___。", listOf("半途而废", "一举两得", "入乡随俗"), 0, skill = Skill.READING),
                choice("cy2", ZH, "骑车上班又锻炼身体又省钱，真是___。", listOf("一举两得", "自相矛盾", "三心二意"), 0, skill = Skill.READING),
                choice("cy3", ZH, "他一会儿说喜欢，一会儿说不喜欢，说话___。", listOf("自相矛盾", "举一反三", "入乡随俗"), 0, skill = Skill.READING),
                translate("cy4", ZH, g("When in Rome, do as the Romans do.", "إذا حللت ببلد فاتّبع عادات أهله."), listOf("入乡随俗")),
                writing("cy5", ZH, g("Tell a short story about learning something (60+ characters) and finish it with a fitting 成语.", "احكِ قصة قصيرة عن تعلّم شيء ما (60 حرفًا على الأقل) واختمها بتعبير 成语 مناسب."), 60,
                    listOf("半途而废", "一举两得", "举一反三")),
            ), prerequisites = listOf("conditions")),
        f.lesson("rhetorical", C1, "c1_skills", g("Surely not? Sounding like a native", "أيُعقل؟ التحدّث كأهل اللغة"), g("Show surprise, criticism and attitude with 难道, 何必, 竟然, 果然 and 反正.", "عبّر عن الدهشة والانتقاد والموقف باستخدام 难道 و何必 و竟然 و果然 و反正."),
            Skill.SPEAKING, listOf("nandao", "hebi", "jingran", "guoran", "bujin", "fanzheng"), "rhetorical",
            extra = listOf(
                fill("rh1", ZH, "___你不知道明天有考试吗？", listOf("难道")),
                choice("rh2", ZH, "他平时很少学习，这次___考了第一名。", listOf("竟然", "果然", "反正"), 0,
                    explanation = g("竟然 = to everyone's surprise.", "‏竟然 = على غير المتوقَّع.")),
                choice("rh3", ZH, "天气预报说今天下雨，下午___下雨了。", listOf("果然", "竟然", "何必"), 0),
                translate("rh4", ZH, g("Why bother getting angry?", "ما الداعي إلى الغضب؟"), listOf("何必生气呢", "何必生气呢？", "何必生气")),
                dictation("rh5", ZH, "反正时间还早，我们走着去吧", listOf("反正时间还早，我们走着去吧。", "反正时间还早我们走着去吧")),
            ), prerequisites = listOf("chengyu")),
    )

    fun mandarinUnits(): List<CourseUnit> = listOf(
        CourseUnit("zh_u_c1_skills", ZH, C1, g("Formal style, conditions and idioms", "الأسلوب الرسمي والشروط والتعابير"), g("Written connectors, 无论/即使/只要/只有, 成语 and rhetorical questions", "أدوات الربط الكتابية و无论/即使/只要/只有 و成语 والاستفهام الإنكاري"),
            listOf("zh_l_formal_connectors", "zh_l_conditions", "zh_l_chengyu", "zh_l_rhetorical")),
    )

    // ---------------- Readings ----------------

    private class Line(val text: String, val en: String, val ar: String)

    private fun l(text: String, en: String, ar: String) = Line(text, en, ar)
    private fun w(term: String, en: String, ar: String, reading: String? = null) = GlossaryEntry(term, g(en, ar), reading)
    private fun q(id: String, prompt: String, options: List<String>, answer: Int, gloss: com.polyglot.academy.core.model.Gloss? = null, optionGlosses: List<com.polyglot.academy.core.model.Gloss>? = null) =
        Exercise.MultipleChoice(id, Skill.READING, Instr.choose, prompt, options, answer, promptGloss = gloss, optionGlosses = optionGlosses)
    private fun o(id: String, prompt: String, en: String, ar: String, model: String, vararg points: String) =
        OpenQuestion(id, prompt, g(en, ar), model, points.toList())
    private fun retell(id: String, lang: LanguageCode, model: String, vararg points: String) = OpenQuestion(
        id, if (lang == ZH) "用三四句话概括这篇文章的主要内容。" else "Summarise the main points of the text in three or four sentences.",
        g("Summarise the main points of the text in three or four sentences.", "لخّص الأفكار الرئيسية للنص في ثلاث جمل أو أربع."), model, points.toList(), summary = true,
    )
    private fun story(
        id: String, lang: LanguageCode, level: CefrLevel, title: String, en: String, ar: String,
        lines: List<Line>, words: List<GlossaryEntry>, qs: List<Exercise.MultipleChoice>, open: List<OpenQuestion>,
    ) = ReadingPassage(id, lang, level, title, g(en, ar), lines.map { it.text }, lines.map { g(it.en, it.ar) }, emptyList(), qs, words, openQuestions = open)

    fun readings(lang: LanguageCode): List<ReadingPassage> = when (lang) {
        EN -> english
        ZH -> mandarin
        else -> emptyList()
    }

    private val english by lazy {
        listOf(
            story("en_st_four_day_week", EN, B2, "Is a four-day week the future?", "Is a four-day week the future?", "هل أسبوع العمل ذو الأيام الأربعة هو المستقبل؟", listOf(
                l("For most of the last century, a working week of five days and around forty hours was considered normal.", "For most of the last century, a working week of five days and around forty hours was considered normal.", "طوال معظم القرن الماضي كان أسبوع العمل المؤلف من خمسة أيام ونحو أربعين ساعة يُعدّ أمرًا طبيعيًا."),
                l("Recently, however, a growing number of companies have been experimenting with a four-day week, without cutting their employees' pay.", "Recently, however, a growing number of companies have been experimenting with a four-day week, without cutting their employees' pay.", "لكن عددًا متزايدًا من الشركات بدأ مؤخرًا يجرّب أسبوع عمل من أربعة أيام، دون تخفيض رواتب الموظفين."),
                l("The idea is simple: if people are rested and motivated, they can produce the same amount of work in less time.", "The idea is simple: if people are rested and motivated, they can produce the same amount of work in less time.", "والفكرة بسيطة: إذا كان الناس مرتاحين ومتحمّسين، يمكنهم إنجاز القدر نفسه من العمل في وقت أقل."),
                l("In several large trials, most of the companies involved reported that productivity stayed the same or even improved.", "In several large trials, most of the companies involved reported that productivity stayed the same or even improved.", "وفي عدة تجارب كبيرة أفادت معظم الشركات المشاركة بأن الإنتاجية بقيت كما هي أو تحسّنت."),
                l("Employees said they slept better, felt less stressed and had more time for their families and hobbies.", "Employees said they slept better, felt less stressed and had more time for their families and hobbies.", "وقال الموظفون إنهم صاروا ينامون أفضل ويشعرون بتوتر أقل ولديهم وقت أكثر لعائلاتهم وهواياتهم."),
                l("Fewer people took sick days, and it became easier for the companies to attract and keep talented staff.", "Fewer people took sick days, and it became easier for the companies to attract and keep talented staff.", "وقلّ عدد من أخذوا إجازات مرضية، وصار أسهل على الشركات جذب الموظفين الموهوبين والاحتفاظ بهم."),
                l("Critics point out, though, that the model does not suit every type of job.", "Critics point out, though, that the model does not suit every type of job.", "غير أن المنتقدين يشيرون إلى أن هذا النموذج لا يناسب كل أنواع الوظائف."),
                l("Hospitals, schools and shops need staff to be present every day, so they would have to hire more people.", "Hospitals, schools and shops need staff to be present every day, so they would have to hire more people.", "فالمستشفيات والمدارس والمتاجر تحتاج إلى حضور موظفيها كل يوم، ولذلك ستضطر إلى توظيف مزيد من الناس."),
                l("Some workers also found that squeezing five days of work into four made their days longer and more intense.", "Some workers also found that squeezing five days of work into four made their days longer and more intense.", "ووجد بعض العاملين أيضًا أن ضغط عمل خمسة أيام في أربعة جعل أيامهم أطول وأشدّ إرهاقًا."),
                l("What the trials do suggest is that the number of hours matters less than how those hours are organised.", "What the trials do suggest is that the number of hours matters less than how those hours are organised.", "وما تشير إليه التجارب فعلًا هو أن عدد الساعات أقل أهمية من طريقة تنظيمها."),
                l("Fewer meetings, clearer goals and fewer interruptions may help, whatever the length of the week.", "Fewer meetings, clearer goals and fewer interruptions may help, whatever the length of the week.", "فقد يساعد تقليل الاجتماعات ووضوح الأهداف وقلة المقاطعات، أيًّا كان طول الأسبوع."),
            ), listOf(
                w("considered", "considered, thought to be", "يُعدّ، يُعتبر"), w("growing number", "growing number", "عدد متزايد"), w("experimenting", "experimenting, trying", "يجرّب"),
                w("cutting", "cutting, reducing", "تخفيض"), w("rested", "rested", "مرتاح"), w("motivated", "motivated", "متحمّس، لديه دافع"), w("trials", "trials, tests", "تجارب"),
                w("involved", "involved, taking part", "مشارك"), w("productivity", "productivity", "إنتاجية"), w("stressed", "stressed", "متوتر"), w("sick days", "sick days", "إجازات مرضية"),
                w("attract", "attract", "يجذب"), w("talented", "talented", "موهوب"), w("staff", "staff", "موظفون"), w("Critics", "critics", "المنتقدون"), w("point out", "point out", "يشير إلى"),
                w("suit", "suit, be right for", "يناسب"), w("present", "present, there", "حاضر"), w("hire", "hire, employ", "يوظّف"), w("squeezing", "squeezing", "ضغط، حشر"),
                w("intense", "intense", "مكثّف، مرهق"), w("organised", "organised", "منظَّم"), w("interruptions", "interruptions", "مقاطعات"), w("whatever", "whatever, no matter what", "أيًّا كان"),
            ), listOf(
                q("en_st_four_day_week_q1", "What happened to productivity in most trials?", listOf("It fell sharply", "It stayed the same or improved", "It was not measured"), 1),
                q("en_st_four_day_week_q2", "Why might hospitals find the model difficult?", listOf("Staff must be present every day", "Doctors do not want it", "It is too expensive to measure"), 0),
                q("en_st_four_day_week_q3", "What is the main conclusion of the text?", listOf("Everyone should work four days", "The five-day week is best", "How hours are organised matters more than their number"), 2),
            ), listOf(
                o("en_st_four_day_week_o1", "What problem did some workers have with the four-day week?", "What problem did some workers have with the four-day week?", "ما المشكلة التي واجهها بعض العاملين في أسبوع الأيام الأربعة؟",
                    "Their days became longer and more intense because they had to do five days of work in four.", "longer|long", "intense|tired|stress|hard"),
                retell("en_st_four_day_week_o2", EN, "Many companies have tried a four-day week without cutting pay. Productivity usually stayed the same, and workers were healthier and happier. However, it does not suit jobs like hospitals or shops, and some days became more intense. The way work is organised may matter more than the number of hours.",
                    "four", "productivity|productive", "suit|hospital|shop|not every", "organis|organiz|hours"),
            )),
            story("en_st_attention", EN, C1, "The battle for your attention", "The battle for your attention", "المعركة على انتباهك", listOf(
                l("It is often said that if you are not paying for a product, you are the product.", "It is often said that if you are not paying for a product, you are the product.", "كثيرًا ما يُقال إنك إذا لم تكن تدفع ثمن المنتج، فأنت المنتج."),
                l("Nowhere is this more true than in the world of free apps, whose income depends almost entirely on advertising.", "Nowhere is this more true than in the world of free apps, whose income depends almost entirely on advertising.", "ولا يصدق هذا القول في مكان أكثر من عالم التطبيقات المجانية، التي يعتمد دخلها كليًّا تقريبًا على الإعلانات."),
                l("The longer we scroll, the more adverts we see; what these companies are really selling, therefore, is our attention.", "The longer we scroll, the more adverts we see; what these companies are really selling, therefore, is our attention.", "فكلما طال تصفّحنا رأينا إعلانات أكثر، ولذلك فإن ما تبيعه هذه الشركات حقًا هو انتباهنا."),
                l("To keep us engaged, designers borrow techniques that psychologists have studied for decades.", "To keep us engaged, designers borrow techniques that psychologists have studied for decades.", "ولإبقائنا منشغلين يستعير المصمّمون أساليب درسها علماء النفس لعقود."),
                l("Notifications arrive at unpredictable moments, rather like the rewards of a slot machine, and feeds never come to a natural end.", "Notifications arrive at unpredictable moments, rather like the rewards of a slot machine, and feeds never come to a natural end.", "فالإشعارات تصل في لحظات لا يمكن توقّعها، تشبه إلى حدٍّ ما مكافآت آلة القمار، وصفحات المحتوى لا تصل أبدًا إلى نهاية طبيعية."),
                l("None of this is illegal, and arguably much of it simply reflects what users want.", "None of this is illegal, and arguably much of it simply reflects what users want.", "ولا شيء من هذا مخالف للقانون، بل يمكن القول إن كثيرًا منه يعكس ببساطة ما يريده المستخدمون."),
                l("Nevertheless, a growing body of research suggests that constant interruptions may come at a cost.", "Nevertheless, a growing body of research suggests that constant interruptions may come at a cost.", "ومع ذلك تشير أبحاث متزايدة إلى أن المقاطعات المستمرة قد يكون لها ثمن."),
                l("Studies have found that it can take several minutes to regain full concentration after checking a message, and that heavy users tend to report higher levels of anxiety.", "Studies have found that it can take several minutes to regain full concentration after checking a message, and that heavy users tend to report higher levels of anxiety.", "فقد وجدت دراسات أن استعادة التركيز الكامل بعد تفقّد رسالة قد تستغرق عدة دقائق، وأن كثيري الاستخدام يميلون إلى الإبلاغ عن مستويات أعلى من القلق."),
                l("It should be emphasised, however, that correlation is not the same as causation: anxious people may simply use their phones more.", "It should be emphasised, however, that correlation is not the same as causation: anxious people may simply use their phones more.", "غير أنه ينبغي التأكيد أن الارتباط ليس هو السببية، فربما يستخدم القلقون هواتفهم أكثر ببساطة."),
                l("Some governments are now considering rules that would limit features such as endless scrolling for children.", "Some governments are now considering rules that would limit features such as endless scrolling for children.", "وتدرس بعض الحكومات الآن قواعد تحدّ من خصائص مثل التصفح اللانهائي للأطفال."),
                l("Others argue that the solution lies not in regulation but in education, teaching people from an early age how these systems work.", "Others argue that the solution lies not in regulation but in education, teaching people from an early age how these systems work.", "ويرى آخرون أن الحل لا يكمن في التنظيم بل في التعليم، أي تعليم الناس منذ الصغر كيف تعمل هذه الأنظمة."),
                l("Whichever approach wins, the underlying question remains the same: who decides how we spend the hours of our day?", "Whichever approach wins, the underlying question remains the same: who decides how we spend the hours of our day?", "وأيًّا كان النهج الذي سيسود، يبقى السؤال الجوهري نفسه: من يقرّر كيف نقضي ساعات يومنا؟"),
            ), listOf(
                w("Nowhere", "nowhere, in no place", "لا… في أي مكان"), w("income", "income", "دخل"), w("entirely", "entirely, completely", "كليًّا"), w("advertising", "advertising", "الإعلان"),
                w("scroll", "scroll", "يتصفّح بالتمرير"), w("adverts", "adverts, ads", "إعلانات"), w("engaged", "engaged, interested and busy", "منشغل، متفاعل"), w("borrow", "borrow", "يستعير"),
                w("techniques", "techniques", "أساليب، تقنيات"), w("psychologists", "psychologists", "علماء النفس"), w("Notifications", "notifications", "إشعارات"), w("unpredictable", "unpredictable", "لا يمكن توقّعه"),
                w("rewards", "rewards", "مكافآت"), w("slot machine", "slot machine", "آلة قمار"), w("feeds", "feeds (of posts)", "صفحات المحتوى المتجدّد"), w("illegal", "illegal", "غير قانوني"),
                w("reflects", "reflects, shows", "يعكس"), w("body of research", "body of research", "مجموعة أبحاث"), w("constant", "constant", "مستمر"), w("come at a cost", "come at a cost", "يكون له ثمن"),
                w("regain", "regain, get back", "يستعيد"), w("concentration", "concentration", "تركيز"), w("anxiety", "anxiety", "قلق"), w("correlation", "correlation", "ارتباط (إحصائي)"),
                w("causation", "causation", "سببية"), w("anxious", "anxious", "قلق"), w("features", "features", "خصائص، ميزات"), w("endless", "endless", "لا نهائي"),
                w("lies", "lies (is found)", "يكمن"), w("regulation", "regulation", "تنظيم (بقوانين)"), w("approach", "approach", "نهج، أسلوب"),
            ), listOf(
                q("en_st_attention_q1", "According to the text, what do free apps really sell?", listOf("Our personal photos", "Our attention", "Their software"), 1),
                q("en_st_attention_q2", "Why does the writer mention a slot machine?", listOf("Notifications are unpredictable rewards", "Apps are a form of gambling", "Both cost money"), 0),
                q("en_st_attention_q3", "What does the writer warn about the research on anxiety?", listOf("It was paid for by app companies", "It is too old", "It shows a link, not necessarily a cause"), 2),
            ), listOf(
                o("en_st_attention_o1", "What two solutions does the text mention, and which do you find more convincing? Why?", "What two solutions does the text mention, and which do you find more convincing? Why?", "ما الحلّان اللذان يذكرهما النص، وأيهما تجده أكثر إقناعًا؟ ولماذا؟",
                    "The text mentions rules that limit features such as endless scrolling for children, and education about how the systems work. I find education more convincing because people can then make their own choices.", "rule|regulat|law|limit", "educat|teach|school"),
                retell("en_st_attention_o2", EN, "Free apps earn money from adverts, so they try to keep our attention for as long as possible, using techniques like unpredictable notifications and endless feeds. Research suggests constant interruptions may harm concentration and be linked to anxiety, although this does not prove a cause. Some want new rules, while others prefer education.",
                    "advert|advertising", "attention", "concentration|anxiety|interrupt", "rule|regulat|educat"),
            )),
            story("en_st_lost_languages", EN, C1, "When a language disappears", "When a language disappears", "حين تختفي لغة", listOf(
                l("Of the roughly seven thousand languages spoken in the world today, linguists estimate that around half may no longer be spoken by the end of this century.", "Of the roughly seven thousand languages spoken in the world today, linguists estimate that around half may no longer be spoken by the end of this century.", "من بين نحو سبعة آلاف لغة يُتحدّث بها في العالم اليوم، يقدّر اللغويون أن نصفها تقريبًا قد لا يبقى متداولًا بحلول نهاية هذا القرن."),
                l("A language rarely dies suddenly; far more often, it fades over two or three generations.", "A language rarely dies suddenly; far more often, it fades over two or three generations.", "ونادرًا ما تموت اللغة فجأة، بل الأغلب أنها تذبل على مدى جيلين أو ثلاثة."),
                l("Parents who believe that a national or global language offers their children better opportunities may stop using their own at home.", "Parents who believe that a national or global language offers their children better opportunities may stop using their own at home.", "فالآباء الذين يرون أن لغة وطنية أو عالمية تتيح لأبنائهم فرصًا أفضل قد يتوقفون عن استخدام لغتهم في البيت."),
                l("Their children may still understand it, but they answer in the dominant language, and their grandchildren hardly know it at all.", "Their children may still understand it, but they answer in the dominant language, and their grandchildren hardly know it at all.", "وقد يظل أبناؤهم يفهمونها لكنهم يجيبون باللغة السائدة، أما أحفادهم فبالكاد يعرفونها."),
                l("What is lost is not merely a set of words, but a particular way of seeing the world.", "What is lost is not merely a set of words, but a particular way of seeing the world.", "وما يضيع ليس مجرد مجموعة من الكلمات، بل طريقة خاصة في رؤية العالم."),
                l("Some languages have dozens of terms for local plants and their medical uses, knowledge that has never been written down anywhere else.", "Some languages have dozens of terms for local plants and their medical uses, knowledge that has never been written down anywhere else.", "فبعض اللغات فيها عشرات الألفاظ للنباتات المحلية واستخداماتها الطبية، وهي معرفة لم تُدوَّن في أي مكان آخر."),
                l("Others describe directions not as left and right but in relation to the sea or the mountains, which appears to shape how their speakers remember space.", "Others describe directions not as left and right but in relation to the sea or the mountains, which appears to shape how their speakers remember space.", "وبعضها يصف الاتجاهات لا باليمين واليسار بل بالنسبة إلى البحر أو الجبال، ويبدو أن ذلك يؤثّر في طريقة تذكّر متحدثيها للمكان."),
                l("Not every story is one of decline, however.", "Not every story is one of decline, however.", "غير أن القصص ليست كلها قصص تراجع."),
                l("Welsh, Maori and Hebrew are often cited as examples of languages that were brought back into daily use through schools, media and sheer determination.", "Welsh, Maori and Hebrew are often cited as examples of languages that were brought back into daily use through schools, media and sheer determination.", "فكثيرًا ما تُذكر الويلزية والماورية والعبرية أمثلةً على لغات أُعيدت إلى الاستخدام اليومي بفضل المدارس ووسائل الإعلام والإصرار الخالص."),
                l("Technology, which is often blamed for spreading a few global languages, can also help: recordings, online dictionaries and apps allow scattered speakers to stay in touch.", "Technology, which is often blamed for spreading a few global languages, can also help: recordings, online dictionaries and apps allow scattered speakers to stay in touch.", "والتقنية، التي كثيرًا ما تُلام على نشر عدد قليل من اللغات العالمية، يمكن أن تساعد أيضًا: فالتسجيلات والقواميس الإلكترونية والتطبيقات تتيح للمتحدثين المتفرّقين أن يبقوا على تواصل."),
                l("Experts agree, nevertheless, that no app can replace the most crucial factor of all: children hearing the language at home.", "Experts agree, nevertheless, that no app can replace the most crucial factor of all: children hearing the language at home.", "ومع ذلك يتفق الخبراء على أن أي تطبيق لا يمكن أن يحلّ محل العامل الأكثر حسمًا: أن يسمع الأطفال اللغة في البيت."),
            ), listOf(
                w("roughly", "roughly, about", "تقريبًا"), w("linguists", "linguists", "علماء اللغة"), w("estimate", "estimate", "يقدّر"), w("fades", "fades, slowly disappears", "يذبل، يتلاشى"),
                w("generations", "generations", "أجيال"), w("national", "national", "وطني"), w("global", "global", "عالمي"), w("opportunities", "opportunities", "فرص"),
                w("dominant", "dominant", "سائد، مهيمن"), w("grandchildren", "grandchildren", "أحفاد"), w("merely", "merely, only", "مجرّد"), w("particular", "particular, special", "خاص، معيّن"),
                w("dozens", "dozens", "عشرات"), w("terms", "terms, words", "ألفاظ، مصطلحات"), w("medical", "medical", "طبي"), w("in relation to", "in relation to", "بالنسبة إلى"),
                w("shape", "shape, influence", "يشكّل، يؤثّر في"), w("decline", "decline", "تراجع"), w("cited", "cited, mentioned as an example", "يُستشهد به"), w("media", "media", "وسائل الإعلام"),
                w("sheer", "sheer, pure", "خالص، محض"), w("determination", "determination", "إصرار، عزيمة"), w("blamed", "blamed", "يُلام"), w("spreading", "spreading", "نشر"),
                w("recordings", "recordings", "تسجيلات"), w("scattered", "scattered, living far apart", "متفرّق، مشتّت"), w("crucial", "crucial", "حاسم"), w("factor", "factor", "عامل"),
            ), listOf(
                q("en_st_lost_languages_q1", "How does a language usually disappear, according to the text?", listOf("Suddenly, because of a war", "Slowly, over two or three generations", "When it is banned by law"), 1),
                q("en_st_lost_languages_q2", "Why are Welsh, Maori and Hebrew mentioned?", listOf("They are the oldest languages", "They have no written form", "They were brought back into daily use"), 2),
                q("en_st_lost_languages_q3", "What do experts see as the most important factor?", listOf("Children hearing the language at home", "Better apps", "Government money"), 0),
            ), listOf(
                o("en_st_lost_languages_o1", "Why might parents stop speaking their own language to their children?", "Why might parents stop speaking their own language to their children?", "لماذا قد يتوقف الآباء عن التحدث بلغتهم مع أبنائهم؟",
                    "Because they believe a national or global language will give their children better opportunities.", "opportunit|job|future|better", "national|global|dominant|english"),
                retell("en_st_lost_languages_o2", EN, "About half of the world's languages may disappear this century, usually over a few generations as parents switch to a dominant language. Losing a language means losing knowledge and a way of seeing the world. Some languages have been revived, and technology can help, but children hearing the language at home matters most.",
                    "half|disappear|die", "generation|parent|children", "knowledge|world|way of seeing", "revive|brought back|Welsh|Hebrew|technology"),
            )),
        )
    }

    private val mandarin by lazy {
        listOf(
            story("zh_st_gaotie", ZH, B2, "高铁改变了我们的生活", "High-speed rail changed our lives", "القطار السريع غيّر حياتنا", listOf(
                l("十几年前，从北京到上海坐火车要十几个小时，很多人宁可坐飞机。", "Ten-odd years ago, the train from Beijing to Shanghai took over ten hours, and many people preferred to fly.", "قبل عشر سنوات ونيّف كانت الرحلة بالقطار من بكين إلى شانغهاي تستغرق أكثر من عشر ساعات، وكان كثيرون يفضّلون الطائرة."),
                l("现在坐高铁只需要四个多小时，而且车站一般在市中心，非常方便。", "Now the high-speed train takes only a little over four hours, and the stations are usually in the city centre, which is very convenient.", "أما الآن فلا يستغرق القطار السريع إلا أكثر من أربع ساعات بقليل، والمحطات في الغالب في وسط المدينة، وهذا مريح جدًا."),
                l("随着高铁网络越来越大，人们的生活方式也发生了很大的变化。", "As the high-speed rail network has grown, people's way of life has also changed a great deal.", "ومع اتساع شبكة القطارات السريعة تغيّر أسلوب حياة الناس كثيرًا أيضًا."),
                l("有的人在一个城市工作，在另一个城市生活，每天坐高铁上下班。", "Some people work in one city and live in another, commuting by high-speed train every day.", "فبعض الناس يعملون في مدينة ويعيشون في أخرى، ويتنقّلون إلى العمل بالقطار السريع كل يوم."),
                l("周末的时候，很多家庭会去附近的城市旅游，当天就能回来。", "At weekends, many families take trips to nearby cities and can be back the same day.", "وفي عطلات نهاية الأسبوع تسافر عائلات كثيرة إلى المدن القريبة للسياحة وتعود في اليوم نفسه."),
                l("一些小城市因为通了高铁，吸引了更多的游客和投资。", "Some small cities have attracted more tourists and investment because the high-speed rail reached them.", "وجذبت بعض المدن الصغيرة سيّاحًا واستثمارات أكثر لأن القطار السريع وصل إليها."),
                l("不过，高铁也带来了一些问题。", "However, high-speed rail has also brought some problems.", "لكن القطار السريع جلب أيضًا بعض المشكلات."),
                l("修建高铁需要大量的资金，有些线路坐的人不多，很难收回成本。", "Building high-speed lines takes huge amounts of money, and some lines carry few passengers, so it is hard to recover the costs.", "فبناء خطوطه يحتاج إلى أموال طائلة، وبعض الخطوط قليلة الركّاب فيصعب استرداد تكاليفها."),
                l("另外，高铁票比普通火车票贵，对收入低的人来说不太容易接受。", "Also, high-speed tickets cost more than ordinary train tickets, which is hard for people on low incomes.", "إضافة إلى ذلك فتذاكره أغلى من تذاكر القطار العادي، وهذا ليس سهلًا على ذوي الدخل المنخفض."),
                l("总的来说，大多数人认为高铁让中国变“小”了，人和人之间的距离也近了。", "On the whole, most people feel high-speed rail has made China \"smaller\" and brought people closer together.", "وعمومًا يرى معظم الناس أن القطار السريع جعل الصين «أصغر» وقرّب المسافة بين الناس."),
            ), listOf(
                w("十几年", "ten-odd years", "عشر سنوات ونيّف", "shí jǐ nián"), w("宁可", "would rather", "يفضّل أن", "nìngkě"), w("高铁", "high-speed rail", "القطار السريع", "gāotiě"),
                w("只需要", "only needs", "لا يحتاج إلا", "zhǐ xūyào"), w("车站", "station", "محطة", "chēzhàn"), w("市中心", "city centre", "وسط المدينة", "shì zhōngxīn"),
                w("随着", "along with, as", "مع، بالتزامن مع", "suízhe"), w("网络", "network", "شبكة", "wǎngluò"), w("生活方式", "way of life", "أسلوب الحياة", "shēnghuó fāngshì"),
                w("发生", "to happen, to take place", "يحدث", "fāshēng"), w("上下班", "to commute to and from work", "يذهب إلى العمل ويعود", "shàng xià bān"), w("家庭", "family, household", "عائلة، أسرة", "jiātíng"),
                w("附近", "nearby", "قريب", "fùjìn"), w("当天", "the same day", "في اليوم نفسه", "dàngtiān"), w("通", "to connect, to reach", "يصل، يتّصل", "tōng"),
                w("吸引", "to attract", "يجذب", "xīyǐn"), w("游客", "tourist", "سائح", "yóukè"), w("投资", "investment", "استثمار", "tóuzī"), w("带来", "to bring", "يجلب", "dàilái"),
                w("修建", "to build (roads, railways)", "يبني، ينشئ", "xiūjiàn"), w("大量", "a large amount", "كمّية كبيرة", "dàliàng"), w("资金", "funds", "أموال، تمويل", "zījīn"),
                w("线路", "line, route", "خط", "xiànlù"), w("收回", "to recover, to get back", "يسترد", "shōuhuí"), w("成本", "cost", "تكلفة", "chéngběn"), w("另外", "in addition", "إضافة إلى ذلك", "lìngwài"),
                w("普通", "ordinary", "عادي", "pǔtōng"), w("收入", "income", "دخل", "shōurù"), w("接受", "to accept", "يتقبّل", "jiēshòu"), w("总的来说", "on the whole", "عمومًا", "zǒng de lái shuō"),
                w("距离", "distance", "مسافة", "jùlí"),
            ), listOf(
                q("zh_st_gaotie_q1", "现在从北京到上海坐高铁要多长时间？", listOf("十几个小时", "四个多小时", "两个小时"), 1, g("How long does the high-speed train from Beijing to Shanghai take now?", "كم تستغرق الرحلة بالقطار السريع من بكين إلى شانغهاي الآن؟"),
                    listOf(g("over ten hours", "أكثر من عشر ساعات"), g("a little over four hours", "أكثر من أربع ساعات بقليل"), g("two hours", "ساعتان"))),
                q("zh_st_gaotie_q2", "小城市通了高铁以后有什么变化？", listOf("人越来越少", "吸引了更多游客和投资", "火车票更便宜了"), 1, g("What changed in small cities after the high-speed rail arrived?", "ما الذي تغيّر في المدن الصغيرة بعد وصول القطار السريع؟"),
                    listOf(g("fewer and fewer people", "قلّ عدد الناس"), g("more tourists and investment", "سيّاح واستثمارات أكثر"), g("train tickets got cheaper", "صارت التذاكر أرخص"))),
                q("zh_st_gaotie_q3", "下面哪一个是文章提到的问题？", listOf("车站离市中心太远", "高铁太慢", "有些线路很难收回成本"), 2, g("Which of these problems does the text mention?", "أي من هذه المشكلات يذكرها النص؟"),
                    listOf(g("stations are far from the centre", "المحطات بعيدة عن الوسط"), g("the trains are too slow", "القطارات بطيئة جدًا"), g("some lines can hardly recover their costs", "بعض الخطوط يصعب استرداد تكاليفها"))),
            ), listOf(
                o("zh_st_gaotie_o1", "为什么有的人不太容易接受高铁？", "Why do some people find high-speed rail hard to accept?", "لماذا لا يتقبّل بعض الناس القطار السريع بسهولة؟", "因为高铁票比普通火车票贵，收入低的人觉得太贵了。", "贵", "收入"),
                retell("zh_st_gaotie_o2", ZH, "高铁让旅行变得更快更方便，人们可以在一个城市工作，在另一个城市生活。小城市也吸引了更多游客和投资。但是修建高铁需要很多钱，票也比较贵。", "快|方便", "工作|生活|旅游", "钱|贵|成本"),
            )),
            story("zh_st_fanyi", ZH, C1, "有了翻译软件，还要学外语吗？", "With translation apps, do we still need to learn languages?", "مع وجود برامج الترجمة، هل ما زلنا بحاجة إلى تعلّم اللغات؟", listOf(
                l("近年来，人工智能翻译的水平提高得非常快，很多人因此开始怀疑学外语还有没有必要。", "In recent years machine translation has improved very fast, and many people have therefore begun to doubt whether learning a foreign language is still necessary.", "في السنوات الأخيرة تحسّن مستوى الترجمة بالذكاء الاصطناعي بسرعة كبيرة، ولذلك بدأ كثيرون يشكّون في ضرورة تعلّم اللغات الأجنبية."),
                l("出国旅游的时候，只要拿出手机，就能看懂菜单、问路，甚至跟当地人简单聊天。", "When travelling abroad, as long as you take out your phone you can read a menu, ask the way and even chat a little with local people.", "فعند السفر إلى الخارج يكفي أن تُخرج هاتفك لتفهم قائمة الطعام وتسأل عن الطريق، بل وتتحدث قليلًا مع أهل البلد."),
                l("然而，语言不仅仅是交流信息的工具，它还反映了一个民族的思维方式和文化。", "However, language is not merely a tool for exchanging information; it also reflects a people's way of thinking and its culture.", "غير أن اللغة ليست مجرد أداة لتبادل المعلومات، بل تعكس أيضًا طريقة تفكير شعب وثقافته."),
                l("比如说，中国人见面时常问“吃了吗？”，这其实是一种问候，并不是真的想请你吃饭。", "For example, Chinese people often ask \"Have you eaten?\" when they meet; this is really a greeting, not a real invitation to a meal.", "مثلًا كثيرًا ما يسأل الصينيون عند اللقاء «هل أكلت؟»، وهي في الحقيقة تحية لا دعوة فعلية إلى الطعام."),
                l("翻译软件可以把这句话翻译得很准确，却很难告诉你它背后的意思。", "A translation app can translate this sentence very accurately, yet it can hardly tell you what lies behind it.", "يستطيع برنامج الترجمة أن يترجم هذه الجملة بدقة، لكنه يصعب عليه أن يخبرك بما وراءها من معنى."),
                l("此外，很多成语、笑话和双关语，一旦翻译成别的语言，就失去了原来的味道。", "In addition, many idioms, jokes and puns lose their original flavour as soon as they are translated into another language.", "إضافة إلى ذلك فإن كثيرًا من التعابير والنكات والتورية تفقد نكهتها الأصلية بمجرد ترجمتها إلى لغة أخرى."),
                l("更重要的是，通过翻译软件交流，人和人之间总隔着一块屏幕。", "More importantly, when people communicate through a translation app, there is always a screen between them.", "والأهم من ذلك أن التواصل عبر برنامج الترجمة يضع دائمًا شاشة بين الناس."),
                l("如果你能直接用对方的语言开个玩笑，对方往往会对你产生好感，从而更愿意跟你合作。", "If you can make a joke directly in the other person's language, they will often warm to you and thus be more willing to work with you.", "فإذا استطعت أن تمزح مباشرة بلغة محدّثك، فغالبًا ما يشعر نحوك بالمودّة، فيصبح بذلك أكثر استعدادًا للتعاون معك."),
                l("当然，这并不意味着翻译软件没有用。", "Of course, this does not mean translation apps are useless.", "وبالطبع لا يعني هذا أن برامج الترجمة عديمة الفائدة."),
                l("对于学外语的人来说，它是查生词、检查句子的好帮手。", "For language learners, it is a good helper for looking up new words and checking sentences.", "فهي للمتعلّمين مساعد جيد للبحث عن الكلمات الجديدة والتحقق من الجمل."),
                l("总之，机器可以帮我们理解别人说了什么，但只有真正学会一门语言，才能理解别人为什么这么说。", "In short, machines can help us understand what others have said, but only by truly learning a language can we understand why they say it that way.", "وخلاصة القول إن الآلة تستطيع أن تساعدنا على فهم ما قاله الآخرون، لكننا لا نفهم لماذا قالوه بهذه الطريقة إلا إذا تعلّمنا اللغة حقًا."),
            ), listOf(
                w("近年来", "in recent years", "في السنوات الأخيرة", "jìnnián lái"), w("人工智能", "artificial intelligence", "الذكاء الاصطناعي", "réngōng zhìnéng"), w("翻译", "translation; to translate", "ترجمة؛ يترجم", "fānyì"),
                w("水平", "level, standard", "مستوى", "shuǐpíng"), w("提高", "to improve, to raise", "يرفع، يحسّن", "tígāo"), w("因此", "therefore", "لذلك", "yīncǐ"), w("怀疑", "to doubt", "يشكّ", "huáiyí"),
                w("必要", "necessary", "ضروري", "bìyào"), w("出国", "to go abroad", "يسافر إلى الخارج", "chūguó"), w("菜单", "menu", "قائمة الطعام", "càidān"), w("甚至", "even", "بل حتى", "shènzhì"),
                w("当地人", "local people", "أهل البلد", "dāngdì rén"), w("然而", "however", "غير أنّ", "rán'ér"), w("不仅仅", "not merely", "ليس مجرد", "bù jǐnjǐn"), w("交流", "to communicate, exchange", "يتواصل، يتبادل", "jiāoliú"),
                w("信息", "information", "معلومات", "xìnxī"), w("工具", "tool", "أداة", "gōngjù"), w("反映", "to reflect", "يعكس", "fǎnyìng"), w("民族", "people, nation", "شعب، قومية", "mínzú"),
                w("思维方式", "way of thinking", "طريقة التفكير", "sīwéi fāngshì"), w("文化", "culture", "ثقافة", "wénhuà"), w("问候", "greeting", "تحية", "wènhòu"), w("准确", "accurate", "دقيق", "zhǔnquè"),
                w("却", "yet, but", "لكن", "què"), w("背后", "behind", "وراء", "bèihòu"), w("此外", "in addition", "إضافةً إلى ذلك", "cǐwài"), w("成语", "idiom", "تعبير اصطلاحي", "chéngyǔ"),
                w("笑话", "joke", "نكتة", "xiàohua"), w("双关语", "pun", "تورية", "shuāngguānyǔ"), w("一旦", "once, as soon as", "بمجرد أن", "yídàn"), w("失去", "to lose", "يفقد", "shīqù"),
                w("味道", "flavour", "نكهة، طعم", "wèidao"), w("隔着", "separated by", "يفصل بينهما", "gézhe"), w("屏幕", "screen", "شاشة", "píngmù"), w("对方", "the other person", "الطرف الآخر", "duìfāng"),
                w("开个玩笑", "to make a joke", "يمزح", "kāi ge wánxiào"), w("往往", "often, tend to", "غالبًا", "wǎngwǎng"), w("产生好感", "to warm to someone", "يشعر بالمودّة", "chǎnshēng hǎogǎn"),
                w("从而", "thus, thereby", "وبذلك", "cóng'ér"), w("合作", "to cooperate", "يتعاون", "hézuò"), w("意味着", "to mean, to imply", "يعني", "yìwèizhe"), w("查", "to look up", "يبحث عن", "chá"),
                w("生词", "new word", "كلمة جديدة", "shēngcí"), w("检查", "to check", "يتحقق، يفحص", "jiǎnchá"), w("帮手", "helper", "مساعد", "bāngshǒu"), w("总之", "in short", "خلاصة القول", "zǒngzhī"),
                w("机器", "machine", "آلة", "jīqì"), w("理解", "to understand", "يفهم", "lǐjiě"),
            ), listOf(
                q("zh_st_fanyi_q1", "为什么很多人开始怀疑学外语的必要？", listOf("因为外语越来越难", "因为人工智能翻译的水平提高得很快", "因为出国的人少了"), 1, g("Why have many people begun to doubt the need to learn languages?", "لماذا بدأ كثيرون يشكّون في ضرورة تعلّم اللغات؟"),
                    listOf(g("languages are getting harder", "اللغات تزداد صعوبة"), g("machine translation has improved fast", "الترجمة الآلية تحسّنت بسرعة"), g("fewer people go abroad", "قلّ المسافرون إلى الخارج"))),
                q("zh_st_fanyi_q2", "“吃了吗？”在文章里是什么意思？", listOf("一种问候", "请你吃饭", "问你饿不饿"), 0, g("What does \"Have you eaten?\" mean in the text?", "ما معنى «هل أكلت؟» في النص؟"),
                    listOf(g("a greeting", "تحية"), g("an invitation to a meal", "دعوة إلى الطعام"), g("asking if you are hungry", "سؤال عن الجوع"))),
                q("zh_st_fanyi_q3", "作者认为翻译软件对学外语的人有什么用？", listOf("没有用", "可以代替老师", "可以查生词、检查句子"), 2, g("How does the writer think translation apps help learners?", "كيف يرى الكاتب أن برامج الترجمة تفيد المتعلّمين؟"),
                    listOf(g("they are useless", "لا فائدة منها"), g("they can replace teachers", "تحلّ محل المعلّم"), g("looking up words and checking sentences", "البحث عن الكلمات والتحقق من الجمل"))),
            ), listOf(
                o("zh_st_fanyi_o1", "直接用对方的语言开玩笑有什么好处？", "What is the advantage of joking directly in the other person's language?", "ما فائدة المزاح بلغة محدّثك مباشرة؟", "对方会对你产生好感，从而更愿意跟你合作。", "好感|喜欢", "合作"),
                retell("zh_st_fanyi_o2", ZH, "人工智能翻译越来越好，但是语言还反映文化和思维方式。翻译软件很难翻译成语和笑话，也让人和人之间隔着屏幕。翻译软件是学外语的好帮手，但只有真正学会外语，才能理解别人为什么这么说。", "翻译", "文化|思维", "成语|笑话|屏幕", "只有|学会"),
            )),
            story("zh_st_laolinghua", ZH, C1, "当社会越来越老", "When society grows older", "حين يشيخ المجتمع", listOf(
                l("根据统计，中国六十岁以上的老年人已经超过两亿，而且这个数字还在不断增加。", "According to statistics, China already has over two hundred million people aged sixty or more, and the number is still rising.", "وفقًا للإحصاءات تجاوز عدد من هم فوق الستين في الصين مئتي مليون، وما زال هذا العدد في ازدياد مستمر."),
                l("一方面，人们的生活条件和医疗水平提高了，寿命越来越长；另一方面，年轻人生的孩子却越来越少。", "On the one hand, living conditions and healthcare have improved and people live longer and longer; on the other, young people are having fewer and fewer children.", "فمن جهة تحسّنت ظروف المعيشة والرعاية الصحية فصار الناس يعيشون أطول فأطول، ومن جهة أخرى صار الشباب ينجبون أطفالًا أقل فأقل."),
                l("这两个原因加在一起，使得人口老龄化成为一个无论如何都不能忽视的问题。", "Together, these two causes have made population ageing a problem that cannot be ignored under any circumstances.", "واجتماع هذين السببين جعل شيخوخة السكان مشكلة لا يمكن تجاهلها بأي حال."),
                l("在传统观念中，养老主要是子女的责任，“养儿防老”的说法流传了几千年。", "In traditional thinking, caring for the old was mainly the children's duty; the saying \"raise sons to provide for old age\" has been handed down for thousands of years.", "في المفاهيم التقليدية كانت رعاية المسنّين مسؤولية الأبناء أساسًا، وقد تناقلت الأجيال مقولة «رَبِّ الأبناء ليرعوك في كبرك» آلاف السنين."),
                l("然而，如今很多年轻人在外地工作，即使想照顾父母，也常常有心无力。", "Nowadays, however, many young people work far from home, and even if they want to look after their parents, they often lack the means.", "غير أن كثيرًا من الشباب اليوم يعملون بعيدًا عن مدنهم، وحتى لو أرادوا رعاية آبائهم فإنهم كثيرًا ما تكون الرغبة موجودة والقدرة غائبة."),
                l("一对夫妻可能要同时照顾四位老人和一个孩子，压力之大可想而知。", "A married couple may have to look after four elderly parents and a child at the same time; you can imagine how great the pressure is.", "وقد يضطر زوجان إلى رعاية أربعة مسنّين وطفل في الوقت نفسه، ولك أن تتخيّل حجم الضغط."),
                l("为了应对这一挑战，政府和社会开始探索新的养老方式。", "To meet this challenge, the government and society have begun to explore new ways of caring for the elderly.", "ولمواجهة هذا التحدي بدأت الحكومة والمجتمع يبحثان عن أساليب جديدة لرعاية المسنّين."),
                l("比如，有的社区开办了老年食堂，老人花很少的钱就能吃上热饭。", "For example, some neighbourhoods have opened canteens for the elderly, where old people can get a hot meal for very little money.", "فمثلًا افتتحت بعض الأحياء مطاعم للمسنّين يحصلون فيها على وجبة ساخنة بمبلغ زهيد."),
                l("还有的城市尝试让年轻人和老人住在一起：年轻人付很低的房租，同时每周花几个小时陪老人聊天。", "Other cities are trying to have young and old people live together: the young pay a very low rent and spend a few hours a week keeping the old company.", "وتجرّب مدن أخرى أن يسكن الشباب مع المسنّين: يدفع الشباب إيجارًا زهيدًا، ويقضون في المقابل بضع ساعات أسبوعيًا في مجالسة المسنّين."),
                l("此外，越来越多的老人自己也不愿意只待在家里，他们上老年大学，学习书法、摄影，甚至用手机拍视频。", "In addition, more and more old people do not want to stay at home; they go to universities for the elderly and learn calligraphy, photography and even how to make videos on their phones.", "إضافة إلى ذلك لم يعد كثير من المسنّين يرغبون في البقاء في البيت، فهم يلتحقون بجامعات كبار السن ويتعلّمون الخط والتصوير، بل وتصوير مقاطع الفيديو بالهاتف."),
                l("可见，老龄化虽然带来了压力，却也在改变人们对“老”的看法。", "So we can see that although ageing brings pressure, it is also changing how people see \"being old\".", "ومن هنا يتبيّن أن الشيخوخة، وإن جلبت الضغوط، تغيّر أيضًا نظرة الناس إلى «الكِبَر»."),
            ), listOf(
                w("根据", "according to", "وفقًا لـ", "gēnjù"), w("统计", "statistics", "إحصاء", "tǒngjì"), w("老年人", "elderly people", "كبار السن", "lǎonián rén"), w("超过", "to exceed", "يتجاوز", "chāoguò"),
                w("亿", "hundred million", "مئة مليون", "yì"), w("数字", "number, figure", "رقم", "shùzì"), w("不断", "continuously", "باستمرار", "búduàn"), w("增加", "to increase", "يزداد", "zēngjiā"),
                w("一方面", "on the one hand", "من جهة", "yì fāngmiàn"), w("医疗", "medical care", "الرعاية الطبية", "yīliáo"), w("寿命", "lifespan", "متوسط العمر", "shòumìng"), w("另一方面", "on the other hand", "من جهة أخرى", "lìng yì fāngmiàn"),
                w("使得", "to cause, to make", "يجعل", "shǐde"), w("人口", "population", "السكان", "rénkǒu"), w("老龄化", "ageing (of a population)", "الشيخوخة السكانية", "lǎolínghuà"), w("无论如何", "in any case, no matter what", "بأي حال", "wúlùn rúhé"),
                w("忽视", "to ignore", "يتجاهل", "hūshì"), w("传统", "tradition", "تقليد", "chuántǒng"), w("观念", "idea, concept", "مفهوم، تصوّر", "guānniàn"), w("养老", "to provide for the elderly", "رعاية المسنّين", "yǎnglǎo"),
                w("子女", "sons and daughters", "الأبناء", "zǐnǚ"), w("责任", "responsibility", "مسؤولية", "zérèn"), w("说法", "saying", "مقولة", "shuōfǎ"), w("流传", "to be handed down", "يتناقله الناس", "liúchuán"),
                w("如今", "nowadays", "اليوم، في هذه الأيام", "rújīn"), w("外地", "another part of the country", "مكان آخر من البلاد", "wàidì"), w("即使", "even if", "حتى لو", "jíshǐ"), w("照顾", "to look after", "يرعى", "zhàogù"),
                w("有心无力", "willing but unable", "الرغبة موجودة والقدرة غائبة", "yǒu xīn wú lì"), w("夫妻", "married couple", "زوجان", "fūqī"), w("同时", "at the same time", "في الوقت نفسه", "tóngshí"), w("压力", "pressure", "ضغط", "yālì"),
                w("可想而知", "one can well imagine", "لك أن تتخيّل", "kě xiǎng ér zhī"), w("应对", "to deal with, to respond to", "يواجه، يتعامل مع", "yìngduì"), w("挑战", "challenge", "تحدٍّ", "tiǎozhàn"), w("探索", "to explore", "يستكشف", "tànsuǒ"),
                w("社区", "community, neighbourhood", "حيّ، مجتمع محلي", "shèqū"), w("开办", "to open, to set up", "يفتتح", "kāibàn"), w("食堂", "canteen", "مطعم جماعي", "shítáng"), w("尝试", "to try", "يجرّب", "chángshì"),
                w("房租", "rent", "إيجار", "fángzū"), w("陪", "to keep company", "يرافق", "péi"), w("此外", "in addition", "إضافةً إلى ذلك", "cǐwài"), w("书法", "calligraphy", "فن الخط", "shūfǎ"),
                w("摄影", "photography", "التصوير", "shèyǐng"), w("视频", "video", "مقطع فيديو", "shìpín"), w("可见", "it can be seen", "من هنا يتبيّن", "kějiàn"), w("看法", "view, opinion", "نظرة، رأي", "kànfǎ"),
            ), listOf(
                q("zh_st_laolinghua_q1", "文章说人口老龄化的两个原因是什么？", listOf("寿命越来越长，孩子越来越少", "老人越来越穷，城市越来越大", "年轻人不工作，老人不运动"), 0, g("What two causes of population ageing does the text give?", "ما السببان اللذان يذكرهما النص لشيخوخة السكان؟"),
                    listOf(g("longer lives and fewer children", "أعمار أطول وأطفال أقل"), g("poorer old people, bigger cities", "مسنّون أفقر ومدن أكبر"), g("young people don't work, old people don't exercise", "الشباب لا يعملون والمسنّون لا يتحرّكون"))),
                q("zh_st_laolinghua_q2", "“有心无力”在这里是什么意思？", listOf("不想照顾父母", "想照顾父母，但是做不到", "很有力气"), 1, g("What does 有心无力 mean here?", "ما معنى 有心无力 هنا؟"),
                    listOf(g("not wanting to look after parents", "لا يريدون رعاية آبائهم"), g("wanting to, but unable", "يريدون لكنهم لا يستطيعون"), g("being very strong", "أقوياء جدًا"))),
                q("zh_st_laolinghua_q3", "年轻人和老人住在一起的时候，年轻人要做什么？", listOf("付很高的房租", "每天给老人做饭", "每周陪老人聊几个小时天"), 2, g("When young and old live together, what must the young people do?", "حين يسكن الشباب مع المسنّين، ماذا عليهم أن يفعلوا؟"),
                    listOf(g("pay a high rent", "يدفعون إيجارًا مرتفعًا"), g("cook for them every day", "يطبخون لهم كل يوم"), g("keep them company a few hours a week", "يجالسونهم بضع ساعات أسبوعيًا"))),
            ), listOf(
                o("zh_st_laolinghua_o1", "为什么说一对夫妻的压力很大？", "Why does the text say a married couple is under great pressure?", "لماذا يقول النص إن الزوجين تحت ضغط كبير؟", "因为他们可能要同时照顾四位老人和一个孩子。", "四", "孩子"),
                retell("zh_st_laolinghua_o2", ZH, "因为寿命越来越长，孩子越来越少，中国的人口老龄化越来越严重。传统上子女照顾老人，但现在很多年轻人在外地工作。社会开始探索新的养老方式，比如老年食堂和年轻人跟老人住在一起。很多老人也开始学习新东西。", "寿命|孩子", "子女|年轻人", "食堂|住在一起|养老", "学习|老年大学"),
            )),
        )
    }
}
