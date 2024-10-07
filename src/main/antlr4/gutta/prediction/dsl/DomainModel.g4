grammar DomainModel;

domainModel:
	elements+=domainModelElement*
	EOF
;

domainModelElement:
	useCaseDeclaration |
	serviceCandidateDeclaration
;	

useCaseDeclaration:
	'UseCase' name
;

serviceCandidateDeclaration:
	'ServiceCandidate' name '{'
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
