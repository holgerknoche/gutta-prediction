package gutta.prediction.event.codec;

import gutta.prediction.domain.Entity;
import gutta.prediction.domain.EntityType;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
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
        
        var entityType = new EntityType("et");
        var entity = new Entity(entityType, "1");
        
        var trace = EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"),
                new EntityReadEvent(traceId, 200, location, entity),
                new EntityWriteEvent(traceId, 300, location, entity),
                new UseCaseEndEvent(traceId, 1000, location, "uc")
                );
                
        var expectedBytes = new byte[] {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Number of traces (1)
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, // Number of string table entries (4)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Length of the first string table entry (2)
                (byte) 0x75, (byte) 0x63, // String data of the first entry ("uc")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Length of the second string table entry (2)
                (byte) 0x65, (byte) 0x74, // String data of the third entry ("et")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Length of the third string table entry (1)
                (byte) 0x31, // String data of the third entry ("1")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, // Length of the fourth string table entry (4)
                (byte) 0x74, (byte) 0x65, (byte) 0x73, (byte) 0x74, // String data of the fourth entry ("test")
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Number of location table entries (1)
                (byte) 0x01, // Type of the first location entry
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, // String index of the host name (3)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7B, // Process id of the location (123)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Thread id of the location (1)
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, // Number of events in the first trace (4)
                
                (byte) 0x01, // Event type (1)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x64, // Timestamp (100)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // String index of the use case name (0)
                                
                (byte) 0x03, // Event type (3)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC8, // Timestamp (200)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index of the first event (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // String index of the entity type name (1)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // String index of the entity id (2)
                
                (byte) 0x04, // Event type (4)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id of the first event (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x2C, // Timestamp of the first event (300)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index of the first event (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // String index of the entity type name (1)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // String index of the entity id (2)
                
                (byte) 0x02, // Event type (2)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0xE8, // Timestamp (1000)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
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
