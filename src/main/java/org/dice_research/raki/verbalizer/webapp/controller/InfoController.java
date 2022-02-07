package org.dice_research.raki.verbalizer.webapp.controller;

import org.dice_research.raki.verbalizer.webapp.handler.InfoHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

  @Value("${static.folder.ontology}")
  private String staticFolderOntology;

  private InfoHandler infoHandler = null;

  @GetMapping("/info")
  public InfoHandler info() {
    if (infoHandler == null) {
      infoHandler = new InfoHandler(staticFolderOntology);
    }
    return infoHandler;
  }
}
