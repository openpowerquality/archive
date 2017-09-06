package jobs.reports;

import com.avaje.ebean.Ebean;
import jobs.EventReportActor;
import jobs.support_classes.DeviceStats;
import jobs.support_classes.PersonDeviceInfo;
import models.AccessKey;
import models.Event;
import models.Person;
import org.openpowerquality.protocol.OpqPacket;
import play.Logger;

import java.util.*;

/**
 * Contains various methods for gathering OPQBox related data and statistics.
 */
public class DeviceReports {

    /**
     * Within a given timestamp interval, returns a mapping of Person id's to PersonDeviceInfo objects,
     * which contains information relating each Person to their OPQBox devices.
     *
     * @param startTimestamp The starting time interval
     * @param endTimestamp The ending time interval
     * @param frequency The emailer frequency setting to check
     * @return Mapping of Person IDs to PersonDeviceInfo
     */
    public static Map<Long, PersonDeviceInfo> generateAllDeviceReport(Long startTimestamp, Long endTimestamp, EventReportActor.Message frequency) {
        Map<Long, PersonDeviceInfo> retMap = new HashMap<Long, PersonDeviceInfo>();
        String strFrequency = null; // Used later in PDI object

        // Get all people.
        //List<Person> persons = Person.getPersons();
        List<Person> persons = null;
        switch (frequency) {
            case DAILY_REPORT:
                persons = Person.find().where()
                        .eq("enableEmailAlerts", true)
                        .eq("enableEmailSummaryNotifications", true)
                        .eq("emailNotifyDaily", true)
                        .findList();
                strFrequency = "Daily";
                break;
            case WEEKLY_REPORT:
                persons = Person.find().where()
                        .eq("enableEmailAlerts", true)
                        .eq("enableEmailSummaryNotifications", true)
                        .eq("emailNotifyDaily", true)
                        .findList();
                strFrequency = "Weekly";
                break;
            default:
                Logger.error("Invalid mailer frequency in DeviceReports.");
                break;
        }


        // For each person, grab event stats for each device associated to person's account.
        for (Person person : persons) {
            // A DeviceStats object holds deviceID, freqEventCount, and voltEventCount.
            List<DeviceStats> deviceStatsList = new ArrayList<DeviceStats>();

            // Reminder: Each AccessKey object holds a 1-1 relationship with an OpqDevice object.
            Set<AccessKey> accessKeys = person.getAccessKeys();
            for (AccessKey accessKey : accessKeys) {
                //For each device, grab deviceID, freqEvents, voltEvents, lastHeartbeat
                Integer freqEventCount = Event.find().where()
                        .eq("accessKey", accessKey)
                        .eq("eventType", OpqPacket.PacketType.EVENT_FREQUENCY)
                        .ge("timestamp", startTimestamp)
                        .le("timestamp", endTimestamp)
                        .findRowCount();

                Integer voltEventCount = Event.find().where()
                        .eq("accessKey", accessKey)
                        .eq("eventType", OpqPacket.PacketType.EVENT_VOLTAGE)
                        .ge("timestamp", startTimestamp)
                        .le("timestamp", endTimestamp)
                        .findRowCount();

                //Long lastHeartbeat = getLastHeartbeatTimestamp(accessKey);
                Long devLastHeartbeat = accessKey.getOpqDevice().getLastHeartbeat();

                //calculateUptime(accessKey, startTimestamp, endTimestamp);

                // Add to DeviceStats object.
                DeviceStats ds = new DeviceStats(accessKey.getDeviceId(), freqEventCount, voltEventCount, devLastHeartbeat);
                deviceStatsList.add(ds);
            }

            // Some users may have not yet associated a device to their account, so don't add those users.
            if (!accessKeys.isEmpty()) {
                PersonDeviceInfo pdi = new PersonDeviceInfo(person.getFirstName(), person.getLastName(),
                        person.getAlertEmail(), strFrequency,  deviceStatsList);

                retMap.put(person.getPrimaryKey(), pdi);
            }
        }

        return retMap;
    }

    /**
     * Retrieves OPQBox device related data/statistics of the given user, between a given time-frame.
     *
     * @param email The user/Person's email account name.
     * @param startTimestamp The starting time interval.
     * @param endTimestamp The ending time interval.
     * @return A PersonDeviceInfo object containing the user's device related data and statistics.
     */
    public static PersonDeviceInfo generateDeviceReport(String email, Long startTimestamp, Long endTimestamp) {
        Person person = Person.find().where().eq("email", email).findUnique();
        if (person.equals(null)) {
            return null;
        }

        List<DeviceStats> deviceStatsList = new ArrayList<DeviceStats>();

        // Reminder: Each AccessKey object holds a 1-1 relationship with an OpqDevice object.
        Set<AccessKey> accessKeys = person.getAccessKeys();
        for (AccessKey accessKey : accessKeys) {
            //For each device, grab information we require.
            Integer freqEventCount = Event.find().where()
                    .eq("accessKey", accessKey)
                    .eq("eventType", OpqPacket.PacketType.EVENT_FREQUENCY)
                    .ge("timestamp", startTimestamp)
                    .le("timestamp", endTimestamp)
                    .findRowCount();

            Integer voltEventCount = Event.find().where()
                    .eq("accessKey", accessKey)
                    .eq("eventType", OpqPacket.PacketType.EVENT_VOLTAGE)
                    .ge("timestamp", startTimestamp)
                    .le("timestamp", endTimestamp)
                    .findRowCount();

            Long devLastHeartbeat = accessKey.getOpqDevice().getLastHeartbeat();

            // Save data to DeviceStats object.
            DeviceStats ds = new DeviceStats(accessKey.getDeviceId(), freqEventCount, voltEventCount, devLastHeartbeat);
            deviceStatsList.add(ds);
        }

        // Some users may have not yet associated a device to their account, so ignore those users.
        if (!accessKeys.isEmpty()) {
            PersonDeviceInfo pdi = new PersonDeviceInfo(person.getFirstName(), person.getLastName(),
                    person.getAlertEmail(), "", deviceStatsList);

            return pdi;
        }

        return null;
    }

    /**
     * Finds the most recent heartbeat timestamp of a given AccessKey/Device.
     *
     * @param accKey The AccessKey to query.
     * @return The most recent heartbeat timestamp, or null if invalid AccessKey is given.
     */
    public static Long getLastHeartbeatTimestamp(AccessKey accKey) {
        Event event = Event.find().where()
                .eq("accessKey", accKey)
                .eq("eventType", OpqPacket.PacketType.EVENT_HEARTBEAT)
                .orderBy("timestamp desc")
                .setMaxRows(1)
                .findUnique();

        if (event != null) {
            return event.getTimestamp();
        }

        return null;
    }

    /**
     * Experimental method to calculate OPQBox uptime.
     *
     * @param accessKey
     * @param startTimestamp
     * @param endTimestamp
     * @return
     */
    public static Double calculateUptime(AccessKey accessKey, Long startTimestamp, Long endTimestamp) {
        // Get list of all heartbeat events within given time-frame. If no events, then we exit method early.
        List<Event> events = Event.find().where()
                .eq("accessKey", accessKey)
                .eq("eventType", OpqPacket.PacketType.EVENT_HEARTBEAT)
                .ge("timestamp", startTimestamp)
                .le("timestamp", endTimestamp)
                .orderBy("timestamp asc")
                .findList();

        Integer actualHeartbeats = events.size(); // Size of list represents number of HB events within time-frame.

        if (actualHeartbeats.equals(0)) {
            return null; // If no heartbeat events found for given accessKey, exit early.
        }

        Long avgHBInterval = deviceAvgHBInterval(accessKey); // Get avg interval of given device.
        Long expectedNumHeartbeats = (endTimestamp - startTimestamp) / avgHBInterval;
        Double uptime = actualHeartbeats / expectedNumHeartbeats.doubleValue(); // Actual / Expected heartbeats.
        System.out.println("Uptime:" + uptime*100);

        return uptime;
    }

    /**
     * Given a device access key, calculates the average interval between all heartbeats from that device.
     * Currently parses through all historical data for the given device. Room for optimization.
     *
     * @param accessKey The AccessKey/Device to check.
     * @return The average heartbeat interval.
     */
    public static Long deviceAvgHBInterval(AccessKey accessKey) {
        // Gets ALL heartbeat events of the given device.
        List<Event> events = Event.find().where()
                .eq("accessKey", accessKey)
                .eq("eventType", OpqPacket.PacketType.EVENT_HEARTBEAT)
                .ge("timestamp", 0L)
                .le("timestamp", new Date().getTime())
                .orderBy("timestamp asc")
                .findList();

        if (events.size() == 0) {
            return null; // If no heartbeat events found for given accessKey, exit early.
        }

        Integer count = 0;
        Long sumIntervals = 0L;
        Long prevTimestamp = 0L;

        for (Event event : events) {
            Long currTimestamp = event.getTimestamp();
            if (count != 0) {
                Long interval = currTimestamp - prevTimestamp;
                sumIntervals += interval;
            }
            prevTimestamp = currTimestamp;
            count++;
        }

        Long avgInterval = sumIntervals/(count-1); // count-1 because count is 1 larger than actual number of intervals summed.
        System.out.println("Avg Interval: " + avgInterval);
        return avgInterval;
    }


    /**
     * Same as generateAllDeviceReport(), except using ebean filters. Wanted to see the performance difference.
     * Note: There is a bug in ebean 3.3.2 with the way filters handle enums that causes this method to hit a runtime
     * exception. After some searching around, this issue seemed to have been resolved in the ebean 4.0.5 release.
     * See: https://github.com/ebean-orm/avaje-ebeanorm/issues/151
     *
     * @param startTimestamp
     * @param endTimestamp
     * @return
     */
    public static Map<Long, PersonDeviceInfo> filteredGenerateDeviceReport(Long startTimestamp, Long endTimestamp) {
        Map<Long, PersonDeviceInfo> retMap = new HashMap<Long, PersonDeviceInfo>();

        // Get all people.
        List<Person> persons = Person.getPersons();

        // For each person, grab event stats for each device associated to person's account.
        for (Person person : persons) {
            // A DeviceStats object holds deviceID, freqEventCount, and voltEventCount.
            List<DeviceStats> deviceStatsList = new ArrayList<DeviceStats>();

            // Reminder: Each AccessKey object holds a 1-1 relationship with an OpqDevice object.
            Set<AccessKey> accessKeys = person.getAccessKeys();
            for (AccessKey accessKey : accessKeys) {
                List<Event> events = Event.find().where().eq("accessKey", accessKey).findList();

                //For each device, grab deviceID, freqEvents, voltEvents, lastHeartbeat
                List<Event> filteredFreqEvents = Ebean.filter(Event.class)
                        .eq("eventType", OpqPacket.PacketType.EVENT_FREQUENCY)
                        .ge("timestamp", startTimestamp)
                        .le("timestamp", endTimestamp)
                        .filter(events);
                Integer freqEventCount = filteredFreqEvents.size();

                List<Event> filteredVoltEvents = Ebean.filter(Event.class)
                        .eq("eventType", OpqPacket.PacketType.EVENT_VOLTAGE)
                        .ge("timestamp", startTimestamp)
                        .le("timestamp", endTimestamp)
                        .filter(events);
                Integer voltEventCount = filteredVoltEvents.size();

                List<Event> lastHeartbeatEvent = Ebean.filter(Event.class)
                        .eq("eventType", OpqPacket.PacketType.EVENT_HEARTBEAT)
                        .sort("timestamp desc")
                        .maxRows(1)
                        .filter(events);

                Long lastHeartbeat = lastHeartbeatEvent.get(0).getTimestamp();
                System.out.println(lastHeartbeat);

                // Add to DeviceStats object.
                DeviceStats ds = new DeviceStats(accessKey.getDeviceId(), freqEventCount, voltEventCount, lastHeartbeat);
                deviceStatsList.add(ds);
            }

            // Some users may have not yet associated a device to their account, so don't add those users.
            if (!accessKeys.isEmpty()) {
                PersonDeviceInfo pdi = new PersonDeviceInfo(person.getFirstName(), person.getLastName(),
                        person.getAlertEmail(), "", deviceStatsList);

                retMap.put(person.getPrimaryKey(), pdi);
            }
        }

        return retMap;
    }


}
