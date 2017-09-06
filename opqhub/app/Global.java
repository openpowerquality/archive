import akka.actor.ActorRef;
import akka.actor.Props;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.event.ServerConfigStartup;
import jobs.EventReportActor;
import play.Application;
import play.GlobalSettings;
import play.data.format.Formatters;
import play.libs.Akka;
import play.Logger;
import scala.concurrent.duration.Duration;
import utils.PqUtils;
import utils.Sms;

import java.text.ParseException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class Global extends GlobalSettings implements ServerConfigStartup {
  @Override
  public void onStart(Application app) {
    Formatters.register(Sms.SmsCarrier.class, new Formatters.SimpleFormatter<Sms.SmsCarrier>() {
      @Override
      public Sms.SmsCarrier parse(String text, Locale locale) throws ParseException {
        return Sms.getCarrierByName(text);
      }

      @Override
      public String print(Sms.SmsCarrier smsCarrier, Locale locale) {
        return smsCarrier.getName();
      }
    });

    Formatters.register(PqUtils.IticRegion.class, new Formatters.SimpleFormatter<PqUtils.IticRegion>() {
      @Override
      public PqUtils.IticRegion parse(String s, Locale locale) throws ParseException {
        return PqUtils.getBySeverityName(s);
      }

      @Override
      public String print(PqUtils.IticRegion iticRegion, Locale locale) {
        return iticRegion.severityName;
      }
    });

    // Start up device health monitoring
    Logger.debug("Starting akka system for scheduled heartbeat checks");
    ActorRef actor = Akka.system().actorOf(Props.create(jobs.HeartbeatAlertActor.class), "heartbeatActor");
    Akka.system().scheduler().schedule(
        Duration.create(0, TimeUnit.MILLISECONDS),
        Duration.create(10, TimeUnit.MINUTES),
        actor,
        "hello, world",
        Akka.system().dispatcher(),
        null);

    //Setup Daily event notification email system.
    ActorRef mailerActor = Akka.system().actorOf(Props.create(EventReportActor.class));
    Akka.system().scheduler().schedule(
      Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay
      Duration.create(1, TimeUnit.DAYS),     //Frequency
      mailerActor,
      EventReportActor.Message.DAILY_REPORT,
      Akka.system().dispatcher(),
      null
    );

    //Setup Weekly event notification email system.
    Akka.system().scheduler().schedule(
            Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay
            Duration.create(7, TimeUnit.DAYS),     //Frequency
            mailerActor,
            EventReportActor.Message.WEEKLY_REPORT,
            Akka.system().dispatcher(),
            null
    );

  }

  @Override
  public void onStart(ServerConfig serverConfig) {
    serverConfig.setUpdateChangesOnly(true);
  }
}

