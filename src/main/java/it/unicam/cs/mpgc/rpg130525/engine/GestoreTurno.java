package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.Professore;
import it.unicam.cs.mpgc.rpg130525.model.Studente;
import it.unicam.cs.mpgc.rpg130525.port.GameView;
import it.unicam.cs.mpgc.rpg130525.port.ProfessoreDto;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;

/**
 * Gestisce un singolo turno della prova orale: il danno inflitto al professore
 * e, se non è KO, il danno subìto dallo studente.
 */
public class GestoreTurno {
    private final CalcolatoreDanno calcolatore;

    public GestoreTurno(CalcolatoreDanno calcolatore) {
        if (calcolatore == null)
            throw new IllegalArgumentException("calcolatore nullo");
        this.calcolatore = calcolatore;
    }

    public void eseguiTurno(Studente studente, Professore professore, GameView view) {
        if (studente == null || professore == null || view == null)
            throw new IllegalArgumentException("parametri del turno nulli");

        int dannoInflitto = calcolatore.dannoInflitto(studente);
        professore.subisciDanno(dannoInflitto);
        view.aggiornaStatoProfessore(ProfessoreDto.da(professore));
        view.mostraMessaggio("Rispondi e infliggi " + dannoInflitto + " danno concettuale");
        if (professore.isKO())
            return;
        int dannoSubito = calcolatore.dannoSubito(professore, studente);
        studente.subisciDanno(dannoSubito);
        view.mostraMessaggio("Il professore incalza: subisci " + dannoSubito + " stress");
        view.aggiornaStatoGiocatore(StudenteDto.da(studente));
    }
}
