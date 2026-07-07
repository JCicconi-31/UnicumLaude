package it.unicam.cs.mpgc.rpg130525.app;

import it.unicam.cs.mpgc.rpg130525.view.JavaFxView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class JavaFxApp extends Application {

    @Override
    public void start(Stage stage) {
        TextArea log = new TextArea();
        log.setEditable(false);
        log.setWrapText(true);

        Label statoGiocatore = new Label("Benvenuto in UniCum Laude!");
        VBox pannelloRisposte = new VBox(8);
        pannelloRisposte.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(statoGiocatore);
        Label nomeProfessore = new Label();
        ProgressBar barraProfessore = new ProgressBar(1.0);
        barraProfessore.setMaxWidth(Double.MAX_VALUE);
        HBox rigaProfessore = new HBox(10, nomeProfessore, barraProfessore);
        Pane pannelloMappa = new Pane();
        pannelloMappa.setPrefSize(300, 260);
        root.setRight(pannelloMappa);
        HBox.setHgrow(barraProfessore, Priority.ALWAYS);
        rigaProfessore.setVisible(false);
        VBox pannelloSuperiore = new VBox(5, statoGiocatore, rigaProfessore);
        pannelloSuperiore.setPadding(new Insets(10));
        root.setTop(pannelloSuperiore);
        barraProfessore.setStyle("-fx-accent: #de2410;");
        root.setCenter(log);
        root.setBottom(pannelloRisposte);
        BorderPane.setMargin(statoGiocatore, new Insets(10));

        stage.setScene(new Scene(root, 640, 480));
        stage.setTitle("UniCum Laude");
        stage.show();

        JavaFxView view = new JavaFxView(log, statoGiocatore, pannelloRisposte, nomeProfessore, barraProfessore, rigaProfessore, pannelloMappa);
        Thread motore = new Thread(() -> {
            Partita.esegui(view, view);
            // partita finita (salvataggio, laurea o burnout): un'ultima conferma per lasciar leggere i messaggi finali, poi si chiude la finestra
            view.scegli("Partita terminata", java.util.List.of("Chiudi il gioco"));
            Platform.exit();
        }, "motore-di-gioco");
        motore.setDaemon(true);
        motore.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
