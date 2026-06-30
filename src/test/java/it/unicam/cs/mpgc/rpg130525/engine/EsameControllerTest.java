package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.port.GameView;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link EsameController}: orchestrazione delle fasi d'esame in
 * sequenza, interruzione al primo fallimento, gestione centralizzata del
 * Burnout e registrazione dell'esame nel libretto a esame superato. Le fasi
 * sono sostituite da test double (lambda) per isolare l'orchestratore.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class EsameControllerTest {

    /** View che registra i messaggi mostrati, per verificare la notifica del Burnout. */
    private static final class RecordingView implements GameView {
        final List<String> messaggi = new ArrayList<>();
        @Override public void mostraMessaggio(String messaggio) { messaggi.add(messaggio); }
        @Override public void aggiornaStatoGiocatore(StudenteDto studente) { }
    }

    private static final GameView VIEW_MUTA = new RecordingView();

    private Esame esame;
    private StatoGioco stato;

    @BeforeEach
    void setUp() {
        Professore prof = new Professore("Anna", "Verdi", 5, 50);
        esame = new Esame(1, "Programmazione", 6, prof); // 6 CFU
        Stanza aula = new Stanza("LA1", TipoStanza.AULA_ESAME, esame);
        Studente studente = new Studente("Mario", "Rossi", 10, 5, 100, 50, new StudenteFullTime());
        stato = new StatoGioco(studente, aula);
    }

    private Libretto libretto() {
        return stato.getStudente().getLibretto();
    }

    @Test
    void tutteLeFasiSuperateRegistraLEsameERitornaTrue() {
        EsameController controller = new EsameController(List.of(
                (s, v) -> true,
                (s, v) -> true));

        assertTrue(controller.sostieniEsame(stato, VIEW_MUTA));
        assertTrue(libretto().getEsamiSuperati().contains(esame));
        assertEquals(6, libretto().getCfuOttenuti()); // CFU accreditati
    }

    @Test
    void unaFaseFallitaInterrompeLaSequenzaSenzaRegistrare() {
        boolean[] terzaEseguita = {false};
        EsameController controller = new EsameController(List.of(
                (s, v) -> true,
                (s, v) -> false, // fallisce qui
                (s, v) -> { terzaEseguita[0] = true; return true; }));

        assertFalse(controller.sostieniEsame(stato, VIEW_MUTA));
        assertFalse(terzaEseguita[0]); // short-circuit: la fase dopo il fallimento non parte
        assertEquals(0, libretto().getCfuOttenuti()); // esame non registrato
        assertTrue(libretto().getEsamiSuperati().isEmpty());
    }

    @Test
    void leFasiVengonoEseguiteNellOrdineDellaLista() {
        List<String> log = new ArrayList<>();
        EsameController controller = new EsameController(List.of(
                (s, v) -> { log.add("scritta"); return true; },
                (s, v) -> { log.add("orale"); return true; }));

        controller.sostieniEsame(stato, VIEW_MUTA);
        assertEquals(List.of("scritta", "orale"), log);
    }

    @Test
    void ilBurnoutVieneCatturatoNotificatoERitornaFalse() {
        RecordingView view = new RecordingView();
        EsameController controller = new EsameController(List.of(
                (s, v) -> { throw new BurnoutException("energie esaurite"); }));

        assertFalse(controller.sostieniEsame(stato, view));
        assertEquals(0, libretto().getCfuOttenuti()); // nessuna registrazione dopo il burnout
        assertTrue(view.messaggi.stream().anyMatch(m -> m.contains("Burnout")));
    }

    @Test
    void costruttoreRifiutaListaNull() {
        assertThrows(IllegalArgumentException.class, () -> new EsameController(null));
    }
}
