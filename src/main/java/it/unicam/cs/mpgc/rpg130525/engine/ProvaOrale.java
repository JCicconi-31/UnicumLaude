package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.Professore;
import it.unicam.cs.mpgc.rpg130525.model.Stanza;
import it.unicam.cs.mpgc.rpg130525.model.StatoGioco;
import it.unicam.cs.mpgc.rpg130525.port.GameInput;
import it.unicam.cs.mpgc.rpg130525.port.GameView;
import it.unicam.cs.mpgc.rpg130525.port.ProfessoreDto;

import java.util.List;

public class ProvaOrale implements FaseEsame {
    private final GestoreTurno gestoreTurno;
    private final GameInput input;

    public ProvaOrale(GestoreTurno gestoreTurno, GameInput input) {
        if (gestoreTurno == null || input == null)
            throw new IllegalArgumentException("parametri nulli");
        this.gestoreTurno = gestoreTurno;
        this.input = input;
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
        view.aggiornaStatoProfessore(ProfessoreDto.da(professore));
        //fase concreta di "combattimento" tra Studente e Professore
        while (!professore.isKO()) {
            input.scegli("Il prof. " + professore.getCognome() + " fa la domanda: ",
                    List.of("Rispondi alla domanda"));
            gestoreTurno.eseguiTurno(stato.getStudente(), professore, view);
        }

        view.mostraMessaggio("Prova orale superata!");
        return true;
    }
}
