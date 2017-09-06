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

package utils;

import models.Person;

import org.apache.commons.mail.*;
import java.util.Set;
import java.util.concurrent.Callable;

//import static play.libs.Akka.future;
import java.util.concurrent.Callable;
import static play.libs.F.Promise.promise;

public class Mailer {
  private static final String host = play.Play.application().configuration().getString("smtp.host");
  private static final int port = play.Play.application().configuration().getInt("smtp.port");
  private static final String user = play.Play.application().configuration().getString("smtp.user");
  private static final String pass = play.Play.application().configuration().getString("smtp.pass");
  private static final boolean ssl = play.Play.application().configuration().getBoolean("smtp.ssl");

  private static void sendEmail(final String to, final String subject, final String body) {

//    future(new Callable<Object>() {
//      @Override
//      public Object call() throws Exception {
//        Email email = new SimpleEmail();
//        email.setHostName(host);
//        email.setSmtpPort(port);
//        email.setAuthenticator(new DefaultAuthenticator(user, pass));
//        email.setSSLOnConnect(ssl);
//
//        email.setFrom("alert@openpowerquality.com");
//        email.setSubject(subject);
//        email.setMsg(body);
//        email.addTo(to);
//        email.send();
//
//        return null;
//      }
//    });
  }

  public static void sendAlerts(Set<Person> persons, String subject, String body) {
    String alertEmail;
    Sms.SmsCarrier smsCarrier;
    String smsNumber;

    for(Person person : persons) {
      alertEmail = person.getAlertEmail();
      smsCarrier = person.getSmsCarrier();
      smsNumber = person.getSmsNumber();

      if(alertEmail != null) {
        Mailer.sendEmail(alertEmail, subject, body);
      }

      if(smsCarrier != null || smsNumber != null) {
        Mailer.sendEmail(Sms.formatSmsEmailAddress(smsNumber,smsCarrier), subject, body);
      }
    }
  }
}
