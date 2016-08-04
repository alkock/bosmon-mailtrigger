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

    private HashMap<String, Date> firedAlarms = new HashMap<>();

    private static final Logger LOG = Logger.getLogger(BosMonExecutor.class.getName());

    
    public void fireAlarm(String alarmText) {
        try {
            if (this.isAllowedToFire(alarmText)) {
                this.callBosMon(alarmText);
            } else {
                LOG.warning("The alarm was already fired. Skipping....");
            }
        } catch (BosMonTriggerExecutionException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            this.storeAlarm(alarmText);
        }
    }

    /**
     * This method will trigger BosMon to fire the alarm
     * @param alarmText The text you want to provide to BosMon
     * @throws BosMonTriggerExecutionException 
     */
    private void callBosMon(String alarmText) throws BosMonTriggerExecutionException {
        try {
            Runtime.getRuntime().exec("cmd /c start bosmon-trigger.bat");
        } catch (IOException ex) {
            throw new BosMonTriggerExecutionException("Unable to execute BosMon Trigger script", ex);
        }
//        curl --basic -u "Benutzername:Passwort" http://lokale IP:Port/telegramin/in1/input.xml --data "type=pocsag&address=1234567&flags=0&function=a&message=Hallo" -vk        
    }

    ;
    
    /**
     * Stores an alarm as fired
     * @param alarmText 
     */
    private void storeAlarm(String alarmText) {
        firedAlarms.put(alarmText, Calendar.getInstance().getTime());
    }

    /**
     * This methods checks whether an alarm has already been provided to BosMon
     *
     * @param alarmText The text you want to emit
     * @return
     */
    private boolean isAllowedToFire(String alarmText) {
        boolean wasFireAlready = firedAlarms.containsKey(alarmText);
        Boolean isAllowedToFire = Boolean.TRUE;
        if (wasFireAlready) {
            /* Check the time if the alarm was fired already */
            Date lastEmitTime = firedAlarms.get(alarmText);
            if (Calendar.getInstance().getTimeInMillis() - lastEmitTime.getTime() < (1000 * 60 * 5)) {
                /* If the timespan is less than the configured time - dont emit to bosmon */
                isAllowedToFire = Boolean.FALSE;
            }
        }
        return isAllowedToFire;
    }

}
