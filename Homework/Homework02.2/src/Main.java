import edu.up.gc.AspectRatio;

import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter width: ");
        int width = scanner.nextInt();

        System.out.print("Enter height: ");
        int height = scanner.nextInt();

        AspectRatio aspectRatio = new AspectRatio(width, height);

        System.out.println("Aspect Ratio: " + aspectRatio.calculate());

        scanner.close();
    }

}

