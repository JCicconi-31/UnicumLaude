package it.unicam.cs.mpgc.rpg130525.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link Studente}, in particolare il calcolo delle statistiche
 * effettive a partire dai modificatori degli equipaggiamenti.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class StudenteTest {

    /** Equipaggiamento fittizio per i test: un tipo e una mappa di modificatori. */
    private static Equipaggiamento equip(TipoItem tipo, Map<Stat, Integer> modificatori) {
        return new Equipaggiamento() {
            @Override public Map<Stat, Integer> getModificatori() { return modificatori; }
            @Override public String getNome() { return tipo.name(); }
            @Override public TipoItem getTipo() { return tipo; }
        };
    }

    private Studente nuovoStudente() {
        // intelligenza 10, resilienza 5
        return new Studente("Mario", "Rossi", 10, 5, 100, 50, new StudenteFullTime());
    }

    @Test
    void senzaEquipaggiamentoLeStatEffettiveSonoQuelleBase() {
        Studente s = nuovoStudente();
        assertEquals(10, s.getIntelligenzaEffettiva());
        assertEquals(5, s.getResilienzaEffettiva());
    }

    @Test
    void unEquipaggiamentoAumentaLIntelligenzaEffettiva() {
        Studente s = nuovoStudente();
        s.equipaggiaItem(equip(TipoItem.LIBRO, Map.of(Stat.INTELLIGENZA, 5)));
        assertEquals(15, s.getIntelligenzaEffettiva());
        assertEquals(5, s.getResilienzaEffettiva());
    }

    @Test
    void unEquipaggiamentoPuoAvereUnModificatoreNegativo() {
        Studente s = nuovoStudente();
        // tipo CHATGPT: +8 intelligenza ma -4 resilienza
        s.equipaggiaItem(equip(TipoItem.CHATGPT, Map.of(Stat.INTELLIGENZA, 8, Stat.RESILIENZA, -4)));
        assertEquals(18, s.getIntelligenzaEffettiva());
        assertEquals(1, s.getResilienzaEffettiva());
    }

    @Test
    void piuEquipaggiamentiDiTipoDiversoSommanoIModificatori() {
        Studente s = nuovoStudente();
        s.equipaggiaItem(equip(TipoItem.LIBRO, Map.of(Stat.INTELLIGENZA, 5)));
        s.equipaggiaItem(equip(TipoItem.CHATGPT, Map.of(Stat.INTELLIGENZA, 8, Stat.RESILIENZA, -4)));
        assertEquals(23, s.getIntelligenzaEffettiva()); // 10 + 5 + 8
        assertEquals(1, s.getResilienzaEffettiva());    // 5 - 4
    }

    @Test
    void equipaggiareLoStessoTipoSostituisceLoSlotPrecedente() {
        Studente s = nuovoStudente();
        s.equipaggiaItem(equip(TipoItem.LIBRO, Map.of(Stat.INTELLIGENZA, 5)));
        s.equipaggiaItem(equip(TipoItem.LIBRO, Map.of(Stat.INTELLIGENZA, 3))); // stesso tipo
        assertEquals(13, s.getIntelligenzaEffettiva()); // 10 + 3, non 10 + 5 + 3
    }

    @Test
    void subisciDannoRiduceLaSaluteMentale() {
        Studente s = nuovoStudente(); // HP max = 100 + 20 (full-time) = 120
        s.subisciDanno(30);
        assertEquals(90, s.getSaluteMentale());
    }

    @Test
    void dannoLetaleProvocaBurnout() {
        Studente s = nuovoStudente();
        assertThrows(BurnoutException.class, () -> s.subisciDanno(1000));
    }

    @Test
    void recuperaHpNonSuperaIlMassimo() {
        Studente s = nuovoStudente(); // HP = 120
        s.subisciDanno(50);           // HP = 70
        s.recuperaHP(1000);
        assertEquals(120, s.getSaluteMentale());
    }
}
