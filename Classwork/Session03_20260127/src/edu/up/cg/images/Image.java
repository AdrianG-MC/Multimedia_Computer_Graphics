package edu.up.cg.images;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Image {
}


public static void drawDiagonal(String fileName, String fileType){
    BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);

    for (int x = 0; x < (image.getWidth()); x++) {
        for (int y = 0; y < (image.getHeight()); y++) {
            if(x * image.getHeight() > y * image.getWidth()){
                image.setRGB(x,y, Color.red.getRGB());
            } else{
                image.setRGB(x,y, Color.blue.getRGB());
            }
        }
    }
}

public static void drawClock(String fileName, String fileType){
    BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);


}

public static void drawCircle(BufferedImage image, int centerX, int centerY, int radius, int thickness, double steps, Color color){
//        Making the circle as a "Parametric Circle with Polar cords".
//        The circumference can be described as a function of the angle, in this case the angle is in radians.
//          x(theta) = radius * cos(theta)
//          y(theta) = radius * sin(theta)

//        As we need to move the circle, we need the (h,k) coords where the center of the circle would be located.
//            x = h + (radius * cos(theta))
//            y = k + (radius * sin(theta))

//        ----- REMEMBER -----
//        The equation of the circumference = 2 * PI * radius;
//        2 * Math.PI equals 360 deg, a full rotation.

//        The angular step or the increment must be equally separated, so it adjust in function of the radius to make it smoother. It would need to make 2*PI*r steps.
//        Angular step = (2 * PI) / (2 * PI * radius) = 1 / radius
    for (double theta = 0; theta < (2 * Math.PI); theta += ((double) 1 / radius)){   // We use theta as the angle, while the step is that way to prevent holes
        // Conversion polar -> cartesian
        int x = (int)(centerX + (radius * Math.cos(theta)));
        int y = (int)(centerY + (radius * Math.sin(theta)));
        image.setRGB(x, y, color.getRGB());
    }
}

public static void drawLine(BufferedImage image, int x1, int y1, int x2, int y2, Color color){
    int dx = x2 - x1;
    int dy = y2 - y1;

    for(float i = 0; i <= 1; i += 0.1f){
        int x = (int) (x1 + i * dx);
        int y = (int) (y1 + i * dy);

        if (x >= 0 && image.getWidth() && y >= 0 )

    }

}

public static void drawGradient(BufferedImage image, Color color1, Color color2){

}


public static void saveImage(BufferedImage image, String fileName, String fileType){
    File file = new File(fileName + "." + fileType);
    try{
        ImageIO.write(image, fileType, file);
    } catch (IOException e){
        throw new RuntimeException(e);
    }
}

