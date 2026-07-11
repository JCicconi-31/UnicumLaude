package it.unicam.cs.mpgc.rpg130525;

import it.unicam.cs.mpgc.rpg130525.app.App;
import it.unicam.cs.mpgc.rpg130525.app.JavaFxApp;

/**
 * Punto d'ingresso dell'applicazione: sceglie l'interfaccia a console
 * (argomento {@code --console}) o quella grafica JavaFX (default).
 */
public class Main {
    static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--console")) App.main(args);
        else JavaFxApp.main(args);
    }
}
