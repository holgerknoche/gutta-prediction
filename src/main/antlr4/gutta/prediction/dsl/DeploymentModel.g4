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
	refToken='Component' name '{'
		elements+=componentDeclarationElement*
	'}'
;

componentDeclarationElement:
	useCaseDeclaration |
	serviceCandidateDeclaration
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
	refToken='UseCase' name
;

serviceCandidateDeclaration:
	refToken='ServiceCandidate' name properties=propertiesDeclaration?
;

dataStoreDeclaration:
	refToken='DataStore' name properties=propertiesDeclaration?
	'{'
		elements+=entityTypeDeclaration*
	'}'
;

entityTypeDeclaration:
	refToken='EntityType' name
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
