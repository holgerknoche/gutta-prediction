grammar DeploymentModel;

deploymentModel:
	deploymentModelElement*
	EOF
;

deploymentModelElement:
	componentDeclaration |
	componentConnectionDeclaration |
	dataStoreDeclaration
	
;	

componentDeclaration:
	refToken='component' name '{'
		elements+=componentDeclarationElement*
	'}'
;

componentDeclarationElement:
	useCaseDeclaration |
	serviceCandidateDeclaration |
	entityTypeDeclaration
;

componentConnectionDeclaration:
	localComponentConnectionDeclaration |
	remoteComponentConnectionDeclaration
;

localComponentConnectionDeclaration:
	refToken='local' source=name '->' target=name
;

remoteComponentConnectionDeclaration:
	asymmetric='asymmetric'? refToken='remote' source=name '->' target=name properties=propertiesDeclaration
;

useCaseDeclaration:
	refToken='useCase' name
;

serviceCandidateDeclaration:
	refToken='serviceCandidate' name properties=propertiesDeclaration?
;

dataStoreDeclaration:
	refToken='dataStore' name properties=propertiesDeclaration?
	'{'
		elements+=entityTypeReference*
	'}'
;

entityTypeDeclaration:
	refToken='entityType' name
;

entityTypeReference:
	refToken='entityType' name
;

propertiesDeclaration:
	'[' properties+=propertyDeclaration* ']'
;

propertyDeclaration:
	name '=' propertyValue
;

propertyValue:
	ID | STRING_LITERAL | INT_LITERAL
;

name:
	ID | STRING_LITERAL
;

ID:
	[A-Za-z][A-Za-z0-9_]*
;

INT_LITERAL:
	[0-9]+
;

STRING_LITERAL:
	'"'~["]*'"'
;
	
WS:
	[ \r\t\n]+ -> skip
;
