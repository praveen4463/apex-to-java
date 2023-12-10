lexer grammar Lexer;

// Keywords
GLOBAL : 'global';
CLASS : 'class';
GET : 'get';
SET : 'set';
RETURN : 'return';

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

// Operators
ASSIGN : '=';
ADD : '+';
COLON : ':';
GT : '>';
LT : '<';

// Generic types
LIST : 'List';
MAP : 'Map';

Types
  : 'String'
  | 'Integer'
  | 'Sobject'
  | 'Double'
  ;

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
  ;

// Ignore whitespace
WS
  : [ \t\r\n\u000C]+ -> skip
  ;