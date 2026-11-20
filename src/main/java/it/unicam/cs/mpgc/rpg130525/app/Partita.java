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
import java.util.Optional;

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
    private static final String INDIETRO = "Indietro";


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
        while (true) {
            List<String> voci = new ArrayList<>();
            voci.add("Nuova Partita");
            if (persistence.esisteSalvataggio())
                voci.add("Carica Partita");
            voci.add("Esci");

            String scelta = voci.get(input.scegli("Menu principale", voci));
            if (scelta.equals("Esci"))
                return null;
            StatoGioco stato = scelta.equals("Carica Partita")
                    ? caricaPartita(view, input, persistence)
                    : nuovaPartita(input, partenza);
            if (stato != null)
                return stato;
            // il giocatore è tornato indietro: si ripropone il menù principale
        }
    }

    /** Mostra il salvataggio trovato e chiede conferma; null se si torna indietro. */
    private static StatoGioco caricaPartita(GameView view, GameInput input, PersistenceManager persistence) {
        StatoGioco stato = persistence.carica();
        Studente studente = stato.getStudente();
        String riepilogo = "Partita salvata: " + studente.getNomeCompleto()
                + " (" + studente.getLibretto().getCfuOttenuti() + " CFU)";
        if (input.scegli(riepilogo, List.of("Continua", INDIETRO)) != 0)
            return null;
        view.mostraMessaggio("Bentornato, " + studente.getNomeCompleto() + "!");
        return stato;
    }

    private static InputStream risorsa(String nomeFile) {
        InputStream in = Partita.class.getResourceAsStream("/" + nomeFile);
        if (in == null)
            throw new PersistenceException("risorsa di configurazione non trovata: " + nomeFile, null);
        return in;
    }

    /**
     * Procedura guidata di creazione del personaggio (nome, cognome, carriera).
     * Da ogni passo si può tornare al precedente; dal primo si torna al menù.
     *
     * @return lo stato della nuova partita, oppure null se si torna al menù
     */
    private static StatoGioco nuovaPartita(GameInput input, Stanza posizioneIniziale) {
        List<CareerStrategy> carriere = List.of(
                new StudenteFullTime(), new StudenteLavoratore(), new StudenteFuoriCorso());
        List<String> voci = new ArrayList<>(carriere.stream()
                .map(c -> c.getNome() + " (" + c.getDescrizione() + ")")
                .toList());
        voci.add(INDIETRO);

        String nome = null;
        String cognome = null;
        int passo = 0;
        while (true) {
            switch (passo) {
                case 0 -> {
                    Optional<String> inserito = input.chiediTestoOppureIndietro("Nome del giocatore");
                    if (inserito.isEmpty())
                        return null;
                    nome = inserito.get();
                    passo = 1;
                }
                case 1 -> {
                    Optional<String> inserito = input.chiediTestoOppureIndietro("Cognome del giocatore");
                    cognome = inserito.orElse(null);
                    passo = inserito.isPresent() ? 2 : 0;
                }
                default -> {
                    int scelta = input.scegli("Scegli la tua carriera", voci);
                    if (scelta == carriere.size()) {
                        passo = 1;
                        continue;
                    }
                    Studente studente = new Studente(nome, cognome, INTELLIGENZA_INIZIALE,
                            RESILIENZA_INIZIALE, HP_INIZIALE, MONETE_INIZIALI, carriere.get(scelta));
                    return new StatoGioco(studente, posizioneIniziale);
                }
            }
        }
    }
}