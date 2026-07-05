package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link CalcolatoreDanno}: danno inflitto dallo studente,
 * danno subito dai boss (Professori) e dai nemici minori, con la mitigazione
 * della Resilienza.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class CalcolatoreDannoTest {

    private final CalcolatoreDanno calcolatore = new CalcolatoreDanno();

    /** Studente con intelligenza 10 e resilienza 5. */
    private Studente nuovoStudente() {
        return new Studente("Mario", "Rossi", 10, 5, 100, 50, new StudenteFullTime());
    }

    private static Equipaggiamento equip(TipoItem tipo, Map<Stat, Integer> modificatori) {
        return new Equipaggiamento() {
            @Override public Map<Stat, Integer> getModificatori() { return modificatori; }
            @Override public String getNome() { return tipo.name(); }
            @Override public TipoItem getTipo() { return tipo; }
        };
    }

    @Test
    void dannoInflittoEUgualeAllIntelligenzaEffettiva() {
        assertEquals(10, calcolatore.dannoInflitto(nuovoStudente()));
    }

    @Test
    void unEquipaggiamentoAumentaIlDannoInflitto() {
        Studente s = nuovoStudente();
        s.equipaggiaItem(equip(TipoItem.Libro, Map.of(Stat.Intelligenza, 5)));
        assertEquals(15, calcolatore.dannoInflitto(s));
    }

    @Test
    void dannoSubitoDalProfessoreEMitigatoDallaResilienza() {
        Professore prof = new Professore("Anna", "Verdi", 8, 50); // difficoltà 8
        assertEquals(3, calcolatore.dannoSubito(prof, nuovoStudente())); // 8 - 5
    }

    @Test
    void dannoSubitoDalProfessoreNonScendeSottoUno() {
        Professore prof = new Professore("Anna", "Verdi", 3, 50); // difficoltà 3 < resilienza 5
        assertEquals(1, calcolatore.dannoSubito(prof, nuovoStudente()));
    }

    @Test
    void unoStudenteInBurnoutNonSubisceUlterioreDanno() {
        Studente s = nuovoStudente();
        assertThrows(BurnoutException.class, () -> s.subisciDanno(1000)); // salute portata a 0
        Professore prof = new Professore("Anna", "Verdi", 8, 50);
        assertEquals(0, calcolatore.dannoSubito(prof, s));
    }

    @Test
    void parametriNulliVengonoRifiutati() {
        Studente s = nuovoStudente();
        Professore prof = new Professore("Anna", "Verdi", 8, 50);
        assertThrows(IllegalArgumentException.class, () -> calcolatore.dannoInflitto(null));
        assertThrows(IllegalArgumentException.class, () -> calcolatore.dannoSubito(prof, null));
        assertThrows(IllegalArgumentException.class, () -> calcolatore.dannoSubito(null, s));
    }
}
