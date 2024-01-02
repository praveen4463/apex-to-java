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
  : Identifier (DOT Identifier)* (LT typeDec GT)?
  ;

classDec
  : GLOBAL? (shareType=(INHERITED | WITHOUT | WITH) SHARING)? STATIC? CLASS Identifier (EXTENDS inheritedType)?
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
  : AT Identifier (LPAREN .*? RPAREN)?
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
  : ELSE ifBlock
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
  : assignmentLS ADD? ASSIGN casting? expression SEMICOLON? #generalAssignment
  | assignmentLS ASSIGN casting?
      NEW typeDec LPAREN assignment (COMMA assignment )* RPAREN SEMICOLON? #propAssignmentDuringInitializationExpressionLabel
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
  | DECIMAL
  ;

builtInTypes
  : DATE
  | HTTP_REQUEST
  | HTTP_RESPONSE
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
  | builtInTypes
  | customTypes
  ;

builtInTypesAllowingMethodInvocation
  : MAP
  | DATE
  | STRING
  | DOUBLE
  | DECIMAL
  ;

constructor
  : GLOBAL? Identifier argsList block
  ;

returnType
  : VOID
  | typeDec
  ;

globalOrPrivate
  : GLOBAL
  | PRIVATE
  ;

funcDec
  : globalOrPrivate? STATIC? returnType Identifier argsList block
  ;

argsList
  : LPAREN ( returnType Identifier ( COMMA returnType Identifier )* )? RPAREN
  ;

functionInvocationConstruct0
  : Identifier LPAREN expressionList? RPAREN indexes?
  ;

functionInvocationConstruct
  : functionInvocationConstruct0 (DOT functionInvocationConstruct0)* ( DOT identifierExpr )?
  ;

functionInvocationObject
  : Identifier
  | builtInTypesAllowingMethodInvocation
  ;

functionInvocation
  : ( functionInvocationObject DOT )* functionInvocationConstruct SEMICOLON?
  ;

varDec
  : GLOBAL typeDec Identifier LBRACE GET SET RBRACE                               #getterSetterVarDec
  | globalOrPrivate FINAL? typeDec Identifier SEMICOLON                           #directVarDec
  | globalOrPrivate STATIC? FINAL? typeDec Identifier ASSIGN expression SEMICOLON #varDecWithInitilization
  ;

generalTypeInitializerExpression
  : NEW typeDec LPAREN expressionList? RPAREN
  ;

soql
  : LBRAC .*? RBRAC
  ;

mapKeyValue
  : expression MAP_KEY_VALUE_OP expression
  ;

expression
  : SUB expression                                                                          #unaryMinusExpression
  | BANG expression                                                                         #notExpression
  | increment                                                                               #incrementExpression
  | decrement                                                                               #decrementExpression
  | expression
      op=( MUL | DIV | MOD | ADD | SUB | GE | LE | GT | LT | EQUAL | NOTEQUAL | AND | OR)
      expression                                                                            #arithLogiExpression
  | NumberLiteral                                                                           #numberExpression
  | StringLiteral                                                                           #stringExpression
  | functionInvocation                                                                      #functionInvocationExpression
  | generalTypeInitializerExpression                                                        #generalTypeInitializerExpressionLabel
  | generalTypeInitializerExpression (DOT functionInvocationConstruct)+                     #generalTypeInitializerAndMethodCallExpression
  | NEW (listType | setType) blockStart expression (COMMA expression)* blockEnd             #listTypeInitializePopulateExpression
  | NEW mapType blockStart mapKeyValue (COMMA mapKeyValue)* blockEnd                        #mapTypeInitializePopulateExpression
  | NEW (primitivetypes | builtInTypes | customTypes) LBRAC RBRAC
      blockStart expression (COMMA expression)* blockEnd                                    #arrayTypeInitializePopulateExpression
  | identifierExpr                                                                          #identifierExpression
  | soql                                                                                    #soqlExpression
  ;

expressionList
  : expression ( COMMA expression )*
  ;