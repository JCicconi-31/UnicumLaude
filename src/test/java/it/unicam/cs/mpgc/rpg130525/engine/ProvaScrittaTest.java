package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.port.GameInput;
import it.unicam.cs.mpgc.rpg130525.port.GameView;
import it.unicam.cs.mpgc.rpg130525.port.ProfessoreDto;
import it.unicam.cs.mpgc.rpg130525.port.StanzaDto;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link ProvaScritta}: estrazione della domanda dal database,
 * valutazione della risposta fornita dall'utente e danno in caso di errore.
 * La View è un test double muto e l'input è un test double che restituisce un
 * indice prefissato, così la fase è verificabile in isolamento.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class ProvaScrittaTest {

    private static final GameView VIEW_MUTA = new GameView() {
        @Override public void mostraMessaggio(String messaggio) { }
        @Override public void aggiornaStatoGiocatore(StudenteDto studente) { }
        @Override public void aggiornaStatoProfessore(ProfessoreDto professoreDto) { }
        @Override public void aggiornaMappa(List<StanzaDto> stanze, String posizioneCorrente) { }
    };

    /** La domanda di test: l'opzione corretta è l'indice 1. */
    private static final Domanda DOMANDA =
            new Domanda("Quale principio riguarda l'estensibilità?", List.of("SRP", "OCP", "DIP"), 1);

    private final CalcolatoreDanno calcolatore = new CalcolatoreDanno();

    /** Database con un solo corso e una sola domanda: l'estrazione è deterministica. */
    /** Input finto che risponde sempre con l'indice dato; gli altri metodi non servono qui. */
    private static GameInput rispostaFissa(int indice) {
        return new GameInput() {
            @Override public int chiediRisposta(Domanda domanda) { return indice; }
            @Override public int scegli(String titolo, List<String> opzioni) {
                throw new UnsupportedOperationException();
            }
            @Override public String chiediTesto(String prompt) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private DatabaseDomande database() {
        Map<String, List<Domanda>> dati = Map.of("Programmazione", List.of(DOMANDA));
        return new DatabaseDomande(dati, new Random(0));
    }

    private Studente nuovoStudente() {
        return new Studente("Mario", "Rossi", 10, 5, 100, 50, new StudenteFullTime()); // resil 5, HP 120
    }

    /** Stato con lo studente in un'aula del corso "Programmazione". */
    private StatoGioco statoConProfessore(Studente studente, Professore prof) {
        Esame esame = new Esame(1, "Programmazione", 6, prof);
        Stanza aula = new Stanza("LA1", TipoStanza.AULA_ESAME, esame);
        return new StatoGioco(studente, aula);
    }

    @Test
    void rispostaCorrettaSuperaLaFaseSenzaDanno() {
        Studente s = nuovoStudente();
        StatoGioco stato = statoConProfessore(s, new Professore("Anna", "Verdi", 8, 50));
        GameInput rispondeCorretto = rispostaFissa(1); // indice corretto

        ProvaScritta prova = new ProvaScritta(database(), rispondeCorretto, calcolatore);

        assertTrue(prova.esegui(stato, VIEW_MUTA));
        assertEquals(120, s.getSaluteMentale()); // nessun danno
    }

    @Test
    void rispostaSbagliataFallisceLaFaseEInfliggeDanno() {
        Studente s = nuovoStudente();
        StatoGioco stato = statoConProfessore(s, new Professore("Anna", "Verdi", 8, 50)); // difficoltà 8
        GameInput rispondeSbagliato = rispostaFissa(0); // indice errato

        ProvaScritta prova = new ProvaScritta(database(), rispondeSbagliato, calcolatore);

        assertFalse(prova.esegui(stato, VIEW_MUTA));
        assertEquals(117, s.getSaluteMentale()); // 120 - max(1, 8 - 5) = 120 - 3
    }

    @Test
    void rispostaSbagliataLetaleProvocaBurnout() {
        Studente s = nuovoStudente();
        s.subisciDanno(119); // salute portata a 1
        StatoGioco stato = statoConProfessore(s, new Professore("Anna", "Verdi", 50, 50));
        GameInput rispondeSbagliato = rispostaFissa(0);

        ProvaScritta prova = new ProvaScritta(database(), rispondeSbagliato, calcolatore);

        assertThrows(BurnoutException.class, () -> prova.esegui(stato, VIEW_MUTA));
    }

    @Test
    void laDomandaEstrattaVienePassataAllInput() {
        Studente s = nuovoStudente();
        StatoGioco stato = statoConProfessore(s, new Professore("Anna", "Verdi", 8, 50));
        Domanda[] ricevuta = new Domanda[1];
        GameInput registra = new GameInput() {
            @Override public int chiediRisposta(Domanda domanda) { ricevuta[0] = domanda; return 1; }
            @Override public int scegli(String titolo, List<String> opzioni) {
                throw new UnsupportedOperationException();
            }
            @Override public String chiediTesto(String prompt) {
                throw new UnsupportedOperationException();
            }
        };

        new ProvaScritta(database(), registra, calcolatore).esegui(stato, VIEW_MUTA);

        assertSame(DOMANDA, ricevuta[0]); // l'input riceve esattamente la domanda del corso
    }

    @Test
    void costruttoreRifiutaParametriNull() {
        GameInput input = rispostaFissa(0);
        assertThrows(IllegalArgumentException.class, () -> new ProvaScritta(null, input, calcolatore));
        assertThrows(IllegalArgumentException.class, () -> new ProvaScritta(database(), null, calcolatore));
        assertThrows(IllegalArgumentException.class, () -> new ProvaScritta(database(), input, null));
    }
}
