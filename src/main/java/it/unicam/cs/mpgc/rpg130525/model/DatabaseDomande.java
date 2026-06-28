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

    /**
     * Crea un database popolato con le domande di "Metodologie di Programmazione".
     * <p>
     * Dati di seed temporanei: serviranno finché la persistenza non caricherà le
     * domande da file JSON. Vanno poi spostati nei file di gioco.
     *
     * @return un {@code DatabaseDomande} pronto all'uso con le domande di MdP
     */
    public static DatabaseDomande seedMetodologie() {
        Map<String, List<Domanda>> dati = Map.of(
            "Metodologie di Programmazione", List.of(
                new Domanda(
                    "Cosa afferma il Single Responsibility Principle (SRP)?",
                    List.of(
                        "Una classe deve avere una sola ragione per cambiare",
                        "Una classe non può avere più di un metodo pubblico",
                        "Ogni classe deve implementare almeno un'interfaccia",
                        "Una classe deve essere final per non essere estesa"
                    ), 0),
                new Domanda(
                    "A cosa serve il pattern Strategy?",
                    List.of(
                        "A creare un'unica istanza condivisa di una classe",
                        "A incapsulare comportamenti intercambiabili dietro un'astrazione",
                        "A notificare automaticamente gli osservatori di un cambiamento",
                        "A costruire oggetti complessi passo dopo passo"
                    ), 1),
                new Domanda(
                    "Qual è il rapporto corretto tra equals() e hashCode() in Java?",
                    List.of(
                        "Sono indipendenti e si possono ridefinire separatamente",
                        "Se due oggetti sono equals, devono avere lo stesso hashCode",
                        "hashCode deve sempre restituire un valore diverso per ogni oggetto",
                        "equals va ridefinito solo per le classi che estendono Object"
                    ), 1),
                new Domanda(
                    "Cosa stabilisce l'Open/Closed Principle (OCP)?",
                    List.of(
                        "Il codice deve essere aperto in lettura ma chiuso in scrittura",
                        "Ogni modulo deve esporre tutti i suoi campi come pubblici",
                        "Il software deve essere aperto all'estensione ma chiuso alla modifica",
                        "Le classi astratte non possono avere metodi concreti"
                    ), 2),
                new Domanda(
                    "Perché si preferisce 'programmare verso un'interfaccia' anziché verso un'implementazione?",
                    List.of(
                        "Perché le interfacce vengono eseguite più velocemente a runtime",
                        "Per ridurre l'accoppiamento e poter sostituire l'implementazione",
                        "Perché le classi concrete non possono essere usate come tipo",
                        "Per evitare di dover scrivere i costruttori"
                    ), 1)
            )
        );
        return new DatabaseDomande(dati);
    }
}
