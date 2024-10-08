grammar DeploymentModel;

deploymentModel:
	deploymentModelElement*
	EOF
;

deploymentModelElement:
	componentDeclaration |
	componentConnectionDeclaration
	
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
	symmetric='symmetric'? refToken='remote' source=name '->' target=name properties=propertiesDeclaration
;

useCaseDeclaration:
	refToken='UseCase' name
;

serviceCandidateDeclaration:
	refToken='ServiceCandidate' name properties=propertiesDeclaration
;

propertiesDeclaration:
	'{' properties+=propertyDeclaration* '}'
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
