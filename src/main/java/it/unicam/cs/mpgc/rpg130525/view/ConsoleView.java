package it.unicam.cs.mpgc.rpg130525.view;

import it.unicam.cs.mpgc.rpg130525.model.Domanda;
import it.unicam.cs.mpgc.rpg130525.port.GameInput;
import it.unicam.cs.mpgc.rpg130525.port.GameView;
import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class ConsoleView implements GameView, GameInput {
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
        out.printf("[%s] HP %d/%d | INTELLIGENZA %d | RESILIENZA %d | CFU %d | Monete %d%n",
                studente.nomeCompleto(),
                studente.saluteMentale(), studente.saluteMentaleMax(),
                studente.intelligenzaEffettiva(), studente.resilienzaEffettiva(),
                studente.cfu(), studente.monete());
    }

    @Override
    public int chiediRisposta(Domanda domanda) {
        out.println(domanda.getTesto());
        return leggiScelta(domanda.getOpzioni());
    }

    @Override
    public int scegli(String titolo, List<String> opzioni) {
        out.println(titolo);
        return leggiScelta(opzioni);
    }

    @Override
    public String chiediTesto(String prompt) {
        while (true) {
            out.print(prompt + ": ");
            String riga = scanner.nextLine().trim();
            if (!riga.isBlank())
                return riga;
            out.println("Il testo non puo' essere vuoto, riprova.");
        }
    }

    //mostra le opzioni numerate in base 1 e legge un indice valido, restituendolo in base 0
    private int leggiScelta(List<String> opzioni) {
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
