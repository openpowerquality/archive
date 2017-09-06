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

package org.openpowerquality.sim.threaded;

import javafx.concurrent.Task;
import org.openpowerquality.protocol.OpqPacket;
import org.openpowerquality.sim.netio.WebsocketClient;
import org.openpowerquality.sim.powerquality.PowerSimulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Task representing a single thread for the stress test simulation.
 */
public class StressTestTask extends Task<Void> {
  /**
   * The websocket server address for this thread.
   */
  private final String websocketAddress;

  /**
   * The device id to associate with this thread.
   */
  private final String deviceId;

  /**
   * The packet types that this thread should generate.
   */
  private final List<OpqPacket.PacketType> packetTypes;

  /**
   * The number of packets per second this thread should generate.
   */
  private final Integer packetsPerSecond;

  /**
   * The frequency that event packets should be generated.
   * Acceptable ranges are between [0 - 1].
   * I.e. a value of .35 would say that 35% of the packets should be an event type packet.
   */
  private final Double eventFrequency;


  /**
   * Random number generator.
   */
  private final Random random;

  /**
   * Constructs a not-started thread for the stress test simulation.
   * @param websocketAddress The address for the websocket server.
   * @param deviceId The device ID for this thread.
   * @param packetTypes The types of opq packets to generate from this thread.
   * @param packetsPerSecond The number of packets per second this thread will generate.
   * @param eventFrequency The frequency that event packets will be generated by this thread.
   */
  public StressTestTask(String websocketAddress, String deviceId, List<OpqPacket.PacketType> packetTypes, Integer packetsPerSecond, Double eventFrequency) {
    this.websocketAddress = websocketAddress;
    this.deviceId = deviceId;
    this.packetTypes = packetTypes;
    this.packetsPerSecond = packetsPerSecond;
    this.eventFrequency = eventFrequency;
    this.random = new Random();
  }

  /**
   * Start this task as a thread.
   * @return Void.
   * @throws Exception If a problem with starting this task occurs.
   */
  @Override
  protected Void call() throws Exception {
    OpqPacket packet;
    WebsocketClient client = new WebsocketClient(websocketAddress);

    while (!isCancelled()) {
      packet = PowerSimulator.generatePartialPacket(this.getPacketType());
      packet.setDeviceId(Long.parseLong(deviceId));
      packet.setTimestamp(new Date().getTime());
      packet.setChecksum();
      client.send(packet);

      try {
        Thread.sleep(1000 / packetsPerSecond);
      }
      catch (InterruptedException e) {
        if (isCancelled()) {
          break;
        }
      }
    }

    // Exited main thread loop, clean up websocket client.
    if (client.isOpen()) {
      client.close();
    }
    return null;
  }

  /**
   * Generates a random packet type based off the the given eventFrequency.
   * @return a random packet type.
   */
  private OpqPacket.PacketType getPacketType() {
    boolean isEventPacket = random.nextDouble() < eventFrequency;

    if (isEventPacket) {
      return getRandomEventPacket();
    }
    else {
      if (packetTypes.contains(OpqPacket.PacketType.MEASUREMENT)) {
        return OpqPacket.PacketType.MEASUREMENT;
      }
    }

    return OpqPacket.PacketType.MEASUREMENT;
  }

  /**
   * Generates a random opq event packet.
   * @return a random opq event packet.
   */
  private OpqPacket.PacketType getRandomEventPacket() {
    List<OpqPacket.PacketType> eventPackets = new ArrayList<>(packetTypes);
    eventPackets.remove(OpqPacket.PacketType.MEASUREMENT);

    if (eventPackets.size() == 0) {
      return null;
    }

    return eventPackets.get(random.nextInt(eventPackets.size()));
  }
}