package edu.up.cg.utils;

public final class ConsoleLogger {

    private ConsoleLogger() {}

    public static void banner() {
        String line = "═".repeat(56);
        System.out.println(line);
        System.out.println("  ✈  VIDEO CREATOR — AI-Powered Travel Videos");
        System.out.println(line);
        System.out.println();
    }

    public static void step(int number, String message) {
        System.out.println();
        System.out.println("── Step " + number + ": " + message + " ──");
    }

    public static void info(String message) {
        System.out.println("  " + message);
    }

    public static void progress(String message) {
        System.out.println("  → " + message);
    }

    public static void ok(String message) {
        System.out.println("  ✔ " + message);
    }

    public static void error(String message) {
        System.err.println("\n[ERROR] " + message + "\n");
    }

    public static void done(String outputPath) {
        String line = "═".repeat(56);
        System.out.println();
        System.out.println(line);
        System.out.println("VIDEO CREATED SUCCESSFULLY!");
        System.out.println("  " + outputPath);
        System.out.println(line);
        System.out.println();
    }
}
