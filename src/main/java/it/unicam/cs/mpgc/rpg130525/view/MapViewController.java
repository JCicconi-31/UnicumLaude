package it.unicam.cs.mpgc.rpg130525.view;

import it.unicam.cs.mpgc.rpg130525.model.TipoStanza;
import it.unicam.cs.mpgc.rpg130525.port.StanzaDto;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Mappa del polo universitario in stile "mondo" di un platform: prato, alberi,
 * sentieri curvi, stanze rotonde con un'icona per tipo e l'omino dello studente
 * che saltella da una stanza all'altra.
 * <p>
 * La disposizione delle stanze è calcolata con una visita in ampiezza a partire
 * dall'Atrio, quindi la mappa si adatta da sola al contenuto di {@code mappa.json}.
 */
public class MapViewController {

    private static final String STANZA_PARTENZA = "Atrio";
    private static final double MARGINE_X = 70, PASSO_X_MAX = 150, PASSO_Y = 110;
    private static final double LARGHEZZA = 680, ALTEZZA = 460;
    private static final double RAGGIO_NODO = 25, RAGGIO_CORRIDOIO = 15;

    private static final Color INCHIOSTRO = Color.web("#2a1e16");
    private static final Color SENTIERO = Color.web("#f0dfa8");
    private static final Color BORDO_SENTIERO = Color.web("#9a7440");

    private final Pane tela = new Pane();
    private final StackPane root = new StackPane();
    private final Personaggio omino = Personaggio.studente().scala(0.42).animato();
    private final Group ominoContenitore = new Group(omino);
    private Point2D ultimaPosizione;

    public MapViewController() {
        tela.setPrefSize(LARGHEZZA, ALTEZZA);
        tela.setMinSize(LARGHEZZA, ALTEZZA);
        tela.setMaxSize(LARGHEZZA, ALTEZZA);
        Rectangle clip = new Rectangle(LARGHEZZA, ALTEZZA);
        clip.setArcWidth(32);
        clip.setArcHeight(32);
        tela.setClip(clip);
        ominoContenitore.setMouseTransparent(true);

        // la tela resta a coordinate fisse; il contenitore la scala per riempire l'area
        root.getChildren().add(new Group(tela));
        root.setMinSize(0, 0);
        DoubleBinding scala = Bindings.createDoubleBinding(() -> {
            double fattore = Math.min(root.getWidth() / LARGHEZZA, root.getHeight() / ALTEZZA);
            return fattore > 0 ? fattore : 1.0;
        }, root.widthProperty(), root.heightProperty());
        tela.scaleXProperty().bind(scala);
        tela.scaleYProperty().bind(scala);
    }

    public StackPane getRoot() {
        return root;
    }

    public void render(List<StanzaDto> stanze, String posizioneCorrente, Consumer<String> onClickStanza) {
        tela.getChildren().clear();
        Map<String, Point2D> posizioni = calcolaLayout(stanze);
        disegnaPrato(posizioni.values());
        disegnaSentieri(stanze, posizioni);
        disegnaNodi(stanze, posizioni, onClickStanza);
        Point2D posizione = posizioni.get(posizioneCorrente);
        if (posizione != null)
            posizionaOmino(posizione);
    }

    // ------------------------------------------------------------------ sfondo

    private void disegnaPrato(Collection<Point2D> nodi) {
        Rectangle prato = new Rectangle(LARGHEZZA, ALTEZZA, new LinearGradient(0, 0, 0, 1, true,
                CycleMethod.NO_CYCLE, new Stop(0, Color.web("#7fd36b")), new Stop(1, Color.web("#5db552"))));
        tela.getChildren().add(prato);
        // macchie d'erba più chiara, sempre nella stessa posizione
        Random caso = new Random(42);
        for (int i = 0; i < 18; i++) {
            Ellipse macchia = new Ellipse(caso.nextDouble() * LARGHEZZA, caso.nextDouble() * ALTEZZA,
                    30 + caso.nextDouble() * 40, 12 + caso.nextDouble() * 14);
            macchia.setFill(Color.web("#a6e58f", 0.35));
            tela.getChildren().add(macchia);
        }
        // alberi lungo i bordi, lontano dalle stanze
        for (int i = 0; i < 26; i++) {
            double x = 20 + caso.nextDouble() * (LARGHEZZA - 40);
            double y = caso.nextBoolean() ? 20 + caso.nextDouble() * 60 : ALTEZZA - 30 - caso.nextDouble() * 50;
            Point2D punto = new Point2D(x, y);
            if (nodi.stream().noneMatch(n -> n.distance(punto) < 70))
                tela.getChildren().add(albero(x, y, 0.8 + caso.nextDouble() * 0.5));
        }
    }

    private static Group albero(double x, double y, double s) {
        Rectangle tronco = new Rectangle(-4, -2, 8, 14);
        tronco.setArcWidth(4);
        tronco.setArcHeight(4);
        tronco.setFill(Color.web("#8a5a2e"));
        tronco.setStroke(INCHIOSTRO);
        tronco.setStrokeWidth(2);
        Circle chioma = new Circle(0, -12, 15, Color.web("#3f9e47"));
        chioma.setStroke(INCHIOSTRO);
        chioma.setStrokeWidth(2);
        Circle luce = new Circle(-5, -17, 5, Color.web("#7ccf6f"));
        Ellipse ombra = new Ellipse(0, 12, 14, 4);
        ombra.setFill(Color.web("#000000", 0.18));
        Group albero = new Group(ombra, tronco, chioma, luce);
        albero.setLayoutX(x);
        albero.setLayoutY(y);
        albero.setScaleX(s);
        albero.setScaleY(s);
        albero.setMouseTransparent(true);
        return albero;
    }

    // ------------------------------------------------------------------ layout

    private Map<String, Point2D> calcolaLayout(List<StanzaDto> stanze) {
        Map<String, StanzaDto> perNome = new LinkedHashMap<>();
        for (StanzaDto st : stanze)
            perNome.put(st.nome(), st);

        Map<Integer, List<String>> livelli = new LinkedHashMap<>();
        Set<String> visitate = new HashSet<>();
        String partenza = perNome.containsKey(STANZA_PARTENZA) ? STANZA_PARTENZA : stanze.get(0).nome();

        Deque<String> coda = new ArrayDeque<>();
        Map<String, Integer> livelloDi = new LinkedHashMap<>();
        coda.add(partenza);
        visitate.add(partenza);
        livelloDi.put(partenza, 0);
        while (!coda.isEmpty()) {
            String corrente = coda.removeFirst();
            livelli.computeIfAbsent(livelloDi.get(corrente), l -> new ArrayList<>()).add(corrente);
            for (String vicina : perNome.get(corrente).adiacenti())
                if (perNome.containsKey(vicina) && visitate.add(vicina)) {
                    livelloDi.put(vicina, livelloDi.get(corrente) + 1);
                    coda.addLast(vicina);
                }
        }
        // eventuali stanze non raggiungibili dalla partenza: colonna aggiuntiva
        List<String> isolate = stanze.stream().map(StanzaDto::nome)
                .filter(n -> !visitate.contains(n)).toList();
        if (!isolate.isEmpty())
            livelli.put(livelli.size(), new ArrayList<>(isolate));

        // il passo orizzontale si adatta perché l'intero percorso stia nel pannello
        int ultimoLivello = livelli.size() - 1;
        double passoX = ultimoLivello == 0
                ? 0
                : Math.min(PASSO_X_MAX, (LARGHEZZA - 2 * MARGINE_X) / ultimoLivello);
        double inizioX = (LARGHEZZA - passoX * ultimoLivello) / 2;

        Map<String, Point2D> posizioni = new LinkedHashMap<>();
        for (var livello : livelli.entrySet()) {
            List<String> nomi = livello.getValue();
            double x = inizioX + livello.getKey() * passoX;
            double zigZag = (livello.getKey() % 2 == 0) ? -22 : 22; // andamento a serpentina
            for (int i = 0; i < nomi.size(); i++) {
                double y = ALTEZZA / 2 + (i - (nomi.size() - 1) / 2.0) * PASSO_Y + zigZag;
                posizioni.put(nomi.get(i), new Point2D(x, y));
            }
        }
        return posizioni;
    }

    // ---------------------------------------------------------------- sentieri

    private void disegnaSentieri(List<StanzaDto> stanze, Map<String, Point2D> posizioni) {
        Set<String> archiDisegnati = new HashSet<>();
        for (StanzaDto st : stanze) {
            Point2D da = posizioni.get(st.nome());
            for (String vicina : st.adiacenti()) {
                Point2D a = posizioni.get(vicina);
                if (a == null || !archiDisegnati.add(chiaveArco(st.nome(), vicina)))
                    continue;
                tela.getChildren().addAll(sentiero(da, a, 18, BORDO_SENTIERO), sentiero(da, a, 11, SENTIERO));
            }
        }
    }

    private String chiaveArco(String a, String b) {
        return a.compareTo(b) < 0 ? a + "->" + b : b + "->" + a;
    }

    /** Sentiero leggermente curvo: il punto di controllo è spostato di lato. */
    private QuadCurve sentiero(Point2D da, Point2D a, double spessore, Color colore) {
        Point2D medio = da.midpoint(a);
        Point2D direzione = a.subtract(da);
        Point2D normale = new Point2D(-direzione.getY(), direzione.getX()).normalize().multiply(18);
        QuadCurve curva = new QuadCurve(da.getX(), da.getY(), medio.getX() + normale.getX(),
                medio.getY() + normale.getY(), a.getX(), a.getY());
        curva.setFill(null);
        curva.setStroke(colore);
        curva.setStrokeWidth(spessore);
        curva.setStrokeLineCap(StrokeLineCap.ROUND);
        return curva;
    }

    // ------------------------------------------------------------------- stanze

    private void disegnaNodi(List<StanzaDto> stanze, Map<String, Point2D> posizioni,
                             Consumer<String> onClickStanza) {
        for (StanzaDto st : stanze) {
            Point2D p = posizioni.get(st.nome());
            boolean corridoio = st.tipo() == TipoStanza.CORRIDOIO;
            double raggio = corridoio ? RAGGIO_CORRIDOIO : RAGGIO_NODO;

            Group nodo = new Group();
            Circle base = new Circle(0, 4, raggio, coloreBordo(st.stato()));
            Circle disco = new Circle(0, 0, raggio, coloreRiempimento(st.stato()));
            disco.setStroke(INCHIOSTRO);
            disco.setStrokeWidth(3);
            nodo.getChildren().addAll(base, disco);
            if (!corridoio) nodo.getChildren().add(icona(st.tipo()));
            if (st.stato() == StanzaDto.Stato.BLOCCATA) nodo.getChildren().add(lucchetto(raggio));
            if (st.stato() == StanzaDto.Stato.SUPERATA) nodo.getChildren().add(spunta(raggio));
            nodo.setLayoutX(p.getX());
            nodo.setLayoutY(p.getY());
            nodo.setCursor(Cursor.HAND);
            nodo.setOnMouseClicked(e -> onClickStanza.accept(st.nome()));
            nodo.setOnMouseEntered(e -> ingrandisci(nodo, 1.12));
            nodo.setOnMouseExited(e -> ingrandisci(nodo, 1.0));

            Label nome = StileRetro.testo(st.nome(), "chip-nome");
            nome.setMinWidth(Label.USE_PREF_SIZE);
            nome.setPrefWidth(110);
            nome.setAlignment(Pos.CENTER);
            nome.setLayoutX(p.getX() - 55);
            nome.setLayoutY(p.getY() + raggio + 8);
            nome.setMouseTransparent(true);

            tela.getChildren().addAll(nodo, nome);
        }
    }

    private static void ingrandisci(Node nodo, double fattore) {
        ScaleTransition t = new ScaleTransition(Duration.millis(120), nodo);
        t.setToX(fattore);
        t.setToY(fattore);
        t.play();
    }

    /** Icona dentro la stanza: un edificio per le aule d'esame, una tazza per l'aula studio. */
    private static Group icona(TipoStanza tipo) {
        Group g = new Group();
        if (tipo == TipoStanza.AULA_ESAME) {
            Polygon tetto = contornata(new Polygon(-13, -3, 0, -14, 13, -3), Color.web("#d8463a"));
            Rectangle muro = contornata(new Rectangle(-10, -3, 20, 14), Color.web("#fff3d6"));
            Rectangle porta = contornata(new Rectangle(-3, 3, 6, 8), Color.web("#8a5a2e"));
            g.getChildren().addAll(muro, tetto, porta);
        } else if (tipo == TipoStanza.AULA_STUDIO) {
            Rectangle tazza = contornata(new Rectangle(-10, -8, 16, 16), Color.WHITE);
            tazza.setArcWidth(8);
            tazza.setArcHeight(8);
            Arc manico = new Arc(7, 0, 5, 5, -90, 180);
            manico.setType(ArcType.OPEN);
            manico.setFill(null);
            manico.setStroke(INCHIOSTRO);
            manico.setStrokeWidth(2.5);
            Rectangle caffe = new Rectangle(-8, -6, 12, 4);
            caffe.setFill(Color.web("#7a4a22"));
            Line vapore1 = linea(-5, -12, -4, -17);
            Line vapore2 = linea(0, -12, 1, -17);
            g.getChildren().addAll(manico, tazza, caffe, vapore1, vapore2);
        }
        return g;
    }

    private static Group lucchetto(double raggio) {
        Arc archetto = new Arc(raggio - 4, -raggio + 2, 5, 6, 0, 180);
        archetto.setType(ArcType.OPEN);
        archetto.setFill(null);
        archetto.setStroke(INCHIOSTRO);
        archetto.setStrokeWidth(2.5);
        Rectangle corpo = contornata(new Rectangle(raggio - 11, -raggio + 1, 14, 11), Color.web("#c9c9c9"));
        corpo.setArcWidth(4);
        corpo.setArcHeight(4);
        return new Group(archetto, corpo);
    }

    private static Group spunta(double raggio) {
        Circle bollino = contornata(new Circle(raggio - 4, -raggio + 4, 9), Color.web("#2f9e44"));
        Line a = linea(raggio - 8, -raggio + 4, raggio - 5, -raggio + 7);
        Line b = linea(raggio - 5, -raggio + 7, raggio, -raggio);
        a.setStroke(Color.WHITE);
        b.setStroke(Color.WHITE);
        return new Group(bollino, a, b);
    }

    private Color coloreRiempimento(StanzaDto.Stato stato) {
        return switch (stato) {
            case BLOCCATA -> Color.web("#b9b9b9");
            case DISPONIBILE -> Color.web("#f8c810");
            case SUPERATA -> Color.web("#7ad87a");
        };
    }

    private Color coloreBordo(StanzaDto.Stato stato) {
        return switch (stato) {
            case BLOCCATA -> Color.web("#6e6e6e");
            case DISPONIBILE -> Color.web("#a07800");
            case SUPERATA -> Color.web("#2f7a32");
        };
    }

    // ------------------------------------------------------------------- omino

    /** Mette l'omino sulla stanza; se si è spostato, lo fa camminare con un saltello. */
    private void posizionaOmino(Point2D p) {
        ominoContenitore.setLayoutX(p.getX());
        ominoContenitore.setLayoutY(p.getY() - 14);
        tela.getChildren().add(ominoContenitore);
        if (ultimaPosizione != null && !ultimaPosizione.equals(p)) {
            TranslateTransition cammino = new TranslateTransition(Duration.millis(450), ominoContenitore);
            cammino.setFromX(ultimaPosizione.getX() - p.getX());
            cammino.setFromY(ultimaPosizione.getY() - p.getY());
            cammino.setToX(0);
            cammino.setToY(0);
            TranslateTransition saltello = new TranslateTransition(Duration.millis(225), omino);
            saltello.setFromY(0);
            saltello.setToY(-14);
            saltello.setAutoReverse(true);
            saltello.setCycleCount(2);
            ParallelTransition movimento = new ParallelTransition(cammino, saltello);
            movimento.setCycleCount(1);
            movimento.play();
        }
        ultimaPosizione = p;
    }

    // ---------------------------------------------------------------- primitive

    private static Line linea(double x1, double y1, double x2, double y2) {
        Line l = new Line(x1, y1, x2, y2);
        l.setStroke(INCHIOSTRO);
        l.setStrokeWidth(2.5);
        l.setStrokeLineCap(StrokeLineCap.ROUND);
        return l;
    }

    private static <S extends Shape> S contornata(S forma, Color colore) {
        forma.setFill(colore);
        forma.setStroke(INCHIOSTRO);
        forma.setStrokeWidth(2);
        return forma;
    }
}
