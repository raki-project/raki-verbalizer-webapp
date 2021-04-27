package org.dice_research.raki.verbalizer.webapp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.raki.verbalizer.webapp.controller.VerbalizerController;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerResults;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class ApplicationTests {
  private static final Logger LOG = LogManager.getLogger(ApplicationTests.class);

  @Test
  public void koalaTest() {
    try {
      final VerbalizerController ctlr = new VerbalizerController();

      final Path axiomsPath = Paths.get("koala.owl");
      final Path ontologyPath = Paths.get("koala.owl");

      final MultipartFile axioms = new MockMultipartFile(axiomsPath.toFile().getPath(),
          new FileInputStream(axiomsPath.toFile()));

      final MultipartFile ontology = new MockMultipartFile(ontologyPath.toFile().getPath(),
          new FileInputStream(ontologyPath.toFile()));

      final VerbalizerResults results = ctlr.rules(axioms, ontology);

      assertTrue(results.response.size() > 0);
      assertTrue(results.response.size() == 36);
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }
}
