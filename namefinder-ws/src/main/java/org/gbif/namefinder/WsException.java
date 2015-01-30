package org.gbif.namefinder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

public class WsException extends ServletException {
  public String errorMessage;
  public int httpErrorCode;
  public Exception exception;

  public WsException(Exception exception) {
    this.httpErrorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    this.exception = exception;
    if (exception != null) {
      this.errorMessage = exception.getMessage();
    }
  }

  public WsException(int httpStatusCode) {
    this.httpErrorCode = httpStatusCode;
  }

  public WsException(int httpStatusCode, Exception exception) {
    this.httpErrorCode = httpStatusCode;
    this.exception = exception;
    if (exception != null) {
      this.errorMessage = exception.getMessage();
    }
  }

  public WsException(int httpStatusCode, String errorMessage) {
    this.httpErrorCode = httpStatusCode;
    this.errorMessage = errorMessage;
  }

  public WsException(int httpStatusCode, String errorMessage, Exception exception) {
    this.httpErrorCode = httpStatusCode;
    this.errorMessage = errorMessage;
    this.exception = exception;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Exception getException() {
    return exception;
  }

  public int getHttpErrorCode() {
    return httpErrorCode;
  }

}
