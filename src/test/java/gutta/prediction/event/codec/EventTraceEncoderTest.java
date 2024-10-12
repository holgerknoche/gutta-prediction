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
class EventTraceEncoderTest {
    
    /**
     * Test case: An empty collection of traces is encoded into an "empty" block.
     */
    @Test
    void emptyTraceCollection() throws IOException {
        var traces = List.<EventTrace>of(); 
        var expectedBytes = new byte[] {
                0x00, 0x00, 0x00, 0x00, // Number of traces (0)
                0x00, 0x00, 0x00, 0x00, // Number of string table entries (0)
                0x00, 0x00, 0x00, 0x00 // Number of location table entries (0)
        };

        this.runEncoderTest(traces, expectedBytes);
    }
    
    /**
     * Test case: A trace with all event types is encoded as expected.
     */
    @Test
    void traceWithAllEventTypes() throws IOException {
        var traceId = 1234;
        var location = new ObservedLocation("test", 123, 1);
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"),
                new UseCaseEndEvent(traceId, 1000, location, "uc")
                );
                
        var expectedBytes = new byte[] {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Number of traces (1)
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Number of string table entries (2)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Length of the first string table entry (2)
                (byte) 0x75, (byte) 0x63, // String data of the first entry ("uc")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, // Length of the second string table entry (4)
                (byte) 0x74, (byte) 0x65, (byte) 0x73, (byte) 0x74, // String data of the second entry ("test")
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Number of location table entries (1)
                (byte) 0x01, // Type of the first location entry
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // String index of the host name (1)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7B, // Process id of the location (123)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Thread id of the location (1)
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Number of events in the first trace (2)
                
                (byte) 0x01, // Event type of the first event (1)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id of the first event (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x64, // Timestamp of the first event (100)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index of the first event (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // String index of the use case name (0)
                
                (byte) 0x02, // Event type of the first event (2)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id of the first event (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0xE8, // Timestamp of the first event (1000)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index of the first event (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 // String index of the use case name (0)
        };
        
        this.runEncoderTest(List.of(trace), expectedBytes);
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
