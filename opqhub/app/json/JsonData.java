package json;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonData {
  public String packetType;

  public JsonData() {
    this.packetType = "";
  }

  public JsonData(String packetType) {
    this.packetType = packetType;
  }

  @Override
  public String toString() {
    return this.toJson();
  }

  public String toJson() {
    return JsonUtils.toJson(this);
  }

  public static JsonData fromJson(String json) {
    return JsonUtils.toObject(json, JsonData.class);
  }
}
