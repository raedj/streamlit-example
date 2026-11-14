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
 * Graded stories for Turkish, Urdu and Malay, following the same three plots as [GradedStories]
 * (A1 market, A2 lost bag, B1 volunteering). Original content; not yet reviewed by native speakers.
 */
internal object GradedStoriesExtra {
    private class Line(val text: String, val en: String, val ar: String)

    private fun l(text: String, en: String, ar: String) = Line(text, en, ar)
    private fun w(term: String, en: String, ar: String, reading: String? = null) = GlossaryEntry(term, Gloss(en, ar), reading)
    private fun q(id: String, prompt: String, options: List<String>, answer: Int) =
        Exercise.MultipleChoice(id, Skill.READING, Instr.choose, prompt, options, answer)

    private fun story(id: String, lang: LanguageCode, level: CefrLevel, title: String, en: String, ar: String, lines: List<Line>, words: List<GlossaryEntry>, qs: List<Exercise.MultipleChoice>) =
        ReadingPassage(id, lang, level, title, g(en, ar), lines.map { it.text }, lines.map { g(it.en, it.ar) }, emptyList(), qs, words)

    val turkish by lazy {
        val L = LanguageCode.TURKISH
        listOf(
            story("tr_st_pazar", L, A1, "Pazarda", "At the market", "في السوق", listOf(
                l("Cumartesi sabahı.", "It is Saturday morning.", "إنه صباح يوم السبت."),
                l("Elif kardeşiyle pazara gidiyor.", "Elif goes to the market with her brother.", "تذهب أليف إلى السوق مع أخيها."),
                l("Elma, domates ve taze ekmek alıyorlar.", "They buy apples, tomatoes and fresh bread.", "يشتريان التفاح والطماطم والخبز الطازج."),
                l("Elmalar kırmızı ve tatlı.", "The apples are red and sweet.", "التفاح أحمر وحلو."),
                l("Kardeşi soğuk bir meyve suyu istiyor.", "Her brother wants a cold juice.", "يريد أخوها عصيرًا باردًا."),
                l("Elif yüz lira ödüyor.", "Elif pays a hundred lira.", "تدفع أليف مئة ليرة."),
                l("Eve dönüyorlar ve birlikte yemek yapıyorlar.", "They go back home and cook together.", "يعودان إلى البيت ويطبخان معًا."),
            ), listOf(
                w("Cumartesi", "Saturday", "السبت"), w("sabahı", "(on Saturday) morning", "صباح"), w("kardeşiyle", "with her brother", "مع أخيها"), w("pazara", "to the market", "إلى السوق"),
                w("alıyorlar", "they buy", "يشتريان"), w("Elma", "apple", "تفاح"), w("domates", "tomatoes", "طماطم"), w("taze", "fresh", "طازج"),
                w("ekmek", "bread", "خبز"), w("kırmızı", "red", "أحمر"), w("tatlı", "sweet", "حلو"), w("soğuk", "cold", "بارد"),
                w("meyve suyu", "fruit juice", "عصير فواكه"), w("istiyor", "wants", "يريد"), w("ödüyor", "pays", "يدفع / تدفع"),
                w("Eve dönüyorlar", "they go back home", "يعودان إلى البيت"), w("birlikte", "together", "معًا"), w("yemek yapıyorlar", "they cook", "يطبخان"),
            ), listOf(
                q("tr_st_pazar_q1", "Pazara ne zaman gidiyorlar?", listOf("Cumartesi", "Pazartesi", "Cuma"), 0),
                q("tr_st_pazar_q2", "Kardeşi ne istiyor?", listOf("Çay", "Soğuk bir meyve suyu", "Su"), 1),
                q("tr_st_pazar_q3", "Elif ne kadar ödüyor?", listOf("Elli lira", "Yüz lira", "İki yüz lira"), 1),
            )),
            story("tr_st_valiz", L, A2, "Kayıp valiz", "The lost suitcase", "الحقيبة الضائعة", listOf(
                l("Geçen yaz Kerem ilk kez uçakla İstanbul'a gitti.", "Last summer, Kerem flew to Istanbul for the first time.", "في الصيف الماضي، سافر كرم بالطائرة إلى إسطنبول لأول مرة."),
                l("Havalimanına vardığında valizini bekledi.", "When he arrived at the airport, he waited for his suitcase.", "عندما وصل إلى المطار، انتظر حقيبته."),
                l("Bir saat sonra valiz hâlâ gelmemişti.", "An hour later, the suitcase still hadn't arrived.", "بعد ساعة، لم تكن الحقيبة قد وصلت بعد."),
                l("Danışma masasına gitti ve sorunu anlattı.", "He went to the information desk and explained the problem.", "ذهب إلى مكتب الاستعلامات وشرح المشكلة."),
                l("Masadaki kadın çok nazikti.", "The woman at the desk was very kind.", "كانت المرأة في المكتب لطيفة جدًا."),
                l("Telefon numarasını ve otelinin adresini yazdı.", "She wrote down his phone number and his hotel's address.", "دوّنت رقم هاتفه وعنوان فندقه."),
                l("Ertesi sabah biri kapısını çaldı.", "The next morning, someone knocked on his door.", "في صباح اليوم التالي، طرق أحدهم بابه."),
                l("Kapıda valizi vardı! Kerem çok rahatladı.", "His suitcase was at the door! Kerem was very relieved.", "كانت حقيبته عند الباب! شعر كرم بارتياح كبير."),
            ), listOf(
                w("Geçen yaz", "last summer", "الصيف الماضي"), w("ilk kez", "for the first time", "لأول مرة"), w("uçakla", "by plane", "بالطائرة"),
                w("Havalimanına", "to the airport", "إلى المطار"), w("vardığında", "when he arrived", "عندما وصل"), w("valizini", "his suitcase", "حقيبته"),
                w("bekledi", "waited (for)", "انتظر"), w("hâlâ", "still", "ما زال"), w("Danışma masasına", "to the information desk", "إلى مكتب الاستعلامات"),
                w("sorunu", "the problem", "المشكلة"), w("anlattı", "explained", "شرح"), w("nazikti", "was kind", "كان لطيفًا"), w("adresini", "the address", "العنوان"),
                w("yazdı", "wrote down", "دوّن"), w("Ertesi sabah", "the next morning", "في الصباح التالي"), w("kapısını çaldı", "knocked on his door", "طرق بابه"),
                w("rahatladı", "was relieved", "شعر بالارتياح"),
            ), listOf(
                q("tr_st_valiz_q1", "Kerem nereye gitti?", listOf("Ankara'ya", "İstanbul'a", "İzmir'e"), 1),
                q("tr_st_valiz_q2", "Kerem'e kim yardım etti?", listOf("Bir taksi şoförü", "Danışma masasındaki kadın", "Arkadaşı"), 1),
                q("tr_st_valiz_q3", "Kerem valizini nerede aldı?", listOf("Havalimanında", "Otelinde", "İstasyonda"), 1),
            )),
            story("tr_st_gonullu", L, B1, "Yardım ederek öğrenmek", "Learning by helping", "التعلّم بالمساعدة", listOf(
                l("Amir İzmir'e taşındığında Türkçe konuşmaktan çok çekiniyordu.", "When Amir moved to Izmir, he was very shy about speaking Turkish.", "عندما انتقل أمير إلى إزمير، كان يتحرّج كثيرًا من التحدث بالتركية."),
                l("Bir gün bir ilan gördü: mahalledeki kütüphane gönüllü arıyordu.", "One day he saw an advert: the neighbourhood library was looking for volunteers.", "في أحد الأيام رأى إعلانًا: كانت مكتبة الحي تبحث عن متطوعين."),
                l("Cumartesi sabahları çocuklara hikâye okuyacak kişilere ihtiyaç vardı.", "They needed people to read stories to children on Saturday mornings.", "كانت هناك حاجة إلى أشخاص يقرؤون القصص للأطفال صباح أيام السبت."),
                l("Başta Amir gergindi ve çok hata yapıyordu.", "At first, Amir was nervous and made many mistakes.", "في البداية، كان أمير متوترًا ويرتكب أخطاء كثيرة."),
                l("Yine de çocuklar hiç aldırmıyordu; sadece hikâyelerin tadını çıkarıyorlardı.", "Still, the children didn't mind at all; they just enjoyed the stories.", "ومع ذلك لم يكترث الأطفال أبدًا؛ كانوا يستمتعون بالقصص فقط."),
                l("Birkaç ay sonra kendine çok daha fazla güveniyordu.", "A few months later, he felt much more confident.", "بعد بضعة أشهر، صار يثق بنفسه أكثر بكثير."),
                l("Ayrıca kitaplardan birçok yeni kelime öğrenmişti.", "He had also learned many new words from the books.", "كما أنه تعلّم كلمات جديدة كثيرة من الكتب."),
                l("Şimdi arkadaşlarını da gönüllü olmaya teşvik ediyor.", "Now he encourages his friends to volunteer too.", "والآن يشجّع أصدقاءه على التطوع أيضًا."),
            ), listOf(
                w("taşındığında", "when he moved", "عندما انتقل"), w("çekiniyordu", "was shy, hesitated", "كان يتحرّج"), w("ilan", "advert", "إعلان"),
                w("mahalledeki", "in the neighbourhood", "في الحي"), w("kütüphane", "library", "مكتبة"), w("gönüllü", "volunteer", "متطوّع"),
                w("arıyordu", "was looking for", "كان يبحث عن"), w("hikâye", "story", "قصة"), w("ihtiyaç vardı", "there was a need", "كانت هناك حاجة"),
                w("Başta", "at first", "في البداية"), w("gergindi", "was nervous", "كان متوترًا"), w("hata", "mistake", "خطأ"), w("Yine de", "still, however", "ومع ذلك"),
                w("aldırmıyordu", "didn't mind", "لم يكترث"), w("tadını çıkarıyorlardı", "they were enjoying", "كانوا يستمتعون"), w("güveniyordu", "felt confident, trusted", "كان يثق"),
                w("Ayrıca", "also, besides", "إضافة إلى ذلك"), w("teşvik ediyor", "encourages", "يشجّع"),
            ), listOf(
                q("tr_st_gonullu_q1", "Amir başta neden Türkçe konuşmuyordu?", listOf("Çok çekiniyordu", "Zamanı yoktu", "Türkçeyi sevmiyordu"), 0),
                q("tr_st_gonullu_q2", "Amir kütüphanede ne yapıyordu?", listOf("Rafları temizliyordu", "Çocuklara hikâye okuyordu", "Kitap satıyordu"), 1),
                q("tr_st_gonullu_q3", "Birkaç ay sonra nasıl hissediyordu?", listOf("Kendine daha çok güveniyordu", "Daha gergindi", "Sıkılmıştı"), 0),
            )),
        )
    }

    val urdu by lazy {
        val L = LanguageCode.URDU
        listOf(
            story("ur_st_bazaar", L, A1, "بازار میں", "At the market", "في السوق", listOf(
                l("ہفتے کی صبح ہے۔", "It is Saturday morning.", "إنه صباح يوم السبت."),
                l("زینب اپنے بھائی کے ساتھ بازار جاتی ہے۔", "Zainab goes to the market with her brother.", "تذهب زينب إلى السوق مع أخيها."),
                l("وہ سیب، ٹماٹر اور تازہ روٹی خریدتے ہیں۔", "They buy apples, tomatoes and fresh bread.", "يشتريان التفاح والطماطم والخبز الطازج."),
                l("سیب لال اور میٹھے ہیں۔", "The apples are red and sweet.", "التفاح أحمر وحلو."),
                l("اس کا بھائی ٹھنڈا جوس چاہتا ہے۔", "Her brother wants a cold juice.", "يريد أخوها عصيرًا باردًا."),
                l("زینب دو سو روپے دیتی ہے۔", "Zainab pays two hundred rupees.", "تدفع زينب مئتي روبية."),
                l("وہ گھر واپس آتے ہیں اور مل کر کھانا پکاتے ہیں۔", "They come back home and cook together.", "يعودان إلى البيت ويطبخان معًا."),
            ), listOf(
                w("ہفتے", "Saturday (of Saturday)", "السبت", "hafte"), w("صبح", "morning", "صباح", "subah"), w("بازار", "market", "سوق", "bāzār"), w("بھائی", "brother", "أخ", "bhāī"),
                w("خریدتے ہیں", "they buy", "يشتريان", "kharīdte haiṇ"), w("سیب", "apples", "تفاح", "seb"), w("ٹماٹر", "tomatoes", "طماطم", "ṭamāṭar"), w("تازہ", "fresh", "طازج", "tāza"),
                w("روٹی", "bread", "خبز", "roṭī"), w("لال", "red", "أحمر", "lāl"), w("میٹھے", "sweet (pl.)", "حلوة", "mīṭhe"), w("ٹھنڈا", "cold", "بارد", "ṭhanḍā"),
                w("جوس", "juice", "عصير", "jūs"), w("چاہتا ہے", "wants", "يريد", "chāhtā hai"), w("دیتی ہے", "gives, pays (f.)", "تعطي، تدفع", "detī hai"),
                w("واپس آتے ہیں", "they come back", "يعودان", "wāpas āte haiṇ"), w("مل کر", "together", "معًا", "mil kar"), w("پکاتے ہیں", "they cook", "يطبخان", "pakāte haiṇ"),
            ), listOf(
                q("ur_st_bazaar_q1", "وہ بازار کب جاتے ہیں؟", listOf("ہفتے کی صبح", "پیر کو", "جمعے کو"), 0),
                q("ur_st_bazaar_q2", "اس کا بھائی کیا چاہتا ہے؟", listOf("چائے", "ٹھنڈا جوس", "پانی"), 1),
                q("ur_st_bazaar_q3", "زینب کتنے روپے دیتی ہے؟", listOf("سو", "دو سو", "پانچ سو"), 1),
            )),
            story("ur_st_bag", L, A2, "کھویا ہوا بیگ", "The lost bag", "الحقيبة الضائعة", listOf(
                l("پچھلی گرمیوں میں عمر پہلی بار ہوائی جہاز سے دبئی گیا۔", "Last summer, Umar flew to Dubai for the first time.", "في الصيف الماضي، سافر عمر بالطائرة إلى دبي لأول مرة."),
                l("ایئرپورٹ پہنچ کر اس نے اپنے بیگ کا انتظار کیا۔", "When he arrived at the airport, he waited for his bag.", "عندما وصل إلى المطار، انتظر حقيبته."),
                l("ایک گھنٹے بعد بھی بیگ نہیں آیا تھا۔", "An hour later, the bag still hadn't come.", "بعد ساعة، لم تكن الحقيبة قد وصلت بعد."),
                l("وہ معلومات کے کاؤنٹر پر گیا اور مسئلہ بتایا۔", "He went to the information desk and explained the problem.", "ذهب إلى مكتب الاستعلامات وشرح المشكلة."),
                l("کاؤنٹر پر موجود خاتون بہت مہربان تھیں۔", "The woman at the desk was very kind.", "كانت المرأة في المكتب لطيفة جدًا."),
                l("انہوں نے اس کا فون نمبر اور ہوٹل کا پتہ لکھ لیا۔", "She wrote down his phone number and the hotel's address.", "دوّنت رقم هاتفه وعنوان الفندق."),
                l("اگلی صبح کسی نے اس کا دروازہ کھٹکھٹایا۔", "The next morning, someone knocked on his door.", "في صباح اليوم التالي، طرق أحدهم بابه."),
                l("یہ اس کا بیگ تھا! عمر کو بہت سکون ملا۔", "It was his bag! Umar was very relieved.", "كانت حقيبته! شعر عمر بارتياح كبير."),
            ), listOf(
                w("پچھلی گرمیوں", "last summer", "الصيف الماضي", "pichhlī garmiyoṇ"), w("پہلی بار", "for the first time", "لأول مرة", "pehlī bār"),
                w("ہوائی جہاز", "aeroplane", "طائرة", "hawāī jahāz"), w("ایئرپورٹ", "airport", "مطار", "eārporṭ"), w("پہنچ کر", "on arriving", "عند الوصول", "pahuṅch kar"),
                w("انتظار کیا", "waited", "انتظر", "intizār kiyā"), w("بیگ", "bag", "حقيبة", "baig"), w("ایک گھنٹے بعد", "an hour later", "بعد ساعة", "ek ghaṇṭe baʿd"),
                w("معلومات کے کاؤنٹر", "information desk", "مكتب الاستعلامات", "maʿlūmāt ke kāunṭar"), w("مسئلہ", "problem", "مشكلة", "masʾala"),
                w("بتایا", "told, explained", "شرح", "batāyā"), w("مہربان", "kind", "لطيف", "mehrbān"), w("پتہ", "address", "عنوان", "pata"), w("لکھ لیا", "wrote down", "دوّن", "likh liyā"),
                w("اگلی صبح", "the next morning", "في الصباح التالي", "aglī subah"), w("کھٹکھٹایا", "knocked", "طرق", "khaṭkhaṭāyā"), w("سکون ملا", "felt relieved", "شعر بالارتياح", "sukūn milā"),
            ), listOf(
                q("ur_st_bag_q1", "عمر کہاں گیا؟", listOf("لاہور", "دبئی", "لندن"), 1),
                q("ur_st_bag_q2", "عمر کی مدد کس نے کی؟", listOf("ایک ٹیکسی والے نے", "کاؤنٹر پر موجود خاتون نے", "اس کے دوست نے"), 1),
                q("ur_st_bag_q3", "عمر کو اپنا بیگ کہاں ملا؟", listOf("ایئرپورٹ پر", "اپنے ہوٹل میں", "اسٹیشن پر"), 1),
            )),
            story("ur_st_madad", L, B1, "مدد کر کے سیکھنا", "Learning by helping", "التعلّم بالمساعدة", listOf(
                l("جب امیر کراچی منتقل ہوا تو وہ اردو بولنے سے بہت جھجکتا تھا۔", "When Amir moved to Karachi, he was very shy about speaking Urdu.", "عندما انتقل أمير إلى كراتشي، كان يتحرّج كثيرًا من التحدث بالأردية."),
                l("ایک دن اس نے ایک اشتہار دیکھا: محلے کی لائبریری کو رضاکاروں کی ضرورت تھی۔", "One day he saw an advert: the neighbourhood library needed volunteers.", "في أحد الأيام رأى إعلانًا: كانت مكتبة الحي بحاجة إلى متطوعين."),
                l("انہیں ایسے لوگ چاہیے تھے جو ہفتے کی صبح بچوں کو کہانیاں سنائیں۔", "They needed people to read stories to the children on Saturday mornings.", "كانوا بحاجة إلى أشخاص يقرؤون القصص للأطفال صباح يوم السبت."),
                l("شروع میں امیر گھبرایا ہوا تھا اور بہت غلطیاں کرتا تھا۔", "At first, Amir was nervous and made a lot of mistakes.", "في البداية، كان أمير متوترًا ويرتكب أخطاء كثيرة."),
                l("پھر بھی بچوں کو کوئی فرق نہیں پڑتا تھا؛ وہ بس کہانیوں کا مزہ لیتے تھے۔", "Still, the children didn't mind; they just enjoyed the stories.", "ومع ذلك لم يكترث الأطفال؛ كانوا يستمتعون بالقصص فقط."),
                l("چند مہینوں بعد اس میں بہت زیادہ اعتماد آ گیا۔", "After a few months, he became much more confident.", "بعد بضعة أشهر، صار أكثر ثقة بكثير."),
                l("اس نے کتابوں سے بہت سے نئے الفاظ بھی سیکھ لیے تھے۔", "He had also learned many new words from the books.", "كما أنه تعلّم كلمات جديدة كثيرة من الكتب."),
                l("اب وہ اپنے دوستوں کو بھی رضاکار بننے کی ترغیب دیتا ہے۔", "Now he encourages his friends to volunteer too.", "والآن يشجّع أصدقاءه على التطوع أيضًا."),
            ), listOf(
                w("منتقل ہوا", "moved (house)", "انتقل", "muntaqil huā"), w("جھجکتا تھا", "was shy, hesitated", "كان يتحرّج", "jhijaktā thā"), w("اشتہار", "advert", "إعلان", "ishtihār"),
                w("محلے", "neighbourhood", "الحي", "mohalle"), w("لائبریری", "library", "مكتبة", "lāibrerī"), w("رضاکاروں", "volunteers", "متطوعين", "razākāroṇ"),
                w("ضرورت", "need", "حاجة", "zarūrat"), w("کہانیاں", "stories", "قصص", "kahāniyāṇ"), w("شروع میں", "at first", "في البداية", "shurūʿ meṇ"),
                w("گھبرایا ہوا", "nervous", "متوتر", "ghabrāyā huā"), w("غلطیاں", "mistakes", "أخطاء", "ghaltiyāṇ"), w("پھر بھی", "still, however", "ومع ذلك", "phir bhī"),
                w("فرق نہیں پڑتا تھا", "didn't mind", "لم يكن يهمّ", "farq nahīṇ paṛtā thā"), w("مزہ لیتے تھے", "enjoyed", "كانوا يستمتعون", "maza lete the"),
                w("اعتماد", "confidence", "ثقة", "iʿtimād"), w("الفاظ", "words", "كلمات", "alfāz"), w("ترغیب دیتا ہے", "encourages", "يشجّع", "targhīb detā hai"),
            ), listOf(
                q("ur_st_madad_q1", "شروع میں امیر اردو کیوں نہیں بولتا تھا؟", listOf("وہ بہت جھجکتا تھا", "اس کے پاس وقت نہیں تھا", "اسے اردو پسند نہیں تھی"), 0),
                q("ur_st_madad_q2", "امیر لائبریری میں کیا کرتا تھا؟", listOf("الماریاں صاف کرتا تھا", "بچوں کو کہانیاں سناتا تھا", "کتابیں بیچتا تھا"), 1),
                q("ur_st_madad_q3", "چند مہینوں بعد وہ کیسا محسوس کرتا تھا؟", listOf("زیادہ پراعتماد", "زیادہ گھبرایا ہوا", "بور"), 0),
            )),
        )
    }

    val malay by lazy {
        val L = LanguageCode.MALAY
        listOf(
            story("ms_st_pasar", L, A1, "Di pasar", "At the market", "في السوق", listOf(
                l("Hari ini hari Sabtu.", "Today is Saturday.", "اليوم هو السبت."),
                l("Siti pergi ke pasar bersama adiknya.", "Siti goes to the market with her younger brother.", "تذهب سيتي إلى السوق مع أخيها الأصغر."),
                l("Mereka membeli epal, tomato dan roti segar.", "They buy apples, tomatoes and fresh bread.", "يشتريان التفاح والطماطم والخبز الطازج."),
                l("Epal itu merah dan manis.", "The apples are red and sweet.", "التفاح أحمر وحلو."),
                l("Adiknya mahu jus yang sejuk.", "Her brother wants a cold juice.", "يريد أخوها عصيرًا باردًا."),
                l("Siti membayar dua puluh ringgit.", "Siti pays twenty ringgit.", "تدفع سيتي عشرين رينغيت."),
                l("Mereka pulang ke rumah dan memasak bersama-sama.", "They go back home and cook together.", "يعودان إلى البيت ويطبخان معًا."),
            ), listOf(
                w("Sabtu", "Saturday", "السبت"), w("pasar", "market", "سوق"), w("adiknya", "her younger brother/sister", "أخوها الأصغر"), w("membeli", "buy", "يشتري"),
                w("epal", "apples", "تفاح"), w("tomato", "tomatoes", "طماطم"), w("segar", "fresh", "طازج"), w("roti", "bread", "خبز"),
                w("merah", "red", "أحمر"), w("manis", "sweet", "حلو"), w("mahu", "wants", "يريد"), w("jus", "juice", "عصير"), w("sejuk", "cold", "بارد"),
                w("membayar", "pays", "يدفع"), w("pulang ke rumah", "go back home", "يعودان إلى البيت"), w("memasak", "cook", "يطبخ"), w("bersama-sama", "together", "معًا"),
            ), listOf(
                q("ms_st_pasar_q1", "Bila mereka pergi ke pasar?", listOf("Hari Sabtu", "Hari Isnin", "Hari Jumaat"), 0),
                q("ms_st_pasar_q2", "Adiknya mahu apa?", listOf("Teh", "Jus yang sejuk", "Air kosong"), 1),
                q("ms_st_pasar_q3", "Berapa Siti membayar?", listOf("Sepuluh ringgit", "Dua puluh ringgit", "Lima puluh ringgit"), 1),
            )),
            story("ms_st_beg", L, A2, "Beg yang hilang", "The lost bag", "الحقيبة الضائعة", listOf(
                l("Cuti lepas, Adam menaiki kapal terbang ke Bangkok buat kali pertama.", "During the last holidays, Adam flew to Bangkok for the first time.", "في العطلة الماضية، سافر آدم بالطائرة إلى بانكوك لأول مرة."),
                l("Apabila tiba di lapangan terbang, dia menunggu begnya.", "When he arrived at the airport, he waited for his bag.", "عندما وصل إلى المطار، انتظر حقيبته."),
                l("Selepas sejam, beg itu masih belum sampai.", "After an hour, the bag still hadn't arrived.", "بعد ساعة، لم تكن الحقيبة قد وصلت بعد."),
                l("Dia pergi ke kaunter pertanyaan dan menerangkan masalahnya.", "He went to the information desk and explained the problem.", "ذهب إلى مكتب الاستعلامات وشرح المشكلة."),
                l("Wanita di kaunter itu sangat baik hati.", "The woman at the desk was very kind.", "كانت المرأة في المكتب لطيفة جدًا."),
                l("Dia mencatat nombor telefon Adam dan alamat hotelnya.", "She wrote down Adam's phone number and his hotel's address.", "دوّنت رقم هاتف آدم وعنوان فندقه."),
                l("Keesokan paginya, seseorang mengetuk pintu biliknya.", "The next morning, someone knocked on his door.", "في صباح اليوم التالي، طرق أحدهم باب غرفته."),
                l("Itu begnya! Adam berasa sangat lega.", "It was his bag! Adam felt very relieved.", "كانت حقيبته! شعر آدم بارتياح كبير."),
            ), listOf(
                w("Cuti lepas", "the last holidays", "العطلة الماضية"), w("kapal terbang", "aeroplane", "طائرة"), w("kali pertama", "the first time", "أول مرة"),
                w("tiba", "arrive", "يصل"), w("lapangan terbang", "airport", "مطار"), w("menunggu", "wait (for)", "ينتظر"), w("sejam", "an hour", "ساعة"),
                w("masih belum", "still not yet", "لم … بعد"), w("kaunter pertanyaan", "information desk", "مكتب الاستعلامات"), w("menerangkan", "explain", "يشرح"),
                w("masalahnya", "the problem", "المشكلة"), w("baik hati", "kind", "طيّب القلب"), w("mencatat", "wrote down", "دوّن"), w("alamat", "address", "عنوان"),
                w("Keesokan paginya", "the next morning", "في الصباح التالي"), w("mengetuk", "knock", "يطرق"), w("lega", "relieved", "مرتاح بعد قلق"),
            ), listOf(
                q("ms_st_beg_q1", "Ke mana Adam pergi?", listOf("Ke Jakarta", "Ke Bangkok", "Ke Singapura"), 1),
                q("ms_st_beg_q2", "Siapa yang menolong Adam?", listOf("Pemandu teksi", "Wanita di kaunter", "Kawannya"), 1),
                q("ms_st_beg_q3", "Di mana Adam dapat begnya semula?", listOf("Di lapangan terbang", "Di hotelnya", "Di stesen"), 1),
            )),
            story("ms_st_sukarela", L, B1, "Belajar sambil membantu", "Learning by helping", "التعلّم بالمساعدة", listOf(
                l("Apabila Amir berpindah ke Kuala Lumpur, dia terlalu malu untuk bercakap bahasa Melayu.", "When Amir moved to Kuala Lumpur, he was too shy to speak Malay.", "عندما انتقل أمير إلى كوالالمبور، كان خجولًا جدًا من التحدث بالملايوية."),
                l("Suatu hari, dia ternampak satu iklan: perpustakaan kawasan itu mencari sukarelawan.", "One day he noticed an advert: the local library was looking for volunteers.", "في أحد الأيام لاحظ إعلانًا: كانت مكتبة المنطقة تبحث عن متطوعين."),
                l("Mereka memerlukan orang untuk membacakan cerita kepada kanak-kanak pada pagi Sabtu.", "They needed people to read stories to children on Saturday mornings.", "كانوا بحاجة إلى أشخاص يقرؤون القصص للأطفال صباح يوم السبت."),
                l("Pada mulanya, Amir gementar dan banyak membuat kesilapan.", "At first, Amir was nervous and made many mistakes.", "في البداية، كان أمير متوترًا ويرتكب أخطاء كثيرة."),
                l("Namun, kanak-kanak itu tidak kisah langsung; mereka cuma seronok mendengar cerita.", "However, the children didn't mind at all; they just enjoyed listening to the stories.", "لكن الأطفال لم يكترثوا أبدًا؛ كانوا يستمتعون بسماع القصص فقط."),
                l("Selepas beberapa bulan, dia berasa jauh lebih yakin.", "After a few months, he felt much more confident.", "بعد بضعة أشهر، صار يشعر بثقة أكبر بكثير."),
                l("Dia juga telah belajar banyak perkataan baharu daripada buku-buku itu.", "He had also learned many new words from those books.", "كما أنه تعلّم كلمات جديدة كثيرة من تلك الكتب."),
                l("Sekarang dia menggalakkan kawan-kawannya menjadi sukarelawan juga.", "Now he encourages his friends to become volunteers too.", "والآن يشجّع أصدقاءه على أن يصبحوا متطوعين أيضًا."),
            ), listOf(
                w("berpindah", "moved (house)", "انتقل"), w("terlalu malu", "too shy", "خجول جدًا"), w("iklan", "advert", "إعلان"), w("perpustakaan", "library", "مكتبة"),
                w("sukarelawan", "volunteers", "متطوعون"), w("memerlukan", "need", "يحتاج إلى"), w("membacakan", "read aloud (to someone)", "يقرأ لـ"),
                w("kanak-kanak", "children", "أطفال"), w("Pada mulanya", "at first", "في البداية"), w("gementar", "nervous", "متوتر"), w("kesilapan", "mistakes", "أخطاء"),
                w("Namun", "however", "لكن، ومع ذلك"), w("tidak kisah", "didn't mind", "لم يكترث"), w("seronok", "enjoy, have fun", "يستمتع"), w("yakin", "confident", "واثق"),
                w("perkataan baharu", "new words", "كلمات جديدة"), w("menggalakkan", "encourages", "يشجّع"),
            ), listOf(
                q("ms_st_sukarela_q1", "Mengapa Amir tidak bercakap bahasa Melayu pada mulanya?", listOf("Dia terlalu malu", "Dia tiada masa", "Dia tidak suka bahasa Melayu"), 0),
                q("ms_st_sukarela_q2", "Apa yang Amir buat di perpustakaan?", listOf("Membersihkan rak", "Membacakan cerita kepada kanak-kanak", "Menjual buku"), 1),
                q("ms_st_sukarela_q3", "Bagaimana perasaannya selepas beberapa bulan?", listOf("Lebih yakin", "Lebih gementar", "Bosan"), 0),
            )),
        )
    }
}
