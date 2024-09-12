package gutta.prediction.domain;

import java.util.Map;
import java.util.Optional;

public class MethodToComponentAllocation {
	
	private final Map<String, Component> methodToComponentMap;
	
	public MethodToComponentAllocation(Map<String, Component> methodToComponentMap) {
		this.methodToComponentMap = methodToComponentMap;
	}
	
	public Optional<Component> resolveComponentForService(String serviceName) {
		return Optional.ofNullable(this.methodToComponentMap.get(serviceName));
	}

}
