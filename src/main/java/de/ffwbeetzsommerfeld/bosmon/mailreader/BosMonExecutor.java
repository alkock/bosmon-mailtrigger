package de.ffwbeetzsommerfeld.bosmon.mailreader;

import de.ffwbeetzsommerfeld.bosmon.mailreader.execute.Executor;
import de.ffwbeetzsommerfeld.bosmon.mailreader.execute.HttpClientExecuter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * Diese Klasse ist für die Weiterleitung der Alarme zur BosMon Instanz
 * zuständig.
 *
 * @author jhomuth
 */
public class BosMonExecutor implements Runnable {

    /**
     * Eine Map aller bereits ausgeführten Alarme. Diese Map wird nicht
     * persistiert und merkt sich daher nur die Alarme seit der letzten
     * Ausführung.
     */
    private static HashMap<Alarm, Date> executedAlarms = new HashMap<>();

    /**
     * Die Alarme die bei der Ausführung dieses Threads weitergeleitet werden
     * sollen
     */
    private List<Alarm> alarmsToFire = new ArrayList<>();

    /**
     * Logger für diese Klasse
     */
    private static final Logger LOG = Logger.getLogger(BosMonExecutor.class.getSimpleName());

    /**
     * Konstruktor
     *
     * @param alarms
     */
    public BosMonExecutor(List<Alarm> alarms) {
        this.alarmsToFire = alarms;
    }

    /**
     * Diese Methode leitet die übergebene Liste von Alarmen an BosMon weiter,
     * sofern für die Alarme alle Bedingungen zutreffen.
     *
     * @param alarms Die Liste von auszuführenden Alarmen
     */
    private void fireAlarms(List<Alarm> alarms) {
        for (Alarm alarm : alarms) {
            this.fireAlarm(alarm);
        }
    }

    /**
     * Diese Methode führt den übergebenen Alarm aus (Weiterleitung an BosMon)
     *
     * @param alarm Der weiterzuleitende Alarm
     */
    private void fireAlarm(Alarm alarm) {
        LOG.log(Level.INFO, "Verarbeite Alarm {0}", alarm.toString());
        try {
            AlarmExecutionStatus state = this.isAllowedToFire(alarm);
            if (state.getIsAllowedToBeFired()) {
                this.callBosMon(alarm);
            } else {
                LOG.warning("Alarm wird nicht weitergeleitet.");
                for (AlarmValidationFailure failure : state.getValidationErrors()) {
                    LOG.warning(failure.getValidationMessage());
                }
            }
        } catch (BosMonTriggerExecutionException ex) {
            LOG.log(Level.SEVERE, "Alarm konnte nicht an BosMon weitergereicht werden", ex);
        } finally {
            this.storeAlarm(alarm);
        }
    }

    /**
     * Diese Methode übernimmt die technische Weiterleitung an BosMon via
     * BosMonDial
     *
     * @param alarm Der weiterzuleitende Alarm
     * @throws BosMonTriggerExecutionException Im Fall das ein Fehler bei der
     * Ausführung von BosMonDial aufgetreten ist.
     */
    private void callBosMon(Alarm alarm) throws BosMonTriggerExecutionException {
        Executor executor = new HttpClientExecuter();
        executor.execute(alarm);
    }

    /**
     * Diese Methode speichert einen Alarm als ausgeführt ab.
     *
     * @param alarm Der zu merkende Alarm
     */
    private synchronized void storeAlarm(Alarm alarm) {
        executedAlarms.put(alarm, Calendar.getInstance().getTime());
    }

    /**
     * Diese Methode prüft ob ein Alarm ausgeführt werden darf. Es werden
     * folgende Fälle geprüft.
     * <ul>
     * <li>Absender-Adresse korrekt?</li>
     * <li>Wurde der Alarm bereits ausgeführt? Unterdrückung mehrfacher
     * Alarmierung innerhalb konfigurierbarer Zeit.</li>
     * <li>Ist der Alarm zu alt? Keine nachträgliche Alarmierung wenn das
     * Programm längere Zeit unterbrochen wurde.</li>
     * </ul>
     *
     * @param alarm
     * @return
     */
    private synchronized AlarmExecutionStatus isAllowedToFire(Alarm alarm) {
        AlarmExecutionStatus status = new AlarmExecutionStatus();
        boolean wasFireAlready = executedAlarms.containsKey(alarm);
        Boolean isAllowedToFire = Boolean.TRUE;

        /* Überprüfe Absender-Adresse */
        if (Boolean.valueOf(Config.get(Config.KEY_SENDER_ADDRESS_VALIDATION))) {
            if (alarm.getFromAddress() == null || !alarm.getFromAddress().contains(Config.get(Config.KEY_ALLOWED_SENDER))) {
                isAllowedToFire = Boolean.FALSE;
                status.addValidationError(AlarmValidationFailure.WRONG_SENDER_ADDRESS);
            }
        }

        /* Überprüfe ob der Alarm bereits ausgeführt wurde */
        if (wasFireAlready) {
            /* Wenn der Alarm bereits ausgeführt wurde überprüfe die abgelaufene Zeit. Eventuell Nachalarmierung? */
            Date lastEmitTime = executedAlarms.get(alarm);
            if ((Calendar.getInstance().getTimeInMillis() - lastEmitTime.getTime()) < (1000 * 60 * new Long(Config.get(Config.KEY_ALARM_SUPRESS_TIME)))) {
                /* Wenn die konfigurierte Zeitspanne noch nicht abgelaufen ist, sorge dafür das der Alarm nicht nochmals an BosMon übertragen wurde */
                isAllowedToFire = Boolean.FALSE;
                status.addValidationError(AlarmValidationFailure.ALARM_ALREADY_FIRED);
            }
        }

        /* Überprüfe das generelle Alter des Alarms. Sollte dieses Programm längere Zeit unterbrochen gewesen sein, sollten alte Alarme nicht nochmal gesendet werden. */
        if ((Calendar.getInstance().getTime().getTime() - alarm.getAlarmTime().getTime()) > (1000L * 60L * new Long(Config.get(Config.KEY_MAX_ALARM_AGE)))) {
            isAllowedToFire = Boolean.FALSE;
            status.addValidationError(AlarmValidationFailure.ALARM_TO_OLD);
        }
        status.setIsAllowedToBeFired(isAllowedToFire);
        return status;
    }

    @Override
    public void run() {
        this.fireAlarms(alarmsToFire);
    }

}
