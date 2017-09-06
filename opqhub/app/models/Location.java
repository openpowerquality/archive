package models;

import com.avaje.ebean.Query;
import play.db.ebean.Model;
import utils.DbUtils;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class Location extends Model {
  /* ----- Fields ----- */
  @Id
  private Long primaryKey;

  private String gridId;

  private Double gridScale;

  private Integer gridRow;

  private Integer gridCol;

  private Double northEastLatitude;

  private Double northEastLongitude;

  private Double southWestLatitude;

  private Double southWestLongitude;

  /* ----- Relationships ----- */
  @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
  private
  List<OpqDevice> opqDevices = new ArrayList<>();

  @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
  private
  List<Event> events = new ArrayList<>();

  public static Finder<Long, Location> find() {
    return new Finder<>(Long.class, Location.class);
  }

  public static Query<Location> getLocationsFromIds(Set<String> ids) {
       return DbUtils.getAnyLike(Location.class, "gridId", ids);
  }


  public Long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(Long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public String getGridId() {
    return gridId;
  }

  public void setGridId(String gridId) {
    this.gridId = gridId;
  }

  public Double getGridScale() {
    return gridScale;
  }

  public void setGridScale(Double gridScale) {
    this.gridScale = gridScale;
  }

  public Integer getGridRow() {
    return gridRow;
  }

  public void setGridRow(Integer gridRow) {
    this.gridRow = gridRow;
  }

  public Integer getGridCol() {
    return gridCol;
  }

  public void setGridCol(Integer gridCol) {
    this.gridCol = gridCol;
  }

  public Double getNorthEastLatitude() {
    return northEastLatitude;
  }

  public void setNorthEastLatitude(Double northEastLatitude) {
    this.northEastLatitude = northEastLatitude;
  }

  public Double getNorthEastLongitude() {
    return northEastLongitude;
  }

  public void setNorthEastLongitude(Double northEastLongitude) {
    this.northEastLongitude = northEastLongitude;
  }

  public Double getSouthWestLatitude() {
    return southWestLatitude;
  }

  public void setSouthWestLatitude(Double southWestLatitude) {
    this.southWestLatitude = southWestLatitude;
  }

  public Double getSouthWestLongitude() {
    return southWestLongitude;
  }

  public void setSouthWestLongitude(Double southWestLongitude) {
    this.southWestLongitude = southWestLongitude;
  }

  public List<OpqDevice> getOpqDevices() {
    return opqDevices;
  }

  public void setOpqDevices(List<OpqDevice> opqDevices) {
    this.opqDevices = opqDevices;
  }

  public List<Event> getEvents() {
    return events;
  }

  public void setEvents(List<Event> events) {
    this.events = events;
  }
}
