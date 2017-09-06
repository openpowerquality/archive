package jobs;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import jobs.support_classes.HtmlMailerMessage;
import models.Event;
import models.Person;
import org.openpowerquality.protocol.OpqPacket;
import utils.DateUtils;
import utils.PqUtils;
import utils.Sms;

import java.util.Set;

public class PowerAlertActor extends UntypedActor {
  private final ActorRef htmlMailerActor;

  public PowerAlertActor() {
    this.htmlMailerActor = getContext().actorOf(Props.create(jobs.HtmlMailerActor.class), "htmlMailerActor");
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if(message instanceof Event) {
      Event evt = (Event) message;
      // Find users affected by this event
      Set<Person> affectedPersons = Event.getAffectedPersons(evt.getPrimaryKey());
      String eventType = evt.getEventType().getName();
      double eventValue = evt.getEventType().equals(OpqPacket.PacketType.EVENT_VOLTAGE) ? evt.getVoltage() : evt.getFrequency();
      long eventDuration = evt.getDuration();
      String dateTime = DateUtils.toDateTime(evt.getTimestamp());
      String eventSeverity = "";
      switch(PqUtils.getIticRegion(evt.getDuration() * 1000, evt.getVoltage())) {
        case NO_INTERRUPTION:
          eventSeverity = "Ok";
          break;
        case NO_DAMAGE:
          eventSeverity = "Moderate";
          break;
        case PROHIBITED:
          eventSeverity = "Severe";
          break;
      }

      String messageBody = String.format("%s %s received at %s\nvalue = %f\nduration = %d\nCheck your OPQHub account" +
          "for more details\n",
        eventSeverity,
        eventType,
        dateTime,
        eventValue,
        eventDuration);

      // Send alerts to e-mail or sms
      for(Person person : affectedPersons) {
        boolean sendSmsAlerts = person.isEnableSmsAlerts();
        boolean sendEmailAlerts = person.isEnableEmailAlerts() && person.isEnableEmailAertNotifications();

        if(sendSmsAlerts) {
          this.htmlMailerActor.tell(
            new HtmlMailerMessage(
              Sms.formatSmsEmailAddress(person.getSmsNumber(), person.getSmsCarrier()),
              "OPQ Power Alert",
              messageBody),
            self());
        }
        if(sendEmailAlerts) {
          this.htmlMailerActor.tell(
            new HtmlMailerMessage(
              person.getAlertEmail(),
              "OPQ Power Alert",
              messageBody),
            self());
        }
      }
    }
    else {
      unhandled(message);
    }
  }
}
