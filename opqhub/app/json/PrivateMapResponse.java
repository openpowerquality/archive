package json;

import play.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivateMapResponse extends JsonData {

  // Events
  public List<Map<String, String>> events;

  // Global statistics
  public int totalRegisteredDevices;
  public int totalActiveDevices;
  public int totalAffectedDevices;
  public int totalEvents;
  public int totalFrequencyEvents;
  public int totalVoltageEvents;
  public long maxDuration;
  public double minFrequency;
  public double maxFrequency;
  public double minVoltage;
  public double maxVoltage;
  public long minTimestamp;
  public long maxTimestamp;

  public PrivateMapResponse() {
    super("private-map-response");
    this.events = new ArrayList<>();
    this.totalRegisteredDevices = 0;
    this.totalActiveDevices = 0;
    this.totalAffectedDevices = 0;
    this.totalEvents = 0;
    this.totalFrequencyEvents = 0;
    this.totalVoltageEvents = 0;
    this.maxDuration = 0L;
    this.minFrequency = 0.0;
    this.maxFrequency = 0.0;
    this.minVoltage = 0;
    this.maxVoltage = 0;
    this.minTimestamp = 0L;
    this.maxTimestamp = 0L;
  }

  public void addEvent(String ... keyValuePairs) {
    final int MAX_EVENTS = 100;
    if(events.size() >= MAX_EVENTS) {
      return;
    }


    if(keyValuePairs.length % 2 != 0) {
      Logger.warn("Sending private event info does not have even number of values");
      return;
    }
    Map<String, String> event = new HashMap<>();
    for(int i = 0; i < keyValuePairs.length; i += 2) {
      event.put(keyValuePairs[i], keyValuePairs[i + 1]);
    }
    events.add(event);
  }
}
