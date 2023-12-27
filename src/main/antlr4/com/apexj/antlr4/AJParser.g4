parser grammar AJParser;

options {
  tokenVocab=AJLexer;
}

compilationUnit
  : comment* classDec comment* EOF
  ;

comment
  : COMMENT
  ;

indexes
  : ( LBRAC expression RBRAC )+
  ;

identifierExpr
  : Identifier indexes? ( DOT identifierExpr )?
  ;

inheritedType
  : Identifier (DOT Identifier)* (LT primitivetypes GT)?
  ;

classDec
  : GLOBAL? CLASS Identifier (EXTENDS inheritedType)?
      ( IMPLEMENTS inheritedType (COMMA inheritedType)* )? block
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
  | enumDec
  ;

casting
  : LPAREN typeDec RPAREN
  ;

assignment
  : typeDec? Identifier (DOT Identifier)* ASSIGN casting? expression SEMICOLON?
  ;

enumDec
  : GLOBAL? ENUM Identifier blockStart Identifier (COMMA Identifier)* blockEnd
  ;

returnStm
  : RETURN expression SEMICOLON
  ;

listType
  : LIST LT typeDec GT
  ;

setType
  : SET_TYPE LT typeDec GT
  ;

mapType
  : MAP LT typeDec COMMA typeDec GT
  ;

primitivetypes
  : STRING
  | INTEGER
  | SOBJECT
  | DOUBLE
  | BOOLEAN
  ;

buildInTypes
  : DATE
  ;

listOfMapsType
  : LIST LT mapType GT
  ;

customTypes
  : Identifier
  ;

typeDec
  : mapType
  | listType
  | setType
  | listOfMapsType
  | primitivetypes
  | buildInTypes
  | customTypes
  ;

builtIntypesAllowingMethodInvocation
  : MAP
  | DATE
  | STRING
  ;

constructor
  : GLOBAL? Identifier argsList block
  ;

returnType
  : VOID
  | typeDec
  | Identifier (DOT Identifier)+
  ;

funcDec
  : GLOBAL? returnType Identifier argsList block
  ;

argsList
  : LPAREN ( returnType Identifier ( COMMA returnType Identifier )* )? RPAREN
  ;

functionInvocationConstruct
  : Identifier LPAREN expressionList? RPAREN indexes? ( DOT identifierExpr )?
  ;

functionInvocation
  : ( (Identifier | builtIntypesAllowingMethodInvocation) DOT )* functionInvocationConstruct SEMICOLON?
  ;

varDec
  : GLOBAL typeDec Identifier LBRACE GET SEMICOLON SET SEMICOLON RBRACE              #getterSetterVarDec
  | (GLOBAL | PRIVATE) FINAL? typeDec Identifier SEMICOLON                           #directVarDec
  | (GLOBAL | PRIVATE) STATIC? FINAL? typeDec Identifier ASSIGN expression SEMICOLON #varDecWithInitilization
  ;

generalTypeInitializerExpression
  : NEW (listType | mapType | setType | customTypes) LPAREN expressionList? RPAREN
  ;

soql
  : LBRAC .*? RBRAC
  ;

expression
  : expression ADD expression                                                               #addExpression
  | NumberLiteral                                                                           #numberExpression
  | StringLiteral                                                                           #stringExpression
  | functionInvocation                                                                      #functionInvocationExpression
  | generalTypeInitializerExpression                                                        #generalTypeInitializerExpressionLabel
  | generalTypeInitializerExpression (DOT functionInvocationConstruct)+                     #generalTypeInitializerAndMethodCallExpression
  | NEW (listType | setType) blockStart expression (COMMA expression)* blockEnd             #listTypeInitializePopulateExpression
  | NEW mapType blockStart expression MAP_KEY_VALUE_OP expression
      (COMMA expression MAP_KEY_VALUE_OP expression)* blockEnd                              #mapTypeInitializePopulateExpression
  | identifierExpr                                                                          #identifierExpression
  | soql                                                                                    #soqlExpression
  ;

expressionList
  : expression ( COMMA expression )*
  ;