package org.dice_research.raki.verbalizer.webapp.handler;

import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * This class aims to provide information about the running service, e.g. versions, how to use the
 * service etc.
 *
 * @author rspeck
 *
 */
public class InfoHandler {

  protected static final Logger LOG = LogManager.getLogger(InfoHandler.class);

  /**
   * The raki verbalizer webapp version.
   */
  public String version = "n/a";

  /**
   * Names of the trained models.
   */
  // public Set<String> modelNames;

  /**
   * Names of the trained models.
   */
  // public Map<String, String> modelNamesToOntologyIRI;

  public InfoHandler() {
    // initializes version
    try {
      final MavenXpp3Reader reader = new MavenXpp3Reader();
      final Model model = reader.read(new FileReader("pom.xml"));
      version = model.getVersion();
    } catch (IOException | XmlPullParserException e) {
      LOG.warn(e.getLocalizedMessage(), e);
    }

  }
}
