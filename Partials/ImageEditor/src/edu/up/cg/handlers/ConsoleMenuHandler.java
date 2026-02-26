package edu.up.cg.handlers;

import edu.up.cg.templates.ImageTemplate;
import edu.up.cg.utils.MenuPrinter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ConsoleMenuHandler {
    private Scanner scanner;
    private ImageFileHandler fileHandler;

    private MenuPrinter menuPrinter;
    private InputHandler inputHandler;
    private OperationHandler operationHandler;

    private ImageEditorHandler editorHandler;

    private String currentFileName;

    public ConsoleMenuHandler() {
        scanner = new Scanner(System.in);
        fileHandler = new ImageFileHandler();

        // Initialize UI helpers
        menuPrinter = new MenuPrinter();
        inputHandler = new InputHandler(scanner);
    }

    /**
     * Starts the application.
     */
    public void start() {

        try {
            loadImage();
            initializeOperationHandler();
            runMenuLoop();
        } catch (Exception e) {
            System.out.println("Fatal error: " + e.getMessage());
        }
    }

    /**
     * Loads image from Images/Input folder.
     */
    private void loadImage() throws IOException {

        File[] images = fileHandler.listInputImages();

        System.out.println("\nAvailable images:");

        for (int i = 0; i < images.length; i++) {
            System.out.println((i + 1) + ". " + images[i].getName());
        }

        int choice = inputHandler.readInt("Select image number: ");

        if (choice < 1 || choice > images.length) {
            throw new IllegalArgumentException("Invalid selection.");
        }

        File selectedFile = images[choice - 1];

        currentFileName = selectedFile.getName();

        BufferedImage image = fileHandler.load(selectedFile);

        ImageTemplate model = new ImageTemplate(image);
        editorHandler = new ImageEditorHandler(model);

        System.out.println("Loaded: " + currentFileName);
    }

    /**
     * Initializes operation handler after image is loaded.
     */
    private void initializeOperationHandler() {

        operationHandler = new OperationHandler(
                editorHandler,
                inputHandler,
                menuPrinter
        );
    }

    /**
     * Main menu loop.
     */
    private void runMenuLoop() {

        boolean running = true;

        while (running) {

            menuPrinter.printMainMenu();
            int choice = inputHandler.readInt("");

            switch (choice) {

                case 1 -> operationHandler.handleCrop();
                case 2 -> operationHandler.handleInvert();
                case 3 -> operationHandler.handleRotate();
                case 4 -> handleSave();
                case 5 -> {
                    running = false;
                    System.out.println("Exiting editor...");
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    /**
     * Saves automatically into Images/Edited
     * with originalName_edited.extension
     */
    private void handleSave() {

        try {

            fileHandler.save(
                    editorHandler.getCurrentImage(),
                    currentFileName
            );

            System.out.println("Image saved inside Images/Edited folder!");

        } catch (IOException e) {
            System.out.println("Save failed: " + e.getMessage());
        }
    }
}
