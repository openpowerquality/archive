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

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.openpowerquality.protocol.OpqPacket;
import org.openpowerquality.sim.netio.WebsocketClient;
import org.openpowerquality.sim.utils.Logger;
import org.openpowerquality.sim.views.Css;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.openpowerquality.sim.utils.DataConverter.bytesToInt;
import static org.openpowerquality.sim.utils.DataConverter.bytesToLong;
import static org.openpowerquality.sim.utils.DataConverter.doubleToBytes;
import static org.openpowerquality.sim.utils.DataConverter.intToBytes;
import static org.openpowerquality.sim.utils.DataConverter.isValidBytesInt;
import static org.openpowerquality.sim.utils.DataConverter.isValidBytesLong;
import static org.openpowerquality.sim.utils.DataConverter.isValidDouble;
import static org.openpowerquality.sim.utils.DataConverter.isValidInt;
import static org.openpowerquality.sim.utils.DataConverter.isValidLong;
import static org.openpowerquality.sim.utils.DataConverter.longToBytes;

/**
 * Controller used for handling the interaction of the SendPacket view. Allows for the sending of individual packets.
 */
public class SendPacket {
  @FXML
  public GridPane sendPacket;

  @FXML
  public Button btnNow;

  @FXML
  public Button btnDisconnect;

  @FXML
  public Button btnConnect;

  // Websocket URL
  @FXML
  private TextField txtWebsocketUrl;

  // Fields for integer column
  @FXML
  private TextField intSequenceNumber;
  @FXML
  private TextField longDeviceId;
  @FXML
  private TextField longTimestamp;
  @FXML
  private TextField intBitfield;
  @FXML
  private TextField doubleFrequency;
  @FXML
  private TextField doubleVoltage;
  @FXML
  private TextField longAlertDuration;

  // Fields for byte column
  @FXML
  private TextField byteSequenceNumber;
  @FXML
  private TextField byteDeviceId;
  @FXML
  private TextField byteTimestamp;
  @FXML
  private TextField byteBitfield;
  @FXML
  private TextField byteFrequency;
  @FXML
  private TextField byteVoltage;
  @FXML
  private TextField byteAlertDuration;

  // Fields for choice box
  @FXML
  private ComboBox intType;

  private WebsocketClient client;

  /**
   * Attempts to make a new connection with a websocket server.
   */
  @FXML
  public void handleConnect() {
    if (client != null && !client.isClosed()) {
      client.close();
    }

    try {
      client = new WebsocketClient(txtWebsocketUrl.getText());
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  /**
   * Disconnects from a websocket server.
   */
  @FXML
  public void handleDisconnect() {
    if (client != null && !client.isClosed()) {
      client.close();
    }
  }

  /**
   * Sends a packet to a WS server if a WS connection is open and all fields are validated.
   */
  @FXML
  protected void handleSendPacket() {
    if (!canValidateFields()) {
      Logger.log("One of the form fields failed to validate.");
      return;
    }

    OpqPacket packet = new OpqPacket();

    // Grab values from forms
    OpqPacket.PacketType packetType = OpqPacket.PacketType.getType(intType.getItems().indexOf(intType.getValue()));
    packet.setType(packetType);
    packet.setSequenceNumber(Integer.parseInt(intSequenceNumber.getText()));
    packet.setDeviceId(Long.parseLong(longDeviceId.getText()));
    packet.setTimestamp(Long.parseLong(longTimestamp.getText()));
    packet.setBitfield(Integer.parseInt(intBitfield.getText()));

    // Manage the payload
    switch (packetType) {
      // Measurement
      case MEASUREMENT:
        packet.setMeasurement(
            Double.parseDouble(doubleFrequency.getText()),
            Double.parseDouble(doubleVoltage.getText()));
        break;
      case EVENT_FREQUENCY:
        packet.setEventValue(
            Double.parseDouble(doubleFrequency.getText()),
            Long.parseLong(longAlertDuration.getText()));
        break;
      case EVENT_VOLTAGE:
        packet.setEventValue(
            Double.parseDouble(doubleVoltage.getText()),
            Long.parseLong(longAlertDuration.getText()));
        break;
      case PING:
        packet.makePingType();
        break;
    }

    // Compute checksum
    packet.setChecksum();

    // Send packet
    client.send(packet);

  }

  /**
   * Converts value from bytes to an integer for fields which have a byte representation of an integer.
   *
   * @param event Event associated with this.
   */
  @FXML
  protected void handleIntToBytes(Event event) {
    TextField textField = (TextField) event.getSource();

    if (textField == intSequenceNumber) {
      byteSequenceNumber.setText(intToBytes(textField.getText()));
    }
    if (textField == intBitfield) {
      byteBitfield.setText(intToBytes(textField.getText()));
    }

    canValidateFields();
  }

  /**
   * Converts a long value into a string of bytes for fields which use a long.
   *
   * @param event Event associated with this.
   */
  @FXML
  protected void handleLongToBytes(Event event) {
    TextField textField = (TextField) event.getSource();

    if (textField == longDeviceId) {
      byteDeviceId.setText(longToBytes(textField.getText()));
    }
    if (textField == longTimestamp) {
      byteTimestamp.setText(longToBytes(textField.getText()));
    }
    if (textField == longAlertDuration) {
      byteAlertDuration.setText(longToBytes(textField.getText()));
    }

    canValidateFields();
  }

  /**
   * Converts bytes into an integer for fields which store integers as bytes.
   *
   * @param event Event associated with this.
   */
  @FXML
  protected void handleBytesToInt(Event event) {
    TextField textField = (TextField) event.getSource();

    if (textField == byteSequenceNumber) {
      intSequenceNumber.setText(bytesToInt(textField.getText()));
    }
    if (textField == byteBitfield) {
      intBitfield.setText(bytesToInt(textField.getText()));
    }

    canValidateFields();
  }

  /**
   * Converts bytes into a long for fields which store longs as bytes.
   *
   * @param event Event associated with this.
   */
  @FXML
  protected void handleBytesToLong(Event event) {
    TextField textField = (TextField) event.getSource();

    if (textField == byteDeviceId) {
      longDeviceId.setText(bytesToLong(textField.getText()));
    }
    if (textField == byteTimestamp) {
      longTimestamp.setText(bytesToLong(textField.getText()));
    }
    if (textField == byteAlertDuration) {
      longAlertDuration.setText(bytesToLong(textField.getText()));
    }

    canValidateFields();
  }

  /**
   * Converts a double value in the specified IEEE 754 byte format.
   * @param event Event associated with this action.
   */
  @FXML
  protected void handleDoubleToBytes(Event event) {
    TextField textField = (TextField) event.getSource();

    if (textField == doubleFrequency) {
      byteFrequency.setText(doubleToBytes(textField.getText()));
    }
    if (textField == doubleVoltage) {
      byteVoltage.setText(doubleToBytes(textField.getText()));
    }

    canValidateFields();
  }

  /**
   * Updates the timestamp with the current time (ms since epoch).
   */
  @FXML
  protected void handleUpdateTimestamp() {
    Long t = (new Date()).getTime();
    longTimestamp.setText(t.toString());
    byteTimestamp.setText(longToBytes(t.toString()));

    canValidateFields();
  }

  /**
   * Makes fields editable or non-editable depending on the type of packet type selected.
   */
  @FXML
  protected void handleTypeSelection() {
    List<TextField> fieldList = Arrays.asList(intSequenceNumber, byteSequenceNumber, intBitfield, byteBitfield,
                                              doubleFrequency, doubleVoltage,
                                              longAlertDuration, byteAlertDuration);
    Set<TextField> fields = new HashSet<>(fieldList);
    Set<TextField> tmp = new HashSet<>(fieldList);


    OpqPacket.PacketType packetType = OpqPacket.PacketType.getType(intType.getItems().indexOf(intType.getValue()));
    switch (packetType) {
      case MEASUREMENT:
        this.resetFields(fields);
        tmp.retainAll(Arrays.asList(longAlertDuration, byteAlertDuration));
        this.disableFields(tmp);
        break;
      case EVENT_FREQUENCY:
        this.resetFields(fields);
        tmp.retainAll(Arrays.asList(doubleVoltage));
        this.disableFields(tmp);
        break;
      case EVENT_VOLTAGE:
        this.resetFields(fields);
        tmp.retainAll(Arrays.asList(doubleFrequency));
        this.disableFields(tmp);
        break;
      case PING:
        this.resetFields(fields);
        this.disableFields(fields);
        break;
    }
  }

  /**
   * Removes CSS styling from passed in fields and sets them as editable.
   * @param fields TextFields to remove CSS from.
   */
  private void resetFields(Set<TextField> fields) {
    for (TextField field : fields) {
      Css.removeClass(Css.Style.INVALID_FIELD, field);
      Css.removeClass(Css.Style.NON_EDITABLE_FIELD, field);
      field.setEditable(true);
    }
  }

  /**
   * Sets the passed in fields to non-editable.
   * @param fields TextFields to set as non-editable.
   */
  private void disableFields(Set<TextField> fields) {
    for (TextField field : fields) {
      Css.addClass(Css.Style.NON_EDITABLE_FIELD, field);
      field.setEditable(false);
    }
  }

  /**
   * Performs form validation.
   * <p/>
   * This is tricky and kind of hacky. Since the fields represent different data types, we need to make sure that
   * they're the correct data type.
   *
   * @return true if all fields validate, false otherwise.
   */
  private boolean canValidateFields() {
    Set<TextField> didValidate = new HashSet<>();
    Set<TextField> didNotValidate = new HashSet<>();

    // validate integer fields
    for (TextField textField : Arrays.asList(intBitfield, intSequenceNumber)) {
      if (isValidInt(textField.getText())) {
        didValidate.add(textField);
      }
      else {
        didNotValidate.add(textField);
      }
    }

    // validate integer byte fields
    for (TextField textField : Arrays.asList(byteBitfield, byteSequenceNumber)) {
      if (isValidBytesInt(textField.getText())) {
        didValidate.add(textField);
      }
      else {
        didNotValidate.add(textField);
      }
    }

    // validate long fields
    for (TextField textField : Arrays.asList(longDeviceId, longTimestamp, longAlertDuration)) {
      if (isValidLong(textField.getText())) {
        didValidate.add(textField);
      }
      else {
        didNotValidate.add(textField);
      }
    }

    // validate long byte fields
    for (TextField textField : Arrays.asList(byteDeviceId, byteTimestamp, byteAlertDuration)) {
      if (isValidBytesLong(textField.getText())) {
        didValidate.add(textField);
      }
      else {
        didNotValidate.add(textField);
      }
    }

    // validate double fields
    for (TextField textField : Arrays.asList(doubleFrequency, doubleVoltage)) {
      if (isValidDouble(textField.getText())) {
        didValidate.add(textField);
      }
      else {
        didNotValidate.add(textField);
      }
    }

    // Mark invalid fields as invalid
    for (TextField textField : didNotValidate) {
      Css.addClass(Css.Style.INVALID_FIELD, textField);
    }


    // Make sure valid fields are not marked as invalid
    for (TextField textField : didValidate) {
      Css.removeClass(Css.Style.INVALID_FIELD, textField);
    }

    return didNotValidate.size() == 0;
  }
}
