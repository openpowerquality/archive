package jobs;

import akka.actor.UntypedActor;
import jobs.support_classes.HtmlMailerMessage;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import play.Play;

/**
 * Actor responsible for sending Html formatted emails.
 * Accepts an HtmlMailerMessage object.
 */
public class HtmlMailerActor extends UntypedActor {

    @Override
    public void onReceive(Object msg) {
        if (msg instanceof HtmlMailerMessage) {
            HtmlMailerMessage message = (HtmlMailerMessage) msg;
            String mailTo = message.getMailTo();
            String mailSubject = message.getMailSubject();
            String htmlTemplate = message.getHtmlTemplate();

            //Setup Mailer
            String smtpHost = Play.application().configuration().getString("smtp.host");
            Integer smtpPort = Play.application().configuration().getInt("smtp.port");
            String smtpUser = Play.application().configuration().getString("smtp.user");
            String smtpPass = Play.application().configuration().getString("smtp.pass");

            try {
                HtmlEmail email = new HtmlEmail();
                email.setHostName(smtpHost);
                email.setSmtpPort(smtpPort);
                email.setAuthenticator(new DefaultAuthenticator(smtpUser, smtpPass));
                email.setSSLOnConnect(true);
                email.setFrom(smtpUser);
                email.setSubject(mailSubject);
                email.setHtmlMsg(htmlTemplate);
                email.addTo(mailTo);
                email.send();
            } catch (EmailException e) {
                e.printStackTrace();
            }
        } else {
            unhandled(msg);
        }

    }
}
