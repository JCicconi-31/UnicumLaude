package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link ControllerMovimento}: spostamento fisico lungo i
 * corridoi e verifica della propedeuticità all'ingresso nelle aule d'esame.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class ControllerMovimentoTest {

    private Esame esame1;
    private Stanza atrio;
    private Stanza aula1;
    private Stanza aula2;
    private Stanza stanzaIsolata;
    private Studente studente;
    private StatoGioco stato;
    private ControllerMovimento controller;

    @BeforeEach
    void setUp() {
        Professore prof = new Professore("Anna", "Verdi", 5, 50);
        esame1 = new Esame(1, "Programmazione", 6, prof);
        Esame esame2 = new Esame(2, "Algoritmi", 6, prof);

        atrio = new Stanza("Atrio", TipoStanza.CORRIDOIO, null);
        aula1 = new Stanza("LA1", TipoStanza.AULA_ESAME, esame1);
        aula2 = new Stanza("LA2", TipoStanza.AULA_ESAME, esame2);
        stanzaIsolata = new Stanza("Seminterrato", TipoStanza.CORRIDOIO, null);

        Mappa mappa = new Mappa();
        mappa.addStanza(atrio);
        mappa.addStanza(aula1);
        mappa.addStanza(aula2);
        mappa.addStanza(stanzaIsolata);

        mappa.addCorridoio(atrio, aula1);
        mappa.addCorridoio(atrio, aula2);
        // stanzaIsolata volutamente senza corridoi verso l'atrio

        mappa.addPropedeuticita(aula1, aula2); // aula1 è prerequisito di aula2

        studente = new Studente("Mario", "Rossi", 10, 5, 100, 50, new StudenteFullTime());
        stato = new StatoGioco(studente, atrio);
        controller = new ControllerMovimento(mappa);
    }

    @Test
    void spostamentoVersoUnAulaSenzaPrerequisitiAggiornaLaPosizione() throws PrerequisitiNonRispettatiException {
        controller.spostati(stato, aula1);
        assertEquals(aula1, stato.getPosizioneCorrente());
    }

    @Test
    void spostamentoBloccatoSeIPrerequisitiNonSonoSoddisfatti() {
        assertThrows(PrerequisitiNonRispettatiException.class, () -> controller.spostati(stato, aula2));
        assertEquals(atrio, stato.getPosizioneCorrente()); // posizione invariata
    }

    @Test
    void dopoAverSuperatoIlPrerequisitoLAulaDiventaAccessibile() throws PrerequisitiNonRispettatiException {
        studente.getLibretto().addEsameSuperato(new EsameSuperato(esame1, 30));
        controller.spostati(stato, aula2);
        assertEquals(aula2, stato.getPosizioneCorrente());
    }

    @Test
    void spostamentoVersoStanzaNonAdiacenteVieneRifiutato() {
        assertThrows(IllegalArgumentException.class, () -> controller.spostati(stato, stanzaIsolata));
    }

    @Test
    void parametriNulliVengonoRifiutati() {
        assertThrows(IllegalArgumentException.class, () -> controller.spostati(null, aula1));
        assertThrows(IllegalArgumentException.class, () -> controller.spostati(stato, null));
    }

    @Test
    void costruttoreRifiutaMappaNull() {
        assertThrows(IllegalArgumentException.class, () -> new ControllerMovimento(null));
    }
}
