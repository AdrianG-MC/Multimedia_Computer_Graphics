package edu.up.cg.handlers;

import edu.up.cg.templates.ImageTemplate;
import edu.up.cg.utils.MenuPrinter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;


/**
 * Leads the application lifecycle.
 *
 */
public class ConsoleMenuHandler {
//    Dependencies


    private Scanner scanner;
    private ImageFileHandler fileHandler;

    private MenuPrinter menuPrinter;
    private InputHandler inputHandler;
    private OperationHandler operationHandler;

    private ImageEditorHandler editorHandler;

    private String currentFileName;


    /**
     * Constructs the ConsoleMenuHandler and initialize stateless helpers/handlers.
     *
     */
    public ConsoleMenuHandler() {
        scanner = new Scanner(System.in);
        fileHandler = new ImageFileHandler();

        // Initialize UI helpers
        menuPrinter = new MenuPrinter();
        inputHandler = new InputHandler(scanner);
    }

    /**
     * Starts the Image Editor lifecycle.
     *
     * Sequence:
     * 1. Load an image from the Input folder.
     * 2. Wire the operation handler when the image is loaded
     * 3. Enter the runMenuLoop.
     *
     * Ifs there any unrecovered exception during loading, its printed as a
     * fatal error and ends the lifecycle.
     */
    public void start() {

        try {
            loadImage();
            initializeOperationHandler();
            runMenuLoop();
        } catch (Exception e) {
            System.out.println("Fatal error: " + e.getMessage());
            System.out.println("The application will now exit.");
        }
    }

    /**
     * Lists images in the Input folder, message the user to pick one,
     * loads it into a BufferedImage, and wraps it in an ImageEditorHandler.
     *
     * After this method returns:
     * -{@code currentFileName} holds the chosen file name
     * -{@code editorHandler} is ready for any operations.
     *
     * @throws IOException if the selected file cant be read
     * @throws IllegalArgumentException if the user enters an out-of-range number.
     *
     */
    private void loadImage() throws IOException {

        // Ask ImageFileHandler to scan the input folder.
        File[] images = fileHandler.listInputImages();

        // Shows the Available Images.
        System.out.println("\nAvailable images:");
        for (int i = 0; i < images.length; i++) {
            System.out.println((i + 1) + ". " + images[i].getName());
        }

        //  Tells the user to select one from the list (1-based index)
        int choice = inputHandler.readInt("Select image number: ");

        if (choice < 1 || choice > images.length) {
            throw new IllegalArgumentException(
                    "Invalid selection." + choice +
                    ". Please choose from 1 to " + (images.length) + "."
            );
        }

        File selectedFile = images[choice - 1];
        currentFileName = selectedFile.getName();

        // Decode the image file into a BufferedImage (this uses the pixels in memory / raster).
        BufferedImage image = fileHandler.load(selectedFile);

        // Wrap in ImageTemplate so operations can swap the image
        ImageTemplate model = new ImageTemplate(image);
        editorHandler = new ImageEditorHandler(model);

        System.out.println("Loaded: " + currentFileName +
                " (" + image.getWidth() + "×" + image.getHeight() + " px)");
    }

    /**
     * Creates the OperationHandler now that editorHandler is available.
     *
     * Called once after loadImage() succeeds.
     * Keeping it separated to make the construction order explicit.
     */
    private void initializeOperationHandler() {

        operationHandler = new OperationHandler(
                editorHandler,
                inputHandler,
                menuPrinter
        );
    }

    /**
     * Runs the main interactive menu loop.
     *
     * Keeps presenting the menu and executing the chosen action until
     * the user selects option 5 (Exit).
     *
     * Menu options:
     *   1 – Crop     (resize canvas to a selected area)
     *   2 – Invert   (negate colours, full image or region)
     *   3 – Rotate   (rotate a region 90°/180°/270°)
     *   4 – Save     (write result to Images/Edited/)
     *   5 – Exit     (quit without saving if not already saved)
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
     * Saves the current (also if its edited or not) image to the Edited folder.
     *
     * Delegates to ImageFileHandler.save(), which automatically appends
     * "_edited" to the original file name:
     *   photo.png  →  Images/Edited/photo_edited.png
     *
     * If saving fails (e.g. disk full, unsupported format), the error is
     * displayed and the loop continues — the session is not terminated.
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
