import edu.up.cg.CartesianCoordinate;
import edu.up.cg.PolarCoordinate;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {


        Scanner scanner = new Scanner(System.in);
        int option;

        do {
            System.out.println("\n--- Coordinate Converter ---");
            System.out.println("1. Polar to Cartesian");
            System.out.println("2. Cartesian to Polar");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            option = scanner.nextInt();

            switch (option) {
                case 1:
                    System.out.print("Enter r: ");
                    double r = scanner.nextDouble();

                    System.out.print("Enter theta (degrees): ");
                    double theta = scanner.nextDouble();

                    PolarCoordinate polar = new PolarCoordinate(r, theta);
                    CartesianCoordinate cartesian = polar.toCartesian();

                    System.out.printf("x = %.4f, y = %.4f%n",
                            cartesian.getX(), cartesian.getY());
                    break;

                case 2:
                    System.out.print("Enter x: ");
                    double x = scanner.nextDouble();

                    System.out.print("Enter y: ");
                    double y = scanner.nextDouble();

                    CartesianCoordinate cart = new CartesianCoordinate(x, y);
                    PolarCoordinate pol = cart.toPolar();

                    System.out.printf("r = %.4f, theta = %.4f degrees%n",
                            pol.getR(), pol.getTheta());
                    break;

                case 0:
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option.");
            }

        } while (option != 0);

        scanner.close();
    }
}
