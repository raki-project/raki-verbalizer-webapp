package org.dice_research.raki.verbalizer.webapp.handler;

import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * This class aims to provide some information about the running service, like version and a how to
 * use the service etc.
 *
 * @author rspeck
 *
 */
public class InfoHandler {

  protected static final Logger LOG = LogManager.getLogger(InfoHandler.class);

  public String version = "n/a";

  public InfoHandler() {
    try {
      final MavenXpp3Reader reader = new MavenXpp3Reader();
      final Model model = reader.read(new FileReader("pom.xml"));
      version = model.getVersion();
    } catch (IOException | XmlPullParserException e) {
    }
  }
}
