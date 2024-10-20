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

class DeploymentModelBuilder extends DeploymentModelBaseVisitor<Void> {

    private static final TransactionBehavior DEFAULT_TX_BEHAVIOR = TransactionBehavior.SUPPORTED;
    
    private static final TransactionPropagation DEFAULT_TX_PROPAGATION = TransactionPropagation.NONE;
    
    private static final ReadWriteConflictBehavior DEFAULT_RW_CONFLICT_BEHAVIOR = ReadWriteConflictBehavior.STALE_READ;

    private final Map<String, Component> nameToComponent = new HashMap<>();
    
    private final Set<ComponentPair> knownConnections = new HashSet<>();
    
    private final Set<String> knownDataStores = new HashSet<>();

    private final Set<String> knownUseCases = new HashSet<>();

    private final Set<String> knownServiceCandidates = new HashSet<>();
    
    private final Set<String> knownEntityTypes = new HashSet<>();

    private final DeploymentModel.Builder builder;

    private Component currentComponent;
    
    private DataStore currentDataStore;
    
    public DeploymentModelBuilder() {
        this.builder = new DeploymentModel.Builder();
    }
    
    public DeploymentModelBuilder(DeploymentModel originalModel) {
        this.builder = originalModel.applyModifications();
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

        var component = new Component(name);
        this.nameToComponent.put(name, component);

        this.currentComponent = component;
        this.visitChildren(context);
        this.currentComponent = null;

        return null;
    }

    @Override
    public Void visitUseCaseDeclaration(UseCaseDeclarationContext context) {
        var name = nameToString(context.name());

        if (this.knownUseCases.contains(name)) {
            throw new DeploymentModelParseException(context.refToken, "Duplicate use case '" + name + "'.");
        }

        var useCase = new UseCase(name);
        this.knownUseCases.add(name);

        // Assign the use case to the current component
        this.builder.assignUseCase(useCase, this.currentComponent);

        return null;
    }

    @Override
    public Void visitServiceCandidateDeclaration(ServiceCandidateDeclarationContext context) {
        var name = nameToString(context.name());

        if (this.knownServiceCandidates.contains(name)) {
            throw new DeploymentModelParseException(context.refToken, "Duplicate service candidate '" + name + "'.");
        }

        var properties = toPropertyMap(context.properties);

        var transactionBehavior = determineTransactionBehavior(properties);
        var serviceCandidate = new ServiceCandidate(name, transactionBehavior);

        this.builder.assignServiceCandidate(serviceCandidate, this.currentComponent);

        return null;
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

    private Component resolveComponent(String name, Token refToken) {
        var component = this.nameToComponent.get(name);
        if (component == null) {
            throw new DeploymentModelParseException(refToken, "Component '" + name + "' does not exist.");
        }

        return component;
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
    
    private static int determineConnectionLatency(Map<String, PropertyValue> properties, Token refToken) {
        var propertyValue = properties.get("latency");
        if (propertyValue == null) {
            throw new DeploymentModelParseException(refToken, "No latency specified.");
        }
        
        try {
            return Integer.parseInt(propertyValue.value());
        } catch (NumberFormatException e) {
            throw new DeploymentModelParseException(propertyValue.token(), "Invalid latency '" + propertyValue.value() + "'.");
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
        var symmetric = (context.symmetric != null);
        var properties = toPropertyMap(context.properties);
        
        var latency = determineConnectionLatency(properties, context.refToken);
        var transactionPropagation = determineTransactionPropagation(properties);
        
        if (symmetric) {
            // TODO Check for existing "opposite" connections
            this.builder.addSymmetricRemoteConnection(sourceComponent, targetComponent, latency, transactionPropagation);
        } else {
            this.builder.addRemoteConnection(sourceComponent, targetComponent, latency, transactionPropagation);
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
        var dataStore = new DataStore(name, readWriteConflictBehavior);
        
        this.knownDataStores.add(name);
        
        this.currentDataStore = dataStore;
        this.visitChildren(context);
        this.currentDataStore = null;
        
        return null;
    }
    
    @Override
    public Void visitEntityTypeDeclaration(EntityTypeDeclarationContext context) {
        var name = nameToString(context.name());
        
        if (this.knownEntityTypes.contains(name)) {
            throw new DeploymentModelParseException(context.refToken, "Duplicate entity type '" + name + "'.");
        }
        
        this.knownEntityTypes.add(name);
        
        var entityType = new EntityType(name);
        this.builder.assignEntityType(entityType, this.currentDataStore);
        
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
    };
    
    private record ComponentPair(Component component1, Component component2) {
    };

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
