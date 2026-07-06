package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.port.GameView;
import it.unicam.cs.mpgc.rpg130525.port.ProfessoreDto;
import it.unicam.cs.mpgc.rpg130525.port.StanzaDto;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link ProvaOrale}: lo scontro a turni contro il Boss
 * (Professore) dell'aula corrente, verificando il decremento della Salute
 * Mentale, il Burnout e l'effetto dei modificatori da equipaggiamento. La View
 * è sostituita da un test double che non fa nulla.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class ProvaOraleTest {

    private static final GameView VIEW_MUTA = new GameView() {
        @Override public void mostraMessaggio(String messaggio) { }
        @Override public void aggiornaStatoGiocatore(StudenteDto studente) { }
        @Override public void aggiornaStatoProfessore(ProfessoreDto professoreDto) { }
        @Override public void aggiornaMappa(List<StanzaDto> stanze, String posizioneCorrente) { }
    };

    private final ProvaOrale provaOrale = new ProvaOrale(new GestoreTurno(new CalcolatoreDanno()));

    private Studente nuovoStudente() {
        return new Studente("Mario", "Rossi", 10, 5, 100, 50, new StudenteFullTime()); // intel 10, resil 5, HP 120
    }

    /** Costruisce uno stato con lo studente in un'aula d'esame governata dal professore dato. */
    private StatoGioco statoConProfessore(Studente studente, Professore prof) {
        Esame esame = new Esame(1, "Metodologie di Programmazione", 6, prof);
        Stanza aula = new Stanza("LA1", TipoStanza.AULA_ESAME, esame);
        return new StatoGioco(studente, aula);
    }

    private static Equipaggiamento equip(TipoItem tipo, Map<Stat, Integer> modificatori) {
        return new Equipaggiamento() {
            @Override public Map<Stat, Integer> getModificatori() { return modificatori; }
            @Override public String getNome() { return tipo.name(); }
            @Override public TipoItem getTipo() { return tipo; }
        };
    }

    @Test
    void professoreSconfittoRestituisceTrue() {
        Studente s = nuovoStudente();
        Professore prof = new Professore("Anna", "Verdi", 2, 30); // difficoltà 2, HP 30 -> 3 colpi da 10
        StatoGioco stato = statoConProfessore(s, prof);

        assertTrue(provaOrale.esegui(stato, VIEW_MUTA));
        assertTrue(prof.isKO());
        assertEquals(118, s.getSaluteMentale()); // due contrattacchi da 1 (l'ultimo turno è KO, niente contrattacco)
    }

    @Test
    void seLoStudenteEsaurisceLaSaluteSiPropagaBurnout() {
        Studente s = nuovoStudente();
        Professore prof = new Professore("Anna", "Verdi", 50, 1000); // troppo resistente, contrattacchi devastanti
        StatoGioco stato = statoConProfessore(s, prof);

        assertThrows(BurnoutException.class, () -> provaOrale.esegui(stato, VIEW_MUTA));
    }

    @Test
    void unEquipaggiamentoRiduceITurniEQuindiIlDannoSubito() {
        // Stesso professore per entrambi (istanze separate perché mutabili).
        Studente base = nuovoStudente();
        Studente potenziato = nuovoStudente();
        potenziato.equipaggiaItem(equip(TipoItem.Libro, Map.of(Stat.Intelligenza, 5))); // intel 10 -> 15

        provaOrale.esegui(statoConProfessore(base, new Professore("Anna", "Verdi", 3, 30)), VIEW_MUTA);
        provaOrale.esegui(statoConProfessore(potenziato, new Professore("Anna", "Verdi", 3, 30)), VIEW_MUTA);

        // Lo studente potenziato vince in meno turni, quindi subisce meno contrattacchi.
        assertTrue(potenziato.getSaluteMentale() > base.getSaluteMentale());
    }

    @Test
    void unAulaSenzaEsameLanciaIllegalState() {
        Studente s = nuovoStudente();
        Stanza aulaStudio = new Stanza("Studio", TipoStanza.AULA_STUDIO, null);
        StatoGioco stato = new StatoGioco(s, aulaStudio);

        assertThrows(IllegalStateException.class, () -> provaOrale.esegui(stato, VIEW_MUTA));
    }

    @Test
    void parametriNulliVengonoRifiutati() {
        Studente s = nuovoStudente();
        StatoGioco stato = statoConProfessore(s, new Professore("Anna", "Verdi", 2, 30));
        assertThrows(IllegalArgumentException.class, () -> provaOrale.esegui(null, VIEW_MUTA));
        assertThrows(IllegalArgumentException.class, () -> provaOrale.esegui(stato, null));
    }

    @Test
    void costruttoreRifiutaGestoreNull() {
        assertThrows(IllegalArgumentException.class, () -> new ProvaOrale(null));
    }
}
