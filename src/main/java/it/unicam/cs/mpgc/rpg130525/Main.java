package it.unicam.cs.mpgc.rpg130525;

import it.unicam.cs.mpgc.rpg130525.app.App;
import it.unicam.cs.mpgc.rpg130525.app.JavaFxApp;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--console"))
            App.main(args);
        else
            JavaFxApp.main(args);
    }
}
