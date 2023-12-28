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
  : GLOBAL? ((INHERITED | WITHOUT | WITH) SHARING)? STATIC? CLASS Identifier (EXTENDS inheritedType)?
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
  | tryStatement
  | ifStatement
  | forListStatement
  | increment
  | decrement
  | annotation
  ;

annotation
  : AT Identifier (LPAREN statement RPAREN)?
  ;

increment
  : identifierExpr INC
  ;

decrement
  : identifierExpr DEC
  ;

tryStatement
  : TRY blockOrStatement catchBlock
  | TRY blockOrStatement finallyBlock
  | TRY blockOrStatement catchBlock finallyBlock
  ;

catchBlock
  : CATCH argsList? blockOrStatement
  ;

finallyBlock
  : FINALLY blockOrStatement
  ;

ifStatement
  : ifBlock elseIfBlock* elseBlock?
  ;

ifBlock
  : IF LPAREN expression RPAREN blockOrStatement
  ;

elseIfBlock
  : ELSE IF LPAREN expression RPAREN blockOrStatement
  ;

elseBlock
  : ELSE blockOrStatement
  ;

forListStatement
  : FOR LPAREN assignmentLS COLON expression RPAREN blockOrStatement
  ;

blockOrStatement
  : block
  | statement
  ;

casting
  : LPAREN typeDec RPAREN
  ;

assignmentLS
  : returnType? identifierExpr
  ;

assignment
  : assignmentLS ADD? ASSIGN casting? expression SEMICOLON?
  ;

enumDec
  : GLOBAL? ENUM Identifier blockStart Identifier (COMMA Identifier)* blockEnd
  ;

returnStm
  : RETURN expression SEMICOLON
  | (UPDATE | INSERT) expression SEMICOLON
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
  | Identifier (DOT Identifier)+
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
  ;

funcDec
  : (GLOBAL | PRIVATE)? STATIC? returnType Identifier argsList block
  ;

argsList
  : LPAREN ( returnType Identifier ( COMMA returnType Identifier )* )? RPAREN
  ;

functionInvocationConstruct
  : Identifier LPAREN expressionList? RPAREN indexes?
      (DOT Identifier LPAREN expressionList? RPAREN indexes?)* ( DOT identifierExpr )?
  ;

functionInvocation
  : ( (Identifier | builtIntypesAllowingMethodInvocation) DOT )* functionInvocationConstruct SEMICOLON?
  ;

varDec
  : GLOBAL typeDec Identifier LBRACE GET SET RBRACE              #getterSetterVarDec
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
  : SUB expression                                                                          #unaryMinusExpression
  | BANG expression                                                                         #notExpression
  | increment                                                                               #incrementExpression
  | decrement                                                                               #decrementExpression
  | expression op=( MUL | DIV | MOD | ADD | SUB ) expression                                #arithExpression
  | expression op=( GE | LE | GT | LT ) expression                                          #compExpression
  | expression op=( EQUAL | NOTEQUAL ) expression                                           #eqExpression
  | expression AND expression                                                               #andExpression
  | expression OR expression                                                                #orExpression
  | NumberLiteral                                                                           #numberExpression
  | StringLiteral                                                                           #stringExpression
  | functionInvocation                                                                      #functionInvocationExpression
  | generalTypeInitializerExpression                                                        #generalTypeInitializerExpressionLabel
  | generalTypeInitializerExpression (DOT functionInvocationConstruct)+                     #generalTypeInitializerAndMethodCallExpression
  | NEW (listType | setType) blockStart expression (COMMA expression)* blockEnd             #listTypeInitializePopulateExpression
  | NEW mapType blockStart expression MAP_KEY_VALUE_OP expression
      (COMMA expression MAP_KEY_VALUE_OP expression)* blockEnd                              #mapTypeInitializePopulateExpression
  | NEW (primitivetypes | buildInTypes | customTypes) LBRAC RBRAC
      blockStart expression (COMMA expression)* blockEnd                                    #arrayTypeInitializePopulateExpression
  | identifierExpr                                                                          #identifierExpression
  | soql                                                                                    #soqlExpression
  ;

expressionList
  : expression ( COMMA expression )*
  ;