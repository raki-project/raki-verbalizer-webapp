package org.dice_research.raki.verbalizer.webapp.controller;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/**
 * This class holds data of an http response.
 */
public class StatusInfo {

  protected static final Logger LOG = LogManager.getLogger(StatusInfo.class);

  /* A status message about the response like errors or warnings. */
  String message = "";

  /* If exception is not null, code has to be defined in HttpStatus */
  int code = -1;

  /* The applications exception. */
  Exception exception = null;

  /**
   * Sets 200 with "ok".
   */
  public static StatusInfo getStatusInfo() {
    return new StatusInfo("ok", 200, null);
  }

  /**
   * Sets 200.
   *
   * @param message
   */
  public static StatusInfo getStatusInfo(final String message) {
    return new StatusInfo(message, 200, null);
  }

  /**
   *
   * @param message
   * @param code
   */
  public static StatusInfo getStatusInfo(final String message, final int code) {
    return new StatusInfo(message, code, null);
  }

  /**
   *
   * @param statusMessage
   * @param code
   * @param exception
   */
  public static StatusInfo getStatusInfo(final String statusMessage, final int code,
      final Exception exception) {
    return new StatusInfo(statusMessage, code, exception);
  }

  /**
   * Private constructor.
   *
   * @param statusMessage
   * @param code
   * @param exception
   */
  private StatusInfo(final String statusMessage, final int code, final Exception exception) {
    message = statusMessage;
    this.code = code;
    this.exception = exception;
  }

  /**
   * Handles response depending on the class variables.
   *
   * @return a response object, exception or http response
   */
  public Object reponse() {
    exception();
    return ResponseEntity.ok(message);
  }

  /**
   * Handles response depending on the class variables.
   *
   */
  public void exception() {
    // if in log debug, full exception message is added
    if (LOG.isDebugEnabled() && exception != null) {
      message = message.concat(System.getProperty("line.separator"))
          .concat(ExceptionUtils.getStackTrace(exception));
    }
    if (exception != null || !HttpStatus.resolve(code).is2xxSuccessful()) {
      LOG.error(exception.getLocalizedMessage(), exception);
      throw new ResponseStatusException(HttpStatus.resolve(code), message);
    }
  }
}
