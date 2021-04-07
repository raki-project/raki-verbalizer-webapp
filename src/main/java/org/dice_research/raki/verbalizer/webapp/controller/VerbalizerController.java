package org.dice_research.raki.verbalizer.webapp.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.raki.verbalizer.webapp.ServiceApp;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerHandler;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerResults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class VerbalizerController {

  protected static final Logger LOG = LogManager.getLogger(VerbalizerController.class);

  @PostMapping("/rules")
  public void rules() {

  }

  @PostMapping("/trained")
  public void trained() {

  }

  @PostMapping("/verbalize")
  public VerbalizerResults verbalize(//
      @RequestParam(value = "axioms") final MultipartFile axioms, //
      @RequestParam(value = "ontology") final MultipartFile ontology) {

    if (axioms == null || axioms.isEmpty() || ontology == null || ontology.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file sent.");
    }

    try {
      return _verbalize(axioms, ontology);
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
        "Could not handle request.");
  }

  /**
   * 
   * @param axioms
   * @param ontology
   * @return
   */
  protected VerbalizerResults _verbalize(final MultipartFile axioms, final MultipartFile ontology) {
    final Path axiomsPath = fileUpload(axioms, ServiceApp.tmp);
    final Path ontologyPath = fileUpload(ontology, ServiceApp.tmp);

    return new VerbalizerHandler(axiomsPath, ontologyPath).getVerbalizerResults();
  }

  protected Path fileUpload(final MultipartFile file, final Path folder) {

    final Path path;
    {
      path = Paths.get(folder.toFile().getAbsolutePath()//
          .concat(File.separator).concat(file.getName()));
      if (!path.toFile().exists()) {
        try {
          path.toFile().createNewFile();
          path.toFile().deleteOnExit();
        } catch (final IOException e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
      }
    }

    try {
      final InputStream in = file.getInputStream();
      final OutputStream out = new FileOutputStream(path.toFile());
      int read = 0;
      final byte[] bytes = new byte[1024];
      while ((read = in.read(bytes)) != -1) {
        out.write(bytes, 0, read);
      }
      out.close();
      in.close();
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return path;
  }
}
