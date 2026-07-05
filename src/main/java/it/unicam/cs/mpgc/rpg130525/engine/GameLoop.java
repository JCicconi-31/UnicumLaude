package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.port.GameInput;
import it.unicam.cs.mpgc.rpg130525.port.GameView;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceManager;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameLoop {
    private static final int RECUPERO_RIPOSO = 20;

    private final Mappa mappa;
    private final ControllerMovimentoStanze movimento;
    private final EsameController esameController;
    private final PersistenceManager persistence;
    private final int cfuPerLaurea;

    public GameLoop(Mappa mappa, ControllerMovimentoStanze movimento,
                    EsameController esameController, PersistenceManager persistence, int cfuPerLaurea) {
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
                    view.mostraMessaggio("Riposo in aula studio e recuperi energie.");
                }
                case ACQUISTO -> acquista(studente, scelta.tipoItem(), view);
                case USA_ITEM -> {
                    studente.usaConsumabile(scelta.consumabile());
                    view.mostraMessaggio("Usi: " + scelta.consumabile().getNome());
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

    private List<Azione> costruisciAzioni(Stanza corrente, Studente studente) {
        List<Azione> azioni = new ArrayList<>();
        if (corrente.getTipo() == TipoStanza.AULA_ESAME
                && !studente.getLibretto().getEsamiSuperati().contains(corrente.getEsame()))
            azioni.add(Azione.esame("Sostieni l'esame di " + corrente.getEsame().getNomeCorso()));
        if (corrente.getTipo() == TipoStanza.AULA_STUDIO) {
            azioni.add(Azione.riposo("Riposati (recuperi " + RECUPERO_RIPOSO + " HP)"));
            for (TipoItem tipo : TipoItem.values())
                azioni.add(Azione.acquisto(tipo,"compra: " + tipo + " (" + CatalogoItem.prezzo(tipo) + " monete)"));
        }

        for (Consumabile c : studente.getZaino())
            azioni.add(Azione.uso(c, "Usa: " + c.getNome()));

        mappa.getAdiacenti(corrente).stream()
                .sorted(Comparator.comparing(Stanza::getNome))
                .forEach(d -> azioni.add(Azione.movimento(d, "Vai a: " + d.getNome())));
        azioni.add(Azione.esci("Salva ed esci"));
        return azioni;
    }

    private void acquista(Studente studente, TipoItem tipo, GameView view) {
        int prezzo = CatalogoItem.prezzo(tipo);
        if (studente.getMonete() < prezzo) {
            view.mostraMessaggio("Monete insufficienti: servono " + prezzo + ".");
            return;
        }
        studente.spendiMonete(prezzo);
        Item item = CatalogoItem.crea(tipo);
        if (item instanceof Consumabile c) studente.aggiungiConsumabile(c);
        else if (item instanceof Equipaggiamento e) studente.equipaggiaItem(e);
        view.mostraMessaggio("Hai acquistato: " + item.getNome());
    }

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

    private void muovi(StatoGioco stato, Stanza destinazione, GameView view) {
        try {
            movimento.spostati(stato, destinazione);
        } catch (PrerequisitiNonRispettatiException e) {
            view.mostraMessaggio(e.getMessage());
        }
    }

    private enum Tipo { ESAME, RIPOSO, ACQUISTO, USA_ITEM, MOVIMENTO, ESCI }

    private record Azione(String etichetta, Tipo tipo,
                          Stanza destinazione, TipoItem tipoItem, Consumabile consumabile) {
        static Azione esame(String e)                 { return new Azione(e, Tipo.ESAME, null, null, null); }
        static Azione riposo(String e)                { return new Azione(e, Tipo.RIPOSO, null, null, null); }
        static Azione esci(String e)                  { return new Azione(e, Tipo.ESCI, null, null, null); }
        static Azione movimento(Stanza d, String e)   { return new Azione(e, Tipo.MOVIMENTO, d, null, null); }
        static Azione acquisto(TipoItem t, String e)  { return new Azione(e, Tipo.ACQUISTO, null, t, null); }
        static Azione uso(Consumabile c, String e)    { return new Azione(e, Tipo.USA_ITEM, null, null, c); }
    }
}