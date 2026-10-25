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
import com.polyglot.academy.core.model.VocabularySource
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

internal object SpanishCurriculum {
    private val L = LanguageCode.SPANISH

    fun build(): LanguageCurriculum {
        val vb = VocabBuilder(L)
        with(vb) {
            v("hola", "hola", "hello", "مرحبًا", PHRASE, A1, "greetings", "/ˈo.la/",
                ex = "¡Hola! ¿Qué tal?", exEn = "Hi! How's it going?", exAr = "مرحبًا! كيف الحال؟")
            v("buenos_dias", "buenos días", "good morning", "صباح الخير", PHRASE, A1, "greetings", "/ˈbwe.nos ˈði.as/")
            v("gracias", "gracias", "thank you", "شكرًا", PHRASE, A1, "greetings", "/ˈɡɾa.sjas/ (LatAm) · /ˈɡɾa.θjas/ (Spain)",
                ex = "Muchas gracias por todo.", exEn = "Thank you very much for everything.", exAr = "شكرًا جزيلًا على كل شيء.")
            v("por_favor", "por favor", "please", "من فضلك", PHRASE, A1, "greetings", "/poɾ faˈβoɾ/")
            v("perdon", "perdón", "sorry / excuse me", "عذرًا / آسف", PHRASE, A1, "greetings", "/peɾˈðon/")
            v("adios", "adiós", "goodbye", "مع السلامة", PHRASE, A1, "greetings", "/aˈðjos/")
            v("me_llamo", "me llamo", "my name is", "اسمي", PHRASE, A1, "greetings", "/me ˈʎa.mo/",
                ex = "Me llamo Laila. ¿Y tú?", exEn = "My name is Laila. And you?", exAr = "اسمي ليلى. وأنت؟")
            v("encantado", "encantado / encantada", "nice to meet you (m./f.)", "تشرّفت بمعرفتك (مذكر/مؤنث)", ADJECTIVE, A1, "greetings")
            v("familia", "la familia", "family", "العائلة", NOUN, A1, "people", gender = FEMININE)
            v("madre", "la madre", "mother", "الأم", NOUN, A1, "people", gender = FEMININE)
            v("padre", "el padre", "father", "الأب", NOUN, A1, "people", gender = MASCULINE)
            v("hermano", "el hermano", "brother", "الأخ", NOUN, A1, "people", gender = MASCULINE)
            v("hermana", "la hermana", "sister", "الأخت", NOUN, A1, "people", gender = FEMININE)
            v("amigo", "el amigo / la amiga", "friend", "الصديق / الصديقة", NOUN, A1, "people",
                ex = "Ella es mi mejor amiga.", exEn = "She is my best friend.", exAr = "هي أعزّ صديقاتي.")
            v("agua", "el agua", "water", "الماء", NOUN, A1, "food", gender = FEMININE)
            v("cafe", "el café", "coffee", "القهوة", NOUN, A1, "food", gender = MASCULINE,
                ex = "Un café con leche, por favor.", exEn = "A coffee with milk, please.", exAr = "قهوة بالحليب من فضلك.")
            v("pan", "el pan", "bread", "الخبز", NOUN, A1, "food", gender = MASCULINE)
            v("cuenta", "la cuenta", "the bill", "الحساب", NOUN, A2, "food", gender = FEMININE,
                ex = "La cuenta, por favor.", exEn = "The bill, please.", exAr = "الحساب من فضلك.")
            v("quiero", "quiero", "I want", "أريد", VERB, A1, "food")
            v("quisiera", "quisiera", "I would like (polite)", "أودّ (بأدب)", VERB, A2, "food",
                ex = "Quisiera una mesa para dos.", exEn = "I'd like a table for two.", exAr = "أودّ طاولة لشخصين.")
            v("rico", "rico / rica", "tasty, delicious", "لذيذ", ADJECTIVE, A1, "food")
            v("hoy", "hoy", "today", "اليوم", ADVERB, A1, "time")
            v("manana", "mañana", "tomorrow; morning", "غدًا؛ صباح", ADVERB, A1, "time",
                ex = "Nos vemos mañana.", exEn = "See you tomorrow.", exAr = "نلتقي غدًا.")
            v("ayer", "ayer", "yesterday", "أمس", ADVERB, A1, "time")
            v("semana", "la semana", "week", "الأسبوع", NOUN, A1, "time", gender = FEMININE)
            v("estacion", "la estación", "station", "المحطة", NOUN, A2, "travel", gender = FEMININE,
                ex = "¿Dónde está la estación?", exEn = "Where is the station?", exAr = "أين المحطة؟")
            v("billete", "el billete (Esp.) / el boleto (LatAm)", "ticket", "التذكرة", NOUN, A2, "travel", gender = MASCULINE)
            v("izquierda", "a la izquierda", "on the left", "على اليسار", PHRASE, A1, "travel")
            v("derecha", "a la derecha", "on the right", "على اليمين", PHRASE, A1, "travel")
            v("todo_recto", "todo recto", "straight on", "إلى الأمام مباشرة", PHRASE, A2, "travel")
            v("coche", "el coche (Esp.) / el carro (LatAm)", "car", "السيارة", NOUN, A1, "travel", gender = MASCULINE)
            v("levantarse", "levantarse", "to get up", "ينهض (من النوم)", VERB, A2, "routine",
                ex = "Me levanto a las siete.", exEn = "I get up at seven.", exAr = "أنهض في السابعة.")
            v("trabajar", "trabajar", "to work", "يعمل", VERB, A1, "routine")
            v("estudiar", "estudiar", "to study", "يدرس", VERB, A1, "routine")
            v("normalmente", "normalmente", "usually", "عادةً", ADVERB, A2, "routine")
            v("cansado", "cansado / cansada", "tired", "متعب / متعبة", ADJECTIVE, A1, "feelings")
            v("contento", "contento / contenta", "happy, pleased", "مسرور / مسرورة", ADJECTIVE, A1, "feelings")
            v("creo_que", "creo que", "I think that", "أعتقد أنّ", PHRASE, B1, "feelings",
                ex = "Creo que es una buena idea.", exEn = "I think it's a good idea.", exAr = "أعتقد أنها فكرة جيدة.")
            v("aunque", "aunque", "although", "على الرغم من أنّ", ADVERB, B1, "feelings")
            v("cuanto", "¿cuánto cuesta?", "how much does it cost?", "كم سعره؟", PHRASE, A1, "shopping",
                ex = "¿Cuánto cuesta esta camisa?", exEn = "How much is this shirt?", exAr = "كم سعر هذا القميص؟")
            v("barato", "barato / barata", "cheap", "رخيص", ADJECTIVE, A1, "shopping")
            v("caro", "caro / cara", "expensive", "غالٍ", ADJECTIVE, A1, "shopping")

            // Home and weather
            v("casa", "la casa", "house, home", "البيت", NOUN, A1, "home", gender = FEMININE,
                ex = "Mi casa es pequeña pero bonita.", exEn = "My house is small but pretty.", exAr = "بيتي صغير لكنه جميل.")
            v("cocina", "la cocina", "kitchen; cooking", "المطبخ؛ الطبخ", NOUN, A1, "home", gender = FEMININE)
            v("dormitorio", "el dormitorio", "bedroom", "غرفة النوم", NOUN, A2, "home", gender = MASCULINE)
            v("me_gusta", "me gusta / me gustan", "I like (one thing / several things)", "يعجبني / تعجبني", PHRASE, A1, "home",
                ex = "Me gustan los días de sol.", exEn = "I like sunny days.", exAr = "تعجبني الأيام المشمسة.")
            v("tiempo", "el tiempo", "weather; time", "الطقس؛ الوقت", NOUN, A1, "weather", gender = MASCULINE)
            v("hace_calor", "hace calor", "it's hot", "الجو حار", PHRASE, A1, "weather")
            v("hace_frio", "hace frío", "it's cold", "الجو بارد", PHRASE, A1, "weather",
                ex = "En enero hace mucho frío.", exEn = "In January it's very cold.", exAr = "في يناير يكون الجو باردًا جدًا.")
            v("llueve", "llueve", "it's raining", "إنها تمطر", VERB, A1, "weather")
            // Health
            v("medico", "el médico / la médica", "doctor", "الطبيب / الطبيبة", NOUN, A1, "health")
            v("me_duele", "me duele / me duelen", "it hurts (me) / they hurt", "يؤلمني / تؤلمني", PHRASE, A2, "health",
                ex = "Me duele la cabeza.", exEn = "I have a headache.", exAr = "رأسي يؤلمني.")
            v("cabeza", "la cabeza", "head", "الرأس", NOUN, A1, "health", gender = FEMININE)
            v("fiebre", "la fiebre", "fever", "الحمّى", NOUN, A2, "health", gender = FEMININE)
            v("farmacia", "la farmacia", "pharmacy", "الصيدلية", NOUN, A1, "health", gender = FEMININE)
            v("cita", "la cita", "appointment", "الموعد", NOUN, A2, "health", gender = FEMININE,
                ex = "Quisiera pedir una cita con el médico.", exEn = "I'd like to make an appointment with the doctor.", exAr = "أودّ حجز موعد مع الطبيب.")
            v("enfermo", "enfermo / enferma", "ill, sick", "مريض / مريضة", ADJECTIVE, A1, "health")
            // Work (professional track)
            v("reunion", "la reunión", "meeting", "الاجتماع", NOUN, A2, "business", gender = FEMININE,
                ex = "La reunión empieza a las diez.", exEn = "The meeting starts at ten.", exAr = "يبدأ الاجتماع في العاشرة.", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("empresa", "la empresa", "company", "الشركة", NOUN, A2, "business", gender = FEMININE, tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("cliente", "el / la cliente", "client, customer", "العميل / العميلة", NOUN, A2, "business", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("presupuesto", "el presupuesto", "budget; quote", "الميزانية؛ عرض السعر", NOUN, B1, "business", gender = MASCULINE, tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("plazo", "el plazo", "deadline; time limit", "المهلة؛ الموعد النهائي", NOUN, B1, "business", gender = MASCULINE,
                ex = "El plazo termina el viernes.", exEn = "The deadline is Friday.", exAr = "تنتهي المهلة يوم الجمعة.", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("propuesta", "la propuesta", "proposal", "المقترح", NOUN, B1, "business", gender = FEMININE, tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("adjunto", "le envío adjunto…", "please find attached… (formal email)", "أرسل إليكم مرفقًا…", PHRASE, B1, "business", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)
            v("estimado", "Estimado / Estimada…:", "Dear… (formal letter opening)", "حضرة… المحترم/المحترمة", PHRASE, B1, "business", tags = setOf("business"), source = VocabularySource.PROFESSIONAL_TRACK)

            // Next steps: plans and wishes
            v("plan", "el plan", "plan", "الخطة", NOUN, B1, "plans", gender = MASCULINE)
            v("ir_a", "ir a + infinitivo", "to be going to (do)", "سوف (لخطة قريبة)", PHRASE, A2, "plans",
                ex = "Voy a estudiar esta noche.", exEn = "I'm going to study tonight.", exAr = "سأدرس الليلة.")
            v("proximo", "el próximo año / la próxima semana", "next year / next week", "العام القادم / الأسبوع القادم", PHRASE, A2, "plans")
            v("viajar", "viajar", "to travel", "يسافر", VERB, A2, "plans")
            v("ahorrar", "ahorrar", "to save (money)", "يدّخر", VERB, B1, "plans")
            v("quizas", "quizás", "maybe, perhaps", "ربما", ADVERB, B1, "plans")
            v("vacaciones", "las vacaciones", "holidays, vacation", "العطلة", NOUN, A2, "plans", gender = FEMININE)
            v("espero_que", "espero que…", "I hope that… (+ subjunctive)", "آمل أن…", PHRASE, B2, "wishes",
                ex = "Espero que tengas un buen día.", exEn = "I hope you have a good day.", exAr = "آمل أن يكون يومك جميلًا.")
            v("ojala", "ojalá", "I wish, hopefully", "ليت / يا ليت", PHRASE, B2, "wishes")
            v("quiero_que", "quiero que…", "I want (someone) to…", "أريد أن (يفعل شخص آخر)…", PHRASE, B2, "wishes")
            v("es_importante", "es importante que…", "it's important that…", "من المهم أن…", PHRASE, B2, "wishes")
            v("suerte", "la suerte", "luck", "الحظ", NOUN, B1, "wishes", gender = FEMININE)
            v("aprobar", "aprobar", "to pass (an exam)", "ينجح (في امتحان)", VERB, B1, "wishes")
            v("examen", "el examen", "exam", "الامتحان", NOUN, A2, "wishes", gender = MASCULINE)
        }
        val vocabulary = vb.items
        val f = LessonFactory(L, vocabulary)

        val grammar = listOf(
            grammar("ser_estar", L, A1, g("Ser and estar", "‏ser و estar"),
                g("Both mean 'to be'. Use ser for identity, origin and characteristics (Soy de Egipto). Use estar for location and temporary states (Estoy cansado).",
                    "كلاهما بمعنى 'يكون'. استخدم ser للهوية والأصل والصفات الثابتة (Soy de Egipto)، وestar للمكان والحالات المؤقتة (Estoy cansado)."),
                listOf(ex("Soy estudiante.", "I am a student.", "أنا طالب."), ex("Estoy en casa.", "I am at home.", "أنا في البيت."),
                    ex("Soy cansado.", "Use estar for a temporary state", "استخدم estar للحالة المؤقتة", wrong = true)),
                listOf(g("✗ Soy en Madrid → ✓ Estoy en Madrid (location uses estar).", "✗ Soy en Madrid ← ✓ Estoy en Madrid (المكان مع estar)."))),
            grammar("gender", L, A1, g("Gender and agreement", "الجنس والمطابقة"),
                g("Nouns are masculine or feminine. Articles and adjectives agree: el coche rojo, la casa roja. Most nouns ending in -o are masculine and in -a feminine (with exceptions like el día).",
                    "الأسماء مذكرة أو مؤنثة، وتتطابق معها أدوات التعريف والصفات: el coche rojo وla casa roja. معظم المنتهية بـ ‎-o مذكرة وبـ ‎-a مؤنثة (مع استثناءات مثل el día)."),
                listOf(ex("la casa blanca", "the white house", "البيت الأبيض"), ex("el libro nuevo", "the new book", "الكتاب الجديد"),
                    ex("la libro", "Wrong article", "أداة خاطئة", wrong = true)),
                listOf(g("Mixing genders: ✗ la problema → ✓ el problema.", "خلط الجنس: ✗ la problema ← ✓ el problema."))),
            grammar("present", L, A1, g("Present tense (regular -ar verbs)", "المضارع (أفعال ‎-ar المنتظمة)"),
                g("trabajar → trabajo, trabajas, trabaja, trabajamos, trabajáis, trabajan. The ending shows the person, so the pronoun is often omitted.",
                    "trabajar ← trabajo وtrabajas وtrabaja وtrabajamos وtrabajáis وtrabajan. النهاية تدل على الفاعل لذا يُحذف الضمير غالبًا."),
                listOf(ex("Trabajo en un hotel.", "I work in a hotel.", "أعمل في فندق."), ex("¿Estudias español?", "Do you study Spanish?", "هل تدرس الإسبانية؟")),
                listOf(g("✗ Yo trabaja → ✓ Yo trabajo.", "✗ Yo trabaja ← ✓ Yo trabajo."))),
            grammar("polite", L, A2, g("Polite requests", "الطلبات المهذبة"),
                g("'Quisiera…' and '¿Me pone…?' (Spain) or '¿Me da…?' (Latin America) are polite ways to order. 'Quiero' is fine but more direct.",
                    "عبارتا Quisiera… و¿Me pone…? (في إسبانيا) أو ¿Me da…? (في أمريكا اللاتينية) طرق مهذبة للطلب. أما Quiero فمقبولة لكنها أكثر مباشرة."),
                listOf(ex("Quisiera un café, por favor.", "I'd like a coffee, please.", "أودّ قهوة من فضلك.")),
                listOf(g("Forgetting 'por favor' can sound abrupt.", "نسيان por favor قد يبدو جافًا."))),
            grammar("preterite", L, B1, g("Past tense (pretérito indefinido)", "الماضي البسيط (pretérito indefinido)"),
                g("For completed past actions: hablar → hablé, hablaste, habló. Use it with ayer, el año pasado, etc.",
                    "للأحداث المنتهية في الماضي: hablar ← hablé وhablaste وhabló. ويُستخدم مع ayer وel año pasado وغيرها."),
                listOf(ex("Ayer trabajé mucho.", "Yesterday I worked a lot.", "أمس عملت كثيرًا.")),
                listOf(g("Missing the accent changes meaning: hablo (I speak) vs habló (he spoke).", "غياب علامة النبر يغيّر المعنى: hablo (أتكلم) مقابل habló (تكلّم)."))),

            grammar("gustar", L, A1, g("Gustar (to like)", "الفعل gustar (يعجب)"),
                g("Gustar works like 'to please': the thing liked is the subject. Use gusta with one thing or a verb, gustan with several: Me gusta el café. Me gustan los libros.",
                    "يعمل gustar بمعنى 'يُعجِب': الشيء المحبوب هو الفاعل. استخدم gusta مع شيء واحد أو فعل، وgustan مع عدة أشياء: Me gusta el café وMe gustan los libros."),
                listOf(ex("Me gusta leer.", "I like reading.", "أحب القراءة."), ex("Nos gustan las películas.", "We like films.", "نحب الأفلام."),
                    ex("Yo gusto el café.", "Use 'me gusta', not 'yo gusto'", "استخدم me gusta وليس yo gusto", wrong = true)),
                listOf(g("✗ Me gusta los perros → ✓ Me gustan los perros (plural).", "✗ Me gusta los perros ← ✓ Me gustan los perros (جمع)."))),
            grammar("usted", L, A2, g("Formal 'you': usted", "المخاطبة الرسمية: usted"),
                g("Use usted (ustedes in the plural) with clients, officials and people you don't know. It takes third-person verb forms: ¿Usted trabaja aquí? ¿Puede ayudarme?",
                    "استخدم usted (وustedes للجمع) مع العملاء والموظفين ومن لا تعرفهم. ويأخذ تصريف الغائب: ¿Usted trabaja aquí? و¿Puede ayudarme?"),
                listOf(ex("¿Puede enviarme el presupuesto?", "Could you send me the quote?", "هل يمكنكم إرسال عرض السعر إليّ؟"),
                    ex("¿Usted puedes venir?", "usted takes 'puede', not 'puedes'", "usted يأخذ puede وليس puedes", wrong = true)),
                listOf(g("Mixing tú and usted forms in one sentence sounds careless in formal emails.", "الخلط بين صيغتي tú وusted في الجملة نفسها يبدو غير متقن في الرسائل الرسمية."))),
            grammar("futuro", L, B1, g("Talking about the future", "الحديث عن المستقبل"),
                g("Use ir a + infinitive for plans: Voy a viajar en julio. The simple future (infinitive + -é, -ás, -á, -emos, -éis, -án) is for predictions and promises: Mañana lloverá. Te llamaré.",
                    "استخدم ir a + المصدر للخطط: Voy a viajar en julio. والمستقبل البسيط (المصدر + ‎-é و‎-ás وغيرهما) للتوقّعات والوعود: Mañana lloverá. Te llamaré."),
                listOf(ex("Vamos a cenar en casa.", "We're going to have dinner at home.", "سنتناول العشاء في البيت."),
                    ex("El año que viene viviré en Madrid.", "Next year I'll live in Madrid.", "في العام القادم سأعيش في مدريد."),
                    ex("Voy a viajo.", "After ir a use the infinitive: voy a viajar", "بعد ir a يأتي المصدر: voy a viajar", wrong = true)),
                listOf(g("Forgetting 'a': ✗ Voy viajar → ✓ Voy a viajar.", "نسيان a: ✗ Voy viajar ← ✓ Voy a viajar."))),
            grammar("subjuntivo", L, B2, g("Present subjunctive for wishes", "المضارع الشرطي (subjuntivo) للأمنيات"),
                g("After wishes, hopes and needs with a different subject, use the subjunctive: Espero que vengas. Quiero que mis hijos hablen inglés. Endings swap vowels: -ar verbs take -e (hablar → hable), -er/-ir verbs take -a (comer → coma, vivir → viva).",
                    "بعد الأمنيات والرجاء والحاجة مع فاعل مختلف يُستخدم subjuntivo: Espero que vengas. وQuiero que mis hijos hablen inglés. تتبادل الحروف الصوتية: أفعال ‎-ar تأخذ ‎-e (hablar ← hable) وأفعال ‎-er/-ir تأخذ ‎-a (comer ← coma، vivir ← viva)."),
                listOf(ex("Espero que apruebes el examen.", "I hope you pass the exam.", "أتمنى أن تنجح في الامتحان."),
                    ex("Ojalá haga buen tiempo.", "Hopefully the weather will be good.", "ليت الجو يكون جميلًا."),
                    ex("Quiero que vienes.", "Use the subjunctive: quiero que vengas", "استخدم subjuntivo: quiero que vengas", wrong = true)),
                listOf(g("Same subject → infinitive: Quiero viajar (not ✗ Quiero que yo viaje).", "عند اتحاد الفاعل يأتي المصدر: Quiero viajar (وليس ✗ Quiero que yo viaje)."))),
        )

        val lessons = listOf(
            f.lesson("saludos", A1, "basics", g("Greetings", "التحيات"), g("Say hello and introduce yourself.", "ألقِ التحية وعرّف بنفسك."),
                Skill.SPEAKING, listOf("hola", "buenos_dias", "gracias", "por_favor", "perdon", "adios", "me_llamo", "encantado"), "ser_estar",
                extra = listOf(
                    fill("s1", L, "Yo ___ estudiante.", listOf("soy")),
                    fill("s2", L, "¿Cómo te ___? — Me llamo Ana.", listOf("llamas")),
                ), minutes = 6),
            f.lesson("familia", A1, "basics", g("Family", "العائلة"), g("Talk about your family with the right articles.", "تحدّث عن عائلتك باستخدام أدوات التعريف الصحيحة."),
                Skill.VOCABULARY, listOf("familia", "madre", "padre", "hermano", "hermana", "amigo"), "gender",
                extra = listOf(
                    choice("f1", L, "___ hermana de Pedro es médica.", listOf("La", "El", "Los"), 0),
                    fill("f2", L, "Mi hermano está ___ (cansado).", listOf("cansado")),
                ), prerequisites = listOf("saludos")),
            f.lesson("cafe", A1, "everyday", g("At the café", "في المقهى"), g("Order politely and ask for the bill.", "اطلب بأدب واطلب الحساب."),
                Skill.SPEAKING, listOf("agua", "cafe", "pan", "cuenta", "quiero", "quisiera", "rico"), "polite",
                extra = listOf(
                    order("c1", L, listOf("Quisiera", "un", "café,", "por", "favor"), g("I'd like a coffee, please.", "أودّ قهوة من فضلك.")),
                    dictation("c2", L, "La cuenta, por favor.", listOf("La cuenta por favor")),
                ), prerequisites = listOf("familia")),
            f.lesson("rutina", A2, "everyday", g("Daily routine", "الروتين اليومي"), g("Describe your day with present-tense verbs.", "صِف يومك بأفعال المضارع."),
                Skill.WRITING, listOf("levantarse", "trabajar", "estudiar", "normalmente", "hoy", "manana", "semana"), "present",
                extra = listOf(
                    fill("r1", L, "Normalmente ___ (trabajar, yo) por la mañana.", listOf("trabajo")),
                    fill("r2", L, "Ella ___ (estudiar) español.", listOf("estudia")),
                    writing("r3", L, g("Describe your typical day in 3–5 sentences.", "صِف يومك المعتاد في 3 إلى 5 جمل."), 25, listOf("me levanto", "normalmente", "trabajo")),
                ), prerequisites = listOf("cafe")),
            f.lesson("viaje", A2, "travel", g("Getting around", "التنقّل"), g("Ask for directions and buy a ticket.", "اسأل عن الاتجاهات واشترِ تذكرة."),
                Skill.LISTENING, listOf("estacion", "billete", "izquierda", "derecha", "todo_recto", "coche"), "ser_estar",
                extra = listOf(
                    fill("v1", L, "¿Dónde ___ la estación?", listOf("está"), g("Location → estar.", "المكان ← estar.")),
                    dictation("v2", L, "Siga todo recto y gire a la derecha.", listOf("Siga todo recto y gire a la derecha")),
                ), prerequisites = listOf("rutina")),
            f.lesson("compras", A2, "travel", g("Shopping", "التسوّق"), g("Ask prices and compare.", "اسأل عن الأسعار وقارن."),
                Skill.SPEAKING, listOf("cuanto", "barato", "caro", "contento", "cansado"), "gender",
                extra = listOf(fill("cm1", L, "Esta camisa es muy ___ (caro).", listOf("cara"), g("camisa is feminine.", "camisa مؤنثة."))),
                prerequisites = listOf("viaje")),
            f.lesson("opiniones", B1, "travel", g("Opinions and the past", "الآراء والماضي"), g("Say what you think and what happened.", "قل رأيك وما حدث."),
                Skill.SPEAKING, listOf("creo_que", "aunque", "ayer", "contento"), "preterite",
                extra = listOf(
                    fill("o1", L, "Ayer ___ (hablar, yo) con mi jefe.", listOf("hablé")),
                    translate("o2", L, g("I think it's expensive.", "أعتقد أنه غالٍ."), listOf("Creo que es caro", "Creo que es cara")),
                ), prerequisites = listOf("compras")),

            f.lesson("casa", A2, "home", g("Home and weather", "البيت والطقس"), g("Describe your home and talk about the weather.", "صِف بيتك وتحدّث عن الطقس."),
                Skill.VOCABULARY, listOf("casa", "cocina", "dormitorio", "me_gusta", "tiempo", "hace_calor", "hace_frio", "llueve"), "gustar",
                extra = listOf(
                    fill("ca1", L, "Me ___ los días de lluvia.", listOf("gustan"), g("los días is plural.", "los días جمع.")),
                    fill("ca2", L, "En invierno hace ___.", listOf("frío", "frio")),
                    order("ca3", L, listOf("Me", "gusta", "mucho", "mi", "casa"), g("I really like my house.", "يعجبني بيتي كثيرًا.")),
                ), prerequisites = listOf("opiniones")),
            f.lesson("salud", A2, "home", g("At the doctor's", "عند الطبيب"), g("Say what hurts and make an appointment.", "قل ما يؤلمك واحجز موعدًا."),
                Skill.SPEAKING, listOf("medico", "me_duele", "cabeza", "fiebre", "farmacia", "cita", "enfermo"), "usted",
                extra = listOf(
                    fill("sa1", L, "Me ___ la cabeza.", listOf("duele")),
                    fill("sa2", L, "Quisiera pedir una ___ con el médico.", listOf("cita")),
                    dictation("sa3", L, "Tengo fiebre desde ayer.", listOf("Tengo fiebre desde ayer")),
                ), prerequisites = listOf("casa")),
            f.lesson("trabajo", B1, "pro_business", g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), g("Budgets, deadlines and polite requests with usted.", "الميزانيات والمواعيد النهائية والطلبات المهذبة بصيغة usted."),
                Skill.WRITING, listOf("reunion", "empresa", "cliente", "presupuesto", "plazo", "propuesta", "adjunto", "estimado"), "usted",
                extra = listOf(
                    fill("tr1", L, "Le envío ___ el presupuesto.", listOf("adjunto")),
                    choice("tr2", L, "Which is the right way to start a formal email?", listOf("¡Hola, tío!", "Estimada señora García:", "¿Qué pasa?"), 1, Skill.FUNCTIONAL),
                    fill("tr3", L, "¿___ (poder, usted) confirmar la fecha de la reunión?", listOf("Puede")),
                    writing("tr4", L, g("Write a short email to a client proposing a meeting next week.", "اكتب رسالة قصيرة إلى عميل تقترح فيها اجتماعًا الأسبوع القادم."), 40,
                        listOf("Estimado", "reunión", "propuesta", "Un saludo")),
                )),
            f.lesson("planes", B1, "next", g("Plans for the future", "خطط المستقبل"), g("Talk about plans with ir a and the simple future.", "تحدّث عن الخطط باستخدام ir a والمستقبل البسيط."),
                Skill.SPEAKING, listOf("plan", "ir_a", "proximo", "viajar", "ahorrar", "quizas", "vacaciones"), "futuro",
                extra = listOf(
                    fill("pl1", L, "El año que viene ___ a viajar a México. (yo, ir)", listOf("voy")),
                    fill("pl2", L, "Mañana ___ (llover).", listOf("lloverá")),
                    order("pl3", L, listOf("Vamos", "a", "ahorrar", "para", "las", "vacaciones"), g("We're going to save for the holidays.", "سنوفّر المال للعطلة.")),
                    writing("pl4", L, g("Write about your plans for next year (30+ words).", "اكتب عن خططك للعام القادم (30 كلمة على الأقل)."), 30, listOf("voy a", "el próximo año", "quizás")),
                ), prerequisites = listOf("salud")),
            f.lesson("deseos", B2, "next", g("Hopes and wishes", "الآمال والأمنيات"), g("Use the present subjunctive after espero que, quiero que and ojalá.", "استخدم subjuntivo بعد espero que وquiero que وojalá."),
                Skill.GRAMMAR, listOf("espero_que", "ojala", "quiero_que", "es_importante", "suerte", "aprobar", "examen"), "subjuntivo",
                extra = listOf(
                    fill("de1", L, "Espero que ___ (tener, tú) un buen viaje.", listOf("tengas")),
                    fill("de2", L, "Quiero que mis hijos ___ (hablar) inglés.", listOf("hablen")),
                    choice("de3", L, "Ojalá ___ buen tiempo mañana.", listOf("hace", "haga", "hará"), 1),
                    translate("de4", L, g("I hope you pass the exam.", "أتمنى أن تنجح في الامتحان."), listOf("Espero que apruebes el examen", "Ojalá apruebes el examen")),
                ), prerequisites = listOf("planes")),
        )
        val units = listOf(
            CourseUnit("es_u_basics", L, A1, g("First steps", "الخطوات الأولى"), g("Greetings and family", "التحيات والعائلة"), listOf("es_l_saludos", "es_l_familia")),
            CourseUnit("es_u_everyday", L, A1, g("Everyday life", "الحياة اليومية"), g("Café and routines", "المقهى والروتين"), listOf("es_l_cafe", "es_l_rutina")),
            CourseUnit("es_u_travel", L, A2, g("Out and about", "في الخارج"), g("Travel, shopping, opinions", "السفر والتسوق والآراء"), listOf("es_l_viaje", "es_l_compras", "es_l_opiniones")),

            CourseUnit("es_u_home", L, A2, g("Home and health", "البيت والصحة"), g("Your home, the weather and the doctor", "بيتك والطقس والطبيب"), listOf("es_l_casa", "es_l_salud")),
            CourseUnit("es_u_pro_business", L, B1, g("Spanish for work", "الإسبانية للعمل"), g("Meetings and formal emails", "الاجتماعات والرسائل الرسمية"), listOf("es_l_trabajo"), trackId = "business"),
            CourseUnit("es_u_next", L, B1, g("Next steps", "الخطوة التالية"), g("Plans, hopes and wishes", "الخطط والآمال والأمنيات"), listOf("es_l_planes", "es_l_deseos")),
        )
        val readings = listOf(
            ReadingPassage(
                "es_r_laila", L, A1, "Un día de Laila", g("A day in Laila's life", "يوم من حياة ليلى"),
                listOf("Laila se levanta a las siete.", "Toma un café con leche y pan.", "Trabaja en una tienda en el centro.", "Por la tarde estudia español.", "Está cansada, pero contenta."),
                listOf(g("Laila gets up at seven.", "تنهض ليلى في السابعة."), g("She has a coffee with milk and bread.", "تتناول قهوة بالحليب وخبزًا."),
                    g("She works in a shop in the centre.", "تعمل في متجر في وسط المدينة."), g("In the afternoon she studies Spanish.", "في المساء تدرس الإسبانية."),
                    g("She is tired, but happy.", "هي متعبة لكنها مسرورة.")),
                listOf("es_levantarse", "es_cafe", "es_pan", "es_trabajar", "es_estudiar", "es_cansado", "es_contento"),
                listOf(
                    Exercise.MultipleChoice("es_r_laila_q1", Skill.READING, Instr.choose, "¿A qué hora se levanta Laila?", listOf("A las seis", "A las siete", "A las ocho"), 1),
                    Exercise.MultipleChoice("es_r_laila_q2", Skill.READING, Instr.choose, "¿Dónde trabaja?", listOf("En una tienda", "En un hotel", "En una escuela"), 0),
                    Exercise.MultipleChoice("es_r_laila_q3", Skill.READING, Instr.choose, "¿Cómo está?", listOf("Triste", "Cansada pero contenta", "Enferma"), 1),
                ),
            ),
        )
        val scenarios = listOf(
            RolePlayScenario(
                "es_s_cafe", L, A1, g("At a café in Madrid", "في مقهى في مدريد"), g("A café on a busy street.", "مقهى في شارع مزدحم."),
                g("Order a drink and something to eat, then pay.", "اطلب مشروبًا وشيئًا للأكل ثم ادفع."),
                "¡Hola! ¿Qué le pongo?", g("Hi! What can I get you?", "أهلًا! ماذا أقدّم لك؟"),
                listOf(
                    ScenarioBeat(listOf("café", "cafe", "agua", "té", "zumo", "jugo", "quisiera", "quiero"), g("Order a drink: 'Quisiera un café, por favor.'", "اطلب مشروبًا: Quisiera un café, por favor."),
                        "Quisiera un café con leche, por favor.", "Muy bien. ¿Algo para comer?", g("Very good. Anything to eat?", "جيد جدًا. هل تريد شيئًا للأكل؟")),
                    ScenarioBeat(listOf("sí", "si", "no", "tostada", "pan", "croissant", "bocadillo"), g("Say what you'd like to eat, or 'No, gracias.'", "قل ما تريد أكله أو No, gracias."),
                        "Sí, una tostada, por favor.", "Perfecto. Ahora mismo se lo traigo.", g("Perfect. I'll bring it right away.", "ممتاز. سأحضره حالًا.")),
                    ScenarioBeat(listOf("cuenta", "cuánto", "cuanto", "pagar"), g("Ask for the bill: 'La cuenta, por favor.'", "اطلب الحساب: La cuenta, por favor."),
                        "La cuenta, por favor.", "Son cuatro euros con cincuenta.", g("That's four euros fifty.", "المبلغ أربعة يوروهات وخمسون سنتًا.")),
                ),
                listOf("Quisiera…", "¿Me pone…?", "La cuenta, por favor.", "¿Cuánto es?"),
            ),
            RolePlayScenario(
                "es_s_direcciones", L, A2, g("Asking for directions", "السؤال عن الاتجاهات"), g("In Mexico City, looking for the metro.", "في مدينة مكسيكو، تبحث عن المترو."),
                g("Find out how to get to the metro station.", "اعرف كيف تصل إلى محطة المترو."),
                "¡Buenas tardes! ¿Le puedo ayudar?", g("Good afternoon! Can I help you?", "مساء الخير! هل أستطيع مساعدتك؟"),
                listOf(
                    ScenarioBeat(listOf("dónde", "donde", "metro", "estación", "estacion"), g("Ask: '¿Dónde está la estación de metro?'", "اسأل: ¿Dónde está la estación de metro?"),
                        "Sí, por favor. ¿Dónde está la estación de metro?", "Siga todo recto y gire a la izquierda en la segunda calle.",
                        g("Go straight on and turn left at the second street.", "امشِ إلى الأمام ثم انعطف يسارًا في الشارع الثاني.")),
                    ScenarioBeat(listOf("lejos", "cerca", "minutos"), g("Ask if it's far: '¿Está lejos?'", "اسأل إن كانت بعيدة: ¿Está lejos?"),
                        "¿Está lejos?", "No, está a unos cinco minutos a pie.", g("No, it's about five minutes on foot.", "لا، على بعد خمس دقائق سيرًا تقريبًا.")),
                    ScenarioBeat(listOf("gracias"), g("Say thank you.", "قل شكرًا."), "¡Muchas gracias!", "¡De nada! ¡Buen viaje!", g("You're welcome! Have a good trip!", "عفوًا! رحلة سعيدة!")),
                ),
                listOf("¿Dónde está…?", "¿Está lejos?", "a la izquierda / a la derecha"),
            ),

            RolePlayScenario(
                "es_s_medico", L, A2, g("At the doctor's", "عند الطبيب"), g("A health centre in Seville. Language practice only — not medical advice.", "مركز صحي في إشبيلية. للتدرّب اللغوي فقط، وليس استشارة طبية."),
                g("Explain what hurts, say since when, and ask about the pharmacy.", "اشرح ما يؤلمك ومنذ متى، واسأل عن الصيدلية."),
                "Buenos días. Siéntese, por favor. ¿Qué le pasa?", g("Good morning. Please sit down. What's the matter?", "صباح الخير. تفضّل بالجلوس. ما بك؟"),
                listOf(
                    ScenarioBeat(listOf("duele", "dolor", "cabeza", "fiebre", "enfermo", "enferma"), g("Say what hurts: 'Me duele la cabeza.'", "قل ما يؤلمك: Me duele la cabeza."),
                        "Me duele la cabeza y tengo fiebre.", "¿Desde cuándo?", g("Since when?", "منذ متى؟")),
                    ScenarioBeat(listOf("desde", "ayer", "días", "dias", "semana"), g("Say since when: 'Desde ayer.'", "قل منذ متى: Desde ayer."),
                        "Desde ayer por la tarde.", "Bien. Descanse y beba mucha agua.", g("All right. Rest and drink plenty of water.", "حسنًا. استرح واشرب الكثير من الماء.")),
                    ScenarioBeat(listOf("farmacia", "dónde", "donde", "receta"), g("Ask where the pharmacy is.", "اسأل أين الصيدلية."),
                        "¿Dónde está la farmacia más cercana?", "Está aquí al lado, a la derecha.", g("It's right next door, on the right.", "إنها بجانبنا مباشرة، على اليمين.")),
                ),
                listOf("Me duele…", "Tengo fiebre.", "¿Desde cuándo?", "¿Dónde está la farmacia?"),
            ),
            RolePlayScenario(
                "es_s_reunion", L, B1, g("Planning a project with a client", "التخطيط لمشروع مع عميل"), g("A video call with a client in Bogotá.", "مكالمة فيديو مع عميل في بوغوتا."),
                g("Agree on the budget, the deadline and the next step.", "اتفق على الميزانية والموعد النهائي والخطوة التالية."),
                "Buenos días. Gracias por la propuesta. ¿Hablamos del presupuesto?", g("Good morning. Thanks for the proposal. Shall we talk about the budget?", "صباح الخير. شكرًا على المقترح. هل نتحدث عن الميزانية؟"),
                listOf(
                    ScenarioBeat(listOf("presupuesto", "precio", "euros", "dólares", "dolares", "coste", "costo"), g("Explain the budget.", "اشرح الميزانية."),
                        "Claro. El presupuesto total es de cinco mil dólares.", "Perfecto. ¿Y el plazo de entrega?", g("Perfect. And the delivery deadline?", "ممتاز. وماذا عن موعد التسليم؟")),
                    ScenarioBeat(listOf("plazo", "semanas", "mes", "fecha", "viernes"), g("Give a deadline: 'en seis semanas'.", "حدّد موعدًا: en seis semanas."),
                        "Podemos entregarlo en seis semanas.", "Nos parece bien.", g("That works for us.", "يناسبنا ذلك.")),
                    ScenarioBeat(listOf("correo", "enviar", "envío", "envio", "contrato", "adjunto"), g("Say you will send a summary by email.", "قل إنك سترسل ملخصًا بالبريد الإلكتروني."),
                        "Le envío hoy un correo con el resumen.", "Muchas gracias. Hasta pronto.", g("Thank you very much. Speak soon.", "شكرًا جزيلًا. إلى اللقاء قريبًا.")),
                ),
                listOf("El presupuesto es de…", "Podemos entregarlo en…", "Le envío…"),
                trackId = "business",
            ),
        )
        fun item(id: String, level: CefrLevel, skill: Skill, prompt: String, options: List<String>, correct: Int) =
            AssessmentItem("es_a_$id", L, level, skill, Exercise.MultipleChoice("es_a_$id", skill, Instr.choose, prompt, options, correct))
        val assessment = listOf(
            item("a1_1", A1, Skill.GRAMMAR, "Yo ___ de Marruecos.", listOf("soy", "estoy", "es"), 0),
            item("a1_2", A1, Skill.VOCABULARY, "'Gracias' significa…", listOf("please", "thank you", "goodbye"), 1),
            item("a1_3", A1, Skill.GRAMMAR, "___ casa es grande.", listOf("El", "La", "Los"), 1),
            item("a2_1", A2, Skill.GRAMMAR, "¿Dónde ___ el banco?", listOf("es", "está", "son"), 1),
            item("a2_2", A2, Skill.GRAMMAR, "Nosotros ___ (trabajar) en una oficina.", listOf("trabajamos", "trabajan", "trabajo"), 0),
            item("a2_3", A2, Skill.VOCABULARY, "Para pagar en un restaurante pides…", listOf("la carta", "la cuenta", "el billete"), 1),
            item("b1_1", B1, Skill.GRAMMAR, "Ayer ___ (comer, yo) en casa de mis padres.", listOf("como", "comí", "comeré"), 1),
            item("b1_2", B1, Skill.GRAMMAR, "Cuando era niño, ___ (jugar) al fútbol todos los días.", listOf("jugué", "jugaba", "juego"), 1),
            item("b1_3", B1, Skill.GRAMMAR, "Espero que ___ buen tiempo mañana.", listOf("hace", "haga", "hará"), 1),
            item("b2_1", B2, Skill.GRAMMAR, "Si tuviera dinero, ___ un coche.", listOf("compro", "compraría", "compraré"), 1),
            item("b2_2", B2, Skill.GRAMMAR, "No creo que ___ razón.", listOf("tienes", "tengas", "tendrás"), 1),
            item("b2_3", B2, Skill.VOCABULARY, "'Echar de menos' significa…", listOf("to miss someone", "to throw away", "to be late"), 0),
        )
        return LanguageCurriculum(L, vocabulary, grammar, units, lessons, readings, scenarios, assessment,
            tracks = listOf(LearningTrack("business", L, g("Spanish for work", "الإسبانية للعمل"), g("Meetings, formal emails, budgets and deadlines.", "الاجتماعات والرسائل الرسمية والميزانيات والمواعيد النهائية."), A2, null)),
            dictations = listOf(
                dictation("d1", L, "Me llamo Laila y soy estudiante.", listOf("Me llamo Laila y soy estudiante")),
                dictation("d2", L, "¿Dónde está la estación?", listOf("Dónde está la estación")),
                dictation("d3", L, "Normalmente trabajo por la mañana.", listOf("Normalmente trabajo por la mañana")),
            ),
            writingPrompts = listOf(writing("wp1", L, g("Introduce yourself and your family.", "عرّف بنفسك وبعائلتك."), 30, listOf("me llamo", "mi familia", "tengo"))),
        )
    }
}
