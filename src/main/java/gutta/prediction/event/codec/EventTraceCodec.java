package gutta.prediction.event.codec;

import gutta.prediction.event.EventTrace;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class EventTraceCodec {
    
    public void encodeTraces(Collection<EventTrace> traces, OutputStream outputStream) throws IOException {
        
    }
    
    public Collection<EventTrace> decodeTraces(File file) throws IOException {
        // TODO
        return null;
    }

}
