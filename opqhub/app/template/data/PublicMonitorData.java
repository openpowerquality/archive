package template.data;

import models.Event;

import java.util.List;
import java.util.Map;

/**
 * Created by anthony on 3/18/15.
 */
public class PublicMonitorData {
  public List<EnhancedEvent> totalEvents;
  public long totalEventsCount;
  public long frequencyEventsCount;
  public long voltageEventsCount;
  public double mapCenterLat;
  public double mapCenterLng;
  public int mapZoom;
  public boolean requestFrequency;
  public double minFrequency;
  public double maxFrequency;
  public boolean requestVoltage;
  public double minVoltage;
  public double maxVoltage;
  public long minDuration;
  public long maxDuration;
  public long minTimestamp;
  public long maxTimestamp;
  public boolean iticSevere;
  public boolean iticModerate;
  public boolean iticOk;
  public EnhancedEvent detailedEvent;
  public int page;
  public int totalPages;
  public Map<String, GridIdEventCounts> gridIdEventCounts;

  public PublicMonitorData(List<EnhancedEvent> totalEvents, long totalEventsCount, long frequencyEventsCount,
                           long voltageEventsCount, double mapCenterLat, double mapCenterLng, int mapZoom,
                           boolean requestFrequency, double minFrequency, double maxFrequency, boolean requestVoltage,
                           double minVoltage, double maxVoltage, long minDuration, long maxDuration, long minTimestamp,
                           long maxTimestamp, boolean iticSevere, boolean iticModerate, boolean iticOk,
                           EnhancedEvent detailedEvent, int page, int totalPages,
                           Map<String, GridIdEventCounts> gridIdEventCounts) {
    this.totalEvents = totalEvents;
    this.totalEventsCount = totalEventsCount;
    this.frequencyEventsCount = frequencyEventsCount;
    this.voltageEventsCount = voltageEventsCount;
    this.mapCenterLat = mapCenterLat;
    this.mapCenterLng = mapCenterLng;
    this.mapZoom = mapZoom;
    this.requestFrequency = requestFrequency;
    this.minFrequency = minFrequency;
    this.maxFrequency = maxFrequency;
    this.requestVoltage = requestVoltage;
    this.minVoltage = minVoltage;
    this.maxVoltage = maxVoltage;
    this.minDuration = minDuration;
    this.maxDuration = maxDuration;
    this.minTimestamp = minTimestamp;
    this.maxTimestamp = maxTimestamp;
    this.iticSevere = iticSevere;
    this.iticModerate = iticModerate;
    this.iticOk = iticOk;
    this.detailedEvent = detailedEvent;
    this.page = page;
    this.totalPages = totalPages;
    this.gridIdEventCounts = gridIdEventCounts;
  }
}
