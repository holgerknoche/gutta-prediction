package gutta.prediction.ui;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.table.TableModel;

/**
 * Template class for a frame for analyses of traces for a use case.
 * 
 * @param <T> The type of the results to show in the result table
 */
abstract class TracesForUseCaseAnalysisFrameTemplate<T extends Comparable<T>> extends AnalysisFrameTemplate {

    private static final long serialVersionUID = 7779060688846998337L;

    private final Collection<EventTrace> traces;

    private final Map<Long, EventTrace> traceLookup;

    protected TracesForUseCaseAnalysisFrameTemplate(Collection<EventTrace> traces, String originalDeploymentModelSpec, DeploymentModel originalDeploymentModel,
            String givenModifiedDeploymentModelSpec) {
        super(originalDeploymentModelSpec, originalDeploymentModel, givenModifiedDeploymentModelSpec);

        this.traces = traces;
        this.traceLookup = traces.stream().collect(Collectors.toMap(EventTrace::traceId, Function.identity()));
    }

    protected void performScenarioAnalysis(DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel) {
        var results = new ArrayList<T>(this.traces.size());

        for (var trace : this.traces) {
            var result = this.analyzeScenario(trace, originalDeploymentModel, modifiedDeploymentModel);
            results.add(result);
        }

        Collections.sort(results);

        var tableModel = this.createTableModel(results);
        this.setResultsTableModel(tableModel);
    }

    protected EventTrace traceWithId(long traceId) {
        return this.traceLookup.get(traceId);
    }

    protected abstract T analyzeScenario(EventTrace trace, DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel);

    protected abstract TableModel createTableModel(List<T> values);

}
