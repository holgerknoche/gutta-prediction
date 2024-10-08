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
	name refToken='->' name '{'
	'}'
;

useCaseDeclaration:
	refToken='UseCase' name
;

serviceCandidateDeclaration:
	refToken='ServiceCandidate' name '{'
	'}'
;

name:
	ID | LITERAL_ID
;	

LITERAL_ID:
	'\''~[']*'\''
;

ID:
	[A-Za-z][A-Za-z0-9_]*
;
	
WS:
	[ \r\t\n]+ -> skip
;
