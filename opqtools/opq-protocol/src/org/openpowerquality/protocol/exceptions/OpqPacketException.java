package org.openpowerquality.protocol.exceptions;

public class OpqPacketException extends RuntimeException {
  public OpqPacketException(String message) {
    super(message);
  }

  public OpqPacketException(String message, Throwable cause) {
    super(message, cause);
  }
}
