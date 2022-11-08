package org.dice_research.raki.verbalizer.webapp.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * A RestController for RAKI to control requests to DRILL and the OWL Verbalizer.
 */
@RestController
public class VerbalizerController {

  protected static final Logger LOG = LogManager.getLogger(VerbalizerController.class);

  @Value("${drill.endpoint}")
  private String drillEndpoint;

  @Autowired
  private InfoController infoController;

  private final MultipartFileHelper fileHelper = new MultipartFileHelper();

  /**
   */
  class ParametersVerbalizer {
    Type type;
    Path axioms;
    Path ontology;
  }

  /**
   * Http POST method that uploads the given feedback file to the applications temporary folder and
   * returns the upload path.
   *
   * @param feedback file with pos and neg examples in json
   * @return a path representing the name of the file or directory, or null if this path has zero
   *         elements
   * @throws IOException
   * @throws IllegalStateException
   */
  @SuppressWarnings("unchecked")
  @PostMapping("/feedback")
  public ResponseEntity<String> feedback(
      @RequestParam(value = "feedback") final MultipartFile feedback) {

    StatusInfo si;
    if (feedback == null || feedback.isEmpty()) {
      si = StatusInfo.getStatusInfo("Empty file sent.", HttpStatus.BAD_REQUEST.value());
    } else {
      try {
        final Path path = fileHelper.fileUpload(feedback, ServiceApp.tmp);
        si = StatusInfo.getStatusInfo(path.getFileName().toString());
      } catch (IllegalStateException | IOException e) {
        si = StatusInfo.getStatusInfo(e.getLocalizedMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
      }
    }
    return (ResponseEntity<String>) si.reponse();
  }

  /**
   * * Http POST method that requests DRILL and then the OWL Verbalizer.
   *
   * @param inputExamples
   * @param onto
   * @param ontoName
   * @param type
   * @return
   */
  @PostMapping("/raki")
  public VerbalizerResults raki(//
      @RequestParam(value = "input") final MultipartFile inputExamples, //
      @RequestParam(value = "ontology") final Optional<MultipartFile> onto,
      @RequestParam(value = "ontologyName") final Optional<String> ontoName,
      @RequestParam(defaultValue = "rules") final String type) {

    if (inputExamples == null || inputExamples.isEmpty()) {
      StatusInfo.getStatusInfo(//
          "Some parameters are missing or wrong. Read the documentation.",
          HttpStatus.BAD_REQUEST.value()).exception();
    } else {
      // request drill
      String drillResponse = null;
      try {
        drillResponse = requestDrill(inputExamples);
      } catch (IllegalStateException | IOException e) {
        StatusInfo.getStatusInfo(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST.value(), e)
            .exception();
      } //

      // request verbalizer
      if (drillResponse == null) {
        StatusInfo.getStatusInfo(//
            "DRILL had no response. See DRILL log for more information.",
            HttpStatus.I_AM_A_TEAPOT.value()).exception();
      } else {
        final MultipartFile axioms = fileHelper.getXMLMultipartFile(drillResponse.getBytes());
        return verbalizer(axioms, onto, ontoName, type);
      } //
    }
    return null;
  }

  /**
   * Uploads the given file and requests DRILL.
   *
   * @param input file with pos and neg examples in JSON
   * @return response of DRILL
   * @throws IOException
   * @throws IllegalStateException
   */
  private String requestDrill(final MultipartFile input) throws IllegalStateException, IOException {
    String drillResponse = null;

    final HttpPost request = new HttpPost(drillEndpoint);
    request.setEntity(new FileEntity(fileHelper.fileUpload(input, ServiceApp.tmp).toFile()));

    HttpResponse response = null;
    try {
      response = HttpClientBuilder.create()//
          .build()//
          .execute(request);
    } catch (final IOException e) {
      StatusInfo.getStatusInfo(
          "Could not request DRILL:  " + request.toString() + " " + request.getEntity().toString(),
          HttpStatus.NOT_ACCEPTABLE.value(), e).exception();
    }

    if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {

      final BufferedReader content =
          new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      final StringBuilder result = new StringBuilder();
      String line;
      while ((line = content.readLine()) != null) {
        result.append(line);
      }
      drillResponse = result.toString();
    } else {
      StatusInfo.getStatusInfo("Could not get a DRILL response:",
          response.getStatusLine().getStatusCode()).exception();
    }
    return drillResponse;
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
      final Optional<MultipartFile> onto, final Optional<String> ontoName, final String type) {

    String ontologyName = null;
    MultipartFile ontology = null;

    if (onto != null && onto.isPresent()) {
      ontology = onto.get();
    } else if (ontoName != null && ontoName.isPresent()) {
      ontologyName = ontoName.get();
    }

    if (!(ontologyName == null && ontology == null)) {
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
            StatusInfo
                .getStatusInfo("Could not find the given ontology.", HttpStatus.BAD_REQUEST.value())
                .exception();
            return null;
          }

          p.axioms = fileHelper.fileUpload(fileHelper.removeImports(axioms), ServiceApp.tmp);
          p.type = RAKIInput.Type.valueOf(type.toUpperCase());
          return p;
        }
      } catch (final Exception e) {
        StatusInfo.getStatusInfo("Parameter check failed.", HttpStatus.BAD_REQUEST.value(), e)
            .exception();
      }
    }
    return null;
  }

  private VerbalizerResults verbalizer(final MultipartFile axioms,
      final Optional<MultipartFile> onto, final Optional<String> ontoName, final String type) {

    final ParametersVerbalizer parameters = checksParams(axioms, onto, ontoName, type);
    if (parameters != null) {
      try {
        return VerbalizerHandler.getVerbalizerResults(//
            parameters.axioms, parameters.ontology, parameters.type);
      } catch (final Exception e) {
        StatusInfo
            .getStatusInfo("VerbalizerHandler error.", HttpStatus.INTERNAL_SERVER_ERROR.value(), e)
            .exception();
      }
    }

    StatusInfo.getStatusInfo(
        "Some parameters are missing or wrong. Read the documentation. Given parameters: "
            + parameters,
        HttpStatus.BAD_REQUEST.value()).exception();
    return null;
  }
}
