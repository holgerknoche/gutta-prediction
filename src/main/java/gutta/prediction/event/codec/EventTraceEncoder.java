package gutta.prediction.event.codec;

import gutta.prediction.event.EventTrace;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.MonitoringEventVisitor;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.SyntheticLocation;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EventTraceEncoder extends MonitoringEventVisitor {
    
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    
    private static final int BUFFER_SIZE = 65536;        

    private Map<String, Integer> stringToCode;
    
    private Map<Location, Integer> locationToCode;

    private int stringCodeCounter = 0;
    
    private int locationCodeCounter = 0;
    
    private DataOutputStream dataStream;
    
    private int storeString(String value) {
        return this.stringToCode.computeIfAbsent(value, ignored -> this.stringCodeCounter++);
    }
    
    private int storeLocation(Location location) {
        return this.locationToCode.computeIfAbsent(location, ignored -> this.locationCodeCounter++);
    }
    
    /**
     * Encodes a collection of traces into an output stream. The collection of traces is encoded as a self-contained block and may be followed by further
     * blocks.
     * 
     * @param traces       The traces to encode into the stream
     * @param outputStream The stream to encode the data into
     * @throws IOException If an I/O error occurs while encoding the traces
     */
    public void encodeTraces(Collection<EventTrace> traces, OutputStream outputStream) throws IOException {
        this.stringToCode = new HashMap<>();
        this.locationToCode = new HashMap<>();
        
        // Encode the records into a separate buffer to collect the necessary metadata while encoding
        try (var bufferStream = new ByteArrayOutputStream(BUFFER_SIZE);
             var dataStream = new DataOutputStream(bufferStream)) {
     
            this.dataStream = dataStream;
            traces.forEach(this::encodeTrace);
           
            // Once everything has been collected, write the metadata and the encoded data to the output stream
            this.writeBlockToOutputStream(traces, outputStream, bufferStream);
        } finally {
            this.dataStream = null;
        }        
    }
    
    private void writeBlockToOutputStream(Collection<EventTrace> traces, OutputStream outputStream, ByteArrayOutputStream bufferStream) throws IOException {
        try (var outputDataStream = new DataOutputStream(outputStream)) {
            // Write the number of traces
            outputDataStream.writeInt(traces.size());
            
            // Encode the locations table to a byte array, since it may add further strings to the string table
            var locationTableData = this.encodeLocationTable();
            
            // Write the string table            
            this.writeStringTable(outputDataStream);
                        
            // Write the location table after the string table
            outputDataStream.write(locationTableData);
            
            // Write the actual data
            bufferStream.writeTo(outputDataStream);
        }
    }
    
    private void writeStringTable(DataOutputStream outputStream) throws IOException {
        // Sort the table entries by ID
        var entries = new ArrayList<StringTableEntry>(this.stringToCode.size());
        this.stringToCode.forEach((value, code) -> entries.add(new StringTableEntry(value, code)));        
        entries.sort((entry1, entry2) -> Integer.compare(entry1.code(), entry2.code()));
        
        // Save the entries to the stream
        outputStream.writeInt(entries.size());
        for (var entry : entries) {
            var encodedValue = entry.value().getBytes(CHARSET);
            
            outputStream.writeInt(encodedValue.length);            
            outputStream.write(encodedValue, 0, encodedValue.length);
        }
    }
    
    private byte[] encodeLocationTable() throws IOException {        
        // Sort the table entries by ID
        var entries = new ArrayList<LocationTableEntry>(this.locationToCode.size());
        this.locationToCode.forEach((location, code) -> entries.add(new LocationTableEntry(location, code)));        
        entries.sort((entry1, entry2) -> Integer.compare(entry1.code(), entry2.code()));

        try (var byteStream = new ByteArrayOutputStream();
                var outputStream = new DataOutputStream(byteStream)) {

            // Save the entries to the stream
            outputStream.writeInt(entries.size());
            for (var entry : entries) {
                var location = entry.location();
                this.encodeLocation(location, outputStream);
            }
            
            outputStream.flush();
            return byteStream.toByteArray();
        }
    }
    
    private void encodeLocation(Location location, DataOutputStream outputStream) throws IOException {
        switch (location) {
        
        case ObservedLocation observed -> {
            outputStream.writeByte(Constants.LOCATION_TYPE_OBSERVED);
            outputStream.writeInt(this.storeString(observed.hostname()));
            outputStream.writeInt(observed.processId());
            outputStream.writeLong(observed.threadId());
        }
        
        case SyntheticLocation synthetic -> {
            outputStream.writeByte(Constants.LOCATION_TYPE_SYNTHETIC);
            outputStream.writeLong(synthetic.id());
        }
        
        }
    }
    
    private void encodeTrace(EventTrace trace) {
        try {
            this.dataStream.writeInt(trace.size());
            
            trace.forEach(this::handleMonitoringEvent);
        } catch (IOException e) {
            throw new EventTraceEncodingException("An error occurred while encoding a trace.", e);
        }
    }
    
    private <T extends MonitoringEvent> void encodeEvent(T event, EventEncodingOperation<T> encodingOperation) {
        try {
            encodingOperation.encodeEvent(event, this.dataStream);
        } catch (IOException e) {
            throw new EventTraceEncodingException("An error occurred while encoding event '" + event + "'.", e);
        }
    }
    
    @Override
    protected void handleUseCaseStartEvent(UseCaseStartEvent event) {
         this.encodeEvent(event, this::encodeUseCaseStartEvent);
    }
    
    private void encodeUseCaseStartEvent(UseCaseStartEvent event, DataOutputStream stream) throws IOException {
        stream.writeByte(Constants.EVENT_TYPE_USE_CASE_START);            
        stream.writeLong(event.traceId());
        stream.writeLong(event.timestamp());
        stream.writeInt(this.storeLocation(event.location()));
        stream.writeInt(this.storeString(event.name()));        
    }
    
    @Override
    protected void handleUseCaseEndEvent(UseCaseEndEvent event) {
        this.encodeEvent(event, this::encodeUseCaseEndEvent);
    }
    
    private void encodeUseCaseEndEvent(UseCaseEndEvent event, DataOutputStream stream) throws IOException {
        stream.writeByte(Constants.EVENT_TYPE_USE_CASE_END);            
        stream.writeLong(event.traceId());
        stream.writeLong(event.timestamp());
        stream.writeInt(this.storeLocation(event.location()));
        stream.writeInt(this.storeString(event.name()));        
    }
    
    static class EventTraceEncodingException extends RuntimeException {
        
        private static final long serialVersionUID = -6813556398013056671L;

        public EventTraceEncodingException(String message, Throwable cause) {
            super(message, cause);
        }
        
    }
    
    private record StringTableEntry(String value, int code) {
    }
    
    private record LocationTableEntry(Location location, int code) {        
    }
    
    private interface EventEncodingOperation<T extends MonitoringEvent> {
        
        void encodeEvent(T event, DataOutputStream outputStream) throws IOException;
        
    }

}
