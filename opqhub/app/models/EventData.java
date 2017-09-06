package models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class EventData extends Model {
  /* ----- Fields ----- */
  @Id
  private Long primaryKey;

  @Column(columnDefinition = "MEDIUMTEXT")
  private String waveform;

  /* ----- Relationships ----- */
  @OneToOne
  private Event event;

  public EventData(String waveform) {
    this.waveform = waveform;
  }

  public Long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(Long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public String getWaveform() {
    return waveform;
  }

  public void setWaveform(String waveform) {
    this.waveform = waveform;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }

  public static Finder<Long, EventData> find() {
    return new Finder<>(Long.class, EventData.class);
  }
}
