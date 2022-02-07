package org.dice_research.raki.verbalizer.webapp.controller;

import java.io.BufferedReader;
import java.io.File;
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
import org.dice_research.raki.verbalizer.pipeline.io.RakiIO;
import org.dice_research.raki.verbalizer.webapp.ServiceApp;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerHandler;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    ParametersVerbalizer parameters = null;
    {
      String ontologyName = null;
      MultipartFile ontology = null;
      if (onto.isPresent()) {
        ontology = onto.get();
      } else if (ontoName.isPresent()) {
        ontologyName = ontoName.get();
      } else {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Some parameters are missing or wrong. Read the documentation.");
      }
      parameters = checksParams(inputExamples, ontology, ontologyName, type);
    }

    if (parameters != null) {

      String drillResponse = requestDrill(inputExamples);

      if (drillResponse != null) {
        drillResponse = new PrePro().removeImports(drillResponse);

        final Path path = Paths.get(ServiceApp.tmp.toFile().getAbsolutePath()//
            .concat(File.separator)//
            .concat(String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()))//
            .concat("_concept_learning"));//

        RakiIO.write(path, drillResponse.getBytes());

        return VerbalizerHandler.getVerbalizerResults(//
            path, parameters.ontology, parameters.type//
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

  class ParametersVerbalizer {
    Type type;
    Path axioms;
    Path ontology;
  }

  private ParametersVerbalizer checksParams(final MultipartFile axioms,
      final MultipartFile ontology, final String ontologyName, final String type) {

    final ParametersVerbalizer p = new ParametersVerbalizer();

    if (axioms != null && !axioms.isEmpty() && //
        type != null && //
        Arrays.asList(RAKIInput.Type.values())//
            .contains(RAKIInput.Type.valueOf(type))//
    ) {
      if (ontology != null && !ontology.isEmpty()) {
        final MultipartFile ontologyWithoutImports = fileHelper.removeImports(ontology);
        p.ontology = fileHelper.fileUpload(ontologyWithoutImports, ServiceApp.tmp);

      } else if (ontologyName != null && !ontologyName.isEmpty()) {
        for (final Map<String, String> map : infoController.info().ontology) {
          if (map.get("name").equals(ontologyName)) {
            p.ontology = Paths.get(map.get("path"));
          }
        }
      } else {
        return null;
      }

      p.axioms = fileHelper.fileUpload(axioms, ServiceApp.tmp);
      p.type = RAKIInput.Type.valueOf(type);
      return p;
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
