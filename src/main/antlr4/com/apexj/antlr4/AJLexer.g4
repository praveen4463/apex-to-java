lexer grammar AJLexer;

// Keywords
GLOBAL
  : 'global'
  | 'public'
  ;
PRIVATE : 'private';
CLASS : 'class';
GET : 'get;';
SET : 'set;';
RETURN : 'return';
NEW : 'new';
IMPLEMENTS : 'implements';
EXTENDS : 'extends';
VOID : 'void';
STATIC : 'static';
FINAL : 'final';
IF : 'if';
ELSE : 'else';
FOR : 'for';
TRY : 'try';
CATCH : 'catch';
FINALLY : 'finally';
UPDATE : 'update';
INSERT : 'insert';
INHERITED : 'inherited';
SHARING : 'sharing';
WITHOUT : 'without';
WITH : 'with';

// Separators
LBRACE : '{';
RBRACE : '}';
LPAREN : '(';
RPAREN : ')';
LBRAC : '[';
RBRAC : ']';
COMMA : ',';
DOT : '.';
SINGLE_QUOTE : '\'';
SEMICOLON : ';';
AT : '@';
COLON : ':';

// Operators
ASSIGN : '=';
ADD : '+';
GT : '>';
LT : '<';
MAP_KEY_VALUE_OP : '=>';
BANG : '!';
EQUAL : '==';
LE : '<=';
GE : '>=';
NOTEQUAL : '!=';
AND : '&&';
OR : '||';
INC : '++';
DEC : '--';
SUB : '-';
MUL : '*';
DIV : '/';
MOD : '%';

// Types
LIST
  : 'List'
  | 'list'
  ;
SET_TYPE : 'Set';
MAP : 'Map';
STRING
  : 'string'
  | 'String'
  ;
INTEGER
  : 'Integer'
  | 'integer'
  ;
SOBJECT
  : 'Sobject'
  | 'SObject'
  | 'object'
  ;
DOUBLE : 'Double';
DECIMAL : 'Decimal';
BOOLEAN
  : 'boolean'
  | 'Boolean'
  ;
DATE: 'Date';
HTTP_REQUEST : 'HttpRequest';
HTTP_RESPONSE : 'HttpResponse';
ENUM: 'enum';

NumberLiteral
  : Integer ( DOT DIGIT+ )?
  | DOT DIGIT+
  ;

fragment
Integer
  : [1-9] DIGIT*
  | '0'
  ;

fragment
DIGIT
  : [0-9]
  ;

StringLiteral
  : SINGLE_QUOTE StringCharacters_Single_Quote?? SINGLE_QUOTE
  ;

fragment
StringCharacters_Single_Quote
  : StringCharacter_Single_Quote+
  ;

// match any char other than quote, backslash (means no escape sequence) and new line
// match t, n, r, quote and backslash escape sequences
fragment
StringCharacter_Single_Quote
  : ~['\\\r\n]
  | '\\' [tnr'\\]
  ;

Identifier
  : [a-zA-Z_][0-9a-zA-Z_]*
  ;


COMMENT
  : '/*' .*? '*/'
  | '//' ~[\r\n]*
  ;

// Ignore whitespace
WS
  : [ \t\r\n\u000C]+ -> skip
  ;