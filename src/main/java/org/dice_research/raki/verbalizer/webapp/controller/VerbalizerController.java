package org.dice_research.raki.verbalizer.webapp.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.raki.verbalizer.pipeline.data.input.IRAKIInput.Type;
import org.dice_research.raki.verbalizer.pipeline.data.input.RAKIInput;
import org.dice_research.raki.verbalizer.webapp.ServiceApp;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerHandler;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MimeTypeUtils;
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

  @Autowired
  private InfoController infoController;

  private final MultipartFileHelper fileHelper = new MultipartFileHelper();

  class ParametersVerbalizer {
    Type type;
    Path axioms;
    Path ontology;
  }

  @PostMapping("/feedback")
  public ResponseEntity<String> feedback(
      @RequestParam(value = "feedback") final MultipartFile feedback) {

    if (feedback == null || feedback.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file sent.");
    } else {

      final Path e = fileHelper.fileUpload(feedback, ServiceApp.tmp);

      return ResponseEntity.ok(e.getFileName().toString());// .build();
      // OR ResponseEntity.ok("body goes here");
    }
  }

  private String requestDrill(final MultipartFile input) {
    String drillResponse = null;
    try {
      final Path file = fileHelper.fileUpload(input, ServiceApp.tmp); //

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
      @RequestParam(value = "input") final MultipartFile inputExamples, //
      @RequestParam(value = "ontology") final Optional<MultipartFile> onto,
      @RequestParam(value = "ontologyName") final Optional<String> ontoName,
      @RequestParam(defaultValue = "rules") final String type) {

    if (inputExamples != null && !inputExamples.isEmpty()) {

      final String drillResponse = requestDrill(inputExamples);
      if (drillResponse != null) {

        final MultipartFile axioms = new MockMultipartFile(//
            String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()), //
            null, //
            MimeTypeUtils.APPLICATION_XML.getType(), //
            drillResponse.getBytes());

        return verbalizer(axioms, onto, ontoName, type);
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
      @RequestParam(value = "ontology", required = false) final Optional<MultipartFile> onto,
      @RequestParam(value = "ontologyName", required = false) final Optional<String> ontoName) {
    return verbalizer(axioms, onto, ontoName, RAKIInput.Type.RULES.name());
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
      @RequestParam(value = "ontology") final Optional<MultipartFile> onto,
      @RequestParam(value = "ontologyName") final Optional<String> ontoName) {
    return verbalizer(axioms, onto, ontoName, RAKIInput.Type.MODEL.name());
  }

  private ParametersVerbalizer checksParams(final MultipartFile axioms,
      final MultipartFile ontology, final String ontologyName, final String type) {

    try {
      final ParametersVerbalizer p = new ParametersVerbalizer();

      if (axioms != null && !axioms.isEmpty() && //
          type != null && //
          Arrays.asList(RAKIInput.Type.values())//
              .contains(RAKIInput.Type.valueOf(type.toUpperCase()))//
      ) {

        if (ontology != null && !ontology.isEmpty()) {
          p.ontology = fileHelper.fileUpload(ontology, ServiceApp.tmp);
        } else if (ontologyName != null && !ontologyName.isEmpty()) {
          for (final Map<String, String> map : infoController.info().ontology) {
            if (map.get("name").equals(ontologyName)) {
              p.ontology = Paths.get(map.get("path"));
            }
          }
        } else {
          return null;
        }

        p.axioms = fileHelper.fileUpload(fileHelper.removeImports(axioms), ServiceApp.tmp);
        p.type = RAKIInput.Type.valueOf(type.toUpperCase());
        return p;
      }
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    return null;
  }

  private VerbalizerResults verbalizer(final MultipartFile axioms,
      final Optional<MultipartFile> onto, final Optional<String> ontoName, final String type) {

    ParametersVerbalizer parameters = null;

    String ontologyName = null;
    MultipartFile ontology = null;

    if (onto != null && onto.isPresent()) {
      ontology = onto.get();
    } else if (ontoName != null && ontoName.isPresent()) {
      ontologyName = ontoName.get();
    }

    if (!(ontologyName == null && ontology == null)) {
      parameters = checksParams(axioms, ontology, ontologyName, type);
      if (parameters != null) {
        try {
          return VerbalizerHandler.getVerbalizerResults(//
              parameters.axioms, parameters.ontology, parameters.type);
        } catch (final Exception e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
      }
    }

    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "Some parameters are missing or wrong. Read the documentation. Given parameters: "
            + parameters);
  }
}
