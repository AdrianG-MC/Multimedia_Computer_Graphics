package edu.up.cg.handlers;

import java.util.Scanner;

/**
 * Responsible only for reading user input safely.
 */
public class InputHandler {

    private Scanner scanner;

    public InputHandler(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    public String readString(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    public int[] readCoordinates(){
        int x1 = readInt("Top Left X: ");
        int y1 = readInt("Top Left Y: ");
        int x2 = readInt("Bottom Right X: ");
        int y2 = readInt("Bottom Right Y: ");
        return new int[]{x1, y1, x2, y2};
    }
}
