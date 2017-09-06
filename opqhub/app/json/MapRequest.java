package json;

import java.util.Set;
import models.Event;
import org.openpowerquality.protocol.OpqPacket.PacketType;
import utils.PqUtils;
import utils.PqUtils.IticRegion;

public class MapRequest extends JsonData {
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
  public Set<String> visibleIds;

  public static MapRequest fromJson(String json) {
    return JsonUtils.toObject(json, MapRequest.class);
  }

  public boolean containsEvent(Event event) {
    switch(event.getEventType()) {
      case EVENT_FREQUENCY:
        if(!requestFrequency || event.getFrequency() < minFrequency || event.getFrequency() > maxFrequency) {
          return false;
        }
        break;
      case EVENT_VOLTAGE:
        if(!requestVoltage || event.getVoltage() < minVoltage || event.getVoltage() > maxVoltage) {
          return false;
        }
        break;
      case EVENT_HEARTBEAT:
        if(!requestHeartbeats) {
          return false;
        }
        break;
    }
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
    if(event.getDuration() < minDuration || event.getDuration() > maxDuration) {
      return false;
    }
    if(event.getTimestamp() < startTimestamp || event.getTimestamp() > stopTimestamp) {
      return false;
    }
    return true; 
  }
}
