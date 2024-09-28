package gutta.prediction.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DeploymentModel {
        
    private final Map<UseCase, Component> useCaseAllocation;
    
    private final Map<ServiceCandidate, Component> serviceCandidateAllocation;
    
    private final Map<ConnectionKey, ComponentConnection> componentConnections;
    
    private final Map<EntityType, DataStore> entityTypeAllocation;
    
    private final Map<String, UseCase> useCaseLookup;
    
    private final Map<String, ServiceCandidate> serviceCandidateLookup;        
    
    private DeploymentModel(Map<UseCase, Component> useCaseAllocation, Map<ServiceCandidate, Component> serviceCandidateAllocation, Map<EntityType, DataStore> entityTypeAllocation, Map<ConnectionKey, ComponentConnection> componentConnections) {
        this.useCaseAllocation = useCaseAllocation;
        this.serviceCandidateAllocation = serviceCandidateAllocation;        
        this.entityTypeAllocation = entityTypeAllocation;
        this.componentConnections = componentConnections;
        
        this.useCaseLookup = useCaseAllocation.keySet().stream().collect(Collectors.toMap(UseCase::name, Function.identity()));
        this.serviceCandidateLookup = serviceCandidateAllocation.keySet().stream().collect(Collectors.toMap(ServiceCandidate::name, Function.identity()));
    }
        
    public Optional<ServiceCandidate> resolveServiceCandidateByName(String candidateName) {
        return Optional.ofNullable(this.serviceCandidateLookup.get(candidateName));
    }
    
    public Optional<UseCase> resolveUseCaseByName(String useCaseName) {
        return Optional.ofNullable(this.useCaseLookup.get(useCaseName));
    }
    
    public Optional<Component> getComponentForUseCase(UseCase useCase) {
        return Optional.ofNullable(this.useCaseAllocation.get(useCase));
    }
    
    public Optional<Component> getComponentForServiceCandidate(ServiceCandidate serviceCandidate) {
        return Optional.ofNullable(this.serviceCandidateAllocation.get(serviceCandidate));
    }
    
    public Optional<DataStore> getDataStoreForEntityType(EntityType entityType) {
        return Optional.ofNullable(this.entityTypeAllocation.get(entityType));
    }
    
    public Optional<ComponentConnection> getConnection(Component source, Component target) {
        if (source.equals(target)) {
            return Optional.of(new LocalComponentConnection(source, target, false));
        } else {
            var searchKey = new ConnectionKey(source, target);
            return Optional.ofNullable(this.componentConnections.get(searchKey));
        }
    }
    
    public Builder applyModifications() {
        return new Builder(this.useCaseAllocation, this.serviceCandidateAllocation, this.entityTypeAllocation, this.componentConnections);
    }
    
    private record ConnectionKey(Component source, Component target) {

        public ConnectionKey(ComponentConnection connection) {
            this(connection.source(), connection.target());
        }

    }
    
    public static class Builder {
        
        private final Map<UseCase, Component> useCaseAllocation;
        
        private final Map<ServiceCandidate, Component> serviceCandidateAllocation;
        
        private final Map<EntityType, DataStore> entityTypeAllocation;
        
        private final Map<ConnectionKey, ComponentConnection> componentConnections;
        
        private final boolean modificationInProgress;
        
        public Builder() {
            this.useCaseAllocation = new HashMap<>();
            this.serviceCandidateAllocation = new HashMap<>();
            this.entityTypeAllocation = new HashMap<>();
            this.componentConnections = new HashMap<>();
            this.modificationInProgress = false;
        }
        
        private Builder(Map<UseCase, Component> useCaseAllocation, Map<ServiceCandidate, Component> serviceCandidateAllocation, Map<EntityType, DataStore> entityTypeAllocation, Map<ConnectionKey, ComponentConnection> componentConnections) {
            this.useCaseAllocation = new HashMap<>(useCaseAllocation);
            this.serviceCandidateAllocation = new HashMap<>(serviceCandidateAllocation);
            this.entityTypeAllocation = new HashMap<>(entityTypeAllocation);
            this.componentConnections = new HashMap<>(componentConnections);
            this.modificationInProgress = true;
        }
        
        public Builder assignUseCase(UseCase useCase, Component component) {
            this.useCaseAllocation.put(useCase, component);
            return this;
        }
        
        public Builder assignServiceCandidate(ServiceCandidate candidate, Component component) {
            this.serviceCandidateAllocation.put(candidate, component);
            return this;
        }
        
        public Builder assignEntityType(EntityType entityType, DataStore dataStore) {
            this.entityTypeAllocation.put(entityType, dataStore);
            return this;
        }

        private void addConnection(ComponentConnection connection) {
            var connectionKey = new ConnectionKey(connection);
            this.componentConnections.put(connectionKey, connection);
            
            if (connection.isSymmetric() && !(connection.source().equals(connection.target()))) {
                var symmetricKey = new ConnectionKey(connection.target(), connection.source());
                this.componentConnections.put(symmetricKey, connection);
            }
        }
        
        public Builder addLocalConnection(Component sourceComponent, Component targetComponent) {
            this.addConnection(new LocalComponentConnection(sourceComponent, targetComponent, this.modificationInProgress));
            return this;
        }
        
        public Builder addRemoteConnection(Component sourceComponent, Component targetComponent, long latency, TransactionPropagation propagation) {
            this.addConnection(new RemoteComponentConnection(sourceComponent, targetComponent, false, latency, propagation, this.modificationInProgress));
            return this;
        }        
        
        public Builder addSymmetricRemoteConnection(Component sourceComponent, Component targetComponent, long latency, TransactionPropagation propagation) {
            this.addConnection(new RemoteComponentConnection(sourceComponent, targetComponent, true, latency, propagation, this.modificationInProgress));
            return this;
        }
        
        public DeploymentModel build() {
            return new DeploymentModel(this.useCaseAllocation, this.serviceCandidateAllocation, this.entityTypeAllocation, this.componentConnections);
        }
        
    }
    
}
