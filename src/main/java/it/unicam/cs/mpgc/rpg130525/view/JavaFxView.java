package it.unicam.cs.mpgc.rpg130525.view;

import it.unicam.cs.mpgc.rpg130525.model.Domanda;
import it.unicam.cs.mpgc.rpg130525.port.*;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Adapter di {@link GameView} e {@link GameInput} per JavaFX: instrada le
 * richieste del motore verso la schermata giusta (menù principale finché la
 * partita non inizia, schermata di gioco dopo il primo aggiornamento della
 * mappa) e raccoglie le scelte del giocatore in modo asincrono tramite
 * {@code CompletableFuture}, tenendo il motore su un thread separato. Il flusso
 * dei dati resta unidirezionale: il motore chiama, la view renderizza.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic, tramite Claude Code), come
 * previsto dalle indicazioni del corso.
 */
public class JavaFxView implements GameView, GameInput {

    private final MainMenuController menu;
    private final GameSceneController gioco;
    private final Runnable passaAllaSchermataDiGioco;

    private boolean partitaAvviata = false;
    private List<String> opzioniPendenti;
    private CompletableFuture<Integer> sceltaPendente;

    public JavaFxView(MainMenuController menu, GameSceneController gioco, Runnable passaAllaSchermataDiGioco) {
        if (menu == null || gioco == null || passaAllaSchermataDiGioco == null)
            throw new IllegalArgumentException("componenti della view nulli");
        this.menu = menu;
        this.gioco = gioco;
        this.passaAllaSchermataDiGioco = passaAllaSchermataDiGioco;
    }

    @Override
    public void mostraMessaggio(String messaggio) {
        Platform.runLater(() -> {
            if (partitaAvviata) gioco.appendiLog(messaggio);
            else menu.mostraMessaggio(messaggio);
        });
    }

    @Override
    public void aggiornaStatoGiocatore(StudenteDto studente) {
        Platform.runLater(() -> gioco.impostaStatoGiocatore(FormattatoreStato.formatta(studente)));
    }

    @Override
    public void aggiornaStatoProfessore(ProfessoreDto professore) {
        Platform.runLater(() -> gioco.aggiornaProfessore(professore));
    }

    @Override
    public void aggiornaMappa(List<StanzaDto> stanze, String posizioneCorrente) {
        Platform.runLater(() -> {
            if (!partitaAvviata) {
                partitaAvviata = true;
                passaAllaSchermataDiGioco.run();
                gioco.mostraIstruzioni();
            }
            gioco.mappa().render(stanze, posizioneCorrente, this::clickSuStanza);
        });
    }

    @Override
    public int chiediRisposta(Domanda domanda) {
        CompletableFuture<Integer> scelta = new CompletableFuture<>();
        Platform.runLater(() -> {
            gioco.mostraBottoni(domanda.getOpzioni(), scelta::complete);
            gioco.impostaPrompt(domanda.getTesto()); // la domanda sta accanto alle risposte
        });
        return scelta.join();
    }

    @Override
    public int scegli(String titolo, List<String> opzioni) {
        CompletableFuture<Integer> scelta = new CompletableFuture<>();
        Platform.runLater(() -> {
            opzioniPendenti = opzioni;
            sceltaPendente = scelta;
            if (partitaAvviata) {
                gioco.mostraBottoni(opzioni, scelta::complete);
                gioco.impostaPrompt(titolo);
            } else
                menu.mostraScelte(titolo, opzioni, scelta::complete);
        });
        return scelta.join();
    }

    @Override
    public String chiediTesto(String prompt) {
        CompletableFuture<String> testo = new CompletableFuture<>();
        Platform.runLater(() -> {
            if (partitaAvviata) gioco.chiediTesto(prompt, testo::complete);
            else menu.chiediTesto(prompt, testo::complete);
        });
        return testo.join();
    }

    /** Il click su una stanza della mappa equivale a scegliere "Vai a: nome". */
    private void clickSuStanza(String nome) {
        if (sceltaPendente == null || sceltaPendente.isDone()) return;
        int indice = opzioniPendenti.indexOf("Vai a: " + nome);
        if (indice < 0) return;
        gioco.pulisciRisposte();
        sceltaPendente.complete(indice);
    }
}
