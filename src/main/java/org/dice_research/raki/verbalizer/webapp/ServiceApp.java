package org.dice_research.raki.verbalizer.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;

/**
 *
 * @author rspeck
 *
 */
@SpringBootApplication
public class ServiceApp {
  public static void main(final String[] args) {

    final SpringApplicationBuilder app =
        new SpringApplicationBuilder(ServiceApp.class).web(WebApplicationType.NONE);
    app.build().addListeners(new ApplicationPidFileWriter("./bin/shutdown.pid"));
    app.run(args);

    // app.application().run(args);
    SpringApplication.run(ServiceApp.class, args);
  }
}
