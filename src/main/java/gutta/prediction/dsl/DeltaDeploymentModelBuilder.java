package gutta.prediction.dsl;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DataStore;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.EntityType;
import gutta.prediction.domain.ReadWriteConflictBehavior;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.UseCase;

class DeltaDeploymentModelBuilder extends DeploymentModelBuilder {
    
    private final DeploymentModel originalDeploymentModel;
    
    public DeltaDeploymentModelBuilder(DeploymentModel originalDeploymentModel) {
        super(originalDeploymentModel.applyModifications());
        
        this.originalDeploymentModel = originalDeploymentModel;
    }
    
    @Override
    protected Component buildComponent(String name) {
        var existingComponent = this.originalDeploymentModel.resolveComponentByName(name);        
        return existingComponent.orElse(new Component(name));
    }
    
    @Override
    protected UseCase buildUseCase(String name) {
        var existingUseCase = this.originalDeploymentModel.resolveUseCaseByName(name);
        return existingUseCase.orElse(new UseCase(name));
    }
    
    @Override
    protected ServiceCandidate buildServiceCandidate(String name, TransactionBehavior transactionBehavior, boolean asynchronous) {
        var existingCandidate = this.originalDeploymentModel.resolveServiceCandidateByName(name);
        
        if (existingCandidate.isPresent() && existingCandidate.get().transactionBehavior() == transactionBehavior && existingCandidate.get().asynchronous() == asynchronous) {
            return existingCandidate.get();
        } else {
            return new ServiceCandidate(name, transactionBehavior, asynchronous);
        }
    }
    
    @Override
    protected DataStore buildDataStore(String name, ReadWriteConflictBehavior readWriteConflictBehavior) {
        var existingDataStore = this.originalDeploymentModel.resolveDataStoreByName(name);
        
        if (existingDataStore.isPresent() && existingDataStore.get().readWriteConflictBehavior() == readWriteConflictBehavior) {
            return existingDataStore.get();
        } else {
            return new DataStore(name, readWriteConflictBehavior);
        }
    }
        
    @Override
    protected EntityType buildEntityType(String name) {
        var existingEntityType = this.originalDeploymentModel.resolveEntityTypeByName(name);
        return existingEntityType.orElse(new EntityType(name));
    }
    
    @Override
    protected Component resolveComponentByName(String name) {
        var component = super.resolveComponentByName(name);
        if (component != null) {
            return component;
        }
        
        return this.originalDeploymentModel.resolveComponentByName(name).orElse(null);
    }

}
