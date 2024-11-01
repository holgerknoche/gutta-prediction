package gutta.prediction.domain;

import gutta.prediction.util.EqualityUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeploymentModel {
    
    private final ComponentAllocation<UseCase> useCaseAllocation;
    
    private final ComponentAllocation<ServiceCandidate> serviceCandidateAllocation;
    
    private final ComponentAllocation<EntityType> entityTypeAllocation;
        
    private final Map<ConnectionKey, ComponentConnection> componentConnections;
    
    private final Map<EntityType, DataStore> entityToDataStore;
    
    private final Map<String, UseCase> useCaseLookup;
    
    private final Map<String, ServiceCandidate> serviceCandidateLookup;
    
    private final Map<String, Component> componentLookup;
    
    private final Map<String, DataStore> dataStoreLookup;
    
    private final Map<String, EntityType> entityTypeLookup;
    
    public static Builder builder() {
        return new Builder();
    }
    
    private DeploymentModel(ComponentAllocation<UseCase> useCaseAllocation, ComponentAllocation<ServiceCandidate> serviceCandidateAllocation, ComponentAllocation<EntityType> entityTypeAllocation, Map<EntityType, DataStore> entityToDataStore, Map<ConnectionKey, ComponentConnection> componentConnections) {
        this.useCaseAllocation = useCaseAllocation;
        this.serviceCandidateAllocation = serviceCandidateAllocation;
        this.entityTypeAllocation = entityTypeAllocation;
        this.entityToDataStore = entityToDataStore;
        this.componentConnections = componentConnections;
        
        var allComponents = Stream.concat(this.serviceCandidateAllocation.values().stream(), this.useCaseAllocation.values().stream())
                .map(ComponentAllocationEntry::component)
                .collect(Collectors.toSet());        
        
        // Build name-based lookups
        this.useCaseLookup = useCaseAllocation.keySet().stream().collect(Collectors.toMap(UseCase::name, Function.identity()));
        this.serviceCandidateLookup = serviceCandidateAllocation.keySet().stream().collect(Collectors.toMap(ServiceCandidate::name, Function.identity()));                
        this.componentLookup = allComponents.stream().collect(Collectors.toMap(Component::name, Function.identity()));
        this.dataStoreLookup = this.entityToDataStore.values().stream().collect(Collectors.toMap(DataStore::name, Function.identity()));
        this.entityTypeLookup = this.entityToDataStore.keySet().stream().collect(Collectors.toMap(EntityType::name, Function.identity()));
    }
            
    public Optional<ServiceCandidate> resolveServiceCandidateByName(String candidateName) {
        return Optional.ofNullable(this.serviceCandidateLookup.get(candidateName));
    }
    
    public Optional<UseCase> resolveUseCaseByName(String useCaseName) {
        return Optional.ofNullable(this.useCaseLookup.get(useCaseName));
    }
    
    public Optional<Component> resolveComponentByName(String componentName) {
        return Optional.ofNullable(this.componentLookup.get(componentName));
    }
    
    public Optional<DataStore> resolveDataStoreByName(String dataStoreName) {
        return Optional.ofNullable(this.dataStoreLookup.get(dataStoreName));
    }
    
    public Optional<EntityType> resolveEntityTypeByName(String entityTypeName) {
        return Optional.ofNullable(this.entityTypeLookup.get(entityTypeName));
    }
    
    public Optional<ComponentAllocationEntry<UseCase>> getComponentAllocationForUseCase(UseCase useCase) {
        return this.useCaseAllocation.get(useCase);
    }
    
    public Optional<ComponentAllocationEntry<ServiceCandidate>> getComponentAllocationForServiceCandidate(ServiceCandidate serviceCandidate) {
        return this.serviceCandidateAllocation.get(serviceCandidate);
    }
    
    public Optional<ComponentAllocationEntry<EntityType>> getComponentAllocationForEntityType(EntityType entityType) {
        return this.entityTypeAllocation.get(entityType);
    }
    
    public Optional<DataStore> getDataStoreForEntityType(EntityType entityType) {
        return Optional.ofNullable(this.entityToDataStore.get(entityType));
    }
    
    public Optional<ComponentConnection> getConnection(Component source, Component target, boolean modifiedAllocation) {
        if (source.equals(target)) {
            return Optional.of(new LocalComponentConnection(source, target, modifiedAllocation));
        } else {
            var searchKey = new ConnectionKey(source, target);
            return Optional.ofNullable(this.componentConnections.get(searchKey));
        }
    }
    
    public Builder applyModifications() {
        return new Builder(this.useCaseAllocation, this.serviceCandidateLookup, this.serviceCandidateAllocation, this.entityTypeLookup, this.entityTypeAllocation, this.entityToDataStore, this.componentConnections);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.useCaseAllocation, this.serviceCandidateAllocation, this.entityTypeAllocation, this.entityToDataStore, this.componentConnections);
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    private boolean equalsInternal(DeploymentModel that) {
        return Objects.equals(this.useCaseAllocation, that.useCaseAllocation) &&
                Objects.equals(this.serviceCandidateAllocation, that.serviceCandidateAllocation) &&
                Objects.equals(this.entityTypeAllocation, that.entityTypeAllocation) &&
                Objects.equals(this.entityToDataStore, that.entityToDataStore) &&
                Objects.equals(this.componentConnections, that.componentConnections);
    }
    
    private record ConnectionKey(Component source, Component target) {

        public ConnectionKey(ComponentConnection connection) {
            this(connection.source(), connection.target());
        }

    }    
    
    public static class Builder {
        
        private final ComponentAllocation<UseCase> useCaseAllocation;
        
        private final Map<String, ServiceCandidate> nameToServiceCandidate;
        
        private final ComponentAllocation<ServiceCandidate> serviceCandidateAllocation;
                
        private final Map<String, EntityType> nameToEntityType;
        
        private final ComponentAllocation<EntityType> entityTypeAllocation;
        
        private final Map<EntityType, DataStore> entityToDataStore;
        
        private final Map<ConnectionKey, ComponentConnection> componentConnections;
        
        private final boolean modificationInProgress;
        
        public Builder() {
            this.useCaseAllocation = new ComponentAllocation<>();
            this.nameToServiceCandidate = new HashMap<>();
            this.serviceCandidateAllocation = new ComponentAllocation<>();
            this.nameToEntityType = new HashMap<>();
            this.entityTypeAllocation = new ComponentAllocation<>();
            this.entityToDataStore = new HashMap<>();
            this.componentConnections = new HashMap<>();            
            this.modificationInProgress = false;
        }
        
        private Builder(ComponentAllocation<UseCase> useCaseAllocation, Map<String, ServiceCandidate> nameToServiceCandidate, ComponentAllocation<ServiceCandidate> serviceCandidateAllocation, Map<String, EntityType> nameToEntityType, ComponentAllocation<EntityType> entityTypeAllocation, Map<EntityType, DataStore> entityToDataStore, Map<ConnectionKey, ComponentConnection> componentConnections) {
            this.useCaseAllocation = new ComponentAllocation<>(useCaseAllocation);
            this.nameToServiceCandidate = new HashMap<>(nameToServiceCandidate);
            this.serviceCandidateAllocation = new ComponentAllocation<>(serviceCandidateAllocation);
            this.nameToEntityType = new HashMap<>(nameToEntityType);
            this.entityTypeAllocation = new ComponentAllocation<>(entityTypeAllocation);            
            this.entityToDataStore = new HashMap<>(entityToDataStore);
            this.componentConnections = new HashMap<>(componentConnections);
            this.modificationInProgress = true;
        }
        
        public Builder assignUseCaseToComponent(UseCase useCase, Component component) {
            var componentAllocationEntry = new ComponentAllocationEntry<>(useCase, this.modificationInProgress, component);
            this.useCaseAllocation.addAllocation(useCase, componentAllocationEntry);
            return this;
        }
        
        public Builder assignServiceCandidateToComponent(ServiceCandidate candidate, Component component) {
            var previousCandidate = this.nameToServiceCandidate.put(candidate.name(), candidate);
                        
            var componentAllocationEntry = new ComponentAllocationEntry<>(candidate, this.modificationInProgress, component);
            this.serviceCandidateAllocation.replaceOrAddAllocation(previousCandidate, candidate, componentAllocationEntry);
            return this;
        }
        
        public Builder assignEntityTypeToComponent(EntityType entityType, Component component) {
            var previousEntityType = this.nameToEntityType.put(entityType.name(), entityType);
            
            var componentAllocationEntry = new ComponentAllocationEntry<>(entityType, this.modificationInProgress, component);
            this.entityTypeAllocation.replaceOrAddAllocation(previousEntityType, entityType, componentAllocationEntry);            
            return this;
        }
        
        public Builder assignEntityTypeToDataStore(EntityType entityType, DataStore dataStore) {
            this.entityToDataStore.put(entityType, dataStore);
            return this;
        }

        private void addConnection(ComponentConnection connection) {
            var connectionKey = new ConnectionKey(connection);
            this.componentConnections.put(connectionKey, connection);            
        }
        
        public Builder addLocalConnection(Component sourceComponent, Component targetComponent) {
            this.addConnection(new LocalComponentConnection(sourceComponent, targetComponent, this.modificationInProgress));
            this.addConnection(new LocalComponentConnection(targetComponent, sourceComponent, this.modificationInProgress));
            return this;
        }
        
        public Builder addRemoteConnection(Component sourceComponent, Component targetComponent, long latency, TransactionPropagation propagation) {
            this.addConnection(new RemoteComponentConnection(sourceComponent, targetComponent, latency, propagation, this.modificationInProgress));
            return this;
        }        
        
        public Builder addSymmetricRemoteConnection(Component sourceComponent, Component targetComponent, long latency, TransactionPropagation propagation) {
            this.addConnection(new RemoteComponentConnection(sourceComponent, targetComponent, latency, propagation, this.modificationInProgress));
            this.addConnection(new RemoteComponentConnection(targetComponent, sourceComponent, latency, propagation, this.modificationInProgress));
            return this;
        }
        
        public DeploymentModel build() {
            return new DeploymentModel(this.useCaseAllocation, this.serviceCandidateAllocation, this.entityTypeAllocation, this.entityToDataStore, this.componentConnections);
        }
        
    }
    
}
