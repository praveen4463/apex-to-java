package com.apexj;

import com.apexj.antlr4.AJParserBaseVisitor;
import com.apexj.antlr4.AJParser;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultInterpreter extends AJParserBaseVisitor<String> {
  private final Set<String> imports = new HashSet<>();
  
  private final List<String> genParts = new ArrayList<>();
  
  public String getGeneratedCode() {
    StringBuilder sb = new StringBuilder();
    sb.append("package com.apexj;");
    sb.append("\n\n");
    sb.append(String.join("\n", imports));
    sb.append("\n");
    
    int indentLevel = 0;
    for (String part : genParts) {
      if (part.equals("{")) {
        indentLevel += 2;
        sb.append(" {");
        continue;
      } else if (part.equals("}")) {
        indentLevel -= 2;
        sb.append("\n");
        sb.append(" ".repeat(Math.max(0, indentLevel)));
        sb.append("}");
        continue;
      }
      sb.append("\n");
      sb.append(" ".repeat(Math.max(0, indentLevel)));
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
    classStm.append("class ").append(ctx.Identifier().getText());
    
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
    inheritedTypeBuilder.append(ctx.Identifier(0).getText());
    
    for (TerminalNode identifier : ctx.Identifier().subList(1, ctx.Identifier().size())) {
      inheritedTypeBuilder.append(".").append(identifier.getText());
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
      indexes.append(String.format(".get(%s)", visit(expr)));
    }
    return indexes.toString();
  }
  
  @Override
  public String visitIdentifierExpr(AJParser.IdentifierExprContext ctx) {
    String expr = ctx.Identifier().getText();
    
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
    genParts.add(ctx.getText());
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
    genParts.add(String.format("catch %s",
        ctx.argsList() != null ? visit(ctx.argsList()) : ""));
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
    forStm += String.format("(%1$s : %2$s)", visit(ctx.assignmentLS()), visit(ctx.expression()));
    genParts.add(forStm);
    visit(ctx.blockOrStatement());
    return null;
  }
  
  @Override
  public String visitCasting(AJParser.CastingContext ctx) {
    return String.format("(%s)", visit(ctx.typeDec()));
  }
  
  @Override
  public String visitAssignmentLS(AJParser.AssignmentLSContext ctx) {
    String asg = "";
    if (ctx.returnType() != null) {
      asg += visit(ctx.returnType());
      asg += " ";
    }
    asg += visit(ctx.identifierExpr());
    return asg;
  }
  
  @Override
  public String visitReturnType(AJParser.ReturnTypeContext ctx) {
    if (ctx.VOID() != null) {
      return "void";
    }
    return visit(ctx.typeDec());
  }
  
  @Override
  public String visitPrimitivetypes(AJParser.PrimitivetypesContext ctx) {
    if (ctx.SOBJECT() != null) {
      return "Object";
    } else if (ctx.STRING() != null) {
      return "String";
    } else if (ctx.INTEGER() != null) {
      return "Integer";
    } else if (ctx.DECIMAL() != null) {
      return "Double";
    }
    return ctx.getText();
  }
  
  @Override
  public String visitBuiltInTypes(AJParser.BuiltInTypesContext ctx) {
    if (ctx.DATE() != null) {
      imports.add("import java.util.Date;");
      return "Date";
    } else if (ctx.HTTP_REQUEST() != null) {
      imports.add("import java.net.http.HttpRequest;");
      return "HttpRequest";
    } else if (ctx.HTTP_RESPONSE() != null) {
      imports.add("import java.net.http.HttpResponse;");
      return "HttpResponse";
    }
    return ctx.getText();
  }
  
  @Override
  public String visitCustomTypes(AJParser.CustomTypesContext ctx) {
    
    StringBuilder customTypes = new StringBuilder(StringUtils.capitalize(ctx.Identifier(0).getText()));
    for (TerminalNode identifier : ctx.Identifier().subList(1, ctx.Identifier().size())) {
      customTypes.append(".").append(StringUtils.capitalize(identifier.getText()));
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
  public String visitBuiltInTypesAllowingMethodInvocation(AJParser.BuiltInTypesAllowingMethodInvocationContext ctx) {
    if (ctx.DATE() != null) {
      imports.add("import java.util.Date;");
      return "Date";
    } else if (ctx.MAP() != null) {
      imports.add("import java.util.Map;");
      return "Map";
    } else if (ctx.STRING() != null) {
      return "String";
    } else if (ctx.DECIMAL() != null) {
      return "Double";
    }
    return ctx.getText();
  }
  
  @Override
  public String visitArgsList(AJParser.ArgsListContext ctx) {
    if (ctx.returnType().isEmpty()) {
      return "()";
    }
    List<String> args = new ArrayList<>();
    for (int i = 0; i < ctx.returnType().size(); i++) {
      args.add(visit(ctx.returnType(i)) + " " + ctx.Identifier(i).getText());
    }
    return String.format("(%s)", String.join(", ", args));
  }
  
  @Override
  public String visitExpressionList(AJParser.ExpressionListContext ctx) {
    return ctx.expression().stream().map(this::visit).collect(Collectors.joining(", "));
  }
  
  @Override
  public String visitFunctionInvocationConstruct0(AJParser.FunctionInvocationConstruct0Context ctx) {
    return String.format("%1$s(%2$s)%3$s",
        ctx.Identifier().getText(),
        ctx.expressionList() != null ? visit(ctx.expressionList()) : "",
        ctx.indexes() != null ? visit(ctx.indexes()) : "");
  }
  
  @Override
  public String visitFunctionInvocationConstruct(AJParser.FunctionInvocationConstructContext ctx) {
    String funCont = ctx.functionInvocationConstruct0()
        .stream().map(this::visit).collect(Collectors.joining("."));
    if (ctx.identifierExpr() != null) {
      funCont += ".";
      funCont += visit(ctx.identifierExpr());
    }
    
    return funCont;
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
        "  " +
        String.format("public %3$s get%1$s() {return %2$s;}",
            capitalizedVarId,
            varId,
            type) +
        "\n\n" +
        "  " +
        String.format("public void set%1$s(%3$s %2$s) {this.%2$s = %2$s;}",
            capitalizedVarId,
            varId,
            type);
    genParts.add(genCode);
    return null;
  }
  
  @Override
  public String visitDirectVarDec(AJParser.DirectVarDecContext ctx) {
    String directVar = visit(ctx.globalOrPrivate());
    if (ctx.FINAL() != null) {
      directVar += " ";
      directVar += "final";
    }
    directVar += " ";
    directVar += visit(ctx.typeDec());
    directVar += " ";
    directVar += ctx.Identifier().getText();
    directVar += ";";
    genParts.add(directVar);
    return null;
  }
  
  @Override
  public String visitVarDecWithInitilization(AJParser.VarDecWithInitilizationContext ctx) {
    String varWithIni = visit(ctx.globalOrPrivate());
    if (ctx.STATIC() != null) {
      varWithIni += " ";
      varWithIni += "static";
    }
    if (ctx.FINAL() != null) {
      varWithIni += " ";
      varWithIni += "final";
    }
    varWithIni += " ";
    varWithIni += visit(ctx.typeDec());
    varWithIni += " ";
    varWithIni += ctx.Identifier().getText();
    varWithIni += " = ";
    varWithIni += visit(ctx.expression());
    varWithIni += ";";
    genParts.add(varWithIni);
    return null;
  }
  
  @Override
  public String visitGeneralTypeInitializerExpression(AJParser.GeneralTypeInitializerExpressionContext ctx) {
    String type = visit(ctx.typeDec());
    
    if (type.startsWith("List")) {
      imports.add("import java.util.ArrayList;");
      type = type.replace("List", "ArrayList");
    } else if (type.startsWith("Map")) {
      imports.add("import java.util.HashMap;");
      type = type.replace("Map", "HashMap");
    } else if (type.startsWith("Set")) {
      imports.add("import java.util.HashSet;");
      type = type.replace("Set", "HashSet");
    }
    return String.format("new %1$s(%2$s)",
        type,
        ctx.expressionList() != null ? visit(ctx.expressionList()) : "");
  }
  
  @Override
  public String visitFunctionInvocationObject(AJParser.FunctionInvocationObjectContext ctx) {
    if (ctx.Identifier() != null) {
      return ctx.Identifier().getText();
    }
    return visit(ctx.builtInTypesAllowingMethodInvocation());
  }
  
  private String processFunInvocation(boolean isExpression, AJParser.FunctionInvocationContext ctx) {
    
    
    String callerObject = ctx.functionInvocationObject() != null ? ctx.functionInvocationObject()
        .stream().map(this::visit).collect(Collectors.joining(".")) : "";
    
    String func = visit(ctx.functionInvocationConstruct());
    
    String funcInv = "";
    
    if (!callerObject.isEmpty()) {
      funcInv = callerObject + ".";
      
      // Try converting the function to java if it's a Java class and we can convert easily.
      // This is just an example. We will have a robust way to convert function later on.
      switch (callerObject) {
        case "Date":
          if (func.equals("today()")) {
            imports.add("import java.time.Instant;");
            func = "from(Instant.now())";
          }
          break;
        case "System":
          if (func.startsWith("debug")) {
            func = func.replace("debug", "out.println");
          }
          break;
        case "String":
          // Apex's String.join args sequence is just opposite to java's
          if (func.startsWith("join")) {
            AJParser.ExpressionListContext expressionListContext =
                ctx.functionInvocationConstruct().functionInvocationConstruct0(0).expressionList();
            func = String.format("join(%1$s, %2$s)",
                visit(expressionListContext.expression(1)),
                visit(expressionListContext.expression(0)));
          }
          break;
      }
    }
    
    funcInv += func;
    
    if (ctx.SEMICOLON() != null) {
      funcInv += ";";
    }
    
    if (isExpression) {
      return funcInv;
    }
    genParts.add(funcInv);
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
  public String visitSoqlExpression(AJParser.SoqlExpressionContext ctx) {
    String entireExp = ctx.getText();
    return String.format("\"%s\"",
        entireExp.substring(1, entireExp.length() - 2).replaceAll("\r\n\t", ""));
  }
  
  private String generalAssignment(AJParser.GeneralAssignmentContext ctx, boolean shouldReturn) {
    String genCode = String.format("%1$s %3$s= %4$s%2$s;",
        visit(ctx.assignmentLS()),
        visit(ctx.expression()),
        ctx.ADD() != null ? "+" : "",
        ctx.casting() != null ? visit(ctx.casting()) : "");
    
    if (shouldReturn) {
      return genCode;
    }
    genParts.add(genCode);
    return null;
  }
  
  @Override
  public String visitGeneralAssignment(AJParser.GeneralAssignmentContext ctx) {
    generalAssignment(ctx, false);
    return null;
  }
  
  @Override
  public String visitPropAssignmentDuringInitializationExpressionLabel(AJParser.PropAssignmentDuringInitializationExpressionLabelContext ctx) {
    String declaration = String.format("%1$s = %2$snew %3$s();",
        visit(ctx.assignmentLS()),
        ctx.casting() != null ? visit(ctx.casting()) : "",
        visit(ctx.typeDec()));
    genParts.add(declaration);
    String identifier = visit(ctx.assignmentLS().identifierExpr());
    for (AJParser.AssignmentContext assign : ctx.assignment()) {
      genParts.add(String.format("%1$s.%2$s",
          identifier,
          generalAssignment((AJParser.GeneralAssignmentContext)assign, true)));
    }
    return null;
  }
  
  @Override
  public String visitEnumDec(AJParser.EnumDecContext ctx) {
    StringBuilder enumStm = new StringBuilder();
    if (ctx.GLOBAL() != null) {
      enumStm.append("public");
    }
    enumStm.append(" ");
    enumStm.append("enum");
    enumStm.append(" ");
    enumStm.append(ctx.Identifier(0).getText());
    enumStm.append(" { ");
    ListIterator<TerminalNode> identifierIterator = ctx.Identifier().listIterator();
    identifierIterator.next(); // Skip the enum name
    while (identifierIterator.hasNext()) {
      TerminalNode identifier = identifierIterator.next();
      enumStm.append(identifier.getText());
      if (identifierIterator.hasNext()) {
        enumStm.append(", ");
      }
    }
    enumStm.append(" } ");
    genParts.add(enumStm.toString());
    return null;
  }
  
  @Override
  public String visitGlobalOrPrivate(AJParser.GlobalOrPrivateContext ctx) {
    if (ctx.GLOBAL() != null) {
      return "public";
    }
    return "private";
  }
  
  @Override
  public String visitFuncDec(AJParser.FuncDecContext ctx) {
    String funStm = visit(ctx.globalOrPrivate());
    
    if (ctx.STATIC() != null) {
      funStm += " ";
      funStm += "static";
    }
    
    funStm += " ";
    funStm += visit(ctx.returnType()) + " " + ctx.Identifier().toString() + visit((ctx.argsList()));
    
    genParts.add(funStm);
    
    visit(ctx.block());
    return null;
  }
  
  @Override
  public String visitReturnStm(AJParser.ReturnStmContext ctx) {
    String keyword = "";
    if (ctx.RETURN() != null) {
      keyword = "return";
    } else if (ctx.UPDATE() != null) {
      keyword = ctx.UPDATE().getText();
    } else if (ctx.INSERT() != null) {
      keyword = ctx.INSERT().getText();
    } else {
      throw new RuntimeException("Unrecognized keyword");
    }
    genParts.add(String.format("%1$s %2$s;", keyword, visit(ctx.expression())));
    return null;
  }
  
  @Override
  public String visitNumberExpression(AJParser.NumberExpressionContext ctx) {
    return ctx.getText();
  }
  
  @Override
  public String visitStringExpression(AJParser.StringExpressionContext ctx) {
    String text = ctx.getText();
    return String.format("\"%1$s\"", StringEscapeUtils.escapeJava(text.substring(1,
        text.length() - 1)));
  }
  
  @Override
  public String visitUnaryMinusExpression(AJParser.UnaryMinusExpressionContext ctx) {
    return "-" + visit(ctx.expression());
  }
  
  @Override
  public String visitNotExpression(AJParser.NotExpressionContext ctx) {
    return "!" + visit(ctx.expression());
  }
  
  @Override
  public String visitArithLogiExpression(AJParser.ArithLogiExpressionContext ctx) {
    return String.format("%1$s %2$s %3$s", visit(ctx.expression(0)),
        ctx.op.getText(), visit(ctx.expression(1)));
  }
  
  @Override
  public String visitGeneralTypeInitializerAndMethodCallExpression(AJParser.GeneralTypeInitializerAndMethodCallExpressionContext ctx) {
    return visit(ctx.generalTypeInitializerExpression()) + "." +
        ctx.functionInvocationConstruct().stream().map(this::visit).collect(Collectors.joining("."));
  }
  
  @Override
  public String visitListTypeInitializePopulateExpression(AJParser.ListTypeInitializePopulateExpressionContext ctx) {
    String code;
    if (ctx.listType() != null) {
      imports.add("import java.util.List;");
      code = "List.of";
    } else {
      imports.add("import java.util.Set;");
      code = "Set.of";
    }
    code += String.format("(%s)", ctx.expression()
        .stream().map(this::visit).collect(Collectors.joining(", ")));
    return code;
  }
  
  @Override
  public String visitMapKeyValue(AJParser.MapKeyValueContext ctx) {
    return visit(ctx.expression(0)) + ", " + visit(ctx.expression(1));
  }
  
  @Override
  public String visitMapTypeInitializePopulateExpression(AJParser.MapTypeInitializePopulateExpressionContext ctx) {
    imports.add("import java.util.Map;");
    return String.format("Map.of(%s)", ctx.mapKeyValue()
        .stream().map(this::visit).collect(Collectors.joining(", ")));
  }
  
  @Override
  public String visitArrayTypeInitializePopulateExpression(AJParser.ArrayTypeInitializePopulateExpressionContext ctx) {
    String type;
    if (ctx.primitivetypes() != null) {
      type = visit(ctx.primitivetypes());
    } else if (ctx.builtInTypes() != null) {
      type = visit(ctx.primitivetypes());
    } else if (ctx.customTypes() != null) {
      type = visit(ctx.customTypes());
    } else {
      throw new RuntimeException("Unrecognized type");
    }
    return String.format("new %1$s[]{%2$s}", type,
        ctx.expression().stream().map(this::visit).collect(Collectors.joining(", ")));
  }
}
