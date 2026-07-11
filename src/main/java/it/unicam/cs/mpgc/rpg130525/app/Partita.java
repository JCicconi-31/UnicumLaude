package it.unicam.cs.mpgc.rpg130525.app;

import it.unicam.cs.mpgc.rpg130525.engine.*;
import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.persistence.JsonGameStatePersistence;
import it.unicam.cs.mpgc.rpg130525.persistence.JsonLoaderDomande;
import it.unicam.cs.mpgc.rpg130525.persistence.JsonLoaderMondo;
import it.unicam.cs.mpgc.rpg130525.port.*;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Composition root della partita: carica il mondo e le domande dai file JSON,
 * costruisce motore e persistenza sui contratti astratti, gestisce il menù
 * iniziale (nuova partita, caricamento del salvataggio o uscita) e avvia il
 * {@link GameLoop}. È espressa solo in termini dei contratti {@code GameView}
 * e {@code GameInput}, senza dipendere dalla UI concreta.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic, tramite Claude Code), come
 * previsto dalle indicazioni del corso.
 */
public final class Partita {

    private static final String FILE_MAPPA = "mappa.json";
    private static final String FILE_DOMANDE = "domandeEsami.json";
    private static final String STANZA_INIZIALE = "Atrio";
    private static final int INTELLIGENZA_INIZIALE = 10;
    private static final int RESILIENZA_INIZIALE = 5;
    private static final int HP_INIZIALE = 100;
    private static final int MONETE_INIZIALI = 50;


    private Partita() {
    }

    public static void esegui(GameView view, GameInput input) {
        if (view == null || input == null)
            throw new IllegalArgumentException("view o input nulli");

        LoaderMondo loaderMondo = new JsonLoaderMondo(risorsa(FILE_MAPPA));
        Mappa mappa = loaderMondo.caricaMappa();
        Stanza partenza = mappa.getStanza(STANZA_INIZIALE)
                .orElseThrow(() -> new PersistenceException(
                        "stanza iniziale '" + STANZA_INIZIALE + "' assente nella mappa", null));
        int cfuPerLaurea = mappa.cfuTotali();
        LoaderDomande loaderDomande = new JsonLoaderDomande(risorsa(FILE_DOMANDE));
        DatabaseDomande database = new DatabaseDomande(loaderDomande.caricaDomande());
        Path fileSalvataggio = Path.of(System.getProperty("user.home"), ".unicumlaude", "datiPartita.json");
        PersistenceManager persistence = new JsonGameStatePersistence(fileSalvataggio, mappa);

        CalcolatoreDanno calcolatore = new CalcolatoreDanno();
        FaseEsame provaScritta = new ProvaScritta(database, input, calcolatore);
        FaseEsame provaOrale = new ProvaOrale(new GestoreTurno(calcolatore), input);
        EsameController esameController = new EsameController(List.of(provaScritta, provaOrale));
        ControllerMovimentoStanze movimento = new ControllerMovimentoStanze(mappa);
        GameLoop gameLoop = new GameLoop(mappa, movimento, esameController, persistence, cfuPerLaurea);

        view.mostraMessaggio("Benvenuto in Unicum Laude!");
        StatoGioco stato = menuIniziale(view, input, persistence, partenza);
        if (stato == null)
            return; // il giocatore ha scelto di uscire dal menù

        gameLoop.gioca(stato, view, input);
    }

    /**
     * Mostra il menù principale e prepara lo stato di gioco in base alla scelta.
     *
     * @return lo stato con cui iniziare a giocare, oppure null se il giocatore esce
     */
    private static StatoGioco menuIniziale(GameView view, GameInput input,
                                           PersistenceManager persistence, Stanza partenza) {
        List<String> voci = new ArrayList<>();
        voci.add("Nuova Partita");
        if (persistence.esisteSalvataggio())
            voci.add("Carica Partita");
        voci.add("Esci");

        String scelta = voci.get(input.scegli("Menu principale", voci));
        if (scelta.equals("Esci"))
            return null;
        if (scelta.equals("Carica Partita")) {
            StatoGioco stato = persistence.carica();
            view.mostraMessaggio("Bentornato, " + stato.getStudente().getNomeCompleto() + "!");
            return stato;
        }
        return nuovaPartita(input, partenza);
    }

    private static InputStream risorsa(String nomeFile) {
        InputStream in = Partita.class.getResourceAsStream("/" + nomeFile);
        if (in == null)
            throw new PersistenceException("risorsa di configurazione non trovata: " + nomeFile, null);
        return in;
    }

    private static StatoGioco nuovaPartita(GameInput input, Stanza posizioneIniziale) {
        String nome = input.chiediTesto("Nome del giocatore");
        String cognome = input.chiediTesto("Cognome del giocatore");
        List<CareerStrategy> carriere = List.of(
                new StudenteFullTime(), new StudenteLavoratore(), new StudenteFuoriCorso());
        List<String> voci = carriere.stream()
                .map(c -> c.getNome() + " (" + c.getDescrizione() + ")")
                .toList();
        CareerStrategy carriera = carriere.get(input.scegli("Scegli la tua carriera", voci));
        Studente studente = new Studente(nome, cognome, INTELLIGENZA_INIZIALE, RESILIENZA_INIZIALE, HP_INIZIALE, MONETE_INIZIALI, carriera);
        return new StatoGioco(studente, posizioneIniziale);
    }
}