package json;

import java.util.List;
import java.util.Set;
import models.Event;
import org.openpowerquality.protocol.OpqPacket.PacketType;
import utils.PqUtils;
import utils.PqUtils.IticRegion;

public class PrivateMapRequest extends JsonData {
  public Double minFrequency;
  public Double maxFrequency;
  public Double minVoltage;
  public Double maxVoltage;
  public Long minDuration;
  public Long maxDuration;
  public Long startTimestamp;
  public Long stopTimestamp;
  public boolean requestFrequency;
  public boolean requestVoltage;
  public boolean requestHeartbeats;
  public boolean requestIticSevere;
  public boolean requestIticModerate;
  public boolean requestIticOk;
  public List<Long> deviceIds;

  public static PrivateMapRequest fromJson(String json) {
    return JsonUtils.toObject(json, PrivateMapRequest.class);
  }

  public boolean containsEvent(Event event) {

    IticRegion region = PqUtils.getIticRegion(event.getDuration() * 1000, event.getVoltage());
    switch(region) {
      case PROHIBITED:
        if(!requestIticSevere) {
          return false;
        }
        break;
      case NO_DAMAGE:
        if(!requestIticModerate) {
          return false;
        }
        break;
      case NO_INTERRUPTION:
        if(!requestIticOk) {
          return false;
        }
        break;
    }

    return true;
  }
}
