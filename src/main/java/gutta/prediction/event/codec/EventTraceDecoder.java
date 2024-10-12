package gutta.prediction.event.codec;

import gutta.prediction.domain.Entity;
import gutta.prediction.domain.EntityType;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.Location;
import gutta.prediction.event.MonitoringEvent;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.SyntheticLocation;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static gutta.prediction.event.codec.Constants.EVENT_TYPE_ENTITY_READ;
import static gutta.prediction.event.codec.Constants.EVENT_TYPE_ENTITY_WRITE;
import static gutta.prediction.event.codec.Constants.EVENT_TYPE_EXPLICIT_TRANSACTION_ABORT;
import static gutta.prediction.event.codec.Constants.EVENT_TYPE_IMPLICIT_TRANSACTION_ABORT;
import static gutta.prediction.event.codec.Constants.EVENT_TYPE_SERVICE_CANDIDATE_ENTRY;
import static gutta.prediction.event.codec.Constants.EVENT_TYPE_SERVICE_CANDIDATE_EXIT;
import static gutta.prediction.event.codec.Constants.EVENT_TYPE_SERVICE_CANDIDATE_INVOCATION;
import static gutta.prediction.event.codec.Constants.EVENT_TYPE_SERVICE_CANDIDATE_RETURN;
import static gutta.prediction.event.codec.Constants.EVENT_TYPE_TRANSACTION_COMMIT;
import static gutta.prediction.event.codec.Constants.EVENT_TYPE_TRANSACTION_START;
import static gutta.prediction.event.codec.Constants.EVENT_TYPE_USE_CASE_END;
import static gutta.prediction.event.codec.Constants.EVENT_TYPE_USE_CASE_START;
import static gutta.prediction.event.codec.Constants.LOCATION_TYPE_OBSERVED;
import static gutta.prediction.event.codec.Constants.LOCATION_TYPE_SYNTHETIC;

public class EventTraceDecoder {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    
    private Map<String, EntityType> entityTypes;

    public Collection<EventTrace> decodeTraces(InputStream inputStream) throws IOException {
        this.entityTypes = new HashMap<>();
        
        try (var dataStream = new DataInputStream(inputStream)) {
            var numberOfTraces = dataStream.readInt();
            var traces = new ArrayList<EventTrace>(numberOfTraces);

            var stringTable = this.readStringTable(dataStream);
            var locationTable = this.readLocationTable(dataStream, stringTable);

            for (int traceIndex = 0; traceIndex < numberOfTraces; traceIndex++) {
                var trace = this.decodeTrace(dataStream, stringTable, locationTable);
                traces.add(trace);
            }

            return traces;
        }
    }

    private StringTable readStringTable(DataInputStream stream) throws IOException {
        var numberOfStrings = stream.readInt();
        var entries = new String[numberOfStrings];

        for (var stringIndex = 0; stringIndex < numberOfStrings; stringIndex++) {
            var stringLength = stream.readInt();
            var stringBytes = new byte[stringLength];
            stream.read(stringBytes, 0, stringLength);

            var string = new String(stringBytes, CHARSET);
            entries[stringIndex] = string;
        }

        return new StringTable(entries);
    }

    private LocationTable readLocationTable(DataInputStream stream, StringTable stringTable) throws IOException {
        var numberOfLocations = stream.readInt();
        var entries = new Location[numberOfLocations];

        for (var locationIndex = 0; locationIndex < numberOfLocations; locationIndex++) {
            var location = this.decodeLocation(stream, stringTable);
            entries[locationIndex] = location;
        }

        return new LocationTable(entries);
    }

    private Location decodeLocation(DataInputStream stream, StringTable stringTable) throws IOException {
        var locationType = stream.readByte();

        switch (locationType) {
        case LOCATION_TYPE_OBSERVED:
            var hostnameIndex = stream.readInt();
            var hostname = stringTable.getEntry(hostnameIndex);
            var processId = stream.readInt();
            var threadId = stream.readLong();

            return new ObservedLocation(hostname, processId, threadId);

        case LOCATION_TYPE_SYNTHETIC:
            var id = stream.readLong();

            return new SyntheticLocation(id);

        default:
            throw new EventTraceDecodingException("Unsupported location type " + locationType + ".");
        }
    }

    private EventTrace decodeTrace(DataInputStream stream, StringTable stringTable, LocationTable locationTable) throws IOException {
        var eventCount = stream.readInt();
        var events = new ArrayList<MonitoringEvent>(eventCount);

        for (var eventIndex = 0; eventIndex < eventCount; eventIndex++) {
            var event = this.decodeEvent(stream, stringTable, locationTable);
            events.add(event);
        }

        return EventTrace.of(events);
    }

    private MonitoringEvent decodeEvent(DataInputStream stream, StringTable stringTable, LocationTable locationTable) throws IOException {
        var eventTypeId = stream.readByte();

        switch (eventTypeId) {
        case EVENT_TYPE_USE_CASE_START:
            return this.decodeEvent(stream, stringTable, locationTable, this::decodeUseCaseStartEvent);
            
        case EVENT_TYPE_USE_CASE_END:
            return this.decodeEvent(stream, stringTable, locationTable, this::decodeUseCaseEndEvent);
            
        case EVENT_TYPE_ENTITY_READ:
            return this.decodeEvent(stream, stringTable, locationTable, this::decodeEntityReadEvent);
            
        case EVENT_TYPE_ENTITY_WRITE:
            return this.decodeEvent(stream, stringTable, locationTable, this::decodeEntityWriteEvent);
            
        case EVENT_TYPE_TRANSACTION_START:
            return this.decodeEvent(stream, stringTable, locationTable, this::decodeTransactionStartEvent);
            
        case EVENT_TYPE_TRANSACTION_COMMIT:
            return this.decodeEvent(stream, stringTable, locationTable, this::decodeTransactionCommitEvent);
            
        case EVENT_TYPE_EXPLICIT_TRANSACTION_ABORT:
            return this.decodeEvent(stream, stringTable, locationTable, this::decodeExplicitTransactionAbortEvent);
            
        case EVENT_TYPE_IMPLICIT_TRANSACTION_ABORT:
            return this.decodeEvent(stream, stringTable, locationTable, this::decodeImplicitTransactionAbortEvent);
            
        case EVENT_TYPE_SERVICE_CANDIDATE_INVOCATION:
            return this.decodeEvent(stream, stringTable, locationTable, this::decodeServiceCandidateInvocationEvent);
            
        case EVENT_TYPE_SERVICE_CANDIDATE_ENTRY:
            return this.decodeEvent(stream, stringTable, locationTable, this::decodeServiceCandidateEntryEvent);
            
        case EVENT_TYPE_SERVICE_CANDIDATE_EXIT:
            return this.decodeEvent(stream, stringTable, locationTable, this::decodeServiceCandidateExitEvent);
            
        case EVENT_TYPE_SERVICE_CANDIDATE_RETURN:
            return this.decodeEvent(stream, stringTable, locationTable, this::decodeServiceCandidateReturnEvent);

        default:
            throw new EventTraceDecodingException("Unknown event type " + eventTypeId + ".");
        }
    }
    
    private <T extends MonitoringEvent> T decodeEvent(DataInputStream stream, StringTable stringTable, LocationTable locationTable, SpecificEventDecoder<T> specificDecoder) throws IOException {
        var traceId = stream.readLong();
        var timestamp = stream.readLong();
        var locationIndex = stream.readInt();
        var location = locationTable.getEntry(locationIndex);
        
        return specificDecoder.decode(traceId, timestamp, location, stream, stringTable);
    }
    
    private EntityType retrieveEntityType(String name) {
        return this.entityTypes.computeIfAbsent(name, EntityType::new);
    }
        
    private UseCaseStartEvent decodeUseCaseStartEvent(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException {
        var useCaseNameIndex = stream.readInt();
        var useCaseName = stringTable.getEntry(useCaseNameIndex);
        
        return new UseCaseStartEvent(traceId, timestamp, location, useCaseName);
    }
    
    private UseCaseEndEvent decodeUseCaseEndEvent(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException {
        var useCaseNameIndex = stream.readInt();
        var useCaseName = stringTable.getEntry(useCaseNameIndex);
        
        return new UseCaseEndEvent(traceId, timestamp, location, useCaseName);
    }
    
    private EntityReadEvent decodeEntityReadEvent(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException {
        var entityTypeNameIndex = stream.readInt();
        var entityTypeName = stringTable.getEntry(entityTypeNameIndex);
        var entityType = this.retrieveEntityType(entityTypeName);
        
        var entityIdIndex = stream.readInt();
        var entityId = stringTable.getEntry(entityIdIndex);
        
        // TODO Deduplicate entities to save heap for large traces
        return new EntityReadEvent(traceId, timestamp, location, new Entity(entityType, entityId));
    }
    
    private EntityWriteEvent decodeEntityWriteEvent(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException {
        var entityTypeNameIndex = stream.readInt();
        var entityTypeName = stringTable.getEntry(entityTypeNameIndex);
        var entityType = this.retrieveEntityType(entityTypeName);
        
        var entityIdIndex = stream.readInt();
        var entityId = stringTable.getEntry(entityIdIndex);
        
        // TODO Deduplicate entities to save heap for large traces
        return new EntityWriteEvent(traceId, timestamp, location, new Entity(entityType, entityId));
    }
    
    private TransactionStartEvent decodeTransactionStartEvent(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException {
        var transactionIdIndex = stream.readInt();
        var transactionId = stringTable.getEntry(transactionIdIndex);
        
        return new TransactionStartEvent(traceId, timestamp, location, transactionId);                
    }
    
    private TransactionCommitEvent decodeTransactionCommitEvent(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException {
        var transactionIdIndex = stream.readInt();
        var transactionId = stringTable.getEntry(transactionIdIndex);
        
        return new TransactionCommitEvent(traceId, timestamp, location, transactionId);                
    }
    
    private ExplicitTransactionAbortEvent decodeExplicitTransactionAbortEvent(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException {
        var transactionIdIndex = stream.readInt();
        var transactionId = stringTable.getEntry(transactionIdIndex);
        
        return new ExplicitTransactionAbortEvent(traceId, timestamp, location, transactionId);                
    }
    
    private ImplicitTransactionAbortEvent decodeImplicitTransactionAbortEvent(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException {
        var transactionIdIndex = stream.readInt();
        var transactionId = stringTable.getEntry(transactionIdIndex);
        var causeIndex = stream.readInt();
        var cause = stringTable.getEntry(causeIndex);
        
        return new ImplicitTransactionAbortEvent(traceId, timestamp, location, transactionId, cause);                
    }
    
    private ServiceCandidateInvocationEvent decodeServiceCandidateInvocationEvent(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException {
        var candidateNameIndex = stream.readInt();
        var candidateName = stringTable.getEntry(candidateNameIndex);
        
        return new ServiceCandidateInvocationEvent(traceId, timestamp, location, candidateName);                
    }
    
    private ServiceCandidateEntryEvent decodeServiceCandidateEntryEvent(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException {
        var candidateNameIndex = stream.readInt();
        var candidateName = stringTable.getEntry(candidateNameIndex);
        var transactionStarted = stream.readBoolean();
        
        if (transactionStarted) {
            var transactionIdIndex = stream.readInt();
            var transactionId = stringTable.getEntry(transactionIdIndex);
            
            return new ServiceCandidateEntryEvent(traceId, timestamp, location, candidateName, true, transactionId);
        } else {
            return new ServiceCandidateEntryEvent(traceId, timestamp, location, candidateName);                    
        }
    }
    
    private ServiceCandidateExitEvent decodeServiceCandidateExitEvent(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException {
        var candidateNameIndex = stream.readInt();
        var candidateName = stringTable.getEntry(candidateNameIndex);
        
        return new ServiceCandidateExitEvent(traceId, timestamp, location, candidateName);                
    }
    
    private ServiceCandidateReturnEvent decodeServiceCandidateReturnEvent(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException {
        var candidateNameIndex = stream.readInt();
        var candidateName = stringTable.getEntry(candidateNameIndex);
        
        return new ServiceCandidateReturnEvent(traceId, timestamp, location, candidateName);                
    }

    static class EventTraceDecodingException extends RuntimeException {

        private static final long serialVersionUID = -6813556398013056671L;

        public EventTraceDecodingException(String message) {
            super(message);
        }

        public EventTraceDecodingException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    private static class StringTable {

        private final String[] entries;

        public StringTable(String[] entries) {
            this.entries = entries;
        }

        public String getEntry(int index) {
            return this.entries[index];
        }

    }

    private static class LocationTable {

        private final Location[] entries;

        public LocationTable(Location[] entries) {
            this.entries = entries;
        }

        public Location getEntry(int index) {
            return this.entries[index];
        }

    }
    
    private interface SpecificEventDecoder<T extends MonitoringEvent> {
        
        T decode(long traceId, long timestamp, Location location, DataInputStream stream, StringTable stringTable) throws IOException;
        
    }

}
