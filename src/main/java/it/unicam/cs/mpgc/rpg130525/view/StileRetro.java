package it.unicam.cs.mpgc.rpg130525.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Utilità di presentazione che centralizza lo stile pixel-art 8-bit condiviso
 * dalle schermate JavaFX (bottoni, etichette, colori), così il look retrò
 * resta coerente e definito in un unico punto.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic, tramite Claude Code), come
 * previsto dalle indicazioni del corso.
 */
final class StileRetro {

    static final String FONT_RETRO = "Monospaced";
    static final String SFONDO_SCURO = "#182038";

    private static final String STILE_BOTTONE = """
            -fx-background-color: #f8c810;
            -fx-text-fill: #3a2000;
            -fx-font-family: 'Monospaced';
            -fx-font-weight: bold;
            -fx-font-size: 14;
            -fx-background-radius: 0;
            -fx-border-radius: 0;
            -fx-border-color: #3a2000;
            -fx-border-width: 3;
            -fx-padding: 6 14 6 14;
            """;

    private static final String STILE_BOTTONE_HOVER = STILE_BOTTONE
            .replace("#f8c810", "#ffe060");

    private StileRetro() {
    }

    /** Crea un bottone in stile 8-bit (bordo netto, niente angoli arrotondati). */
    static Button bottone(String testo) {
        Button bottone = new Button(testo);
        bottone.setStyle(STILE_BOTTONE);
        bottone.setMaxWidth(Double.MAX_VALUE);
        bottone.setOnMouseEntered(e -> bottone.setStyle(STILE_BOTTONE_HOVER));
        bottone.setOnMouseExited(e -> bottone.setStyle(STILE_BOTTONE));
        return bottone;
    }

    /** Crea un'etichetta "chip" con sfondo scuro, usata per i nomi sulla mappa. */
    static Label chip(String testo) {
        Label chip = new Label(testo);
        chip.setStyle("""
                -fx-background-color: rgba(0,0,0,0.65);
                -fx-text-fill: white;
                -fx-font-family: 'Monospaced';
                -fx-font-weight: bold;
                -fx-font-size: 10;
                -fx-padding: 1 5 1 5;
                """);
        return chip;
    }

    /** Crea un'etichetta di testo chiaro per gli sfondi scuri. */
    static Label testoChiaro(String testo, int dimensione) {
        Label label = new Label(testo);
        label.setStyle("-fx-text-fill: #e8e8f8; -fx-font-family: 'Monospaced'; -fx-font-size: " + dimensione + ";");
        return label;
    }
}
