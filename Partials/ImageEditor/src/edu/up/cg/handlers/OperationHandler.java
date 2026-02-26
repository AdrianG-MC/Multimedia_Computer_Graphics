package edu.up.cg.handlers;

import edu.up.cg.utils.MenuPrinter;

public class OperationHandler {

    private ImageEditorHandler editorHandler;
    private InputHandler inputHandler;
    private MenuPrinter menuPrinter;

    public OperationHandler(ImageEditorHandler editorHandler, InputHandler inputHandler, MenuPrinter menuPrinter) {
        this.editorHandler = editorHandler;
        this.inputHandler = inputHandler;
        this.menuPrinter = menuPrinter;
    }


    public void handleCrop(){
        try{
            int[] coords = inputHandler.readCoordinates();
            editorHandler.crop(coords[0], coords[1], coords[2], coords[3]);
            System.out.println("Crop complete");
        } catch(Exception e){
            System.out.println("Error" + e.getMessage());
        }
    }

    public void handleInvert(){

        menuPrinter.printInvertMenu();
        int option = inputHandler.readInt("");

        try{
            switch (option){
                case 1:
                    editorHandler.invertFull();
                    System.out.println("Full image inverted complete");
                    break;
                case 2:
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
