package org.dice_research.raki.verbalizer.webapp.handler;

import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.raki.verbalizer.pipeline.Pipeline;
import org.dice_research.raki.verbalizer.pipeline.data.input.RAKIInput;
import org.dice_research.raki.verbalizer.pipeline.data.output.IOutput;
import org.dice_research.raki.verbalizer.pipeline.data.output.OutputJsonTrainingData;

import simplenlg.lexicon.Lexicon;

public class VerbalizerHandler {

  protected static final Logger LOG = LogManager.getLogger(VerbalizerHandler.class);

  // TODO: how to use outut Json?
  final String output = "";

  private final String axioms;
  private final String ontology;

  private VerbalizerResults verbalizerResults;

  /**
   *
   * @param axioms
   * @param ontology
   */
  public VerbalizerHandler(final String axioms, final String ontology) {
    this.axioms = axioms;
    this.ontology = ontology;

    handle();
  }

  public boolean hasResults() {
    // TODO: update me to handle errors with error messages.
    return true;
  }

  protected void handle() {
    _handle();
    // TODO: call OWL2NL and init results
    verbalizerResults = new VerbalizerResults();
  }

  protected void _handle() {
    final RAKIInput in = new RAKIInput();
    in//
        .setAxioms(Paths.get(axioms))//
        .setOntologyPath(Paths.get(ontology))//
        .setLexicon(Lexicon.getDefaultLexicon());

    final IOutput out = new OutputJsonTrainingData(Paths.get(output));
    // final IOutput out = new OutputTerminal();

    Pipeline.getInstance().setInput(in).setOutput(out).run().getOutput();
  }

  public String getAxioms() {
    return axioms;
  }

  public String getOntology() {
    return ontology;
  }

  public VerbalizerResults getVerbalizerResults() {
    return verbalizerResults;
  }
}
