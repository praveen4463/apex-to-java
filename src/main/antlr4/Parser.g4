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
  : LBRACE (statement | classDec)* RBRACE
  ;

listType
  : LIST LT Types GT
  ;

mapType
  : MAP LT Types COMMA Types GT
  ;

getterSetter
  : LBRACE GET COMMA SET COMMA RBRACE
  ;

typeDec
  : mapType
  | listType
  | Types
  ;

constructuer
  : GLOBAL? Identifier LPAREN

argsList
  : LPAREN typeDec Identifier ( COMMA typeDec Identifier )*
  ;
