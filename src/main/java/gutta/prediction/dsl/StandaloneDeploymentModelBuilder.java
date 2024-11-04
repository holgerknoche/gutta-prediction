package gutta.prediction.dsl;

import gutta.prediction.domain.DeploymentModel;

class StandaloneDeploymentModelBuilder extends DeploymentModelBuilder {
        
    public StandaloneDeploymentModelBuilder() {
        super(new DeploymentModel.Builder());
    }
            
}
