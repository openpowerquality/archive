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

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.openpowerquality.protocol.OpqPacket;
import org.openpowerquality.sim.threaded.StressTestTask;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StressTestSimulation {
  @FXML
  public Button btnStopStressTest;

  @FXML
  public Button btnStartStressTest;

  @FXML
  public Button btnRemoveDeviceId;

  @FXML
  public Button btnAddDevice;

  // Websocket URL
  @FXML
  private TextField txtWebsocketAddress;

  // Device Id Fields
  @FXML
  private TextField txtDeviceId;
  @FXML
  private ListView<String> lstDeviceIds;

  // Packet Types
  @FXML
  private CheckBox chkMeasurement;
  @FXML
  private CheckBox chkEventFrequency;
  @FXML
  private CheckBox chkEventVoltage;
  @FXML
  private CheckBox chkEventDevice;

  // Packet Statistics
  @FXML
  private TextField txtPacketsPerSecond;
  @FXML
  private TextField txtEventFrequency;

  // Task Works
  private List<StressTestTask> tasks;

  /**
   * Adds a device to the list of devices used in the simulation.
   */
  @FXML
  public void handleAddDevice() {
    String device = txtDeviceId.getText();

    if (device.length() > 0) {
      lstDeviceIds.getItems().add(txtDeviceId.getText());
      txtDeviceId.setText("");
      txtDeviceId.requestFocus();
    }
  }

  /**
   * Removes the selected device from the devices used in the simulation.
   */
  @FXML
  public void handleRemoveDevice() {
    String selected = lstDeviceIds.getFocusModel().getFocusedItem();

    if (selected != null) {
      lstDeviceIds.getItems().remove(selected);
    }
  }

  /**
   * Starts the stress test simulation.
   *
   * A thread is created for each device in the simulation using the parameters passed in via the TextFields for this
   * simulation.
   */
  @FXML
  public void handleStartStressTest() {
    String websocketAddress = txtWebsocketAddress.getText();
    List<String> devices = lstDeviceIds.getItems();
    System.out.println(devices);
    List<OpqPacket.PacketType> packetTypes = getPacketTypes();
    System.out.println(packetTypes);
    Integer packetsPerSecond = Integer.parseInt(txtPacketsPerSecond.getText());
    Double eventFrequency = Double.parseDouble(txtEventFrequency.getText());

    if (tasks == null) {
      tasks = new LinkedList<>();
    }

    for (String device : devices) {
      StressTestTask
          stressTestTask = new StressTestTask(websocketAddress, device, packetTypes, packetsPerSecond, eventFrequency);
      tasks.add(stressTestTask);
      Thread thread = new Thread(stressTestTask);
      thread.setDaemon(true);
      thread.start();
    }
  }

  /**
   * Returns a list of selected packet types for this simulation.
   * @return A list of selected packet types for this simulation.
   */
  private List<OpqPacket.PacketType> getPacketTypes() {
    List<OpqPacket.PacketType> packetTypes = new ArrayList<>();

    if (chkMeasurement.isSelected()) {
      packetTypes.add(OpqPacket.PacketType.MEASUREMENT);
    }
    if (chkEventFrequency.isSelected()) {
      packetTypes.add(OpqPacket.PacketType.EVENT_FREQUENCY);
    }
    if (chkEventVoltage.isSelected()) {
      packetTypes.add(OpqPacket.PacketType.EVENT_VOLTAGE);
    }
    /*if(chkEventDevice.isSelected()) {
      packetTypes.add(OpqPacket.PacketType.EVENT_DEVICE);
    }*/

    return packetTypes;
  }

  /**
   * Stops this simulation by cancelling each thread created by this simulation.
   */
  @FXML
  public void handleStopStressTest() {
    for (StressTestTask task : tasks) {
      task.cancel();
    }
  }
}
