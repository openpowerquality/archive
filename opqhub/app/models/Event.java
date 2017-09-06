/*
  This file is part of OPQHub.

  OPQHub is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  OPQHub is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with OPQHub.  If not, see <http://www.gnu.org/licenses/>.

  Copyright 2014 Anthony Christe
 */

package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Junction;
import com.avaje.ebean.Query;
import org.openpowerquality.protocol.OpqPacket;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.reflect.api.Exprs;
import template.data.EnhancedEvent;
import utils.DbUtils;
import utils.PqUtils;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.print.attribute.standard.JobHoldUntil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This contains methods for viewing and modifying the persistent Event object.
 * <p/>
 * Alerts are triggered when power quality is not up to snuff. They indicate that either the voltage or frequency are
 * off the status-quo, or that an error has occurred with the user's opq-device.
 */
@Entity
public class Event extends Model implements Comparable<Event> {
  /* ----- Fields ----- */
  @Id
  private Long primaryKey;

  @Required
  private Long timestamp;

  @Required
  private OpqPacket.PacketType eventType;

  @Required
  private Double frequency;

  @Required
  private Double voltage;

  /*
  /**
   * Contains raw power data.
   *
  @Column(columnDefinition = "MEDIUMTEXT")
  private String rawPowerData;
  */


  @Required
  private Long duration;


  /* ----- Relationships ----- */
  @ManyToOne(cascade = CascadeType.ALL)
  private AccessKey accessKey;
  @ManyToOne(cascade = CascadeType.ALL)
  private Location location;
  @OneToOne
  private EventData eventData;

  /**
   * Convenience method for creating an event during testing.
   *
   * @param eventType     The event type.
   * @param timestamp     Timestamp for when alert happened representing number of milliseconds since epoch.
   */
  public Event(Long timestamp, OpqPacket.PacketType eventType, Double frequency, Double voltage, Long duration) {
    this.setTimestamp(timestamp);
    this.setEventType(eventType);
    this.setFrequency(frequency);
    this.setVoltage(voltage);
    this.setDuration(duration);
  }

  /**
   * Create a new finder for persisted alerts.
   *
   * @return A new finder for persisted alerts.
   */
  public static Finder<Long, Event> find() {
    return new Finder<>(Long.class, Event.class);
  }


  /**
   * Primary Key.
   */
  public Long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(Long primaryKey) {
    this.primaryKey = primaryKey;
  }

  /**
   * Time alert occurred as milliseconds since the epoch.
   */
  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * The type of alert as given by the AlertType Enum.
   */
  public OpqPacket.PacketType getEventType() {
    return eventType;
  }

  public void setEventType(OpqPacket.PacketType eventType) {
    this.eventType = eventType;
  }

  /**
   * The frequency of the event.
   */
  public Double getFrequency() {
    return frequency;
  }

  public void setFrequency(Double frequency) {
    this.frequency = frequency;
  }

  /**
   * The voltage of the event.
   */
  public Double getVoltage() {
    return voltage;
  }

  public void setVoltage(Double voltage) {
    this.voltage = voltage;
  }

  /**
   * The amount of time the alert lasted in milliseconds.
   */
  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  /**
   * Many alerts can be associated with a single device.
   */
  public AccessKey getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(AccessKey accessKey) {
    this.accessKey = accessKey;
  }

  /**
   * Many alerts can be associated with an external event.
   */
  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public EventData getEventData() {
    return eventData;
  }

  public void setEventData(EventData eventData) {
    this.eventData = eventData;
  }

  @Override
  public int compareTo(Event event) {    
    return event.timestamp.compareTo(this.timestamp);
  }

  public static List<EnhancedEvent> getPublicEvents(double minFreq, double maxFreq, double minVolt, double maxVolt,
                                      int minDuration, int maxDuration, long minTimestamp, long maxTimestamp,
                                      boolean includeSevere, boolean includeModerate, boolean includeOk,
                                      boolean includeFrequency, boolean includeVoltage, boolean sharingData,
                                      List<String> gridIds) {


    ExpressionList<Event> eventExpressionList = Event.find().where()
                              .eq("accessKey.opqDevice.sharingData", sharingData)
                              .between("frequency", minFreq, maxFreq)
                              .between("voltage", minVolt, maxVolt)
                              .between("duration", minDuration, maxDuration)
                              .between("timestamp", minTimestamp, maxTimestamp)
                              .ne("eventType", OpqPacket.PacketType.EVENT_HEARTBEAT);

    // If voltage and frequency events are not requested, then there is no data to return
    if(!includeFrequency && !includeVoltage) {
      return new ArrayList<EnhancedEvent>();
    }


    // We want to find all events that occurred where the list of gridIds starts with a location id
    Junction<Event> locationJunction = eventExpressionList.disjunction();
    for(String gridId : gridIds) {
      locationJunction.add(Expr.startsWith("location.gridId", gridId));
    }
    eventExpressionList = locationJunction.endJunction();

    // Create another disjunction for the inclusion of frequency vs voltage events

    Junction<Event> eventTypeJunction = eventExpressionList.disjunction();
    if(includeVoltage) {
      eventTypeJunction.add(Expr.eq("eventType", OpqPacket.PacketType.EVENT_VOLTAGE));
    }
    if(includeFrequency) {
      eventTypeJunction.add(Expr.eq("eventType", OpqPacket.PacketType.EVENT_FREQUENCY));
    }
    eventExpressionList = eventTypeJunction.endJunction();

    // Set events to reverse chronological order.
    eventExpressionList.orderBy("timestamp desc");

    List<EnhancedEvent> events;

    events = PqUtils.filterEventsWithRegions(eventExpressionList.findList(),
                                             includeSevere,
                                             includeModerate,
                                             includeOk);

    return events;
  }

  public static Set<Person> getAffectedPersons(long id) {
    return Event.find().byId(id).getAccessKey().getPersons();
  }

  public static EnhancedEvent getPublicEventById(long id) {
    Event publicEvent = Event.find().byId(id);
    if(publicEvent == null || !publicEvent.getAccessKey().getOpqDevice().getSharingData()) {
      return null;
    }
    return new EnhancedEvent(publicEvent, PqUtils.getIticRegion(publicEvent.getDuration() * 1000, publicEvent.getVoltage()));
  }

//  public static List<Event> getPrivateEvents(double minFreq, double maxFreq, double minVolt, double maxVolt,
//                                             int minDuration, int maxDuration, long minTimestamp, long maxTimestamp,
//                                             boolean includeSevere, boolean includeModerate, boolean includeOk,
//                                             boolean includeVoltage, boolean includeFrequency, boolean sharingData,
//                                             List<AccessKey> accessKeys) {
//
//    ExpressionList<Event> eventExpressionList = Event.find().where()
//      .eq("accessKey.opqDevice.sharingData", sharingData)
//      .between("frequency", minFreq, maxFreq)
//      .between("voltage", minVolt, maxVolt)
//      .between("duration", minDuration, maxDuration)
//      .between("timestamp", minTimestamp, maxTimestamp)
//      .ne("eventType", OpqPacket.PacketType.EVENT_HEARTBEAT);
//
//    // We want to find all events that are tied to the passed in access keys
//    Junction<Event> locationJunction = eventExpressionList.disjunction();
//    for(AccessKey accessKey : accessKeys) {
//      locationJunction.add(Expr.eq("accessKey", accessKey));
//    }
//    eventExpressionList = locationJunction.endJunction();
//
//    // Create another disjunction for the inclusion of frequency vs voltage events
//    Junction<Event> eventTypeJunction = eventExpressionList.disjunction();
//    if(includeVoltage) eventTypeJunction.add(Expr.eq("eventType", OpqPacket.PacketType.EVENT_VOLTAGE));
//    if(includeFrequency) eventTypeJunction.add(Expr.eq("eventType", OpqPacket.PacketType.EVENT_FREQUENCY));
//    eventExpressionList = eventTypeJunction.endJunction();
//
//    List<Event> events = PqUtils.filterEventsWithRegions(eventExpressionList.findList(), includeSevere, includeModerate,
//      includeOk);
//
//
//    return events;
//  }

  public static long getLastHeartbeat(OpqDevice device) {
    return Ebean.createSqlQuery("SELECT MAX(timestamp) FROM event WHERE event_type = 0")
      .findUnique().getLong("max(timestamp)");
  }

  public static double getMinDouble(String field) {
    return Ebean.createSqlQuery("SELECT MIN(" + field + ") FROM event WHERE event_type != 0")
                .findUnique().getDouble("min(" + field + ")");
  }

  public static double getMaxDouble(String field) {
    return Ebean.createSqlQuery("SELECT MAX(" + field + ") FROM event WHERE event_type != 0")
                .findUnique().getDouble("max(" + field + ")");
  }

  public static long getMinLong(String field) {
    return Ebean.createSqlQuery("SELECT MIN(" + field + ") FROM event WHERE event_type != 0")
                .findUnique().getLong("min(" + field + ")");
  }

  public static long getMaxLong(String field) {
    return Ebean.createSqlQuery("SELECT MAX(" + field + ") FROM event WHERE event_type != 0")
                .findUnique().getLong("max(" + field + ")");
  }

  public static double getMinFrequency() {
    return getMinDouble("frequency");
  }

  public static double getMaxFrequency() {
    return getMaxDouble("frequency");
  }

  public static double getMinVoltage() {
    return getMinDouble("voltage");
  }

  public static double getMaxVoltage() {
    return getMaxDouble("voltage");
  }

  public static long getMinDuration() {
    return getMinLong("duration");
  }

  public static long getMaxDuration() {
    return getMaxLong("duration");
  }

  public static long getMinTimestamp() {
    return getMinLong("timestamp");
  }

  public static long getMaxTimestamp() {
    return getMaxLong("timestamp");
  }

}
