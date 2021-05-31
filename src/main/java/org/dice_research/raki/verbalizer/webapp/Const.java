package org.dice_research.raki.verbalizer.webapp;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Const {

  // folder to store files
  public static final Path tmp;

  static {
    tmp = Paths.get(System.getProperty("java.io.tmpdir")//
        .concat(File.separator)//
        .concat("raki"));

    if (!tmp.toFile().exists()) {
      tmp.toFile().mkdirs();
      tmp.toFile().deleteOnExit();
    }
  }

  public static String DRILL = "http://drill:9080/concept_learning";
  // public static String VERB = "http://127.0.0.1:9081/verbalize";
}
