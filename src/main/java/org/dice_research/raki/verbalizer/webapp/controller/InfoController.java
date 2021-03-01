package org.dice_research.raki.verbalizer.webapp.controller;

import org.dice_research.raki.verbalizer.webapp.handler.InfoHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

  @GetMapping("/info")
  public InfoHandler info() {
    return new InfoHandler();
  }
}
