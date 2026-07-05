package it.unicam.cs.mpgc.rpg130525.app;

import it.unicam.cs.mpgc.rpg130525.engine.*;
import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.persistence.JsonGameStatePersistence;
import it.unicam.cs.mpgc.rpg130525.port.GameInput;
import it.unicam.cs.mpgc.rpg130525.port.GameView;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceManager;

import java.nio.file.Path;
import java.util.List;

/**
 * Composition root della partita, espresso solo in termini dei contratti
 * {@link GameView} e {@link GameInput}: costruisce il mondo statico, il motore
 * e la persistenza, gestisce il menù iniziale (nuova partita con nome e scelta
 * carriera, oppure ripresa dal salvataggio) e avvia il {@link GameLoop}.
 * La stessa partita gira identica su console e interfaccia grafica.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic), come previsto dalle
 * indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
public final class Partita {

    private Partita() { }

    public static void esegui(GameView view, GameInput input) {
        if (view == null || input == null)
            throw new IllegalArgumentException("view o input nulli");

        // 1. Mondo statico: stanze, esami, professori, corridoi e propedeuticità
        Mappa mappa = new Mappa();
        Professore profProgrammazione = new Professore("Ada", "Lovelace", 3, 25);
        Professore profMetodologie = new Professore("Alberto", "Rossini", 4, 30);
        Esame programmazione = new Esame(1, "Programmazione", 6, profProgrammazione);
        Esame metodologie = new Esame(2, "Metodologie di Programmazione", 6, profMetodologie);

        Stanza atrio = new Stanza("Atrio", TipoStanza.CORRIDOIO, null);
        Stanza aulaStudio = new Stanza("Aula Studio", TipoStanza.AULA_STUDIO, null);
        Stanza la1 = new Stanza("Aula LA1", TipoStanza.AULA_ESAME, programmazione);
        Stanza la2 = new Stanza("Aula LA2", TipoStanza.AULA_ESAME, metodologie);

        mappa.addStanza(atrio);
        mappa.addStanza(aulaStudio);
        mappa.addStanza(la1);
        mappa.addStanza(la2);
        mappa.addCorridoio(atrio, aulaStudio);
        mappa.addCorridoio(atrio, la1);
        mappa.addCorridoio(atrio, la2);
        mappa.addPropedeuticita(la1, la2);   // Programmazione è prerequisito di Metodologie

        int cfuPerLaurea = programmazione.getCfuAssociati() + metodologie.getCfuAssociati();

        // 2. Persistenza e motore, cablati sui contratti astratti
        Path fileSalvataggio = Path.of(System.getProperty("java.io.tmpdir"), "unicumlaude-save.json");
        PersistenceManager persistence = new JsonGameStatePersistence(fileSalvataggio, mappa);

        CalcolatoreDanno calcolatore = new CalcolatoreDanno();
        FaseEsame provaScritta = new ProvaScritta(DatabaseDomande.seedCompleto(), input, calcolatore);
        FaseEsame provaOrale = new ProvaOrale(new GestoreTurno(calcolatore));
        EsameController esameController = new EsameController(List.of(provaScritta, provaOrale));
        ControllerMovimentoStanze movimento = new ControllerMovimentoStanze(mappa);
        GameLoop gameLoop = new GameLoop(mappa, movimento, esameController, persistence, cfuPerLaurea);

        // 3. Menù iniziale: nuova partita o ripresa dal salvataggio
        view.mostraMessaggio("=== UniCum Laude ===");
        StatoGioco stato;
        if (persistence.esisteSalvataggio()
                && input.scegli("Menu principale", List.of("Nuova partita", "Continua partita")) == 1) {
            stato = persistence.carica();
            view.mostraMessaggio("Bentornato, " + stato.getStudente().getNomeCompleto() + "!");
        } else {
            stato = nuovaPartita(input, atrio);
        }

        // 4. Si gioca
        gameLoop.gioca(stato, view, input);
    }

    //setup di una nuova partita: nome del giocatore e scelta della carriera
    private static StatoGioco nuovaPartita(GameInput input, Stanza posizioneIniziale) {
        String nome = input.chiediTesto("Nome del giocatore");
        String cognome = input.chiediTesto("Cognome del giocatore");

        List<CareerStrategy> carriere = List.of(
                new StudenteFullTime(), new StudenteLavoratore(), new StudenteFuoriCorso());
        List<String> voci = carriere.stream()
                .map(c -> c.getNome() + " (" + c.getDescrizione() + ")")
                .toList();
        CareerStrategy carriera = carriere.get(input.scegli("Scegli la tua carriera", voci));

        Studente studente = new Studente(nome, cognome, 10, 5, 100, 50, carriera);
        return new StatoGioco(studente, posizioneIniziale);
    }
}
