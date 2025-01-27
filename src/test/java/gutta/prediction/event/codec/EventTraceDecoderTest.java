package gutta.prediction.event.codec;

import gutta.prediction.event.EventTrace;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test cases for the class {@link EventTraceDecoder}.
 */
class EventTraceDecoderTest extends EventTraceCodecTestTemplate {

    /**
     * Test case: An empty trace collection is decoded successfully.
     */
    @Test
    void decodeEmptyTraceCollection() {
        var traceBytes = serializedEmptyTrace();
        var expectedTraces = List.<EventTrace>of();

        this.runDecoderTest(traceBytes, expectedTraces);
    }

    /**
     * Test case: A trace with all event types is decoded successfully.
     */
    @Test
    void decodeTraceWithAllEventTypes() {
        var traceBytes = serializedTraceWithAllEventTypes();
        var expectedTraces = List.of(traceWithAllEventTypes());

        this.runDecoderTest(traceBytes, expectedTraces);
    }
    
    /**
     * Test case: A trace with a synthetic location is decoded successfully.
     */
    @Test
    void decodeTraceWithSyntheticLocation() {
        var traceBytes = serializedTraceWithSyntheticLocation();
        var expectedTraces = List.of(traceWithSyntheticLocation());                
        
        this.runDecoderTest(traceBytes, expectedTraces);        
    }
    
    /**
     * Test case: A trace across multiple locations is decoded successfully.
     */
    @Test
    void decodeTraceWithMultipleLocations() {
        var traceBytes = serializedTraceWithMultipleLocations();
        var expectedTraces = List.of(traceWithMultipleLocations());        
        
        this.runDecoderTest(traceBytes, expectedTraces);
    }
    
    /**
     * Test case: A block consisting of multiple traces is decoded successfully.
     */
    @Test
    void decodeMultipleTraces() {
        var traceBytes = serializedBlockWithMultipleTraces();
        var expectedTraces = blockWithMultipleTraces();                
                
        this.runDecoderTest(traceBytes, expectedTraces);
    }
    
    
    /**
     * Test case: Variants of entities (root, subordinate).
     */
    @Test
    void decodeAllEntityVariants() {
        var traceBytes = serializedTraceWithAllEntityVariants();
        var expectedTraces = List.of(traceWithAllEntityVariants());
        
        this.runDecoderTest(traceBytes, expectedTraces);
    }
    
    private void runDecoderTest(byte[] serializedTraces, Collection<EventTrace> expectedTraces) {
        try (var inputStream = new ByteArrayInputStream(serializedTraces)) {
            var decodedTraces = new EventTraceDecoder().decodeTraces(inputStream);

            assertEquals(expectedTraces, decodedTraces);
        } catch (IOException e) {
            fail(e);
        }
    }

}
