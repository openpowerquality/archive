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

import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

/**
 * Allows us to use annotation based security by checking the username and defining what happens
 * when a user attempts to access an area and they are not authorized.
 */
public class Secured extends Security.Authenticator {
  /**
   * Returns either the e-mail of the current logged in user or null.
   * @param context Current context storing session.
   * @return Either the e-mail of the current logged in user or null.
   */
  @Override
  public String getUsername(Http.Context context) {
    //Logger.info(context.request().body().toString());
    return context.session().get("email");
  }

  /**
   * This method is perform when a user attempts to access a restricted method without proper credentials.
   * @param context Current context storing session.
   * @return Redirect to login page.
   */
  @Override
  public Result onUnauthorized(Http.Context context) {
    return redirect(routes.PublicMonitor.publicMonitor());
  }
}
