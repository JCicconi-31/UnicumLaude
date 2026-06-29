package it.unicam.cs.mpgc.rpg130525.model;

public class StatoGioco {
    private final Studente studente;
    private final Stanza posizioneCorrente;

    public StatoGioco(Studente studente, Stanza stanzaAttuale) {
        if (studente == null || stanzaAttuale == null)
            throw new IllegalArgumentException("studente o posizione nulla");
        this.studente = studente;
        this.posizioneCorrente = stanzaAttuale;
    }
    public Studente getStudente() { return studente; }
    public Stanza getPosizioneCorrente() { return posizioneCorrente; }
}
