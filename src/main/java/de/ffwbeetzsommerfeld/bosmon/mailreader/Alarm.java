package de.ffwbeetzsommerfeld.bosmon.mailreader;

import java.util.Date;
import java.util.Objects;

/**
 *
 * @author jhomuth
 */
public class Alarm {

    private String ric;

    private String message;

    private String fromAddress;

    private Date alarmTime;

    /**
     * Get the value of alarmTime
     *
     * @return the value of alarmTime
     */
    public Date getAlarmTime() {
        return alarmTime;
    }

    /**
     * Set the value of alarmTime
     *
     * @param alarmTime new value of alarmTime
     */
    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

    /**
     * Get the value of fromAddress
     *
     * @return the value of fromAddress
     */
    public String getFromAddress() {
        return fromAddress;
    }

    /**
     * Set the value of fromAddress
     *
     * @param fromAddress new value of fromAddress
     */
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    /**
     * Get the value of content
     *
     * @return the value of content
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the value of content
     *
     * @param message new value of content
     */
    public void setMessage(String message) {
        if (message != null) {
            message = message.trim();
        }
        this.message = message;
    }

    /**
     * Get the value of subject
     *
     * @return the value of subject
     */
    public String getRic() {
        return ric;
    }

    /**
     * Set the value of subject
     *
     * @param ric new value of subject
     */
    public void setRic(String ric) {
        if (ric != null) {
            ric = ric.trim();
        }
        this.ric = ric;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return Boolean.FALSE;
        }
        try {
            Alarm alarm = (Alarm) obj;
            if (this.getRic().equals(alarm.getRic())
                    && this.getMessage().equals(alarm.getMessage())) {
                return Boolean.TRUE;
            }
        } catch (ClassCastException e) {
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.ric);
        hash = 61 * hash + Objects.hashCode(this.message);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n").append("Alarm ").append(this.getAlarmTime()).append("\n");
        stringBuilder.append("\t").append("RIC: ").append(this.getRic()).append("\n");
        stringBuilder.append("\t").append("Meldung: ").append(this.getMessage()).append("\n");
        return stringBuilder.toString();
    }
}
