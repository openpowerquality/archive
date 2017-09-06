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

import javafx.scene.control.TextArea;

import java.sql.Timestamp;

/**
 * Provides a singleton class to log messages to the status window.
 */
public class Logger {
  /**
   * Log area to send messages to.
   */
  private static TextArea logArea;

  /**
   * Sets the log area to send messages to.
   * @param textArea The area to send log message to.
   */
  public static void setLogArea(TextArea textArea) {
    logArea = textArea;
  }

  /**
   * Logs a new message to the status window.
   * @param message Message to log to the status window.
   */
  public static void log(String message) {
    if (logArea != null) {
      logArea.appendText(String.format("[%s] %s\n", getTimestamp(), message));
    }
  }

  /**
   * Clears the status window.
   */
  public static void clear() {
    if (logArea != null) {
      logArea.clear();
    }
  }

  /**
   * Returns the entire text of the status window.
   * @return the entire text of the status window.
   */
  public static String getText() {
    if (logArea != null) {
      return logArea.getText();
    }
    return "";
  }

  /**
   * Returns a timestamp of the current time for messages in the status window.
   * @return a timestamp of the current time for messages in the status window.
   */
  private static String getTimestamp() {
    return new Timestamp(new java.util.Date().getTime()).toString();
  }
}
