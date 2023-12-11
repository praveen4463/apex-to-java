package com.apexj;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class SampleTests {
  
  @Tag("complex-type1")
  @Test
  void complexType1() throws IOException {
    //Paths.get("resources/complex-type1.apex")
    API api = new API(Paths.get("resources/complex-type1.apex"), StandardCharsets.UTF_8);
    String javaCode = api.interpret();
    Files.writeString(Paths.get("resources/complex-type1.java"), javaCode, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }
}
