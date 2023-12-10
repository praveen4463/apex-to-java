package com.apexj;

import com.apexj.antlr4.Parser;
import com.apexj.antlr4.ParserBaseVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DefaultInterpreter extends ParserBaseVisitor<String> {
  private Set<String> imports;
  
  private final List<String> genParts = new ArrayList<>();
  
  public String getGeneratedCode() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.join("\n", imports));
    
    boolean indent = false;
    for (String part : genParts) {
      if (part.equals("{")) {
        indent = true;
        sb.append(" {");
        continue;
      } else if (part.equals("}")) {
        indent = false;
        sb.append("}");
        continue;
      }
      sb.append("\n");
      if (indent) {
        sb.append("  ");
      }
      sb.append(part);
    }
    sb.append("\n");
    return sb.toString();
  }
  
  @Override
  public String visitClassDec(Parser.ClassDecContext ctx) {
    String classStm = "";
    if (ctx.GLOBAL() != null) {
      classStm = "public ";
    }
    classStm += "class " + ctx.Identifier().toString();
    
    genParts.add(classStm);
    
    visit(ctx.block());
    return null;
  }
  
  @Override
  public String visitComment(Parser.CommentContext ctx) {
    genParts.add(ctx.COMMENT().toString());
    return null;
  }
  
  @Override
  public String visitBlockStart(Parser.BlockStartContext ctx) {
    genParts.add("{");
    return null;
  }
  
  @Override
  public String visitBlockEnd(Parser.BlockEndContext ctx) {
    genParts.add("}");
    return null;
  }
  
  @Override
  public String visitTypes(Parser.TypesContext ctx) {
    String type = ctx.getText();
    if (type.equals(ctx.SOBJECT().toString())) {
      return "Object";
    }
    return type;
  }
  
  @Override
  public String visitListType(Parser.ListTypeContext ctx) {
    imports.add("import java.util.List;");
    return String.format("List<%s>", visit(ctx.types()));
  }
  
  @Override
  public String visitMapType(Parser.MapTypeContext ctx) {
    imports.add("import java.util.Map;");
    return String.format("Map<%s, %s>", visit(ctx.types(0)), visit(ctx.types(1)));
  }
  
  @Override
  public String visitListOfMapsType(Parser.ListOfMapsTypeContext ctx) {
    imports.add("import java.util.List;");
    return String.format("List<%s>", visit(ctx.mapType()));
  }
  
  @Override
  public String visitArgsList(Parser.ArgsListContext ctx) {
    if (ctx.typeDec().isEmpty()) {
      return "()";
    }
    List<String> args = new ArrayList<>();
    for (int i = 0; i < ctx.typeDec().size(); i++) {
      args.add(visit(ctx.typeDec(i)) + " " + ctx.Identifier(i));
    }
    return String.format("(%s)", String.join(", ", args));
  }
  
  @Override
  public String visitConstructor(Parser.ConstructorContext ctx) {
    String ctrStm = "";
    if (ctx.GLOBAL() != null) {
      ctrStm = "public ";
    }
    ctrStm += ctx.Identifier().toString() + visit((ctx.argsList()));
    
    genParts.add(ctrStm);
    
    visit(ctx.block());
    return null;
  }
}
