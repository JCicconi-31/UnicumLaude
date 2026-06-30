package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.Studente;
import it.unicam.cs.mpgc.rpg130525.model.Professore;
import it.unicam.cs.mpgc.rpg130525.model.NemicoMinore;

public class CalcolatoreDanno {
    public int dannoInflitto(Studente studente) {
        if (studente == null)
            throw new IllegalArgumentException("studente passato null");
        return studente.getIntelligenzaEffettiva();
    }

    public int dannoSubito(Professore professore, Studente studente) {
        if (professore == null || studente == null)
            throw new IllegalArgumentException("impossibile calcolare danno subito su una o più entity null");
        if (studente.getSaluteMentale() > 0){
            return Math.max(1, professore.getDifficolta() - studente.getResilienzaEffettiva());
        }
        return 0;
    }

    public int dannoSubito(NemicoMinore nemico, Studente studente) {
        if (nemico == null)
            throw new IllegalArgumentException("nemico  null");
        if (studente == null)
            throw new IllegalArgumentException("studente null");
        return Math.max(1, nemico.getDannoStress() - studente.getResilienzaEffettiva());
    }
}