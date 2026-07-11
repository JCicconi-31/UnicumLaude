package it.unicam.cs.mpgc.rpg130525.model;

/**
 * Stato dinamico della partita: lo studente e la sua posizione corrente sulla
 * mappa, gli unici dati che cambiano durante il gioco.
 */
public class StatoGioco {
    private final Studente studente;
    private Stanza posizioneCorrente;

    public StatoGioco(Studente studente, Stanza stanzaAttuale) {
        if (studente == null || stanzaAttuale == null) throw new IllegalArgumentException("studente o posizione nulla");
        this.studente = studente;
        this.posizioneCorrente = stanzaAttuale;
    }

    public void spostaIn(Stanza nuova) {
        if (nuova == null) throw new IllegalArgumentException("posizione nulla");
        this.posizioneCorrente = nuova;
    }

    public Studente getStudente() {
        return studente;
    }

    public Stanza getPosizioneCorrente() {
        return posizioneCorrente;
    }
}
