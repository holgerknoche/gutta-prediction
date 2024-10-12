package gutta.prediction.event.codec;

import gutta.prediction.event.EventTrace;
import gutta.prediction.event.Location;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.SyntheticLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

public class EventTraceCodec {

    /**
     * Encodes a collection of traces into an output stream. The collection of traces is encoded as a self-contained block and may be followed by further
     * blocks.
     * 
     * @param traces       The traces to encode into the stream
     * @param outputStream The stream to encode the data into
     * @throws IOException If an I/O error occurs while encoding the traces
     */
    public void encodeTraces(Collection<EventTrace> traces, OutputStream outputStream) throws IOException {

    }

    private void encodeLocation(Location location) {
        // TODO
        switch (location) {
        case ObservedLocation observed -> {
            
        } 
        
        case SyntheticLocation synthetic -> {
            
        }
        }
    }    
    
    public Collection<EventTrace> decodeTraces(InputStream inputStream) throws IOException {
        // TODO
        return null;
    }

}
