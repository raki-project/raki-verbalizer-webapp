package org.dice_research.raki.verbalizer.webapp.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.raki.verbalizer.webapp.ServiceApp;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class MultipartFileHelper {

  protected static final Logger LOG = LogManager.getLogger(MultipartFileHelper.class);

  /**
   * Removes imports in rdf owl.
   *
   * @param file
   * @return
   */
  public MultipartFile removeImports(final MultipartFile file) {
    String content = "";
    try {
      content = new PrePro().removeImports(fileUpload(file, ServiceApp.tmp));
    } catch (final FileNotFoundException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return new MockMultipartFile(//
        file.getName(), file.getName(), file.getContentType(), content.getBytes());
  }

  /**
   * Uploads the given file to the given folder.
   *
   * @param file
   * @param folder
   * @return Path of the stored file
   */
  public Path fileUpload(final MultipartFile file, final Path folder) {
    final Path path = Paths.get(folder.toFile()//
        .getAbsolutePath()//
        .concat(File.separator)//
        .concat(String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()))//
        .concat("_")//
        .concat(file.getOriginalFilename()));
    try {
      file.transferTo(path.toFile());
      path.toFile().deleteOnExit();
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return path;
  }
}
