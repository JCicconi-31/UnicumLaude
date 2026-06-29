package it.unicam.cs.mpgc.rpg130525.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link Libretto}.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class LibrettoTest {

    private Esame esame(int codice, int cfu) {
        return new Esame(codice, "Corso " + codice, cfu, new Professore("Prof", "Esempio", 5, 50));
    }

    @Test
    void addEsameSuperatoAccumulaICfu() {
        Libretto libretto = new Libretto();
        libretto.addEsameSuperato(new EsameSuperato(esame(1, 6), 28));
        libretto.addEsameSuperato(new EsameSuperato(esame(2, 9), 30));
        assertEquals(15, libretto.getCfuOttenuti());
    }

    @Test
    void esameDuplicatoNonVieneRegistratoNeConteggiato() {
        Libretto libretto = new Libretto();
        assertTrue(libretto.addEsameSuperato(new EsameSuperato(esame(1, 6), 28)));
        // stesso esame (stesso codice), non importa il voto: deve essere rifiutato
        assertFalse(libretto.addEsameSuperato(new EsameSuperato(esame(1, 6), 31)));
        assertEquals(6, libretto.getCfuOttenuti());
        assertEquals(1, libretto.getEsamiSuperati().size());
    }

    @Test
    void getEsamiSuperatiContieneGliEsamiAggiunti() {
        Libretto libretto = new Libretto();
        Esame e1 = esame(1, 6);
        Esame e2 = esame(2, 9);
        libretto.addEsameSuperato(new EsameSuperato(e1, 28));
        libretto.addEsameSuperato(new EsameSuperato(e2, 24));
        assertEquals(Set.of(e1, e2), libretto.getEsamiSuperati());
    }

    @Test
    void getEsamiSuperatiEImmutabile() {
        Libretto libretto = new Libretto();
        assertThrows(UnsupportedOperationException.class,
                () -> libretto.getEsamiSuperati().add(esame(99, 6)));
    }

    @Test
    void getDettaglioEsamiEImmutabile() {
        Libretto libretto = new Libretto();
        libretto.addEsameSuperato(new EsameSuperato(esame(1, 6), 28));
        assertThrows(UnsupportedOperationException.class,
                () -> libretto.getDettaglioEsami().clear());
    }

    @Test
    void addEsameSuperatoRifiutaNull() {
        Libretto libretto = new Libretto();
        assertThrows(IllegalArgumentException.class, () -> libretto.addEsameSuperato(null));
    }
}
