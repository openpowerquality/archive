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

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * Contains methods for manipulating persisted OpqDevices.
 * <p/>
 * OPQ devices have an id, description, and optional locational information. OPQ devices tend to link the rest of the
 * persisted object together.
 */
@Entity
public class OpqDevice extends Model {
  /* ----- Fields ----- */
  @Id
  private Long primaryKey;

  @Constraints.Required
  private Long deviceId;

  private String description;

  private Boolean sharingData;

  private Long lastHeartbeat;

  /* ----- Relationships ----- */
  @OneToOne
  private AccessKey accessKey;

  @ManyToOne(cascade = CascadeType.ALL)
  private Location location;

  public OpqDevice(Long deviceId) {
    this.setDeviceId(deviceId);
  }

  /**
   * The primary key.
   */
  public Long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(Long primaryKey) {
    this.primaryKey = primaryKey;
  }

  /**
   * The device id as a 64-bit integer.
   */
  public Long getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(Long deviceId) {
    this.deviceId = deviceId;
  }

  /**
   * Short description of the device. I.e. basement, office, etc.
   */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Determines if device is participating in sharing data.
   */
  public Boolean getSharingData() {
    return sharingData;
  }

  public void setSharingData(Boolean sharingData) {
    this.sharingData = sharingData;
  }

  public Long getLastHeartbeat() {
    return lastHeartbeat;
  }

  public void setLastHeartbeat(Long lastHeartbeat) {
    this.lastHeartbeat = lastHeartbeat;
  }

  public AccessKey getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(AccessKey accessKey) {
    this.accessKey = accessKey;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  /**
   * Finder for filtering persisted devices.
   *
   * @return Finder for filtering persisted devices.
   */
  public static Finder<Long, OpqDevice> find() {
    return new Finder<>(Long.class, OpqDevice.class);
  }
}
