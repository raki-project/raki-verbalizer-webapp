package org.dice_research.raki.verbalizer.webapp.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.raki.verbalizer.pipeline.data.input.RAKIInput;
import org.dice_research.raki.verbalizer.pipeline.io.RakiIO;
import org.dice_research.raki.verbalizer.webapp.ServiceApp;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerHandler;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerResults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class VerbalizerController {

  protected static final Logger LOG = LogManager.getLogger(VerbalizerController.class);

  @Value("${drill.endpoint}")
  private String drillEndpoint;

  @PostMapping("/feedback")
  public ResponseEntity<String> feedback(
      @RequestParam(value = "feedback") final MultipartFile feedback) {

    if (feedback == null || feedback.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file sent.");
    } else {

      final Path e = fileUpload(feedback, ServiceApp.tmp);

      return ResponseEntity.ok(e.getFileName().toString());// .build();
      // OR ResponseEntity.ok("body goes here");
    }
  }

  private String requestDrill(final MultipartFile input) {
    String drillResponse = null;
    try {
      final Path file = fileUpload(input, ServiceApp.tmp); //

      final HttpPost request = new HttpPost(drillEndpoint);
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
        LOG.error(request.toString());
        LOG.error(response.toString());
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
      @RequestParam(value = "input") final MultipartFile axioms, //
      @RequestParam(value = "ontology") final MultipartFile ontology,
      @RequestParam(defaultValue = "rules") final String type) {

    if (checksParams(axioms, ontology, type)) {

      String drillResponse = requestDrill(axioms);

      if (drillResponse != null) {
        drillResponse = new PrePro().getWithoutImports(drillResponse);

        final Path path = Paths.get(ServiceApp.tmp.toFile().getAbsolutePath()//
            .concat(File.separator)//
            .concat(String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()))//
            .concat("_concept_learning"));//

        RakiIO.write(path, drillResponse.getBytes());

        return VerbalizerHandler.getVerbalizerResults(//
            path, fileUpload(ontology, ServiceApp.tmp), RAKIInput.Type.valueOf(type)//
        );
      }
    }
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "Some parameters are missing or wrong. Read the documentation.");
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
    return verbalizer(axioms, ontology, RAKIInput.Type.RULES.name());
  }

  /**
   * Runs the nn base algorithm.
   *
   * @param axioms files
   * @param ontology files
   * @return VerbalizerResults
   */
  @PostMapping("/verbalize")
  public VerbalizerResults verbalize(//
      @RequestParam(value = "axioms") final MultipartFile axioms, //
      @RequestParam(value = "ontology") final MultipartFile ontology) {
    return verbalizer(axioms, ontology, RAKIInput.Type.MODEL.name());
  }

  private boolean checksParams(final MultipartFile axioms, final MultipartFile ontology,
      final String type) {
    // checks inputs
    if (axioms == null || axioms.isEmpty() || ontology == null || ontology.isEmpty() || type == null
        || !Arrays.asList(RAKIInput.Type.values()).contains(RAKIInput.Type.valueOf(type))) {
      return false;
    } else {
      return true;
    }
  }

  private VerbalizerResults verbalizer(final MultipartFile axioms, final MultipartFile ontology,
      final String type) {

    if (checksParams(axioms, ontology, type)) {
      try {
        final MultipartFile ontologyWithoutImports = removeImports(ontology);
        return VerbalizerHandler.getVerbalizerResults(//
            fileUpload(axioms, ServiceApp.tmp), //
            fileUpload(ontologyWithoutImports, ServiceApp.tmp), //
            RAKIInput.Type.valueOf(type)//
        );
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "Some parameters are missing or wrong. Read the documentation.");
  }

  private MultipartFile removeImports(final MultipartFile file) {
    String c = "";;
    try {
      c = new PrePro().getWithoutImports(fileUpload(file, ServiceApp.tmp));
    } catch (final FileNotFoundException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return new MockMultipartFile(file.getName(), file.getName(), file.getContentType(),
        c.getBytes());
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
