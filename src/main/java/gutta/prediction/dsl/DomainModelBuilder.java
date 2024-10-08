package gutta.prediction.dsl;

import gutta.prediction.domain.Component;
import gutta.prediction.domain.DeploymentModel;
import gutta.prediction.domain.UseCase;
import gutta.prediction.dsl.DomainModelParser.ComponentDeclarationContext;
import gutta.prediction.dsl.DomainModelParser.NameContext;
import gutta.prediction.dsl.DomainModelParser.ServiceCandidateDeclarationContext;
import gutta.prediction.dsl.DomainModelParser.UseCaseDeclarationContext;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class DomainModelBuilder extends DomainModelBaseVisitor<Void> {
    
    private final Map<String, Component> nameToComponent = new HashMap<>();
    
    private final Set<String> knownUseCases = new HashSet<>();
    
    private final DeploymentModel.Builder builder = new DeploymentModel.Builder();
    
    private Component currentComponent;
    
    public DeploymentModel getBuiltModel() {
        return this.builder.build();
    }
    
    @Override
    public Void visitComponentDeclaration(ComponentDeclarationContext context) {
        var name = nameToString(context.name());
        
        if (nameToComponent.containsKey(name)) {
            throw new DomainModelParseException(context.refToken, "Duplicate component '" + name + "'.");
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
            throw new DomainModelParseException(context.refToken, "Duplicate use case '" + name + "'.");
        }
        
        var useCase = new UseCase(name);
        this.knownUseCases.add(name);
        
        // Assign the use case to the current component
        this.builder.assignUseCase(useCase, this.currentComponent);
        
        return null;
    }
    
    @Override
    public Void visitServiceCandidateDeclaration(ServiceCandidateDeclarationContext arg0) {
        // TODO Auto-generated method stub
        return super.visitServiceCandidateDeclaration(arg0);
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
    
    static class DomainModelParseException extends RuntimeException {
        
        private static final long serialVersionUID = -3560623347221634775L;

        private static String formatMessage(Token token, String message) {
            return token.getLine() + "," + token.getCharPositionInLine() + ": " + message;
        }
        
        public DomainModelParseException(Token referenceToken, String message) {
            super(formatMessage(referenceToken, message));
        }
        
    }

}
