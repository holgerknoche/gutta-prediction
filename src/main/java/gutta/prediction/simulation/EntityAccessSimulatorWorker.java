package gutta.prediction.simulation;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;

import java.util.List;

class EntityAccessSimulatorWorker extends TransactionTraceSimulatorWorker {

    public EntityAccessSimulatorWorker(List<TraceSimulationListener> listeners, EventTrace trace, DeploymentModel deploymentModel) {
        super(listeners, trace, deploymentModel);
    }
    
    @Override
    protected void updateSimulationOnReadEvent(EntityReadEvent event) {
        super.updateSimulationOnReadEvent(event);
        
        var currentTransaction = this.currentTransaction();                
        var entity = event.entity();

        if (this.hasConflict(entity, currentTransaction)) {
            this.listeners.forEach(listener -> listener.onReadWriteConflict(event, this.context));
        }
    }
    
    @Override
    protected void updateSimulationOnWriteEvent(EntityWriteEvent event) {
        var currentTransaction = this.currentTransaction();                
        var entity = event.entity();
        
        if (this.hasConflict(entity, currentTransaction)) {
            this.listeners.forEach(listener -> listener.onWriteWriteConflict(event, this.context));
            
            if (currentTransaction != null) {
                currentTransaction.setAbortOnly();
            }
        } else if (currentTransaction != null) {       
            // If a transaction is available, record the pending write
            this.context.registerPendingWrite(event);
        } else {
            // No transaction available, so the event is auto-committed
            this.listeners.forEach(listener -> listener.onCommittedWrite(event, this.context));
        }
    }
    
    private boolean hasConflict(Entity entity, Transaction currentTransaction) {
        var changingTransaction = this.context.getTransactionWithPendingWriteTo(entity);
        
        if (changingTransaction == null) {
            return false;
        } else if (currentTransaction == null) {
            return true;
        } else {
            return !(changingTransaction.equals(currentTransaction));
        }
    }
    
}
