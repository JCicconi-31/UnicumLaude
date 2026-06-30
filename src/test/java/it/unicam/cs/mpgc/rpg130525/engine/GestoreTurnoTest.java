package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.port.GameView;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link GestoreTurno}: gestione di un singolo turno dello
 * scontro orale (lo studente risponde, e se il professore non è KO contrattacca).
 * La View è sostituita da un test double che non fa nulla.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class GestoreTurnoTest {

    /** View "muta": non visualizza nulla, isola il test dall'interfaccia. */
    private static final GameView VIEW_MUTA = new GameView() {
        @Override public void mostraMessaggio(String messaggio) { }
        @Override public void aggiornaStatoGiocatore(StudenteDto studente) { }
    };

    private final GestoreTurno gestore = new GestoreTurno(new CalcolatoreDanno());

    private Studente nuovoStudente() {
        return new Studente("Mario", "Rossi", 10, 5, 100, 50, new StudenteFullTime()); // intel 10, resil 5, HP 120
    }

    @Test
    void unTurnoInfliggeDannoAlProfessoreEPoiSubisceIlContrattacco() {
        Studente s = nuovoStudente();
        Professore prof = new Professore("Anna", "Verdi", 4, 25); // difficoltà 4, HP 25

        gestore.eseguiTurno(s, prof, VIEW_MUTA);

        assertEquals(15, prof.getHpProfessore()); // 25 - 10
        assertEquals(119, s.getSaluteMentale());  // 120 - max(1, 4 - 5) = 120 - 1
    }

    @Test
    void seIlProfessoreVieneSconfittoNonContrattacca() {
        Studente s = nuovoStudente();
        Professore prof = new Professore("Anna", "Verdi", 4, 10); // HP 10, cade in un colpo

        gestore.eseguiTurno(s, prof, VIEW_MUTA);

        assertTrue(prof.isKO());
        assertEquals(120, s.getSaluteMentale()); // nessun contrattacco: HP intatti
    }

    @Test
    void ilContrattaccoLetaleProvocaBurnout() {
        Studente s = nuovoStudente();
        s.subisciDanno(119); // salute portata a 1
        Professore prof = new Professore("Anna", "Verdi", 50, 100); // sopravvive al colpo e ribatte forte

        assertThrows(BurnoutException.class, () -> gestore.eseguiTurno(s, prof, VIEW_MUTA));
    }

    @Test
    void parametriNulliVengonoRifiutati() {
        Studente s = nuovoStudente();
        Professore prof = new Professore("Anna", "Verdi", 4, 25);
        assertThrows(IllegalArgumentException.class, () -> gestore.eseguiTurno(null, prof, VIEW_MUTA));
        assertThrows(IllegalArgumentException.class, () -> gestore.eseguiTurno(s, null, VIEW_MUTA));
        assertThrows(IllegalArgumentException.class, () -> gestore.eseguiTurno(s, prof, null));
    }

    @Test
    void costruttoreRifiutaCalcolatoreNull() {
        assertThrows(IllegalArgumentException.class, () -> new GestoreTurno(null));
    }
}
