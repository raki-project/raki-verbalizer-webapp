package org.dice_research.raki.verbalizer.webapp.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerHandler;
import org.dice_research.raki.verbalizer.webapp.handler.VerbalizerResults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class VerbalizerController {

  protected static final Logger LOG = LogManager.getLogger(VerbalizerController.class);

  VerbalizerHandler v;

  // TODO: use: "Reader reader = new InputStreamReader(file.getInputStream());"?
  public String readStream(final InputStream is) throws IOException {
    final Scanner s = new Scanner(is);
    s.useDelimiter("\\A");
    final String result = s.hasNext() ? s.next() : "";
    is.close();
    s.close();
    return result;
  }

  public String fileUpload(final MultipartFile file, final String folder) {
    InputStream inputStream = null;
    OutputStream outputStream = null;
    final String fileName = file.getOriginalFilename();
    final File newFile = new File(folder + fileName);

    try {
      inputStream = file.getInputStream();

      if (!newFile.exists()) {
        newFile.createNewFile();
      }
      outputStream = new FileOutputStream(newFile);
      int read = 0;
      final byte[] bytes = new byte[1024];

      while ((read = inputStream.read(bytes)) != -1) {
        outputStream.write(bytes, 0, read);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return newFile.getAbsolutePath();
  }

  @PostMapping("/verbalize")
  public VerbalizerResults verbalize(//
      @RequestParam(value = "axioms") final MultipartFile axioms, //
      @RequestParam(value = "ontology") final MultipartFile ontology) {

    if (axioms == null || axioms.isEmpty() || ontology == null || ontology.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file sent.");
    }

    final String defaultBaseDir = System.getProperty("java.io.tmpdir").concat("/");
    /**
     * <code>
     try {
       FileWriter myWriter = new FileWriter(defaultBaseDir.concat(axioms.getResource().getFile().getName()));
       myWriter.write(readStream(axioms.getInputStream()));
       myWriter.close();
     } catch (IOException e) {
     }
    
     </code>
     */
    final String axiomsPath = fileUpload(axioms, defaultBaseDir);
    final String ontologyPath = fileUpload(ontology, defaultBaseDir);

    final VerbalizerHandler vh = new VerbalizerHandler(//
        axiomsPath, ontologyPath);
    if (vh != null && vh.hasResults()) {
      return vh.getVerbalizerResults();
    }

    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
        "Could not handle request.");
  }
}
