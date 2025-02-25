package gutta.prediction.simulation;

import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.Entity;
import gutta.prediction.event.EntityReadEvent;
import gutta.prediction.event.EntityWriteEvent;
import gutta.prediction.event.EventTrace;

import java.util.List;

/**
 * Specific {@link TraceSimulatorWorker} to implement the simulation mode {@link TraceSimulationMode#WITH_ENTITY_ACCESSES}. 
 */
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

            var currentCandidate = this.context.currentServiceCandidate();
            if (currentCandidate != null && currentCandidate.asynchronous()) {
                // If the current candidate is invoked asynchronously, register an asynchronously written entity
                this.context.registerAsynchronouslyChangedEntity(event.entity());
            }
        }
    }

    private boolean hasConflict(Entity entity, Transaction currentTransaction) {
        if (this.context.isAsynchronouslyChanged(entity)) {
            return true;
        }

        var changingTransaction = this.context.getTransactionWithPendingWriteTo(entity);

        if (changingTransaction == null) {
            return false;
        } else if (currentTransaction == null) {
            return true;
        } else {
            return !(changingTransaction.equals(currentTransaction));
        }
    }

    @Override
    protected void notifyListenersOfCommittedWrites(Transaction transaction, boolean asynchronous) {
        this.notifyListenersOfWrite(transaction, asynchronous, TraceSimulationListener::onCommittedWrite);
    }

    @Override
    protected void notifyListenersOfRevertedWrites(Transaction transaction, boolean asynchronous) {
        this.notifyListenersOfWrite(transaction, asynchronous, TraceSimulationListener::onRevertedWrite);
    }

    private void notifyListenersOfWrite(Transaction transaction, boolean asynchronous, WriteListenerNotifier notifier) {
        var pendingWrites = this.context.getAndRemovePendingWritesFor(transaction);
        pendingWrites.forEach(writeEvent -> this.listeners.forEach(listener -> notifier.notifyListener(listener, writeEvent, this.context)));

        if (asynchronous) {
            pendingWrites.stream().map(EntityWriteEvent::entity).forEach(this.context::registerAsynchronouslyChangedEntity);
        }
    }

    private interface WriteListenerNotifier {

        void notifyListener(TraceSimulationListener listener, EntityWriteEvent event, TraceSimulationContext context);

    }

}
