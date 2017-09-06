/*
  This file is part of opq-sim.

  opq-sim is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  opq-sim is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with opq-sim. If not, see <http://www.gnu.org/licenses/>.

  Copyright 2014 Anthony Christe
*/

package org.openpowerquality.sim.utils;

/**
 * Helper class to convert between different data types from text fields.
 * The parameter types and return types for the conversions are Strings since they take and update directly values for
 * the TextField fields.
 */
public class DataConverter {
  /**
   * Converts an integer into the an array of bytes.
   * @param n the integer.
   * @return the array of bytes.
   */
  public static String intToBytes(String n) {
    int i;

    try {
      i = Integer.parseInt(n);
    }
    catch (NumberFormatException e) {
      return "";
    }

    return Integer.toHexString(i);
  }

  /**
   * Converts a long into an array of bytes.
   * @param n the long.
   * @return an array of bytes.
   */
  public static String longToBytes(String n) {
    long i;

    try {
      i = Long.parseLong(n);
    }
    catch (NumberFormatException e) {
      return "";
    }

    return Long.toHexString(i);
  }

  /**
   * Converts a double into an array of bytes.
   * @param n the double.
   * @return an array of bytes.
   */
  public static String doubleToBytes(String n) {
    double d;

    try {
      d = Double.parseDouble(n);
    }
    catch (NumberFormatException e) {
      return "";
    }

    return Double.toHexString(d);
  }

  /**
   * Converts bytes into an integer.
   * @param n the bytes.
   * @return an integer.
   */
  public static String bytesToInt(String n) {
    Integer i;

    try {
      i = Integer.parseInt(n, 16);
    }
    catch (NumberFormatException e) {
      return "";
    }

    return i.toString();
  }

  /**
   * Converts bytes into a long.
   * @param n the bytes.
   * @return a long.
   */
  public static String bytesToLong(String n) {
    Long i;

    try {
      i = Long.parseLong(n, 16);
    }
    catch (NumberFormatException e) {
      return "";
    }

    return i.toString();
  }

  /**
   * Determines if the string representation of the integer is valid.
   * @param n the integer.
   * @return the validity of the integer.
   */
  public static boolean isValidInt(String n) {
    try {
      Integer.parseInt(n);
      return true;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Determines if the string representation of the integer bytes is valid.
   * @param n the integer bytes.
   * @return the validity of the integer bytes.
   */
  public static boolean isValidBytesInt(String n) {
    try {
      Integer.parseInt(n, 16);
      return true;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Determines if the string representation of the long is valid.
   * @param n the long.
   * @return the validity of the long.
   */
  public static boolean isValidLong(String n) {
    try {
      Long.parseLong(n);
      return true;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Determines if the string representation of the long bytes is valid.
   * @param n the long bytes.
   * @return the validity of the long bytes.
   */
  public static boolean isValidBytesLong(String n) {
    try {
      Long.parseLong(n, 16);
      return true;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Determines if the string representation of the double is valid.
   * @param n the double.
   * @return the validity of the double.
   */
  public static boolean isValidDouble(String n) {
    try {
      Double.parseDouble(n);
      return true;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }

}
