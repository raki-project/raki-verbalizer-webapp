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

  private final VerbalizerResults verbalizerResults = new VerbalizerResults();

  /**
   *
   * @param axioms
   * @param ontology
   */
  public VerbalizerHandler(final Path axioms, final Path ontology) {
    final RAKIInput in = new RAKIInput();
    in.setAxioms(axioms).setOntology(ontology);

    final IOutput<JSONArray> out = new OutputJsonTrainingData();
    // final IOutput out = new OutputTerminal();

    Pipeline.getInstance()//
        .setInput(new RAKIInput().setAxioms(axioms).setOntology(ontology))//
        .setOutput(out)//
        .run();

    verbalizerResults.setResponse(out.getResults());
  }

  public VerbalizerResults getVerbalizerResults() {
    return verbalizerResults;
  }
}
