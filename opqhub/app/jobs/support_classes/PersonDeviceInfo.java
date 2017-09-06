package jobs.support_classes;

import java.util.List;

/**
 * Specialization class to hold data linking a Person with his/her devices.
 */
public class PersonDeviceInfo {
    private String personFirstName;
    private String personLastName;
    private String personEmail;
    private String emailFrequency;
    private List<DeviceStats> deviceStatsList;
    private Integer numDevices;

    public PersonDeviceInfo(String personFirstName, String personLastName, String personEmail, String emailFrequency,
                            List<DeviceStats> deviceStatsList) {
        this.personFirstName = personFirstName;
        this.personLastName = personLastName;
        this.personEmail = personEmail;
        this.emailFrequency = emailFrequency;
        this.deviceStatsList = deviceStatsList;
        this.numDevices = deviceStatsList.size();
    }

    public String getPersonFirstName() {
        return personFirstName;
    }

    public void setPersonFirstName(String personFirstName) {
        this.personFirstName = personFirstName;
    }

    public String getPersonLastName() {
        return personLastName;
    }

    public void setPersonLastName(String personLastName) {
        this.personLastName = personLastName;
    }

    public String getPersonEmail() {
        return personEmail;
    }

    public void setPersonEmail(String personEmail) {
        this.personEmail = personEmail;
    }

    public List<DeviceStats> getDeviceStatsList() {
        return deviceStatsList;
    }

    public void setDeviceStatsList(List<DeviceStats> deviceStatsList) {
        this.deviceStatsList = deviceStatsList;
        this.numDevices = deviceStatsList.size();
    }

    public Integer getNumDevices() {
        return numDevices;
    }

    public String getEmailFrequency() {
        return emailFrequency;
    }

    public void setEmailFrequency(String emailFrequency) {
        this.emailFrequency = emailFrequency;
    }
}

