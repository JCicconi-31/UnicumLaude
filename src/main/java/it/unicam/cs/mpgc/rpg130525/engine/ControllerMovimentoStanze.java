package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.*;

/**
 * Governa lo spostamento del giocatore tra stanze, verificando l'esistenza del
 * corridoio e il rispetto delle propedeuticità prima di aggiornare la posizione.
 */
public class ControllerMovimentoStanze {
    private final Mappa mappa;

    public ControllerMovimentoStanze(Mappa mappa) {
        if (mappa == null) throw new IllegalArgumentException("mappa nulla");
        this.mappa = mappa;
    }

    public void spostati(StatoGioco stato, Stanza destinazione) throws MovimentoNonConsentitoException {
        if (stato == null || destinazione == null) throw new IllegalArgumentException("parametri passati null");
        if (!mappa.getAdiacenti(stato.getPosizioneCorrente()).contains(destinazione))
            throw new MovimentoNonConsentitoException("nessun corridoio tra " + stato.getPosizioneCorrente().getNome() + " e " + destinazione.getNome());
        if (destinazione.getTipo() == TipoStanza.AULA_ESAME) {
            boolean disponibile = mappa.isDisponibile(destinazione, stato.getStudente().getLibretto().getEsamiSuperati());
            if (!disponibile) throw new MovimentoNonConsentitoException(destinazione.getNome());
        }
        stato.spostaIn(destinazione);
    }
}
