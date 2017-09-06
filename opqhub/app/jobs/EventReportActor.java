package jobs;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import jobs.reports.DeviceReports;
import jobs.support_classes.HtmlMailerMessage;
import jobs.support_classes.PersonDeviceInfo;
import play.Logger;
import play.twirl.api.Html;
import utils.CssInliner;
import utils.DateUtils;
import views.html.emaileventreport;

import java.util.Date;
import java.util.Map;

/**
 * Actor responsible for collecting event report data (from helper class DeviceReports).
 * Spawns child actors responsible for sending Html formatted emails to users/persons.
 */
public class EventReportActor extends UntypedActor {

    private final ActorRef htmlMailerActor;

    public static enum Message {
        DAILY_REPORT, WEEKLY_REPORT, MONTHLY_REPORT, FULL_REPORT;
    }

    public EventReportActor() {
        this.htmlMailerActor = getContext().actorOf(Props.create(jobs.HtmlMailerActor.class), "htmlMailerActor");
    }

    @Override
    public void onReceive(Object msg) {
        if (msg instanceof Message) {
            long currentTimestamp = new Date().getTime();
            long startTimestamp;

            switch ((Message) msg) {
                case DAILY_REPORT:
                    startTimestamp = DateUtils.getPastTime(currentTimestamp, DateUtils.TimeUnit.Day);
                    prepareAndSendmail(startTimestamp, currentTimestamp, Message.DAILY_REPORT);
                    break;
                case WEEKLY_REPORT:
                    startTimestamp = DateUtils.getPastTime(currentTimestamp, DateUtils.TimeUnit.Week);
                    prepareAndSendmail(startTimestamp, currentTimestamp, Message.WEEKLY_REPORT);
                    break;
                case MONTHLY_REPORT:
                    startTimestamp = DateUtils.getPastTime(currentTimestamp, DateUtils.TimeUnit.Month);
                    prepareAndSendmail(startTimestamp, currentTimestamp, Message.MONTHLY_REPORT);
                    break;
                case FULL_REPORT:
                    startTimestamp = 0L;
                    prepareAndSendmail(startTimestamp, currentTimestamp, Message.FULL_REPORT);
                    break;
                default:
                    Logger.error("EventReportActor received an invalid message.");
                    break;
            }
        }
        else {
            unhandled(msg);
        }
    }

    private void prepareAndSendmail(Long startTimestamp, Long endTimestamp, Message frequency) {
        Map<Long, PersonDeviceInfo> map = DeviceReports.generateAllDeviceReport(startTimestamp, endTimestamp, frequency);

        for (PersonDeviceInfo pdi : map.values()) {
            // Create Html template string.
            Html htmlTemplate = emaileventreport.render(pdi);
            String htmlString = CssInliner.toInlinedCss(htmlTemplate.toString());

            // Create message object to be sent to HtmlMailerActor
//            final HtmlMailerMessage htmlMessage = new HtmlMailerMessage("aghalarp@gmail.com", "OPQ Event Report", htmlString); // Uncomment on development.
            final HtmlMailerMessage htmlMessage = new HtmlMailerMessage(pdi.getPersonEmail(), "OPQ Event Report", htmlString); // Uncomment on production.

            //Create HtmlMailerActor and send message.
            //final ActorRef htmlMailer = getContext().actorOf(Props.create(jobs.HtmlMailerActor.class), "htmlMailerActor");
            this.htmlMailerActor.tell(htmlMessage, getSelf());
        }
    }

}
