parser grammar Parser;

options {
  tokenVocab=Lexer;
}

compilationUnit
  : COMMENT* classDec EOF
  ;

classDec
  : GLOBAL? CLASS Identifier block
  ;

block
  : LBRACE (statement | classDec | constructor)* RBRACE
  ;

statement
  : varDec
  | assignment
  | funcDec
  | functionInvocation
  | returnStm
  ;

assignment
  : typeDec? Identifier ASSIGN expression SEMICOLON
  ;

returnStm
  : RETURN expression SEMICOLON
  ;

listType
  : LIST LT Types GT
  ;

mapType
  : MAP LT Types COMMA Types GT
  ;

listOfMapsType
  : LIST LT mapType GT
  ;

getterSetter
  : LBRACE GET SEMICOLON SET SEMICOLON RBRACE
  ;

typeDec
  : mapType
  | listType
  | listOfMapsType
  | Types
  ;

constructor
  : GLOBAL? Identifier argsList block
  ;

funcDec
  : GLOBAL? typeDec Identifier argsList block
  ;

argsList
  : LPAREN ( typeDec Identifier ( COMMA typeDec Identifier )* )? RPAREN
  ;

functionInvocation
  : ( Identifier DOT )? Identifier LPAREN expressionList? RPAREN SEMICOLON
  ;

varDec
  : GLOBAL? typeDec Identifier getterSetter
  | GLOBAL? typeDec Identifier SEMICOLON
  ;

expression
  : expression ADD expression                           #addExpression
  | NumberLiteral                                       #numberExpression
  | StringLiteral                                       #stringExpression
  | functionInvocation                                  #functionInvocationExpression
  | NEW (listType | mapType) LPAREN RPAREN              #typeInitializerExpression
  | SOQL                                                #soqlExpression
  | Identifier                                          #identifierExpression
  ;

expressionList
  : expression ( COMMA expression )*
  ;