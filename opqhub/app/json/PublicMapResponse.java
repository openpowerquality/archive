package json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicMapResponse extends JsonData {
  // Map specific data
  public Map<String, Integer> gridIdsToEvents;
  public Map<String, Integer> gridIdsToDevices;
  public Map<String, Integer[]> gridIdToEventMetrics;  

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

  public PublicMapResponse() {
    super("public-map-response");
    this.gridIdsToEvents = new HashMap<>();
    this.gridIdsToDevices = new HashMap<>();
    this.gridIdsToDevices = new HashMap<>();
    this.gridIdToEventMetrics = new HashMap<>();
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
}
