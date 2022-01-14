package org.dice_research.raki.verbalizer.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication(exclude = SolrAutoConfiguration.class)
public class ServiceApp extends SpringBootServletInitializer {

  public static void main(final String[] args) {

    new SpringApplicationBuilder(ServiceApp.class)//
        .listeners(new ApplicationPidFileWriter(".shutdown.pid"))//
        .web(WebApplicationType.NONE)//
        .build()//
        .run(args);

    SpringApplication.run(ServiceApp.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
    return builder.sources(ServiceApp.class);
  }

  /**
   * Makes the app public to each origin request.
   *
   * @return
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/*").allowedOrigins("*");
      }
    };
  }
}
