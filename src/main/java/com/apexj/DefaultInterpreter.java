package com.apexj;

import com.apexj.antlr4.Parser;
import com.apexj.antlr4.ParserBaseVisitor;

import java.util.List;

public class DefaultInterpreter extends ParserBaseVisitor<String> {
  
  private final String apexCode;
  
  private String mainClass;
  private List<String> imports;
  
  private List<String> statements;
  
  public DefaultInterpreter(String apexCode) {
    this.apexCode = apexCode;
  }
  
  public String getGeneratedCode() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.join("\n", imports));
    sb.append("\n");
    sb.append(mainClass);
    sb.append(" {\n");
    sb.append(String.join("\n", statements.stream().map(s -> "  " + s).toList()));
    sb.append("\n");
    sb.append("}\n");
    return sb.toString();
  }
  
  @Override
  public String visitCompilationUnit(Parser.CompilationUnitContext ctx) {
    mainClass = "";
    if (ctx.COMMENT() != null) {
      mainClass = ctx.COMMENT().toString(); // Apex and java multiline comments have same syntax
    }
    // Any class level comments and class statement itself
    mainClass += visit(ctx.classDec());
    return null;
  }
  
  @Override
  public String visitClassDec(Parser.ClassDecContext ctx) {
    String classStm = "";
    if (ctx.GLOBAL() != null) {
      classStm = "public ";
    }
    classStm += "class " + ctx.Identifier().toString();
    return classStm;
  }
}
