package org.dice_research.raki.verbalizer.webapp;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(exclude = SolrAutoConfiguration.class)
public class ServiceApp extends SpringBootServletInitializer {

  public static final Path tmp;
  static {
    tmp = Paths.get(System.getProperty("java.io.tmpdir").concat(File.separator).concat("raki"));
    if (!tmp.toFile().exists()) {
      tmp.toFile().mkdirs();
    }
  }

  /**
   *
   * @param args
   */
  public static void main(final String[] args) {

    final SpringApplication springApplication =
        new SpringApplicationBuilder(ServiceApp.class).web(WebApplicationType.NONE).build();
    springApplication.addListeners(new ApplicationPidFileWriter(".shutdown.pid"));
    springApplication.run(args);

    SpringApplication.run(ServiceApp.class, args);
  }

  /**
   *
   */
  @Override
  protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
    return builder.sources(ServiceApp.class);
  }
}
