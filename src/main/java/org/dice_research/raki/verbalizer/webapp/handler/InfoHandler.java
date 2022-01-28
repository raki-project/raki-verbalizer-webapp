package org.dice_research.raki.verbalizer.webapp.handler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.map.HashedMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dice_research.raki.verbalizer.webapp.Const;

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
   */
  public Set<Map<String, String>> version = new HashSet<>();

  /**
   */
  public Set<Map<String, String>> ontology = new HashSet<>();

  public InfoHandler() {
    // initializes versions
    try {
      final MavenXpp3Reader reader = new MavenXpp3Reader();
      final Model model = reader.read(new FileReader("pom.xml"));
      final Map<String, String> element = new HashedMap<>();
      element.put("name", model.getName());
      element.put("version", model.getVersion());
      version.add(element);

    } catch (IOException | XmlPullParserException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    // initializes owl files
    ontology.addAll(getFiles(Const.staticFilesFolderOntology));
  }

  /**
   * Gets all files with the "owl" file extension in the given and all sub directories.
   *
   * @return file name and path
   */
  protected Set<Map<String, String>> getFiles(final String name) {
    final Set<Map<String, String>> ontology = new HashSet<>();
    LOG.info(name);
    try {
      for (final File file : new File(getClass()//
          .getResource(name)//
          .toURI()).listFiles()) {
        if (file.isDirectory()) {
          ontology.addAll(getFiles(name.concat("/").concat(file.getName())));
        } else if ("owl".equals(FilenameUtils.getExtension(file.getName()))) {
          final Map<String, String> element = new HashedMap<>();
          element.put("name", file.getName());
          element.put("path", file.getPath());
          ontology.add(element);
        }
      }
    } catch (final URISyntaxException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return ontology;
  }
}
