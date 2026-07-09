package it.unicam.cs.mpgc.rpg130525.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link Mappa}, in particolare i vincoli di propedeuticità.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class MappaTest {

    private Esame esame(int codice) {
        return new Esame(codice, "Corso " + codice, 6, new Professore("Prof", "Esempio", 5, 50));
    }

    private Stanza aulaEsame(Esame esame) {
        return new Stanza("Aula " + esame.getCodiceCorso(), TipoStanza.AULA_ESAME, esame);
    }

    @Test
    void stanzaSenzaPrerequisitiEsempreDisponibile() {
        Mappa mappa = new Mappa();
        Stanza prog = aulaEsame(esame(1));
        mappa.addStanza(prog);
        assertTrue(mappa.isDisponibile(prog, Set.of()));
    }

    @Test
    void stanzaConPrerequisitoNonSoddisfattoNonEDisponibile() {
        Mappa mappa = new Mappa();
        Esame esameProg = esame(1);
        Stanza prog = aulaEsame(esameProg);
        Stanza mdp = aulaEsame(esame(2));
        mappa.addStanza(prog);
        mappa.addStanza(mdp);
        mappa.addPropedeuticita(prog, mdp); // prog è prerequisito di mdp

        assertFalse(mappa.isDisponibile(mdp, Set.of()));
    }

    @Test
    void stanzaDiventaDisponibileQuandoIlPrerequisitoESuperato() {
        Mappa mappa = new Mappa();
        Esame esameProg = esame(1);
        Stanza prog = aulaEsame(esameProg);
        Stanza mdp = aulaEsame(esame(2));
        mappa.addStanza(prog);
        mappa.addStanza(mdp);
        mappa.addPropedeuticita(prog, mdp);

        assertTrue(mappa.isDisponibile(mdp, Set.of(esameProg)));
    }

    @Test
    void getAdiacentiDiUnaStanzaNuovaEVuoto() {
        Mappa mappa = new Mappa();
        Stanza prog = aulaEsame(esame(1));
        mappa.addStanza(prog);
        assertTrue(mappa.getAdiacenti(prog).isEmpty());
    }

    @Test
    void getAdiacentiEImmutabile() {
        Mappa mappa = new Mappa();
        Stanza prog = aulaEsame(esame(1));
        mappa.addStanza(prog);
        assertThrows(UnsupportedOperationException.class,
                () -> mappa.getAdiacenti(prog).add(prog));
    }

    @Test
    void getStanzaTrovaLaStanzaPerNome() {
        Mappa mappa = new Mappa();
        Stanza prog = aulaEsame(esame(1)); // nome "Aula 1"
        mappa.addStanza(prog);
        assertTrue(mappa.getStanza("Aula 1").isPresent());
        assertSame(prog, mappa.getStanza("Aula 1").get());
    }

    @Test
    void getStanzaAssenteRestituisceOptionalVuoto() {
        Mappa mappa = new Mappa();
        mappa.addStanza(aulaEsame(esame(1)));
        assertTrue(mappa.getStanza("Inesistente").isEmpty());
    }

    @Test
    void cfuTotaliSommaSoloLeAuleConEsame() {
        Mappa mappa = new Mappa();
        mappa.addStanza(new Stanza("Atrio", TipoStanza.CORRIDOIO, null)); // nessun esame: ignorata
        mappa.addStanza(aulaEsame(esame(1)));                             // 6 CFU
        mappa.addStanza(aulaEsame(esame(2)));                             // 6 CFU
        assertEquals(12, mappa.cfuTotali());
    }

    @Test
    void cfuTotaliDiUnaMappaVuotaEZero() {
        assertEquals(0, new Mappa().cfuTotali());
    }
}
