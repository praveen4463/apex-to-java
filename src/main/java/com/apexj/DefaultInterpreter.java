package com.apexj;

import com.apexj.antlr4.AJParserBaseVisitor;
import com.apexj.antlr4.AJParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultInterpreter extends AJParserBaseVisitor<String> {
  private final Set<String> imports = new HashSet<>();
  
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
        sb.append("\n}");
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
  public String visitClassDec(AJParser.ClassDecContext ctx) {
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
  public String visitComment(AJParser.CommentContext ctx) {
    genParts.add(ctx.COMMENT().toString());
    return null;
  }
  
  @Override
  public String visitBlockStart(AJParser.BlockStartContext ctx) {
    genParts.add("{");
    return null;
  }
  
  @Override
  public String visitBlockEnd(AJParser.BlockEndContext ctx) {
    genParts.add("}");
    return null;
  }
  
  @Override
  public String visitTypes(AJParser.TypesContext ctx) {
    if (ctx.SOBJECT() != null) {
      return "Object";
    }
    return ctx.getText();
  }
  
  @Override
  public String visitListType(AJParser.ListTypeContext ctx) {
    imports.add("import java.util.List;");
    return String.format("List<%s>", visit(ctx.types()));
  }
  
  @Override
  public String visitMapType(AJParser.MapTypeContext ctx) {
    imports.add("import java.util.Map;");
    return String.format("Map<%s, %s>", visit(ctx.types(0)), visit(ctx.types(1)));
  }
  
  @Override
  public String visitListOfMapsType(AJParser.ListOfMapsTypeContext ctx) {
    imports.add("import java.util.List;");
    return String.format("List<%s>", visit(ctx.mapType()));
  }
  
  @Override
  public String visitArgsList(AJParser.ArgsListContext ctx) {
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
  public String visitConstructor(AJParser.ConstructorContext ctx) {
    String ctrStm = "";
    if (ctx.GLOBAL() != null) {
      ctrStm = "public ";
    }
    ctrStm += ctx.Identifier().toString() + visit((ctx.argsList()));
    
    genParts.add(ctrStm);
    
    visit(ctx.block());
    return null;
  }
  
  @Override
  public String visitGetterSetterVarDec(AJParser.GetterSetterVarDecContext ctx) {
    String type = visit(ctx.typeDec());
    String varId = ctx.Identifier().toString();
    String capitalizedVarId = varId.substring(0, 1).toUpperCase() + varId.substring(1);
    String genCode = String.format("private %1$s %2$s;", type, varId) +
        "\n\n" +
        String.format("public %3$s get%1$s() {return %2$s;}",
            capitalizedVarId,
            varId,
            type) +
        "\n\n" +
        String.format("public void set%1$s(%3$s %2$s) {this.%2$s = %2$s;}",
            capitalizedVarId,
            varId,
            type);
    genParts.add(genCode);
    return null;
  }
  
  @Override
  public String visitPrivateVarDec(AJParser.PrivateVarDecContext ctx) {
    String type = visit(ctx.typeDec());
    String varId = ctx.Identifier().toString();
    genParts.add(String.format("private %1$s %2$s;", type, varId));
    return null;
  }
  
  @Override
  public String visitAddExpression(AJParser.AddExpressionContext ctx) {
    AJParser.ExpressionContext left = ctx.expression(0);
    AJParser.ExpressionContext right = ctx.expression(1);
    return left.getText() + " + " + right.getText();
  }
  
  private String processFunInvocation(boolean isExpression, AJParser.FunctionInvocationContext ctx) {
    String genCode;
    if (ctx.Identifier().size() > 1) {
      genCode = ctx.Identifier(0).toString() + "." + ctx.Identifier(1).toString();
    } else {
      genCode = ctx.Identifier(0).toString();
    }
    String params = "";
    if (ctx.expressionList() != null) {
      params = ctx.expressionList().getText();
    }
    genCode += String.format("(%1$s);", params);
    if (isExpression) {
      return genCode;
    }
    genParts.add(genCode);
    return null;
  }
  
  @Override
  public String visitFunctionInvocationExpression(AJParser.FunctionInvocationExpressionContext ctx) {
    return processFunInvocation(true, ctx.functionInvocation());
  }
  
  @Override
  public String visitFunctionInvocation(AJParser.FunctionInvocationContext ctx) {
    return processFunInvocation(false, ctx);
  }
  
  @Override
  public String visitTypeInitializerExpression(AJParser.TypeInitializerExpressionContext ctx) {
    String type;
    if (ctx.listType() != null) {
      type = visit(ctx.listType());
    } else {
      type = visit(ctx.mapType());
    }
    return String.format("new %1$s()", type);
  }
  
  @Override
  public String visitSoqlExpression(AJParser.SoqlExpressionContext ctx) {
    String entireExp = ctx.getText();
    return entireExp.substring(1, entireExp.length() - 2);
  }
  
  @Override
  public String visitAssignment(AJParser.AssignmentContext ctx) {
    String genCode = "";
    if (ctx.typeDec() != null) {
      genCode = visit(ctx.typeDec()) + " ";
    }
    genCode += String.format("%1$s = %2$s;", ctx.Identifier(), visit(ctx.expression()));
    genParts.add(genCode);
    return null;
  }
  
  @Override
  public String visitFuncDec(AJParser.FuncDecContext ctx) {
    String funStm = "";
    if (ctx.GLOBAL() != null) {
      funStm = "public ";
    }
    funStm += visit(ctx.typeDec()) + " " + ctx.Identifier().toString() + visit((ctx.argsList()));
    
    genParts.add(funStm);
    
    visit(ctx.block());
    return null;
  }
  
  @Override
  public String visitReturnStm(AJParser.ReturnStmContext ctx) {
    String soqlNormalized = visit(ctx.expression()).replaceAll("\n", "");
    genParts.add(String.format("return \"%1$s\";", soqlNormalized));
    return null;
  }
  
  @Override
  public String visitNumberExpression(AJParser.NumberExpressionContext ctx) {
    return ctx.getText();
  }
  
  @Override
  public String visitStringExpression(AJParser.StringExpressionContext ctx) {
    return String.format("\"%1$s\"", ctx.getText().substring(1, ctx.getText().length() - 2));
  }
  
  @Override
  public String visitIdentifierExpression(AJParser.IdentifierExpressionContext ctx) {
    return ctx.getText();
  }
}
