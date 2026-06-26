package it.unicam.cs.mpgc.rpg130525.model;

public abstract class Persona {
    protected String nome;
    protected String cognome;

    public Persona(String nome, String cognome) {
        if (nome == null || cognome == null)
            throw new IllegalArgumentException("parametri null");
        this.nome = nome;
        this.cognome = cognome;
    }

    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

}
