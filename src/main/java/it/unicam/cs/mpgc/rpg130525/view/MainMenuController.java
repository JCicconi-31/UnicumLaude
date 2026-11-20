package it.unicam.cs.mpgc.rpg130525.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Schermata del menù principale: titolo, lo studente protagonista e un pannello
 * in cui vengono renderizzate le scelte e i campi di testo richiesti dal motore
 * (nuova partita, caricamento, nome del giocatore, carriera). Non contiene
 * logica di gioco: mostra ciò che il motore chiede tramite i contratti e
 * restituisce le scelte via callback.
 */
public class MainMenuController {

    private static final String INDIETRO = "Indietro";

    private final StackPane root = new StackPane();
    private final Label messaggio = StileRetro.testo("", "messaggio-menu");
    private final VBox areaInterazione = new VBox(10);
    private final Personaggio protagonista = Personaggio.studente().animato();

    public MainMenuController() {
        root.getStyleClass().add("sfondo-gioco");

        Label titolo = StileRetro.testo("UNICUM LAUDE", "titolo-gioco");
        Label sottotitolo = StileRetro.testo("un RPG universitario", "sottotitolo");

        areaInterazione.setAlignment(Pos.CENTER);
        VBox pannello = new VBox(12, messaggio, areaInterazione);
        pannello.setAlignment(Pos.CENTER);
        pannello.getStyleClass().add("pannello-scuro");
        pannello.setPrefWidth(540);
        pannello.setMaxWidth(540);

        protagonista.scala(1.6);
        protagonista.setLayoutX(110);
        protagonista.setLayoutY(250);
        Pane palco = new Pane(stelle(), protagonista);
        palco.setPrefSize(220, 270);
        palco.setMinSize(220, 270);

        HBox centro = new HBox(30, palco, pannello);
        centro.setAlignment(Pos.CENTER);

        VBox colonna = new VBox(18, titolo, sottotitolo, centro);
        colonna.setAlignment(Pos.CENTER);
        colonna.setPadding(new Insets(30));
        root.getChildren().add(colonna);
    }

    public Parent getRoot() {
        return root;
    }

    /** Mostra un messaggio informativo sopra le scelte. */
    public void mostraMessaggio(String testo) {
        messaggio.setText(testo);
    }

    /**
     * Renderizza un elenco di scelte come bottoni del menù.
     *
     * @param onScelta callback invocata con l'indice (in base 0) dell'opzione scelta
     */
    public void mostraScelte(String titolo, List<String> opzioni, IntConsumer onScelta) {
        areaInterazione.getChildren().setAll(StileRetro.testo(titolo, "prompt"));
        for (int i = 0; i < opzioni.size(); i++) {
            final int indice = i;
            Button bottone = opzioni.get(i).equals(INDIETRO) ? bottoneIndietro() : StileRetro.bottone(opzioni.get(i));
            bottone.setMaxWidth(Double.MAX_VALUE);
            bottone.setOnAction(e -> {
                areaInterazione.getChildren().clear();
                if (opzioni.get(indice).startsWith("Nuova")) protagonista.esulta();
                onScelta.accept(indice);
            });
            areaInterazione.getChildren().add(bottone);
        }
        if (areaInterazione.getChildren().size() > 1)
            areaInterazione.getChildren().get(1).requestFocus();
    }

    /**
     * Renderizza una richiesta di testo libero (es. nome del giocatore).
     *
     * @param onTesto callback invocata con il testo inserito, mai vuoto
     */
    public void chiediTesto(String prompt, Consumer<String> onTesto) {
        chiediTesto(prompt, onTesto, null);
    }

    /**
     * Come {@link #chiediTesto(String, Consumer)}, con in più un bottone
     * "Indietro" se {@code onIndietro} non è null.
     */
    public void chiediTesto(String prompt, Consumer<String> onTesto, Runnable onIndietro) {
        TextField campo = new TextField();
        campo.getStyleClass().add("campo-testo");
        Button conferma = StileRetro.bottone("OK");
        conferma.setMaxWidth(Double.MAX_VALUE);
        conferma.setOnAction(e -> {
            String inserito = campo.getText().trim();
            if (!inserito.isBlank()) {
                areaInterazione.getChildren().clear();
                onTesto.accept(inserito);
            }
        });
        campo.setOnAction(conferma.getOnAction());
        areaInterazione.getChildren().setAll(StileRetro.testo(prompt, "prompt"), campo, conferma);
        if (onIndietro != null) {
            Button indietro = bottoneIndietro();
            indietro.setOnAction(e -> {
                areaInterazione.getChildren().clear();
                onIndietro.run();
            });
            areaInterazione.getChildren().add(indietro);
        }
        campo.requestFocus();
    }

    private static Button bottoneIndietro() {
        Button indietro = StileRetro.bottone(INDIETRO);
        indietro.getStyleClass().add("bottone-secondario");
        indietro.setMaxWidth(Double.MAX_VALUE);
        return indietro;
    }

    /** Qualche stellina dietro al protagonista. */
    private static Group stelle() {
        Group g = new Group();
        double[][] punti = {{30, 40, 3}, {190, 30, 4}, {160, 90, 2.5}, {20, 140, 2}, {200, 170, 3}, {60, 90, 2}};
        for (double[] p : punti)
            g.getChildren().add(new Circle(p[0], p[1], p[2], Color.web("#ffe9a8", 0.8)));
        return g;
    }
}
