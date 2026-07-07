package it.unicam.cs.mpgc.rpg130525.view;

import it.unicam.cs.mpgc.rpg130525.model.Domanda;
import it.unicam.cs.mpgc.rpg130525.port.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JavaFxView implements GameView, GameInput {
    private final TextArea log;
    private final Label statoGiocatore;
    private final VBox pannelloRisposte;
    private final Label nomeProfessore;
    private final ProgressBar barraProfessore;
    private final HBox rigaProfessore;

    public JavaFxView(TextArea log, Label statoGiocatore, VBox pannelloRisposte, Label nomeProfessore, ProgressBar barraProfessore, HBox rigaProfessore) {
        if (log == null || statoGiocatore == null || pannelloRisposte == null || nomeProfessore == null || barraProfessore == null || rigaProfessore == null)
            throw new IllegalArgumentException("componenti grafici nulli");
        this.log = log;
        this.statoGiocatore = statoGiocatore;
        this.pannelloRisposte = pannelloRisposte;
        this.nomeProfessore = nomeProfessore;
        this.barraProfessore = barraProfessore;
        this.rigaProfessore = rigaProfessore;
    }

    @Override
    public void mostraMessaggio(String messaggio) {
        Platform.runLater(() -> log.appendText(messaggio + "\n"));
    }

    @Override
    public void aggiornaStatoGiocatore(StudenteDto s) {
        Platform.runLater(() -> statoGiocatore.setText(String.format(
                "[%s] HP %d/%d | INTELLIGENZA %d | RESILIENZA %d | CFU %d | Monete %d",
                s.nomeCompleto(), s.saluteMentale(), s.saluteMentaleMax(),
                s.intelligenzaEffettiva(), s.resilienzaEffettiva(),
                s.cfu(), s.monete())));
    }

    @Override
    public void aggiornaStatoProfessore(ProfessoreDto professoreDto) {
        Platform.runLater(() -> {
            nomeProfessore.setText("Prof. " + professoreDto.nome() + "  " + professoreDto.hp() + "/" + professoreDto.hpMax());
            barraProfessore.setProgress(professoreDto.hp() / (double) professoreDto.hpMax());
            rigaProfessore.setVisible(professoreDto.hp() > 0);
        });
    }

    @Override
    public void aggiornaMappa(List<StanzaDto> stanze, String posizioneCorrente) {
        // la schermata mappa renderizzerà qui i nodi del polo
    }

    @Override
    public int chiediRisposta(Domanda domanda) {
        CompletableFuture<Integer> scelta = new CompletableFuture<>();
        Platform.runLater(() -> {
            log.appendText(domanda.getTesto() + "\n");
            mostraBottoni(domanda.getOpzioni(), scelta);
        });
        return scelta.join();
    }

    @Override
    public int scegli(String titolo, List<String> opzioni) {
        CompletableFuture<Integer> scelta = new CompletableFuture<>();
        Platform.runLater(() -> {
            log.appendText(titolo + "\n");
            mostraBottoni(opzioni, scelta);
        });
        return scelta.join();
    }

    @Override
    public String chiediTesto(String prompt) {
        CompletableFuture<String> testo = new CompletableFuture<>();
        Platform.runLater(() -> {
            pannelloRisposte.getChildren().clear();
            Label etichetta = new Label(prompt);
            TextField campo = new TextField();
            Button conferma = new Button("OK");
            conferma.setOnAction(e -> {
                String inserito = campo.getText().trim();
                if (!inserito.isBlank()) {
                    pannelloRisposte.getChildren().clear();
                    testo.complete(inserito);
                }
            });
            campo.setOnAction(conferma.getOnAction());
            pannelloRisposte.getChildren().addAll(etichetta, campo, conferma);
            campo.requestFocus();
        });
        return testo.join();
    }

    private void mostraBottoni(List<String> opzioni, CompletableFuture<Integer> scelta) {
        pannelloRisposte.getChildren().clear();
        for (int i = 0; i < opzioni.size(); i++) {
            final int indice = i;
            Button bottone = new Button((i + 1) + ") " + opzioni.get(i));
            bottone.setMaxWidth(Double.MAX_VALUE);
            bottone.setOnAction(e -> {
                pannelloRisposte.getChildren().clear();
                scelta.complete(indice);
            });
            pannelloRisposte.getChildren().add(bottone);
        }
    }
}
