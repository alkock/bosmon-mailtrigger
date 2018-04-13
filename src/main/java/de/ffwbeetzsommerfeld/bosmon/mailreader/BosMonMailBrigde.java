package de.ffwbeetzsommerfeld.bosmon.mailreader;

import de.ffwbeetzsommerfeld.bosmon.mailreader.config.Config;
import de.ffwbeetzsommerfeld.bosmon.mailreader.config.ConfigurationException;
import de.ffwbeetzsommerfeld.bosmon.mailreader.util.Recipient;
import java.io.File;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jhomuth
 */
public class BosMonMailBrigde implements Recipient {

    private Postman postman;

    private static final Logger LOG = LoggerFactory.getLogger(BosMonMailBrigde.class);

    public static Date lastSuccessFullExection;

    public static void main(String[] args) {
        try {
            Config.init(new File(args[0]));
        } catch (ConfigurationException ex) {
            LOG.error("Konfiguration nicht valide", ex);
            System.exit(1);
        }
        BosMonMailBrigde bridge = new BosMonMailBrigde();
        int pollingSeconds = new Integer(Config.get(Config.KEY_POLLING_SECONDS));
        while (true) {
            LOG.info("Going to fetch mails from server");
            bridge.process();
            LOG.trace(String.format("Process finished.... Waiting %s Seconds", pollingSeconds));
            try {
                Thread.sleep(1000 * pollingSeconds);
            } catch (InterruptedException ex) {
                LOG.warn("BosMonBrigdeProcess interrupted... Bye");
                System.exit(1);
            }
        }

    }

    public BosMonMailBrigde() {
        postman = new Postman();
        postman.registerRecipient(this);
    }

    public void process() {
        try {
            postman.fetchMailsAndQuitConnection();
        } catch (Exception me) {
            LOG.error("Konnte Emails nicht abrufen", me);
        }

    }

    /**
     * Callback when new alarms have arrived
     *
     * @param alarms
     */
    @Override
    public void deliver(List<Alarm> alarms) {
        try {
            LOG.info(String.format("%s Alarm%sempfangen...", alarms.size(), alarms.size() == 1 ? " " : "e "));
            BosMonExecutor bme = new BosMonExecutor(alarms);
            Thread t = new Thread(bme);
            t.start();

        } catch (Throwable e) {
            LOG.error("Fehler bei der Verarbeitung", e);
        }

    }
}
