package org.dice_research.raki.verbalizer.webapp.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.raki.verbalizer.pipeline.io.RakiIO;
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

  public static String DRILL = "http://localhost:9080/concept_learning";
  public static String VERB = "http://localhost:4443/verbalize";

  @PostMapping("/raki")
  public VerbalizerResults raki(//
      @RequestParam(value = "input") final MultipartFile input, //
      @RequestParam(value = "ontology") final MultipartFile ontology) {

    if (input == null || input.isEmpty() || ontology == null || ontology.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file sent.");
    }

    // http drill
    String drillResponse = null;
    try {
      final Path file = fileUpload(input, ServiceApp.tmp); //

      final HttpPost request = new HttpPost(DRILL);
      request.setEntity(new FileEntity(file.toFile()));

      final HttpResponse response = HttpClientBuilder.create()//
          .build()//
          .execute(request);

      if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {

        final BufferedReader reader = new BufferedReader(new InputStreamReader(//
            response.getEntity().getContent()//
        ));
        final StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          result.append(line);
        }

        drillResponse = result.toString();

      } else {
        throw new ResponseStatusException(
            HttpStatus.resolve(response.getStatusLine().getStatusCode()),
            "Could not handle request.");
      }
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    if (drillResponse != null) {

      final Path path = Paths.get(ServiceApp.tmp.toFile().getAbsolutePath()//
          .concat(File.separator)//
          .concat(String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()))//
          .concat("_concept_learning"));//

      RakiIO.write(path, drillResponse.getBytes());

      // http verbalizer
      return new VerbalizerHandler(//
          path, //
          fileUpload(ontology, ServiceApp.tmp)//
      )//
          .runsModel()//
          .getVerbalizerResults();
    }

    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
        "Could not handle request.");
  }

  @PostMapping("/verbalize")
  public VerbalizerResults verbalize(//
      @RequestParam(value = "axioms") final MultipartFile axioms, //
      @RequestParam(value = "ontology") final MultipartFile ontology) {

    if (axioms == null || axioms.isEmpty() || ontology == null || ontology.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file sent.");
    }
    try {
      return new VerbalizerHandler(//
          fileUpload(axioms, ServiceApp.tmp), //
          fileUpload(ontology, ServiceApp.tmp)//
      )//
          .runsModel()//
          .getVerbalizerResults();
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
        "Could not handle request.");
  }

  /**
   * Runs the rule base algorithm.
   *
   * @param axioms files
   * @param ontology files
   * @return VerbalizerResults
   */
  @PostMapping("/rules")
  public VerbalizerResults rules(//
      @RequestParam(value = "axioms") final MultipartFile axioms, //
      @RequestParam(value = "ontology") final MultipartFile ontology) {
    if (axioms == null || axioms.isEmpty() || ontology == null || ontology.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file sent.");
    }
    try {
      return new VerbalizerHandler(//
          fileUpload(axioms, ServiceApp.tmp), //
          fileUpload(ontology, ServiceApp.tmp)//
      )//
          .runsRules()//
          .getVerbalizerResults();

    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
        "Could not handle request.");
  }

  /**
   * Uploads the given file to the given folder.
   *
   * @param file
   * @param folder
   * @return Path of the stored file
   */
  protected Path fileUpload(final MultipartFile file, final Path folder) {

    final Path path = Paths.get(folder.toFile()//
        .getAbsolutePath()//
        .concat(File.separator)//
        .concat(String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()))//
        .concat("_")//
        .concat(file.getOriginalFilename()));

    // if (!path.toFile().exists()) {
    try {
      file.transferTo(path.toFile());
      path.toFile().deleteOnExit();
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    // }
    return path;
  }
}
