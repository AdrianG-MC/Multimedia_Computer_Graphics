import edu.up.cg.shapes.Circle;
import edu.up.cg.shapes.Shape;
import edu.up.cg.shapes.Square;
import edu.up.cg.tools.IOConsole;
import edu.up.cg.tools.IOHandler;

public class Main {
    public static void  main(String[] args){
        IOHandler console = new IOConsole();
        console.showInfo("Welcome to the Shape Calculator!");
        int option = console.getInt("1) Square\n2) Circle\n3) Triangle", "Please select valid option.");
        Shape shape = null;

        switch (option){
            case 1:
                shape = new Square(console);
                break;
            case 2:
                shape = new Circle(console);
                break;


        }
    }
}
