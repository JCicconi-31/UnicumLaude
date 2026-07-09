package it.unicam.cs.mpgc.rpg130525.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link DatabaseDomande}.
 * <p>
 * L'estrazione casuale viene resa deterministica iniettando uno stub di
 * {@link Random} (approccio Mock/Stub della sez. 5 del documento di analisi):
 * così il test sa esattamente quale domanda verrà restituita.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class DatabaseDomandeTest {

    private static final String CORSO = "Corso di Prova";

    private final Domanda d0 = new Domanda("prima", List.of("a", "b"), 0);
    private final Domanda d1 = new Domanda("seconda", List.of("a", "b"), 1);

    /** Stub di Random che restituisce sempre un indice fisso. */
    private static Random randomCheRestituisce(int valore) {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return valore;
            }
        };
    }

    @Test
    void estraiCasualeUsaLaSorgenteDiCasualitaIniettata() {
        Map<String, List<Domanda>> dati = Map.of(CORSO, List.of(d0, d1));
        DatabaseDomande db = new DatabaseDomande(dati, randomCheRestituisce(1));

        assertSame(d1, db.estraiCasuale(CORSO));
    }

    @Test
    void estraiCasualePuoRestituireLaPrimaDomanda() {
        Map<String, List<Domanda>> dati = Map.of(CORSO, List.of(d0, d1));
        DatabaseDomande db = new DatabaseDomande(dati, randomCheRestituisce(0));

        assertSame(d0, db.estraiCasuale(CORSO));
    }

    @Test
    void estraiCasualeLanciaEccezioneSeCorsoInesistente() {
        DatabaseDomande db = new DatabaseDomande(Map.of(CORSO, List.of(d0)));
        assertThrows(IllegalArgumentException.class,
                () -> db.estraiCasuale("Corso Inesistente"));
    }

    @Test
    void costruttoreRifiutaMappaNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new DatabaseDomande(null));
    }

    @Test
    void costruttoreRifiutaRandomNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new DatabaseDomande(Map.of(CORSO, List.of(d0)), null));
    }
}
