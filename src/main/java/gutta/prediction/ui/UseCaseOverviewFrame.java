package gutta.prediction.ui;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.event.codec.EventTraceDecoder;
import gutta.prediction.span.TraceBuilder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;

public class UseCaseOverviewFrame extends JFrame {

    private static final long serialVersionUID = 1827116057177051262L; 
    
    private final InitializeOnce<JPanel> mainPanel = new InitializeOnce<>(this::createMainPanel);
    
    private final InitializeOnce<JScrollPane> tracesPane = new InitializeOnce<>(this::createTracesPane);
    
    private final InitializeOnce<JList<EventTraceView>> tracesList = new InitializeOnce<>(this::createTracesList);
    
    private final InitializeOnce<JScrollPane> traceViewPane = new InitializeOnce<>(this::createTraceViewPane);
    
    private final InitializeOnce<TraceViewComponent> traceView = new InitializeOnce<>(this::createTraceView);
    
    private DeploymentModel deploymentModel;
    
    private Collection<EventTrace> traces;
    
    public UseCaseOverviewFrame() {
        this.initialize();
        this.initializeControls();
        this.initializeData();
    }
    
    private void initialize() {
        this.setTitle("Use Case Overview");
        this.setSize(new Dimension(1024, 768));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initializeControls() {
        this.setLayout(new BorderLayout());
        
        this.add(this.mainPanel.get(), BorderLayout.CENTER);
    }        
    
    private void initializeData() {
        var useCase1 = new UseCase("Use Case 1");
        var useCase2 = new UseCase("Use Case 2");
        var useCase3 = new UseCase("Use Case 3");
        
        var serviceCandidate1 = new ServiceCandidate("Service Candidate 1", TransactionBehavior.SUPPORTED);
        var serviceCandidate2 = new ServiceCandidate("Service Candidate 2", TransactionBehavior.SUPPORTED);
        var serviceCandidate3 = new ServiceCandidate("Service Candidate 3", TransactionBehavior.SUPPORTED);
        var serviceCandidate4 = new ServiceCandidate("Service Candidate 4", TransactionBehavior.SUPPORTED);
        var serviceCandidate5 = new ServiceCandidate("Service Candidate 5", TransactionBehavior.SUPPORTED);
        var serviceCandidate6 = new ServiceCandidate("Service Candidate 6", TransactionBehavior.SUPPORTED);
        var serviceCandidate7 = new ServiceCandidate("Service Candidate 7", TransactionBehavior.SUPPORTED);
        var serviceCandidate8 = new ServiceCandidate("Service Candidate 8", TransactionBehavior.SUPPORTED);
        var serviceCandidate9 = new ServiceCandidate("Service Candidate 9", TransactionBehavior.SUPPORTED);
        var serviceCandidate10 = new ServiceCandidate("Service Candidate 10", TransactionBehavior.SUPPORTED);
        
        var component1 = new Component("Component 1");
        var component2 = new Component("Component 2");
        var component3 = new Component("Component 3");
        
        this.deploymentModel = new DeploymentModel.Builder()
                .assignUseCase(useCase1, component1)
                .assignUseCase(useCase2, component2)
                .assignUseCase(useCase3, component3)
                .assignServiceCandidate(serviceCandidate1, component1)
                .assignServiceCandidate(serviceCandidate2, component2)
                .assignServiceCandidate(serviceCandidate3, component3)
                .assignServiceCandidate(serviceCandidate4, component1)
                .assignServiceCandidate(serviceCandidate5, component2)
                .assignServiceCandidate(serviceCandidate6, component3)
                .assignServiceCandidate(serviceCandidate7, component1)
                .assignServiceCandidate(serviceCandidate8, component2)
                .assignServiceCandidate(serviceCandidate9, component3)
                .assignServiceCandidate(serviceCandidate10, component1)
                .addSymmetricRemoteConnection(component1, component2, 0, TransactionPropagation.NONE)
                .addSymmetricRemoteConnection(component1, component3, 0, TransactionPropagation.NONE)
                .addSymmetricRemoteConnection(component2, component3, 0, TransactionPropagation.NONE)
                .build();
        
        try (var inputStream = new FileInputStream("traces.dat")) {
            this.traces = new EventTraceDecoder().decodeTraces(inputStream);
            this.refreshTracesList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private JPanel createMainPanel() {
        var panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(this.tracesPane.get());
        panel.add(this.traceViewPane.get());
        
        return panel;
    }
    
    private JScrollPane createTracesPane() {
        var pane = new JScrollPane();
        
        pane.setViewportView(this.tracesList.get());
        
        return pane;
    }
    
    private JList<EventTraceView> createTracesList() {
        var list =  new JList<EventTraceView>();
        
        list.addListSelectionListener(this::onTraceSelection);
        
        return list;
    }
    
    private void onTraceSelection(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        
        var selectedView = this.tracesList.get().getSelectedValue();
        if (selectedView == null) {
            return;
        }
        
        var selectedEventTrace = selectedView.trace();
        var spanTrace = new TraceBuilder().buildTrace(selectedEventTrace, this.deploymentModel, Set.of());
        
        this.traceView.get().trace(spanTrace);
    }
    
    private JScrollPane createTraceViewPane() {
        var pane = new JScrollPane();
        
        pane.setViewportView(this.traceView.get());
        
        return pane;
    }
    
    private TraceViewComponent createTraceView() {
        return new TraceViewComponent();
    }
    
    private void refreshTracesList() {
        var views = this.traces.stream()
            .map(EventTraceView::new)
            .collect(Collectors.toList());
        
        var viewsArray = new EventTraceView[views.size()];
        views.toArray(viewsArray);
        
        this.tracesList.get().setListData(viewsArray);
    }
    
    private static class EventTraceView {
        
        private final String useCase;
        
        private final EventTrace trace;

        private static String determineUseCaseName(EventTrace trace) {
            if (trace.events().isEmpty()) {
                return "<empty>";
            } else {
                var firstEvent = trace.events().get(0);
                if (firstEvent instanceof UseCaseStartEvent startEvent) {
                    return startEvent.name();
                } else {
                    return "<invalid trace>";
                }
            }
        }
        
        public EventTraceView(EventTrace trace) {            
            this.useCase = determineUseCaseName(trace);
            this.trace = trace;
        }
        
        public EventTrace trace() {
            return this.trace;
        }
        
        @Override
        public final String toString() {
            return "Trace for Use case '" + this.useCase + "'";
        }
        
    }
    
}
