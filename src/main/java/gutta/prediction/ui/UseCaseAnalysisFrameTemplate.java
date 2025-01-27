package gutta.prediction.ui;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.event.EventTrace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableModel;

/**
 * Frame template for all analyses of use cases.
 * 
 * @param <T> The type of the analysis results to show in the results table
 */
abstract class UseCaseAnalysisFrameTemplate<T extends Comparable<T>> extends AnalysisFrameTemplate {

    private static final long serialVersionUID = 5209108354998887792L;

    protected final Map<String, Collection<EventTrace>> tracesPerUseCase;

    protected UseCaseAnalysisFrameTemplate(Map<String, Collection<EventTrace>> tracesPerUseCase, String originalDeploymentModelSpec,
            DeploymentModel originalDeploymentModel) {
        super(originalDeploymentModelSpec, originalDeploymentModel);

        this.tracesPerUseCase = tracesPerUseCase;
    }

    protected void performScenarioAnalysis(DeploymentModel originalDeploymentModel, DeploymentModel modifiedDeploymentModel) {
        var results = new ArrayList<T>();
        for (var entry : this.tracesPerUseCase.entrySet()) {
            var useCaseName = entry.getKey();
            var traces = entry.getValue();

            var result = this.analyzeScenario(useCaseName, traces, originalDeploymentModel, modifiedDeploymentModel);
            results.add(result);
        }

        Collections.sort(results);

        var tableModel = this.createTableModel(results);
        this.setResultsTableModel(tableModel);
    }

    protected abstract T analyzeScenario(String useCaseName, Collection<EventTrace> traces, DeploymentModel originalDeploymentModel,
            DeploymentModel modifiedDeploymentModel);

    protected abstract TableModel createTableModel(List<T> values);

}
