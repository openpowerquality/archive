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

package org.openpowerquality.sim.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;

/**
 * Handles application specific actions such as menu items.
 */
public class Application {
  @FXML
  public TextArea logArea;

  @FXML
  public MenuItem menuClose;

  /**
   * Handles menu item for closing the program.
   */
  @FXML
  public void handleMenuClose() {
    Platform.exit();
  }
}
