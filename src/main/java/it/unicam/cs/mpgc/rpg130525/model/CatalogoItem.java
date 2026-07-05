package it.unicam.cs.mpgc.rpg130525.model;

import it.unicam.cs.mpgc.rpg130525.model.Items.Appunti;
import it.unicam.cs.mpgc.rpg130525.model.Items.Caffe;
import it.unicam.cs.mpgc.rpg130525.model.Items.ChatGPT;
import it.unicam.cs.mpgc.rpg130525.model.Items.Libro;

public final class CatalogoItem {
    private CatalogoItem() { }

    public static Item crea(TipoItem tipo) {
        return switch (tipo) {
            case Caffè          -> new Caffe(20);
            case AppuntiLezione -> new Appunti();
            case Libro          -> new Libro();
            case ChatGPT        -> new ChatGPT();
        };
    }

    public static int prezzo(TipoItem tipo) {
        return switch (tipo) {
            case Caffè          -> 10;
            case AppuntiLezione -> 15;
            case Libro          -> 25;
            case ChatGPT        -> 40;
        };
    }
}