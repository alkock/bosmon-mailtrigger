package de.ffwbeetzsommerfeld.bosmon.mailreader;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(BosMonExecutor.class.getSimpleName());

    /**
     * This method will fire the provided list of alarms
     *
     * @param alarms
     */
    public void fireAlarms(List<Alarm> alarms) {
        for (Alarm alarm : alarms) {
            this.fireAlarm(alarm);
        }
    }

    /**
     * Method will fire the alarm provided to bosmon if the alarm wasn't
     * executed before
     *
     * @param alarm
     */
    public void fireAlarm(Alarm alarm) {
        LOG.log(Level.INFO, "Processing alarm {0}", alarm.toString());
        try {
            AlarmFireStatus state = this.isAllowedToFire(alarm);
            if (state.getIsAllowedToBeFired()) {
                this.callBosMon(alarm);
            } else {
                LOG.warning("Alarm wird nicht weitergeleitet.");
                for (AlarmValidationFailure failure : state.getValidationErrors()) {
                    LOG.warning(failure.getValidationMessage());
                }
            }
        } catch (BosMonTriggerExecutionException ex) {
            LOG.log(Level.SEVERE, "Unable to call bosmon", ex);
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
    private void callBosMon(Alarm alarm) throws BosMonTriggerExecutionException {
        String[] bosMonDialCommand = {Config.get(Config.BOSMON_DIAL_EXE),
            "-username " + Config.get(Config.BOSMON_USER),
            "-password " + Config.get(Config.BOSMON_PASS),
            "-alertaddress " + alarm.getRic(),
            "-alertmessage " + alarm.getMessage(),
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
    private AlarmFireStatus isAllowedToFire(Alarm alarm) {
        AlarmFireStatus status = new AlarmFireStatus();
        boolean wasFireAlready = firedAlarms.containsKey(alarm);
        Boolean isAllowedToFire = Boolean.TRUE;

        /* Check sender address */
        if (Boolean.valueOf(Config.get(Config.SENDER_ADDRESS_VALIDATION))) {
            if (alarm.getFromAddress() == null || !alarm.getFromAddress().contains(Config.get(Config.ALLOWED_SENDER))) {
                isAllowedToFire = Boolean.FALSE;
                status.addValidationError(AlarmValidationFailure.WRONG_SENDER_ADDRESS);
            }
        }

        /* Check for already fired alarm */
        if (wasFireAlready) {
            /* Check the time if the alarm was fired already */
            Date lastEmitTime = firedAlarms.get(alarm);
            if ((Calendar.getInstance().getTimeInMillis() - lastEmitTime.getTime()) < (1000 * 60 * new Long(Config.get(Config.ALARM_SUPRESS_TIME)))) {
                /* If the timespan is less than the configured time - dont emit to bosmon */
                isAllowedToFire = Boolean.FALSE;
                status.addValidationError(AlarmValidationFailure.ALARM_ALREADY_FIRED);
            }
        }

        /* Check if the alarm is too old */
        if ((Calendar.getInstance().getTime().getTime() - alarm.getAlarmTime().getTime()) > (1000L * 60L * new Long(Config.get(Config.MAX_ALARM_AGE)))) {
            isAllowedToFire = Boolean.FALSE;
            status.addValidationError(AlarmValidationFailure.ALARM_TO_OLD);
        }
        status.setIsAllowedToBeFired(isAllowedToFire);
        return status;
    }

}
