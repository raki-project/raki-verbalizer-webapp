package org.dice_research.raki.verbalizer.webapp.handler;

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.raki.verbalizer.pipeline.Pipeline;
import org.dice_research.raki.verbalizer.pipeline.data.input.RAKIInput;
import org.dice_research.raki.verbalizer.pipeline.data.output.IOutput;
import org.dice_research.raki.verbalizer.pipeline.data.output.OutputJsonTrainingData;
import org.json.JSONArray;

public class VerbalizerHandler {

  protected static final Logger LOG = LogManager.getLogger(VerbalizerHandler.class);

  private VerbalizerResults verbalizerResults = null;
  private RAKIInput in = null;

  /**
   *
   * @param axioms
   * @param ontology
   */
  public VerbalizerHandler(final Path axioms, final Path ontology) {

    in = new RAKIInput();

    in.setAxioms(axioms).setOntology(ontology);

  }

  public static VerbalizerResults getVerbalizerResults(final Path axioms, final Path ontology,
      final RAKIInput.Type type) {
    return new VerbalizerHandler(axioms, ontology)//
        .run(type)//
        .getVerbalizerResults();
  }

  protected VerbalizerHandler run(final RAKIInput.Type type) {
    verbalizerResults = new VerbalizerResults();
    final IOutput<JSONArray> out = new OutputJsonTrainingData();
    // out = new OutputTerminal();

    in.setType(type);

    Pipeline.getInstance()//
        .setInput(in)//
        .setOutput(out)//
        .run();

    verbalizerResults.setResponse(out.getResults());
    return this;
  }

  protected VerbalizerResults getVerbalizerResults() {
    return verbalizerResults;
  }
}
