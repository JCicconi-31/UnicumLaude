package it.unicam.cs.mpgc.rpg130525.app;

import it.unicam.cs.mpgc.rpg130525.view.ConsoleView;

/**
 * Avvio dell'interfaccia a console: costruisce la {@link ConsoleView} e lancia
 * la partita.
 */
public class App {
    public static void main(String[] args) {
        ConsoleView console = new ConsoleView();
        Partita.esegui(console, console);
    }
}
