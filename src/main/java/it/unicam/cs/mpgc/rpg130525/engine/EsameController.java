package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.BurnoutException;
import it.unicam.cs.mpgc.rpg130525.model.Esame;
import it.unicam.cs.mpgc.rpg130525.model.EsameSuperato;
import it.unicam.cs.mpgc.rpg130525.model.StatoGioco;
import it.unicam.cs.mpgc.rpg130525.port.GameView;

import java.util.List;

public class EsameController {
    private final List<FaseEsame> fasi;

    public EsameController(List<FaseEsame> fasi) {
        if (fasi == null)
            throw new IllegalArgumentException("fase nulla");
        this.fasi = List.copyOf(fasi);
    }

    public boolean sostieniEsame(StatoGioco stato, GameView view) {
        try {
            for (FaseEsame fase : fasi)
                if (!fase.esegui(stato, view))
                    return false;

        } catch (BurnoutException e) {
            view.mostraMessaggio("Burnout! " + e.getMessage());
            return false;
        }
        Esame esame = stato.getPosizioneCorrente().getEsame();
        stato.getStudente().getLibretto().addEsameSuperato(new EsameSuperato(esame, (int)(Math.random() * 12) + 18));
        return true;
    }
}