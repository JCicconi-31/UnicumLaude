package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.Mappa;
import it.unicam.cs.mpgc.rpg130525.model.PrerequisitiNonRispettatiException;
import it.unicam.cs.mpgc.rpg130525.model.Stanza;
import it.unicam.cs.mpgc.rpg130525.model.StatoGioco;
import it.unicam.cs.mpgc.rpg130525.model.Studente;
import it.unicam.cs.mpgc.rpg130525.model.TipoStanza;
import it.unicam.cs.mpgc.rpg130525.port.GameInput;
import it.unicam.cs.mpgc.rpg130525.port.GameView;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceManager;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ciclo di gioco guidato dal giocatore: a ogni iterazione mostra la posizione
 * corrente, propone le azioni possibili (sostenere l'esame dell'aula, riposarsi
 * in aula studio, spostarsi verso una stanza adiacente, salvare e uscire) ed
 * esegue quella scelta. La partita termina con la Laurea al raggiungimento
 * della soglia di CFU, con il Burnout, o con l'uscita volontaria.
 * <p>
 * La classe dipende solo dai contratti astratti (GameView, GameInput,
 * PersistenceManager) ed è quindi identica per console e interfaccia grafica.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic), come previsto dalle
 * indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
public class GameLoop {
    private static final int RECUPERO_RIPOSO = 20;

    private final Mappa mappa;
    private final ControllerMovimentoStanze movimento;
    private final EsameController esameController;
    private final PersistenceManager persistence;
    private final int cfuPerLaurea;

    public GameLoop(Mappa mappa, ControllerMovimentoStanze movimento,
                    EsameController esameController, PersistenceManager persistence,
                    int cfuPerLaurea) {
        if (mappa == null || movimento == null || esameController == null || persistence == null)
            throw new IllegalArgumentException("dipendenze del game loop nulle");
        if (cfuPerLaurea <= 0)
            throw new IllegalArgumentException("la soglia CFU per la laurea deve essere positiva");
        this.mappa = mappa;
        this.movimento = movimento;
        this.esameController = esameController;
        this.persistence = persistence;
        this.cfuPerLaurea = cfuPerLaurea;
    }

    /** Esegue la partita fino a Laurea, Burnout o uscita volontaria. */
    public void gioca(StatoGioco stato, GameView view, GameInput input) {
        if (stato == null || view == null || input == null)
            throw new IllegalArgumentException("stato, view o input nulli");
        while (true) {
            Studente studente = stato.getStudente();
            view.aggiornaStatoGiocatore(StudenteDto.da(studente));
            if (studente.getLibretto().getCfuOttenuti() >= cfuPerLaurea) {
                view.mostraMessaggio("Hai raggiunto " + studente.getLibretto().getCfuOttenuti()
                        + " CFU: LAUREA! Hai scoperto il leggendario UNICUM!");
                return;
            }
            Stanza corrente = stato.getPosizioneCorrente();
            List<Azione> azioni = costruisciAzioni(corrente, studente);
            List<String> etichette = azioni.stream().map(Azione::etichetta).toList();
            Azione scelta = azioni.get(input.scegli("Sei in: " + corrente.getNome(), etichette));
            switch (scelta.tipo()) {
                case ESAME -> {
                    if (!affrontaEsame(stato, view))
                        return;   // burnout: partita finita
                }
                case RIPOSO -> {
                    studente.recuperaHP(RECUPERO_RIPOSO);
                    view.mostraMessaggio("Ti riposi in aula studio e recuperi energie.");
                }
                case MOVIMENTO -> muovi(stato, scelta.destinazione(), view);
                case ESCI -> {
                    persistence.salva(stato);
                    view.mostraMessaggio("Partita salvata. A presto!");
                    return;
                }
            }
        }
    }

    //le azioni dipendono dal tipo di stanza; le destinazioni sono ordinate per nome (UI stabile)
    private List<Azione> costruisciAzioni(Stanza corrente, Studente studente) {
        List<Azione> azioni = new ArrayList<>();
        if (corrente.getTipo() == TipoStanza.AULA_ESAME
                && !studente.getLibretto().getEsamiSuperati().contains(corrente.getEsame()))
            azioni.add(new Azione("Sostieni l'esame di " + corrente.getEsame().getNomeCorso(),
                    Tipo.ESAME, null));
        if (corrente.getTipo() == TipoStanza.AULA_STUDIO)
            azioni.add(new Azione("Riposati (recuperi " + RECUPERO_RIPOSO + " HP)", Tipo.RIPOSO, null));
        mappa.getAdiacenti(corrente).stream()
                .sorted(Comparator.comparing(Stanza::getNome))
                .forEach(d -> azioni.add(new Azione("Vai a: " + d.getNome(), Tipo.MOVIMENTO, d)));
        azioni.add(new Azione("Salva ed esci", Tipo.ESCI, null));
        return azioni;
    }

    /** @return false se la partita è terminata per Burnout */
    private boolean affrontaEsame(StatoGioco stato, GameView view) {
        boolean superato = esameController.sostieniEsame(stato, view);
        if (!superato && stato.getStudente().getSaluteMentale() == 0) {
            view.mostraMessaggio("GAME OVER: Burnout, rinunci agli studi.");
            return false;
        }
        if (!superato)
            view.mostraMessaggio("Esame non superato: riposati e riprova.");
        return true;
    }

    //il vincolo di propedeuticita' e' una condizione recuperabile: si mostra e si continua
    private void muovi(StatoGioco stato, Stanza destinazione, GameView view) {
        try {
            movimento.spostati(stato, destinazione);
        } catch (PrerequisitiNonRispettatiException e) {
            view.mostraMessaggio(e.getMessage());
        }
    }

    private enum Tipo { ESAME, RIPOSO, MOVIMENTO, ESCI }

    private record Azione(String etichetta, Tipo tipo, Stanza destinazione) { }
}
