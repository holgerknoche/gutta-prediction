package gutta.prediction.ui;

public class UITest {

    public static void main(String[] arguments) {
        var testCase = (arguments.length > 0) ? arguments[0] : "shapes";

        var frameToShow = switch (testCase) {        
        case "shapes" -> new DemoFrame();
        case "span" -> new SpanViewFrame();
        default -> new DemoFrame();
        };

        frameToShow.setVisible(true);
    }

}
