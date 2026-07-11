package it.unicam.cs.mpgc.rpg130525.app;

import it.unicam.cs.mpgc.rpg130525.view.GameSceneController;
import it.unicam.cs.mpgc.rpg130525.view.JavaFxView;
import it.unicam.cs.mpgc.rpg130525.view.MainMenuController;
import it.unicam.cs.mpgc.rpg130525.view.MapViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

/**
 * Avvio dell'interfaccia grafica JavaFX: assembla le schermate (menù principale
 * e scena di gioco), crea la {@link JavaFxView} come unica implementazione dei
 * contratti di I/O e fa girare la partita su un thread dedicato, così
 * l'interfaccia utente resta reattiva.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic, tramite Claude Code), come
 * previsto dalle indicazioni del corso.
 */
public class JavaFxApp extends Application {

    @Override
    public void start(Stage stage) {
        MainMenuController menu = new MainMenuController();
        GameSceneController gioco = new GameSceneController(new MapViewController());
        Scene scena = new Scene(menu.getRoot(), 1020, 640);
        JavaFxView view = new JavaFxView(menu, gioco, () -> scena.setRoot(gioco.getRoot()));

        stage.setScene(scena);
        stage.setTitle("Unicum Laude");
        stage.show();

        Thread motore = new Thread(() -> {
            try {
                Partita.esegui(view, view);
            } catch (Exception e) {
                view.mostraMessaggio("Errore imprevisto: " + e.getMessage());
            }
            // partita finita (uscita, laurea o burnout): un'ultima conferma per
            // lasciar leggere i messaggi finali, poi si chiude la finestra
            view.scegli("Partita terminata", List.of("Chiudi il gioco"));
            Platform.exit();
        }, "motore-di-gioco");
        motore.setDaemon(true);
        motore.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
