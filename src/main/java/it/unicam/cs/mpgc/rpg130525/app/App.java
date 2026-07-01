package it.unicam.cs.mpgc.rpg130525.app;

import it.unicam.cs.mpgc.rpg130525.engine.*;
import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.persistence.JsonGameStatePersistence;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceManager;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;
import it.unicam.cs.mpgc.rpg130525.view.ConsoleView;

import java.nio.file.Path;
import java.util.List;

/**
 * Composition root dell'applicazione: l'unico punto in cui gli strati vengono
 * collegati tra loro. Costruisce il mondo statico, lo studente, gli adapter
 * concreti dei port (ConsoleView, JsonGameStatePersistence) e la catena del
 * motore, poi li inietta e avvia una sessione dimostrativa. Tutte le classi del
 * motore dipendono solo dai contratti astratti: qui, e solo qui, si conoscono
 * le implementazioni concrete.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic), come previsto dalle
 * indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
public class App {

    public static void main(String[] args) {
        // 1. Mondo statico: mappa, stanze, esame, professore (boss)
        Mappa mappa = new Mappa();
        Professore prof = new Professore("Alberto", "Rossini", 4, 30);
        Esame esameMdP = new Esame(1, "Metodologie di Programmazione", 6, prof);
        Stanza atrio = new Stanza("Atrio", TipoStanza.CORRIDOIO, null);
        Stanza aulaMdP = new Stanza("Aula LA1", TipoStanza.AULA_ESAME, esameMdP);
        mappa.addStanza(atrio);
        mappa.addStanza(aulaMdP);
        mappa.addCorridoio(atrio, aulaMdP);

        // 2. Stato iniziale: studente in una carriera scelta, posizionato nell'atrio
        Studente studente = new Studente("Mario", "Rossi", 15, 8, 100, 50, new StudenteFullTime());
        StatoGioco stato = new StatoGioco(studente, atrio);

        // 3. Adapter concreti dei contratti di confine (i port)
        ConsoleView view = new ConsoleView();                    // GameView + GameInput
        Path fileSalvataggio = Path.of(System.getProperty("java.io.tmpdir"), "unicumlaude-save.json");
        PersistenceManager persistence = new JsonGameStatePersistence(fileSalvataggio, mappa);

        // 4. Catena del motore: dipende solo dalle astrazioni, non dagli adapter
        CalcolatoreDanno calcolatore = new CalcolatoreDanno();
        GestoreTurno gestoreTurno = new GestoreTurno(calcolatore);
        DatabaseDomande domande = DatabaseDomande.seedMetodologie();
        FaseEsame provaScritta = new ProvaScritta(domande, view, calcolatore);
        FaseEsame provaOrale = new ProvaOrale(gestoreTurno);
        EsameController esameController = new EsameController(List.of(provaScritta, provaOrale));
        ControllerMovimentoStanze movimento = new ControllerMovimentoStanze(mappa);

        // 5. Sessione dimostrativa: movimento -> esame -> persistenza
        view.mostraMessaggio("=== UniCum Laude ===");
        view.aggiornaStatoGiocatore(StudenteDto.da(studente));

        try {
            view.mostraMessaggio("\nTi sposti verso: " + aulaMdP.getNome());
            movimento.spostati(stato, aulaMdP);
        } catch (PrerequisitiNonRispettatiException e) {
            view.mostraMessaggio(e.getMessage());
            return;
        }

        view.mostraMessaggio("\nInizia l'esame di " + esameMdP.getNomeCorso() + "\n");
        boolean superato = esameController.sostieniEsame(stato, view);
        view.mostraMessaggio(superato ? "\nEsame SUPERATO!" : "\nEsame non superato.");
        view.aggiornaStatoGiocatore(StudenteDto.da(studente));

        // 6. Persistenza: salvataggio e ricarica per mostrare il round-trip
        persistence.salva(stato);
        view.mostraMessaggio("\nPartita salvata in: " + fileSalvataggio);
        StatoGioco ricaricato = persistence.carica();
        view.mostraMessaggio("Ricaricato: sei in " + ricaricato.getPosizioneCorrente().getNome()
                + " con " + ricaricato.getStudente().getLibretto().getCfuOttenuti() + " CFU");
    }
}
