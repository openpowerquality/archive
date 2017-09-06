package org.openpowerquality.protocol.exceptions;


public class InvalidByteSizeException extends OpqPacketException {
  public InvalidByteSizeException(int givenSize, int expected) {
    super(String.format("Incorrect number of bytes found. Given %d expected %d.",givenSize, expected));
  }
}
