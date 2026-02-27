package edu.up.cg.handlers;

import edu.up.cg.utils.MenuPrinter;

/**
 *
 * Translates user selections and raw coordinate input into
 * concrete calls to ImageEditorHandler's editing methods.
 */
public class OperationHandler {

    private ImageEditorHandler editorHandler;
    private InputHandler inputHandler;
    private MenuPrinter menuPrinter;

    /**
     * Constructs an OperationHandler with all required dependencies.
     *
     * @param editorHandler The editor service to delegate operations to.
     * @param inputHandler  Input reader for coordinates and choices.
     * @param menuPrinter   Printer for sub-menus (e.g. invert mode selection).
     */
    public OperationHandler(ImageEditorHandler editorHandler, InputHandler inputHandler, MenuPrinter menuPrinter) {
        this.editorHandler = editorHandler;
        this.inputHandler = inputHandler;
        this.menuPrinter = menuPrinter;
    }

    /**
     * Handles the Crop operation from the main menu.
     *
     * Workflow:
     *   1. Ask user for two corner coordinates (top-left, bottom-right).
     *   2. Pass them to ImageEditorHandler.crop().
     *   3. After crop, the canvas is permanently resized to the selection.
     *
     * Errors (e.g. out-of-bounds coordinates) are caught and displayed
     * to the user without crashing the application.
     */
    public void handleCrop(){
        try{
            int[] coords = inputHandler.readCoordinates();
            // coords = { x1, y1, x2, y2 }
            editorHandler.crop(coords[0], coords[1], coords[2], coords[3]);
            System.out.println("Crop complete");
        } catch(Exception e){
            System.out.println("Error" + e.getMessage());
        }
    }


    /**
     * Handles the Invert operation from the main menu.
     * <p>
     * Shows a sub-menu asking where to invert:
     *   1. The full image (every pixel).
     *   2. A specific rectangular region.
     * <p>
     * Invert replaces each color channel C with (255 − C),
     * creating a photographic negative effect.
     * Alpha (transparency) is preserved.
     */
    public void handleInvert(){

        // Print the invert sub-menu (Full / Region).
        menuPrinter.printInvertMenu();
        int option = inputHandler.readInt("");

        try{
            switch (option){
                case 1:     // Full image Invert
                    editorHandler.invertFull();
                    System.out.println("Full image inverted complete");
                    break;
                case 2:     // Region Invert
                    int[] coords = inputHandler.readCoordinates();
                    editorHandler.invertRegion(coords[0], coords[1], coords[2], coords[3]);
                    System.out.println("Region inverted complete");
                    break;
                default:
                    System.out.println("Invalid option");
            }
        } catch(Exception e){
            System.out.println("Error" + e.getMessage());
        }
    }

    /**
     * Handles the Rotate operation from the main menu.
     *
     * Workflow:
     *   1. Ask user for two corner coordinates defining the region to rotate.
     *   2. Ask user for the rotation angle (must be 90, 180, or 270).
     *   3. Validate the angle — reject anything outside the allowed set.
     *   4. Delegate to ImageEditorHandler.rotate().
     *
     * The rotation is clockwise. The canvas size does not change.
     * Pixels outside the selected region are untouched.
     */
    public void handleRotate(){

        try {
            int[] coords = inputHandler.readCoordinates();
            int angle = inputHandler.readInt("Only Options available are 90, 180 and 270.\nEnter angle: ");

            if(angle != 90 && angle != 180 && angle != 270){
                System.out.println("Invalid angle");
                return;
            }

            editorHandler.rotate(coords[0], coords[1], coords[2], coords[3], angle);
            System.out.println("Rotation complete");
        }catch(Exception e){
            System.out.println("Error" + e.getMessage());
        }
    }


}
