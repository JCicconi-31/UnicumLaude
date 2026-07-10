package it.unicam.cs.mpgc.rpg130525.persistence;

import it.unicam.cs.mpgc.rpg130525.model.Esame;
import it.unicam.cs.mpgc.rpg130525.model.Mappa;
import it.unicam.cs.mpgc.rpg130525.model.Professore;
import it.unicam.cs.mpgc.rpg130525.model.Stanza;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per {@link JsonLoaderMondo}: verifica che una configurazione JSON venga
 * deserializzata in una {@link Mappa} del dominio coerente — numero di stanze,
 * adiacenze bidirezionali, propedeuticità e mapping di esame/professore — e che
 * ogni sorgente illeggibile o incoerente venga incapsulata in
 * {@link PersistenceException}. La sorgente è un {@link ByteArrayInputStream}
 * costruito da una stringa, così il test resta indipendente dal file system.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class JsonLoaderMondoTest {

    /**
     * Mappa di prova a tre stanze. Le adiacenze sono dichiarate volutamente su un
     * solo lato (Atrio -> LA1, LA1 -> LA2) per verificare che il corridoio venga
     * ricostruito comunque bidirezionale. LA2 è propedeutica a LA1.
     */
    private static final String MAPPA_JSON = """
            {
              "stanze": [
                { "nome": "Atrio", "tipoStanza": "CORRIDOIO", "esame": null,
                  "adiacenze": ["Aula LA1"], "propedeuticita": [] },
                { "nome": "Aula LA1", "tipoStanza": "AULA_ESAME",
                  "esame": { "codiceCorso": 1, "nome": "Programmazione", "cfu": 6,
                             "professore": { "nome": "Ada", "cognome": "Lovelace", "hp": 25, "attacco": 3 } },
                  "adiacenze": ["Aula LA2"], "propedeuticita": [] },
                { "nome": "Aula LA2", "tipoStanza": "AULA_ESAME",
                  "esame": { "codiceCorso": 2, "nome": "Metodologie", "cfu": 9,
                             "professore": { "nome": "Alan", "cognome": "Turing", "hp": 30, "attacco": 4 } },
                  "adiacenze": [], "propedeuticita": ["Aula LA1"] }
              ]
            }
            """;

    private InputStream sorgente(String json) {
        return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    }

    private Stanza stanza(Mappa mappa, String nome) {
        return mappa.getStanze().stream()
                .filter(s -> s.getNome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new AssertionError("stanza assente nella mappa: " + nome));
    }

    @Test
    void caricaTutteLeStanze() {
        Mappa mappa = new JsonLoaderMondo(sorgente(MAPPA_JSON)).caricaMappa();
        assertEquals(3, mappa.getStanze().size());
    }

    @Test
    void adiacenzaRicostruitaBidirezionale() {
        Mappa mappa = new JsonLoaderMondo(sorgente(MAPPA_JSON)).caricaMappa();
        Stanza atrio = stanza(mappa, "Atrio");
        Stanza la1 = stanza(mappa, "Aula LA1");
        Stanza la2 = stanza(mappa, "Aula LA2");
        // dichiarate su un solo lato nel JSON, ma i corridoi sono bidirezionali
        assertTrue(mappa.getAdiacenti(atrio).contains(la1));
        assertTrue(mappa.getAdiacenti(la1).contains(atrio));
        assertTrue(mappa.getAdiacenti(la1).contains(la2));
        assertTrue(mappa.getAdiacenti(la2).contains(la1));
    }

    @Test
    void propedeuticitaRicostruita() {
        Mappa mappa = new JsonLoaderMondo(sorgente(MAPPA_JSON)).caricaMappa();
        Stanza la1 = stanza(mappa, "Aula LA1");
        Stanza la2 = stanza(mappa, "Aula LA2");
        Esame esameLA1 = la1.getEsame();
        // LA2 richiede l'esame di LA1: bloccata senza, disponibile una volta superato
        assertFalse(mappa.isDisponibile(la2, Set.of()));
        assertTrue(mappa.isDisponibile(la2, Set.of(esameLA1)));
    }

    @Test
    void esameEProfessoreMappatiCorrettamente() {
        Mappa mappa = new JsonLoaderMondo(sorgente(MAPPA_JSON)).caricaMappa();
        Esame esame = stanza(mappa, "Aula LA1").getEsame();
        assertEquals(1, esame.codiceCorso());
        assertEquals("Programmazione", esame.nomeCorso());
        assertEquals(6, esame.cfuAssociati());
        Professore prof = esame.professore();
        assertEquals("Ada Lovelace", prof.getNomeCompleto());
        assertEquals(3, prof.getDifficolta()); // 'attacco' nel JSON -> difficolta nel dominio
        assertEquals(25, prof.getHpMax());      // 'hp' nel JSON -> hpMax nel dominio
    }

    @Test
    void stanzaCorridoioNonHaEsame() {
        Mappa mappa = new JsonLoaderMondo(sorgente(MAPPA_JSON)).caricaMappa();
        assertNull(stanza(mappa, "Atrio").getEsame());
    }

    @Test
    void jsonMalformatoLanciaPersistenceException() {
        assertThrows(PersistenceException.class,
                () -> new JsonLoaderMondo(sorgente("{")).caricaMappa());
    }

    @Test
    void configurazioneSenzaStanzeLanciaPersistenceException() {
        assertThrows(PersistenceException.class,
                () -> new JsonLoaderMondo(sorgente("{ \"stanze\": [] }")).caricaMappa());
    }

    @Test
    void adiacenzaVersoStanzaInesistenteLanciaPersistenceException() {
        String json = """
                { "stanze": [
                    { "nome": "Atrio", "tipoStanza": "CORRIDOIO", "esame": null,
                      "adiacenze": ["Fantasma"], "propedeuticita": [] } ] }
                """;
        assertThrows(PersistenceException.class,
                () -> new JsonLoaderMondo(sorgente(json)).caricaMappa());
    }

    @Test
    void tipoStanzaSconosciutoLanciaPersistenceException() {
        String json = """
                { "stanze": [
                    { "nome": "Atrio", "tipoStanza": "SCANTINATO", "esame": null,
                      "adiacenze": [], "propedeuticita": [] } ] }
                """;
        assertThrows(PersistenceException.class,
                () -> new JsonLoaderMondo(sorgente(json)).caricaMappa());
    }

    @Test
    void costruttoreRifiutaInputNull() {
        assertThrows(IllegalArgumentException.class, () -> new JsonLoaderMondo(null));
    }
}
