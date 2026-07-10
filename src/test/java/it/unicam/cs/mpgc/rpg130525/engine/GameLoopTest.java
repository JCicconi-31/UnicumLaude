package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link GameLoop}: movimento con vincolo di propedeuticità,
 * esame con accredito CFU e vittoria, Burnout con game over, riposo, negozio
 * (acquisto consumabili ed equipaggiamenti, con controllo delle monete), uso
 * dei consumabili e salvataggio all'uscita. View, input e persistenza sono
 * test double: l'input è una sequenza prefissata di scelte, la persistenza
 * registra le chiamate in memoria.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class GameLoopTest {

    /**
     * View che registra i messaggi, per verificare vittoria/burnout/vincoli.
     */
    private static final class RecordingView implements GameView {
        final List<String> messaggi = new ArrayList<>();
        List<StanzaDto> ultimaMappa;
        String ultimaPosizione;

        @Override
        public void mostraMessaggio(String messaggio) {
            messaggi.add(messaggio);
        }

        @Override
        public void aggiornaStatoGiocatore(StudenteDto studente) {
        }

        @Override
        public void aggiornaStatoProfessore(ProfessoreDto professoreDto) {
        }

        @Override
        public void aggiornaMappa(List<StanzaDto> stanze, String posizioneCorrente) {
            ultimaMappa = stanze;
            ultimaPosizione = posizioneCorrente;
        }
    }

    /**
     * Estrae dallo snapshot della mappa lo stato della stanza col nome dato.
     */
    private static StanzaDto.Stato statoDi(List<StanzaDto> stanze, String nome) {
        return stanze.stream()
                .filter(s -> s.nome().equals(nome))
                .findFirst().orElseThrow()
                .stato();
    }

    /**
     * Input che restituisce una sequenza prefissata di scelte.
     */
    private static GameInput script(int... scelte) {
        Deque<Integer> coda = new ArrayDeque<>();
        for (int s : scelte) coda.add(s);
        return new GameInput() {
            @Override
            public int chiediRisposta(Domanda domanda) {
                return coda.remove();
            }

            @Override
            public int scegli(String titolo, List<String> opzioni) {
                return coda.remove();
            }

            @Override
            public String chiediTesto(String prompt) {
                return "test";
            }
        };
    }

    /**
     * Persistenza in memoria: registra il salvataggio senza toccare il disco.
     */
    private static final class PersistenzaInMemoria implements PersistenceManager {
        StatoGioco salvato;

        @Override
        public void salva(StatoGioco stato) {
            salvato = stato;
        }

        @Override
        public StatoGioco carica() {
            return salvato;
        }

        @Override
        public boolean esisteSalvataggio() {
            return salvato != null;
        }
    }

    private Mappa mappa;
    private Stanza atrio;
    private Stanza la1;
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
        Stanza aulaStudio = new Stanza("Aula Studio", TipoStanza.AULA_STUDIO, null);
        la1 = new Stanza("Aula LA1", TipoStanza.AULA_ESAME, esame1);
        Stanza la2 = new Stanza("Aula LA2", TipoStanza.AULA_ESAME, esame2);

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
     * GameLoop con fasi d'esame stub.
     * <p>Azioni nell'Atrio (corridoio): 0) Vai a: Aula LA1 - 1) Vai a: Aula LA2 -
     * 2) Vai a: Aula Studio - 3) Salva ed esci.
     * <p>Azioni in Aula Studio: 0) Riposati - 1) Compra CAFFE - 2) Compra APPUNTI_LEZIONE -
     * 3) Compra LIBRO - 4) Compra CHATGPT - [5) Usa &lt;item&gt; se lo zaino non è vuoto] -
     * poi Vai a: Atrio - Salva ed esci.
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
        // già a metà percorso: 6 CFU da un esame pregresso, tramite l'API pubblica (non la vecchia backdoor)
        studente.getLibretto().addEsameSuperato(
                new EsameSuperato(new Esame(99, "Corso Pregresso", 6, new Professore("Test", "Prof", 4, 30)), 28));
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
        // vai in Aula Studio (2), riposati (0), poi salva ed esci (6)
        loop((s, v) -> true).gioca(stato, view, script(2, 0, 6));
        assertEquals(90, studente.getSaluteMentale()); // 70 + 20 di riposo
    }

    @Test
    void acquistoConsumabileScalaLeMoneteERiempieLoZaino() {
        // Aula Studio (2), Compra CAFFE (1, costo 10); ora lo zaino ha un item e
        // compare "Usa CAFFE" (5), quindi "Salva ed esci" scala all'indice 7
        loop((s, v) -> true).gioca(stato, view, script(2, 1, 7));
        assertEquals(40, studente.getMonete()); // 50 - 10
        assertEquals(1, studente.getZaino().size());
        assertEquals("CAFFE", studente.getZaino().get(0).getNome());
    }

    @Test
    void acquistoEquipaggiamentoAumentaLeStatisticheEffettive() {
        // Aula Studio (2), Compra LIBRO (3, costo 25, +3 INTELLIGENZA), Salva ed esci (6)
        loop((s, v) -> true).gioca(stato, view, script(2, 3, 6));
        assertEquals(25, studente.getMonete()); // 50 - 25
        assertEquals(13, studente.getIntelligenzaEffettiva()); // 10 base + 3 LIBRO
    }

    @Test
    void acquistoConMoneteInsufficientiVieneRifiutato() {
        studente.spendiMonete(45); // restano 5 monete
        // Aula Studio (2), prova a Comprare CAFFE (1, costo 10 > 5), Salva ed esci (6)
        loop((s, v) -> true).gioca(stato, view, script(2, 1, 6));
        assertEquals(5, studente.getMonete());        // nessun addebito
        assertTrue(studente.getZaino().isEmpty());
        assertTrue(view.messaggi.stream().anyMatch(m -> m.contains("insufficienti")));
    }

    @Test
    void usoConsumabileRecuperaSaluteMentaleELoRimuoveDalloZaino() {
        studente.subisciDanno(50); // HP: 120 -> 70
        // Aula Studio (2), Compra CAFFE (1); ora compare "Usa CAFFE" all'indice 5;
        // usa CAFFE (5, +20 HP) -> lo zaino torna vuoto, quindi "Salva ed esci" torna a 6
        loop((s, v) -> true).gioca(stato, view, script(2, 1, 5, 6));
        assertEquals(90, studente.getSaluteMentale()); // 70 + 20 del caffè
        assertTrue(studente.getZaino().isEmpty());     // consumato
    }

    @Test
    void laMappaNotificataRifletteGliStatiDerivati() {
        // salva ed esci subito: la view ha comunque ricevuto lo snapshot iniziale
        loop((s, v) -> true).gioca(stato, view, script(3));

        assertEquals("Atrio", view.ultimaPosizione);
        assertEquals(StanzaDto.Stato.DISPONIBILE, statoDi(view.ultimaMappa, "Aula LA1"));
        assertEquals(StanzaDto.Stato.BLOCCATA, statoDi(view.ultimaMappa, "Aula LA2")); // prerequisito LA1
        assertEquals(StanzaDto.Stato.DISPONIBILE, statoDi(view.ultimaMappa, "Aula Studio"));
    }

    @Test
    void dopoLEsameLaMappaMostraSuperataESbloccaLaSuccessiva() {
        // vai in LA1 (0), supera l'esame (0), poi salva ed esci (1)
        loop((s, v) -> true).gioca(stato, view, script(0, 0, 1));

        assertEquals("Aula LA1", view.ultimaPosizione);
        assertEquals(StanzaDto.Stato.SUPERATA, statoDi(view.ultimaMappa, "Aula LA1"));
        assertEquals(StanzaDto.Stato.DISPONIBILE, statoDi(view.ultimaMappa, "Aula LA2")); // sbloccata
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
