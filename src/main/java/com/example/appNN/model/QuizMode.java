// com.example.appNN.model.QuizMode
package com.example.appNN.model;

public enum QuizMode {
    WORD_PRON_TO_MEANING, // C1: word + pronunciation -> meaning
    WORD_TO_MEANING,      // C2: word -> meaning
    MEANING_TO_WORD,      // C3: meaning -> word
    FILL_IN               // C4: meaning -> word (fill in the blank)
}
