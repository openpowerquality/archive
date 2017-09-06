package json;

/**
 * Created by anthony on 8/12/14.
 */
public class PublicEventRequest extends JsonData {
  public Long pk;

  public static PublicEventRequest fromJson(String json) {
    {
      return JsonUtils.toObject(json, PublicEventRequest.class);
    }
  }
}
