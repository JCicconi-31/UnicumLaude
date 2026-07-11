package it.unicam.cs.mpgc.rpg130525.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Schermata del menù principale in stile retro: titolo del gioco, messaggi di
 * benvenuto e area d'interazione in cui vengono renderizzate le scelte e i
 * campi di testo richiesti dal motore (nuova partita, caricamento, nome del
 * giocatore, carriera). Non contiene logica di gioco: mostra ciò che il motore
 * chiede tramite i contratti e restituisce le scelte via callback.
 */
public class MainMenuController {

    private final StackPane root = new StackPane();
    private final Label messaggio = StileRetro.testoChiaro("", 14);
    private final VBox areaInterazione = new VBox(10);

    public MainMenuController() {
        root.setStyle("-fx-background-color: " + StileRetro.SFONDO_SCURO + ";");

        Label titolo = new Label("UNICUM LAUDE");
        titolo.setFont(Font.font(StileRetro.FONT_RETRO, FontWeight.BOLD, 52));
        titolo.setTextFill(Color.web("#f8c810"));
        DropShadow ombraPixel = new DropShadow(0, 5, 5, Color.web("#b02010"));
        titolo.setEffect(ombraPixel);

        Label sottotitolo = StileRetro.testoChiaro("~ un RPG universitario ~", 16);

        areaInterazione.setAlignment(Pos.CENTER);
        areaInterazione.setMaxWidth(660);

        VBox colonna = new VBox(22, titolo, sottotitolo, messaggio, areaInterazione);
        colonna.setAlignment(Pos.CENTER);
        colonna.setPadding(new Insets(40));
        root.getChildren().add(colonna);
    }

    public Parent getRoot() {
        return root;
    }

    /** Mostra un messaggio informativo sotto il titolo. */
    public void mostraMessaggio(String testo) {
        messaggio.setText(testo);
    }

    /**
     * Renderizza un elenco di scelte come bottoni del menù.
     *
     * @param onScelta callback invocata con l'indice (in base 0) dell'opzione scelta
     */
    public void mostraScelte(String titolo, List<String> opzioni, IntConsumer onScelta) {
        areaInterazione.getChildren().setAll(StileRetro.testoChiaro(titolo, 15));
        for (int i = 0; i < opzioni.size(); i++) {
            final int indice = i;
            Button bottone = StileRetro.bottone(opzioni.get(i));
            bottone.setOnAction(e -> {
                areaInterazione.getChildren().clear();
                onScelta.accept(indice);
            });
            areaInterazione.getChildren().add(bottone);
        }
    }

    /**
     * Renderizza una richiesta di testo libero (es. nome del giocatore).
     *
     * @param onTesto callback invocata con il testo inserito, mai vuoto
     */
    public void chiediTesto(String prompt, Consumer<String> onTesto) {
        TextField campo = new TextField();
        campo.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 14; -fx-background-radius: 0;");
        Button conferma = StileRetro.bottone("OK");
        conferma.setOnAction(e -> {
            String inserito = campo.getText().trim();
            if (!inserito.isBlank()) {
                areaInterazione.getChildren().clear();
                onTesto.accept(inserito);
            }
        });
        campo.setOnAction(conferma.getOnAction());
        areaInterazione.getChildren().setAll(StileRetro.testoChiaro(prompt, 15), campo, conferma);
        campo.requestFocus();
    }
}
