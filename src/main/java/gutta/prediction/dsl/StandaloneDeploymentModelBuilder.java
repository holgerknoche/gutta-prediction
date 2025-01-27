package gutta.prediction.dsl;

import gutta.prediction.domain.DeploymentModel;

/**
 * A {@link StandaloneDeploymentModelBuilder} is a special deployment model builder to process fully specified (standalone) models, as opposed to partially
 * specified (delta) models.
 */
class StandaloneDeploymentModelBuilder extends DeploymentModelBuilder {

    public StandaloneDeploymentModelBuilder() {
        super(new DeploymentModel.Builder());
    }

}
