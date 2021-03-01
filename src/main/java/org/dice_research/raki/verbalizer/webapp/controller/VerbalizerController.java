package org.dice_research.raki.verbalizer.webapp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerHandler;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerResults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author rspeck
 *
 */
@RestController
public class VerbalizerController {

  protected static final Logger LOG = LogManager.getLogger(VerbalizerController.class);

  VerbalizerHandler v;

  // TODO: use: "Reader reader = new InputStreamReader(file.getInputStream());"?
  private String readStream(final InputStream is) throws IOException {
    final Scanner s = new Scanner(is);
    s.useDelimiter("\\A");
    final String result = s.hasNext() ? s.next() : "";
    is.close();
    s.close();
    return result;
  }

  /**
   *
   * @param axioms
   * @param ontology
   * @return
   */
  @PostMapping("/verbalize")
  public VerbalizerResults verbalize(//
      @RequestParam(value = "axioms") final MultipartFile axioms, //
      @RequestParam(value = "ontology") final MultipartFile ontology) {

    if (axioms.isEmpty() || ontology.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file sent.");
    }

    VerbalizerHandler vh = null;
    try {
      vh = new VerbalizerHandler(//
          readStream(axioms.getInputStream()), //
          readStream(ontology.getInputStream()) //
      );

      if (vh != null && vh.hasResults()) {
        return vh.getVerbalizerResults();
      }

    } catch (final IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Could not handle request.");
    }

    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
        "Could not handle request.");
  }
}
