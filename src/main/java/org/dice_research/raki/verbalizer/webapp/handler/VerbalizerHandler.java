package org.dice_research.raki.verbalizer.webapp.handler;

/**
 *
 * @author rspeck
 *
 */
public class VerbalizerHandler {

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
    // TODO: call OWL2NL and init results
    verbalizerResults = new VerbalizerResults();
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
