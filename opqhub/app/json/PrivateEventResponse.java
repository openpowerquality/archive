package json;

import java.util.ArrayList;
import java.util.List;

public class PrivateEventResponse extends JsonData {
  public Long deviceId;
  public String deviceDescription;
  public String timestamp;
  public String eventType;
  public String gridId;
  public Double centerLat;
  public Double centerLng;
  public Double gridScale;
  public String frequency;
  public String voltage;
  public String eventLevel;
  public Long duration;
  public List<Double> waveform;

  public PrivateEventResponse() {
    super("private-event-response");
    this.deviceId = 0L;
    this.deviceDescription = "";
    this.timestamp = "";
    this.eventType = "";
    this.gridId = "";
    this.centerLat = 0.0;
    this.centerLng = 0.0;
    this.gridScale = 0.0;
    this.frequency = "";
    this.voltage = "";
    this.eventLevel = "";
    this.duration = 0L;
    this.waveform = new ArrayList<>();
  }
}
