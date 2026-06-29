package it.unicam.cs.mpgc.rpg130525.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per i nemici minori ({@link NemicoMinore}, {@link Dubbio},
 * {@link Distrazione}).
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class NemicoMinoreTest {

    /** Studente con 100 + 20 (bonus full-time) = 120 HP. */
    private Studente nuovoStudente() {
        return new Studente("Mario", "Rossi", 10, 5, 100, 50, new StudenteFullTime());
    }

    @Test
    void attaccoSottraeSaluteMentale() {
        Studente s = nuovoStudente();
        new Dubbio(30).attacca(s);
        assertEquals(90, s.getSaluteMentale());
    }

    @Test
    void attacchiMultipliSiAccumulano() {
        Studente s = nuovoStudente();
        new Dubbio(30).attacca(s);
        new Distrazione(20).attacca(s);
        assertEquals(70, s.getSaluteMentale());
    }

    @Test
    void attaccoLetaleProvocaBurnout() {
        Studente s = nuovoStudente();
        assertThrows(BurnoutException.class, () -> new Distrazione(200).attacca(s));
    }

    @Test
    void dubbioEDistrazioneHannoNomiDistinti() {
        assertEquals("Dubbio", new Dubbio(5).getNome());
        assertEquals("Distrazione", new Distrazione(5).getNome());
    }

    @Test
    void costruttoreRifiutaDannoNonPositivo() {
        assertThrows(IllegalArgumentException.class, () -> new Dubbio(0));
        assertThrows(IllegalArgumentException.class, () -> new Distrazione(-5));
    }

    @Test
    void attaccoRifiutaStudenteNull() {
        assertThrows(IllegalArgumentException.class, () -> new Dubbio(10).attacca(null));
    }
}
