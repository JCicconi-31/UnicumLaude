package it.unicam.cs.mpgc.rpg130525.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.util.List;

/**
 * Personaggio disegnato con forme vettoriali in stile "chibi" (testa grande e
 * tonda, corpo piccolo), con faccia ed espressioni intercambiabili e piccole
 * animazioni (respiro, colpo subito, esultanza).
 * <p>
 * L'origine (0,0) è ai piedi del personaggio, al centro: così è facile
 * appoggiarlo su una piattaforma o su un nodo della mappa.
 */
final class Personaggio extends Group {

    /** Espressioni che la faccia può assumere. */
    enum Espressione {NORMALE, FELICE, DOLORE, KO}

    private static final Color CONTORNO = Color.web("#2a1e16");
    private static final double SPESSORE = 2.5;

    private final Group occhiNormali = new Group();
    private final Group occhiChiusi = new Group();
    private final Group occhiDolore = new Group();
    private final Group occhiKo = new Group();
    private final Group boccaSorriso = new Group();
    private final Group boccaFelice = new Group();
    private final Group boccaDolore = new Group();
    private final Group boccaKo = new Group();
    private final Group corpo = new Group();
    private final ColorAdjust flash = new ColorAdjust();
    private Timeline respiro;
    private Timeline battito;
    private Espressione espressione = Espressione.NORMALE;

    private Personaggio() {
        corpo.setEffect(flash);
        getChildren().add(corpo);
    }

    // ------------------------------------------------------------------ fabbriche

    /** Lo studente protagonista: berretto rosso, felpa blu, zaino. */
    static Personaggio studente() {
        Personaggio p = new Personaggio();
        Color pelle = Color.web("#f6c9a0");
        p.ombra(26);
        p.gambe(Color.web("#33415e"), Color.web("#5a3a22"));
        // zaino dietro al corpo
        p.forma(rett(-30, -66, 14, 34, 7, Color.web("#e07a2e")));
        p.busto(Color.web("#3d6fd8"), 56, 44);
        p.forma(rett(-6, -60, 12, 8, 4, Color.web("#f8c810")));      // tasca della felpa
        p.braccia(Color.web("#3d6fd8"), pelle, 56);
        p.testa(pelle, 30);
        // capelli che spuntano ai lati del berretto
        p.forma(ellisse(-22, -112, 9, 7, Color.web("#5a3418")));
        p.forma(ellisse(22, -112, 9, 7, Color.web("#5a3418")));
        // berretto: calotta + visiera
        Arc calotta = new Arc(0, -112, 29, 22, 0, 180);
        calotta.setType(ArcType.CHORD);
        p.forma(contornata(calotta, Color.web("#e0322a")));
        p.forma(ellisse(14, -112, 22, 5, Color.web("#b8211b")));
        p.forma(new Circle(0, -133, 4, Color.web("#f8c810")));
        p.faccia(0, -97, 1.0);
        return p;
    }

    /**
     * Un professore il cui aspetto (capelli, barba, occhiali, giacca) è ricavato
     * in modo deterministico dal nome: lo stesso professore ha sempre lo stesso
     * aspetto, professori diversi hanno aspetti diversi.
     */
    static Personaggio professore(String nome) {
        int seme = mescola((nome == null ? "" : nome).hashCode());
        List<Color> giacche = List.of(Color.web("#6b4f8a"), Color.web("#2f6b5a"),
                Color.web("#8a3b3b"), Color.web("#45556e"), Color.web("#7a5a2a"));
        List<Color> capelli = List.of(Color.web("#d9d9d9"), Color.web("#3a2a1e"),
                Color.web("#8a8a8a"), Color.web("#6e4a2a"));
        List<Color> pelli = List.of(Color.web("#f6c9a0"), Color.web("#e8b088"), Color.web("#c98e62"));
        Color giacca = giacche.get(seme % giacche.size());
        Color capello = capelli.get((seme >>> 4) % capelli.size());
        Color pelle = pelli.get((seme >>> 8) % pelli.size());
        int pettinatura = (seme >>> 12) % 3;    // 0 calvo con lati, 1 pieni, 2 ricci
        boolean barba = ((seme >>> 16) & 1) == 0;
        boolean occhiali = (seme >>> 20) % 3 != 0;

        Personaggio p = new Personaggio();
        p.ombra(30);
        p.gambe(Color.web("#2b2b35"), Color.web("#1e1410"));
        p.busto(giacca, 64, 50);
        // camicia e cravatta
        p.forma(piena(new Polygon(-10, -76, 10, -76, 0, -58), Color.WHITE));
        p.forma(contornata(new Polygon(-4, -72, 4, -72, 6, -50, 0, -44, -6, -50),
                Color.web((seme >>> 24) % 2 == 0 ? "#d8342a" : "#e0a020")));
        p.braccia(giacca, pelle, 64);
        p.testa(pelle, 31);
        switch (pettinatura) {
            case 0 -> {
                p.forma(ellisse(-27, -104, 7, 12, capello));
                p.forma(ellisse(27, -104, 7, 12, capello));
            }
            case 1 -> {
                Arc ciuffo = new Arc(0, -110, 32, 24, 0, 180);
                ciuffo.setType(ArcType.CHORD);
                p.forma(contornata(ciuffo, capello));
            }
            default -> {
                for (int i = -2; i <= 2; i++)
                    p.forma(contornata(new Circle(i * 12, -124 + Math.abs(i) * 4, 10), capello));
            }
        }
        if (barba) {
            Arc pizzetto = new Arc(0, -88, 22, 20, 180, 180);
            pizzetto.setType(ArcType.CHORD);
            p.forma(contornata(pizzetto, capello));
        }
        // sopracciglia severe
        p.forma(linea(-17, -110, -5, -107, capello.darker(), 3));
        p.forma(linea(5, -107, 17, -110, capello.darker(), 3));
        p.faccia(0, -97, 1.0);
        if (occhiali) {
            Circle lenteSx = new Circle(-10, -98, 9, Color.web("#ffffff", 0.25));
            Circle lenteDx = new Circle(10, -98, 9, Color.web("#ffffff", 0.25));
            for (Circle l : List.of(lenteSx, lenteDx)) {
                l.setStroke(CONTORNO);
                l.setStrokeWidth(2);
            }
            p.forma(lenteSx);
            p.forma(lenteDx);
            p.forma(linea(-1, -98, 1, -98, CONTORNO, 2));
        }
        return p;
    }

    /** Il foglio della prova scritta, con faccia: l'avversario dello scritto. */
    static Personaggio foglioEsame() {
        Personaggio p = new Personaggio();
        p.ombra(30);
        Rectangle foglio = rett(-34, -120, 68, 92, 8, Color.web("#fffdf4"));
        p.forma(foglio);
        for (int i = 0; i < 4; i++)
            p.forma(linea(-24, -70 + i * 10, 24, -70 + i * 10, Color.web("#9cb4e0"), 2));
        // matita appoggiata
        p.forma(contornata(new Polygon(30, -40, 40, -50, 46, -44, 36, -34), Color.web("#f8c810")));
        p.forma(contornata(new Polygon(30, -40, 36, -34, 27, -31), Color.web("#f6c9a0")));
        p.faccia(0, -100, 0.9);
        return p;
    }

    /** Sparpaglia i bit dell'hash, così nomi simili danno aspetti diversi. */
    private static int mescola(int h) {
        h ^= h >>> 16;
        h *= 0x45d9f3b;
        h ^= h >>> 16;
        return h & Integer.MAX_VALUE;
    }

    /**
     * Ingrandisce o rimpicciolisce il personaggio tenendo fermi i piedi
     * (lo scaling di JavaFX ruoterebbe attorno al centro e lo farebbe "galleggiare").
     */
    Personaggio scala(double fattore) {
        getTransforms().add(new Scale(fattore, fattore, 0, 0));
        return this;
    }

    // --------------------------------------------------------------- espressioni

    void setEspressione(Espressione nuova) {
        espressione = nuova;
        occhiNormali.setVisible(nuova == Espressione.NORMALE || nuova == Espressione.FELICE);
        occhiChiusi.setVisible(false);
        occhiDolore.setVisible(nuova == Espressione.DOLORE);
        occhiKo.setVisible(nuova == Espressione.KO);
        boccaSorriso.setVisible(nuova == Espressione.NORMALE);
        boccaFelice.setVisible(nuova == Espressione.FELICE);
        boccaDolore.setVisible(nuova == Espressione.DOLORE);
        boccaKo.setVisible(nuova == Espressione.KO);
    }

    Espressione getEspressione() {
        return espressione;
    }

    // ---------------------------------------------------------------- animazioni

    /** Avvia il respiro (leggero su e giù) e il battito delle palpebre. */
    Personaggio animato() {
        respiro = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(corpo.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(900), new KeyValue(corpo.translateYProperty(), -3)));
        respiro.setAutoReverse(true);
        respiro.setCycleCount(Animation.INDEFINITE);
        respiro.play();

        battito = new Timeline(
                new KeyFrame(Duration.millis(3200), e -> chiudiOcchi(true)),
                new KeyFrame(Duration.millis(3350), e -> chiudiOcchi(false)));
        battito.setCycleCount(Animation.INDEFINITE);
        battito.play();
        return this;
    }

    private void chiudiOcchi(boolean chiusi) {
        boolean aperti = espressione == Espressione.NORMALE || espressione == Espressione.FELICE;
        if (!aperti) return;
        occhiNormali.setVisible(!chiusi);
        occhiChiusi.setVisible(chiusi);
    }

    /** Colpo subito: lampo rosso, tremolio e faccia di dolore per un attimo. */
    void colpito() {
        setEspressione(Espressione.DOLORE);
        Timeline lampo = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(flash.brightnessProperty(), 0.5),
                        new KeyValue(flash.saturationProperty(), 0.6), new KeyValue(flash.hueProperty(), -0.05)),
                new KeyFrame(Duration.millis(300), new KeyValue(flash.brightnessProperty(), 0),
                        new KeyValue(flash.saturationProperty(), 0), new KeyValue(flash.hueProperty(), 0)));
        lampo.play();
        TranslateTransition scossa = new TranslateTransition(Duration.millis(50), corpo);
        scossa.setFromX(-6);
        scossa.setToX(6);
        scossa.setCycleCount(6);
        scossa.setAutoReverse(true);
        scossa.setOnFinished(e -> corpo.setTranslateX(0));
        scossa.play();
        PauseTransition ritorno = new PauseTransition(Duration.millis(700));
        ritorno.setOnFinished(e -> {
            if (espressione == Espressione.DOLORE) setEspressione(Espressione.NORMALE);
        });
        ritorno.play();
    }

    /** Saltello di gioia. */
    void esulta() {
        setEspressione(Espressione.FELICE);
        TranslateTransition salto = new TranslateTransition(Duration.millis(160), this);
        salto.setByY(-18);
        salto.setCycleCount(4);
        salto.setAutoReverse(true);
        salto.play();
    }

    /** Ferma tutte le animazioni continue (da chiamare quando il personaggio esce di scena). */
    void ferma() {
        if (respiro != null) respiro.stop();
        if (battito != null) battito.stop();
    }

    // --------------------------------------------------------------- costruzione

    private void forma(Node n) {
        corpo.getChildren().add(n);
    }

    private void ombra(double raggio) {
        getChildren().add(0, piena(new Ellipse(0, 0, raggio, raggio * 0.22), Color.web("#000000", 0.22)));
    }

    private void gambe(Color pantaloni, Color scarpe) {
        forma(rett(-15, -30, 12, 26, 6, pantaloni));
        forma(rett(3, -30, 12, 26, 6, pantaloni));
        forma(ellisse(-10, -4, 10, 5, scarpe));
        forma(ellisse(10, -4, 10, 5, scarpe));
    }

    private void busto(Color colore, double larghezza, double altezza) {
        forma(rett(-larghezza / 2, -26 - altezza, larghezza, altezza, 18, colore));
    }

    private void braccia(Color manica, Color pelle, double larghezzaBusto) {
        double x = larghezzaBusto / 2;
        forma(rett(-x - 9, -64, 14, 30, 7, manica));
        forma(rett(x - 5, -64, 14, 30, 7, manica));
        forma(contornata(new Circle(-x - 2, -34, 6), pelle));
        forma(contornata(new Circle(x + 2, -34, 6), pelle));
    }

    private void testa(Color pelle, double raggio) {
        forma(contornata(new Circle(-raggio + 1, -97, 6), pelle));   // orecchie
        forma(contornata(new Circle(raggio - 1, -97, 6), pelle));
        forma(contornata(new Circle(0, -100, raggio), pelle));
    }

    /** Disegna occhi, guance e bocca per tutte le espressioni (una sola visibile). */
    private void faccia(double cx, double cy, double s) {
        // guance
        forma(piena(new Ellipse(cx - 17 * s, cy + 8 * s, 6 * s, 3.5 * s), Color.web("#ff8a8a", 0.55)));
        forma(piena(new Ellipse(cx + 17 * s, cy + 8 * s, 6 * s, 3.5 * s), Color.web("#ff8a8a", 0.55)));

        for (int lato : new int[]{-1, 1}) {
            double ox = cx + lato * 10 * s;
            Ellipse occhio = new Ellipse(ox, cy, 4 * s, 6 * s);
            occhio.setFill(CONTORNO);
            Circle luce = new Circle(ox + 1.5 * s, cy - 2.5 * s, 1.6 * s, Color.WHITE);
            occhiNormali.getChildren().addAll(occhio, luce);
            occhiChiusi.getChildren().add(linea(ox - 4 * s, cy + 1, ox + 4 * s, cy + 1, CONTORNO, 2.5));
            // dolore: occhi a ">" "<"
            occhiDolore.getChildren().addAll(
                    linea(ox - 4 * s * lato, cy - 4 * s, ox + 4 * s * lato, cy, CONTORNO, 2.5),
                    linea(ox + 4 * s * lato, cy, ox - 4 * s * lato, cy + 4 * s, CONTORNO, 2.5));
            occhiKo.getChildren().addAll(
                    linea(ox - 4 * s, cy - 4 * s, ox + 4 * s, cy + 4 * s, CONTORNO, 2.5),
                    linea(ox + 4 * s, cy - 4 * s, ox - 4 * s, cy + 4 * s, CONTORNO, 2.5));
        }

        Arc sorriso = new Arc(cx, cy + 9 * s, 7 * s, 5 * s, 200, 140);
        sorriso.setType(ArcType.OPEN);
        sorriso.setFill(Color.TRANSPARENT);
        sorriso.setStroke(CONTORNO);
        sorriso.setStrokeWidth(2.5);
        sorriso.setStrokeLineCap(StrokeLineCap.ROUND);
        boccaSorriso.getChildren().add(sorriso);

        Arc risata = new Arc(cx, cy + 8 * s, 8 * s, 8 * s, 180, 180);
        risata.setType(ArcType.CHORD);
        risata.setFill(Color.web("#a8322a"));
        risata.setStroke(CONTORNO);
        risata.setStrokeWidth(2);
        boccaFelice.getChildren().add(risata);

        Ellipse urlo = new Ellipse(cx, cy + 12 * s, 4 * s, 5 * s);
        urlo.setFill(Color.web("#a8322a"));
        urlo.setStroke(CONTORNO);
        urlo.setStrokeWidth(2);
        boccaDolore.getChildren().add(urlo);

        // KO: bocca ondulata
        double y = cy + 12 * s;
        for (int i = 0; i < 3; i++) {
            Arc onda = new Arc(cx - 6 * s + i * 6 * s, y, 3 * s, 2.5 * s, i % 2 == 0 ? 0 : 180, 180);
            onda.setType(ArcType.OPEN);
            onda.setFill(Color.TRANSPARENT);
            onda.setStroke(CONTORNO);
            onda.setStrokeWidth(2);
            boccaKo.getChildren().add(onda);
        }
        corpo.getChildren().addAll(occhiNormali, occhiChiusi, occhiDolore, occhiKo,
                boccaSorriso, boccaFelice, boccaDolore, boccaKo);
        setEspressione(Espressione.NORMALE);
    }

    // ---------------------------------------------------------------- primitive

    private static Rectangle rett(double x, double y, double w, double h, double raggio, Color colore) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setArcWidth(raggio * 2);
        r.setArcHeight(raggio * 2);
        return contornata(r, colore);
    }

    private static Ellipse ellisse(double cx, double cy, double rx, double ry, Color colore) {
        return contornata(new Ellipse(cx, cy, rx, ry), colore);
    }

    private static Line linea(double x1, double y1, double x2, double y2, Color colore, double spessore) {
        Line l = new Line(x1, y1, x2, y2);
        l.setStroke(colore);
        l.setStrokeWidth(spessore);
        l.setStrokeLineCap(StrokeLineCap.ROUND);
        return l;
    }

    private static <S extends Shape> S piena(S forma, Color riempimento) {
        forma.setFill(riempimento);
        return forma;
    }

    private static <S extends Shape> S contornata(S forma, Color riempimento) {
        forma.setFill(riempimento);
        forma.setStroke(CONTORNO);
        forma.setStrokeWidth(SPESSORE);
        return forma;
    }
}
