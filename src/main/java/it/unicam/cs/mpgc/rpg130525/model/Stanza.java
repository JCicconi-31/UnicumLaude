package it.unicam.cs.mpgc.rpg130525.model;

public class Stanza {
    private final String nome;
    private final TipoStanza tipo;
    private final Esame esameAssociato;

    public Stanza(String nome, TipoStanza tipo, Esame esameAssociato) {
        if (nome == null) throw new IllegalArgumentException("Nome aula nullo");
        this.nome = nome;
        this.tipo = tipo;
        if (tipo == TipoStanza.AULA_ESAME && esameAssociato == null)
            throw new IllegalArgumentException("l'esame associato ad una aula esame non può essere null");
        this.esameAssociato = esameAssociato;
    }

    public String getNome() {
        return nome;
    }

    public TipoStanza getTipo() {
        return tipo;
    }

    public Esame getEsame() {
        return esameAssociato;
    }

}