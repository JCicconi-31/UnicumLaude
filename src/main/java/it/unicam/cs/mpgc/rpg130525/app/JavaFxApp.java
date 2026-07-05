package it.unicam.cs.mpgc.rpg130525.app;

import it.unicam.cs.mpgc.rpg130525.view.JavaFxView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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
        root.setCenter(log);
        root.setBottom(pannelloRisposte);
        BorderPane.setMargin(statoGiocatore, new Insets(10));

        stage.setScene(new Scene(root, 640, 480));
        stage.setTitle("UniCum Laude");
        stage.show();

        JavaFxView view = new JavaFxView(log, statoGiocatore, pannelloRisposte);
        Thread motore = new Thread(() -> Partita.esegui(view, view), "motore-di-gioco");
        motore.setDaemon(true);
        motore.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
