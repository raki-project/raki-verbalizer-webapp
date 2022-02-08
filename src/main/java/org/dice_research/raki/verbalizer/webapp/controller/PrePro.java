package org.dice_research.raki.verbalizer.webapp.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Helper class to remove imported ontologies within an ontology, since we don't want to verbalize
 * those imports.
 *
 * @author rspeck
 *
 */
public class PrePro {
  protected static final Logger LOG = LogManager.getLogger(PrePro.class);

  final String tag = "owl:Ontology";

  protected String documentToString(final Document doc) {
    try {
      final StringWriter writer = new StringWriter();
      TransformerFactory.newInstance().newTransformer()//
          .transform(new DOMSource(doc), new StreamResult(writer));
      return writer.toString();
    } catch (final TransformerException e) {
      LOG.error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  public String removeImports(final Path xml) throws FileNotFoundException {
    return documentToString(_removeImports(xml));
  }

  public String removeImports(final String xml) {
    return documentToString(_removeImports(xml));
  }

  public String removeImports(final InputSource xml) {
    return documentToString(_removeImports(xml));
  }

  protected Document _removeImports(final Document doc) {

    while (doc.getElementsByTagName(tag).getLength() > 0) {
      final NodeList nodeList = doc.getElementsByTagName(tag);
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);
        node.getParentNode().removeChild(node);
      }
    }
    doc.normalize();
    return doc;
  }

  protected Document _removeImports(final InputSource is) {
    try {
      return _removeImports(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is));
    } catch (ParserConfigurationException | SAXException | IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  protected Document _removeImports(final String xml) {
    return _removeImports(new InputSource(new StringReader(xml)));
  }

  protected Document _removeImports(final Path file) throws FileNotFoundException {
    return _removeImports(new InputSource(new FileInputStream(file.toFile().getAbsolutePath())));
  }

}
