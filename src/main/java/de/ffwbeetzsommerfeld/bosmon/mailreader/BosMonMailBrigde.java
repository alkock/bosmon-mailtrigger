package de.ffwbeetzsommerfeld.bosmon.mailreader;

import de.ffwbeetzsommerfeld.bosmon.mailreader.config.Config;
import de.ffwbeetzsommerfeld.bosmon.mailreader.config.ConfigurationException;
import de.ffwbeetzsommerfeld.bosmon.mailreader.util.Recipient;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

/**
 *
 * @author jhomuth
 */
public class BosMonMailBrigde implements Recipient {

    private Postman postman;

    private static final Logger LOG = Logger.getLogger(BosMonMailBrigde.class.getSimpleName());

    public static void main(String[] args) {
        try {
            Config.init(new File(args[0]));
        } catch (ConfigurationException ex) {
            LOG.log(Level.SEVERE, "Konfiguration nicht valide", ex);
            System.exit(1);
        }
        BosMonMailBrigde bridge = new BosMonMailBrigde();
        bridge.process();

    }

    public BosMonMailBrigde() {
        postman = new Postman();
        postman.registerRecipient(this);
    }

    public void process() {
        int pollingSeconds = new Integer(Config.get(Config.KEY_POLLING_SECONDS));
        while (true) {
            try {
                postman.fetchMailsAndQuitConnection();
                LOG.log(Level.FINER, String.format("Process finished.... Waiting %s Seconds", pollingSeconds));
                try {
                    Thread.sleep(1000 * pollingSeconds);
                } catch (InterruptedException ex) {
                    LOG.log(Level.WARNING, "BosMonBrigdeProcess interrupted... Bye");
                    System.exit(1);
                }
            } catch (MessagingException me) {
                LOG.log(Level.SEVERE, "Konnte Emails nicht abrufen", me);
            }
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
            LOG.log(Level.SEVERE, "Fehler bei der Verarbeitung", e);
        }

    }
}
