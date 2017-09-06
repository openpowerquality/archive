/*
  This file is part of OPQHub.

  OPQHub is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  OPQHub is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with OPQHub.  If not, see <http://www.gnu.org/licenses/>.

  Copyright 2014 Anthony Christe
 */

package utils;

import play.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Provides various helper utilities for dealing with forms.
 */
public final class FormUtils {
  /**
   * Ensure that util class can not be instantiated.
   */
  private FormUtils() {
    throw new AssertionError("FormUtils should not be instantiated");
  }

  /**
   * Hashes a password using the SHA-256 algorithm.
   *
   * @param password The password to be hashed.
   *
   * @return The secure hash of the password.
   */
  public static byte[] hashPassword(String password, byte[] salt) {
    MessageDigest md;
    byte[] passwordBytes = password.getBytes();
    byte[] data = new byte[salt.length + passwordBytes.length];
    byte[] hashed;

    System.arraycopy(salt, 0, data, 0, salt.length);
    System.arraycopy(passwordBytes, 0, data, salt.length, passwordBytes.length);

    try {
      md = MessageDigest.getInstance("SHA-256");
      md.update(data);
      hashed = md.digest();
    }
    catch (NoSuchAlgorithmException e) {
      hashed = new byte[0];
      Logger.error("This system does not support the SHA-256 hashing algorithm.");
      e.printStackTrace();
    }
    return hashed;
  }

  /**
   * Generates a random 32 byte salt.
   *
   * @return Random 32 byte salt.
   */
  public static byte[] generateRandomSalt() {
    byte[] salt = new byte[32];
    SecureRandom random = new SecureRandom();
    random.nextBytes(salt);
    return salt;
  }

}
