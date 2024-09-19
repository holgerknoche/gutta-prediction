package gutta.prediction.span;

import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ProcessLocation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class TraceBuilderTest {
    
    /**
     * Test case: A trace without a change in location is processed as expected.
     */
    @Test
    void localTrace() {
        final var traceId = 1234L;
        final var location = new ProcessLocation("test", 1234, 1);

        var inputEvents = Arrays.<MonitoringEvent>asList(
                );
        
        var trace = new TraceBuilder().buildTrace(inputEvents);
        
        System.out.println(trace);
        
    }

}
