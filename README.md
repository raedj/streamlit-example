# Language Learner

A small Streamlit app for practicing vocabulary in Spanish, French and German.

## Features

- **Flashcards**: flip a card, then mark whether you knew it.
- **Multiple choice**: pick the right translation out of four options.
- **Typing**: type the translation. Accents are optional unless you turn on strict mode.
- **Word list**: browse every word in the topics you picked.
- **Progress**: see your accuracy and which words to review.

You can practice in either direction (English → target or target → English) and pick topics
(Greetings, Food, Numbers, Family). Words you miss come up more often.

## Run locally

```bash
pip install -r requirements.txt
streamlit run streamlit_app.py
```

To add words or languages, edit the `VOCAB` dictionary at the top of `streamlit_app.py`.
