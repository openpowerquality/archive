package utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sms {

  /**
   * List of popular U.S. based wireless carriers and their sms gateways.
   * <p/>
   * To use the gateways, simply send an e-mail to the address associated with the wireless carrier and replace the
   * pound symbol "#" with the users sms number. See: https://en.wikipedia.org/wiki/List_of_SMS_gateways
   */

  public enum SmsCarrier {
    ALLTEL        ("Alltel",        "sms.alltelwireless.com"),
    ATT           ("AT&T",          "txt.att.net"),
    BOOST_MOBILE  ("Boost Mobile",  "myboostmobile.com"),
    CRICKET       ("Cricket",       "sms.myscricket.com"),
    SPRINT        ("Sprint",        "messaging.sprintpcs.com"),
    STRAIGHT_TALK ("Straight Talk", "vtext.com"),
    T_MOBILE      ("T-Mobile",      "tmomail.net"),
    TRAC_PHONE    ("TracFone",      "mmst5.tracfone.com"),
    US_CELLULAR   ("US Cellular",   "email.uscc.com"),
    VERIZON       ("Verizon",       "vtext.com"),
    VIRGIN_MOBILE ("Virgin Mobile", "vmobl.com");

    private final String name;
    private final String emailGateway;

    SmsCarrier(final String name, final String emailGateway) {
      this.name = name;
      this.emailGateway = emailGateway;
    }

    public final String getName() {
      return this.name;
    }

    public final String getEmailGateway() {
      return this.emailGateway;
    }
  }

  public static List<String> listOfCarriers() {
    List<String> carrierList = new ArrayList<>();
    for(SmsCarrier smsCarrier : SmsCarrier.values()) {
      carrierList.add(smsCarrier.getName());
    }
    return carrierList;
  }

  public static SmsCarrier getCarrierByName(String name) {
    for(SmsCarrier smsCarrier : SmsCarrier.values()) {
      if(smsCarrier.getName().equals(name)) {
        return smsCarrier;
      }
    }
    return null;
  }

  public static String formatSmsEmailAddress(String smsNumber, SmsCarrier smsCarrier) {
    return String.format("%s@%s", smsNumber, smsCarrier.getEmailGateway());
  }
}
