package edu.up.cg.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    static String drawingInstructionsIMG1 = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <svg width="640" height="490" viewBox="0 0 640 490"
                 xmlns="http://www.w3.org/2000/svg">
            
                <!-- Background -->
                <rect x="0" y="0" width="640" height="490" fill="#ffffff"/>
            
                <!-- Sun -->
                <circle cx="125" cy="95" r="58" fill="#f4f436"/>
            
                <!-- Sun rays -->
                <g stroke="#c86a6a" stroke-width="2">
                    <!-- vertical / horizontal -->
                    <line x1="125" y1="15"  x2="125" y2="-5"/>
                    <line x1="125" y1="175" x2="125" y2="195"/>
                    <line x1="45"  y1="95"  x2="25"  y2="95"/>
                    <line x1="205" y1="95"  x2="225" y2="95"/>
            
                    <!-- diagonals -->
                    <line x1="75"  y1="45"  x2="60"  y2="30"/>
                    <line x1="175" y1="45"  x2="190" y2="30"/>
                    <line x1="75"  y1="145" x2="60"  y2="160"/>
                    <line x1="175" y1="145" x2="190" y2="160"/>
                </g>
            
                <!-- Grass (repeating smooth bumps) -->
                <path fill="#00f030" stroke="none"
                      d="
                      M 0 370
                      Q 25 330 50 370
                      T 100 370
                      T 150 370
                      T 200 370
                      T 250 370
                      T 300 370
                      T 350 370
                      T 400 370
                      T 450 370
                      T 500 370
                      T 550 370
                      T 600 370
                      T 650 370
                      L 650 490
                      L 0 490
                      Z"/>
            
            </svg>
            """;

    static String drawingInstructionsIMG2 = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <svg width="400" height="300" viewBox="0 0 400 300"
                 xmlns="http://www.w3.org/2000/svg">
            
                <!-- Upper-right triangle (Red) -->
                <polygon points="0,0 400,0 400,300"
                         fill="#ff1a1a"/>
            
                <!-- Lower-left triangle (Blue) -->
                <polygon points="0,0 0,300 400,300"
                         fill="#1a1aff"/>
            </svg>
            
            """;



    public static void main(String[] args){
        File outputFile = new File("src/edu/up/cg/Images/Image2.svg");
        try (FileWriter draw = new FileWriter(outputFile)){
            draw.write(drawingInstructionsIMG2);
            System.out.println("\nThe image was successfully created.");
        } catch (IOException e){
            System.out.println("\nError creating the file due to "+ e.getMessage());
        }

    }
}
