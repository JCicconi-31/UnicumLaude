package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.DatabaseDomande;
import it.unicam.cs.mpgc.rpg130525.model.Domanda;
import it.unicam.cs.mpgc.rpg130525.model.Esame;
import it.unicam.cs.mpgc.rpg130525.model.StatoGioco;
import it.unicam.cs.mpgc.rpg130525.port.GameInput;
import it.unicam.cs.mpgc.rpg130525.port.GameView;

public class ProvaScritta implements FaseEsame {
    private final DatabaseDomande database;
    private final GameInput input;
    private final CalcolatoreDanno calcolatoreDanno;

    public ProvaScritta(DatabaseDomande database, GameInput input, CalcolatoreDanno calcolatoreDanno) {
        if (database == null || input == null || calcolatoreDanno == null)
            throw new IllegalArgumentException("parametri null");
        this.database = database;
        this.input = input;
        this.calcolatoreDanno = calcolatoreDanno;
    }

    @Override
    public boolean esegui(StatoGioco stato, GameView view) {
        Esame esame = stato.getPosizioneCorrente().getEsame();
        Domanda domanda = database.estraiCasuale(esame.nomeCorso());
        int scelta = input.chiediRisposta(domanda);
        if (domanda.isCorretta(scelta)) {
            view.mostraMessaggio("Risposta corretta!");
            return true;
        }
        int danno = calcolatoreDanno.dannoSubito(esame.professore(), stato.getStudente());
        stato.getStudente().subisciDanno(danno);
        view.mostraMessaggio("Risposta errata: subisci " + danno + " stress");
        return false;
    }

}