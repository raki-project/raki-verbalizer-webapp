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
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 */
public class MultipartFileHelper {

  protected static final Logger LOG = LogManager.getLogger(MultipartFileHelper.class);

  /**
   * Removes imports in rdf owl.
   *
   * @param file
   * @return
   * @throws IOException
   * @throws IllegalStateException
   * @throws FileNotFoundException
   */
  public MultipartFile removeImports(final MultipartFile file)
      throws FileNotFoundException, IllegalStateException, IOException {
    String content = "";
    content = new PrePro().removeImports(fileUpload(file, ServiceApp.tmp));
    return new MockMultipartFile(//
        file.getName(), file.getName(), file.getContentType(), content.getBytes());
  }

  /**
   * Uploads the given file to the given folder and renames the file with a time stamp prefix.
   * Deletes all uploaded files on exit.
   *
   * @param file
   * @param folder
   * @return Path of the stored file
   * @throws IOException
   * @throws IllegalStateException
   */
  public Path fileUpload(final MultipartFile file, final Path folder)
      throws IllegalStateException, IOException {
    final Path path = Paths.get(folder.toFile()//
        .getAbsolutePath()//
        .concat(File.separator)//
        .concat(String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()))//
        .concat("_")//
        .concat(file.getOriginalFilename()));
    file.transferTo(path.toFile());
    path.toFile().deleteOnExit();
    return path;
  }

  public MultipartFile getXMLMultipartFile(final byte[] b) {
    return new MockMultipartFile(//
        String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()), //
        null, //
        MimeTypeUtils.APPLICATION_XML.getType(), //
        b);
  }
}
