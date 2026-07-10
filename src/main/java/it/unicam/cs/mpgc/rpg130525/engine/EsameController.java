package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.BurnoutException;
import it.unicam.cs.mpgc.rpg130525.model.Esame;
import it.unicam.cs.mpgc.rpg130525.model.EsameSuperato;
import it.unicam.cs.mpgc.rpg130525.model.StatoGioco;
import it.unicam.cs.mpgc.rpg130525.port.GameView;

import java.util.List;
import java.util.Random;

public class EsameController {
    private final List<FaseEsame> fasi;
    private final Random random;

    public EsameController(List<FaseEsame> fasi) {
        this(fasi, new Random());
    }

    public EsameController(List<FaseEsame> fasi, Random random) {
        if (fasi == null || random == null)
            throw new IllegalArgumentException("input nulli");
        this.fasi = List.copyOf(fasi);
        this.random = random;
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
        /* Il voto è generato qui con un Random iniettato.
         * Non si crea una classe Valutatore dedicata, per ora è pura casualità e per il principio KISS
         * non vale una nuova astrazione. Se in futuro la valutazione avrà una logica ben precisa tipo media, lode,
         * bonus, allora sì andrà in un GeneratoreVoto separato per SRP.
         */
        int voto = 18 + random.nextInt(13);
        stato.getStudente().getLibretto().addEsameSuperato(new EsameSuperato(esame, voto));
        return true;
    }
}