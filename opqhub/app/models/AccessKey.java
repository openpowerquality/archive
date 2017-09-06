package models;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class AccessKey extends Model {
  /* ----- Fields ----- */
  @Id
  private Long primaryKey;

  @Constraints.Required
  private Long deviceId;

  @Constraints.Required
  private String accessKey;

  /* ----- Relationships ----- */
  @ManyToMany(cascade = CascadeType.ALL)
  private Set<Person> persons = new HashSet<>();

  @OneToOne
  private OpqDevice opqDevice;

  @OneToMany(mappedBy = "accessKey")
  private List<Event> events = new ArrayList<>();

  public Long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(Long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public Set<Person> getPersons() {
    return persons;
  }

  public void setPersons(Set<Person> persons) {
    this.persons = persons;
  }

  public OpqDevice getOpqDevice() {
    return opqDevice;
  }

  public void setOpqDevice(OpqDevice opqDevice) {
    this.opqDevice = opqDevice;
  }

  public List<Event> getEvents() {
    return events;
  }

  public void setEvents(List<Event> events) {
    this.events = events;
  }

  public Long getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(Long deviceId) {
    this.deviceId = deviceId;
  }

  public static Finder<Long, AccessKey> find() {
    return new Finder<>(Long.class, AccessKey.class);
  }

  public static boolean keyExists(AccessKey key) {
    return findKey(key) != null;
  }

  public static AccessKey findKey(AccessKey key) {
    return findKey(key.getDeviceId(), key.getAccessKey());
  }

  public static AccessKey findKey(Long deviceId, String accessKey) {
    return find().where().eq("deviceId", deviceId).eq("accessKey", accessKey).findUnique();
  }

  @Override
  public String toString() {
    return String.format("AccessKey: deviceId=%d accessKey=%s", this.deviceId, this.accessKey);
  }

  public void addEvent(Event event) {
    events.add(event);
  }
}
