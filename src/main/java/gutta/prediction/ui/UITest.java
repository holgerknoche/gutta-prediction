package gutta.prediction.ui;

public class UITest {

    public static void main(String[] arguments) {
        var testCase = (arguments.length > 0) ? arguments[0] : "shapes";

        var frameToShow = switch (testCase) {        
        case "span" -> new SpanViewFrame();
        case "span-simulation" -> new SpanSimulationFrame();
        case "use-cases" -> new TestUseCaseOverviewFrame();
        default -> new SpanViewFrame();
        };

        frameToShow.setVisible(true);
    }

}
