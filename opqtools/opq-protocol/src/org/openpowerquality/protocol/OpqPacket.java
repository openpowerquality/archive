package org.openpowerquality.protocol;

import java.util.Arrays;

public class OpqPacket {
  public Integer magicWord;
  public PacketType packetType;
  public Long deviceId;
  public String deviceKey;
  public Long timestamp;
  public Long duration;
  public Integer checksum;
  public Double frequency;
  public Double voltage;
  public Integer payloadSize;
  public Double[] payload;

  /**
   * Enumerates the different packet types associated with this protocol.
   */
  public enum PacketType {
    EVENT_HEARTBEAT (0, "Heartbeat Event"),
    EVENT_FREQUENCY (1, "Frequency Event"),
    EVENT_VOLTAGE   (2, "Voltage Event"),
    EVENT_DEVICE    (3, "Device Event");

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

  @Override
  public String toString() {
    return String.format("magicWord %s\n" +
                         "packetType %s\n" +
                         "deviceId %s\n" +
                         "deviceKey %s\n" +
                         "timestamp %s\n" +
                         "duration %s\n" +
                         "checksum %s\n" +
                         "frequency %s\n" +
                         "voltage %s\n" +
                         "payloadSize %s\n" +
                         "payload %s",
                         magicWord,
                         packetType,
                         deviceId,
                         deviceKey,
                         timestamp,
                         duration,
                         checksum,
                         frequency,
                         voltage,
                         payloadSize,
                         Arrays.toString(payload));
  }
}
