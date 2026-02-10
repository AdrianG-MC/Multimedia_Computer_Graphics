package edu.up.cg.images;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;



public class Main {
    public static void main(String[] args){


        int multiplier = 2;
        int width = 400 * multiplier;
        int height = 300 * multiplier;
        BufferedImage image = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);


//        Setting a single pixel in yellow
//        image.setRGB(200,200, Color.yellow.getRGB());

//        Setting a whole array of pixels into a color
//        for (int x = 0; x < 400; x++) {
//            for (int y = 0; y < 100; y++) {
//
//                image.setRGB(x,y, Color.red.getRGB());
//            }
//        }

//        Classwork 01
//      ---------- Drawing a 2 color diagonal-divided image ----------
//        for (int x = 0; x < (width-1); x++) {
//            for (int y = 0; y < (height-1); y++) {
//                if(x * height > y * width){
//                    image.setRGB(x,y, Color.red.getRGB());
//                } else{
//                    image.setRGB(x,y, Color.blue.getRGB());
//                }
//            }
//        }


//      Classwork 01
//      ---------- Drawing a Clock ----------

//      Circle params using (h,k) as the center of the circle
        int centerX = width/2;                      //We use centerX as h
        int centerY = height/2;                     //We use centerY as k
        int radius = 100 * multiplier;              //The radius is fixed, but it changes based on the multiplier of the whole image.
        int circleColor = Color.white.getRGB();


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
            image.setRGB(x, y, circleColor);
        }

//      Making the hour marks

//        Knowing that the clock have 12 hours, the circle needs to be marked 12 times.
//        Taking degrees in consideration, we need to divide the full rotation into 12, so:
//          360°/12 = 30° each hour, but as we don't use degrees, we need to convert into radians:
//              360° = (2 * PI), so,   30° =  (2 * PI) / 12 = PI / 6.
//
//        Angle of each hour is determined by:
//          thetaHour = hour * (PI / 6)

//        Considering that our system in java start with our angle theta facing right and the angles
//        grow counterclockwise, we need to adjust the equation by subtracting 90 degrees or (PI/2).
//          thetaHour = hour * (PI / 6) - (PI / 2)

//        This would be needed if we add numbers so the 12 marks stay in the correct position.

        int hourMarkOffset = (int)(radius * 0.2);

        for (int hours = 0; hours < 12; hours++) {                          // Loop controlled by the hours
            double thetaHour = ( Math.PI * hours / 6) - (Math.PI / 2);          // Equation previously explained.
            int x = (int)(centerX + (radius - hourMarkOffset) * Math.cos(thetaHour));       // These equations are the same of the circle, but we subtract 40px so it doesn't overlap the circumference
            int y = (int)(centerY + (radius - hourMarkOffset) * Math.sin(thetaHour));
            image.setRGB(x, y, circleColor);
        }

//      Making the clock hands
        
//      Hands Params
        int minute = 5;
        int hour = 10;

//      For the minutes hand, 60 minutes it's a full rotation or 2PI so the angle would be:
//            thetaMin = ((2 * PI) / 60) * minute = (PI / 30) * minute
//      Making the same adjustment as before, putting the 12th mark at the top (subtract PI/2):
//            thetaMin = ((PI / 30) * minute) - (PI / 2)

//      For the hours (with the adjustment),  12 hours it's a full rotation (2PI), so 1 hour its PI/6:
//            thetaHour =  ((PI / 6) * hour) * (PI / 2)

        double thetaMin = (((Math.PI / 30) * minute) - (Math.PI/2));
        double thetaHour = (((Math.PI / 6) * hour) - (Math.PI/2));

//      We mark the end point of the hands similar to the hours mark

        int minLength = (int)(radius * 0.7);
        int hourLength = (int)(radius * 0.45);

//      Using these loops using the polar coordinates, we use d as a distance marker which increments and draw a pixel each iteration.
        for (int d = 0; d <= minLength; d++) {
            int x = (int)(centerX + d * Math.cos(thetaMin));
            int y = (int)(centerY + d * Math.sin(thetaMin));
            image.setRGB(x, y, circleColor);
        }

        for (int d = 0; d <= hourLength; d++) {
            int x = (int)(centerX + d * Math.cos(thetaHour));
            int y = (int)(centerY + d * Math.sin(thetaHour));
            image.setRGB(x, y, circleColor);
        }


//      Classwork 01
//      ---------- Drawing the sun a sine wave as grass ----------

//      We paint the whole background white by coloring out each pixel with white color.

        for (int y = 0; y < height; y++){
           for(int x = 0; x < width; x++){
               image.setRGB(x, y, Color.white.getRGB());
           }
        }

//        Making the sun

        int sunX = 150;
        int sunY = 130;
        int sunRadius = 40 * multiplier;

//        The math function that represents a point that belongs to a circle if:
//          (x - sunX)^2 + (y - sunY)^2 <= sunRadius^2

        for (int y = sunY - sunRadius; y <= sunY + sunRadius; y++){
            for (int x = sunX - sunRadius; x <= sunX + sunRadius; x++){
                int dx = x - sunX;
                int dy = y - sunY;

                if (dx*dx + dy*dy <= sunRadius*sunRadius){
                    image.setRGB(x, y, Color.yellow.getRGB());
                }
            }
        }





       int rayCount = 8;
       int rayLength = 60 * multiplier;

       for (int i = 0; i < rayCount; i++) {

           double theta = (2 * Math.PI / rayCount) * i;

           for (int d = sunRadius; d <= rayLength; d++) {

               int x = (int)(sunX + d * Math.cos(theta));
               int y = (int)(sunY + d * Math.sin(theta));

               image.setRGB(x, y, Color.RED.getRGB());
           }
       } 




//        Making the grass using a sin wave

//        Using the sin function

        int grassBaseY = height - (60 * multiplier);
        int amplitude = 30;
        double frequency = 0.05;

        for (int x = 0; x < width; x++){
            int waveY = (int)(grassBaseY + amplitude * Math.sin(frequency*x));

            for (int y = waveY; y < height; y++){
                image.setRGB(x, y, Color.green.getRGB());
            }
        }














        File outputImage = new File("image.jpg");

        try{
            ImageIO.write(image, "jpg", outputImage);
        } catch(IOException e) {
            throw  new RuntimeException(e);
        }
    }
}



