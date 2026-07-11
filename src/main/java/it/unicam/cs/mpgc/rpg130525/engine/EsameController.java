package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.BurnoutException;
import it.unicam.cs.mpgc.rpg130525.model.Esame;
import it.unicam.cs.mpgc.rpg130525.model.EsameSuperato;
import it.unicam.cs.mpgc.rpg130525.model.StatoGioco;
import it.unicam.cs.mpgc.rpg130525.port.GameView;

import java.util.List;
import java.util.Random;

/**
 * Gestisce in sequenza le fasi d'esame, quando tutte sono completate, registra
 * l'esame nel libretto con un voto casuale (sorgente iniettabile per i test).
 */
public class EsameController {
    private final List<FaseEsame> fasi;
    private final Random random;
    private static final int VOTO_MINIMO = 18;
    private static final int VOTO_MASSIMO = 30;

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
        int voto = VOTO_MINIMO + random.nextInt(VOTO_MASSIMO - VOTO_MINIMO + 1);
        stato.getStudente().getLibretto().addEsameSuperato(new EsameSuperato(esame, voto));
        return true;
    }
}