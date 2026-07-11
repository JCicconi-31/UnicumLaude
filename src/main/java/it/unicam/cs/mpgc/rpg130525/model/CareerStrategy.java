package it.unicam.cs.mpgc.rpg130525.model;

/**
 * pattern Strategy per la carriera dello studente: modifica gli HP
 * massimi e le monete iniziali, permettendo di aggiungere carriere senza toccare
 * lo {@code Studente}.
 */
public interface CareerStrategy {

    /**
     * @return il bonus (positivo) o malus (negativo) agli HP massimi dello
     * studente; il totale risultante deve restare positivo
     */
    int modificatoreHpMax();

    /**
     * @return il bonus (positivo) o malus (negativo) alle monete iniziali;
     * il totale risultante non deve diventare negativo
     */
    int modificatoreMoneteIniziali();

    /**
     * @return il nome della carriera mostrato al giocatore, mai nullo
     */
    String getNome();

    default String getDescrizione() {
        return String.format("HP max %+d, monete iniziali %+d", modificatoreHpMax(), modificatoreMoneteIniziali());
    }
}
