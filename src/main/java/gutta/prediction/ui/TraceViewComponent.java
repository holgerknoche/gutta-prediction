package gutta.prediction.ui;

import gutta.prediction.span.Trace;
import gutta.prediction.ui.shape.DrawableShape;
import gutta.prediction.ui.shape.SpanComponentsCreator;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

class TraceViewComponent extends TraceComponent {

    private static final long serialVersionUID = 29828340270697690L;
    
    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
    
    private static final int BORDER_WIDTH = 20;
    
    private static final int VERTICAL_DISTANCE_BETWEEN_SPANS = 60;
    
    private static final Dimension DEFAULT_DIMENSION = new Dimension(100, 100);
                
    private Dimension preferredSize = DEFAULT_DIMENSION;
    
    private InitializeOnce<JPopupMenu> popupMenu = new InitializeOnce<>(this::createPopupMenu);
    
    private List<DrawableShape> shapes = new ArrayList<>();
    
    public TraceViewComponent() {
        this.setComponentPopupMenu(this.popupMenu.get());
    }
    
    private JPopupMenu createPopupMenu() {
        var menu = new JPopupMenu();
        
        var saveToSvgItem = new JMenuItem("Save to SVG...");
        saveToSvgItem.addActionListener(this::saveToSvgAction);
        menu.add(saveToSvgItem);
        
        return menu;
    }
    
    public void trace(Trace trace) {
        if (trace == null) {
            // Delete all shapes
            this.shapes = new ArrayList<>();
            this.preferredSize = DEFAULT_DIMENSION;
            return;
        }
        
        var startTimestamp = trace.startTimestamp();
        var endTimestamp = trace.endTimestamp();
        
        // TODO Use the duration to calculate the necessary size / zoom factor
        var duration = (endTimestamp - startTimestamp);
        // Calculate X offset (based on the longest span name)
        var spanNamesDimensions = new SpanNameSpaceCalculator(BORDER_WIDTH, VERTICAL_DISTANCE_BETWEEN_SPANS, this.getFontMetrics(TEXT_FONT), this.getGraphics()).calulateSpaceForSpanNames(trace);        
        var xOffset = spanNamesDimensions.width + (3 * BORDER_WIDTH);
                
        // Build the necessary shapes and repaint
        this.shapes = new SpanComponentsCreator(startTimestamp, xOffset, BORDER_WIDTH, VERTICAL_DISTANCE_BETWEEN_SPANS).createShapesFor(trace);
        
        // Calculate the required dimensions (two border widths to separate the names from the spans)
        var preferredXSize = spanNamesDimensions.width + (int) duration + (4 * BORDER_WIDTH);
        this.preferredSize = new Dimension(preferredXSize, spanNamesDimensions.height);
        
        this.repaint();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return this.preferredSize;
    }
    
    @Override
    protected void paint(Graphics2D graphics) {
        this.shapes.forEach(shape -> shape.drawOn(graphics));
    }    
   
    private void saveToSvgAction(ActionEvent event) {
        var optionalFile = this.loadFileWithDialog();
        
        optionalFile.ifPresent(this::saveToSvg);
    }
    
    private Optional<File> loadFileWithDialog() {
        var openDialog = new FileDialog((Frame) null);
        openDialog.setMultipleMode(false);
        openDialog.setVisible(true);

        var selectedFiles = openDialog.getFiles();
        if (selectedFiles.length == 0) {
            return Optional.empty();
        } else {
            return Optional.of(selectedFiles[0]);            
        }
    }
    
    private void saveToSvg(File file) {
        var domImplementation = GenericDOMImplementation.getDOMImplementation();
        var document = domImplementation.createDocument(SVG_NAMESPACE, "svg", null);
        var svgGraphics = new SVGGraphics2D(document);

        this.paint(svgGraphics);

        try (var writer = new FileWriter(file)) {
            svgGraphics.stream(writer, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
