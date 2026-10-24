import random
import unicodedata

import pandas as pd
import streamlit as st

# Vocabulary: English -> translation, grouped by language and topic.
VOCAB = {
    "Spanish": {
        "Greetings": {
            "hello": "hola", "goodbye": "adiós", "please": "por favor",
            "thank you": "gracias", "good morning": "buenos días",
            "good night": "buenas noches", "yes": "sí", "no": "no",
        },
        "Food": {
            "bread": "pan", "water": "agua", "apple": "manzana",
            "cheese": "queso", "milk": "leche", "egg": "huevo",
            "chicken": "pollo", "rice": "arroz",
        },
        "Numbers": {
            "one": "uno", "two": "dos", "three": "tres", "four": "cuatro",
            "five": "cinco", "six": "seis", "seven": "siete", "eight": "ocho",
        },
        "Family": {
            "mother": "madre", "father": "padre", "brother": "hermano",
            "sister": "hermana", "son": "hijo", "daughter": "hija",
            "grandmother": "abuela", "grandfather": "abuelo",
        },
    },
    "French": {
        "Greetings": {
            "hello": "bonjour", "goodbye": "au revoir", "please": "s'il vous plaît",
            "thank you": "merci", "good evening": "bonsoir",
            "good night": "bonne nuit", "yes": "oui", "no": "non",
        },
        "Food": {
            "bread": "pain", "water": "eau", "apple": "pomme",
            "cheese": "fromage", "milk": "lait", "egg": "œuf",
            "chicken": "poulet", "rice": "riz",
        },
        "Numbers": {
            "one": "un", "two": "deux", "three": "trois", "four": "quatre",
            "five": "cinq", "six": "six", "seven": "sept", "eight": "huit",
        },
        "Family": {
            "mother": "mère", "father": "père", "brother": "frère",
            "sister": "sœur", "son": "fils", "daughter": "fille",
            "grandmother": "grand-mère", "grandfather": "grand-père",
        },
    },
    "German": {
        "Greetings": {
            "hello": "hallo", "goodbye": "auf Wiedersehen", "please": "bitte",
            "thank you": "danke", "good morning": "guten Morgen",
            "good night": "gute Nacht", "yes": "ja", "no": "nein",
        },
        "Food": {
            "bread": "Brot", "water": "Wasser", "apple": "Apfel",
            "cheese": "Käse", "milk": "Milch", "egg": "Ei",
            "chicken": "Hähnchen", "rice": "Reis",
        },
        "Numbers": {
            "one": "eins", "two": "zwei", "three": "drei", "four": "vier",
            "five": "fünf", "six": "sechs", "seven": "sieben", "eight": "acht",
        },
        "Family": {
            "mother": "Mutter", "father": "Vater", "brother": "Bruder",
            "sister": "Schwester", "son": "Sohn", "daughter": "Tochter",
            "grandmother": "Großmutter", "grandfather": "Großvater",
        },
    },
}


def normalize(text, strict_accents):
    text = text.strip().lower()
    if not strict_accents:
        text = unicodedata.normalize("NFKD", text)
        text = "".join(c for c in text if not unicodedata.combining(c))
        text = text.replace("ß", "ss").replace("œ", "oe")
    return text


def get_stats(language, word):
    stats = st.session_state.setdefault("stats", {})
    return stats.setdefault((language, word), {"right": 0, "wrong": 0})


def record(language, word, correct):
    s = get_stats(language, word)
    s["right" if correct else "wrong"] += 1


def pick_word(language, words):
    # Favor words the learner gets wrong more often.
    weights = []
    for w in words:
        s = get_stats(language, w)
        weights.append(1 + 2 * s["wrong"] - 0.5 * min(s["right"], 1))
    return random.choices(words, weights=weights, k=1)[0]


def new_question(key, language, pairs, direction):
    words = list(pairs)
    word = pick_word(language, words)
    prompt, answer = (word, pairs[word]) if direction == "English → target" else (pairs[word], word)
    others = [w for w in words if w != word]
    distractors = random.sample(others, min(3, len(others)))
    if direction == "English → target":
        options = [pairs[w] for w in distractors] + [answer]
    else:
        options = distractors + [answer]
    random.shuffle(options)
    st.session_state["q_count"] = st.session_state.get("q_count", 0) + 1
    st.session_state[key] = {
        "id": st.session_state["q_count"], "word": word, "prompt": prompt, "answer": answer,
        "options": options, "result": None,
    }


st.set_page_config(page_title="Language Learner", page_icon="🗣️")

with st.sidebar:
    st.header("Settings")
    language = st.selectbox("Language", list(VOCAB))
    topics = st.multiselect("Topics", list(VOCAB[language]), default=list(VOCAB[language]))
    direction = st.radio("Direction", ["English → target", "Target → English"])
    strict_accents = st.checkbox("Require exact accents when typing", value=False)
    mode = st.radio("Mode", ["Flashcards", "Multiple choice", "Typing", "Word list", "Progress"])

st.title(f"🗣️ Language Learner — {language}")

if not topics:
    st.info("Pick at least one topic in the sidebar to get started.")
    st.stop()

pairs = {}
for t in topics:
    pairs.update(VOCAB[language][t])

# Reset the current question whenever the settings change.
settings = (language, tuple(topics), direction, mode)
if st.session_state.get("settings") != settings:
    st.session_state["settings"] = settings
    for k in ("card", "mc", "typing"):
        st.session_state.pop(k, None)

if mode == "Flashcards":
    if "card" not in st.session_state:
        new_question("card", language, pairs, direction)
        st.session_state["flipped"] = False
    card = st.session_state["card"]

    st.markdown(f"<h1 style='text-align:center'>{card['prompt']}</h1>", unsafe_allow_html=True)
    if st.session_state["flipped"]:
        st.markdown(f"<h2 style='text-align:center;color:#0068c9'>{card['answer']}</h2>",
                    unsafe_allow_html=True)
        c1, c2 = st.columns(2)
        if c1.button("✅ I knew it", width="stretch"):
            record(language, card["word"], True)
            new_question("card", language, pairs, direction)
            st.session_state["flipped"] = False
            st.rerun()
        if c2.button("❌ Still learning", width="stretch"):
            record(language, card["word"], False)
            new_question("card", language, pairs, direction)
            st.session_state["flipped"] = False
            st.rerun()
    elif st.button("Show answer", width="stretch"):
        st.session_state["flipped"] = True
        st.rerun()

elif mode == "Multiple choice":
    if "mc" not in st.session_state:
        new_question("mc", language, pairs, direction)
    q = st.session_state["mc"]

    st.subheader(f"What is the translation of **{q['prompt']}**?")
    choice = st.radio("Choose one:", q["options"], index=None, key=f"mc_choice_{q['id']}")
    if q["result"] is None:
        if st.button("Check", disabled=choice is None):
            q["result"] = choice == q["answer"]
            record(language, q["word"], q["result"])
            st.rerun()
    else:
        if q["result"]:
            st.success("Correct! 🎉")
        else:
            st.error(f"Not quite — the answer is **{q['answer']}**.")
        if st.button("Next question"):
            new_question("mc", language, pairs, direction)
            st.rerun()

elif mode == "Typing":
    if "typing" not in st.session_state:
        new_question("typing", language, pairs, direction)
    q = st.session_state["typing"]

    st.subheader(f"Type the translation of **{q['prompt']}**")
    with st.form("typing_form", clear_on_submit=False):
        guess = st.text_input("Your answer", key=f"typing_{q['id']}")
        submitted = st.form_submit_button("Check", disabled=q["result"] is not None)
    if submitted and q["result"] is None:
        q["result"] = normalize(guess, strict_accents) == normalize(q["answer"], strict_accents)
        record(language, q["word"], q["result"])
    if q["result"] is not None:
        if q["result"]:
            st.success(f"Correct! 🎉 ({q['answer']})")
        else:
            st.error(f"Not quite — the answer is **{q['answer']}**.")
        if st.button("Next word"):
            new_question("typing", language, pairs, direction)
            st.rerun()

elif mode == "Word list":
    df = pd.DataFrame(
        [(topic, en, tr) for topic in topics for en, tr in VOCAB[language][topic].items()],
        columns=["Topic", "English", language],
    )
    st.dataframe(df, hide_index=True, width="stretch")

elif mode == "Progress":
    rows = []
    for en in pairs:
        s = get_stats(language, en)
        total = s["right"] + s["wrong"]
        rows.append({
            "English": en, language: pairs[en], "Right": s["right"], "Wrong": s["wrong"],
            "Accuracy": f"{s['right'] / total:.0%}" if total else "—",
        })
    df = pd.DataFrame(rows)
    right, wrong = df["Right"].sum(), df["Wrong"].sum()
    c1, c2, c3 = st.columns(3)
    c1.metric("Answers", int(right + wrong))
    c2.metric("Correct", int(right))
    c3.metric("Accuracy", f"{right / (right + wrong):.0%}" if right + wrong else "—")
    st.subheader("Words to review")
    review = df[df["Wrong"] > 0].sort_values("Wrong", ascending=False)
    if review.empty:
        st.write("Nothing to review yet — keep practicing!")
    else:
        st.dataframe(review, hide_index=True, width="stretch")
    with st.expander("All words"):
        st.dataframe(df, hide_index=True, width="stretch")
    if st.button("Reset progress"):
        st.session_state["stats"] = {}
        st.rerun()
