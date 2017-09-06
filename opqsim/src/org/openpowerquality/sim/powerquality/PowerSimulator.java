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

package org.openpowerquality.sim.powerquality;

import org.openpowerquality.protocol.OpqPacket;

import java.util.Random;

/**
 * Singleton class that provides simulated power readings, measurements, and events.
 */
public class PowerSimulator {
  private static final Random random = new Random();

  /**
   * Returns a valid good frequency between [59 - 61] Hz.
   * @return A valid good frequency between [59 - 61] Hz.
   */
  private static double getGoodFrequency() {
    double jitter = random.nextDouble();
    double frequency = 60;

    if (random.nextBoolean()) {
      jitter *= -1;
    }

    return frequency + jitter;
  }

  /**
   * Returns a valid good voltage between [117 - 123] volts.
   * @return a valid good voltage between [117 - 123] volts.
   */
  private static double getGoodVoltage() {
    double jitter = random.nextInt(3) + random.nextDouble();
    double voltage = 120;

    if (random.nextBoolean()) {
      jitter *= -1;
    }

    return voltage + jitter;
  }

  /**
   * Returns a valid "event" frequency within the ranges [48 - 58] and [62 - 72] Hz.
   * @return a valid "event" frequency within the ranges [48 - 58] and [62 - 72] Hz.
   */
  private static double getBadFrequency() {
    double jitter = 2 + random.nextInt(10) + random.nextDouble();
    double frequency = 60;

    if (random.nextBoolean()) {
      jitter *= -1;
    }

    return frequency + jitter;
  }

  /**
   * Returns a valid "event" voltage within the ranges [95 - 110] and [130 - 145] volts.
   * @return a valid "event" voltage within the ranges [95 - 110] and [130 - 145] volts.
   */
  private static double getBadVoltage() {
    double jitter = 10 + random.nextInt(15) + random.nextDouble();
    double voltage = 120;

    if (random.nextBoolean()) {
      jitter *= -1;
    }

    return voltage + jitter;
  }

  /**
   * Returns a random duration (in ms) between [0 - 1000] ms.
   * @return a random duration (in ms) between [0 - 1000] ms.
   */
  private static Long getRandomEventDuration() {
    return (long) random.nextInt(1000);
  }

  /**
   * Returns a mostly finished OPQ packet depending on the packet type.
   * Depending on the packet type, either a good measurement is created, or a frequency or voltage event is created.
   * This packet returned by this method contains everything except for the device ID, timestamp, and checksum.
   * @param packetType The type of packet to generate.
   * @return The partial opq packet.
   */
  public static OpqPacket generatePartialPacket(OpqPacket.PacketType packetType) {
    OpqPacket packet = new OpqPacket();

    packet.setType(packetType);

    switch (packetType) {
      case MEASUREMENT:
        packet.setMeasurement(getGoodFrequency(), getGoodVoltage());
        break;
      case EVENT_FREQUENCY:
        packet.setEventValue(getBadFrequency(), getRandomEventDuration());
        break;
      case EVENT_VOLTAGE:
        packet.setEventValue(getBadVoltage(), getRandomEventDuration());
        break;
      case EVENT_DEVICE:
        break;
    }

    return packet;
  }

}
