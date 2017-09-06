package utils;

import models.Event;
import template.data.EnhancedEvent;

import java.awt.Polygon;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

public class PqUtils {
  private static final Polygon PROHIBITED_POLYGON = new Polygon(
      new int[] {0, 1, 3, 3, 500, 500, Integer.MAX_VALUE, Integer.MAX_VALUE},
      new int[] {500, 200, 140, 120, 120, 110, 110, 500},
      8);

  private static final Polygon NO_DAMAGE_POLYGON = new Polygon(
      new int[] {20, 20, 500, 500, 10000, 10000, 100000, Integer.MAX_VALUE, Integer.MAX_VALUE},
      new int[] {0, 70, 70, 80, 80, 90, 90, 90, 0},
      9);

  public enum IticRegion {
    NO_INTERRUPTION("No Interruption", 2, "Ok"),
    NO_DAMAGE("No Damage", 1, "Moderate"),
    PROHIBITED("Prohibited", 0, "Severe"),
    UNKNOWN("Unknown", -1, "Unknown");

    public final String name;
    public final Integer severity; // Lower number -> higher severity
    public final String severityName;

    private IticRegion(final String name, final Integer severity, final String severityName) {
      this.name = name;
      this.severity = severity;
      this.severityName = severityName;
    }
  }

  public static IticRegion getBySeverityName(final String name) {
    switch(name) {
      case "Ok": return IticRegion.NO_INTERRUPTION;
      case "Moderate": return IticRegion.NO_DAMAGE;
      case "Severe": return IticRegion.PROHIBITED;
      default: return IticRegion.UNKNOWN;
    }
  }

  public static double voltageToPercentNominal(double voltage) {
    return Math.abs(voltage / 120.0 * 100);
  }

  public static IticRegion getIticRegion(long duration, double voltage) {
    double pnv = voltageToPercentNominal(voltage);
    if (PROHIBITED_POLYGON.contains((double) duration, pnv)) {
      return IticRegion.PROHIBITED;
    }
    else if (NO_DAMAGE_POLYGON.contains((double) duration, pnv)) {
      return IticRegion.NO_DAMAGE;
    }
    else {
      return IticRegion.NO_INTERRUPTION;
    }
  }

  public static List<EnhancedEvent> filterEventsWithRegions(List<Event> events, boolean includeSevere, boolean includeModerate,
                                                    boolean includeOk) {
    Set<IticRegion> regions = new HashSet<>();
    if(includeSevere) regions.add(IticRegion.PROHIBITED);
    if(includeModerate) regions.add(IticRegion.NO_DAMAGE);
    if(includeOk) regions.add(IticRegion.NO_INTERRUPTION);
    List<EnhancedEvent> enhancedEvents = events.parallelStream()
                                               .map(evt -> new EnhancedEvent(evt,
                                                                             getIticRegion(
                                                                                 evt.getDuration() * 1000,
                                                                                 evt.getVoltage())))
                                               .filter(evt -> regions.contains(evt.iticRegion))
                                               .collect(Collectors.toList());
    return enhancedEvents;
  }
}

