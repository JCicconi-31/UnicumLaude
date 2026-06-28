package it.unicam.cs.mpgc.rpg130525.model;

import java.util.List;

/**
 * Rappresenta una singola domanda a risposta multipla della prova scritta.
 * <p>
 * Oggetto immutabile: il testo, le opzioni e l'indice della risposta corretta
 * sono fissati alla costruzione e non possono essere alterati. La risposta
 * corretta non viene esposta tramite getter, così la UI può mostrare testo e
 * opzioni ma la correzione passa esclusivamente da {@link #isCorretta(int)}.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic), come previsto dalle
 * indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
public class Domanda {
    private final String testo;
    private final List<String> opzioni;
    private final int indiceCorretta;

    public Domanda(String testo, List<String> opzioni, int indiceCorretta) {
        if (testo == null || testo.isBlank())
            throw new IllegalArgumentException("testo domanda non valido");
        if (opzioni == null || opzioni.size() < 2)
            throw new IllegalArgumentException("servono almeno due opzioni");
        if (indiceCorretta < 0 || indiceCorretta >= opzioni.size())
            throw new IllegalArgumentException("indice risposta corretta fuori range");
        this.testo = testo;
        this.opzioni = List.copyOf(opzioni); // copia immutabile difensiva
        this.indiceCorretta = indiceCorretta;
    }

    /**
     * Verifica se l'indice scelto corrisponde alla risposta corretta.
     *
     * @param rispostaScelta indice dell'opzione selezionata dal giocatore
     * @return true se la risposta è corretta
     */
    public boolean isCorretta(int rispostaScelta) {
        return rispostaScelta == indiceCorretta;
    }

    public String getTesto() { return testo; }

    public List<String> getOpzioni() { return opzioni; } // già immutabile (List.copyOf)
}
