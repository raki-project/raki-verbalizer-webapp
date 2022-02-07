package org.dice_research.raki.verbalizer.webapp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.dice_research.raki.verbalizer.webapp.controller.VerbalizerController;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerResults;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class ApplicationTests {

  // private static final Logger LOG = LogManager.getLogger(ApplicationTests.class);

  final VerbalizerController ctlr = new VerbalizerController();

  String koala = "src/main/resources/static/ontology/koala.owl";

  /**
   * Rules test with 1 file for axioms and ontology parameter.
   *
   * @throws IOException
   * @throws FileNotFoundException
   */
  @Test
  public void rulesTest() throws FileNotFoundException, IOException {

    final Path axiomsPath = Paths.get(koala);
    final MultipartFile axioms = new MockMultipartFile(axiomsPath.toFile().getName(),
        new FileInputStream(axiomsPath.toFile()));

    final VerbalizerResults results = ctlr.rules(axioms, Optional.of(axioms), Optional.of(""));

    assertTrue(results.response.size() > 0);
    assertTrue(results.response.size() == 42);
  }
}
