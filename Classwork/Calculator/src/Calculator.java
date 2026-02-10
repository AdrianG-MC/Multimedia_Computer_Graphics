import java.util.Scanner;

public class Calculator {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int option;

        do {
            System.out.println("AREA AND PERIMETER CALCULATOR");
            System.out.println("1. Square");
            System.out.println("2. Rectangle");
            System.out.println("3. Triangle");
            System.out.println("4. Circle");
            System.out.println("5. Regular Pentagon");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");
            option = scanner.nextInt();

            switch (option) {

                case 1:
                    // Square
                    System.out.print("Enter the side of the square: ");
                    double side = scanner.nextDouble();

                    double squareArea = side * side;
                    double squarePerimeter = 4 * side;

                    System.out.println("Area: " + squareArea);
                    System.out.println("Perimeter: " + squarePerimeter);
                    break;

                case 2:
                    // Rectangle
                    System.out.print("Enter the length: ");
                    double length = scanner.nextDouble();
                    System.out.print("Enter the width: ");
                    double width = scanner.nextDouble();

                    double rectangleArea = length * width;
                    double rectanglePerimeter = 2 * (length + width);

                    System.out.println("Area: " + rectangleArea);
                    System.out.println("Perimeter: " + rectanglePerimeter);
                    break;

                case 3:
                    // Triangle
                    System.out.print("Enter base: ");
                    double base = scanner.nextDouble();
                    System.out.print("Enter height: ");
                    double height = scanner.nextDouble();
                    System.out.print("Enter side 1: ");
                    double side1 = scanner.nextDouble();
                    System.out.print("Enter side 2: ");
                    double side2 = scanner.nextDouble();
                    System.out.print("Enter side 3: ");
                    double side3 = scanner.nextDouble();

                    double triangleArea = (base * height) / 2;
                    double trianglePerimeter = side1 + side2 + side3;

                    System.out.println("Area: " + triangleArea);
                    System.out.println("Perimeter: " + trianglePerimeter);
                    break;

                case 4:
                    // Circle
                    System.out.print("Enter the radius: ");
                    double radius = scanner.nextDouble();

                    double circleArea = Math.PI * radius * radius;
                    double circlePerimeter = 2 * Math.PI * radius;

                    System.out.println("Area: " + circleArea);
                    System.out.println("Perimeter: " + circlePerimeter);
                    break;

                case 5:
                    System.out.print("Enter the side length: ");
                    double pentagonSide = scanner.nextDouble();

                    double pentagonPerimeter = 5 * pentagonSide;
                    double pentagonArea = (5 * pentagonSide * pentagonSide) /
                            (4 * Math.tan(Math.PI / 5));
                    System.out.println("Area: " + pentagonArea);
                    System.out.println("Perimeter: " + pentagonPerimeter);
                    break;

                case 6:
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option. Try again.");
            }

            System.out.println();

        } while (option != 6);

        scanner.close();
    }
}
