package jobs.support_classes;

import utils.DateUtils;

/**
 * Specialization class holding certain device related data.
 */
public class DeviceStats {
    private Long deviceID;
    private Integer freqEventCount;
    private Integer voltEventCount;
    private Long lastHeartbeatTimestamp;
    private String readableLastHeartbeat;
    private Boolean isRecentLastHeartbeat = Boolean.FALSE;

    public DeviceStats(){

    }

    public DeviceStats(Long deviceID, Integer freqEventCount, Integer voltEventCount, Long lastHeartbeat) {
        this.deviceID = deviceID;
        this.freqEventCount = freqEventCount;
        this.voltEventCount = voltEventCount;
        this.setLastHeartbeatTimestamp(lastHeartbeat); // Note: This also sets readableLastHeartbeat.

        // Last Heartbeat is considered 'recent' if it was received within the last 5 minutes.
        if ((lastHeartbeat != null) && (DateUtils.getMillis() - lastHeartbeat) <= 300000) {
            this.isRecentLastHeartbeat = Boolean.TRUE;
        }
    }

    public Long getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(Long deviceID) {
        this.deviceID = deviceID;
    }

    public Integer getFreqEventCount() {
        return freqEventCount;
    }

    public void setFreqEventCount(Integer freqEventCount) {
        this.freqEventCount = freqEventCount;
    }

    public Integer getVoltEventCount() {
        return voltEventCount;
    }

    public void setVoltEventCount(Integer voltEventCount) {
        this.voltEventCount = voltEventCount;
    }

    public Long getLastHeartbeatTimestamp() {
        return lastHeartbeatTimestamp;
    }

    public void setLastHeartbeatTimestamp(Long lastHeartbeatTimestamp) {
        this.lastHeartbeatTimestamp = lastHeartbeatTimestamp;

        //Set readable timestamp automatically
        if (lastHeartbeatTimestamp != null) {
            this.readableLastHeartbeat = DateUtils.toDateTime(lastHeartbeatTimestamp);
        }
    }

    public String getReadableLastHeartbeat() {
        return readableLastHeartbeat;
    }

    public Boolean getIsRecentLastHeartbeat() {
        return isRecentLastHeartbeat;
    }

    public void setIsRecentLastHeartbeat(Boolean recentLastHeartbeat) {
        this.isRecentLastHeartbeat = recentLastHeartbeat;
    }
}
