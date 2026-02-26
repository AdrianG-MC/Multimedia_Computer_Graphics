package edu.up.cg.utils;

/**
 * Responsible only for printing menus to the console.
 * No logic. No input reading.
 */
public class MenuPrinter {
    public void printMainMenu() {
        System.out.println("\n=== IMAGE EDITOR MENU ===");
        System.out.println("1. Crop image");
        System.out.println("2. Invert colors");
        System.out.println("3. Rotate region");
        System.out.println("4. Save image");
        System.out.println("5. Exit");
        System.out.print("Choose option: ");
    }

    public void printInvertMenu() {
        System.out.println("\n1. Invert full image");
        System.out.println("2. Invert selected region");
        System.out.print("Choose option: ");
    }
}
