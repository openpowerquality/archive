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

package controllers;

import jobs.reports.DeviceReports;
import jobs.support_classes.PersonDeviceInfo;
import models.AccessKey;
import models.OpqDevice;
import models.Person;
import models.Location;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.deviceadmin;
import views.html.deviceconfig;
import views.html.error;
import views.html.useradmin;

import java.util.Date;
import java.util.Set;

import static play.data.Form.form;

/**
 * Contains methods which allow users to interact with the views and models for administrating their account and their
 * device.
 */
public class Administration extends Controller {

  /**
   * Render the view for user administration.
   *
   * @return The rendered view for user administration.
   */
  @Security.Authenticated(SecuredAndMatched.class)
  public static Result user(String email) {
    Person person = Person.find().where().eq("email", email).findUnique();
    Form<Person> personForm = form(Person.class).fill(person);
    //return ok(adminuser.render(personForm));
    return ok(useradmin.render(personForm));
  }

  /**
   * Updates person fields and redirects back to person administration.
   * @return Redirect to person administration.
   */
  @Security.Authenticated(SecuredAndMatched.class)
  public static Result updateUser(String email) {
    Person person = Person.getLoggedIn();
    Form<Person> personForm = form(Person.class).bindFromRequest();

    if(!person.getEmail().equals(email)) {
        return unauthorized("You do not have access to edit this person's data.");
    }
    if (personForm.hasErrors()) {
      Logger.debug(String.format("%s user information NOT updated due to errors %s", person.getPrimaryKey(), personForm.errors().toString()));
      return ok(error.render("Problem updating person", personForm.errors().toString()));
    }

    Logger.debug(String.format("%s user information updated", person.getPrimaryKey()));

    // Generate person obj from form.
    Person personFromForm = personForm.get();

    // If form password field was empty, manually set password hash and salt.
    if (personForm.field("password").value().equals(null) || personForm.field("password").value().isEmpty()) {
      personFromForm.setPasswordHash(person.getPasswordHash());
      personFromForm.setPasswordSalt(person.getPasswordSalt());
    }

    personFromForm.update(person.getPrimaryKey());
    flash("updated", "Account Updated");
    return redirect(controllers.routes.Administration.user(person.getEmail()));
  }

  /**
   * Render the view for device administration.
   *
   * @return The rendered view for device administration.
   */
  @Security.Authenticated(SecuredAndMatched.class)
  public static Result device(String email) {
    Form<AccessKey> keyForm = form(AccessKey.class);

    Person person = Person.find().where().eq("email", email).findUnique();
    Set<AccessKey> keys = person.getAccessKeys();

    PersonDeviceInfo pdi = DeviceReports.generateDeviceReport(email, 0L, new Date().getTime());

    //return ok(admindevice.render(keyForm, keys));
    return ok(deviceadmin.render(keyForm, keys, pdi));
  }


  /**
   * Saves a new OPQ device to the DB.
   * @return Redirect to device administration.
   */
  @Security.Authenticated(SecuredAndMatched.class)
  public static Result saveDevice(String email) {
    Form<AccessKey> keyForm = form(AccessKey.class).bindFromRequest();

    if (keyForm.hasErrors()) {
      Logger.debug(String.format("New device not saved due to errors %s", keyForm.errors().toString()));
      return ok(error.render("Problem saving new device", keyForm.errors().toString()));
    }

    AccessKey key = keyForm.get();
    AccessKey existingKey;
    Person person = Person.getLoggedIn();

    if(!person.getEmail().equals(email)) {
        return unauthorized("You do not have access to modify this account");
    }

    if(AccessKey.keyExists(key)) {
      existingKey = AccessKey.findKey(key);
      existingKey.getPersons().add(person);
      existingKey.update();
    }
    else {
      OpqDevice device = new OpqDevice(key.getDeviceId());

      key.save();
      device.save();

      // Update person
      person.getAccessKeys().add(key);
      person.update();

      // Update key
      key.getPersons().add(person);
      key.setOpqDevice(device);
      key.update();

      // Update device
      device.setAccessKey(key);
      device.update();
    }

    Logger.debug(String.format("New key [%s] saved", key));

    flash("added", "Device added");
    return redirect(routes.Administration.configureDevice(email, key.getAccessKey(),key.getDeviceId()));
  }

  @Security.Authenticated(SecuredAndMatched.class)
  public static Result configureDevice(String email, String accessKey, Long deviceId) {
    AccessKey key = AccessKey.findKey(deviceId, accessKey);
    OpqDevice device = key.getOpqDevice();
    Location location = device.getLocation();
    Form<OpqDevice> deviceForm  = form(OpqDevice.class).fill(device);
    Form<Location> locationForm = (location == null) ? form(Location.class) : form(Location.class).fill(location);

    // For some reason switching the devices id number will create a new location with null values
    // As ugly as it is, we need to check for null values and set the entire location to null.
    // A better fix would be to not create any location at all when a new device is saved.
    if(location.getGridId() == null || location.getGridScale() == null || location.getGridCol() == null ||
       location.getGridRow() == null || location.getNorthEastLatitude() == null || location.getNorthEastLongitude() == null ||
       location.getSouthWestLatitude() == null || location.getSouthWestLongitude() == null) {
      location = null;
    }

    return ok(deviceconfig.render(key.getDeviceId(), key.getAccessKey(), deviceForm, locationForm, location));
  }

  public static Result saveDeviceConfiguration(String email, String accessKey, Long deviceId) {
    Form<OpqDevice> deviceForm = form(OpqDevice.class).bindFromRequest();
    Form<Location> locationForm = form(Location.class).bindFromRequest();

    if (deviceForm.hasErrors()) {
      Logger.debug(String.format("Device not updated due to errors %s", deviceForm.errors().toString()));
      return ok(error.render("Problem updating device", deviceForm.errors().toString()));
    }
    if (locationForm.hasErrors()) {
      Logger.debug(String.format("Location not updated due to errors %s", locationForm.errors().toString()));
      return ok(error.render("Problem updating location...", locationForm.errors().toString()));
    }

    OpqDevice deviceFromForm = deviceForm.get();
    Location location = locationForm.get();
    Location oldLocation = Location.find().where().eq("gridId", location.getGridId()).findUnique();

    if(oldLocation != null) {
      location = oldLocation;
    }

    OpqDevice device = OpqDevice.find().where().eq("deviceId", deviceFromForm.getDeviceId()).findUnique();

    device.setDescription(deviceFromForm.getDescription());
    device.setSharingData(deviceFromForm.getSharingData());
    device.setLocation(location);
    device.update();

    if(oldLocation == null) {
      location.getOpqDevices().add(device);
      location.save();
    } else {
      if(!location.getOpqDevices().contains(device)) {
        location.getOpqDevices().add(device);
      }
      location.update();
    }


    flash("updated", "Updated device information.");
    return redirect(routes.Administration.configureDevice(email, accessKey, deviceId));
  }
}
