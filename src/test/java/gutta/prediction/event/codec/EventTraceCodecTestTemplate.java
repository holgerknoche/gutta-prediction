package gutta.prediction.event.codec;

import gutta.prediction.domain.Entity;
import gutta.prediction.domain.EntityType;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;
import gutta.prediction.event.ExplicitTransactionAbortEvent;
import gutta.prediction.event.ImplicitTransactionAbortEvent;
import gutta.prediction.event.ObservedLocation;
import gutta.prediction.event.ServiceCandidateEntryEvent;
import gutta.prediction.event.ServiceCandidateExitEvent;
import gutta.prediction.event.ServiceCandidateInvocationEvent;
import gutta.prediction.event.ServiceCandidateReturnEvent;
import gutta.prediction.event.TransactionCommitEvent;
import gutta.prediction.event.TransactionStartEvent;
import gutta.prediction.event.UseCaseEndEvent;
import gutta.prediction.event.UseCaseStartEvent;

abstract class EventTraceCodecTestTemplate {
    
    protected static byte[] serializedEmptyTrace() {
        return new byte[] {
                0x00, 0x00, 0x00, 0x00, // Number of traces (0)
                0x00, 0x00, 0x00, 0x00, // Number of string table entries (0)
                0x00, 0x00, 0x00, 0x00 // Number of location table entries (0)
        };
    }
    
    protected static EventTrace traceWithAllEventTypes() {
        var traceId = 1234;
        var location = new ObservedLocation("test", 123, 1);
        
        var entityType = new EntityType("et");
        var entity = new Entity(entityType, "1");
        
        // Trace that contains all event types (and is semantically incorrect on purpose)
        return EventTrace.of(
                new UseCaseStartEvent(traceId, 100, location, "uc"),
                new EntityReadEvent(traceId, 200, location, entity),
                new EntityWriteEvent(traceId, 300, location, entity),
                new TransactionStartEvent(traceId, 400, location, "tx"),
                new TransactionCommitEvent(traceId, 500, location, "tx"),
                new ExplicitTransactionAbortEvent(traceId, 600, location, "tx"),
                new ImplicitTransactionAbortEvent(traceId, 700, location, "tx", "cause"),
                new ServiceCandidateInvocationEvent(traceId, 800, location, "sc"),
                new ServiceCandidateEntryEvent(traceId, 900, location, "sc"),
                new ServiceCandidateEntryEvent(traceId, 1000, location, "sc", true, "tx2"),
                new ServiceCandidateExitEvent(traceId, 1100, location, "sc"),
                new ServiceCandidateReturnEvent(traceId, 1200, location, "sc"),
                new UseCaseEndEvent(traceId, 1300, location, "uc")
                );
    }
    
    protected static byte[] serializedTraceWithAllEventTypes() {
        return new byte[] {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Number of traces (1)
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, // Number of string table entries (8)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Length of the first string table entry (2)
                (byte) 0x75, (byte) 0x63, // String data of the first entry ("uc")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Length of the second string table entry (2)
                (byte) 0x65, (byte) 0x74, // String data of the third entry ("et")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Length of the third string table entry (1)
                (byte) 0x31, // String data of the third entry ("1")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Length of the fourth string table entry (2)
                (byte) 0x74, (byte) 0x78, // String data of the fourth entry ("tx")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, // Length of the fifth string table entry (5)
                (byte) 0x63, (byte) 0x61, (byte) 0x75, (byte) 0x73, (byte) 0x65, // String data of the fifth entry ("cause")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // Length of the sixth string table entry (2)
                (byte) 0x73, (byte) 0x63, // String data of the sixth entry ("sc")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, // Length of the seventh string table entry (3)
                (byte) 0x74, (byte) 0x78, (byte) 0x32, // String data of the seventh entry ("tx2")
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, // Length of the eighth string table entry (4)
                (byte) 0x74, (byte) 0x65, (byte) 0x73, (byte) 0x74, // String data of the eighth entry ("test")
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Number of location table entries (1)
                (byte) 0x01, // Type of the first location entry
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07, // String index of the host name (7)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7B, // Process id of the location (123)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Thread id of the location (1)
                
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0D, // Number of events in the first trace (13)
                
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
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x2C, // Timestamp (300)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // String index of the entity type name (1)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, // String index of the entity id (2)
                
                (byte) 0x05, // Event type (5)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x90, // Timestamp (400)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, // String index of the transaction id (3)
                
                (byte) 0x06, // Event type (6)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xF4, // Timestamp (500)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, // String index of the transaction id (3)
                
                (byte) 0x07, // Event type (7)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x58, // Timestamp (600)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, // String index of the transaction id (3)
                
                (byte) 0x08, // Event type (8)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xBC, // Timestamp (700)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, // String index of the transaction id (3)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, // String index of the cause (4)
                                
                (byte) 0x09, // Event type (9)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x20, // Timestamp (800)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, // String index of the candidate name (5)
                
                (byte) 0x0A, // Event type (10)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x84, // Timestamp (900)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, // String index of the candidate name (5)
                (byte) 0x00, // No transaction started (flag)
                
                (byte) 0x0A, // Event type (10)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0xE8, // Timestamp (1000)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, // String index of the candidate name (5)
                (byte) 0x01, // Transaction started (flag)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, // String index of the transaction id (6)
                
                (byte) 0x0B, // Event type (11)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x4C, // Timestamp (1100)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, // String index of the candidate name (5)
                
                (byte) 0x0C, // Event type (12)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xB0, // Timestamp (1200)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, // String index of the candidate name (5)
                
                (byte) 0x02, // Event type (2)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD2, // Trace id (1234)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x14, // Timestamp (1300)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Location index (0)
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 // String index of the use case name (0)
        };
    }

}
