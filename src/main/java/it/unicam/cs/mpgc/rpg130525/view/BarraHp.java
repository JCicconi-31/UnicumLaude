package it.unicam.cs.mpgc.rpg130525.view;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Barra dei punti vita arrotondata: si svuota con un'animazione e cambia
 * colore (verde, giallo, rosso) in base alla percentuale rimasta.
 */
final class BarraHp extends HBox {

    private final Pane traccia = new Pane();
    private final Rectangle riempimento = new Rectangle();
    private final Label valore = new Label();
    private double frazione = 1.0;

    BarraHp(String etichetta, double larghezza) {
        super(8);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("barra-hp");

        Label sigla = StileRetro.testo(etichetta, "barra-hp-sigla");
        sigla.setMinWidth(Label.USE_PREF_SIZE);
        traccia.getStyleClass().add("barra-hp-traccia");
        traccia.setPrefSize(larghezza, 14);
        traccia.setMinSize(60, 14);
        traccia.setMaxHeight(14);
        HBox.setHgrow(traccia, Priority.ALWAYS);

        riempimento.setHeight(8);
        riempimento.setLayoutX(3);
        riempimento.setLayoutY(3);
        riempimento.setArcWidth(8);
        riempimento.setArcHeight(8);
        riempimento.getStyleClass().add("barra-hp-piena");
        traccia.getChildren().add(riempimento);
        traccia.widthProperty().addListener((o, a, b) -> riempimento.setWidth(larghezzaPiena(frazione)));

        valore.getStyleClass().add("barra-hp-valore");
        valore.setMinWidth(Label.USE_PREF_SIZE);
        valore.setPrefWidth(62);
        getChildren().addAll(sigla, traccia, valore);
    }

    /** Aggiorna la barra; se {@code animata} il riempimento scorre fino al nuovo valore. */
    void imposta(int hp, int hpMax, boolean animata) {
        frazione = hpMax <= 0 ? 0 : Math.max(0, Math.min(1, hp / (double) hpMax));
        valore.setText(hp + "/" + hpMax);
        riempimento.getStyleClass().removeAll("hp-alto", "hp-medio", "hp-basso");
        riempimento.getStyleClass().add(frazione > 0.5 ? "hp-alto" : frazione > 0.2 ? "hp-medio" : "hp-basso");
        double destinazione = larghezzaPiena(frazione);
        if (!animata) {
            riempimento.setWidth(destinazione);
            return;
        }
        new Timeline(new KeyFrame(Duration.millis(450),
                new KeyValue(riempimento.widthProperty(), destinazione))).play();
    }

    private double larghezzaPiena(double f) {
        return Math.max(0, (traccia.getWidth() > 0 ? traccia.getWidth() : traccia.getPrefWidth()) - 6) * f;
    }
}
