package gutta.prediction.domain;

import gutta.prediction.util.EqualityUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link DeploymentModel} represents the deployment of a set of {@linkplain Component components} and other elements, both in the original state and the
 * state of an analyzed scenario.
 */
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

    private DeploymentModel(ComponentAllocation<UseCase> useCaseAllocation, ComponentAllocation<ServiceCandidate> serviceCandidateAllocation,
            ComponentAllocation<EntityType> entityTypeAllocation, Map<EntityType, DataStore> entityToDataStore,
            Map<ConnectionKey, ComponentConnection> componentConnections) {
        this.useCaseAllocation = useCaseAllocation;
        this.serviceCandidateAllocation = serviceCandidateAllocation;
        this.entityTypeAllocation = entityTypeAllocation;
        this.entityToDataStore = entityToDataStore;
        this.componentConnections = componentConnections;

        var allComponents = Stream.concat(this.serviceCandidateAllocation.values().stream(), this.useCaseAllocation.values().stream())
                .map(ComponentAllocationEntry::component).collect(Collectors.toSet());

        var dataStoreSet = this.entityToDataStore.values().stream().collect(Collectors.toSet());

        // Build name-based lookups
        this.useCaseLookup = useCaseAllocation.keySet().stream().collect(Collectors.toMap(UseCase::name, Function.identity()));
        this.serviceCandidateLookup = serviceCandidateAllocation.keySet().stream().collect(Collectors.toMap(ServiceCandidate::name, Function.identity()));
        this.componentLookup = allComponents.stream().collect(Collectors.toMap(Component::name, Function.identity()));
        this.dataStoreLookup = dataStoreSet.stream().collect(Collectors.toMap(DataStore::name, Function.identity()));
        this.entityTypeLookup = this.entityTypeAllocation.keySet().stream().collect(Collectors.toMap(EntityType::name, Function.identity()));
    }

    /**
     * Resolves a service candidate by its name in this model.
     * 
     * @param candidateName The name of the desired service candidate
     * @return The appropriate service candidate if it exists
     */
    public Optional<ServiceCandidate> resolveServiceCandidateByName(String candidateName) {
        return Optional.ofNullable(this.serviceCandidateLookup.get(candidateName));
    }

    /**
     * Resolves a use case by its name in this model.
     * 
     * @param useCaseName The name of the desired use case
     * @return The appropriate use case if it exists
     */
    public Optional<UseCase> resolveUseCaseByName(String useCaseName) {
        return Optional.ofNullable(this.useCaseLookup.get(useCaseName));
    }

    /**
     * Resolves a component by its name in this model.
     * 
     * @param componentName The name of the desired component
     * @return The appropriate component if it exists
     */
    public Optional<Component> resolveComponentByName(String componentName) {
        return Optional.ofNullable(this.componentLookup.get(componentName));
    }

    /**
     * Resolves a data store by its name in this model.
     * 
     * @param dataStoreName The name of the desired data store
     * @return The appropriate data store if it exists
     */
    public Optional<DataStore> resolveDataStoreByName(String dataStoreName) {
        return Optional.ofNullable(this.dataStoreLookup.get(dataStoreName));
    }

    /**
     * Resolves a entity type by its name in this model.
     * 
     * @param entityTypeName The name of the desired entity type
     * @return The appropriate entity type if it exists
     */
    public Optional<EntityType> resolveEntityTypeByName(String entityTypeName) {
        return Optional.ofNullable(this.entityTypeLookup.get(entityTypeName));
    }

    /**
     * Returns the {@linkplain ComponentAllocationEntry component allocation} for the given use case in this model.
     * 
     * @param useCase The use case to find the allocation for
     * @return The component allocation if it exists
     */
    public Optional<ComponentAllocationEntry<UseCase>> getComponentAllocationForUseCase(UseCase useCase) {
        return this.useCaseAllocation.get(useCase);
    }

    /**
     * Returns the {@linkplain ComponentAllocationEntry component allocation} for the given service candidate in this model.
     * 
     * @param serviceCandidate The service candidate to find the allocation for
     * @return The component allocation if it exists
     */
    public Optional<ComponentAllocationEntry<ServiceCandidate>> getComponentAllocationForServiceCandidate(ServiceCandidate serviceCandidate) {
        return this.serviceCandidateAllocation.get(serviceCandidate);
    }

    /**
     * Returns the {@linkplain ComponentAllocationEntry component allocation} for the given entity type in this model.
     * 
     * @param entityType The entity type to find the allocation for
     * @return The component allocation if it exists
     */
    public Optional<ComponentAllocationEntry<EntityType>> getComponentAllocationForEntityType(EntityType entityType) {
        return this.entityTypeAllocation.get(entityType);
    }

    /**
     * Returns the data store in which the given entity type is stored in this model.
     * 
     * @param entityType The entity type
     * @return The data store the entity type is stored in, provided an allocation exists
     */
    public Optional<DataStore> getDataStoreForEntityType(EntityType entityType) {
        return Optional.ofNullable(this.entityToDataStore.get(entityType));
    }

    /**
     * Gets the connection between the given components in this model.
     * 
     * @param source             The source component
     * @param target             The target component
     * @param modifiedAllocation A flag denoting whether this connection is considered to be modified. This is only relevant for local connections.
     * @return The connection if it exists
     */
    public Optional<ComponentConnection> getConnection(Component source, Component target, boolean modifiedAllocation) {
        if (source.equals(target)) {
            return Optional.of(new LocalComponentConnection(source, target, modifiedAllocation));
        } else {
            var searchKey = new ConnectionKey(source, target);
            return Optional.ofNullable(this.componentConnections.get(searchKey));
        }
    }

    /**
     * Creates a new builder based on this model to facilitate the specification of modifications.
     * 
     * @return A builder initialized with the current state of this model
     */
    public Builder applyModifications() {
        return new Builder(this.useCaseAllocation, this.serviceCandidateLookup, this.serviceCandidateAllocation, this.entityTypeLookup,
                this.entityTypeAllocation, this.entityToDataStore, this.componentConnections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.useCaseAllocation, this.serviceCandidateAllocation, this.entityTypeAllocation, this.entityToDataStore,
                this.componentConnections);
    }

    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }

    private boolean equalsInternal(DeploymentModel that) {
        return Objects.equals(this.useCaseAllocation, that.useCaseAllocation) && //
                Objects.equals(this.serviceCandidateAllocation, that.serviceCandidateAllocation) && //
                Objects.equals(this.entityTypeAllocation, that.entityTypeAllocation) && //
                Objects.equals(this.entityToDataStore, that.entityToDataStore) && //
                Objects.equals(this.componentConnections, that.componentConnections);
    }

    private record ConnectionKey(Component source, Component target) {

        public ConnectionKey(ComponentConnection connection) {
            this(connection.source(), connection.target());
        }

        public int hashCode() {
            return (this.source().hashCode() + this.target().hashCode());
        }

    }

    /**
     * A builder type for {@linkplain DeploymentModel deployment models}.
     */
    public static class Builder {

        private final ComponentAllocation<UseCase> useCaseAllocation;

        private final Map<String, ServiceCandidate> nameToServiceCandidate;

        private final ComponentAllocation<ServiceCandidate> serviceCandidateAllocation;

        private final Map<String, EntityType> nameToEntityType;

        private final ComponentAllocation<EntityType> entityTypeAllocation;

        private final Map<EntityType, DataStore> entityToDataStore;

        private final Map<ConnectionKey, ComponentConnection> componentConnections;

        private final boolean modificationInProgress;

        /**
         * Creates a new builder.
         */
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

        private Builder(ComponentAllocation<UseCase> useCaseAllocation, Map<String, ServiceCandidate> nameToServiceCandidate,
                ComponentAllocation<ServiceCandidate> serviceCandidateAllocation, Map<String, EntityType> nameToEntityType,
                ComponentAllocation<EntityType> entityTypeAllocation, Map<EntityType, DataStore> entityToDataStore,
                Map<ConnectionKey, ComponentConnection> componentConnections) {
            this.useCaseAllocation = new ComponentAllocation<>(useCaseAllocation);
            this.nameToServiceCandidate = new HashMap<>(nameToServiceCandidate);
            this.serviceCandidateAllocation = new ComponentAllocation<>(serviceCandidateAllocation);
            this.nameToEntityType = new HashMap<>(nameToEntityType);
            this.entityTypeAllocation = new ComponentAllocation<>(entityTypeAllocation);
            this.entityToDataStore = new HashMap<>(entityToDataStore);
            this.componentConnections = new HashMap<>(componentConnections);
            this.modificationInProgress = true;
        }

        /**
         * Assigns the given use case to the given component.
         * 
         * @param useCase   The use case to assign
         * @param component The component to assign the use case to
         * @return The builder for a fluent interface
         */
        public Builder assignUseCaseToComponent(UseCase useCase, Component component) {
            var componentAllocationEntry = new ComponentAllocationEntry<>(useCase, this.modificationInProgress, component);
            this.useCaseAllocation.addAllocation(useCase, componentAllocationEntry);
            return this;
        }

        /**
         * Assigns the given service candidate to the given component.
         * 
         * @param serviceCandidate The service candidate to assign
         * @param component        The component to assign the service candidate to
         * @return The builder for a fluent interface
         */
        public Builder assignServiceCandidateToComponent(ServiceCandidate serviceCandidate, Component component) {
            var previousCandidate = this.nameToServiceCandidate.put(serviceCandidate.name(), serviceCandidate);

            var componentAllocationEntry = new ComponentAllocationEntry<>(serviceCandidate, this.modificationInProgress, component);
            this.serviceCandidateAllocation.replaceOrAddAllocation(previousCandidate, serviceCandidate, componentAllocationEntry);
            return this;
        }

        /**
         * Assigns the given entity type to the given component.
         * 
         * @param entityType The entity type to assign
         * @param component  The component to assign the entity type to
         * @return The builder for a fluent interface
         */
        public Builder assignEntityTypeToComponent(EntityType entityType, Component component) {
            var previousEntityType = this.nameToEntityType.put(entityType.name(), entityType);

            var componentAllocationEntry = new ComponentAllocationEntry<>(entityType, this.modificationInProgress, component);
            this.entityTypeAllocation.replaceOrAddAllocation(previousEntityType, entityType, componentAllocationEntry);
            return this;
        }

        /**
         * Assigns the given entity type to the given data store.
         * 
         * @param entityType The entity type to assign
         * @param dataStore  The data store to assign the entity type to
         * @return The builder for a fluent interface
         */
        public Builder assignEntityTypeToDataStore(EntityType entityType, DataStore dataStore) {
            this.entityToDataStore.put(entityType, dataStore);
            return this;
        }

        private void addConnection(ComponentConnection connection) {
            var connectionKey = new ConnectionKey(connection);
            this.componentConnections.put(connectionKey, connection);
        }

        /**
         * Adds a local connection between the two components, replacing an existing connection if applicable.
         * 
         * @param sourceComponent The source component of the connection
         * @param targetComponent The target component of the connection
         * @return The builder for a fluent interface
         */
        public Builder addLocalConnection(Component sourceComponent, Component targetComponent) {
            this.addConnection(new LocalComponentConnection(sourceComponent, targetComponent, this.modificationInProgress));
            this.addConnection(new LocalComponentConnection(targetComponent, sourceComponent, this.modificationInProgress));
            return this;
        }

        /**
         * Adds an asymmetric remote connection between the two components, replacing an existing connection if applicable.
         * 
         * @param sourceComponent The source component of the connection
         * @param targetComponent The target component of the connection
         * @param overhead        The amount of overhead caused by this connection
         * @param propagation     The transaction propagation supported by this connection
         * @return The builder for a fluent interface
         */
        public Builder addRemoteConnection(Component sourceComponent, Component targetComponent, long overhead, TransactionPropagation propagation) {
            this.addConnection(new RemoteComponentConnection(sourceComponent, targetComponent, overhead, propagation, this.modificationInProgress));
            return this;
        }

        /**
         * Adds a symmetric remote connection between the two components, replacing an existing connection if applicable.
         * 
         * @param sourceComponent The source component of the connection
         * @param targetComponent The target component of the connection
         * @param overhead        The amount of overhead caused by this connection
         * @param propagation     The transaction propagation supported by this connection
         * @return The builder for a fluent interface
         */
        public Builder addSymmetricRemoteConnection(Component sourceComponent, Component targetComponent, long overhead, TransactionPropagation propagation) {
            this.addConnection(new RemoteComponentConnection(sourceComponent, targetComponent, overhead, propagation, this.modificationInProgress));
            this.addConnection(new RemoteComponentConnection(targetComponent, sourceComponent, overhead, propagation, this.modificationInProgress));
            return this;
        }

        /**
         * Builds the deployment model specified by the current state of this builder.
         * 
         * @return The resulting deployment model
         */
        public DeploymentModel build() {
            return new DeploymentModel(this.useCaseAllocation, this.serviceCandidateAllocation, this.entityTypeAllocation, this.entityToDataStore,
                    this.componentConnections);
        }

    }

}
