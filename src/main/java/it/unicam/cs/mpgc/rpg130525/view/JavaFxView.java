package it.unicam.cs.mpgc.rpg130525.view;

import it.unicam.cs.mpgc.rpg130525.model.Domanda;
import it.unicam.cs.mpgc.rpg130525.port.*;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class JavaFxView implements GameView, GameInput {
    private static final Map<String, Point2D> POSIZIONI = Map.of(
            "Atrio",       new Point2D(150, 190),
            "Aula Studio", new Point2D(60, 90),
            "Aula LA1",    new Point2D(150, 40),
            "Aula LA2",    new Point2D(240, 90));

    private final TextArea log;
    private final Label statoGiocatore;
    private final VBox pannelloRisposte;
    private final Label nomeProfessore;
    private final ProgressBar barraProfessore;
    private final HBox rigaProfessore;
    private final Pane pannelloMappa;

    private List<String> opzioniPendenti;
    private CompletableFuture<Integer> sceltaPendente;

    public JavaFxView(TextArea log, Label statoGiocatore, VBox pannelloRisposte,
                      Label nomeProfessore, ProgressBar barraProfessore, HBox rigaProfessore,
                      Pane pannelloMappa) {
        if (log == null || statoGiocatore == null || pannelloRisposte == null
                || nomeProfessore == null || barraProfessore == null || rigaProfessore == null
                || pannelloMappa == null)
            throw new IllegalArgumentException("componenti grafici nulli");
        this.log = log;
        this.statoGiocatore = statoGiocatore;
        this.pannelloRisposte = pannelloRisposte;
        this.nomeProfessore = nomeProfessore;
        this.barraProfessore = barraProfessore;
        this.rigaProfessore = rigaProfessore;
        this.pannelloMappa = pannelloMappa;
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
        Platform.runLater(() -> {
            pannelloMappa.getChildren().clear();
            for (StanzaDto st : stanze) {
                Point2D da = POSIZIONI.get(st.nome());
                if (da == null) continue;
                for (String vicina : st.adiacenti()) {
                    Point2D a = POSIZIONI.get(vicina);
                    if (a == null) continue;
                    Line corridoio = new Line(da.getX(), da.getY(), a.getX(), a.getY());
                    corridoio.setStroke(Color.DARKGRAY);
                    pannelloMappa.getChildren().add(corridoio);
                }
            }
            for (StanzaDto st : stanze) {
                Point2D p = POSIZIONI.get(st.nome());
                if (p == null) continue;
                Circle nodo = new Circle(p.getX(), p.getY(), 18, coloreDi(st.stato()));
                nodo.setStroke(Color.BLACK);
                nodo.setOnMouseClicked(e -> clickSuStanza(st.nome()));

                Label etichetta = new Label(st.nome());
                etichetta.setLayoutX(p.getX() - 30);
                etichetta.setLayoutY(p.getY() + 22);

                pannelloMappa.getChildren().addAll(nodo, etichetta);

                if (st.nome().equals(posizioneCorrente)) {
                    Circle giocatore = new Circle(p.getX(), p.getY(), 7, Color.ROYALBLUE);
                    giocatore.setMouseTransparent(true);
                    pannelloMappa.getChildren().add(giocatore);
                }
            }
        });
    }

    private Color coloreDi(StanzaDto.Stato stato) {
        return switch (stato) {
            case BLOCCATA    -> Color.LIGHTGRAY;
            case DISPONIBILE -> Color.GOLD;
            case SUPERATA    -> Color.LIGHTGREEN;
        };
    }

    private void clickSuStanza(String nome) {
        if (sceltaPendente == null || sceltaPendente.isDone())
            return;
        int indice = opzioniPendenti.indexOf("Vai a: " + nome);
        if (indice < 0)
            return;
        pannelloRisposte.getChildren().clear();
        sceltaPendente.complete(indice);
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
            opzioniPendenti = opzioni;
            sceltaPendente = scelta;
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
