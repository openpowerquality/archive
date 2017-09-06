package org.openpowerquality.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;

public class JsonOpqPacketFactory {
  private static ObjectMapper mapper = new ObjectMapper();

  public static OpqPacket opqPacketFromJson(String json) {
    OpqPacket opqPacket = null;
    try {
      opqPacket = mapper.readValue(json, OpqPacket.class);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return opqPacket;
  }

  public static OpqPacket opqPacketFromBase64EncodedJson(String base64EncodedJson) {
    return opqPacketFromJson(new String(DatatypeConverter.parseBase64Binary(base64EncodedJson)));
  }

  public static void main(String[] args) {
    String json = "{" +
                  "\"magicWord\": 1," +
                  "\"packetType\": 2," +
                  "\"deviceId\": 3," +
                  "\"deviceKey\": \"hello, world\"," +
                  "\"timestamp\": 4," +
                  "\"checksum\": 5," +
                  "\"frequency\": 6.1," +
                  "\"voltage\": 7.1," +
                  "\"payloadSize\": 8," +
                  "\"payload\": [1,2,3]" +
                  "}";

    System.out.println(opqPacketFromJson(json));
  }
}
