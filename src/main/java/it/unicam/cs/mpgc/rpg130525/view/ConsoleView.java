package it.unicam.cs.mpgc.rpg130525.view;

import it.unicam.cs.mpgc.rpg130525.model.Domanda;
import it.unicam.cs.mpgc.rpg130525.port.GameInput;
import it.unicam.cs.mpgc.rpg130525.port.GameView;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class ConsoleView implements GameView, GameInput{
    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleView() {
        this(new Scanner(System.in), System.out);
    }

    public ConsoleView(Scanner scanner, PrintStream out) {
        if (scanner == null || out == null)
            throw new IllegalArgumentException("scanner o out nulli");
        this.scanner = scanner;
        this.out = out;
    }

    @Override
    public void mostraMessaggio(String messaggio) {
        out.println(messaggio);
    }

    @Override
    public void aggiornaStatoGiocatore(StudenteDto studente) {
        out.printf("[%s] HP %d/%d | INT %d | RES %d | CFU %d | Monete %d%n",
                studente.nomeCompleto(),
                studente.saluteMentale(), studente.saluteMentaleMax(),
                studente.intelligenzaEffettiva(), studente.resilienzaEffettiva(),
                studente.cfu(), studente.monete());
    }

    @Override
    public int chiediRisposta(Domanda domanda) {
        out.println(domanda.getTesto());
        List<String> opzioni = domanda.getOpzioni();
        for (int i = 0; i < opzioni.size(); i++)
            out.println("  " + (i + 1) + ") " + opzioni.get(i));

        while (true) {
            out.print("Scegli (1-" + opzioni.size() + "): ");
            String riga = scanner.nextLine().trim();
            try {
                int scelta = Integer.parseInt(riga) - 1;
                if (scelta >= 0 && scelta < opzioni.size())
                    return scelta;
            } catch (NumberFormatException ignored) {}
            out.println("input non valido, riprova.");
        }
    }
}
