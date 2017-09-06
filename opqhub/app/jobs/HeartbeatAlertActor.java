package jobs;

import akka.actor.UntypedActor;
import models.AccessKey;
import play.Logger;
import utils.DateUtils;
import utils.Mailer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HeartbeatAlertActor extends UntypedActor {
  private static final Map<AccessKey, Long> deviceHeartbeats = new HashMap<>();

  public static synchronized void update(AccessKey accessKey, Long timestamp) {
    // If this device is just coming up, let user know that its connected
    if(!deviceHeartbeats.containsKey(accessKey)) {
      Mailer.sendAlerts(accessKey.getPersons(), "OPQBox Online",
                        String.format("OPQBox with deviceId = %s has come online.", accessKey.getDeviceId()));
    }

    deviceHeartbeats.put(accessKey, timestamp);
  }

  @Override
  public void onReceive(Object message) {
    //System.out.println(message.toString());
    Set<AccessKey> deadDevices = checkHeartbeats();
    handleDeadDevices(deadDevices);
  }

  public Set<AccessKey> checkHeartbeats() {
    Logger.debug("Checking heartbeats");
    long currentTime = DateUtils.getMillis();
    long cutoff = DateUtils.getPastTime(currentTime, DateUtils.TimeUnit.Minute, 60);
    Set<AccessKey> deadDevices = new HashSet<>();

    for(AccessKey accessKey : deviceHeartbeats.keySet()) {
      if(deviceHeartbeats.get(accessKey) < cutoff) {
        deadDevices.add(accessKey);
      }
    }

    return deadDevices;
  }

  public void handleDeadDevices(Set<AccessKey> deadDevices) {
    Logger.debug(String.format("Removing %d dead devices %s", deadDevices.size(), deadDevices));
    for(AccessKey accessKey : deadDevices) {
      Mailer.sendAlerts(accessKey.getPersons(), "OPQ Heartbeat Alert",
                        String.format("OPQBox with deviceId = %s not detected. The device was either shutoff or " +
                        "there is a problem with it.", accessKey.getDeviceId()));

      // Delete device from list
      deviceHeartbeats.remove(accessKey);
    }
  }
}
