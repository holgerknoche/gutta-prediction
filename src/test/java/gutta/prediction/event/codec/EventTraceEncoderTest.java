package gutta.prediction.event.codec;

import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;
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
        var traceId = 1234;
        var location1 = new ObservedLocation("test", 1234, 1);
        var location2 = new ObservedLocation("xyz", 5678, 2);
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location1, "uc"),
                new UseCaseEndEvent(traceId, 200, location2, "uc")
                );
        
        var expectedBytes = new byte[] {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Number of traces (1)
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, // Number of string table entries (3)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Length of the first string table entry (2)
                (byte) 0x75, (byte) 0x63, // String data of the first entry ("uc")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, // Length of the second string table entry (4)
                (byte) 0x74, (byte) 0x65, (byte) 0x73, (byte) 0x74, // String data of the second entry ("test")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, // Length of the third string table entry (3)
                (byte) 0x78, (byte) 0x79, (byte) 0x7A, // String data of the third entry ("xyz")
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Number of location table entries (2)
                (byte) 0x01, // Type of the first location entry
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // String index of the host name (1)
                (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Process id of the location (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Thread id of the location (1)                
                (byte) 0x01, // Type of the second location entry
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // String index of the host name (2)
                (byte) 0x00, (byte) 0x00, (byte) 0x16, (byte) 0x2E, // Process id of the location (5678)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Thread id of the location (2)

                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Number of events in the first trace (2)
                
                (byte) 0x01, // Event type (1)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x64, // Timestamp (100)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // String index of the use case name (0)
                
                (byte) 0x02, // Event type (2)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC8, // Timestamp (200)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Location index (1)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 // String index of the use case name (0)
        };
        
        this.runEncoderTest(List.of(trace), expectedBytes);
    }
    
    /**
     * Test case: A block consisting of multiple traces is encoded as expected.
     */
    @Test
    void encodeMultipleTraces() {
        var traceId1 = 1234;
        var traceId2 = 5678;        
        var location = new ObservedLocation("test", 1234, 1);

        var trace1 = EventTrace.of(
                new UseCaseStartEvent(traceId1, 100, location, "uc")               
                );
        
        var trace2 = EventTrace.of(
                new UseCaseStartEvent(traceId2, 200, location, "uc")               
                );
        
        var expectedBytes = new byte[] {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Number of traces (2)
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Number of string table entries (2)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Length of the first string table entry (2)
                (byte) 0x75, (byte) 0x63, // String data of the first entry ("uc")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, // Length of the second string table entry (4)
                (byte) 0x74, (byte) 0x65, (byte) 0x73, (byte) 0x74, // String data of the second entry ("test")
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Number of location table entries (1)
                (byte) 0x01, // Type of the first location entry
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // String index of the host name (1)
                (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Process id of the location (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Thread id of the location (1)                
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Number of events in the first trace (1)
                
                (byte) 0x01, // Event type (1)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x64, // Timestamp (100)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // String index of the use case name (0)

                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Number of events in the second trace (1)

                (byte) 0x01, // Event type (1)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x16, (byte) 0x2E, // Trace id (5678)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC8, // Timestamp (200)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 // String index of the use case name (0)
        };
        
        this.runEncoderTest(List.of(trace1, trace2), expectedBytes);
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
