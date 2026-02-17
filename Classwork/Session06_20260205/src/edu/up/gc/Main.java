import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public static void main(String[] args) {
    int multiplier = 2;
    int width = 400 * multiplier;
    int height = 400 * multiplier;
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    // Triangular gradient

    double Ax = 0, Ay = height;
    double Bx = width, By = height;
    double Cx = width / 2d, Cy = 0;

    double denominator = ((By - Cy) * (Ax - Cx) + (Cx - Bx) * (Ay - Cy));

    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            double lambda1 = ((By - Cy) * (x - Cx) + (Cx - Bx) * (y - Cy)) / denominator;
            double lambda2 = ((Cy - Ay) * (x - Cx) + (Ax - Cx) * (y - Cy)) / denominator;
            double lambda3 = 1 - lambda1 - lambda2;

            if (lambda1 >= 0 && lambda2 >= 0 && lambda3 >= 0) {
                int r = (int) (lambda1 * 255);
                int g = (int) (lambda2 * 255);
                int b = (int) (lambda3 * 255);

                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
            }
        }


        File outputImage = new File("image.jpg");

        try {
            ImageIO.write(image, "jpg", outputImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}




//    The triangle is defined by 3 points:
//    A(x1,y1)
//    B(x2,y2)
//    C(x3,y3)
//    Each pixel we verify if we are inside those coords:
//      P(x,y) = lambda1 * (A) + lambda2 * (B) + lambda3 * (C)
//    Where:
//      lambda1 + lambda2 + lambda3 = 1
//      lambda1 = ((By - Cy) * (x - Cx) + (Cx - Bx) * (y - Cy)) / denominator
//      lambda2 = ((Cy - Ay) * (x - Cx) + (Ax - Cx) * (y - Cy)) / denominator
//      lambda3 = 1 - lambda1 - lambda2
//      denominator = ((By - Cy) * (Ax - Cx) + (Cx - Bx) * (Ay - Cy))

//    P it's inside the triangle when 0-1
//      lambda1 >= 0
//      lambda2 >= 0
//      lambda3 >= 0
//
//    Defining a triangle with the coords at:
//    A(0,height)
//    B(width/2,0)
//    C(width,height)
//

//public static void drawTriangle(BufferedImage image, int width, int height){
////        Here we define the coords of the triangle limits:
//    double Ax = 0, Ay = 400;
//    double Bx = 400/2d, By = 0;
//    double Cx = 400, Cy = 400;
//
//    double denominator = ((By - Cy) * (Ax - Cx) + (Cx - Bx) * (Ay - Cy));
//
//    for (int x = 0; x < width; x++){
//        for (int y = 0; y < height; y++){
//            double lambda1 = ((By - Cy) * (x - Cx) + (Cx - Bx) * (y - Cy)) / denominator;
//            double lambda2 = ((Cy - Ay) * (x - Cx) + (Ax - Cx) * (y - Cy)) / denominator;
//            double lambda3 = 1 - lambda1 - lambda2;
//
//            if (lambda1 >= 0 && lambda2 >= 0 && lambda3 >= 0  )
//                image.setRGB(x, y, Color.green.getRGB());
//        }
//    }
//
//
//
//
//
//}


