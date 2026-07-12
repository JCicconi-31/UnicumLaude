package it.unicam.cs.mpgc.rpg130525.view;

import it.unicam.cs.mpgc.rpg130525.port.ProfessoreDto;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Schermata di gioco: assembla e gestisce i componenti della partita — barra di
 * stato del giocatore, barra HP del professore, mappa retro scorrevole, log
 * degli eventi e pannello delle azioni. Espone metodi di aggiornamento
 * puramente grafici, senza alcuna logica di gioco (SRP): è la
 * {@code JavaFxView} a decidere quando chiamarli in reazione al motore.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic, tramite Claude Code), come
 * previsto dalle indicazioni del corso.
 */
public class GameSceneController {

    private final StackPane rootConOverlay;
    private final TextArea log = new TextArea();
    private final Label statoGiocatore = new Label("Benvenuto in Unicum Laude!");
    private final Label prompt = new Label();
    private final FlowPane pannelloRisposte = new FlowPane(8, 8);
    private final Label nomeProfessore = new Label();
    private final ProgressBar barraProfessore = new ProgressBar(1.0);
    private final HBox rigaProfessore = new HBox(10, nomeProfessore, barraProfessore);
    private final MapViewController mappa;

    public GameSceneController(MapViewController mappa) {
        if (mappa == null)
            throw new IllegalArgumentException("mappa nulla");
        this.mappa = mappa;

        BorderPane schermata = new BorderPane();
        schermata.setStyle("-fx-background-color: " + StileRetro.SFONDO_SCURO + ";");

        statoGiocatore.setStyle("-fx-text-fill: #f8c810; -fx-font-family: 'Monospaced'; "
                + "-fx-font-weight: bold; -fx-font-size: 14;");
        nomeProfessore.setStyle("-fx-text-fill: #f89090; -fx-font-family: 'Monospaced'; -fx-font-weight: bold;");
        barraProfessore.setMaxWidth(Double.MAX_VALUE);
        barraProfessore.setStyle("-fx-accent: #de2410;");
        HBox.setHgrow(barraProfessore, Priority.ALWAYS);
        rigaProfessore.setVisible(false);
        VBox pannelloSuperiore = new VBox(6, statoGiocatore, rigaProfessore);
        pannelloSuperiore.setPadding(new Insets(10));
        schermata.setTop(pannelloSuperiore);

        log.setEditable(false);
        log.setWrapText(true);
        log.setPrefWidth(300);
        log.setMaxWidth(320);
        log.setStyle("-fx-control-inner-background: #101018; -fx-text-fill: #d8d8e8; "
                + "-fx-font-family: 'Monospaced'; -fx-font-size: 14;");

        // la mappa assorbe lo spazio extra della finestra, il log resta stretto
        HBox centro = new HBox(10, mappa.getRoot(), log);
        centro.setPadding(new Insets(0, 10, 0, 10));
        HBox.setHgrow(mappa.getRoot(), Priority.ALWAYS);
        HBox.setHgrow(log, Priority.NEVER);
        schermata.setCenter(centro);

        prompt.setStyle("-fx-text-fill: #ffe060; -fx-font-family: 'Monospaced'; "
                + "-fx-font-weight: bold; -fx-font-size: 14;");
        prompt.setWrapText(true);
        pannelloRisposte.setAlignment(Pos.CENTER_LEFT);
        VBox zonaAzioni = new VBox(6, prompt, pannelloRisposte);
        zonaAzioni.setPadding(new Insets(10));
        schermata.setBottom(zonaAzioni);

        rootConOverlay = new StackPane(schermata);
    }

    public Parent getRoot() {
        return rootConOverlay;
    }

    public MapViewController mappa() {
        return mappa;
    }

    public void appendiLog(String messaggio) {
        log.appendText(messaggio + "\n");
    }

    public void impostaStatoGiocatore(String testo) {
        statoGiocatore.setText(testo);
    }

    public void aggiornaProfessore(ProfessoreDto professore) {
        nomeProfessore.setText("Prof. " + professore.nome() + "  " + professore.hp() + "/" + professore.hpMax());
        barraProfessore.setProgress(professore.hp() / (double) professore.hpMax());
        rigaProfessore.setVisible(professore.hp() > 0);
    }

    /**
     * Mostra le opzioni come bottoni d'azione numerati; il pannello va a capo
     * automaticamente, così tutte le opzioni restano sempre visibili.
     *
     * @param onScelta callback invocata con l'indice (in base 0) dell'opzione scelta
     */
    public void mostraBottoni(List<String> opzioni, IntConsumer onScelta) {
        pannelloRisposte.getChildren().clear();
        for (int i = 0; i < opzioni.size(); i++) {
            final int indice = i;
            Button bottone = StileRetro.bottone((i + 1) + ") " + opzioni.get(i));
            bottone.setOnAction(e -> {
                pulisciRisposte();
                onScelta.accept(indice);
            });
            pannelloRisposte.getChildren().add(bottone);
        }
    }

    /** Mostra il testo della richiesta corrente accanto ai bottoni di risposta. */
    public void impostaPrompt(String testo) {
        prompt.setText(testo);
    }

    /**
     * Mostra una richiesta di testo libero nel pannello delle azioni.
     *
     * @param onTesto callback invocata con il testo inserito, mai vuoto
     */
    public void chiediTesto(String richiesta, Consumer<String> onTesto) {
        pulisciRisposte();
        impostaPrompt(richiesta);
        TextField campo = new TextField();
        campo.setStyle("-fx-font-family: 'Monospaced';");
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
        pannelloRisposte.getChildren().clear();
        prompt.setText("");
    }

    /**
     * Mostra un pannello sovrapposto con le istruzioni di gioco; resta visibile
     * finché il giocatore non lo chiude, senza bloccare il motore.
     */
    public void mostraIstruzioni() {
        Label titolo = StileRetro.testoChiaro("COME SI GIOCA", 20);
        titolo.setStyle(titolo.getStyle() + "-fx-font-weight: bold; -fx-text-fill: #f8c810;");
        Label testo = StileRetro.testoChiaro("""
                Obiettivo: supera tutti gli esami del polo e raggiungi
                i CFU necessari alla laurea per scoprire il leggendario
                UNICUM!

                - Muoviti cliccando le stanze sulla mappa o con i
                  bottoni in basso
                - Stanze GIALLE: stanze disponibili
                - Stanze GRIGIE: bloccate dalle propedeuticita'
                - Stanze VERDI: esami gia' superati
                - E = aula esame: prova scritta (quiz) + prova orale
                  (sfida a colpi di domande col professore)
                - R = aula studio: riposa per recuperare HP e compra
                  oggetti utili
                - Gli HP sono la tua salute mentale: se arrivano a 0
                  e' BURNOUT e la partita finisce!
                  Rispondere male agli esami ti costa HP
                - Con "Salva ed esci" riprendi la partita la prossima
                  volta

                In bocca al lupo matricola!""", 14);
        testo.setWrapText(true);
        Button chiudi = StileRetro.bottone("Ho capito");

        VBox pannello = new VBox(16, titolo, testo, chiudi);
        pannello.setAlignment(Pos.CENTER);
        pannello.setPadding(new Insets(24));
        pannello.setMaxWidth(620);
        pannello.setMaxHeight(Region.USE_PREF_SIZE);
        pannello.setStyle("-fx-background-color: #182038; -fx-border-color: #f8c810; -fx-border-width: 4;");

        StackPane velo = new StackPane(pannello);
        velo.setStyle("-fx-background-color: rgba(0,0,0,0.65);");
        chiudi.setOnAction(e -> rootConOverlay.getChildren().remove(velo));
        rootConOverlay.getChildren().add(velo);
    }
}
