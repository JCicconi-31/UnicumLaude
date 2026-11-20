package it.unicam.cs.mpgc.rpg130525.view;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Punto unico del tema grafico: carica i font e il foglio di stile
 * {@code unicumlaude.css}, e offre piccole fabbriche per i controlli più usati.
 * Colori, bordi e arrotondamenti stanno nel CSS, non nel codice Java.
 */
public final class StileRetro {

    static final String FOGLIO_STILE = "/css/unicumlaude.css";
    /** Nunito per i testi (leggibile), Pixelify Sans solo per il titolo del gioco. */
    private static final String[] FONT = {"/fonts/Nunito-Regular.ttf", "/fonts/Nunito-Bold.ttf",
            "/fonts/PixelifySans-Regular.ttf", "/fonts/PixelifySans-Bold.ttf"};

    private StileRetro() {
    }

    /** Carica i font e applica il foglio di stile alla scena. */
    public static void applicaA(Scene scena) {
        for (String percorso : FONT)
            try (InputStream in = StileRetro.class.getResourceAsStream(percorso)) {
                if (in != null) Font.loadFont(in, 14);
            } catch (IOException ignorata) {
                // senza i font inclusi il gioco resta utilizzabile con quello di sistema
            }
        URL css = StileRetro.class.getResource(FOGLIO_STILE);
        if (css != null)
            scena.getStylesheets().add(css.toExternalForm());
    }

    /** Bottone principale (giallo, arrotondato, con effetto "rilievo"). */
    static Button bottone(String testo) {
        Button bottone = new Button(testo);
        bottone.getStyleClass().add("bottone");
        bottone.setWrapText(true);
        return bottone;
    }

    /** Etichetta con una o più classi di stile del foglio CSS. */
    static Label testo(String testo, String... classi) {
        Label label = new Label(testo);
        label.getStyleClass().addAll(classi);
        return label;
    }
}
