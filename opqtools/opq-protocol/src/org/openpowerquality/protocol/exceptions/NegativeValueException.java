package org.openpowerquality.protocol.exceptions;

public class NegativeValueException extends OpqPacketException {

  public NegativeValueException(long val) {
    super(String.format("Value (%d) can not be negative.", val));
  }

  public NegativeValueException(double val) {
    super(String.format("Value (%f) can not be negative.", val));
  }
}
