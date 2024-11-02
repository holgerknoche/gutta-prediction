package gutta.prediction.dsl;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DataStore;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.EntityType;
import gutta.prediction.domain.ReadWriteConflictBehavior;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.UseCase;

class StandaloneDeploymentModelBuilder extends DeploymentModelBuilder {
        
    public StandaloneDeploymentModelBuilder() {
        super(new DeploymentModel.Builder());
    }
    
    @Override
    protected Component buildComponent(String name) {
        return new Component(name);
    }
    
    @Override
    protected UseCase buildUseCase(String name) {
        return new UseCase(name);
    }
    
    @Override
    protected ServiceCandidate buildServiceCandidate(String name, TransactionBehavior transactionBehavior, boolean asynchronous) {
        return new ServiceCandidate(name, transactionBehavior, asynchronous);
    }

    @Override
    protected DataStore buildDataStore(String name, ReadWriteConflictBehavior readWriteConflictBehavior) {
        return new DataStore(name, readWriteConflictBehavior);
    }

    @Override
    protected EntityType buildEntityType(String name) {
        return new EntityType(name);
    }

}
