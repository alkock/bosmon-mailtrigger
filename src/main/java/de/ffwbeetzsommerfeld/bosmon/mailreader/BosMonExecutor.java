/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ffwbeetzsommerfeld.bosmon.mailreader;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhomuth
 */
public class BosMonExecutor {

    /**
     * A map of all alarms send by this running instance of the BosMonExecutor
     * (not persisted)
     */
    private HashMap<Alarm, Date> firedAlarms = new HashMap<>();

    private static final Logger LOG = Logger.getLogger(BosMonExecutor.class.getName());

    public void fireAlarm(Alarm alarm) {
        try {
            if (this.isAllowedToFire(alarm)) {
                this.callBosMon(alarm);
            } else {
                LOG.warning("The alarm was already fired. Skipping....");
            }
        } catch (BosMonTriggerExecutionException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            this.storeAlarm(alarm);
        }
    }

    /**
     * This method will trigger BosMon to fire the alarm
     *
     * @param alarmText The text you want to provide to BosMon
     * @throws BosMonTriggerExecutionException
     */
    private void callBosMon(Alarm mail) throws BosMonTriggerExecutionException {
        String[] bosMonDialCommand = {Config.get(Config.BOSMON_DIAL_EXE),
            "-username " + Config.get(Config.BOSMON_USER),
            "-password " + Config.get(Config.BOSMON_PASS),
            "-alertaddress " + mail.getRic(),
            "-alertmessage " + mail.getMessage(),
            "-close"};
        try {
            Runtime.getRuntime().exec(bosMonDialCommand);
        } catch (IOException ex) {
            throw new BosMonTriggerExecutionException("Unable to execute BosMonDial", ex);
        }
    }

    /**
     * Stores an alarm as fired
     *
     * @param alarmText
     */
    private void storeAlarm(Alarm alarm) {
        firedAlarms.put(alarm, Calendar.getInstance().getTime());
    }

    /**
     * This methods checks whether an alarm has already been provided to BosMon
     *
     * @param alarm The text you want to emit
     * @return
     */
    private boolean isAllowedToFire(Alarm alarm) {
        boolean wasFireAlready = firedAlarms.containsKey(alarm);
        Boolean isAllowedToFire = Boolean.TRUE;
        if (wasFireAlready) {
            /* Check the time if the alarm was fired already */
            Date lastEmitTime = firedAlarms.get(alarm);
            if (Calendar.getInstance().getTimeInMillis() - lastEmitTime.getTime() < (1000 * 60 * 5)) {
                /* If the timespan is less than the configured time - dont emit to bosmon */
                isAllowedToFire = Boolean.FALSE;
            }
        }
        return isAllowedToFire;
    }

}
