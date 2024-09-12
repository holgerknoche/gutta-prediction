package gutta.prediction.ui;

import gutta.prediction.ui.TransactionEventShape.EventType;
import gutta.prediction.ui.TransactionMarkerShape.TransactionState;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.font.LineMetrics;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

class DemoFrame extends JFrame {

    private static final long serialVersionUID = 2761383897066773507L;

    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";

    private JMenuBar mainMenuBar;

    private JPanel mainPanel;

    public DemoFrame() {
        this.initialize();
    }

    private void initialize() {
        this.setTitle("Trace UI Test");
        this.setSize(new Dimension(1024, 768));

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.add(this.getMainPanel());
        this.setJMenuBar(this.getMainMenuBar());
    }

    private JMenuBar getMainMenuBar() {
        if (this.mainMenuBar == null) {
            this.mainMenuBar = new JMenuBar();

            JMenu fileMenu = new JMenu("File");
            JMenuItem saveMenuItem = new JMenuItem("Save to file");
            saveMenuItem.addActionListener(this::saveTraceAction);

            fileMenu.add(saveMenuItem);
            this.mainMenuBar.add(fileMenu);
        }

        return this.mainMenuBar;
    }

    private JComponent getMainPanel() {
        if (this.mainPanel == null) {
            this.mainPanel = new JPanel();

            this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
            this.mainPanel.add(new SimpleTraceComponent());
            this.mainPanel.add(new DistributedTraceComponent());
        }

        return this.mainPanel;
    }

    private void saveTraceAction(ActionEvent event) {
        this.saveTraceToSvg(new DistributedTraceComponent(), new File("test.svg"));
    }

    private void saveTraceToSvg(TraceComponent component, File file) {
        DOMImplementation domImplementation = GenericDOMImplementation.getDOMImplementation();
        Document document = domImplementation.createDocument(SVG_NAMESPACE, "svg", null);
        SVGGraphics2D svgGraphics = new SVGGraphics2D(document);

        component.paint(svgGraphics);

        try (Writer writer = new FileWriter(file)) {
            svgGraphics.stream(writer, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static abstract class TraceComponent extends JComponent {

        private static final long serialVersionUID = 7164581631239551683L;

        private static final Font TEXT_FONT = Font.decode(Font.SANS_SERIF);

        @Override
        protected final void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            this.paint(graphics2D);
        }

        protected abstract void paint(Graphics2D graphics);

        protected void drawCentered(String text, int x, int y, Graphics2D graphics) {
            FontMetrics metrics = this.getFontMetrics(TEXT_FONT);
            LineMetrics lineMetrics = metrics.getLineMetrics(text, graphics);

            graphics.setFont(TEXT_FONT);
            graphics.drawString(text, x, y + (lineMetrics.getHeight() / 2));
        }

    }

    private static class SimpleTraceComponent extends TraceComponent {

        private static final long serialVersionUID = -814047481506022992L;

        private final List<DrawableShape> shapes = Arrays.asList(new TransactionMarkerShape(150, 10, 200, TransactionState.CLEAN),
                new TransactionMarkerShape(200, 10, 300, TransactionState.DIRTY), new SpanShape(100, 20, 500),
                new TransactionEventShape(150, 25, EventType.READ), new TransactionEventShape(200, 25, EventType.WRITE),
                new TransactionEventShape(300, 25, EventType.COMMIT));

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(600, 100);
        }

        @Override
        protected void paint(Graphics2D graphics) {
            this.drawCentered("Service A", 10, 30, graphics);

            this.shapes.forEach(shape -> shape.drawOn(graphics));
        }

    }

    private static class DistributedTraceComponent extends TraceComponent {

        private static final long serialVersionUID = -4493146564742882814L;

        private final List<DrawableShape> shapes = Arrays.asList(
                // Service A

                new TransactionMarkerShape(130, 0, 150, TransactionState.CLEAN), new TransactionMarkerShape(150, 0, 200, TransactionState.DIRTY),
                new TransactionMarkerShape(200, 0, 450, TransactionState.SUSPENDED), new TransactionMarkerShape(450, 0, 500, TransactionState.DIRTY),

                new SpanShape(100, 10, 500),

                new TransactionEventShape(130, 15, EventType.READ), new TransactionEventShape(150, 15, EventType.WRITE),
                new TransactionEventShape(500, 15, EventType.ABORT),

                // Service B

                new TransactionMarkerShape(200, 50, 310, TransactionState.CLEAN), new TransactionMarkerShape(310, 50, 450, TransactionState.DIRTY),
                new TransactionMarkerShape(450, 50, 500, TransactionState.SUSPENDED),

                new SpanShape(200, 60, 450),

                new TransactionEventShape(270, 65, EventType.STALE_READ), new TransactionEventShape(310, 65, EventType.CONFLICTING_WRITE),

                // Service C

                new TransactionMarkerShape(250, 100, 300, TransactionState.CLEAN), new PrependedLatencyShape(220, 110, 250), new SpanShape(250, 110, 300),
                new AppendedLatencyShape(300, 110, 320));

        @Override
        protected void paint(Graphics2D graphics) {
            this.drawCentered("Service A", 10, 15, graphics);
            this.drawCentered("Service B", 10, 65, graphics);
            this.drawCentered("Service C", 10, 115, graphics);

            this.shapes.forEach(shape -> shape.drawOn(graphics));
        }

    }

}
