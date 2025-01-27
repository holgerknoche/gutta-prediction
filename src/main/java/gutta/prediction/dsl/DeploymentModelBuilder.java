package gutta.prediction.dsl;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DataStore;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.EntityType;
import gutta.prediction.domain.ReadWriteConflictBehavior;
import gutta.prediction.domain.ServiceCandidate;
import gutta.prediction.domain.TransactionBehavior;
import gutta.prediction.domain.TransactionPropagation;
import gutta.prediction.domain.UseCase;
import gutta.prediction.dsl.DeploymentModelParser.ComponentDeclarationContext;
import gutta.prediction.dsl.DeploymentModelParser.DataStoreDeclarationContext;
import gutta.prediction.dsl.DeploymentModelParser.EntityTypeDeclarationContext;
import gutta.prediction.dsl.DeploymentModelParser.EntityTypeReferenceContext;
import gutta.prediction.dsl.DeploymentModelParser.LocalComponentConnectionDeclarationContext;
import gutta.prediction.dsl.DeploymentModelParser.NameContext;
import gutta.prediction.dsl.DeploymentModelParser.PropertiesDeclarationContext;
import gutta.prediction.dsl.DeploymentModelParser.PropertyValueContext;
import gutta.prediction.dsl.DeploymentModelParser.RemoteComponentConnectionDeclarationContext;
import gutta.prediction.dsl.DeploymentModelParser.ServiceCandidateDeclarationContext;
import gutta.prediction.dsl.DeploymentModelParser.UseCaseDeclarationContext;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * A {@link DeploymentModelBuilder} creates a {@linkplain DeploymentModel deployment model} from the parser output. Specialized deployment builders are used for
 * either building a fully specified model or a delta model for an existing deployment model.
 */
abstract class DeploymentModelBuilder extends DeploymentModelBaseVisitor<Void> {

    private static final TransactionBehavior DEFAULT_TX_BEHAVIOR = TransactionBehavior.defaultBehavior();

    private static final TransactionPropagation DEFAULT_TX_PROPAGATION = TransactionPropagation.defaultPropagation();

    private static final ReadWriteConflictBehavior DEFAULT_RW_CONFLICT_BEHAVIOR = ReadWriteConflictBehavior.defaultBehavior();

    private final Map<String, Component> nameToComponent = new HashMap<>();

    private final Map<String, EntityType> nameToEntityType = new HashMap<>();

    private final Set<ComponentPair> knownConnections = new HashSet<>();

    private final Set<String> knownDataStores = new HashSet<>();

    private final Set<String> knownUseCases = new HashSet<>();

    private final Set<String> knownServiceCandidates = new HashSet<>();

    private final Set<String> knownEntityTypes = new HashSet<>();

    private Component currentComponent;

    private DataStore currentDataStore;

    private final DeploymentModel.Builder builder;

    protected DeploymentModelBuilder(DeploymentModel.Builder builder) {
        this.builder = builder;
    }

    public DeploymentModel getBuiltModel() {
        return this.builder.build();
    }

    @Override
    public Void visitComponentDeclaration(ComponentDeclarationContext context) {
        var name = nameToString(context.name());

        if (nameToComponent.containsKey(name)) {
            throw new DeploymentModelParseException(context.refToken, "Duplicate component '" + name + "'.");
        }

        var component = this.buildComponent(name);
        this.nameToComponent.put(name, component);

        this.currentComponent = component;
        this.visitChildren(context);
        this.currentComponent = null;

        return null;
    }

    protected Component buildComponent(String name) {
        return new Component(name);
    }

    @Override
    public Void visitUseCaseDeclaration(UseCaseDeclarationContext context) {
        var name = nameToString(context.name());

        if (this.knownUseCases.contains(name)) {
            throw new DeploymentModelParseException(context.refToken, "Duplicate use case '" + name + "'.");
        }

        var useCase = this.buildUseCase(name);
        this.knownUseCases.add(name);

        // Assign the use case to the current component
        this.builder.assignUseCaseToComponent(useCase, this.currentComponent);

        return null;
    }

    protected UseCase buildUseCase(String name) {
        return new UseCase(name);
    }

    @Override
    public Void visitServiceCandidateDeclaration(ServiceCandidateDeclarationContext context) {
        var name = nameToString(context.name());

        if (this.knownServiceCandidates.contains(name)) {
            throw new DeploymentModelParseException(context.refToken, "Duplicate service candidate '" + name + "'.");
        }

        var properties = toPropertyMap(context.properties);

        var transactionBehavior = determineTransactionBehavior(properties);
        var asynchronous = (context.asynchronous != null);
        var serviceCandidate = this.buildServiceCandidate(name, transactionBehavior, asynchronous);

        this.builder.assignServiceCandidateToComponent(serviceCandidate, this.currentComponent);

        return null;
    }

    protected ServiceCandidate buildServiceCandidate(String name, TransactionBehavior transactionBehavior, boolean asynchronous) {
        return new ServiceCandidate(name, transactionBehavior, asynchronous);
    }

    private static TransactionBehavior determineTransactionBehavior(Map<String, PropertyValue> properties) {
        var propertyValue = properties.get("transactionBehavior");
        if (propertyValue == null) {
            return DEFAULT_TX_BEHAVIOR;
        }

        try {
            return TransactionBehavior.valueOf(propertyValue.value().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DeploymentModelParseException(propertyValue.token(), "Unsupported transaction behavior '" + propertyValue.value() + "'.");
        }
    }

    protected Component resolveComponentByName(String name) {
        return this.nameToComponent.get(name);
    }

    private Component resolveComponent(String name, Token refToken) {
        var component = this.resolveComponentByName(name);
        if (component == null) {
            throw new DeploymentModelParseException(refToken, "Component '" + name + "' does not exist.");
        }

        return component;
    }

    protected EntityType resolveEntityTypeByName(String name) {
        return this.nameToEntityType.get(name);
    }

    private EntityType resolveEntityType(String name, Token refToken) {
        var entityType = this.resolveEntityTypeByName(name);
        if (entityType == null) {
            throw new DeploymentModelParseException(refToken, "Entity type '" + name + "' does not exist.");
        }

        return entityType;
    }

    private Void processComponentConnection(NameContext sourceName, NameContext targetName, BiConsumer<Component, Component> specificAction) {
        var sourceComponentName = nameToString(sourceName);
        var targetComponentName = nameToString(targetName);

        var sourceComponent = this.resolveComponent(sourceComponentName, sourceName.start);
        var targetComponent = this.resolveComponent(targetComponentName, targetName.start);

        // Check for duplicates
        var componentPair = new ComponentPair(sourceComponent, targetComponent);
        if (this.knownConnections.contains(componentPair)) {
            throw new DeploymentModelParseException(sourceName.start, "Duplicate connection from '" + sourceName + "' to '" + targetName + "'.");
        }

        specificAction.accept(sourceComponent, targetComponent);
        return null;
    }

    @Override
    public Void visitLocalComponentConnectionDeclaration(LocalComponentConnectionDeclarationContext context) {
        return this.processComponentConnection(context.source, context.target,
                (sourceComponent, targetComponent) -> this.buildLocalComponentConnection(sourceComponent, targetComponent, context));
    }

    private void buildLocalComponentConnection(Component sourceComponent, Component targetComponent, LocalComponentConnectionDeclarationContext context) {
        this.builder.addLocalConnection(sourceComponent, targetComponent);
    }

    @Override
    public Void visitRemoteComponentConnectionDeclaration(RemoteComponentConnectionDeclarationContext context) {
        return this.processComponentConnection(context.source, context.target,
                (sourceComponent, targetComponent) -> this.buildRemoteComponentConnection(sourceComponent, targetComponent, context));
    }

    private static int determineConnectionOverhead(Map<String, PropertyValue> properties, Token refToken) {
        var propertyValue = properties.get("overhead");
        if (propertyValue == null) {
            throw new DeploymentModelParseException(refToken, "No overhead specified.");
        }

        try {
            return Integer.parseInt(propertyValue.value());
        } catch (NumberFormatException e) {
            throw new DeploymentModelParseException(propertyValue.token(), "Invalid overhead '" + propertyValue.value() + "'.");
        }
    }

    private static TransactionPropagation determineTransactionPropagation(Map<String, PropertyValue> properties) {
        var propertyValue = properties.get("transactionPropagation");
        if (propertyValue == null) {
            return DEFAULT_TX_PROPAGATION;
        }

        try {
            return TransactionPropagation.valueOf(propertyValue.value().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DeploymentModelParseException(propertyValue.token(), "Unsupported transaction propagation '" + propertyValue.value() + "'.");
        }
    }

    private void buildRemoteComponentConnection(Component sourceComponent, Component targetComponent, RemoteComponentConnectionDeclarationContext context) {
        var asymmetric = (context.asymmetric != null);
        var properties = toPropertyMap(context.properties);

        var overhead = determineConnectionOverhead(properties, context.refToken);
        var transactionPropagation = determineTransactionPropagation(properties);

        if (asymmetric) {
            this.builder.addRemoteConnection(sourceComponent, targetComponent, overhead, transactionPropagation);
        } else {
            // TODO Check for existing "opposite" connections
            this.builder.addSymmetricRemoteConnection(sourceComponent, targetComponent, overhead, transactionPropagation);
        }
    }

    private static ReadWriteConflictBehavior determineReadWriteConflictBehavior(Map<String, PropertyValue> properties) {
        var propertyValue = properties.get("readWriteConflictBehavior");
        if (propertyValue == null) {
            return DEFAULT_RW_CONFLICT_BEHAVIOR;
        }

        try {
            return ReadWriteConflictBehavior.valueOf(propertyValue.value().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DeploymentModelParseException(propertyValue.token(), "Unsupported read-write conflict behavior '" + propertyValue.value() + "'.");
        }
    }

    @Override
    public Void visitDataStoreDeclaration(DataStoreDeclarationContext context) {
        var name = nameToString(context.name());
        var properties = toPropertyMap(context.properties);

        if (this.knownDataStores.contains(name)) {
            throw new DeploymentModelParseException(context.refToken, "Duplicate data store '" + name + "'.");
        }

        var readWriteConflictBehavior = determineReadWriteConflictBehavior(properties);
        var dataStore = this.buildDataStore(name, readWriteConflictBehavior);

        this.knownDataStores.add(name);

        this.currentDataStore = dataStore;
        this.visitChildren(context);
        this.currentDataStore = null;

        return null;
    }

    protected DataStore buildDataStore(String name, ReadWriteConflictBehavior readWriteConflictBehavior) {
        return new DataStore(name, readWriteConflictBehavior);
    }

    @Override
    public Void visitEntityTypeDeclaration(EntityTypeDeclarationContext context) {
        var name = nameToString(context.typeName);

        if (this.knownEntityTypes.contains(name)) {
            throw new DeploymentModelParseException(context.refToken, "Duplicate entity type '" + name + "'.");
        }

        this.knownEntityTypes.add(name);

        var rootTypeName = (context.rootTypeName != null) ? nameToString(context.rootTypeName) : null;
        var entityType = this.buildEntityType(name, rootTypeName, context.refToken);
        this.builder.assignEntityTypeToComponent(entityType, this.currentComponent);

        this.nameToEntityType.put(name, entityType);

        return null;
    }

    protected EntityType buildEntityType(String name, String rootTypeName, Token referenceToken) {
        if (rootTypeName != null) {
            var rootType = this.resolveEntityTypeByName(rootTypeName);
            if (rootType == null) {
                throw new DeploymentModelParseException(referenceToken, "Root type '" + rootTypeName + "' of entity type '" + name + "' is not defined.");
            } else if (rootType.rootType() != null) {
                throw new DeploymentModelParseException(referenceToken, "Root type '" + rootTypeName + "' must not have a root type itself.");
            }

            return new EntityType(name, rootType);
        } else {
            return new EntityType(name);
        }
    }

    @Override
    public Void visitEntityTypeReference(EntityTypeReferenceContext context) {
        var name = nameToString(context.name());
        var entityType = this.resolveEntityType(name, context.refToken);

        this.builder.assignEntityTypeToDataStore(entityType, this.currentDataStore);

        return null;
    }

    private static Map<String, PropertyValue> toPropertyMap(PropertiesDeclarationContext context) {
        if (context == null || context.properties.isEmpty()) {
            // If no properties have been specified, return an empty map
            return Map.of();
        }

        var properties = new HashMap<String, PropertyValue>(context.properties.size());

        for (var property : context.properties) {
            var propertyName = nameToString(property.name());
            var propertyValueString = valueToString(property.propertyValue());

            var propertyValue = new PropertyValue(propertyValueString, property.start);

            var previousValue = properties.put(propertyName, propertyValue);
            if (previousValue != null) {
                throw new DeploymentModelParseException(property.name().getStart(), "Duplicate property '" + propertyName + "'.");
            }
        }

        return properties;
    }

    private static String unquote(String input) {
        return input.substring(1, input.length() - 1);
    }

    private static String nameToString(NameContext context) {
        if (context.ID() != null) {
            return context.ID().getText();
        } else {
            return unquote(context.STRING_LITERAL().getText());
        }
    }

    private static String valueToString(PropertyValueContext context) {
        if (context.ID() != null) {
            return context.ID().getText();
        } else if (context.STRING_LITERAL() != null) {
            return unquote(context.STRING_LITERAL().getText());
        } else {
            return context.INT_LITERAL().getText();
        }
    }

    private record PropertyValue(String value, Token token) {
    }

    private record ComponentPair(Component component1, Component component2) {
    }

    static class DeploymentModelParseException extends RuntimeException {

        private static final long serialVersionUID = -3560623347221634775L;

        private static String formatMessage(Token token, String message) {
            return token.getLine() + "," + token.getCharPositionInLine() + ": " + message;
        }

        public DeploymentModelParseException(Token referenceToken, String message) {
            super(formatMessage(referenceToken, message));
        }

    }

}
