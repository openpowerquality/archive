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

package tests;
/*
import org.junit.Before;
import org.junit.Test;
import org.openpowerquality.protocol.OpqPacketBinary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
  */
public class OpqPacketTests {
 /* private OpqPacketBinary opqPacket;

  @Before
  public void setup() {
    this.opqPacket = new OpqPacketBinary();
  }

  @Test
  public void testEmptyConstructor() {
    assertEquals(opqPacket.getHeader(), 0x00C0FFEE);
    assertEquals(opqPacket.getData().length, OpqPacketBinary.PACKET_SIZE);
  }

  @Test
  public void testHeader() {
    assertEquals(opqPacket.getHeader(), 0x00C0FFEE);
    assertArrayEquals(opqPacket.getDataPart(OpqPacketBinary.Protocol.HEADER), new byte[]{0x00, (byte)0xC0, (byte)0xFF, (byte)0xEE});
  }

  @Test
  public void testType() {
    for(OpqPacketBinary.PacketType packetType : OpqPacketBinary.PacketType.values()) {
      opqPacket = new OpqPacketBinary();
      opqPacket.setType(packetType);
      assertEquals(opqPacket.getType(), packetType);
    }
  }

  @Test
  public void testValidSequenceNumber() {
    opqPacket.setSequenceNumber(0);
    assertEquals(opqPacket.getSequenceNumber(), 0);
    opqPacket.setSequenceNumber(1);
    assertEquals(opqPacket.getSequenceNumber(), 1);
    opqPacket.setSequenceNumber(10000);
    assertEquals(opqPacket.getSequenceNumber(), 10000);
    opqPacket.setSequenceNumber(Integer.MAX_VALUE);
    assertEquals(opqPacket.getSequenceNumber(), Integer.MAX_VALUE);
  }

  @Test
  public void testDeviceId() {
    opqPacket.setDeviceId(0);
    assertEquals(opqPacket.getDeviceId(), 0);
    opqPacket.setDeviceId(1);
    assertEquals(opqPacket.getDeviceId(), 1);
    opqPacket.setDeviceId(10000);
    assertEquals(opqPacket.getDeviceId(), 10000);
    opqPacket.setDeviceId(Long.MAX_VALUE);
    assertEquals(opqPacket.getDeviceId(), Long.MAX_VALUE);
  }

  @Test
  public void testTimestamp() {
    opqPacket.setTimestamp(0);
    assertEquals(opqPacket.getTimestamp(), 0);
    opqPacket.setTimestamp(1);
    assertEquals(opqPacket.getTimestamp(), 1);
    opqPacket.setTimestamp(10000);
    assertEquals(opqPacket.getTimestamp(), 10000);
    opqPacket.setTimestamp(Long.MAX_VALUE);
    assertEquals(opqPacket.getTimestamp(), Long.MAX_VALUE);
  }

  @Test
  public void testBitfield() {
    opqPacket.setBitfield(0);
    assertEquals(opqPacket.getBitfield(), 0);
    opqPacket.setBitfield(1);
    assertEquals(opqPacket.getBitfield(), 1);
    opqPacket.setBitfield(10000);
    assertEquals(opqPacket.getBitfield(), 10000);
    opqPacket.setBitfield(Integer.MAX_VALUE);
    assertEquals(opqPacket.getBitfield(), Integer.MAX_VALUE);
  }

  @Test
  public void testPayload() {
    byte[] payload = {0x01, 0x02, 0x03, 0x04};
    opqPacket.setPayload(payload);
    assertEquals(opqPacket.getData().length, OpqPacketBinary.PACKET_SIZE + payload.length);
    assertArrayEquals(opqPacket.getPayload(), payload);
    assertArrayEquals(opqPacket.getDataPart(OpqPacketBinary.Protocol.PAYLOAD), payload);
    assertEquals(opqPacket.getPayloadSize(), 4);
    assertArrayEquals(opqPacket.getDataPart(OpqPacketBinary.Protocol.PAYLOAD_SIZE), new byte[]{0x00, 0x00, 0x00, 0x04});
  }

  @Test
  public void testMeasurementVoltage() {
    opqPacket.setMeasurement(0, 0);
    assertEquals(opqPacket.getVoltage(), 0, 0);
    opqPacket.setMeasurement(0, 1.0);
    assertEquals(opqPacket.getVoltage(), 1.0, 0);
    opqPacket.setMeasurement(0, 10000.0);
    assertEquals(opqPacket.getVoltage(), 10000.0, 0);
    opqPacket.setMeasurement(0, Double.MAX_VALUE);
    assertEquals(opqPacket.getVoltage(), Double.MAX_VALUE, 0);
  }

  @Test
  public void testMeasurementFrequency() {
    opqPacket.setMeasurement(0, 0);
    assertEquals(opqPacket.getFrequency(), 0, 0);
    opqPacket.setMeasurement(1.00, 0);
    assertEquals(opqPacket.getFrequency(), 1.0, 0);
    opqPacket.setMeasurement(10000.0, 0);
    assertEquals(opqPacket.getFrequency(), 10000.0, 0);
    opqPacket.setMeasurement(Double.MAX_VALUE, 0);
    assertEquals(opqPacket.getFrequency(), Double.MAX_VALUE, 0);
  }

  @Test
  public void testMeasurementAlertValue() {
    opqPacket.setEventValue(0, 0);
    assertEquals(opqPacket.getEventValue(), 0, 0);
    opqPacket.setEventValue(1.0, 0);
    assertEquals(opqPacket.getEventValue(), 1.0, 0);
    opqPacket.setEventValue(10000.0, 0);
    assertEquals(opqPacket.getEventValue(), 10000.0, 0);
    opqPacket.setEventValue(Double.MAX_VALUE, 0);
    assertEquals(opqPacket.getEventValue(), Double.MAX_VALUE, 0);
  }

  @Test
  public void testMeasurementAlertDuration() {
    opqPacket.setEventValue(0, 0);
    assertEquals(opqPacket.getEventDuration(), 0);
    opqPacket.setEventValue(0, 1);
    assertEquals(opqPacket.getEventDuration(), 1);
    opqPacket.setEventValue(0, 10000);
    assertEquals(opqPacket.getEventDuration(), 10000);
    opqPacket.setEventValue(0, Long.MAX_VALUE);
    assertEquals(opqPacket.getEventDuration(), Long.MAX_VALUE);
  }

  @Test
  public void testMeasurementFull() {
    opqPacket.setEventValue(0, 0);
    assertEquals(opqPacket.getEventDuration(), 0);
    assertEquals(opqPacket.getEventValue(), 0.0, 0);

    opqPacket.setEventValue(1.0, 1);
    assertEquals(opqPacket.getEventDuration(), 1);
    assertEquals(opqPacket.getEventValue(), 1.0, 0);

    opqPacket.setEventValue(10000.0, 10000);
    assertEquals(opqPacket.getEventDuration(), 10000);
    assertEquals(opqPacket.getEventValue(), 10000.0, 0);

    opqPacket.setEventValue(Double.MAX_VALUE, Long.MAX_VALUE);
    assertEquals(opqPacket.getEventDuration(), Long.MAX_VALUE);
    assertEquals(opqPacket.getEventValue(), Double.MAX_VALUE, 0);
  }

  @Test
  public void testChecksumOnEmpty() {
    byte[] data = opqPacket.getData();
    int total = data[0] + data[1] + data[2] + data[3];
    opqPacket.setChecksum();
    assertEquals(opqPacket.getChecksum(), total);
  }

  @Test
  public void testStringConstructor() {
    opqPacket.setType(OpqPacketBinary.PacketType.EVENT_FREQUENCY);
    opqPacket.setSequenceNumber(2);
    opqPacket.setDeviceId(0xabcd);
    opqPacket.setTimestamp(12);
    opqPacket.setBitfield(13);
    opqPacket.setEventValue(59.111, 123);
    opqPacket.computeChecksum();

    OpqPacketBinary other = new OpqPacketBinary(opqPacket.getBase64Encoding());
    assertArrayEquals(other.getData(), opqPacket.getData());
  }

  @Test
  public void testCompareToEquals() {
    OpqPacketBinary other = new OpqPacketBinary();
    opqPacket.setTimestamp(1);
    other.setTimestamp(1);
    assertEquals(opqPacket.compareTo(other), 0);
  }

  @Test
  public void testCompareToGreaterThan() {
    OpqPacketBinary other = new OpqPacketBinary();
    opqPacket.setTimestamp(10);
    other.setTimestamp(1);
    assertTrue(opqPacket.compareTo(other) > 0);
  }

  @Test
  public void testCompareToLessThan() {
    OpqPacketBinary other = new OpqPacketBinary();
    opqPacket.setTimestamp(1);
    other.setTimestamp(10);
    assertTrue(opqPacket.compareTo(other) < 0);
  }

  @Test
  public void testEqualsReflexive() {
    assertTrue(opqPacket.equals(opqPacket));
  }

  @Test
  public void testEqualsSymmetric() {
    OpqPacketBinary other = new OpqPacketBinary();
    assertTrue(opqPacket.equals(other));
    assertTrue(other.equals(opqPacket));
  }

  @Test
  public void testEqualsTransitive() {
    OpqPacketBinary otherA = new OpqPacketBinary();
    OpqPacketBinary otherB = new OpqPacketBinary();
    assertTrue(opqPacket.equals(otherA));
    assertTrue(otherA.equals(otherB));
    assertTrue(opqPacket.equals(otherB));
  }

  @Test
  public void testHashCodeConsistent() {
    assertEquals(opqPacket.hashCode(), opqPacket.hashCode());
  }

  @Test
  public void testHashCodeEquals() {
    OpqPacketBinary other = new OpqPacketBinary();
    assertTrue(opqPacket.equals(other));
    assertEquals(opqPacket.hashCode(), other.hashCode());
  }
        */
}
