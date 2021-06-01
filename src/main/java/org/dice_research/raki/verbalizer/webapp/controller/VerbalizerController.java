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
import org.dice_research.raki.verbalizer.webapp.Const;
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

  protected String requestDrill(final MultipartFile input) {
    // http drill
    String drillResponse = null;
    try {
      final Path file = fileUpload(input, Const.tmp); //

      final HttpPost request = new HttpPost(Const.DRILL);
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
        LOG.info(request.toString());
        LOG.info(response.toString());
        throw new ResponseStatusException(
            HttpStatus.resolve(response.getStatusLine().getStatusCode()), response.toString());
      }
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return drillResponse;
  }

  @PostMapping("/raki")
  public VerbalizerResults raki(//
      @RequestParam(value = "input") final MultipartFile input, //
      @RequestParam(value = "ontology") final MultipartFile ontology,
      @RequestParam(defaultValue = "model") final String type) {

    if (input == null || input.isEmpty() || ontology == null || ontology.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file sent.");
    }

    String drillResponse = requestDrill(input);

    if (drillResponse != null) {
      drillResponse = new PrePro().getWithoutImports(drillResponse);

      final Path path = Paths.get(Const.tmp.toFile().getAbsolutePath()//
          .concat(File.separator)//
          .concat(String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()))//
          .concat("_concept_learning"));//

      RakiIO.write(path, drillResponse.getBytes());

      if (type.equals("model")) {
        return new VerbalizerHandler(//
            path, //
            fileUpload(ontology, Const.tmp)//
        )//
            .runsModel()//
            .getVerbalizerResults();
      } else if (type.equals("rules")) {
        return new VerbalizerHandler(//
            path, //
            fileUpload(ontology, Const.tmp)//
        )//
            .runsRules()//
            .getVerbalizerResults();
      }
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
          fileUpload(axioms, Const.tmp), //
          fileUpload(ontology, Const.tmp)//
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
          fileUpload(axioms, Const.tmp), //
          fileUpload(ontology, Const.tmp)//
      )//
          .runsRules()//
          .getVerbalizerResults();

    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    throw new ResponseStatusException(//
        HttpStatus.INTERNAL_SERVER_ERROR, //
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
    try {
      file.transferTo(path.toFile());
      path.toFile().deleteOnExit();
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return path;
  }
}
