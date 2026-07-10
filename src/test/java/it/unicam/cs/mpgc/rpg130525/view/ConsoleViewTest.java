package it.unicam.cs.mpgc.rpg130525.view;

import it.unicam.cs.mpgc.rpg130525.model.Domanda;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per {@link ConsoleView}: lettura delle scelte con validazione e
 * riproposta (input non numerico o fuori range), conversione della numerazione
 * da base 1 (utente) a base 0 (dominio), rifiuto del testo vuoto e formato
 * dell'output. Scanner e PrintStream sono simulati in memoria: l'input è una
 * stringa preconfezionata, l'output un buffer rileggibile.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe di test è stata realizzata con
 * l'assistenza di un'intelligenza artificiale (Claude, Anthropic), come previsto
 * dalle indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
class ConsoleViewTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    /**
     * ConsoleView con input simulato dalla stringa e output catturato nel buffer.
     */
    private ConsoleView view(String input) {
        return new ConsoleView(new Scanner(input),
                new PrintStream(out, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void scegliConvertsDaBase1ABase0() {
        int scelta = view("2\n").scegli("Menu", List.of("A", "B", "C"));
        assertEquals(1, scelta); // l'utente digita 2, il dominio riceve indice 1
    }

    @Test
    void scegliRipresentaSeInputNonNumerico() {
        int scelta = view("abc\n2\n").scegli("Menu", List.of("A", "B"));
        assertEquals(1, scelta);
        assertTrue(output().contains("non valido")); // ha segnalato e riproposto
    }

    @Test
    void scegliRipresentaSeIndiceFuoriRange() {
        int scelta = view("9\n0\n1\n").scegli("Menu", List.of("A", "B"));
        assertEquals(0, scelta); // 9 e 0 rifiutati (fuori da 1..2), poi 1 -> indice 0
    }

    @Test
    void scegliMostraTitoloEOpzioniNumerate() {
        view("1\n").scegli("Sei in: Atrio", List.of("Vai a: LA1", "Salva ed esci"));
        assertTrue(output().contains("Sei in: Atrio"));
        assertTrue(output().contains("1) Vai a: LA1"));
        assertTrue(output().contains("2) Salva ed esci"));
    }

    @Test
    void chiediRispostaMostraLaDomandaERitornaIndiceBase0() {
        Domanda domanda = new Domanda("Cos'e' il DIP?", List.of("X", "Y", "Z"), 0);
        int risposta = view("1\n").chiediRisposta(domanda);
        assertEquals(0, risposta);
        assertTrue(output().contains("Cos'e' il DIP?"));
    }

    @Test
    void chiediTestoRifiutaRigheVuote() {
        String testo = view("\n   \nMario\n").chiediTesto("Nome");
        assertEquals("Mario", testo); // riga vuota e riga di soli spazi scartate
    }

    @Test
    void chiediTestoRimuoveGliSpaziAiBordi() {
        assertEquals("Mario", view("  Mario  \n").chiediTesto("Nome"));
    }

    @Test
    void mostraMessaggioScriveIlTesto() {
        view("").mostraMessaggio("Benvenuto!");
        assertTrue(output().contains("Benvenuto!"));
    }

    @Test
    void aggiornaStatoGiocatoreScriveLeStatistiche() {
        view("").aggiornaStatoGiocatore(new StudenteDto("Mario Rossi", 90, 120, 15, 5, 6, 70));
        assertTrue(output().contains("Mario Rossi"));
        assertTrue(output().contains("HP 90/120"));
        assertTrue(output().contains("CFU 6"));
    }

    @Test
    void costruttoreRifiutaParametriNull() {
        PrintStream stream = new PrintStream(out);
        assertThrows(IllegalArgumentException.class, () -> new ConsoleView(null, stream));
        assertThrows(IllegalArgumentException.class, () -> new ConsoleView(new Scanner(""), null));
    }
}
