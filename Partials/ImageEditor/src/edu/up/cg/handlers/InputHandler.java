package edu.up.cg.handlers;

import java.util.Scanner;

/**
 * Wraps a Scanner to provide safe, user-friendly methods for
 * reading integers and strings from standard input.
 *
 */
public class InputHandler {

    private Scanner scanner;


    /**
     * Constructs an InputHandler with the given Scanner.
     *
     * The Scanner is injected rather than created here so the same
     * instance can be reused through the lifecycle without opening
     * System.in a lot.
     *
     * @param scanner A scanner connected to System.in (input stream)
     */
    public InputHandler(Scanner scanner) {
        this.scanner = scanner;
    }


    /**
     * Message the user, reads a line, and parses it as an integer.
     *
     * @param message The "prompt" to display before reading input.
     * @return The integer value entered by the user.
     */
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

    /**
     * Prompts the user and reads a line of text.
     *
     * @param message The "prompt" to display.
     * @return The raw string entered by the user.
     */
    public String readString(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }


    /**
     * Reads two corner coordinates that define a rectangle.
     *
     * Prompts the user for four values:
     *   - Top-left  X (column of the left edge)
     *   - Top-left  Y (row of the top edge)
     *   - Bottom-right X (column just past the right edge, exclusive)
     *   - Bottom-right Y (row just past the bottom edge, exclusive)
     *
     * @return int[4] containing {x1, y1, x2, y2}.
     */
    public int[] readCoordinates(){
        int x1 = readInt("Top Left X: ");
        int y1 = readInt("Top Left Y: ");
        int x2 = readInt("Bottom Right X: ");
        int y2 = readInt("Bottom Right Y: ");
        return new int[]{x1, y1, x2, y2};
    }
}
