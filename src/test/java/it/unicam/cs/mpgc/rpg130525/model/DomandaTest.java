package it.unicam.cs.mpgc.rpg130525.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link Domanda}.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class DomandaTest {

    @Test
    void isCorrettaRiconosceLaRispostaGiusta() {
        Domanda d = new Domanda("2 + 2 = ?", List.of("3", "4", "5"), 1);
        assertTrue(d.isCorretta(1));
        assertFalse(d.isCorretta(0));
        assertFalse(d.isCorretta(2));
    }

    @Test
    void costruttoreRifiutaTestoVuoto() {
        assertThrows(IllegalArgumentException.class,
                () -> new Domanda("   ", List.of("a", "b"), 0));
    }

    @Test
    void costruttoreRifiutaTestoNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Domanda(null, List.of("a", "b"), 0));
    }

    @Test
    void costruttoreRifiutaMenoDiDueOpzioni() {
        assertThrows(IllegalArgumentException.class,
                () -> new Domanda("domanda", List.of("unica"), 0));
    }

    @Test
    void costruttoreRifiutaIndiceFuoriRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new Domanda("domanda", List.of("a", "b"), 2));
        assertThrows(IllegalArgumentException.class,
                () -> new Domanda("domanda", List.of("a", "b"), -1));
    }

    @Test
    void leOpzioniRestituiteSonoImmutabili() {
        Domanda d = new Domanda("domanda", List.of("a", "b"), 0);
        assertThrows(UnsupportedOperationException.class,
                () -> d.getOpzioni().add("c"));
    }

    @Test
    void leOpzioniSonoCopiaDifensiva() {
        List<String> sorgente = new java.util.ArrayList<>(List.of("a", "b"));
        Domanda d = new Domanda("domanda", sorgente, 0);
        sorgente.set(0, "modificata"); // modifica esterna dopo la costruzione
        assertEquals("a", d.getOpzioni().getFirst()); // la domanda non è stata intaccata
    }
}
