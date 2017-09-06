package json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

public class JsonUtils {
  public static ObjectMapper mapper = new ObjectMapper();

  public static String toJson(Object o) {
    try {
      return mapper.writeValueAsString(o);
    }
    catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return "";
  }

  public static <T> T toObject(String json, Class<T> clazz) {
    try {
      return mapper.readValue(json, clazz);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String getPacketType(String json) {
    try {
      JsonNode root = mapper.readValue(json, JsonNode.class);
      return root.get("packetType").asText();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }
}
