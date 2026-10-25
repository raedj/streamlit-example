package com.polyglot.academy.core.content

import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CefrLevel.A1
import com.polyglot.academy.core.model.CefrLevel.A2
import com.polyglot.academy.core.model.CefrLevel.B1
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.GlossaryEntry
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.ReadingPassage
import com.polyglot.academy.core.model.Skill

/**
 * Original graded stories (A1 market, A2 lost luggage, B1 volunteering) in every language.
 * Every word in the glossary can be tapped in the reader; other words fall back to the vocabulary.
 */
internal object GradedStories {
    private class Line(val text: String, val en: String, val ar: String)

    private fun l(text: String, en: String, ar: String) = Line(text, en, ar)
    private fun w(term: String, en: String, ar: String, reading: String? = null) = GlossaryEntry(term, Gloss(en, ar), reading)
    private fun q(id: String, prompt: String, options: List<String>, answer: Int, gloss: Gloss? = null, optionGlosses: List<Gloss>? = null) =
        Exercise.MultipleChoice(id, Skill.READING, Instr.choose, prompt, options, answer, promptGloss = gloss, optionGlosses = optionGlosses)

    private fun story(id: String, lang: LanguageCode, level: CefrLevel, title: String, en: String, ar: String, lines: List<Line>, words: List<GlossaryEntry>, qs: List<Exercise.MultipleChoice>) =
        ReadingPassage(id, lang, level, title, g(en, ar), lines.map { it.text }, lines.map { g(it.en, it.ar) }, emptyList(), qs, words)

    fun of(lang: LanguageCode): List<ReadingPassage> = when (lang) {
        LanguageCode.ENGLISH -> english
        LanguageCode.SPANISH -> spanish
        LanguageCode.FRENCH -> french
        LanguageCode.MANDARIN -> mandarin
        LanguageCode.ARABIC -> arabic
    }

    private val english by lazy {
        val L = LanguageCode.ENGLISH
        listOf(
            story("en_st_market", L, A1, "At the market", "At the market", "في السوق", listOf(
                l("It is Saturday morning.", "It is Saturday morning.", "إنه صباح يوم السبت."),
                l("Lina goes to the market with her brother.", "Lina goes to the market with her brother.", "تذهب لينا إلى السوق مع أخيها."),
                l("They buy apples, tomatoes and fresh bread.", "They buy apples, tomatoes and fresh bread.", "يشتريان التفاح والطماطم والخبز الطازج."),
                l("The apples are red and sweet.", "The apples are red and sweet.", "التفاح أحمر وحلو."),
                l("Her brother wants a cold juice.", "Her brother wants a cold juice.", "يريد أخوها عصيرًا باردًا."),
                l("Lina pays ten dollars.", "Lina pays ten dollars.", "تدفع لينا عشرة دولارات."),
                l("They walk home and cook lunch together.", "They walk home and cook lunch together.", "يمشيان إلى البيت ويطبخان الغداء معًا."),
            ), listOf(
                w("Saturday", "Saturday", "السبت"), w("market", "market", "سوق"), w("brother", "brother", "أخ"), w("buy", "buy", "يشتري"),
                w("apples", "apples", "تفاح"), w("tomatoes", "tomatoes", "طماطم"), w("fresh", "fresh", "طازج"), w("bread", "bread", "خبز"),
                w("sweet", "sweet", "حلو"), w("wants", "wants", "يريد"), w("cold", "cold", "بارد"), w("juice", "juice", "عصير"),
                w("pays", "pays", "يدفع"), w("walk", "walk", "يمشي"), w("cook", "cook", "يطبخ"), w("lunch", "lunch", "غداء"), w("together", "together", "معًا"),
            ), listOf(
                q("en_st_market_q1", "When do they go to the market?", listOf("On Saturday", "On Monday", "On Friday"), 0),
                q("en_st_market_q2", "What does her brother want?", listOf("Tea", "A cold juice", "Water"), 1),
                q("en_st_market_q3", "How much does Lina pay?", listOf("Five dollars", "Ten dollars", "Twenty dollars"), 1),
            )),
            story("en_st_lost_bag", L, A2, "The lost bag", "The lost bag", "الحقيبة الضائعة", listOf(
                l("Last summer, Karim flew to London for the first time.", "Last summer, Karim flew to London for the first time.", "في الصيف الماضي، سافر كريم بالطائرة إلى لندن لأول مرة."),
                l("When he arrived at the airport, he waited for his bag.", "When he arrived at the airport, he waited for his bag.", "عندما وصل إلى المطار، انتظر حقيبته."),
                l("After an hour, the bag still wasn't there.", "After an hour, the bag still wasn't there.", "بعد ساعة، لم تكن الحقيبة قد وصلت بعد."),
                l("He went to the information desk and explained the problem.", "He went to the information desk and explained the problem.", "ذهب إلى مكتب الاستعلامات وشرح المشكلة."),
                l("The woman at the desk was very kind.", "The woman at the desk was very kind.", "كانت المرأة في المكتب لطيفة جدًا."),
                l("She wrote down his phone number and the address of his hotel.", "She wrote down his phone number and the address of his hotel.", "دوّنت رقم هاتفه وعنوان فندقه."),
                l("The next morning, someone knocked on his door.", "The next morning, someone knocked on his door.", "في الصباح التالي، طرق أحدهم بابه."),
                l("It was his bag! Karim was so relieved.", "It was his bag! Karim was so relieved.", "كانت حقيبته! شعر كريم بارتياح كبير."),
            ), listOf(
                w("summer", "summer", "صيف"), w("flew", "flew (past of fly)", "سافر بالطائرة"), w("for the first time", "for the first time", "لأول مرة"),
                w("arrived", "arrived", "وصل"), w("airport", "airport", "مطار"), w("waited", "waited", "انتظر"), w("bag", "bag", "حقيبة"),
                w("still", "still", "ما زال"), w("information desk", "information desk", "مكتب الاستعلامات"), w("explained", "explained", "شرح"),
                w("problem", "problem", "مشكلة"), w("kind", "kind", "لطيف"), w("wrote down", "wrote down", "دوّن"), w("address", "address", "عنوان"),
                w("knocked", "knocked", "طرق"), w("relieved", "relieved", "مرتاح بعد قلق"),
            ), listOf(
                q("en_st_lost_bag_q1", "Where did Karim fly to?", listOf("Paris", "London", "Dubai"), 1),
                q("en_st_lost_bag_q2", "Who helped Karim?", listOf("A taxi driver", "A woman at the information desk", "His friend"), 1),
                q("en_st_lost_bag_q3", "Where did Karim get his bag back?", listOf("At the airport", "At his hotel", "At the station"), 1),
            )),
            story("en_st_volunteer", L, B1, "Learning by helping", "Learning by helping", "التعلّم بالمساعدة", listOf(
                l("Maya had always wanted to improve her English, but she was too shy to speak.", "Maya had always wanted to improve her English, but she was too shy to speak.", "كانت مايا تريد دائمًا أن تحسّن لغتها الإنجليزية، لكنها كانت خجولة جدًا من التحدث."),
                l("One day, she noticed an advert for volunteers at a local library.", "One day, she noticed an advert for volunteers at a local library.", "في أحد الأيام، لاحظت إعلانًا عن متطوعين في مكتبة محلية."),
                l("The library needed people to read stories to children on Saturday mornings.", "The library needed people to read stories to children on Saturday mornings.", "كانت المكتبة تحتاج إلى أشخاص يقرؤون القصص للأطفال صباح أيام السبت."),
                l("At first, Maya was nervous and made a lot of mistakes.", "At first, Maya was nervous and made a lot of mistakes.", "في البداية، كانت مايا متوترة وارتكبت أخطاء كثيرة."),
                l("However, the children didn't mind at all; they just enjoyed the stories.", "However, the children didn't mind at all; they just enjoyed the stories.", "لكن الأطفال لم يمانعوا أبدًا؛ كانوا يستمتعون بالقصص فقط."),
                l("After a few months, she felt much more confident.", "After a few months, she felt much more confident.", "بعد بضعة أشهر، شعرت بثقة أكبر بكثير."),
                l("She had also learned many new words from the books she read aloud.", "She had also learned many new words from the books she read aloud.", "وكانت قد تعلّمت أيضًا كلمات جديدة كثيرة من الكتب التي قرأتها بصوت عالٍ."),
                l("Now she encourages her friends to volunteer too.", "Now she encourages her friends to volunteer too.", "والآن تشجّع صديقاتها على التطوع أيضًا."),
            ), listOf(
                w("improve", "improve", "يحسّن"), w("shy", "shy", "خجول"), w("noticed", "noticed", "لاحظ"), w("advert", "advert, advertisement", "إعلان"),
                w("volunteers", "volunteers", "متطوعون"), w("local", "local", "محلي"), w("library", "library", "مكتبة"), w("at first", "at first", "في البداية"),
                w("nervous", "nervous", "متوتر"), w("mistakes", "mistakes", "أخطاء"), w("However", "however", "لكن، ومع ذلك"),
                w("didn't mind", "didn't mind", "لم يمانعوا"), w("enjoyed", "enjoyed", "استمتع"), w("confident", "confident", "واثق من نفسه"),
                w("read aloud", "read aloud", "قرأ بصوت عالٍ"), w("encourages", "encourages", "يشجّع"), w("volunteer", "to volunteer", "يتطوّع"),
            ), listOf(
                q("en_st_volunteer_q1", "Why didn't Maya speak English before?", listOf("She was too shy", "She had no time", "She didn't like English"), 0),
                q("en_st_volunteer_q2", "What did Maya do at the library?", listOf("She cleaned the shelves", "She read stories to children", "She sold books"), 1),
                q("en_st_volunteer_q3", "How did Maya feel after a few months?", listOf("More confident", "More nervous", "Bored"), 0),
            )),
        )
    }

    private val spanish by lazy {
        val L = LanguageCode.SPANISH
        listOf(
            story("es_st_mercado", L, A1, "En el mercado", "At the market", "في السوق", listOf(
                l("Es sábado por la mañana.", "It is Saturday morning.", "إنه صباح يوم السبت."),
                l("Lucía va al mercado con su hermano.", "Lucía goes to the market with her brother.", "تذهب لوسيا إلى السوق مع أخيها."),
                l("Compran manzanas, tomates y pan fresco.", "They buy apples, tomatoes and fresh bread.", "يشتريان التفاح والطماطم والخبز الطازج."),
                l("Las manzanas son rojas y dulces.", "The apples are red and sweet.", "التفاح أحمر وحلو."),
                l("Su hermano quiere un zumo frío.", "Her brother wants a cold juice.", "يريد أخوها عصيرًا باردًا."),
                l("Lucía paga diez euros.", "Lucía pays ten euros.", "تدفع لوسيا عشرة يوروهات."),
                l("Vuelven a casa y cocinan juntos.", "They go back home and cook together.", "يعودان إلى البيت ويطبخان معًا."),
            ), listOf(
                w("sábado", "Saturday", "السبت"), w("por la mañana", "in the morning", "في الصباح"), w("mercado", "market", "سوق"), w("hermano", "brother", "أخ"),
                w("Compran", "they buy", "يشتريان"), w("manzanas", "apples", "تفاح"), w("tomates", "tomatoes", "طماطم"), w("pan", "bread", "خبز"),
                w("fresco", "fresh", "طازج"), w("rojas", "red (fem. plural)", "حمراء"), w("dulces", "sweet (plural)", "حلوة"), w("quiere", "wants", "يريد"),
                w("zumo", "juice (Spain)", "عصير"), w("frío", "cold", "بارد"), w("paga", "pays", "يدفع / تدفع"), w("Vuelven a casa", "they go back home", "يعودان إلى البيت"),
                w("cocinan", "they cook", "يطبخان"), w("juntos", "together", "معًا"),
            ), listOf(
                q("es_st_mercado_q1", "¿Cuándo van al mercado?", listOf("El sábado", "El lunes", "El viernes"), 0),
                q("es_st_mercado_q2", "¿Qué quiere su hermano?", listOf("Un café", "Un zumo frío", "Agua"), 1),
                q("es_st_mercado_q3", "¿Cuánto paga Lucía?", listOf("Cinco euros", "Diez euros", "Veinte euros"), 1),
            )),
            story("es_st_maleta", L, A2, "La maleta perdida", "The lost suitcase", "الحقيبة الضائعة", listOf(
                l("El verano pasado, Carlos viajó a Madrid por primera vez.", "Last summer, Carlos travelled to Madrid for the first time.", "في الصيف الماضي، سافر كارلوس إلى مدريد لأول مرة."),
                l("Cuando llegó al aeropuerto, esperó su maleta.", "When he arrived at the airport, he waited for his suitcase.", "عندما وصل إلى المطار، انتظر حقيبته."),
                l("Después de una hora, la maleta todavía no estaba allí.", "After an hour, the suitcase still wasn't there.", "بعد ساعة، لم تكن الحقيبة هناك بعد."),
                l("Fue al mostrador de información y explicó el problema.", "He went to the information desk and explained the problem.", "ذهب إلى مكتب الاستعلامات وشرح المشكلة."),
                l("La mujer del mostrador fue muy amable.", "The woman at the desk was very kind.", "كانت المرأة في المكتب لطيفة جدًا."),
                l("Apuntó su número de teléfono y la dirección de su hotel.", "She wrote down his phone number and the address of his hotel.", "دوّنت رقم هاتفه وعنوان فندقه."),
                l("A la mañana siguiente, alguien llamó a la puerta.", "The next morning, someone knocked at the door.", "في الصباح التالي، طرق أحدهم الباب."),
                l("¡Era su maleta! Carlos se sintió muy aliviado.", "It was his suitcase! Carlos felt very relieved.", "كانت حقيبته! شعر كارلوس بارتياح كبير."),
            ), listOf(
                w("verano", "summer", "صيف"), w("pasado", "last, past", "الماضي"), w("viajó", "travelled", "سافر"), w("por primera vez", "for the first time", "لأول مرة"),
                w("llegó", "arrived", "وصل"), w("aeropuerto", "airport", "مطار"), w("esperó", "waited (for)", "انتظر"), w("maleta", "suitcase", "حقيبة سفر"),
                w("todavía", "still", "ما زال"), w("mostrador de información", "information desk", "مكتب الاستعلامات"), w("explicó", "explained", "شرح"),
                w("amable", "kind", "لطيف"), w("Apuntó", "wrote down", "دوّن"), w("dirección", "address", "عنوان"), w("A la mañana siguiente", "the next morning", "في الصباح التالي"),
                w("llamó a la puerta", "knocked at the door", "طرق الباب"), w("aliviado", "relieved", "مرتاح بعد قلق"),
            ), listOf(
                q("es_st_maleta_q1", "¿Adónde viajó Carlos?", listOf("A Barcelona", "A Madrid", "A Sevilla"), 1),
                q("es_st_maleta_q2", "¿Quién ayudó a Carlos?", listOf("Un taxista", "Una mujer del mostrador", "Su amigo"), 1),
                q("es_st_maleta_q3", "¿Dónde recibió Carlos su maleta?", listOf("En el aeropuerto", "En su hotel", "En la estación"), 1),
            )),
            story("es_st_voluntario", L, B1, "Aprender ayudando", "Learning by helping", "التعلّم بالمساعدة", listOf(
                l("Cuando Amir se mudó a Valencia, era demasiado tímido para hablar español.", "When Amir moved to Valencia, he was too shy to speak Spanish.", "عندما انتقل أمير إلى فالنسيا، كان خجولًا جدًا من التحدث بالإسبانية."),
                l("Un día vio un anuncio: la biblioteca del barrio buscaba voluntarios.", "One day he saw an advert: the neighbourhood library was looking for volunteers.", "في أحد الأيام رأى إعلانًا: كانت مكتبة الحي تبحث عن متطوعين."),
                l("Necesitaban personas para leer cuentos a los niños los sábados.", "They needed people to read stories to the children on Saturdays.", "كانوا يحتاجون إلى أشخاص يقرؤون القصص للأطفال أيام السبت."),
                l("Al principio, Amir estaba nervioso y cometía muchos errores.", "At first, Amir was nervous and made many mistakes.", "في البداية، كان أمير متوترًا ويرتكب أخطاء كثيرة."),
                l("Sin embargo, a los niños no les importaba; solo disfrutaban de los cuentos.", "However, the children didn't mind; they just enjoyed the stories.", "لكن الأطفال لم يهتموا بذلك؛ كانوا يستمتعون بالقصص فقط."),
                l("Después de unos meses, se sentía mucho más seguro.", "After a few months, he felt much more confident.", "بعد بضعة أشهر، صار يشعر بثقة أكبر بكثير."),
                l("Además, había aprendido muchas palabras nuevas de los libros.", "He had also learned many new words from the books.", "كما أنه تعلّم كلمات جديدة كثيرة من الكتب."),
                l("Ahora anima a sus amigos a hacer de voluntarios también.", "Now he encourages his friends to volunteer too.", "والآن يشجّع أصدقاءه على التطوع أيضًا."),
            ), listOf(
                w("se mudó", "moved (house)", "انتقل"), w("demasiado", "too (much)", "أكثر من اللازم"), w("tímido", "shy", "خجول"), w("anuncio", "advert", "إعلان"),
                w("biblioteca", "library", "مكتبة"), w("barrio", "neighbourhood", "حي"), w("buscaba", "was looking for", "كانت تبحث عن"), w("voluntarios", "volunteers", "متطوعون"),
                w("cuentos", "stories, tales", "قصص"), w("Al principio", "at first", "في البداية"), w("nervioso", "nervous", "متوتر"),
                w("cometía muchos errores", "made many mistakes", "يرتكب أخطاء كثيرة"), w("Sin embargo", "however", "لكن، ومع ذلك"),
                w("no les importaba", "they didn't mind", "لم يهتموا"), w("disfrutaban", "they enjoyed", "كانوا يستمتعون"), w("seguro", "confident, sure", "واثق"),
                w("Además", "also, besides", "إضافة إلى ذلك"), w("anima", "encourages", "يشجّع"),
            ), listOf(
                q("es_st_voluntario_q1", "¿Por qué Amir no hablaba español al principio?", listOf("Era muy tímido", "No tenía tiempo", "No le gustaba el español"), 0),
                q("es_st_voluntario_q2", "¿Qué hacía Amir en la biblioteca?", listOf("Limpiaba las mesas", "Leía cuentos a los niños", "Vendía libros"), 1),
                q("es_st_voluntario_q3", "¿Cómo se sentía después de unos meses?", listOf("Más seguro", "Más nervioso", "Aburrido"), 0),
            )),
        )
    }

    private val french by lazy {
        val L = LanguageCode.FRENCH
        listOf(
            story("fr_st_marche", L, A1, "Au marché", "At the market", "في السوق", listOf(
                l("C'est samedi matin.", "It is Saturday morning.", "إنه صباح يوم السبت."),
                l("Léa va au marché avec son frère.", "Léa goes to the market with her brother.", "تذهب ليا إلى السوق مع أخيها."),
                l("Ils achètent des pommes, des tomates et du pain frais.", "They buy apples, tomatoes and fresh bread.", "يشتريان التفاح والطماطم والخبز الطازج."),
                l("Les pommes sont rouges et sucrées.", "The apples are red and sweet.", "التفاح أحمر وحلو."),
                l("Son frère veut un jus froid.", "Her brother wants a cold juice.", "يريد أخوها عصيرًا باردًا."),
                l("Léa paie dix euros.", "Léa pays ten euros.", "تدفع ليا عشرة يوروهات."),
                l("Ils rentrent à la maison et cuisinent ensemble.", "They go back home and cook together.", "يعودان إلى البيت ويطبخان معًا."),
            ), listOf(
                w("samedi", "Saturday", "السبت"), w("matin", "morning", "صباح"), w("marché", "market", "سوق"), w("frère", "brother", "أخ"),
                w("achètent", "they buy", "يشتريان"), w("pommes", "apples", "تفاح"), w("tomates", "tomatoes", "طماطم"), w("pain", "bread", "خبز"),
                w("frais", "fresh", "طازج"), w("rouges", "red (plural)", "حمراء"), w("sucrées", "sweet (fem. plural)", "حلوة"), w("veut", "wants", "يريد"),
                w("jus", "juice", "عصير"), w("froid", "cold", "بارد"), w("paie", "pays", "يدفع / تدفع"), w("rentrent à la maison", "go back home", "يعودان إلى البيت"),
                w("cuisinent", "they cook", "يطبخان"), w("ensemble", "together", "معًا"),
            ), listOf(
                q("fr_st_marche_q1", "Quand vont-ils au marché ?", listOf("Le samedi", "Le lundi", "Le vendredi"), 0),
                q("fr_st_marche_q2", "Que veut son frère ?", listOf("Un café", "Un jus froid", "De l'eau"), 1),
                q("fr_st_marche_q3", "Combien Léa paie-t-elle ?", listOf("Cinq euros", "Dix euros", "Vingt euros"), 1),
            )),
            story("fr_st_valise", L, A2, "La valise perdue", "The lost suitcase", "الحقيبة الضائعة", listOf(
                l("L'été dernier, Karim a pris l'avion pour Paris pour la première fois.", "Last summer, Karim flew to Paris for the first time.", "في الصيف الماضي، سافر كريم بالطائرة إلى باريس لأول مرة."),
                l("Quand il est arrivé à l'aéroport, il a attendu sa valise.", "When he arrived at the airport, he waited for his suitcase.", "عندما وصل إلى المطار، انتظر حقيبته."),
                l("Après une heure, la valise n'était toujours pas là.", "After an hour, the suitcase still wasn't there.", "بعد ساعة، لم تكن الحقيبة هناك بعد."),
                l("Il est allé au comptoir d'information et il a expliqué le problème.", "He went to the information desk and explained the problem.", "ذهب إلى مكتب الاستعلامات وشرح المشكلة."),
                l("La femme au comptoir était très gentille.", "The woman at the desk was very kind.", "كانت المرأة في المكتب لطيفة جدًا."),
                l("Elle a noté son numéro de téléphone et l'adresse de son hôtel.", "She wrote down his phone number and the address of his hotel.", "دوّنت رقم هاتفه وعنوان فندقه."),
                l("Le lendemain matin, quelqu'un a frappé à sa porte.", "The next morning, someone knocked on his door.", "في صباح اليوم التالي، طرق أحدهم بابه."),
                l("C'était sa valise ! Karim était très soulagé.", "It was his suitcase! Karim was very relieved.", "كانت حقيبته! شعر كريم بارتياح كبير."),
            ), listOf(
                w("été", "summer", "صيف"), w("dernier", "last", "الماضي"), w("a pris l'avion", "took the plane, flew", "سافر بالطائرة"),
                w("pour la première fois", "for the first time", "لأول مرة"), w("est arrivé", "arrived", "وصل"), w("aéroport", "airport", "مطار"),
                w("a attendu", "waited (for)", "انتظر"), w("valise", "suitcase", "حقيبة سفر"), w("toujours pas", "still not", "ما زال لا"),
                w("comptoir d'information", "information desk", "مكتب الاستعلامات"), w("a expliqué", "explained", "شرح"), w("gentille", "kind (fem.)", "لطيفة"),
                w("a noté", "wrote down", "دوّنت"), w("adresse", "address", "عنوان"), w("Le lendemain", "the next day", "اليوم التالي"),
                w("a frappé", "knocked", "طرق"), w("soulagé", "relieved", "مرتاح بعد قلق"),
            ), listOf(
                q("fr_st_valise_q1", "Où Karim est-il allé ?", listOf("À Lyon", "À Paris", "À Nice"), 1),
                q("fr_st_valise_q2", "Qui a aidé Karim ?", listOf("Un chauffeur de taxi", "Une femme au comptoir", "Son ami"), 1),
                q("fr_st_valise_q3", "Où Karim a-t-il retrouvé sa valise ?", listOf("À l'aéroport", "À son hôtel", "À la gare"), 1),
            )),
            story("fr_st_benevole", L, B1, "Apprendre en aidant", "Learning by helping", "التعلّم بالمساعدة", listOf(
                l("Quand Amir s'est installé à Lyon, il était trop timide pour parler français.", "When Amir settled in Lyon, he was too shy to speak French.", "عندما استقر أمير في ليون، كان خجولًا جدًا من التحدث بالفرنسية."),
                l("Un jour, il a vu une annonce : la bibliothèque du quartier cherchait des bénévoles.", "One day he saw an advert: the neighbourhood library was looking for volunteers.", "في أحد الأيام رأى إعلانًا: كانت مكتبة الحي تبحث عن متطوعين."),
                l("Elle avait besoin de personnes pour lire des histoires aux enfants le samedi.", "It needed people to read stories to the children on Saturdays.", "كانت تحتاج إلى أشخاص يقرؤون القصص للأطفال أيام السبت."),
                l("Au début, Amir était nerveux et faisait beaucoup de fautes.", "At first, Amir was nervous and made a lot of mistakes.", "في البداية، كان أمير متوترًا ويرتكب أخطاء كثيرة."),
                l("Pourtant, cela ne dérangeait pas les enfants : ils aimaient simplement les histoires.", "However, the children didn't mind: they simply loved the stories.", "لكن ذلك لم يزعج الأطفال: كانوا يحبون القصص فحسب."),
                l("Au bout de quelques mois, il se sentait beaucoup plus sûr de lui.", "After a few months, he felt much more confident.", "بعد بضعة أشهر، صار يشعر بثقة أكبر بكثير."),
                l("Il avait aussi appris beaucoup de nouveaux mots grâce aux livres.", "He had also learned a lot of new words thanks to the books.", "كما أنه تعلّم كلمات جديدة كثيرة بفضل الكتب."),
                l("Maintenant, il encourage ses amis à faire du bénévolat aussi.", "Now he encourages his friends to volunteer too.", "والآن يشجّع أصدقاءه على التطوع أيضًا."),
            ), listOf(
                w("s'est installé", "settled, moved in", "استقر"), w("trop", "too (much)", "أكثر من اللازم"), w("timide", "shy", "خجول"), w("annonce", "advert", "إعلان"),
                w("bibliothèque", "library", "مكتبة"), w("quartier", "neighbourhood", "حي"), w("cherchait", "was looking for", "كانت تبحث عن"), w("bénévoles", "volunteers", "متطوعون"),
                w("avait besoin de", "needed", "كانت تحتاج إلى"), w("histoires", "stories", "قصص"), w("Au début", "at first", "في البداية"), w("nerveux", "nervous", "متوتر"),
                w("fautes", "mistakes", "أخطاء"), w("Pourtant", "however, yet", "لكن، ومع ذلك"), w("ne dérangeait pas", "didn't bother", "لم يزعج"),
                w("Au bout de", "after (a period)", "بعد مرور"), w("sûr de lui", "confident", "واثق من نفسه"), w("grâce aux", "thanks to", "بفضل"),
                w("encourage", "encourages", "يشجّع"), w("bénévolat", "volunteering", "العمل التطوعي"),
            ), listOf(
                q("fr_st_benevole_q1", "Pourquoi Amir ne parlait-il pas français au début ?", listOf("Il était trop timide", "Il n'avait pas le temps", "Il n'aimait pas le français"), 0),
                q("fr_st_benevole_q2", "Que faisait Amir à la bibliothèque ?", listOf("Il rangeait les livres", "Il lisait des histoires aux enfants", "Il vendait des livres"), 1),
                q("fr_st_benevole_q3", "Comment se sentait-il après quelques mois ?", listOf("Plus sûr de lui", "Plus nerveux", "Fatigué"), 0),
            )),
        )
    }

    private val mandarin by lazy {
        val L = LanguageCode.MANDARIN
        listOf(
            story("zh_st_shichang", L, A1, "在市场", "At the market", "في السوق", listOf(
                l("今天是星期六。", "Today is Saturday.", "اليوم هو السبت."),
                l("小丽和哥哥去市场。", "Xiaoli and her older brother go to the market.", "تذهب شياو لي وأخوها الأكبر إلى السوق."),
                l("他们买苹果、西红柿和面包。", "They buy apples, tomatoes and bread.", "يشتريان التفاح والطماطم والخبز."),
                l("苹果很红，也很甜。", "The apples are very red and also very sweet.", "التفاح أحمر جدًا وحلو أيضًا."),
                l("哥哥想喝冰果汁。", "Her brother wants to drink an iced juice.", "يريد أخوها أن يشرب عصيرًا مثلجًا."),
                l("小丽付了十块钱。", "Xiaoli paid ten yuan.", "دفعت شياو لي عشرة يوانات."),
                l("他们回家一起做午饭。", "They go home and make lunch together.", "يعودان إلى البيت ويحضّران الغداء معًا."),
            ), listOf(
                w("今天", "today", "اليوم", "jīntiān"), w("星期六", "Saturday", "السبت", "xīngqīliù"), w("小丽", "Xiaoli (a name)", "شياو لي (اسم)", "Xiǎolì"),
                w("哥哥", "older brother", "الأخ الأكبر", "gēge"), w("市场", "market", "سوق", "shìchǎng"), w("他们", "they", "هم", "tāmen"), w("买", "to buy", "يشتري", "mǎi"),
                w("苹果", "apple", "تفاح", "píngguǒ"), w("西红柿", "tomato", "طماطم", "xīhóngshì"), w("面包", "bread", "خبز", "miànbāo"), w("甜", "sweet", "حلو", "tián"),
                w("想", "to want to", "يريد أن", "xiǎng"), w("冰", "ice; iced", "ثلج؛ مثلّج", "bīng"), w("果汁", "juice", "عصير", "guǒzhī"), w("付", "to pay", "يدفع", "fù"),
                w("块", "yuan (spoken)", "يوان (بالعامية)", "kuài"), w("钱", "money", "مال", "qián"), w("回家", "to go home", "يعود إلى البيت", "huí jiā"),
                w("一起", "together", "معًا", "yìqǐ"), w("做", "to make, to do", "يصنع، يفعل", "zuò"), w("午饭", "lunch", "غداء", "wǔfàn"),
            ), listOf(
                q("zh_st_shichang_q1", "他们哪天去市场？", listOf("星期六", "星期一", "星期五"), 0, g("Which day do they go to the market?", "في أي يوم يذهبان إلى السوق؟"),
                    listOf(g("Saturday", "السبت"), g("Monday", "الاثنين"), g("Friday", "الجمعة"))),
                q("zh_st_shichang_q2", "哥哥想喝什么？", listOf("茶", "冰果汁", "水"), 1, g("What does her brother want to drink?", "ماذا يريد أخوها أن يشرب؟"),
                    listOf(g("tea", "شاي"), g("iced juice", "عصير مثلّج"), g("water", "ماء"))),
                q("zh_st_shichang_q3", "小丽付了多少钱？", listOf("五块", "十块", "二十块"), 1, g("How much did Xiaoli pay?", "كم دفعت شياو لي؟"),
                    listOf(g("five yuan", "خمسة يوانات"), g("ten yuan", "عشرة يوانات"), g("twenty yuan", "عشرون يوانًا"))),
            )),
            story("zh_st_xinglixiang", L, A2, "丢了的行李箱", "The lost suitcase", "الحقيبة الضائعة", listOf(
                l("去年夏天，大卫第一次坐飞机去北京。", "Last summer, David flew to Beijing for the first time.", "في الصيف الماضي، سافر ديفيد بالطائرة إلى بكين لأول مرة."),
                l("到了机场以后，他等他的行李箱。", "After he got to the airport, he waited for his suitcase.", "بعد أن وصل إلى المطار، انتظر حقيبته."),
                l("等了一个小时，行李箱还没有来。", "He waited for an hour, and the suitcase still hadn't come.", "انتظر ساعة، ولم تصل الحقيبة بعد."),
                l("他去服务台说了这个问题。", "He went to the service desk and explained the problem.", "ذهب إلى مكتب الخدمة وشرح المشكلة."),
                l("服务台的小姐非常热情。", "The woman at the service desk was very warm and helpful.", "كانت الموظفة في مكتب الخدمة ودودة جدًا."),
                l("她写下了他的电话号码和酒店的地址。", "She wrote down his phone number and the hotel's address.", "دوّنت رقم هاتفه وعنوان الفندق."),
                l("第二天早上，有人敲门。", "The next morning, someone knocked on the door.", "في صباح اليوم التالي، طرق أحدهم الباب."),
                l("是他的行李箱！大卫放心了。", "It was his suitcase! David was relieved.", "كانت حقيبته! اطمأن ديفيد."),
            ), listOf(
                w("去年", "last year", "العام الماضي", "qùnián"), w("夏天", "summer", "صيف", "xiàtiān"), w("大卫", "David (a name)", "ديفيد (اسم)", "Dàwèi"),
                w("第一次", "the first time", "المرة الأولى", "dì-yī cì"), w("坐飞机", "to take a plane", "يركب الطائرة", "zuò fēijī"), w("北京", "Beijing", "بكين", "Běijīng"),
                w("机场", "airport", "مطار", "jīchǎng"), w("以后", "after", "بعد", "yǐhòu"), w("等", "to wait", "ينتظر", "děng"), w("行李箱", "suitcase", "حقيبة سفر", "xínglixiāng"),
                w("小时", "hour", "ساعة", "xiǎoshí"), w("还没有", "still not", "لم … بعد", "hái méiyǒu"), w("服务台", "service desk", "مكتب الخدمة", "fúwùtái"),
                w("问题", "problem; question", "مشكلة؛ سؤال", "wèntí"), w("小姐", "young woman; Miss", "آنسة", "xiǎojiě"), w("热情", "warm, friendly", "ودود، متحمّس", "rèqíng"),
                w("写下", "to write down", "يدوّن", "xiěxià"), w("电话号码", "phone number", "رقم الهاتف", "diànhuà hàomǎ"), w("酒店", "hotel", "فندق", "jiǔdiàn"),
                w("地址", "address", "عنوان", "dìzhǐ"), w("第二天", "the next day", "اليوم التالي", "dì-èr tiān"), w("有人", "someone", "أحدهم", "yǒu rén"),
                w("敲门", "to knock on the door", "يطرق الباب", "qiāo mén"), w("放心", "to feel relieved", "يطمئن", "fàngxīn"),
            ), listOf(
                q("zh_st_xinglixiang_q1", "大卫去了哪儿？", listOf("上海", "北京", "香港"), 1, g("Where did David go?", "إلى أين ذهب ديفيد؟"),
                    listOf(g("Shanghai", "شنغهاي"), g("Beijing", "بكين"), g("Hong Kong", "هونغ كونغ"))),
                q("zh_st_xinglixiang_q2", "谁帮助了大卫？", listOf("出租车司机", "服务台的小姐", "他的朋友"), 1, g("Who helped David?", "من ساعد ديفيد؟"),
                    listOf(g("a taxi driver", "سائق تاكسي"), g("the woman at the service desk", "الموظفة في مكتب الخدمة"), g("his friend", "صديقه"))),
                q("zh_st_xinglixiang_q3", "大卫在哪儿拿到了行李箱？", listOf("在机场", "在酒店", "在火车站"), 1, g("Where did David get his suitcase?", "أين استلم ديفيد حقيبته؟"),
                    listOf(g("at the airport", "في المطار"), g("at the hotel", "في الفندق"), g("at the train station", "في محطة القطار"))),
            )),
            story("zh_st_zhiyuanzhe", L, B1, "一边帮忙，一边学习", "Learning by helping", "التعلّم بالمساعدة", listOf(
                l("阿米尔刚搬到上海的时候，不好意思说中文。", "When Amir had just moved to Shanghai, he was too embarrassed to speak Chinese.", "عندما انتقل أمير إلى شنغهاي حديثًا، كان يخجل من التحدث بالصينية."),
                l("有一天，他看到一个广告：附近的图书馆需要志愿者。", "One day he saw an advert: the library nearby needed volunteers.", "في أحد الأيام رأى إعلانًا: المكتبة القريبة تحتاج إلى متطوعين."),
                l("图书馆希望有人每个星期六给孩子们读故事。", "The library hoped someone would read stories to the children every Saturday.", "كانت المكتبة تأمل أن يقرأ أحدهم القصص للأطفال كل سبت."),
                l("开始的时候，阿米尔很紧张，常常说错。", "At first, Amir was very nervous and often said things wrong.", "في البداية، كان أمير متوترًا جدًا وكثيرًا ما يخطئ في الكلام."),
                l("可是孩子们一点儿也不在意，他们只是喜欢听故事。", "But the children didn't mind at all; they just liked listening to stories.", "لكن الأطفال لم يهتموا بذلك أبدًا؛ كانوا يحبون الاستماع إلى القصص فحسب."),
                l("几个月以后，他觉得自己有信心多了。", "After a few months, he felt much more confident.", "بعد بضعة أشهر، شعر بثقة أكبر بكثير."),
                l("他也从书里学会了很多新词。", "He also learned many new words from the books.", "وتعلّم أيضًا كلمات جديدة كثيرة من الكتب."),
                l("现在，他鼓励朋友们也去当志愿者。", "Now he encourages his friends to become volunteers too.", "والآن يشجّع أصدقاءه على أن يصبحوا متطوعين أيضًا."),
            ), listOf(
                w("阿米尔", "Amir (a name)", "أمير (اسم)", "Āmǐ'ěr"), w("刚", "just (recently)", "للتوّ", "gāng"), w("搬", "to move (house)", "ينتقل", "bān"),
                w("上海", "Shanghai", "شنغهاي", "Shànghǎi"), w("的时候", "when …", "عندما", "de shíhou"), w("不好意思", "to feel embarrassed", "يخجل", "bù hǎoyìsi"),
                w("广告", "advertisement", "إعلان", "guǎnggào"), w("附近", "nearby", "قريب", "fùjìn"), w("图书馆", "library", "مكتبة", "túshūguǎn"),
                w("需要", "to need", "يحتاج", "xūyào"), w("志愿者", "volunteer", "متطوّع", "zhìyuànzhě"), w("希望", "to hope", "يأمل", "xīwàng"),
                w("孩子们", "children", "الأطفال", "háizimen"), w("故事", "story", "قصة", "gùshi"), w("开始", "beginning; to begin", "البداية؛ يبدأ", "kāishǐ"),
                w("紧张", "nervous", "متوتر", "jǐnzhāng"), w("常常", "often", "كثيرًا", "chángcháng"), w("说错", "to say wrongly", "يخطئ في الكلام", "shuō cuò"),
                w("可是", "but", "لكن", "kěshì"), w("一点儿也不", "not at all", "لا … أبدًا", "yìdiǎnr yě bù"), w("在意", "to mind, to care", "يهتم", "zàiyì"),
                w("几个月", "a few months", "بضعة أشهر", "jǐ ge yuè"), w("觉得", "to feel, to think", "يشعر، يظن", "juéde"), w("自己", "oneself", "نفسه", "zìjǐ"),
                w("信心", "confidence", "ثقة", "xìnxīn"), w("学会", "to learn (and master)", "يتعلّم ويتقن", "xuéhuì"), w("新词", "new words", "كلمات جديدة", "xīn cí"),
                w("现在", "now", "الآن", "xiànzài"), w("鼓励", "to encourage", "يشجّع", "gǔlì"), w("当", "to work as, to become", "يصبح، يعمل كـ", "dāng"),
            ), listOf(
                q("zh_st_zhiyuanzhe_q1", "开始的时候，阿米尔为什么不说中文？", listOf("他不好意思", "他没有时间", "他不喜欢中文"), 0, g("Why didn't Amir speak Chinese at first?", "لماذا لم يتحدث أمير بالصينية في البداية؟"),
                    listOf(g("he felt embarrassed", "كان يخجل"), g("he had no time", "لم يكن لديه وقت"), g("he didn't like Chinese", "لم يحب الصينية"))),
                q("zh_st_zhiyuanzhe_q2", "阿米尔在图书馆做什么？", listOf("打扫", "给孩子们读故事", "卖书"), 1, g("What did Amir do at the library?", "ماذا كان أمير يفعل في المكتبة؟"),
                    listOf(g("cleaning", "التنظيف"), g("reading stories to the children", "يقرأ القصص للأطفال"), g("selling books", "يبيع الكتب"))),
                q("zh_st_zhiyuanzhe_q3", "几个月以后，他觉得怎么样？", listOf("更有信心了", "更紧张了", "很累"), 0, g("How did he feel after a few months?", "كيف شعر بعد بضعة أشهر؟"),
                    listOf(g("more confident", "بثقة أكبر"), g("more nervous", "بتوتر أكبر"), g("very tired", "متعب جدًا"))),
            )),
        )
    }

    private val arabic by lazy {
        val L = LanguageCode.ARABIC
        fun same(text: String, en: String) = l(text, en, text)
        listOf(
            story("ar_st_souq", L, A1, "في السوق", "At the market", "في السوق", listOf(
                same("اليومُ يومُ السبت.", "Today is Saturday."),
                same("تذهبُ ليلى إلى السوق مع أخيها.", "Layla goes to the market with her brother."),
                same("يشتريان التفاحَ والطماطمَ والخبزَ الطازج.", "They buy apples, tomatoes and fresh bread."),
                same("التفاحُ أحمرُ وحلو.", "The apples are red and sweet."),
                same("يريدُ أخوها عصيرًا باردًا.", "Her brother wants a cold juice."),
                same("تدفعُ ليلى عشرةَ دنانير.", "Layla pays ten dinars."),
                same("يرجعان إلى البيت ويطبخان الغداءَ معًا.", "They go back home and cook lunch together."),
            ), listOf(
                w("اليوم", "today", "اليوم", "al-yawm"), w("السبت", "Saturday", "السبت", "as-sabt"), w("تذهب", "she goes", "تذهب", "tadhhab"),
                w("السوق", "the market", "السوق", "as-sūq"), w("أخيها", "her brother", "أخوها", "akhīhā"), w("يشتريان", "they (two) buy", "يشتريان", "yashtariyān"),
                w("التفاح", "apples", "التفاح", "at-tuffāḥ"), w("الطماطم", "tomatoes", "الطماطم", "aṭ-ṭamāṭim"), w("الخبز", "bread", "الخبز", "al-khubz"),
                w("الطازج", "fresh", "الطازج", "aṭ-ṭāzij"), w("أحمر", "red", "أحمر", "aḥmar"), w("حلو", "sweet", "حلو", "ḥulw"), w("يريد", "he wants", "يريد", "yurīd"),
                w("أخوها", "her brother", "أخوها", "akhūhā"), w("عصيرًا", "juice", "عصير", "ʿaṣīran"), w("باردًا", "cold", "بارد", "bāridan"),
                w("تدفع", "she pays", "تدفع", "tadfaʿ"), w("دنانير", "dinars", "دنانير", "danānīr"), w("يرجعان", "they (two) go back", "يرجعان", "yarjiʿān"),
                w("البيت", "the house, home", "البيت", "al-bayt"), w("يطبخان", "they (two) cook", "يطبخان", "yaṭbukhān"), w("الغداء", "lunch", "الغداء", "al-ghadāʾ"),
                w("معًا", "together", "معًا", "maʿan"),
            ), listOf(
                q("ar_st_souq_q1", "متى تذهب ليلى إلى السوق؟", listOf("يوم السبت", "يوم الاثنين", "يوم الجمعة"), 0, g("When does Layla go to the market?", "متى تذهب ليلى إلى السوق؟"),
                    listOf(g("on Saturday", "يوم السبت"), g("on Monday", "يوم الاثنين"), g("on Friday", "يوم الجمعة"))),
                q("ar_st_souq_q2", "ماذا يريد أخوها؟", listOf("شايًا", "عصيرًا باردًا", "ماءً"), 1, g("What does her brother want?", "ماذا يريد أخوها؟"),
                    listOf(g("tea", "شايًا"), g("a cold juice", "عصيرًا باردًا"), g("water", "ماءً"))),
                q("ar_st_souq_q3", "كم تدفع ليلى؟", listOf("خمسة دنانير", "عشرة دنانير", "عشرين دينارًا"), 1, g("How much does Layla pay?", "كم تدفع ليلى؟"),
                    listOf(g("five dinars", "خمسة دنانير"), g("ten dinars", "عشرة دنانير"), g("twenty dinars", "عشرين دينارًا"))),
            )),
            story("ar_st_haqiba", L, A2, "الحقيبة الضائعة", "The lost bag", "الحقيبة الضائعة", listOf(
                same("في الصيف الماضي، سافر كريم إلى القاهرة بالطائرة لأول مرة.", "Last summer, Karim flew to Cairo for the first time."),
                same("عندما وصل إلى المطار، انتظر حقيبته.", "When he arrived at the airport, he waited for his bag."),
                same("بعد ساعة، لم تصل الحقيبة بعد.", "After an hour, the bag still hadn't arrived."),
                same("ذهب إلى مكتب الاستعلامات وشرح المشكلة.", "He went to the information desk and explained the problem."),
                same("كانت الموظفة لطيفة جدًا.", "The employee (a woman) was very kind."),
                same("كتبت رقم هاتفه وعنوان فندقه.", "She wrote down his phone number and the address of his hotel."),
                same("في صباح اليوم التالي، طرق أحدهم الباب.", "The next morning, someone knocked on the door."),
                same("كانت حقيبته! شعر كريم بارتياح كبير.", "It was his bag! Karim felt very relieved."),
            ), listOf(
                w("الصيف", "summer", "الصيف", "aṣ-ṣayf"), w("الماضي", "last, past", "الماضي", "al-māḍī"), w("سافر", "he travelled", "سافر", "sāfara"),
                w("القاهرة", "Cairo", "القاهرة", "al-qāhira"), w("بالطائرة", "by plane", "بالطائرة", "biṭ-ṭāʾira"), w("لأول مرة", "for the first time", "لأول مرة", "li-awwali marra"),
                w("وصل", "he arrived", "وصل", "waṣala"), w("المطار", "the airport", "المطار", "al-maṭār"), w("انتظر", "he waited (for)", "انتظر", "intaẓara"),
                w("حقيبته", "his bag", "حقيبته", "ḥaqībatahu"), w("ساعة", "an hour", "ساعة", "sāʿa"), w("لم تصل", "did not arrive", "لم تصل", "lam taṣil"),
                w("مكتب الاستعلامات", "information desk", "مكتب الاستعلامات", "maktab al-istiʿlāmāt"), w("شرح", "he explained", "شرح", "sharaḥa"),
                w("المشكلة", "the problem", "المشكلة", "al-mushkila"), w("الموظفة", "the (female) employee", "الموظفة", "al-muwaẓẓafa"), w("لطيفة", "kind (fem.)", "لطيفة", "laṭīfa"),
                w("كتبت", "she wrote", "كتبت", "katabat"), w("رقم هاتفه", "his phone number", "رقم هاتفه", "raqm hātifihi"), w("عنوان", "address", "عنوان", "ʿunwān"),
                w("فندقه", "his hotel", "فندقه", "funduqihi"), w("التالي", "the next", "التالي", "at-tālī"), w("طرق", "he knocked", "طرق", "ṭaraqa"),
                w("أحدهم", "someone", "أحدهم", "aḥaduhum"), w("الباب", "the door", "الباب", "al-bāb"), w("بارتياح", "with relief", "بارتياح", "bi-rtiyāḥ"),
            ), listOf(
                q("ar_st_haqiba_q1", "إلى أين سافر كريم؟", listOf("إلى دبي", "إلى القاهرة", "إلى بيروت"), 1, g("Where did Karim travel to?", "إلى أين سافر كريم؟"),
                    listOf(g("to Dubai", "إلى دبي"), g("to Cairo", "إلى القاهرة"), g("to Beirut", "إلى بيروت"))),
                q("ar_st_haqiba_q2", "مَن ساعد كريمًا؟", listOf("سائق تاكسي", "الموظفة في مكتب الاستعلامات", "صديقه"), 1, g("Who helped Karim?", "مَن ساعد كريمًا؟"),
                    listOf(g("a taxi driver", "سائق تاكسي"), g("the employee at the information desk", "الموظفة في مكتب الاستعلامات"), g("his friend", "صديقه"))),
                q("ar_st_haqiba_q3", "أين استلم كريم حقيبته؟", listOf("في المطار", "في الفندق", "في المحطة"), 1, g("Where did Karim get his bag back?", "أين استلم كريم حقيبته؟"),
                    listOf(g("at the airport", "في المطار"), g("at the hotel", "في الفندق"), g("at the station", "في المحطة"))),
            )),
            story("ar_st_tatawwu", L, B1, "التعلّم بالمساعدة", "Learning by helping", "التعلّم بالمساعدة", listOf(
                same("عندما انتقل سام إلى عمّان، كان خجولًا جدًا من التحدث بالعربية.", "When Sam moved to Amman, he was too shy to speak Arabic."),
                same("في أحد الأيام، رأى إعلانًا: مكتبة الحيّ تبحث عن متطوعين.", "One day he saw an advert: the neighbourhood library was looking for volunteers."),
                same("كانت المكتبة تحتاج إلى أشخاص يقرؤون القصص للأطفال أيام السبت.", "The library needed people to read stories to the children on Saturdays."),
                same("في البداية، كان سام متوترًا ويرتكب أخطاء كثيرة.", "At first, Sam was nervous and made many mistakes."),
                same("لكنّ الأطفال لم يهتموا بذلك، بل استمتعوا بالقصص.", "But the children didn't mind; they enjoyed the stories."),
                same("بعد بضعة أشهر، أصبح يشعر بثقة أكبر بكثير.", "After a few months, he felt much more confident."),
                same("وتعلّم أيضًا كلمات جديدة كثيرة من الكتب.", "He also learned many new words from the books."),
                same("والآن يشجّع أصدقاءه على التطوع أيضًا.", "Now he encourages his friends to volunteer too."),
            ), listOf(
                w("انتقل", "he moved", "انتقل", "intaqala"), w("عمّان", "Amman", "عمّان", "ʿammān"), w("خجولًا", "shy", "خجول", "khajūlan"),
                w("التحدث", "speaking", "التحدث", "at-taḥadduth"), w("إعلانًا", "an advert", "إعلان", "iʿlānan"), w("مكتبة", "library", "مكتبة", "maktaba"),
                w("الحيّ", "the neighbourhood", "الحيّ", "al-ḥayy"), w("تبحث عن", "is looking for", "تبحث عن", "tabḥathu ʿan"), w("متطوعين", "volunteers", "متطوعين", "mutaṭawwiʿīn"),
                w("تحتاج إلى", "needs", "تحتاج إلى", "taḥtāju ilā"), w("يقرؤون", "they read", "يقرؤون", "yaqraʾūn"), w("القصص", "the stories", "القصص", "al-qiṣaṣ"),
                w("للأطفال", "for the children", "للأطفال", "lil-aṭfāl"), w("في البداية", "at first", "في البداية", "fī l-bidāya"), w("متوترًا", "nervous", "متوتر", "mutawattiran"),
                w("يرتكب أخطاء", "makes mistakes", "يرتكب أخطاء", "yartakibu akhṭāʾ"), w("لم يهتموا", "they didn't mind", "لم يهتموا", "lam yahtammū"),
                w("استمتعوا", "they enjoyed", "استمتعوا", "istamtaʿū"), w("بضعة أشهر", "a few months", "بضعة أشهر", "biḍʿat ashhur"), w("أصبح", "he became", "أصبح", "aṣbaḥa"),
                w("بثقة", "with confidence", "بثقة", "bi-thiqa"), w("وتعلّم", "and he learned", "وتعلّم", "wa-taʿallama"), w("يشجّع", "he encourages", "يشجّع", "yushajjiʿ"),
                w("التطوع", "volunteering", "التطوع", "at-taṭawwuʿ"),
            ), listOf(
                q("ar_st_tatawwu_q1", "لماذا لم يتحدث سام بالعربية في البداية؟", listOf("كان خجولًا جدًا", "لم يكن لديه وقت", "لم يحب العربية"), 0,
                    g("Why didn't Sam speak Arabic at first?", "لماذا لم يتحدث سام بالعربية في البداية؟"),
                    listOf(g("he was too shy", "كان خجولًا جدًا"), g("he had no time", "لم يكن لديه وقت"), g("he didn't like Arabic", "لم يحب العربية"))),
                q("ar_st_tatawwu_q2", "ماذا كان سام يفعل في المكتبة؟", listOf("ينظّف الرفوف", "يقرأ القصص للأطفال", "يبيع الكتب"), 1,
                    g("What did Sam do at the library?", "ماذا كان سام يفعل في المكتبة؟"),
                    listOf(g("cleaned the shelves", "ينظّف الرفوف"), g("read stories to the children", "يقرأ القصص للأطفال"), g("sold books", "يبيع الكتب"))),
                q("ar_st_tatawwu_q3", "كيف أصبح يشعر بعد بضعة أشهر؟", listOf("بثقة أكبر", "بتوتر أكبر", "بالملل"), 0,
                    g("How did he feel after a few months?", "كيف أصبح يشعر بعد بضعة أشهر؟"),
                    listOf(g("more confident", "بثقة أكبر"), g("more nervous", "بتوتر أكبر"), g("bored", "بالملل"))),
            )),
        )
    }
}
