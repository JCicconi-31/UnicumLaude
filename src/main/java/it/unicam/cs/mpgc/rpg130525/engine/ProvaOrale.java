package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.Professore;
import it.unicam.cs.mpgc.rpg130525.model.Stanza;
import it.unicam.cs.mpgc.rpg130525.model.StatoGioco;
import it.unicam.cs.mpgc.rpg130525.port.GameView;

public class ProvaOrale implements FaseEsame {
    private final GestoreTurno gestoreTurno;

    public ProvaOrale(GestoreTurno gestoreTurno) {
        if (gestoreTurno == null)
            throw new IllegalArgumentException("gestore turno nullo");
        this.gestoreTurno = gestoreTurno;
    }

    @Override
    public boolean esegui(StatoGioco stato, GameView view) {
        if (stato == null || view == null)
            throw new IllegalArgumentException("stato o view nulli");

        Stanza aula = stato.getPosizioneCorrente();
        if (aula.getEsame() == null)
            throw new IllegalStateException("la stanza non ha un esame associato");

        Professore professore = aula.getEsame().getProfessore();
        view.mostraMessaggio("Inizia la prova orale con il prof. " + professore.getNomeCompleto());
        //fase concreta di "combattimento" tra Studente e Professore
        while (!professore.isKO())
            gestoreTurno.eseguiTurno(stato.getStudente(), professore, view);

        view.mostraMessaggio("Prova orale superata!");
        return true;
    }
}
