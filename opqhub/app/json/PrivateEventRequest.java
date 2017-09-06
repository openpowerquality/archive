package json;

public class PrivateEventRequest extends JsonData {
  public Long pk;

  public static PrivateEventRequest fromJson(String json) {
    {
      return JsonUtils.toObject(json, PrivateEventRequest.class);
    }
  }
}
