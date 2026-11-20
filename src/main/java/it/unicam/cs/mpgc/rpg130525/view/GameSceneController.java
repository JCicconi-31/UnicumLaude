package it.unicam.cs.mpgc.rpg130525.view;

import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Schermata principale di gioco: in alto il cartellino del giocatore con la barra
 * HP, al centro la mappa e il diario dei messaggi, in basso le azioni
 * disponibili. Sopra a tutto si apre, quando serve, la {@link SchermataEsame}.
 * <p>
 * I tasti numerici 1-9 selezionano le opzioni visibili, sia sulla mappa sia
 * durante gli esami.
 */
public class GameSceneController {

    private static final int MAX_VOCI_DIARIO = 60;

    private final StackPane rootConOverlay;
    private final MapViewController mappa;
    private final SchermataEsame esame = new SchermataEsame();

    private final Label nomeGiocatore = StileRetro.testo("", "nome-giocatore");
    private final BarraHp hpGiocatore = new BarraHp("HP", 200);
    private final Label intelligenza = StileRetro.testo("", "chip");
    private final Label resilienza = StileRetro.testo("", "chip");
    private final Label cfu = StileRetro.testo("", "chip");
    private final Label monete = StileRetro.testo("", "chip");

    private final VBox vociDiario = new VBox(2);
    private final ScrollPane diario = new ScrollPane(vociDiario);
    private final Label prompt = StileRetro.testo("", "prompt");
    private final FlowPane pannelloRisposte = new FlowPane(10, 10);
    private final List<Button> bottoniAzione = new ArrayList<>();

    public GameSceneController(MapViewController mappa) {
        if (mappa == null)
            throw new IllegalArgumentException("mappa nulla");
        this.mappa = mappa;

        BorderPane schermata = new BorderPane();
        schermata.getStyleClass().add("sfondo-gioco");
        schermata.setTop(costruisciHud());
        schermata.setCenter(costruisciCentro());
        schermata.setBottom(costruisciAzioni());

        rootConOverlay = new StackPane(schermata, esame.getRoot());
        appendiLog("Inizia la tua carriera universitaria!");
        rootConOverlay.setOnKeyPressed(e -> {
            KeyCode codice = e.getCode();
            if (!codice.isDigitKey()) return;
            int indice = codice.getName().charAt(codice.getName().length() - 1) - '1';
            List<Button> attivi = esame.isAperta() ? esame.bottoniCorrenti() : bottoniAzione;
            if (indice >= 0 && indice < attivi.size()) {
                attivi.get(indice).fire();
                e.consume();
            }
        });
    }

    public Parent getRoot() {
        return rootConOverlay;
    }

    public MapViewController mappa() {
        return mappa;
    }

    SchermataEsame esame() {
        return esame;
    }

    // ---------------------------------------------------------------- costruzione

    private Region costruisciHud() {
        Personaggio ritratto = Personaggio.studente().animato();
        ritratto.scala(0.5);
        // si mostra solo la testa: il ritratto è ritagliato in un cerchio
        Pane cornice = new Pane(new Group(ritratto));
        ritratto.setLayoutX(26);
        ritratto.setLayoutY(78);
        cornice.setPrefSize(52, 52);
        cornice.setMinSize(52, 52);
        cornice.setMaxSize(52, 52);
        cornice.setClip(new Circle(26, 26, 26));
        cornice.setStyle("-fx-background-color: #cfe8ff; -fx-background-radius: 26;");

        VBox identita = new VBox(4, nomeGiocatore, hpGiocatore);
        identita.setPrefWidth(320);
        HBox statistiche = new HBox(8, intelligenza, resilienza, cfu, monete);
        statistiche.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(statistiche, Priority.ALWAYS);

        HBox hud = new HBox(14, cornice, identita, statistiche);
        hud.setAlignment(Pos.CENTER_LEFT);
        hud.getStyleClass().add("pannello");
        BorderPane.setMargin(hud, new Insets(12, 12, 8, 12));
        return hud;
    }

    private Region costruisciCentro() {
        StackPane riquadroMappa = new StackPane(mappa.getRoot());
        riquadroMappa.setMinSize(0, 0);

        Label titoloDiario = StileRetro.testo("Diario", "diario-titolo");
        diario.getStyleClass().add("diario");
        diario.setFitToWidth(true);
        diario.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        vociDiario.heightProperty().addListener((o, a, b) -> diario.setVvalue(1.0));
        VBox.setVgrow(diario, Priority.ALWAYS);
        VBox pannelloDiario = new VBox(6, titoloDiario, diario);
        pannelloDiario.getStyleClass().add("pannello");
        pannelloDiario.setPrefWidth(280);
        pannelloDiario.setMinWidth(240);
        pannelloDiario.setMaxWidth(300);

        HBox centro = new HBox(12, riquadroMappa, pannelloDiario);
        centro.setPadding(new Insets(0, 12, 0, 12));
        HBox.setHgrow(riquadroMappa, Priority.ALWAYS);
        return centro;
    }

    private Region costruisciAzioni() {
        prompt.setWrapText(true);
        pannelloRisposte.setAlignment(Pos.CENTER_LEFT);
        VBox zona = new VBox(8, prompt, pannelloRisposte);
        zona.getStyleClass().add("pannello-scuro");
        zona.setMinHeight(96);
        BorderPane.setMargin(zona, new Insets(8, 12, 12, 12));
        return zona;
    }

    // ------------------------------------------------------------- aggiornamenti

    public void appendiLog(String messaggio) {
        vociDiario.getChildren().forEach(n -> n.getStyleClass().remove("voce-diario-nuova"));
        Label voce = StileRetro.testo("› " + messaggio, "voce-diario", "voce-diario-nuova");
        voce.setWrapText(true);
        voce.setMaxWidth(Double.MAX_VALUE);
        vociDiario.getChildren().add(voce);
        if (vociDiario.getChildren().size() > MAX_VOCI_DIARIO)
            vociDiario.getChildren().remove(0);
    }

    public void aggiornaGiocatore(StudenteDto s) {
        boolean prima = nomeGiocatore.getText().isEmpty();
        nomeGiocatore.setText(s.nomeCompleto());
        hpGiocatore.imposta(s.saluteMentale(), s.saluteMentaleMax(), !prima);
        intelligenza.setText("INT " + s.intelligenzaEffettiva());
        resilienza.setText("RES " + s.resilienzaEffettiva());
        cfu.setText("CFU " + s.cfu());
        monete.setText("Monete " + s.monete());
    }

    public void mostraBottoni(List<String> opzioni, IntConsumer onScelta) {
        pulisciBottoni();
        for (int i = 0; i < opzioni.size(); i++) {
            final int indice = i;
            Button bottone = StileRetro.bottone(opzioni.get(i));
            if (i < 9) bottone.setGraphic(StileRetro.testo(String.valueOf(i + 1), "bottone-numero"));
            bottone.setOnAction(e -> {
                pulisciRisposte();
                onScelta.accept(indice);
            });
            bottoniAzione.add(bottone);
            pannelloRisposte.getChildren().add(bottone);
        }
        rootConOverlay.requestFocus();
    }

    public void impostaPrompt(String testo) {
        prompt.setText(testo);
    }

    public void chiediTesto(String richiesta, Consumer<String> onTesto) {
        pulisciRisposte();
        impostaPrompt(richiesta);
        TextField campo = new TextField();
        campo.getStyleClass().add("campo-testo");
        Button conferma = StileRetro.bottone("OK");
        conferma.setOnAction(e -> {
            String inserito = campo.getText().trim();
            if (!inserito.isBlank()) {
                pulisciRisposte();
                onTesto.accept(inserito);
            }
        });
        campo.setOnAction(conferma.getOnAction());
        pannelloRisposte.getChildren().addAll(campo, conferma);
        campo.requestFocus();
    }

    public void pulisciRisposte() {
        pulisciBottoni();
        prompt.setText("");
    }

    private void pulisciBottoni() {
        pannelloRisposte.getChildren().clear();
        bottoniAzione.clear();
    }

    public void mostraIstruzioni() {
        Label titolo = StileRetro.testo("Come si gioca", "prompt");
        titolo.setStyle("-fx-font-size: 24px;");
        Label testo = StileRetro.testo("""
                Supera tutti gli esami del polo e raggiungi i CFU necessari \
                alla laurea per scoprire il leggendario UNICUM!

                • Muoviti cliccando le stanze sulla mappa o con i bottoni in basso \
                (oppure con i tasti 1-9).
                • Stanze gialle: disponibili. Grigie col lucchetto: bloccate dalle \
                propedeuticità. Verdi con la spunta: esami superati.
                • Edificio = aula d'esame: prova scritta (quiz) e prova orale \
                (sfida a colpi di domande col professore).
                • Tazza = aula studio: riposa per recuperare HP e compra oggetti utili.
                • Gli HP sono la tua salute mentale: se arrivano a 0 è BURNOUT e la \
                partita finisce.
                • Con "Salva ed esci" riprendi la partita la prossima volta.

                In bocca al lupo, matricola!""", "testo-chiaro");
        testo.setWrapText(true);
        Button chiudi = StileRetro.bottone("Ho capito");

        Personaggio guida = Personaggio.studente().animato();
        guida.setEspressione(Personaggio.Espressione.FELICE);
        Pane spazioGuida = new Pane(guida);
        guida.setLayoutX(50);
        guida.setLayoutY(150);
        spazioGuida.setPrefSize(100, 160);
        spazioGuida.setMinWidth(100);

        VBox colonna = new VBox(14, titolo, testo, chiudi);
        HBox pannello = new HBox(18, spazioGuida, colonna);
        pannello.setAlignment(Pos.CENTER_LEFT);
        pannello.getStyleClass().add("pannello-scuro");
        pannello.setPadding(new Insets(24));
        pannello.setMaxWidth(720);
        pannello.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane velo = new StackPane(pannello);
        velo.getStyleClass().add("velo");
        chiudi.setOnAction(e -> {
            guida.ferma();
            rootConOverlay.getChildren().remove(velo);
            rootConOverlay.requestFocus();
        });
        rootConOverlay.getChildren().add(velo);
        chiudi.requestFocus();
    }
}
