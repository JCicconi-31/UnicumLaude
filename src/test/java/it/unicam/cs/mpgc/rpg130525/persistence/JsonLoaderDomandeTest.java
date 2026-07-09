package it.unicam.cs.mpgc.rpg130525.persistence;

import it.unicam.cs.mpgc.rpg130525.model.Domanda;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per {@link JsonLoaderDomande}: verifica che il file delle domande venga
 * deserializzato nella {@code Map<String, List<Domanda>>} attesa dal contratto,
 * con i corsi come chiavi e le {@link Domanda} costruite tramite il loro vero
 * costruttore (così le invarianti restano validate), e che le sorgenti illeggibili
 * o incoerenti vengano incapsulate in {@link PersistenceException}.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class JsonLoaderDomandeTest {

    private static final String DOMANDE_JSON = """
            {
              "corsi": [
                { "nomeCorso": "Programmazione", "domande": [
                    { "testo": "Parola chiave per una costante in Java?",
                      "opzioni": ["final", "const", "static"], "indiceCorretta": 0 },
                    { "testo": "3 / 2 fra interi in Java vale?",
                      "opzioni": ["1", "1.5", "2"], "indiceCorretta": 0 } ] },
                { "nomeCorso": "Metodologie", "domande": [
                    { "testo": "Cosa afferma il Single Responsibility Principle?",
                      "opzioni": ["Una sola ragione per cambiare", "Un solo metodo pubblico"],
                      "indiceCorretta": 0 } ] }
              ]
            }
            """;

    private InputStream sorgente(String json) {
        return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void caricaTuttiICorsi() {
        Map<String, List<Domanda>> db = new JsonLoaderDomande(sorgente(DOMANDE_JSON)).caricaDomande();
        assertEquals(2, db.size());
        assertTrue(db.containsKey("Programmazione"));
        assertTrue(db.containsKey("Metodologie"));
    }

    @Test
    void caricaTutteLeDomandeDiUnCorso() {
        Map<String, List<Domanda>> db = new JsonLoaderDomande(sorgente(DOMANDE_JSON)).caricaDomande();
        assertEquals(2, db.get("Programmazione").size());
        assertEquals(1, db.get("Metodologie").size());
    }

    @Test
    void domandeCostruiteConLaRispostaCorretta() {
        Map<String, List<Domanda>> db = new JsonLoaderDomande(sorgente(DOMANDE_JSON)).caricaDomande();
        Domanda prima = db.get("Programmazione").get(0);
        assertEquals("Parola chiave per una costante in Java?", prima.getTesto());
        assertEquals(3, prima.getOpzioni().size());
        assertTrue(prima.isCorretta(0));
        assertFalse(prima.isCorretta(1));
    }

    @Test
    void jsonMalformatoLanciaPersistenceException() {
        assertThrows(PersistenceException.class,
                () -> new JsonLoaderDomande(sorgente("{")).caricaDomande());
    }

    @Test
    void configurazioneSenzaCorsiLanciaPersistenceException() {
        assertThrows(PersistenceException.class,
                () -> new JsonLoaderDomande(sorgente("{ \"corsi\": [] }")).caricaDomande());
    }

    @Test
    void corsoSenzaDomandeLanciaPersistenceException() {
        String json = """
                { "corsi": [ { "nomeCorso": "Vuoto", "domande": [] } ] }
                """;
        assertThrows(PersistenceException.class,
                () -> new JsonLoaderDomande(sorgente(json)).caricaDomande());
    }

    @Test
    void domandaInvalidaLanciaPersistenceException() {
        // indiceCorretta fuori range: il costruttore Domanda rifiuta, wrappato in PersistenceException
        String json = """
                { "corsi": [ { "nomeCorso": "X", "domande": [
                    { "testo": "Domanda", "opzioni": ["a", "b"], "indiceCorretta": 9 } ] } ] }
                """;
        assertThrows(PersistenceException.class,
                () -> new JsonLoaderDomande(sorgente(json)).caricaDomande());
    }

    @Test
    void costruttoreRifiutaInputNull() {
        assertThrows(IllegalArgumentException.class, () -> new JsonLoaderDomande(null));
    }
}
