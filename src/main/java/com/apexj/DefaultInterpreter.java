package com.apexj;

import com.apexj.antlr4.AJParserBaseVisitor;
import com.apexj.antlr4.AJParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

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
    StringBuilder classStm = new StringBuilder();
    if (ctx.GLOBAL() != null) {
      classStm.append("public");
    }
    
    if (ctx.shareType != null) {
      // TODO: No action currently on shareType until we find (or even need) a suitable java equivalent
    }
    
    if (ctx.STATIC() != null) {
      classStm.append(" ");
      classStm.append("static");
    }
    
    classStm.append(" ");
    classStm.append("class ").append(ctx.Identifier().toString());
    
    if (ctx.EXTENDS() != null) {
      classStm.append(" ");
      classStm.append("extends ").append(visit(ctx.inheritedType(0)));
    }
    
    if (ctx.IMPLEMENTS() != null) {
      classStm.append(" ");
      classStm.append("implements");
      
      ListIterator<AJParser.InheritedTypeContext> inheritedTypeContextIterator =
          ctx.inheritedType().listIterator();
      if (ctx.EXTENDS() != null) {
        inheritedTypeContextIterator.next();
      }
      while (inheritedTypeContextIterator.hasNext()) {
        AJParser.InheritedTypeContext inheritedTypeContext = inheritedTypeContextIterator.next();
        classStm.append(" ").append(visit(inheritedTypeContext));
        if (inheritedTypeContextIterator.hasNext()) {
          classStm.append(",");
        }
      }
    }
    
    genParts.add(classStm.toString());
    
    visit(ctx.block());
    return null;
  }
  
  @Override
  public String visitInheritedType(AJParser.InheritedTypeContext ctx) {
    StringBuilder inheritedTypeBuilder = new StringBuilder();
    inheritedTypeBuilder.append(ctx.Identifier(0));
    
    for (TerminalNode identifier : ctx.Identifier().subList(1, ctx.Identifier().size())) {
      inheritedTypeBuilder.append(".").append(identifier);
    }
    
    if (ctx.GT() != null) {
      inheritedTypeBuilder.append(String.format("<%s>", visit(ctx.typeDec())));
    }
    
    return inheritedTypeBuilder.toString();
  }
  
  @Override
  public String visitComment(AJParser.CommentContext ctx) {
    genParts.add(ctx.COMMENT().toString());
    return null;
  }
  
  @Override
  public String visitIndexes(AJParser.IndexesContext ctx) {
    StringBuilder indexes = new StringBuilder();
    for (AJParser.ExpressionContext expr : ctx.expression()) {
      indexes.append(String.format("[%s]", visit(expr)));
    }
    return indexes.toString();
  }
  
  @Override
  public String visitIdentifierExpr(AJParser.IdentifierExprContext ctx) {
    String expr = ctx.Identifier().toString();
    
    if (ctx.indexes() != null) {
      expr += visit(ctx.indexes());
    }
    
    if (ctx.identifierExpr() != null) {
      expr += "." + visit(ctx.identifierExpr());
    }
    
    return expr;
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
  public String visitAnnotation(AJParser.AnnotationContext ctx) {
    String annotation = "@" + ctx.Identifier();
    if (ctx.statement() != null) {
      annotation += String.format("(%s)", visit(ctx.statement()));
    }
    genParts.add(annotation);
    return null;
  }
  
  private String stmOrExpr(String code, boolean isExpr) {
    if (isExpr) {
      return code;
    }
    genParts.add(code);
    return null;
  }
  
  private String increment(AJParser.IncrementContext ctx, boolean isExpr) {
    return stmOrExpr(visit(ctx.identifierExpr()) + "++", isExpr);
  }
  
  @Override
  public String visitIncrement(AJParser.IncrementContext ctx) {
    return increment(ctx, false);
  }
  
  @Override
  public String visitIncrementExpression(AJParser.IncrementExpressionContext ctx) {
    return increment(ctx.increment(), true);
  }
  
  private String decrement(AJParser.DecrementContext ctx, boolean isExpr) {
    return stmOrExpr(visit(ctx.identifierExpr()) + "--", isExpr);
  }
  
  @Override
  public String visitDecrement(AJParser.DecrementContext ctx) {
    return decrement(ctx, false);
  }
  
  @Override
  public String visitDecrementExpression(AJParser.DecrementExpressionContext ctx) {
    return decrement(ctx.decrement(), true);
  }
  
  @Override
  public String visitTryStatement(AJParser.TryStatementContext ctx) {
    genParts.add("try");
    visit(ctx.blockOrStatement());
    if (ctx.catchBlock() != null) {
      visit(ctx.catchBlock());
    }
    if (ctx.finallyBlock() != null) {
      visit(ctx.finallyBlock());
    }
    return null;
  }
  
  @Override
  public String visitCatchBlock(AJParser.CatchBlockContext ctx) {
    genParts.add("catch");
    if (ctx.argsList() != null) {
      visit(ctx.argsList());
    }
    visit(ctx.blockOrStatement());
    return null;
  }
  
  @Override
  public String visitFinallyBlock(AJParser.FinallyBlockContext ctx) {
    genParts.add("finally");
    visit(ctx.blockOrStatement());
    return null;
  }
  
  @Override
  public String visitIfBlock(AJParser.IfBlockContext ctx) {
    String ifBlock = "if";
    ifBlock += " ";
    ifBlock += String.format("(%s)", visit(ctx.expression()));
    genParts.add(ifBlock);
    visit(ctx.blockOrStatement());
    return null;
  }
  
  @Override
  public String visitElseIfBlock(AJParser.ElseIfBlockContext ctx) {
    genParts.add("else ");
    visit(ctx.ifBlock());
    return null;
  }
  
  @Override
  public String visitElseBlock(AJParser.ElseBlockContext ctx) {
    genParts.add("else");
    visit(ctx.blockOrStatement());
    return null;
  }
  
  @Override
  public String visitForListStatement(AJParser.ForListStatementContext ctx) {
    String forStm = "for ";
    forStm += String.format("(%1$s : %2$s),", visit(ctx.assignmentLS()), visit(ctx.expression()));
    genParts.add(forStm);
    visit(ctx.blockOrStatement());
    return null;
  }
  
  @Override
  public String visitAssignmentLS(AJParser.AssignmentLSContext ctx) {
    String asg = "";
    if (ctx.returnType() != null) {
      asg += visit(ctx.returnType());
    }
    asg += visit(ctx.identifierExpr());
    return asg;
  }
  
  @Override
  public String visitReturnType(AJParser.ReturnTypeContext ctx) {
    if (ctx.VOID() != null) {
      return "void";
    }
    visit(ctx.typeDec());
    return null;
  }
  
  @Override
  public String visitPrimitivetypes(AJParser.PrimitivetypesContext ctx) {
    if (ctx.SOBJECT() != null) {
      return "Object";
    } else if (ctx.STRING() != null) {
      return "String";
    } else if (ctx.INTEGER() != null) {
      return "Integer";
    }
    return ctx.getText();
  }
  
  @Override
  public String visitBuildInTypes(AJParser.BuildInTypesContext ctx) {
    return ctx.getText();
  }
  
  @Override
  public String visitCustomTypes(AJParser.CustomTypesContext ctx) {
    StringBuilder customTypes = new StringBuilder(ctx.Identifier(0).toString());
    for (TerminalNode identifier : ctx.Identifier().subList(1, ctx.Identifier().size())) {
      customTypes.append(".").append(identifier);
    }
    return customTypes.toString();
  }
  
  @Override
  public String visitListType(AJParser.ListTypeContext ctx) {
    imports.add("import java.util.List;");
    return String.format("List<%s>", visit(ctx.typeDec()));
  }
  
  @Override
  public String visitSetType(AJParser.SetTypeContext ctx) {
    imports.add("import java.util.Set;");
    return String.format("Set<%s>", visit(ctx.typeDec()));
  }
  
  @Override
  public String visitMapType(AJParser.MapTypeContext ctx) {
    imports.add("import java.util.Map;");
    return String.format("Map<%s, %s>", visit(ctx.typeDec(0)), visit(ctx.typeDec(1)));
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
