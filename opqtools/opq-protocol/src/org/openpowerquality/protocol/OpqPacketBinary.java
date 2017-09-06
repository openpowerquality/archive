/*
  This file is part of opq-tools.

  opq-tools is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  opq-tools is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with opq-tools. If not, see <http://www.gnu.org/licenses/>.

  Copyright 2014 Anthony Christe
*/

package org.openpowerquality.protocol;

import org.openpowerquality.protocol.exceptions.InvalidByteSizeException;
import org.openpowerquality.protocol.exceptions.NegativeValueException;
import org.openpowerquality.protocol.exceptions.OpqPacketException;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

/**
 * Provides a reference implementation of the OPQ communication protocol. Protocol details can be found at
 * https://github.com/openpowerquality/opq/wiki/OPQ-Communication-Protocol
 */
public class OpqPacketBinary implements Comparable<OpqPacketBinary> {
  /**
   * Used for header of packet.
   */
  public static final int MAGIC_WORD = 0x00C0FFEE;

  /**
   * Size of the packet without the payload.
   */
  public static final int PACKET_SIZE = 56;

  /**
   * Stores the start and stop indices for each portion of the this packet.
   */
  public enum Protocol {
    HEADER(0, 3),
    TYPE(4, 7),
    SEQUENCE_NUMBER(8, 11),
    DEVICE_ID(12, 19),
    TIMESTAMP(20, 27),
    BITFIELD(28, 31),
    PAYLOAD_SIZE(32, 35),
    RESERVED(36, 51),
    CHECKSUM(52, 55),
    PAYLOAD(56, -1);

    /**
     * The starting index in the packet for a particular data part.
     */
    private final int startByte;

    /**
     * The stopping index in the packet for a particular data part.
     */
    private final int stopByte;

    Protocol(int startByte, int endByte) {
      this.startByte = startByte;
      this.stopByte = endByte;
    }

    public int getStartByte() {
      return this.startByte;
    }

    public int getStopByte() {
      return this.stopByte;
    }

    public int getSize() {
      return this.stopByte - this.startByte + 1;
    }
  }

  /**
   * Enumerates the different packet types associated with this protocol.
   */
  public enum PacketType {
    EVENT_HEARTBEAT (0, "Heartbeat Event"),
    EVENT_FREQUENCY (1, "Frequency Event"),
    EVENT_VOLTAGE   (2, "Voltage Event"),
    EVENT_DEVICE    (3, "Device Event"),
    SETTING         (4, "Setting"),
    MONITOR         (5, "Monitor");

    private final int val;
    private final String name;

    PacketType(final int val, final String name) {
      this.val = val;
      this.name = name;
    }

    public int val() {
      return this.val;
    }

    public String getName() {
      return this.name;
    }

    /**
     * Given the integer value, return the packet type.
     *
     * @param i Value associated with packet type.
     *
     * @return The PacketType associated with the given integer.
     */
    public static PacketType getType(int i) {
      for (PacketType packetType : values()) {
        if (packetType.val() == i) {
          return packetType;
        }
      }
      return null;
    }
  }

  public enum Setting {
    SET_DEVICE_ID,
    FREQUENCY_BOUNDS,
    VOLTAGE_BOUNDS;
  }

  /**
   * Packet data.
   */
  private byte[] data;

  private Map<Setting, String> settingsMap;

  /**
   * Creates an empty packet.
   */
  public OpqPacketBinary() {
    this.data = new byte[PACKET_SIZE];
    this.setHeader();
  }

  /**
   * Creates a packet from a string encoded in base 64.
   *
   * @param encodedPacket String encoded in base 64.
   */
  public OpqPacketBinary(String encodedPacket) {
    this.data = DatatypeConverter.parseBase64Binary(encodedPacket);
  }

  /**
   * Returns a specific part of the packet.
   *
   * @param protocol Enum which determines which part of the data to return.
   *
   * @return A copy of the subarray representing the pieces of data specified by the protocol.
   */
  public byte[] getDataPart(final Protocol protocol) {
    int stopByte = protocol.equals(Protocol.PAYLOAD) ? data.length : protocol.getStopByte() + 1;
    return Arrays.copyOfRange(data, protocol.getStartByte(), stopByte);
  }

  /**
   * Updates part of the packet based on which part of the packet is being modified.
   *
   * @param protocol The enum specifying the part of the packet being modified.
   * @param data     The data to update in the packet.
   */
  public void setDataPart(final Protocol protocol, byte[] data) {
    byte[] newData;

    // If we're changing the payload, we need to resize the array to hold the payload data.
    if (protocol.equals(Protocol.PAYLOAD)) {
      newData = new byte[PACKET_SIZE + data.length];
      System.arraycopy(this.data, 0, newData, 0, PACKET_SIZE);
      System.arraycopy(data, 0, newData, Protocol.PAYLOAD.getStartByte(), data.length);
      this.data = newData;
    }
    else {
      System.arraycopy(data, 0, this.data, protocol.getStartByte(), data.length);
    }
  }

  /**
   * Computes a checksum over the data by summing all fields minus the checksum.
   *
   * @return The computed checksum.
   */
  public int computeChecksum() {
    int sum = 0;

    for (int i = 0; i < Protocol.CHECKSUM.getStartByte(); i++) {
      sum += data[i];
    }

    for (int i = Protocol.CHECKSUM.getStopByte() + 1; i < data.length; i++) {
      sum += data[i];
    }

    return sum;
  }

  /**
   * Return this packet as an encoded String in base 64.
   *
   * @return Base 64 encoded String.
   */
  public String getBase64Encoding() {
    try {
      return DatatypeConverter.printBase64Binary(this.data);
    }
    catch (IllegalArgumentException e) {
      throw new OpqPacketException("Base 64 string could not be decoded", e.getCause());
    }

  }

  /**
   * Returns a copy of the raw data of this packet.
   *
   * @return A copy of the raw data of this packet.
   */
  public byte[] getData() {
    return this.data.clone();
  }

  /**
   * Returns the header.
   *
   * @return The header.
   */
  public int getHeader() {
    return bytesToInt(this.getDataPart(Protocol.HEADER));
  }

  /**
   * Sets the header.
   * <p/>
   * Note that since the header should never change, this method simply sets the header to whatever is stored as the
   * MAGIC_WORD.
   */
  public void setHeader() {
    this.setDataPart(Protocol.HEADER, intToBytes(MAGIC_WORD));
  }

  /**
   * Returns the type of this packet.
   *
   * @return The type of this paclet.
   */
  public PacketType getType() {
    return PacketType.getType(bytesToInt(this.getDataPart(Protocol.TYPE)));
  }

  /**
   * Sets the type of this packet.
   *
   * @param packetType The type of this packet.
   */
  public void setType(PacketType packetType) {
    this.setDataPart(Protocol.TYPE, intToBytes(packetType.val()));
  }

  public void makePingType() {
  //  this.setType(PacketType.PING);
  }

  /**
   * Returns the sequence number of this packet.
   *
   * @return The sequence number of this packet.
   */
  public int getSequenceNumber() {
    return bytesToInt(this.getDataPart(Protocol.SEQUENCE_NUMBER));
  }

  /**
   * Sets the sequence number of this packet.
   *
   * @param sequenceNumber The squence number of this packet.
   */
  public void setSequenceNumber(int sequenceNumber) {
    if(sequenceNumber < 0) {
      throw new NegativeValueException(sequenceNumber);
    }

    this.setDataPart(Protocol.SEQUENCE_NUMBER, intToBytes(sequenceNumber));
  }

  /**
   * Returns the device id of this packet.
   *
   * @return The device id of this packet.
   */
  public long getDeviceId() {
    return bytesToLong(this.getDataPart(Protocol.DEVICE_ID));
  }

  /**
   * Sets the device id of this packet.
   *
   * @param deviceId The device id of this packet.
   */
  public void setDeviceId(long deviceId) {
    if(deviceId < 0) {
      throw new NegativeValueException(deviceId);
    }

    this.setDataPart(Protocol.DEVICE_ID, longToBytes(deviceId));
  }

  /**
   * Returns the timestamp of this packet.
   *
   * @return The timestamp of this packet.
   */
  public long getTimestamp() {
    return bytesToLong(this.getDataPart(Protocol.TIMESTAMP));
  }

  /**
   * Sets the timestamp of this packet.
   *
   * @param timestamp The timestamp of this packet (ms since epoch).
   */
  public void setTimestamp(long timestamp) {
    if(timestamp < 0) {
      throw new NegativeValueException(timestamp);
    }

    this.setDataPart(Protocol.TIMESTAMP, longToBytes(timestamp));
  }

  /**
   * Returns the bitfield of this packet.
   *
   * @return the bitfield of this packet.
   */
  public int getBitfield() {
    return bytesToInt(this.getDataPart(Protocol.BITFIELD));
  }

  /**
   * Sets the bitfield of this packet.
   *
   * @param bitField The bitfield of this packet.
   */
  public void setBitfield(int bitField) {
    this.setDataPart(Protocol.BITFIELD, intToBytes(bitField));
  }


  /**
   * Returns the payload size of this packet.
   *
   * @return The payload size of this packet.
   */
  public int getPayloadSize() {
    return bytesToInt(this.getDataPart(Protocol.PAYLOAD_SIZE));
  }

  /**
   * Sets the payload size of this packet.
   *
   * @param payloadSize The payload size of this packet.
   */
  public void setPayloadSize(int payloadSize) {
    if(payloadSize < 0) {
      throw new NegativeValueException(payloadSize);
    }

    this.setDataPart(Protocol.PAYLOAD_SIZE, intToBytes(payloadSize));
  }

  /**
   * Returns the checksum of this packet.
   *
   * @return The checksum of this packet.
   */
  public int getChecksum() {
    return bytesToInt(this.getDataPart(Protocol.CHECKSUM));
  }

  /**
   * Sets the checksum of this packet.
   * <p/>
   * Note that the checksum can't be set manually. Instead, the computeChecksum method is called.
   */
  public void setChecksum() {
    this.setDataPart(Protocol.CHECKSUM, intToBytes(this.computeChecksum()));
  }

  /**
   * Returns the payload of this packet.
   *
   * @return The payload of this packet.
   */
  public byte[] getPayload() {
    return this.getDataPart(Protocol.PAYLOAD);
  }

  /**
   * Sets the payload of this packet.
   *
   * @param payload The payload of this packet.
   */
  public void setPayload(byte[] payload) {
    this.setPayloadSize(payload.length);
    this.setDataPart(Protocol.PAYLOAD, payload);
  }

  public double[] getRawPowerData() {
    // The first 16 bytes represent the event value and the event duration
    int dataSize = (this.getPayloadSize() - 16) / 8;

    if(dataSize <= 0) {
      return new double[0];
    }

    byte[] payload = this.getPayload();
    double[] rawPowerData = new double[dataSize];
    System.out.println("payload size:" + this.getPayloadSize());
    int i = 0;
    for(int j = 16; j < dataSize * 8; j += 8) {
      rawPowerData[i++] = bytesToDouble(Arrays.copyOfRange(payload, j, j + 8));
    }

    System.out.println("size of power data: " + rawPowerData.length);
    return rawPowerData;
  }

  public void setRawPowerData(double[] rms) {

  }

  /**
   * Returns the voltage of this measurement.
   *
   * @return The voltage of this measurement.
   */
  public double getVoltage() {
    byte[] payload = this.getPayload();
    byte[] voltage = Arrays.copyOfRange(payload, 8, payload.length);

    if(payload.length < 16) {
      throw new InvalidByteSizeException(payload.length, 16);
    }
    if(voltage.length < 8) {
      throw new InvalidByteSizeException(voltage.length, 8);
    }

    return bytesToDouble(voltage);
  }

  /**
   * Returns the frequency of this measurement.
   *
   * @return The frequency of this measurement.
   */
  public double getFrequency() {
    byte[] payload = this.getPayload();
    byte[] frequency = Arrays.copyOfRange(payload, 0, 8);

    if(payload.length < 16) {
      throw new InvalidByteSizeException(payload.length, 16);
    }
    if(frequency.length < 8) {
      throw new InvalidByteSizeException(frequency.length, 8);
    }

    return bytesToDouble(frequency);
  }

  /**
   * Returns the value of this event.
   * @return the value of this event.
   */
  public double getEventValue() {
    byte[] payload = this.getPayload();
    byte[] eventValue = Arrays.copyOfRange(payload, 0, 8);

    if(payload.length < 16) {
      throw new InvalidByteSizeException(payload.length, 16);
    }
    if(eventValue.length < 8) {
      throw new InvalidByteSizeException(eventValue.length, 8);
    }

    return bytesToDouble(eventValue);
  }

  /**
   * Returns the duration of this event (in ms).
   * @return The duration of this event (in ms).
   */
  public long getEventDuration() {
    byte[] payload = this.getPayload();
    byte[] durationValue = Arrays.copyOfRange(payload, 8, 16);

    if(payload.length < 16) {
      throw new InvalidByteSizeException(payload.length, 16);
    }
    if(durationValue.length < 8) {
      throw new InvalidByteSizeException(durationValue.length, 8);
    }

    return bytesToLong(durationValue);
  }

  /**
   * Sets the value of this event.
   *
   * @param value Value of this event.
   * @param duration The duration of this event (in ms).
   */
  public void setEventValue(double value, long duration) {
    if(value < 0 || duration < 0) {
      throw new OpqPacketException("value and duration must be non-negative");
    }

    byte[] eventData = doubleToBytes(value);
    byte[] durationData = longToBytes(duration);
    byte[] eventValue = new byte[16];

    if(eventData.length < 8) {
      throw new InvalidByteSizeException(eventData.length, 8);
    }
    if(durationData.length < 8) {
      throw new InvalidByteSizeException(durationData.length, 8);
    }

    System.arraycopy(eventData, 0, eventValue, 0, eventData.length);
    System.arraycopy(durationData, 0, eventValue, 8, durationData.length);

    this.setPayload(eventValue);
  }

  /**
   * Creates the byte sequence payload for setting the measurement.
   *
   * @param frequency - The frequency of this measurement.
   * @param voltage   - The voltage of this measurement.
   */
  public void setMeasurement(double frequency, double voltage) {
    if(frequency < 0 || voltage < 0) {
      throw new OpqPacketException("Values must be non-negative");
    }

    byte[] frequencyData = doubleToBytes(frequency);
    byte[] voltageData = doubleToBytes(voltage);
    byte[] measurement = new byte[16];

    if(frequencyData.length < 8) {
      throw new InvalidByteSizeException(frequencyData.length, 8);
    }
    if(voltageData.length < 8) {
      throw new InvalidByteSizeException(frequencyData.length, 8);
    }

    System.arraycopy(frequencyData, 0, measurement, 0, frequencyData.length);
    System.arraycopy(voltageData, 0, measurement, 8, voltageData.length);
    this.setPayload(measurement);
  }

  public void addSetting(Setting setting, String value) {
    settingsMap.put(setting, value);
  }

  public void setSettings() {
    String settingsStr = "";
    for(Setting k : settingsMap.keySet()) {
      settingsStr += k + "," + settingsMap.get(k) + ",";
    }
    settingsStr = settingsStr.substring(0, settingsStr.length() - 1);
    setPayload(settingsStr.getBytes());
  }

  private byte[] reverseByteArray(byte[] bs) {
    byte[] result = new byte[bs.length];
    int j = result.length - 1;
    for(int i = 0; i < bs.length; i++) {
      result[j--] = bs[i];
    }
    return result;
  }

  public void reverseBytes() {
    for(Protocol protocol : Protocol.values()) {
      setDataPart(protocol, reverseByteArray(getDataPart(protocol)));
    }
  }

  /**
   * Convert an array of bytes into an integer.
   *
   * @param b Array of bytes (must be of length 4).
   *
   * @return An integer representing the array of bytes.
   */
  public static int bytesToInt(byte[] b) {
    if(b.length != 4) {
      throw new InvalidByteSizeException(b.length, 4);
    }
    return ByteBuffer.wrap(b).getInt();
  }

  /**
   * Converts an array of bytes into a long.
   *
   * @param b Array of bytes (must be length 8)
   *
   * @return A long representing the array of bytes.
   */
  public static long bytesToLong(byte[] b) {
    if(b.length != 8) {
      throw new InvalidByteSizeException(b.length, 8);
    }
    return ByteBuffer.wrap(b).getLong();
  }

  /**
   * Converts an array of bytes into a double.
   * @param b Arrays of bytes (must be of length 8).
   * @return A double representing the array of bytes.
   */
  public static double bytesToDouble(byte[] b) {
    if(b.length != 8) {
      throw new InvalidByteSizeException(b.length, 8);
    }
    return ByteBuffer.wrap(b).getDouble();
  }

  /**
   * Converts an integer into an array of bytes.
   *
   * @param i The integer to convert.
   *
   * @return The byte representation of the integer.
   */
  public static byte[] intToBytes(int i) {
    return ByteBuffer.allocate(4).putInt(i).array();
  }

  /**
   * Converts a long into an array of bytes.
   *
   * @param i The long to convert.
   *
   * @return The byte representation of the long.
   */
  public static byte[] longToBytes(long i) {
    return ByteBuffer.allocate(8).putLong(i).array();
  }

  /**
   * Converts a long into an array of bytes.
   * @param value The double value to get an array of bytes from.
   * @return The byte representation of the long.
   */
  public static byte[] doubleToBytes(double value) {
    return ByteBuffer.allocate(8).putDouble(value).array();
  }

  /**
   * Compares two packets by their timestamps.
   *
   * @param opqPacket The other OpqPacket.
   *
   * @return The comparison between the two timestamps.
   */
  @Override
  public int compareTo(OpqPacketBinary opqPacket) {
    return ((Long) this.getTimestamp()).compareTo(opqPacket.getTimestamp());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof OpqPacketBinary)) {
      return false;
    }

    OpqPacketBinary opqPacket = (OpqPacketBinary) o;
    return Arrays.equals(this.data, opqPacket.getData());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.data);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Field\tValue\tData\n");
    sb.append("header: " + this.getHeader() + " " + Arrays.toString(getDataPart(Protocol.HEADER)) + "\n");
    sb.append("type: " + this.getType() + " " + Arrays.toString(getDataPart(Protocol.TYPE)) + "\n");
    sb.append("seq #: " + this.getSequenceNumber() + " " + Arrays.toString(getDataPart(Protocol.SEQUENCE_NUMBER)) + "\n");
    sb.append("device #: " + this.getDeviceId() + " " +  Arrays.toString(getDataPart(Protocol.DEVICE_ID)) + "\n");
    sb.append("timestamp: " + this.getTimestamp() + " " +  Arrays.toString(getDataPart(Protocol.TIMESTAMP)) + "\n");
    sb.append("bitfield: " + this.getBitfield() + " " +  Arrays.toString(getDataPart(Protocol.BITFIELD)) + "\n");
    sb.append("payload size: " + this.getPayloadSize() + " " +  Arrays.toString(getDataPart(Protocol.PAYLOAD_SIZE)) + "\n");
    sb.append("reserved: " + Arrays.toString(getDataPart(Protocol.RESERVED)) + "\n");
    sb.append("checksum: " + this.getChecksum() + " " +  Arrays.toString(getDataPart(Protocol.CHECKSUM)) + "\n");
    sb.append("payload: " + Arrays.toString(this.getPayload()) + "\n");

    return sb.toString();
  }
}
