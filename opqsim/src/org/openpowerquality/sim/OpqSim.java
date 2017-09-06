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


package org.openpowerquality.sim;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.openpowerquality.sim.utils.Logger;

/**
 * Provides an entry point for this simulator.
 */
public class OpqSim extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Start the application.
   * @param primaryStage the primaryStage.
   * @throws Exception any exceptions thrown from this application.
   */
  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("views/opq-sim.fxml"));
    Scene scene = new Scene(root, 1000, 800);
    scene.getStylesheets().add("org/openpowerquality/sim/views/opq-sim.css");

    // Set up the logging area
    Logger.setLogArea((TextArea) root.lookup("#logArea"));
    primaryStage.setTitle("opq-sim");
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}
