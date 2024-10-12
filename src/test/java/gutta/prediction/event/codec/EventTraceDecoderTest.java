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

    private void runDecoderTest(byte[] serializedTraces, Collection<EventTrace> expectedTraces) {
        try (var inputStream = new ByteArrayInputStream(serializedTraces)) {
            var decodedTraces = new EventTraceDecoder().decodeTraces(inputStream);

            assertEquals(expectedTraces, decodedTraces);
        } catch (IOException e) {
            fail(e);
        }
    }

}
