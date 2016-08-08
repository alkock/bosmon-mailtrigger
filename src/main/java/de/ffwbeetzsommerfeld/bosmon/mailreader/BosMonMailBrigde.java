package de.ffwbeetzsommerfeld.bosmon.mailreader;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhomuth
 */
public class BosMonMailBrigde {

    private final Postman postman = new Postman();

    private final BosMonExecutor executor = new BosMonExecutor();

    private static final Logger LOG = Logger.getLogger(BosMonMailBrigde.class.getSimpleName());

    public static void main(String[] args) {
        Config.init(new File(args[0]));
        BosMonMailBrigde bridge = new BosMonMailBrigde();
        bridge.process();

    }

    public void process() {
        int pollingSeconds = new Integer(Config.get(Config.POLLING_SECONDS));
        while (true) {
            try {
                List<Alarm> openAlarms = postman.fetchMails();
                if (openAlarms == null || openAlarms.isEmpty()) {
                    LOG.info("Keine neuen Alarme empfangen...");
                } else {
                    LOG.info(String.format("%s Alarm%sempfangen...", openAlarms.size(), openAlarms.size() == 1 ? " " : "e "));
                    executor.fireAlarms(openAlarms);
                }
            } catch (Throwable e) {
                LOG.log(Level.SEVERE, "Fehler bei der Verarbeitung", e);
            }

            LOG.log(Level.FINER, String.format("Process finished.... Waiting %s Seconds", pollingSeconds));

            try {
                Thread.sleep(1000 * pollingSeconds);
            } catch (InterruptedException ex) {
                LOG.log(Level.WARNING, "BosMonBrigdeProcess interrupted... Bye");
                System.exit(1);
            }
        }
    }

}
