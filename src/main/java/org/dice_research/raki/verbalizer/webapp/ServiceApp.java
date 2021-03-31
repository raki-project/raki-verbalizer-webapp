package org.dice_research.raki.verbalizer.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(exclude = SolrAutoConfiguration.class)
public class ServiceApp extends SpringBootServletInitializer {
  public static void main(final String[] args) {

    final SpringApplication springApplication =
        new SpringApplicationBuilder(ServiceApp.class).web(WebApplicationType.NONE).build();
    springApplication.addListeners(new ApplicationPidFileWriter("./bin/shutdown.pid"));
    springApplication.run(args);

    SpringApplication.run(ServiceApp.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
    return builder.sources(ServiceApp.class);
  }
}
