package gutta.prediction.event.codec;

import gutta.prediction.event.EventTrace;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test cases for the class {@link EventTraceEncoder}.
 */
class EventTraceEncoderTest extends EventTraceCodecTestTemplate {
    
    /**
     * Test case: An empty collection of traces is encoded into an "empty" block.
     */
    @Test
    void emptyTraceCollection() {
        var traces = List.<EventTrace>of(); 
        var expectedBytes = serializedEmptyTrace();

        this.runEncoderTest(traces, expectedBytes);
    }
    
    /**
     * Test case: A trace with all event types is encoded as expected.
     */
    @Test
    void encodeTraceWithAllEventTypes() {
        var trace = traceWithAllEventTypes();
        var expectedBytes = serializedTraceWithAllEventTypes(); 
        
        this.runEncoderTest(List.of(trace), expectedBytes);
    }
    
    /**
     * Test case: A trace with a synthetic location is encoded as expected.
     */
    @Test
    void encodeTraceWithSyntheticLocation() {       
        var trace = traceWithSyntheticLocation();        
        var expectedBytes = serializedTraceWithSyntheticLocation();
        
        this.runEncoderTest(List.of(trace), expectedBytes);        
    }
    
    /**
     * Test case: A trace across multiple locations is encoded as expected.
     */
    @Test
    void encodeTraceWithMultipleLocations() {
        var trace = traceWithMultipleLocations();
        var expectedBytes = serializedTraceWithMultipleLocations();
        
        this.runEncoderTest(List.of(trace), expectedBytes);
    }
    
    /**
     * Test case: A block consisting of multiple traces is encoded as expected.
     */
    @Test
    void encodeMultipleTraces() {
        var traces = blockWithMultipleTraces();        
        var expectedBytes = serializedBlockWithMultipleTraces();
                
        this.runEncoderTest(traces, expectedBytes);
    }
    
    private void runEncoderTest(Collection<EventTrace> traces, byte[] expectedBytes) {
        try (var outputStream = new ByteArrayOutputStream()) {
            new EventTraceEncoder().encodeTraces(traces, outputStream);
            
            var writtenBytes = outputStream.toByteArray();
                        
            assertArrayEquals(expectedBytes, writtenBytes);
        } catch (IOException e) {
            fail(e);
        }
    }

}
