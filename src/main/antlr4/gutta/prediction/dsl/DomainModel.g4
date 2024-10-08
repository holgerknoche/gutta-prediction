grammar DomainModel;

domainModel:
	domainModelElement*
	EOF
;

domainModelElement:
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
	refToken='local' name '->' name
;

remoteComponentConnectionDeclaration:
	refToken='remote' name '->' name '{'
		properties+=propertyDeclaration*
	'}';

useCaseDeclaration:
	refToken='UseCase' name
;

serviceCandidateDeclaration:
	refToken='ServiceCandidate' name '{'
		properties+=propertyDeclaration*
	'}'
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
