package it.unicam.cs.mpgc.rpg130525.model;

import it.unicam.cs.mpgc.rpg130525.model.Items.Appunti;
import it.unicam.cs.mpgc.rpg130525.model.Items.Caffe;
import it.unicam.cs.mpgc.rpg130525.model.Items.ChatGPT;
import it.unicam.cs.mpgc.rpg130525.model.Items.Libro;

public final class CatalogoItem {
    private CatalogoItem() { }

    public static Item crea(TipoItem tipo) {
        return switch (tipo) {
            case CAFFE -> new Caffe(20);
            case APPUNTI_LEZIONE -> new Appunti();
            case LIBRO -> new Libro();
            case CHATGPT -> new ChatGPT();
        };
    }

    public static int prezzo(TipoItem tipo) {
        return switch (tipo) {
            case CAFFE -> 10;
            case APPUNTI_LEZIONE -> 15;
            case LIBRO -> 25;
            case CHATGPT -> 40;
        };
    }
}