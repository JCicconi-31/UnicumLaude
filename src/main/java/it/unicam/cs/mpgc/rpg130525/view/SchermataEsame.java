package it.unicam.cs.mpgc.rpg130525.view;

import it.unicam.cs.mpgc.rpg130525.port.ProfessoreDto;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Schermata d'esame in stile JRPG: lo studente in basso a sinistra, l'avversario
 * (il foglio della prova scritta o il professore all'orale) in alto a destra,
 * i cartellini con i punti vita, il box di dialogo e il menu delle risposte.
 * <p>
 * Gli eventi in arrivo dal motore (messaggi, danni, richieste di scelta) vengono
 * messi in coda e mostrati uno dopo l'altro, così le animazioni non si
 * sovrappongono anche quando il motore ne invia molti di fila.
 */
final class SchermataEsame {

    private static final double LARGHEZZA = 960, ALTEZZA = 400;
    private static final double AVVERSARIO_X = 810, AVVERSARIO_Y = 214;
    private static final double STUDENTE_X = 165, STUDENTE_Y = 372;

    private final BorderPane root = new BorderPane();
    private final Pane arena = new Pane();
    private final Group strato = new Group();
    private final Personaggio studente = Personaggio.studente().animato();
    private Personaggio avversario;

    private final Label nomeAvversario = StileRetro.testo("", "cartellino-nome");
    private final Label sottoAvversario = StileRetro.testo("", "cartellino-sotto");
    private final BarraHp hpAvversario = new BarraHp("HP", 180);
    private final VBox cartellinoAvversario = new VBox(4, nomeAvversario, sottoAvversario, hpAvversario);

    private final Label nomeStudente = StileRetro.testo("", "cartellino-nome");
    private final Label sottoStudente = StileRetro.testo("", "cartellino-sotto");
    private final BarraHp hpStudente = new BarraHp("HP", 180);

    private final Label rigaPrecedente = StileRetro.testo("", "dialogo-precedente");
    private final Label rigaCorrente = StileRetro.testo("", "dialogo-testo");
    private final GridPane opzioni = new GridPane();
    private final List<Button> bottoniCorrenti = new ArrayList<>();

    private final Deque<Evento> coda = new ArrayDeque<>();
    private boolean inEsecuzione = false;
    private boolean aperta = false;
    private int ultimoHpStudente = -1;
    private int ultimoHpAvversario = -1;
    private Timeline macchinaDaScrivere;

    /** Un passo della coda: un'azione e quanto aspettare prima del successivo. */
    private record Evento(Runnable azione, double millisDopo) {
    }

    SchermataEsame() {
        costruisciArena();
        costruisciZonaInferiore();
        root.getStyleClass().add("sfondo-gioco");
        root.setVisible(false);
    }

    Parent getRoot() {
        return root;
    }

    boolean isAperta() {
        return aperta;
    }

    /** I bottoni attualmente selezionabili, per le scorciatoie da tastiera. */
    List<Button> bottoniCorrenti() {
        return bottoniCorrenti;
    }

    // ------------------------------------------------------------ eventi pubblici

    /** Apre la schermata per la prova scritta: l'avversario è il foglio d'esame. */
    void apriScritto(StudenteDto stato) {
        aperta = true;
        ultimoHpAvversario = -1;
        if (stato != null) ultimoHpStudente = stato.saluteMentale();
        accoda(() -> {
            mostra(stato);
            cambiaAvversario(Personaggio.foglioEsame(), "Prova scritta", "rispondi alla domanda");
            cartellinoAvversario.getChildren().remove(hpAvversario);
        }, 350);
    }

    /** Entra in scena il professore per la prova orale (o si aggiornano i suoi HP). */
    void aggiornaProfessore(ProfessoreDto prof, StudenteDto stato) {
        if (ultimoHpAvversario < 0) {
            if (!aperta && stato != null) ultimoHpStudente = stato.saluteMentale();
            aperta = true;
            ultimoHpAvversario = prof.hp();
            accoda(() -> {
                mostra(stato);
                cambiaAvversario(Personaggio.professore(prof.nome()), "Prof. " + prof.nome(), "prova orale");
                if (!cartellinoAvversario.getChildren().contains(hpAvversario))
                    cartellinoAvversario.getChildren().add(hpAvversario);
                hpAvversario.imposta(prof.hp(), prof.hpMax(), false);
            }, 500);
            return;
        }
        boolean colpito = prof.hp() < ultimoHpAvversario;
        ultimoHpAvversario = prof.hp();
        accoda(() -> {
            hpAvversario.imposta(prof.hp(), prof.hpMax(), true);
            if (colpito) avversario.colpito();
            if (prof.hp() <= 0) {
                avversario.setEspressione(Personaggio.Espressione.KO);
                studente.esulta();
            }
        }, colpito ? 650 : 100);
    }

    void aggiornaStudente(StudenteDto s) {
        boolean colpito = ultimoHpStudente >= 0 && s.saluteMentale() < ultimoHpStudente;
        ultimoHpStudente = s.saluteMentale();
        accoda(() -> {
            aggiornaCartellinoStudente(s, true);
            if (colpito) {
                studente.colpito();
                scuotiArena();
            }
            if (s.saluteMentale() <= 0) studente.setEspressione(Personaggio.Espressione.KO);
        }, colpito ? 650 : 50);
    }

    void messaggio(String testo) {
        boolean positivo = testo.startsWith("Risposta corretta") || testo.startsWith("Prova orale superata");
        boolean scritto = ultimoHpAvversario < 0;
        accoda(() -> {
            scrivi(testo);
            if (positivo && avversario != null) {
                studente.esulta();
                if (scritto) avversario.setEspressione(Personaggio.Espressione.KO);
            }
        }, 300 + Math.min(600, testo.length() * 8));
    }

    /** Mostra la domanda nel box di dialogo e le opzioni nel menu a griglia. */
    void chiediScelta(String domanda, List<String> voci, IntConsumer onScelta) {
        accoda(() -> {
            if (domanda != null && !domanda.isBlank()) scrivi(domanda);
            mostraOpzioni(voci, onScelta);
        }, 0);
    }

    /** Chiude la schermata dopo una breve pausa, così l'esito resta leggibile. */
    void chiudi() {
        if (!aperta) return;
        aperta = false;
        ultimoHpAvversario = -1;
        accoda(() -> {
        }, 1400);
        accoda(() -> {
            FadeTransition dissolvenza = new FadeTransition(Duration.millis(350), root);
            dissolvenza.setToValue(0);
            dissolvenza.setOnFinished(e -> {
                root.setVisible(false);
                root.setOpacity(1);
            });
            dissolvenza.play();
            if (avversario != null) {
                avversario.ferma();
                strato.getChildren().remove(avversario);
            }
            avversario = null;
            studente.setEspressione(Personaggio.Espressione.NORMALE);
            pulisciOpzioni();
        }, 350);
    }

    // -------------------------------------------------------------- coda eventi

    private void accoda(Runnable azione, double millisDopo) {
        coda.addLast(new Evento(azione, millisDopo));
        if (!inEsecuzione) prossimo();
    }

    private void prossimo() {
        Evento evento = coda.pollFirst();
        if (evento == null) {
            inEsecuzione = false;
            return;
        }
        inEsecuzione = true;
        evento.azione().run();
        PauseTransition attesa = new PauseTransition(Duration.millis(Math.max(1, evento.millisDopo())));
        attesa.setOnFinished(e -> prossimo());
        attesa.play();
    }

    // -------------------------------------------------------------- costruzione

    private void costruisciArena() {
        arena.setPrefSize(LARGHEZZA, ALTEZZA);
        arena.setMinSize(LARGHEZZA, ALTEZZA);
        arena.setMaxSize(LARGHEZZA, ALTEZZA);
        Rectangle clip = new Rectangle(LARGHEZZA, ALTEZZA);
        clip.setArcWidth(36);
        clip.setArcHeight(36);
        arena.setClip(clip);

        Rectangle cielo = new Rectangle(LARGHEZZA, ALTEZZA, new LinearGradient(0, 0, 0, 1, true,
                CycleMethod.NO_CYCLE, new Stop(0, Color.web("#7ec0ff")), new Stop(1, Color.web("#d6efff"))));
        Circle sole = new Circle(90, 70, 34, Color.web("#ffe27a"));
        arena.getChildren().addAll(cielo, sole, nuvola(300, 60, 1.0), nuvola(560, 30, 0.8), nuvola(880, 50, 0.6));
        // colline in lontananza
        arena.getChildren().addAll(
                new Circle(120, 470, 260, Color.web("#8fd47a")),
                new Circle(520, 520, 300, Color.web("#7cc96a")),
                new Circle(900, 480, 260, Color.web("#8fd47a")));
        Rectangle prato = new Rectangle(0, 300, LARGHEZZA, 100);
        prato.setFill(Color.web("#6cbf5c"));
        arena.getChildren().add(prato);
        arena.getChildren().addAll(piattaforma(AVVERSARIO_X, AVVERSARIO_Y, 125), piattaforma(STUDENTE_X, STUDENTE_Y, 140));

        studente.setLayoutX(STUDENTE_X);
        studente.setLayoutY(STUDENTE_Y - 4);
        studente.scala(1.45);
        strato.getChildren().add(studente);
        arena.getChildren().add(strato);

        cartellinoAvversario.getStyleClass().add("pannello");
        // ogni cartellino sta accanto al proprio personaggio
        cartellinoAvversario.setLayoutX(425);
        cartellinoAvversario.setLayoutY(40);
        cartellinoAvversario.setPrefWidth(300);

        VBox cartellinoStudente = new VBox(4, nomeStudente, sottoStudente, hpStudente);
        cartellinoStudente.getStyleClass().add("pannello");
        cartellinoStudente.setLayoutX(330);
        cartellinoStudente.setLayoutY(262);
        cartellinoStudente.setPrefWidth(310);
        arena.getChildren().addAll(cartellinoAvversario, cartellinoStudente);

        // l'arena ha coordinate fisse e viene scalata per riempire lo spazio disponibile
        StackPane contenitore = new StackPane(new Group(arena));
        contenitore.setMinSize(0, 0);
        contenitore.setPadding(new Insets(12, 12, 0, 12));
        var scala = Bindings.createDoubleBinding(() -> {
            double f = Math.min((contenitore.getWidth() - 24) / LARGHEZZA, (contenitore.getHeight() - 12) / ALTEZZA);
            return f > 0 ? f : 1.0;
        }, contenitore.widthProperty(), contenitore.heightProperty());
        arena.scaleXProperty().bind(scala);
        arena.scaleYProperty().bind(scala);
        root.setCenter(contenitore);
    }

    private void costruisciZonaInferiore() {
        rigaCorrente.setWrapText(true);
        rigaPrecedente.setWrapText(true);
        VBox dialogo = new VBox(6, rigaPrecedente, rigaCorrente);
        dialogo.getStyleClass().add("pannello-scuro");
        dialogo.setPrefWidth(440);
        dialogo.setMinHeight(150);
        HBox.setHgrow(dialogo, Priority.ALWAYS);

        opzioni.setHgap(10);
        opzioni.setVgap(10);
        ColumnConstraints colonna = new ColumnConstraints();
        colonna.setPercentWidth(50);
        opzioni.getColumnConstraints().addAll(colonna, colonna);
        VBox menu = new VBox(opzioni);
        menu.getStyleClass().add("pannello-scuro");
        menu.setPrefWidth(470);
        menu.setMinHeight(150);
        menu.setAlignment(Pos.CENTER);

        HBox zona = new HBox(12, dialogo, menu);
        zona.setPadding(new Insets(12));
        root.setBottom(zona);
    }

    private static Group nuvola(double x, double y, double s) {
        Color bianco = Color.web("#ffffff", 0.9);
        Group g = new Group(new Ellipse(0, 0, 40, 18), new Circle(-18, -8, 18), new Circle(14, -14, 22));
        g.getChildren().forEach(n -> ((javafx.scene.shape.Shape) n).setFill(bianco));
        g.setLayoutX(x);
        g.setLayoutY(y);
        g.setScaleX(s);
        g.setScaleY(s);
        return g;
    }

    private static Group piattaforma(double x, double y, double raggio) {
        Ellipse bordo = new Ellipse(x, y + 4, raggio, raggio * 0.22);
        bordo.setFill(Color.web("#3f8a3a"));
        Ellipse cima = new Ellipse(x, y, raggio, raggio * 0.2);
        cima.setFill(Color.web("#9be08a"));
        cima.setStroke(Color.web("#2a1e16"));
        cima.setStrokeWidth(2.5);
        return new Group(bordo, cima);
    }

    // ------------------------------------------------------------------ helper

    private void mostra(StudenteDto stato) {
        if (root.isVisible() && root.getOpacity() > 0) return;
        root.setOpacity(0);
        root.setVisible(true);
        rigaPrecedente.setText("");
        rigaCorrente.setText("");
        if (stato != null) aggiornaCartellinoStudente(stato, false);
        FadeTransition comparsa = new FadeTransition(Duration.millis(300), root);
        comparsa.setToValue(1);
        comparsa.play();
    }

    private void aggiornaCartellinoStudente(StudenteDto s, boolean animata) {
        nomeStudente.setText(s.nomeCompleto());
        sottoStudente.setText("INT " + s.intelligenzaEffettiva() + "   RES " + s.resilienzaEffettiva() + "   CFU " + s.cfu());
        hpStudente.imposta(s.saluteMentale(), s.saluteMentaleMax(), animata);
    }

    private void cambiaAvversario(Personaggio nuovo, String nome, String sotto) {
        if (avversario != null) {
            avversario.ferma();
            strato.getChildren().remove(avversario);
        }
        avversario = nuovo.animato();
        avversario.setLayoutX(AVVERSARIO_X);
        avversario.setLayoutY(AVVERSARIO_Y - 4);
        avversario.scala(1.25);
        strato.getChildren().add(0, avversario);
        nomeAvversario.setText(nome);
        sottoAvversario.setText(sotto);
        // ingresso da destra
        TranslateTransition ingresso = new TranslateTransition(Duration.millis(420), avversario);
        ingresso.setFromX(260);
        ingresso.setToX(0);
        ingresso.play();
    }

    /** Scrive il testo lettera per lettera; il messaggio precedente sale di una riga. */
    private void scrivi(String testo) {
        if (macchinaDaScrivere != null) macchinaDaScrivere.stop();
        if (!rigaCorrente.getText().isEmpty()) rigaPrecedente.setText(rigaCorrente.getUserData() + "");
        rigaCorrente.setUserData(testo);
        rigaCorrente.setText("");
        macchinaDaScrivere = new Timeline();
        for (int i = 1; i <= testo.length(); i++) {
            String parziale = testo.substring(0, i);
            macchinaDaScrivere.getKeyFrames().add(new KeyFrame(Duration.millis(i * 14.0),
                    e -> rigaCorrente.setText(parziale)));
        }
        macchinaDaScrivere.play();
    }

    private void mostraOpzioni(List<String> voci, IntConsumer onScelta) {
        pulisciOpzioni();
        for (int i = 0; i < voci.size(); i++) {
            final int indice = i;
            Button b = StileRetro.bottone(voci.get(i));
            b.getStyleClass().add("bottone-risposta");
            if (voci.size() > 1) {
                Label numero = StileRetro.testo(String.valueOf(i + 1), "bottone-numero");
                b.setGraphic(numero);
            }
            b.setMaxWidth(Double.MAX_VALUE);
            b.setMaxHeight(Double.MAX_VALUE);
            b.setOnAction(e -> {
                pulisciOpzioni();
                onScelta.accept(indice);
            });
            bottoniCorrenti.add(b);
            if (voci.size() == 1) {
                GridPane.setColumnSpan(b, 2);
                opzioni.add(b, 0, 0);
            } else {
                opzioni.add(b, i % 2, i / 2);
            }
        }
        if (!bottoniCorrenti.isEmpty()) bottoniCorrenti.get(0).requestFocus();
    }

    private void pulisciOpzioni() {
        opzioni.getChildren().clear();
        bottoniCorrenti.clear();
    }

    private void scuotiArena() {
        TranslateTransition t = new TranslateTransition(Duration.millis(45), arena);
        t.setFromY(-4);
        t.setToY(4);
        t.setCycleCount(6);
        t.setAutoReverse(true);
        t.setOnFinished(e -> arena.setTranslateY(0));
        t.play();
    }
}
