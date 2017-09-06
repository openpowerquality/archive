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

package org.openpowerquality.sim.netio;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.openpowerquality.protocol.OpqPacket;
import org.openpowerquality.sim.utils.Logger;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class represents a websocket client.
 */
public class WebsocketClient extends WebSocketClient {
  /**
   * Create a websocket connection by using a String representing the websocket URI.
   * @param URI The URI to the websocket server.
   * @throws URISyntaxException If the URI is not formatted correctly.
   */
  public WebsocketClient(String URI) throws URISyntaxException {
    this(new URI(URI));
  }

  /**
   * Creates a websocket connection using the given URI.
   * @param serverURI The URI of the websocket server.
   */
  private WebsocketClient(URI serverURI) {
    super(serverURI);
    this.connect();
  }

  /**
   * Sends an OpqPacket to the websocket server if the connection is open.
   * @param opqPacket A valid OpqPacket.
   */
  public void send(OpqPacket opqPacket) {
    if (this.isOpen()) {
      super.send(opqPacket.getBase64Encoding());
      Logger.log(String.format("Message [%s] sent to %s", opqPacket.getType().getName(), this.getURI()));
    }
    else {
      Logger.log("Websocket connection is not open.");
    }
  }

  /**
   * This callback is called when a connection with the websocket server is opened.
   * @param serverHandshake The server handshake.
   */
  @Override
  public void onOpen(ServerHandshake serverHandshake) {
    Logger.log(String.format("Connection opened to %s", this.getURI()));
  }

  /**
   * Callback that is called when a message is received from the websocket server.
   * @param s The message received from the websocket server.
   */
  @Override
  public void onMessage(String s) {
    Logger.log(String.format("Received message from %s:\n%s", this.getURI(), s));
  }

  /**
   * Callback that is called when this websocket connection is closed.
   * @param i Integer code representing the reason this connection was closed.
   * @param s String representing the reason this connection was closed.
   * @param b If true the connection was closed by a remote peer, otherwise the connetion was closed by us.
   */
  @Override
  public void onClose(int i, String s, boolean b) {
    Logger.log(String.format("Connection with %s closed by %s due to %s[%d]", this.getURI(), (b ? "remote peer" : "us"),
                             s, i));
  }

  /**
   * Callback this is called when an error occurs with the websocket connection between the client and server.
   * @param e Exception that caused the error.
   */
  @Override
  public void onError(Exception e) {
    //Logger.log(String.format("Websocket error: %s", e));
    System.out.println(e);
  }
}
