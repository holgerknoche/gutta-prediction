package gutta.prediction.ui.analysis;

import gutta.prediction.event.EventTrace;
import gutta.prediction.event.MonitoringEventVisitor;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.UseCaseStartEvent;
import gutta.prediction.simulation.EventStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This analyis provides the necessary data for the use case overview in the UI.
 */
public class UseCaseOverviewAnalysis {

    private static String determineUseCaseOf(EventTrace trace) {
        var traceEvents = trace.events();

        if (traceEvents.isEmpty()) {
            return "<none>";
        }

        var firstEvent = traceEvents.get(0);
        if (firstEvent instanceof UseCaseStartEvent startEvent) {
            return startEvent.name();
        } else {
            return "<invalid trace>";
        }
    }

    /**
     * Utility function to group event traces by use case.
     * 
     * @param traces The traces to group
     * @return A map use case name to event traces
     */
    public static Map<String, Collection<EventTrace>> groupByUseCase(Collection<EventTrace> traces) {
        var useCaseToTraces = new HashMap<String, Collection<EventTrace>>();

        for (var trace : traces) {
            var useCaseName = determineUseCaseOf(trace);

            var tracesForUseCase = useCaseToTraces.computeIfAbsent(useCaseName, name -> new ArrayList<EventTrace>());
            tracesForUseCase.add(trace);
        }

        return useCaseToTraces;
    }

    /**
     * Analyzes the given traces and returns the necessary data for the overview.
     * 
     * @param traces The traces to analyze
     * @return A map of use case name to the overview object containing further information
     */
    public Map<String, UseCaseOverview> analyzeTraces(Collection<EventTrace> traces) {
        var tracesPerUseCase = groupByUseCase(traces);
        var useCaseOverviews = new HashMap<String, UseCaseOverview>();

        for (var entry : tracesPerUseCase.entrySet()) {
            var useCaseName = entry.getKey();
            var tracesForUseCase = entry.getValue();

            var overview = this.calculateOverview(tracesForUseCase);
            useCaseOverviews.put(useCaseName, overview);
        }

        return useCaseOverviews;
    }

    private UseCaseOverview calculateOverview(Collection<EventTrace> traces) {
        var totalDuration = 0L;
        var totalOverhead = 0L;

        for (var trace : traces) {
            var traceDuration = determineDuration(trace);
            var traceOverhead = determineOverhead(trace);

            totalDuration += traceDuration;
            totalOverhead += traceOverhead;
        }

        var averageDuration = (double) totalDuration / (double) traces.size();
        var overheadPercentage = (double) totalOverhead / (double) totalDuration;

        return new UseCaseOverview(traces, averageDuration, overheadPercentage);
    }

    /**
     * Determines the duration of the given event trace.
     * 
     * @param trace The trace to analyze
     * @return The duration of the trace
     */
    public static long determineDuration(EventTrace trace) {
        var traceEvents = trace.events();

        if (traceEvents.size() < 2) {
            return 0L;
        }

        var firstEvent = traceEvents.get(0);
        var lastEvent = traceEvents.get(traceEvents.size() - 1);

        return Math.max(0L, (lastEvent.timestamp() - firstEvent.timestamp()));
    }

    /**
     * Determines the amount of overhead in the given event trace.
     * 
     * @param trace The trace to analyze
     * @return The amount of overhead in the trace
     */
    public static long determineOverhead(EventTrace trace) {
        return new OverheadSummarizer().determineOverheadIn(trace);
    }

    /**
     * This record provides the necessary data for the use case overview in the UI.
     * 
     * @param traces             The event traces in the use case
     * @param averageDuration    The average duration of the event traces
     * @param overheadPercentage The percentage of overhead in all traces
     */
    public record UseCaseOverview(Collection<EventTrace> traces, double averageDuration, double overheadPercentage) {
    }

    private static class OverheadSummarizer extends MonitoringEventVisitor {

        private long totalOverhead;

        private EventStream events;

        public long determineOverheadIn(EventTrace trace) {
            this.totalOverhead = 0L;

            this.events = new EventStream(trace);
            this.events.forEachRemaining(this::handleMonitoringEvent);

            return this.totalOverhead;
        }

        @Override
        protected void handleServiceCandidateInvocationEvent(ServiceCandidateInvocationEvent event) {
            var nextEvent = this.events.lookahead(1);

            if (nextEvent instanceof ServiceCandidateEntryEvent entryEvent) {
                var overhead = (entryEvent.timestamp() - event.timestamp());
                this.totalOverhead += overhead;
            }
        }

        @Override
        protected void handleServiceCandidateExitEvent(ServiceCandidateExitEvent event) {
            var nextEvent = this.events.lookahead(1);

            if (nextEvent instanceof ServiceCandidateReturnEvent returnEvent) {
                var overhead = (returnEvent.timestamp() - event.timestamp());
                this.totalOverhead += overhead;
            }
        }

    }

}
