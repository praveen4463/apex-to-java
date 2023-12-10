parser grammar Parser;

options {
  tokenVocab=Lexer;
}

compilationUnit
  : comment* classDec comment* EOF
  ;

comment
  : COMMENT
  ;

classDec
  : GLOBAL? CLASS Identifier block
  ;

block
  : blockStart (statement | classDec | constructor | comment)* blockEnd
  ;

blockStart
  : LBRACE
  ;

blockEnd
  : RBRACE
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
  : LIST LT types GT
  ;

mapType
  : MAP LT types COMMA types GT
  ;

types
  : STRING
  | INTEGER
  | SOBJECT
  | DOUBLE
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
  | types
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