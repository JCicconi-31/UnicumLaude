package it.unicam.cs.mpgc.rpg130525.model;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Database delle domande della prova scritta, organizzate per nome del corso
 * <p>
 * Incapsula una {@code Map<String, List<Domanda>>} e isola in un unico punto
 * la logica di estrazione casuale, così che {@code ProvaScritta} possa pescare
 * una domanda specifica per l'aula in cui si trova il giocatore. La sorgente di
 * casualità ({@link Random}) è iniettabile dal costruttore per rendere i test
 * deterministici
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic), come previsto dalle
 * indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
public class DatabaseDomande {
    private final Map<String, List<Domanda>> domandePerCorso;
    private final Random random;

    public DatabaseDomande(Map<String, List<Domanda>> domandePerCorso) {
        this(domandePerCorso, new Random());
    }

    /**
     * @param domandePerCorso mappa corso → domande
     * @param random sorgente di casualità (iniettabile per i test deterministici)
     */
    public DatabaseDomande(Map<String, List<Domanda>> domandePerCorso, Random random) {
        if (domandePerCorso == null)
            throw new IllegalArgumentException("database nullo");
        if (random == null)
            throw new IllegalArgumentException("random nullo");
        this.domandePerCorso = Map.copyOf(domandePerCorso); // immutabile dopo la costruzione
        this.random = random;
    }

    /**
     * Estrae casualmente una domanda fra quelle del corso indicato.
     *
     * @param nomeCorso nome del corso (es. "Metodologie di Programmazione")
     * @return una domanda casuale del corso
     * @throws IllegalArgumentException se il corso non ha domande
     */
    public Domanda estraiCasuale(String nomeCorso) {
        List<Domanda> domande = domandePerCorso.get(nomeCorso);
        if (domande == null || domande.isEmpty())
            throw new IllegalArgumentException("nessuna domanda per il corso: " + nomeCorso);
        return domande.get(random.nextInt(domande.size()));
    }

}
