package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.port.GameInput;
import it.unicam.cs.mpgc.rpg130525.port.GameView;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceManager;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link GameLoop}: costruzione delle azioni disponibili,
 * movimento con vincolo di propedeuticità, esame con accredito CFU e vittoria,
 * Burnout con game over, riposo in aula studio e salvataggio all'uscita.
 * View, input e persistenza sono test double: l'input è una sequenza
 * prefissata di scelte, la persistenza registra le chiamate in memoria.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class GameLoopTest {

    /** View che registra i messaggi, per verificare vittoria/burnout/vincoli. */
    private static final class RecordingView implements GameView {
        final List<String> messaggi = new ArrayList<>();
        @Override public void mostraMessaggio(String messaggio) { messaggi.add(messaggio); }
        @Override public void aggiornaStatoGiocatore(StudenteDto studente) { }
    }

    /** Input che restituisce una sequenza prefissata di scelte. */
    private static GameInput script(int... scelte) {
        Deque<Integer> coda = new ArrayDeque<>();
        for (int s : scelte) coda.add(s);
        return new GameInput() {
            @Override public int chiediRisposta(Domanda domanda) { return coda.remove(); }
            @Override public int scegli(String titolo, List<String> opzioni) { return coda.remove(); }
            @Override public String chiediTesto(String prompt) { return "test"; }
        };
    }

    /** Persistenza in memoria: registra il salvataggio senza toccare il disco. */
    private static final class PersistenzaInMemoria implements PersistenceManager {
        StatoGioco salvato;
        @Override public void salva(StatoGioco stato) { salvato = stato; }
        @Override public StatoGioco carica() { return salvato; }
        @Override public boolean esisteSalvataggio() { return salvato != null; }
    }

    private Mappa mappa;
    private Stanza atrio;
    private Stanza aulaStudio;
    private Stanza la1;
    private Stanza la2;
    private Esame esame1;
    private Studente studente;
    private StatoGioco stato;
    private RecordingView view;
    private PersistenzaInMemoria persistence;

    @BeforeEach
    void setUp() {
        Professore prof = new Professore("Anna", "Verdi", 4, 30);
        esame1 = new Esame(1, "Programmazione", 6, prof);
        Esame esame2 = new Esame(2, "Metodologie", 6, prof);

        atrio = new Stanza("Atrio", TipoStanza.CORRIDOIO, null);
        aulaStudio = new Stanza("Aula Studio", TipoStanza.AULA_STUDIO, null);
        la1 = new Stanza("Aula LA1", TipoStanza.AULA_ESAME, esame1);
        la2 = new Stanza("Aula LA2", TipoStanza.AULA_ESAME, esame2);

        mappa = new Mappa();
        mappa.addStanza(atrio);
        mappa.addStanza(aulaStudio);
        mappa.addStanza(la1);
        mappa.addStanza(la2);
        mappa.addCorridoio(atrio, aulaStudio);
        mappa.addCorridoio(atrio, la1);
        mappa.addCorridoio(atrio, la2);
        mappa.addPropedeuticita(la1, la2);

        studente = new Studente("Mario", "Rossi", 10, 5, 100, 50, new StudenteFullTime());
        stato = new StatoGioco(studente, atrio);
        view = new RecordingView();
        persistence = new PersistenzaInMemoria();
    }

    /**
     * GameLoop con fasi d'esame stub. Nell'Atrio le azioni sono (ordinate):
     * 0) Vai a: Aula LA1 - 1) Vai a: Aula LA2 - 2) Vai a: Aula Studio - 3) Salva ed esci
     */
    private GameLoop loop(FaseEsame... fasi) {
        EsameController controller = new EsameController(List.of(fasi));
        return new GameLoop(mappa, new ControllerMovimentoStanze(mappa),
                controller, persistence, 12);
    }

    @Test
    void salvaEdEsciSalvaLaPartitaETermina() {
        loop((s, v) -> true).gioca(stato, view, script(3));
        assertSame(stato, persistence.salvato);
        assertTrue(view.messaggi.stream().anyMatch(m -> m.contains("salvata")));
    }

    @Test
    void aulaConPrerequisitiNonSoddisfattiRestaInaccessibile() {
        // vai verso LA2 (bloccata da LA1), poi salva ed esci
        loop((s, v) -> true).gioca(stato, view, script(1, 3));
        assertEquals(atrio, stato.getPosizioneCorrente()); // il vincolo ha impedito lo spostamento
        assertTrue(view.messaggi.stream().anyMatch(m -> m.contains("non può accedere")));
    }

    @Test
    void esameSuperatoAccreditaCfuNelLibretto() {
        // vai in LA1 (0), sostieni l'esame (0); esame superato -> l'azione esame sparisce,
        // quindi in LA1 restano: 0) Vai a: Atrio - 1) Salva ed esci
        loop((s, v) -> true).gioca(stato, view, script(0, 0, 1));
        assertEquals(6, studente.getLibretto().getCfuOttenuti());
        assertTrue(studente.getLibretto().getEsamiSuperati().contains(esame1));
    }

    @Test
    void alRaggiungimentoDellaSogliaCfuScattaLaLaurea() {
        studente.getLibretto().aggiungiCFU(6); // già a metà percorso
        // vai in LA1 e supera l'esame: 6 + 6 = 12 = soglia -> laurea, il loop termina da solo
        loop((s, v) -> true).gioca(stato, view, script(0, 0));
        assertTrue(view.messaggi.stream().anyMatch(m -> m.contains("LAUREA")));
    }

    @Test
    void ilBurnoutDuranteLEsameTerminaLaPartita() {
        FaseEsame faseLetale = (s, v) -> {
            s.getStudente().subisciDanno(10_000); // lancia BurnoutException con HP a 0
            return false;
        };
        loop(faseLetale).gioca(stato, view, script(0, 0)); // vai in LA1, sostieni l'esame
        assertEquals(0, studente.getSaluteMentale());
        assertTrue(view.messaggi.stream().anyMatch(m -> m.contains("GAME OVER")));
    }

    @Test
    void ilRiposoInAulaStudioRecuperaSaluteMentale() {
        studente.subisciDanno(50); // HP: 120 -> 70
        // vai in Aula Studio (2), riposati (0), poi salva ed esci (2)
        loop((s, v) -> true).gioca(stato, view, script(2, 0, 2));
        assertEquals(90, studente.getSaluteMentale()); // 70 + 20 di riposo
    }

    @Test
    void costruttoreRifiutaParametriNonValidi() {
        EsameController controller = new EsameController(List.of());
        ControllerMovimentoStanze movimento = new ControllerMovimentoStanze(mappa);
        assertThrows(IllegalArgumentException.class,
                () -> new GameLoop(null, movimento, controller, persistence, 12));
        assertThrows(IllegalArgumentException.class,
                () -> new GameLoop(mappa, movimento, controller, persistence, 0));
    }
}
