package it.unicam.cs.mpgc.rpg130525.persistence;

import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di round-trip per {@link JsonGameStatePersistence}: salvataggio su file
 * JSON e successivo caricamento, verificando che lo stato dinamico venga
 * ripristinato e che stanza ed esame vengano riagganciati alle istanze della
 * Mappa. Si usa una cartella temporanea per non toccare il file system reale.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class JsonGameStatePersistenceTest {

    @TempDir
    Path cartellaTemporanea;

    private Mappa mappa;
    private Esame esame1;
    private Stanza aula2;

    @BeforeEach
    void setUp() {
        Professore prof = new Professore("Anna", "Verdi", 5, 50);
        esame1 = new Esame(1, "Programmazione", 6, prof);
        Esame esame2 = new Esame(2, "Algoritmi", 9, prof);

        Stanza atrio = new Stanza("Atrio", TipoStanza.CORRIDOIO, null);
        Stanza aula1 = new Stanza("LA1", TipoStanza.AULA_ESAME, esame1);
        aula2 = new Stanza("LA2", TipoStanza.AULA_ESAME, esame2);

        mappa = new Mappa();
        mappa.addStanza(atrio);
        mappa.addStanza(aula1);
        mappa.addStanza(aula2);
        mappa.addCorridoio(atrio, aula1);
        mappa.addCorridoio(atrio, aula2);
        mappa.addPropedeuticita(aula1, aula2);
    }

    /**
     * Studente Lavoratore (HP -10, monete +20) con un esame superato e HP ridotti, in LA2.
     */
    private StatoGioco statoConProgresso() {
        Studente s = new Studente("Mario", "Rossi", 10, 5, 100, 50, new StudenteLavoratore());
        // HP max = 100 - 10 = 90 ; monete = 50 + 20 = 70
        s.subisciDanno(30); // HP attuale 60
        s.getLibretto().addEsameSuperato(new EsameSuperato(esame1, 28)); // 6 CFU
        return new StatoGioco(s, aula2);
    }

    private Path fileSalvataggio() {
        return cartellaTemporanea.resolve("partita.json");
    }

    @Test
    void salvaCreaUnFileJson() {
        JsonGameStatePersistence persistence = new JsonGameStatePersistence(fileSalvataggio(), mappa);
        persistence.salva(statoConProgresso());
        assertTrue(Files.exists(fileSalvataggio()));
    }

    @Test
    void roundTripRipristinaLoStatoDinamico() {
        JsonGameStatePersistence persistence = new JsonGameStatePersistence(fileSalvataggio(), mappa);
        persistence.salva(statoConProgresso());

        Studente caricato = persistence.carica().getStudente();

        assertEquals("Mario Rossi", caricato.getNomeCompleto());
        assertEquals(60, caricato.getSaluteMentale());       // HP attuale ripristinato
        assertEquals(90, caricato.getSaluteMentaleMax());    // modificatore di carriera NON raddoppiato
        assertEquals(70, caricato.getMonete());
        assertEquals(10, caricato.getIntelligenzaEffettiva());
        assertEquals(5, caricato.getResilienzaEffettiva());
    }

    @Test
    void roundTripRipristinaLibrettoEProgressione() {
        JsonGameStatePersistence persistence = new JsonGameStatePersistence(fileSalvataggio(), mappa);
        persistence.salva(statoConProgresso());

        Studente caricato = persistence.carica().getStudente();

        assertEquals(6, caricato.getLibretto().getCfuOttenuti());
        assertTrue(caricato.getLibretto().getEsamiSuperati().contains(esame1));
    }

    @Test
    void roundTripRipristinaInventarioEquipaggiatoEZaino() {
        Studente s = new Studente("Mario", "Rossi", 10, 5, 100, 50, new StudenteFullTime());
        s.equipaggiaItem((Equipaggiamento) CatalogoItem.crea(TipoItem.LIBRO)); // +3 INTELLIGENZA
        s.aggiungiConsumabile((Consumabile) CatalogoItem.crea(TipoItem.CAFFE));
        s.aggiungiConsumabile((Consumabile) CatalogoItem.crea(TipoItem.CAFFE));
        StatoGioco stato = new StatoGioco(s, aula2);

        JsonGameStatePersistence persistence = new JsonGameStatePersistence(fileSalvataggio(), mappa);
        persistence.salva(stato);

        Studente caricato = persistence.carica().getStudente();

        assertEquals(13, caricato.getIntelligenzaEffettiva()); // 10 base + 3 del LIBRO equipaggiato
        assertEquals(2, caricato.getZaino().size());           // i due CAFFE ripristinati nello zaino
    }

    @Test
    void caricaRiaggancioStanzaEdEsameAlleIstanzeDellaMappa() {
        JsonGameStatePersistence persistence = new JsonGameStatePersistence(fileSalvataggio(), mappa);
        persistence.salva(statoConProgresso());

        StatoGioco caricato = persistence.carica();

        // la posizione è la STESSA istanza presente nella mappa, non una copia
        assertSame(aula2, caricato.getPosizioneCorrente());
        // anche l'esame nel libretto è riagganciato all'istanza della mappa
        EsameSuperato superato = caricato.getStudente().getLibretto().getDettaglioEsami().iterator().next();
        assertSame(esame1, superato.esame());
    }

    @Test
    void caricaSuFileInesistenteLanciaPersistenceException() {
        Path inesistente = cartellaTemporanea.resolve("nessun-salvataggio.json");
        JsonGameStatePersistence persistence = new JsonGameStatePersistence(inesistente, mappa);
        assertThrows(PersistenceException.class, persistence::carica);
    }

    @Test
    void costruttoreRifiutaParametriNull() {
        assertThrows(IllegalArgumentException.class, () -> new JsonGameStatePersistence(null, mappa));
        assertThrows(IllegalArgumentException.class, () -> new JsonGameStatePersistence(fileSalvataggio(), null));
    }
}
