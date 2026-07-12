package it.unicam.cs.mpgc.rpg130525.view;

import it.unicam.cs.mpgc.rpg130525.model.TipoStanza;
import it.unicam.cs.mpgc.rpg130525.port.StanzaDto;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;
import java.util.function.Consumer;

/**
 * Mappa di gioco in stile retro ispirata alle mappe dei mondi di Super Mario
 * Bros 3: prato a scacchiera, sentieri di sabbia fra i nodi e stanze come
 * "tile" quadrate colorate per stato (bloccata, disponibile, superata), con lo
 * sprite pixel-art del giocatore sulla posizione corrente. Il layout dispone i
 * nodi per livelli di distanza (BFS) dalla stanza di partenza, da sinistra
 * verso destra come una progressione di mondo.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic, tramite Claude Code), come
 * previsto dalle indicazioni del corso.
 */
public class MapViewController {

    private static final String STANZA_PARTENZA = "Atrio";
    private static final double MARGINE_X = 65, PASSO_X_MAX = 145, PASSO_Y = 95;
    private static final double LARGHEZZA = 640, ALTEZZA = 450, TILE_SFONDO = 24;
    private static final double LATO_NODO = 40, BORDO_NODO = 4;

    private static final Color ERBA_CHIARA = Color.web("#48b03c");
    private static final Color ERBA_SCURA = Color.web("#3c9a32");
    private static final Color SENTIERO = Color.web("#e8d8a0");
    private static final Color BORDO_SENTIERO = Color.web("#7a5c2e");

    private final Pane tela = new Pane();
    private final StackPane root = new StackPane();

    public MapViewController() {
        tela.setPrefSize(LARGHEZZA, ALTEZZA);
        tela.setMinSize(LARGHEZZA, ALTEZZA);
        tela.setMaxSize(LARGHEZZA, ALTEZZA);
        // la tela di disegno resta a coordinate fisse; il contenitore la scala
        // in proporzione allo spazio disponibile, così la mappa riempie l'area
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

    /**
     * Ridisegna l'intera mappa a partire dallo snapshot fornito dal motore.
     *
     * @param onClickStanza callback invocata col nome della stanza cliccata
     */
    public void render(List<StanzaDto> stanze, String posizioneCorrente, Consumer<String> onClickStanza) {
        tela.getChildren().clear();
        disegnaPrato();
        Map<String, Point2D> posizioni = calcolaLayout(stanze);
        disegnaSentieri(stanze, posizioni);
        disegnaNodi(stanze, posizioni, onClickStanza);
        Point2D posizione = posizioni.get(posizioneCorrente);
        if (posizione != null)
            disegnaGiocatore(posizione);
    }

    /** Prato a scacchiera due tonalità di verde, come i mondi erbosi di SMB3. */
    private void disegnaPrato() {
        for (int riga = 0; riga * TILE_SFONDO < ALTEZZA; riga++)
            for (int colonna = 0; colonna * TILE_SFONDO < LARGHEZZA; colonna++) {
                Rectangle tile = new Rectangle(colonna * TILE_SFONDO, riga * TILE_SFONDO, TILE_SFONDO, TILE_SFONDO);
                tile.setFill((riga + colonna) % 2 == 0 ? ERBA_CHIARA : ERBA_SCURA);
                tela.getChildren().add(tile);
            }
    }

    /**
     * Dispone le stanze per livelli di distanza (BFS) dalla stanza di partenza:
     * ogni livello è una colonna, la progressione va da sinistra a destra.
     */
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

        Map<String, Point2D> posizioni = new LinkedHashMap<>();
        for (var livello : livelli.entrySet()) {
            List<String> nomi = livello.getValue();
            double x = MARGINE_X + livello.getKey() * passoX;
            double zigZag = (livello.getKey() % 2 == 0) ? -14 : 14; // andamento a serpentina
            for (int i = 0; i < nomi.size(); i++) {
                double y = ALTEZZA / 2 + (i - (nomi.size() - 1) / 2.0) * PASSO_Y + zigZag;
                posizioni.put(nomi.get(i), new Point2D(x, y));
            }
        }
        return posizioni;
    }

    /** Sentieri di sabbia bordati fra stanze adiacenti (ogni arco una sola volta). */
    private void disegnaSentieri(List<StanzaDto> stanze, Map<String, Point2D> posizioni) {
        Set<String> archiDisegnati = new HashSet<>();
        for (StanzaDto st : stanze) {
            Point2D da = posizioni.get(st.nome());
            for (String vicina : st.adiacenti()) {
                Point2D a = posizioni.get(vicina);
                if (a == null || !archiDisegnati.add(chiaveArco(st.nome(), vicina)))
                    continue;
                tela.getChildren().addAll(
                        sentiero(da, a, 14, BORDO_SENTIERO),
                        sentiero(da, a, 8, SENTIERO));
            }
        }
    }

    private String chiaveArco(String a, String b) {
        return a.compareTo(b) < 0 ? a + "->" + b : b + "->" + a;
    }

    private Line sentiero(Point2D da, Point2D a, double spessore, Color colore) {
        Line linea = new Line(da.getX(), da.getY(), a.getX(), a.getY());
        linea.setStrokeWidth(spessore);
        linea.setStroke(colore);
        return linea;
    }

    /** Stanze come tile quadrate: colore per stato, lettera per tipo, nome sotto. */
    private void disegnaNodi(List<StanzaDto> stanze, Map<String, Point2D> posizioni,
                             Consumer<String> onClickStanza) {
        for (StanzaDto st : stanze) {
            Point2D p = posizioni.get(st.nome());
            StackPane nodo = new StackPane();
            nodo.setLayoutX(p.getX() - LATO_NODO / 2);
            nodo.setLayoutY(p.getY() - LATO_NODO / 2);

            Rectangle bordo = new Rectangle(LATO_NODO, LATO_NODO, coloreBordo(st.stato()));
            Rectangle interno = new Rectangle(LATO_NODO - BORDO_NODO * 2, LATO_NODO - BORDO_NODO * 2,
                    coloreRiempimento(st.stato()));
            Label lettera = new Label(letteraTipo(st.tipo()));
            lettera.setFont(Font.font(StileRetro.FONT_RETRO, FontWeight.BOLD, 18));
            lettera.setTextFill(coloreBordo(st.stato()));

            nodo.getChildren().addAll(bordo, interno, lettera);
            nodo.setOnMouseClicked(e -> onClickStanza.accept(st.nome()));

            Label nome = StileRetro.chip(st.nome());
            nome.setLayoutX(p.getX() - 48);
            nome.setLayoutY(p.getY() + LATO_NODO / 2 + 4);
            nome.setPrefWidth(96);
            nome.setAlignment(javafx.geometry.Pos.CENTER);

            tela.getChildren().addAll(nodo, nome);
        }
    }

    private Color coloreRiempimento(StanzaDto.Stato stato) {
        return switch (stato) {
            case BLOCCATA -> Color.web("#9c9c9c");
            case DISPONIBILE -> Color.web("#f8c810");
            case SUPERATA -> Color.web("#58c858");
        };
    }

    private Color coloreBordo(StanzaDto.Stato stato) {
        return switch (stato) {
            case BLOCCATA -> Color.web("#4a4a4a");
            case DISPONIBILE -> Color.web("#8a5a00");
            case SUPERATA -> Color.web("#1e6e1e");
        };
    }

    private String letteraTipo(TipoStanza tipo) {
        return switch (tipo) {
            case AULA_ESAME -> "E";
            case AULA_STUDIO -> "R";
            case CORRIDOIO -> "";
        };
    }

    /** Sprite pixel-art del giocatore (berretto, viso e busto) sul nodo corrente. */
    private void disegnaGiocatore(Point2D p) {
        Group sprite = new Group(
                pixel(p.getX() - 8, p.getY() - 26, 16, 5, Color.web("#d82818")),  // berretto
                pixel(p.getX() - 6, p.getY() - 21, 12, 7, Color.web("#f8c890")),  // viso
                pixel(p.getX() - 8, p.getY() - 14, 16, 9, Color.web("#3858c8")),  // busto
                pixel(p.getX() - 7, p.getY() - 5, 5, 4, Color.web("#5a3a1a")),    // piede sx
                pixel(p.getX() + 2, p.getY() - 5, 5, 4, Color.web("#5a3a1a")));   // piede dx
        sprite.setMouseTransparent(true);
        tela.getChildren().add(sprite);
    }

    private Rectangle pixel(double x, double y, double larghezza, double altezza, Color colore) {
        Rectangle rettangolo = new Rectangle(x, y, larghezza, altezza);
        rettangolo.setFill(colore);
        rettangolo.setStroke(Color.web("#101010"));
        rettangolo.setStrokeWidth(1);
        return rettangolo;
    }
}
