package com.apexj;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class SampleTests {
  
  @Tag("complex-type2")
  @Test
  void complexType2() throws IOException {
    String homeDir = System.getProperty("user.home");
    API api = new API(Paths.get(homeDir + "/complex-type2.apex"), StandardCharsets.UTF_8);
    String javaCode = api.interpret();
    Files.writeString(Paths.get(homeDir + "/complex-type2.java"), javaCode, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }
}
