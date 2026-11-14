package com.polyglot.academy.core.content

import com.polyglot.academy.core.model.CefrLevel.PRE_A1
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.GrammarTopic
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Lesson
import com.polyglot.academy.core.model.Skill

/**
 * The start of the Mandarin course for a learner who has never seen Chinese: how the language works, the
 * sounds of pinyin (initials, finals), tone changes, strokes and their order, radicals, and the first
 * characters written by hand. Each rule is shown as a note right before the exercises that use it.
 * Written for this app; AI-written and not yet reviewed by a native speaker.
 */
internal object ZhFoundations {
    private val ZH = LanguageCode.MANDARIN
    private fun g(en: String, ar: String) = Gloss(en, ar)

    private val listenPinyin = g("Listen. Which pinyin matches what you hear?", "استمع. أيّ كتابة بالبينيين تطابق ما تسمعه؟")

    /** A question about how Chinese works, shown in the learner's language. */
    private fun ask(id: String, prompt: Gloss, options: List<Gloss>, correct: Int, explanation: Gloss? = null, skill: Skill = Skill.GRAMMAR) =
        Exercise.MultipleChoice("zh_x_$id", skill, Instr.choose, prompt.en, options.map { it.en }, correct, 1, explanation,
            promptGloss = prompt, optionGlosses = options)

    /** Hear a character and pick its pinyin. */
    private fun hear(id: String, character: String, options: List<String>, correct: Int, explanation: Gloss? = null) =
        Exercise.MultipleChoice("zh_x_$id", Skill.LISTENING, listenPinyin, "🔊", options, correct, 2, explanation, audioText = character)

    private fun write(id: String, character: String, word: String, reading: String, en: String, ar: String) =
        Exercise.WriteCharacter("zh_x_write_$id", Skill.WRITING, Instr.writeChar, character, word, reading, g(en, ar))

    fun grammar(): List<GrammarTopic> = listOf(
        grammar("f_overview", ZH, PRE_A1, g("How Chinese works", "كيف تعمل اللغة الصينية"),
            g("Mandarin is built from syllables, and each syllable has a tone that changes its meaning: mā (mum) and mǎ (horse) differ only in tone. In writing, each syllable is one character (汉字 hànzì), and a character carries a meaning, not only a sound. Most words have one or two characters. Pinyin is the spelling of the sounds in Latin letters: you use it to learn pronunciation and to type, but Chinese readers do not need it. Characters are made of strokes written in a fixed order, and of recurring parts (radicals) that often hint at the meaning or the sound.",
                "تتكوّن الصينية الماندرينية من مقاطع صوتية، ولكل مقطع نغمة تغيّر معناه: mā (أم) وmǎ (حصان) لا يختلفان إلا في النغمة. وفي الكتابة يُكتب كل مقطع بحرف واحد (汉字 hànzì)، والحرف يحمل معنى لا صوتًا فقط. ومعظم الكلمات من حرف أو حرفين. أما البينيين فهو كتابة الأصوات بالحروف اللاتينية: تستعمله لتعلّم النطق وللكتابة على لوحة المفاتيح، ولا يحتاجه القارئ الصيني. وتتكوّن الحروف من ضربات تُكتب بترتيب ثابت، ومن أجزاء تتكرر (الجذور) تدلّ غالبًا على المعنى أو الصوت."),
            listOf(
                ex("你好", "nǐ hǎo: hello. Two syllables, two characters.", "nǐ hǎo: مرحبًا. مقطعان وحرفان."),
                ex("中国", "Zhōngguó: China. 中 middle + 国 country.", "Zhōngguó: الصين. 中 وسط + 国 بلد."),
                ex("妈 / 马", "mā mum / mǎ horse: 妈 is 女 (woman) + 马 for the sound.", "mā أم / mǎ حصان: 妈 هو 女 (امرأة) + 马 للدلالة على الصوت."),
            ),
            listOf(g("Thinking pinyin is how Chinese is written: it is only a guide to the sounds.", "الظن أن البينيين هو الكتابة الصينية: إنه دليل للنطق فقط."),
                g("Ignoring the tones: a wrong tone can give a different word.", "إهمال النغمات: النغمة الخاطئة قد تعطي كلمة أخرى."))),

        grammar("f_initials", ZH, PRE_A1, g("Pinyin: the first sound of a syllable", "البينيين: الصوت الأول في المقطع"),
            g("The initial is the consonant at the start of a syllable. Do not read the letters as in English or Arabic. b, d, g are voiceless and soft, with no puff of air (between Arabic ب and English p); p, t, k are the same sounds with a strong puff of air. h is a light خ. j, q, x are said with the tongue flat behind the lower teeth and the lips spread: j is a light «جي», q is «تشي» with a puff of air, x is between س and ش. zh, ch, sh, r are said with the tip of the tongue curled up: zh is like ج, ch like «تش» with a puff, sh like ش, r between ر and ژ. z is «دز», c is «تس» with a puff, s is س.",
                "الصوت الأول (initial) هو الحرف الساكن في بداية المقطع. لا تقرأ الحروف كما في الإنجليزية أو العربية. فـb وd وg مهموسة وخفيفة بلا نفخة هواء (بين ب العربية وp الإنجليزية)، وp وt وk هي الأصوات نفسها مع نفخة هواء قوية. وh خاء خفيفة. وj وq وx تُنطق واللسان منبسط خلف الأسنان السفلى والشفتان مفرودتان: j «جي» خفيفة، وq «تشي» مع نفخة هواء، وx بين السين والشين. وzh وch وsh وr تُنطق وطرف اللسان مرفوع إلى الخلف: zh كالجيم، وch «تش» مع نفخة، وsh كالشين، وr بين الراء والژ. وz «دز»، وc «تس» مع نفخة، وs سين."),
            listOf(
                ex("八 bā / 爬 pá", "eight / to climb: b with no puff of air, p with a puff.", "ثمانية / يتسلّق: b دون نفخة هواء، وp مع نفخة."),
                ex("七 qī / 吃 chī", "seven / to eat: q with a flat tongue, ch with a curled tongue.", "سبعة / يأكل: q واللسان منبسط، وch واللسان مرفوع."),
                ex("西 xī / 是 shì / 四 sì", "west / to be / four", "غرب / يكون / أربعة"),
            ),
            listOf(g("Reading q as «k», x as «ks» or c as «k»: in pinyin they are other sounds.", "قراءة q «ك» وx «كس» وc «ك»: هي في البينيين أصوات أخرى."),
                g("Saying b, d, g with the voice, like Arabic ب and د: in Mandarin they are voiceless.", "نطق b وd وg مجهورة كالباء والدال العربيتين: هي في الصينية مهموسة."))),

        grammar("f_finals", ZH, PRE_A1, g("Pinyin: the rest of the syllable", "البينيين: بقية المقطع"),
            g("The final is the vowel part: a, o, e, i, u, ü, alone or combined (ai, ao, an, ang, ie, iu, ui, ün…). e (as in 饿 è, hungry) is a relaxed «uh», not «ي» or the English e. ü is said like «ي» with the lips rounded as for «و». After j, q, x and y, ü is written u without dots: 去 qù, 鱼 yú. After z, c, s, zh, ch, sh, r, the letter i is not «ي» but a buzz that continues the consonant: 四 sì, 是 shì. With no initial, i is written yi or y, and u is written wu or w: 一 yī, 我 wǒ. -n ends with the tongue on the teeth like Arabic ن; -ng ends at the back of the mouth, as in English «sing».",
                "الصوت الأخير (final) هو الجزء الصوتي: a وo وe وi وu وü، وحدها أو مركّبة (ai وao وan وang وie وiu وui وün…). والـe (كما في 饿 è، جائع) صوت مرتخٍ يشبه «أَ» المكتومة، لا ياءً ولا e الإنجليزية. والـü تُنطق ياءً مع ضمّ الشفتين كأنك تقول واوًا. وبعد j وq وx وy تُكتب ü دون نقطتين: 去 qù و鱼 yú. وبعد z وc وs وzh وch وsh وr لا تكون i ياءً، بل طنينًا يُكمل الحرف الساكن: 四 sì و是 shì. وإن لم يسبقها ساكن كُتبت i على صورة yi أو y، وu على صورة wu أو w: 一 yī و我 wǒ. وتنتهي -n واللسان على الأسنان كالنون العربية، وتنتهي -ng في مؤخرة الفم كما في الإنجليزية sing."),
            listOf(
                ex("绿 lǜ / 路 lù", "green / road: ü against u.", "أخضر / طريق: ü مقابل u."),
                ex("去 qù", "to go: written u, said ü.", "يذهب: تُكتب u وتُنطق ü."),
                ex("山 shān / 上 shàng", "mountain / up: -n against -ng.", "جبل / فوق: -n مقابل -ng."),
            ),
            listOf(g("Saying 去 qù with the u of «food»: after q it is ü.", "نطق 去 qù بضمة كاملة: بعد q هي ü."),
                g("Saying the i of 是 shì like «ي»: after sh it is a buzz.", "نطق i في 是 shì ياءً: بعد sh هي طنين."))),

        grammar("f_tone_change", ZH, PRE_A1, g("When tones change", "متى تتغيّر النغمات"),
            g("Some tones change when syllables meet; dictionaries usually write the original tone. 1) Two 3rd tones in a row: the first is said as a 2nd tone: 你好 nǐ hǎo is said ní hǎo. 2) 不 bù becomes bú before a 4th tone: 不是 bú shì, 不要 bú yào. 3) 一 yī is yī alone, when counting and at the end of a word; before a 4th tone it is yí (一个 yí ge, 一样 yíyàng); before the other tones it is yì (一天 yì tiān, 一起 yìqǐ). 4) A 3rd tone before another tone is usually only low, without the rise at the end.",
                "تتغيّر بعض النغمات عند تجاور المقاطع، والقواميس تكتب غالبًا النغمة الأصلية. 1) إذا تتابعت نغمتان ثالثتان نُطقت الأولى نغمة ثانية: 你好 nǐ hǎo تُنطق ní hǎo. 2) تصير 不 bù نغمة ثانية bú قبل النغمة الرابعة: 不是 bú shì و不要 bú yào. 3) تُنطق 一 yī كما هي وحدها وفي العدّ وفي آخر الكلمة، وقبل النغمة الرابعة تصير yí (一个 yí ge و一样 yíyàng)، وقبل النغمات الأخرى تصير yì (一天 yì tiān و一起 yìqǐ). 4) النغمة الثالثة قبل نغمة أخرى تُنطق منخفضة فقط، دون الصعود في آخرها."),
            listOf(
                ex("很好", "hěn hǎo, said hén hǎo: very good.", "hěn hǎo، وتُنطق hén hǎo: جيد جدًا."),
                ex("不对", "bù duì, said bú duì: not right.", "bù duì، وتُنطق bú duì: غير صحيح."),
                ex("一天", "yī tiān, said yì tiān: one day.", "yī tiān، وتُنطق yì tiān: يوم واحد."),
            ),
            listOf(g("Saying 不是 with a 4th tone on 不: before a 4th tone it is bú.", "نطق 不 في 不是 بالنغمة الرابعة: قبل النغمة الرابعة تصير bú."))),

        grammar("f_strokes", ZH, PRE_A1, g("Strokes and stroke order", "الضربات وترتيبها"),
            g("Every character is written with strokes, each drawn in one movement. The basic ones: 横 héng, horizontal, left to right (一); 竖 shù, vertical, top to bottom (丨); 撇 piě, falling to the left (丿); 点 diǎn, a dot (丶); 捺 nà, falling to the right (㇏); 提 tí, rising to the right; 折 zhé, a turn; 钩 gōu, a hook at the end. The order follows rules: top before bottom (三); left before right (八); horizontal before vertical when they cross (十); left-falling before right-falling (人); outside before inside (月), but the bottom of a closed frame comes last (口, 国); the middle before the two sides (小). Writing in this order makes characters well shaped and easier to remember and to read in handwriting.",
                "يُكتب كل حرف بضربات، وكل ضربة حركة واحدة للقلم. والضربات الأساسية: 横 héng أفقية من اليسار إلى اليمين (一)، و竖 shù عمودية من أعلى إلى أسفل (丨)، و撇 piě مائلة نازلة إلى اليسار (丿)، و点 diǎn نقطة (丶)، و捺 nà مائلة نازلة إلى اليمين (㇏)، و提 tí صاعدة إلى اليمين، و折 zhé منعطفة، و钩 gōu خطّاف في نهاية الضربة. ويتبع الترتيب قواعد: الأعلى قبل الأسفل (三)، واليسار قبل اليمين (八)، والأفقية قبل العمودية إذا تقاطعتا (十)، والمائلة إلى اليسار قبل المائلة إلى اليمين (人)، والخارج قبل الداخل (月)، لكن أسفل الإطار المغلق يُكتب أخيرًا (口 و国)، والوسط قبل الجانبين (小). والكتابة بهذا الترتيب تجعل الحرف متناسقًا وأسهل للحفظ ولقراءة خط اليد."),
            listOf(
                ex("十", "shí, ten: the horizontal first, then the vertical.", "shí، عشرة: الأفقية أولًا ثم العمودية."),
                ex("人", "rén, person: 丿 first, then ㇏.", "rén، شخص: 丿 أولًا ثم ㇏."),
                ex("口", "kǒu, mouth: left side, top and right side, then the bottom line last.", "kǒu، فم: الجانب الأيسر، ثم الأعلى والأيمن، ثم الخط السفلي أخيرًا."),
            ),
            listOf(g("Closing a frame before writing what is inside it, as in 国: the inside comes first.", "إغلاق الإطار قبل كتابة ما بداخله كما في 国: الداخل يأتي أولًا."))),

        grammar("f_radicals", ZH, PRE_A1, g("Radicals: the parts of a character", "الجذور: أجزاء الحرف"),
            g("Most characters are made of a part that hints at the meaning (the radical) and a part that hints at the sound. 妈 mā (mum) = 女 woman + 马 mǎ; 吗 ma and 骂 mà have the same sound part. The hint is a help, not a rule: the sound part often gives only a similar syllable. Common radicals: 亻 person (你, 他); 女 woman (妈, 好); 口 mouth (吃, 喝, 叫); 氵 water (河, 海, 汤); 木 tree, wood (林, 树); 扌 hand (打, 拿); 讠 speech (说, 话, 谢); 艹 grass, plants (茶, 花); 宀 roof (家, 字); 日 sun, day (明, 早); 忄 heart, feeling (忙, 快). Knowing them makes new characters easier to learn and to look up.",
                "تتكوّن معظم الحروف من جزء يدل على المعنى (الجذر) وجزء يدل على الصوت. فـ妈 mā (أم) = 女 امرأة + 马 mǎ، و吗 ma و骂 mà فيهما جزء الصوت نفسه. والدلالة مساعدة لا قاعدة: جزء الصوت يعطي غالبًا مقطعًا قريبًا فقط. ومن الجذور الشائعة: 亻 شخص (你 و他)، و女 امرأة (妈 و好)، و口 فم (吃 و喝 و叫)، و氵 ماء (河 و海 و汤)، و木 شجرة وخشب (林 و树)، و扌 يد (打 و拿)، و讠 كلام (说 و话 و谢)، و艹 عشب ونبات (茶 و花)، و宀 سقف (家 و字)، و日 شمس ويوم (明 و早)، و忄 قلب وشعور (忙 و快). ومعرفتها تسهّل تعلّم الحروف الجديدة والبحث عنها."),
            listOf(
                ex("妈 = 女 + 马", "mā, mum: woman (meaning) + mǎ (sound).", "mā، أم: امرأة (المعنى) + mǎ (الصوت)."),
                ex("河 = 氵 + 可", "hé, river: water (meaning) + kě (sound).", "hé، نهر: ماء (المعنى) + kě (الصوت)."),
                ex("说", "shuō, to speak: 讠 speech.", "shuō، يتكلّم: 讠 كلام."),
            ),
            listOf(g("Expecting the sound part to give the exact sound: it often gives only a similar syllable.", "توقّع أن يعطي جزء الصوت النطق الدقيق: يعطي غالبًا مقطعًا قريبًا فقط."))),

        grammar("f_words", ZH, PRE_A1, g("Building words from characters", "بناء الكلمات من الحروف"),
            g("Many words join the meanings of their characters. 中 middle + 国 country = 中国 China; add 人 person: 中国人, a Chinese person. 火 fire + 山 mountain = 火山, a volcano. 大 big + 人 person = 大人, an adult. 山 mountain + 水 water = 山水, landscape. Guessing from the parts works often, but not always, so check the meaning of a new word.",
                "تجمع كلمات كثيرة معاني حروفها. 中 وسط + 国 بلد = 中国 الصين، وأضف 人 شخص: 中国人 شخص صيني. و火 نار + 山 جبل = 火山 بركان. و大 كبير + 人 شخص = 大人 شخص بالغ. و山 جبل + 水 ماء = 山水 منظر طبيعي. والتخمين من الأجزاء ينجح كثيرًا لا دائمًا، فتحقّق من معنى الكلمة الجديدة."),
            listOf(
                ex("中国人", "Zhōngguó rén: a Chinese person.", "Zhōngguó rén: شخص صيني."),
                ex("火山", "huǒshān: a volcano.", "huǒshān: بركان."),
                ex("人口", "rénkǒu: population (people + mouths).", "rénkǒu: عدد السكان (أشخاص + أفواه)."),
            ),
            listOf(g("Translating every word character by character: the whole word can mean something new.", "ترجمة كل كلمة حرفًا حرفًا: قد يكون للكلمة كلها معنى جديد."))),
    )

    fun lessons(f: LessonFactory): List<Lesson> = listOf(
        f.lesson("how_chinese_works", PRE_A1, "foundations", g("How Chinese works", "كيف تعمل اللغة الصينية"),
            g("Syllables, tones, characters and pinyin: what you will learn and why.", "المقاطع والنغمات والحروف والبينيين: ما ستتعلّمه ولماذا."),
            Skill.READING, emptyList(), "f_overview",
            extra = listOf(
                ask("f_ov1", g("How many characters are in 中国人 (a Chinese person)?", "كم حرفًا في 中国人 (شخص صيني)؟"), listOf(g("2", "2"), g("3", "3"), g("4", "4")), 1,
                    g("One character for each syllable: Zhōng-guó-rén.", "حرف لكل مقطع: Zhōng-guó-rén.")),
                ask("f_ov2", g("What is pinyin?", "ما البينيين؟"),
                    listOf(g("The spelling of Chinese sounds in Latin letters", "كتابة أصوات الصينية بالحروف اللاتينية"), g("The usual way Chinese is written", "الطريقة المعتادة لكتابة الصينية"), g("A kind of Chinese character", "نوع من الحروف الصينية")), 0),
                ask("f_ov3", g("mā (mum) and mǎ (horse) differ only in…", "لا تختلف mā (أم) وmǎ (حصان) إلا في…"), listOf(g("the tone", "النغمة"), g("the consonant", "الحرف الساكن"), g("the vowel", "الحرف الصائت")), 0),
                ask("f_ov4", g("What does one character usually stand for?", "علامَ يدل الحرف الواحد غالبًا؟"),
                    listOf(g("One syllable with a meaning", "مقطع واحد له معنى"), g("One letter", "حرف هجاء واحد"), g("A whole sentence", "جملة كاملة")), 0),
                Exercise.Matching("zh_x_f_ov5", Skill.READING, Instr.matching, listOf("人" to g("person", "شخص"), "口" to g("mouth", "فم"), "山" to g("mountain", "جبل"), "大" to g("big", "كبير"))),
                Exercise.ToneChoice("zh_x_tone_ma1_f", Skill.PRONUNCIATION, Instr.tone, "ma", 1, "妈", g("mum", "أم")),
                Exercise.ToneChoice("zh_x_tone_ma3_f", Skill.PRONUNCIATION, Instr.tone, "ma", 3, "马", g("horse", "حصان")),
                hear("f_ov6", "马", listOf("mā", "mǎ"), 1, g("马 mǎ, horse, has the 3rd tone: low, then rising.", "‏马 mǎ (حصان) نغمته ثالثة: منخفضة ثم صاعدة.")),
            ), minutes = 6),
        f.lesson("pinyin_initials", PRE_A1, "foundations", g("Pinyin: first sounds", "البينيين: الأصوات الأولى"),
            g("b/p, d/t, g/k, j/q/x, zh/ch/sh/r and z/c/s, compared with Arabic sounds.", "b/p وd/t وg/k وj/q/x وzh/ch/sh/r وz/c/s مقارنةً بأصوات العربية."),
            Skill.PRONUNCIATION, emptyList(), "f_initials",
            extra = listOf(
                hear("f_in1", "八", listOf("bā", "pā"), 0),
                hear("f_in2", "他", listOf("dā", "tā"), 1),
                hear("f_in3", "哥", listOf("gē", "kē"), 0),
                hear("f_in4", "七", listOf("qī", "chī", "xī"), 0),
                hear("f_in5", "是", listOf("sì", "shì", "xì"), 1),
                hear("f_in6", "四", listOf("sì", "shì", "xì"), 0),
                hear("f_in7", "吃", listOf("qī", "chī", "cī"), 1),
                ask("f_in8", g("Which pair differs only by a puff of air?", "أيّ زوج لا يختلف إلا بنفخة الهواء؟"), listOf(g("b – p", "b – p"), g("b – m", "b – m"), g("s – sh", "s – sh")), 0, skill = Skill.PRONUNCIATION),
                Exercise.ToneChoice("zh_x_tone_ba1_f", Skill.PRONUNCIATION, Instr.tone, "ba", 1, "八", g("eight", "ثمانية")),
                Exercise.ToneChoice("zh_x_tone_qi1_f", Skill.PRONUNCIATION, Instr.tone, "qi", 1, "七", g("seven", "سبعة")),
            ), minutes = 8, prerequisites = listOf("how_chinese_works")),
        f.lesson("pinyin_finals", PRE_A1, "foundations", g("Pinyin: vowels and endings", "البينيين: الأصوات الصائتة والنهايات"),
            g("e, ü, the buzzing i, -n and -ng, and the spelling rules for y and w.", "e وü وi الطنّانة و-n و-ng، وقواعد الكتابة بـy وw."),
            Skill.PRONUNCIATION, emptyList(), "f_finals",
            extra = listOf(
                hear("f_fi1", "绿", listOf("lǜ", "lù"), 0),
                hear("f_fi2", "路", listOf("lǜ", "lù"), 1),
                hear("f_fi3", "山", listOf("shān", "shāng"), 0),
                hear("f_fi4", "上", listOf("shàn", "shàng"), 1),
                ask("f_fi5", g("In 去 qù, the u is said as…", "في 去 qù تُنطق u…"), listOf(g("ü", "ü"), g("u as in «food»", "ضمة كاملة كما في «فول»"), g("o", "o")), 0,
                    g("After j, q, x and y, u is always ü.", "بعد j وq وx وy تكون u دائمًا ü."), Skill.PRONUNCIATION),
                ask("f_fi6", g("How is the syllable i written when it has no initial?", "كيف يُكتب المقطع i إذا لم يسبقه ساكن؟"), listOf(g("yi", "yi"), g("i", "i"), g("wi", "wi")), 0, skill = Skill.PRONUNCIATION),
                Exercise.ToneChoice("zh_x_tone_lu4_f", Skill.PRONUNCIATION, Instr.tone, "lu", 4, "路", g("road", "طريق")),
                Exercise.ToneChoice("zh_x_tone_shan1_f", Skill.PRONUNCIATION, Instr.tone, "shan", 1, "山", g("mountain", "جبل")),
            ), minutes = 8, prerequisites = listOf("pinyin_initials")),
        f.lesson("tone_changes", PRE_A1, "sounds", g("When tones change", "متى تتغيّر النغمات"),
            g("Two 3rd tones, 不 and 一.", "نغمتان ثالثتان، و不، و一."),
            Skill.PRONUNCIATION, emptyList(), "f_tone_change",
            extra = listOf(
                ask("f_tc1", g("How is 不 said in 不对 (对 duì has the 4th tone)?", "كيف تُنطق 不 في 不对 (对 duì نغمتها رابعة)؟"), listOf(g("bú", "bú"), g("bù", "bù"), g("bǔ", "bǔ")), 0, skill = Skill.PRONUNCIATION),
                ask("f_tc2", g("How is 一 said in 一天 (天 tiān has the 1st tone)?", "كيف تُنطق 一 في 一天 (天 tiān نغمتها أولى)؟"), listOf(g("yì", "yì"), g("yí", "yí"), g("yī", "yī")), 0, skill = Skill.PRONUNCIATION),
                ask("f_tc3", g("How is 一 said in 一个 (个 gè has the 4th tone)?", "كيف تُنطق 一 في 一个 (个 gè نغمتها رابعة)؟"), listOf(g("yí", "yí"), g("yì", "yì"), g("yī", "yī")), 0, skill = Skill.PRONUNCIATION),
                ask("f_tc4", g("很好 is written hěn hǎo. How is it said?", "‏很好 تُكتب hěn hǎo. كيف تُنطق؟"), listOf(g("hén hǎo", "hén hǎo"), g("hěn háo", "hěn háo"), g("hèn hǎo", "hèn hǎo")), 0, skill = Skill.PRONUNCIATION),
                Exercise.ToneChoice("zh_x_tone_si4", Skill.PRONUNCIATION, Instr.tone, "si", 4, "四", g("four", "أربعة")),
                Exercise.ToneChoice("zh_x_tone_shi2", Skill.PRONUNCIATION, Instr.tone, "shi", 2, "十", g("ten", "عشرة")),
                Exercise.ToneChoice("zh_x_tone_wu3", Skill.PRONUNCIATION, Instr.tone, "wu", 3, "五", g("five", "خمسة")),
                Exercise.ToneChoice("zh_x_tone_ma5", Skill.PRONUNCIATION, Instr.tone, "ma", 5, "吗", g("(question particle)", "(أداة استفهام)")),
            ), minutes = 7, prerequisites = listOf("tones_intro")),
        f.lesson("strokes", PRE_A1, "writing", g("Strokes and their order", "الضربات وترتيبها"),
            g("The basic strokes and the rules of stroke order; write your first characters.", "الضربات الأساسية وقواعد ترتيبها، واكتب حروفك الأولى."),
            Skill.WRITING, emptyList(), "f_strokes",
            extra = listOf(
                ask("f_st1", g("In 十, which stroke comes first?", "في 十، أيّ ضربة تأتي أولًا؟"), listOf(g("The horizontal 一", "الأفقية 一"), g("The vertical 丨", "العمودية 丨")), 0, skill = Skill.WRITING),
                ask("f_st2", g("In 人, which stroke comes first?", "في 人، أيّ ضربة تأتي أولًا؟"), listOf(g("The left-falling 丿", "المائلة إلى اليسار 丿"), g("The right-falling ㇏", "المائلة إلى اليمين ㇏")), 0, skill = Skill.WRITING),
                ask("f_st3", g("In 口, when is the bottom line written?", "في 口، متى يُكتب الخط السفلي؟"), listOf(g("Last", "أخيرًا"), g("First", "أولًا"), g("Second", "ثانيًا")), 0, skill = Skill.WRITING),
                ask("f_st4", g("In 小, which stroke comes first?", "في 小، أيّ ضربة تأتي أولًا؟"), listOf(g("The middle hook", "الخطّاف الأوسط"), g("The left dot", "النقطة اليسرى"), g("The right stroke", "الضربة اليمنى")), 0, skill = Skill.WRITING),
                write("yi", "一", "一", "yī", "one", "واحد"),
                write("er", "二", "二", "èr", "two", "اثنان"),
                write("san", "三", "三", "sān", "three", "ثلاثة"),
                write("shi", "十", "十", "shí", "ten", "عشرة"),
                write("ren", "人", "人", "rén", "person", "شخص"),
                write("da", "大", "大", "dà", "big", "كبير"),
            ), minutes = 10, prerequisites = listOf("tone_changes")),
        f.lesson("radicals", PRE_A1, "writing", g("Radicals: the parts of a character", "الجذور: أجزاء الحرف"),
            g("The parts that hint at meaning and sound, and the most common radicals.", "الأجزاء التي تدل على المعنى والصوت، وأشهر الجذور."),
            Skill.READING, emptyList(), "f_radicals",
            extra = listOf(
                Exercise.Matching("zh_x_f_ra1", Skill.READING, Instr.matching, listOf("亻" to g("person", "شخص"), "氵" to g("water", "ماء"), "口" to g("mouth", "فم"), "讠" to g("speech", "كلام"))),
                Exercise.Matching("zh_x_f_ra2", Skill.READING, Instr.matching, listOf("木" to g("tree, wood", "شجرة، خشب"), "女" to g("woman", "امرأة"), "艹" to g("grass, plants", "عشب، نبات"), "忄" to g("heart, feeling", "قلب، شعور"))),
                ask("f_ra3", g("Which part of 妈 (mā, mum) hints at the sound?", "أيّ جزء في 妈 (mā، أم) يدل على الصوت؟"), listOf(g("马 (mǎ)", "马 (mǎ)"), g("女 (woman)", "女 (امرأة)")), 0, skill = Skill.READING),
                ask("f_ra4", g("Which part of 河 (hé, river) hints at the meaning?", "أيّ جزء في 河 (hé، نهر) يدل على المعنى؟"), listOf(g("氵 water", "氵 ماء"), g("可 (kě)", "可 (kě)")), 0, skill = Skill.READING),
                ask("f_ra5", g("茶 (chá, tea) has which radical on top?", "ما الجذر في أعلى 茶 (chá، شاي)؟"), listOf(g("艹 grass, plants", "艹 عشب، نبات"), g("宀 roof", "宀 سقف"), g("人 person", "人 شخص")), 0, skill = Skill.READING),
                write("kou", "口", "口", "kǒu", "mouth", "فم"),
                write("ri", "日", "日", "rì", "sun, day", "شمس، يوم"),
                write("yue", "月", "月", "yuè", "moon, month", "قمر، شهر"),
                write("mu", "木", "木", "mù", "tree, wood", "شجرة، خشب"),
                write("nv", "女", "女", "nǚ", "woman", "امرأة"),
                write("zi", "子", "子", "zǐ", "child", "طفل"),
            ), minutes = 10, prerequisites = listOf("strokes")),
        f.lesson("first_characters", PRE_A1, "writing", g("First characters and words", "الحروف والكلمات الأولى"),
            g("Build words from characters you know, and write them.", "ابنِ كلمات من حروف تعرفها واكتبها."),
            Skill.WRITING, emptyList(), "f_words",
            extra = listOf(
                ask("f_wo1", g("火山 = 火 fire + 山 mountain. What is it?", "‏火山 = 火 نار + 山 جبل. ما هو؟"), listOf(g("A volcano", "بركان"), g("A fireplace", "مدفأة"), g("A forest", "غابة")), 0, skill = Skill.READING),
                ask("f_wo2", g("大人 = 大 big + 人 person. What is it?", "‏大人 = 大 كبير + 人 شخص. ما هو؟"), listOf(g("An adult", "شخص بالغ"), g("A giant", "عملاق"), g("A king", "ملك")), 0, skill = Skill.READING),
                ask("f_wo3", g("山水 = 山 mountain + 水 water. What is it?", "‏山水 = 山 جبل + 水 ماء. ما هو؟"), listOf(g("Landscape", "منظر طبيعي"), g("A river", "نهر"), g("Rain", "مطر")), 0, skill = Skill.READING),
                ask("f_wo4", g("人口 = 人 person + 口 mouth. What is it?", "‏人口 = 人 شخص + 口 فم. ما هو؟"), listOf(g("Population", "عدد السكان"), g("A person's mouth", "فم شخص"), g("A crowd's noise", "ضجيج الحشد")), 0, skill = Skill.READING),
                write("zhong", "中", "中国", "Zhōngguó", "China", "الصين"),
                write("guo", "国", "中国", "Zhōngguó", "China", "الصين"),
                write("xiao", "小", "小", "xiǎo", "small", "صغير"),
                write("shan", "山", "山", "shān", "mountain", "جبل"),
                write("shui", "水", "水", "shuǐ", "water", "ماء"),
                write("huo", "火", "火", "huǒ", "fire", "نار"),
            ), minutes = 10, prerequisites = listOf("radicals")),
    )

    fun units(): List<CourseUnit> = listOf(
        CourseUnit("zh_u_foundations", ZH, PRE_A1, g("Start here: how Chinese works", "ابدأ هنا: كيف تعمل اللغة الصينية"),
            g("Syllables, tones, characters and the sounds of pinyin", "المقاطع والنغمات والحروف وأصوات البينيين"),
            listOf("zh_l_how_chinese_works", "zh_l_pinyin_initials", "zh_l_pinyin_finals")),
        CourseUnit("zh_u_writing", ZH, PRE_A1, g("Writing characters", "كتابة الحروف"),
            g("Strokes, stroke order, radicals and your first characters", "الضربات وترتيبها والجذور وحروفك الأولى"),
            listOf("zh_l_strokes", "zh_l_radicals", "zh_l_first_characters")),
    )
}
