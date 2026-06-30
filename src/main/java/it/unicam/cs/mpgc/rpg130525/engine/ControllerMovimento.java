package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.*;

import java.util.Set;

public class ControllerMovimento {
    private final Mappa mappa;

    public ControllerMovimento(Mappa mappa){
        if (mappa == null)
            throw new IllegalArgumentException("mappa nulla");
        this.mappa = mappa;
    }

    public void spostati(StatoGioco stato, Stanza destinazione) throws PrerequisitiNonRispettatiException {
        if (stato == null || destinazione == null)
            throw new IllegalArgumentException("parametri passati null");
        Set<Stanza> stanzeAdiacenti = mappa.getAdiacenti(stato.getPosizioneCorrente());
        if (!mappa.getAdiacenti(stato.getPosizioneCorrente()).contains(destinazione))
            throw new IllegalArgumentException("nessun corridoio tra " + stato.getPosizioneCorrente().getNome() + " e " + destinazione.getNome());
        if (destinazione.getTipo() == TipoStanza.AULA_ESAME) {
            boolean disponibile = mappa.isDisponibile(destinazione, stato.getStudente().getLibretto().getEsamiSuperati());
            if (!disponibile)
                throw new PrerequisitiNonRispettatiException(destinazione.getNome());
        }
        stato.spostaIn(destinazione);
    }
}
