package com.apexj;

import com.apexj.antlr4.AJLexer;
import com.apexj.antlr4.AJParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

public final class API {
  
  private final AJLexer lexer;
  
  public API(Path file, Charset charset) throws IOException {
    lexer = new AJLexer(CharStreams.fromPath(file));
  }
  
  public String interpret() {
    DefaultInterpreter interpreter = new DefaultInterpreter();
    AJParser parser = new AJParser(new CommonTokenStream(lexer));
    parser.setBuildParseTree(true);
    interpreter.visit(parser.compilationUnit());
    return interpreter.getGeneratedCode();
  }
}
