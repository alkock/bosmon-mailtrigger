package de.ffwbeetzsommerfeld.bosmon.mailreader.execute;

import de.ffwbeetzsommerfeld.bosmon.mailreader.Alarm;
import de.ffwbeetzsommerfeld.bosmon.mailreader.BosMonTriggerExecutionException;

/**
 * Interface für alle Klassen die die weitergabe an BosMon implementieren
 * wollen.
 *
 * Mögliche Beispiele wären:
 * <ol>
 * <li>BosMonDial-Executor</li>
 * <li>Curl-Executor</li>
 * <li>Standard-HTTP-Executor</li>
 * </ol>
 *
 * @author jhomuth
 */
public interface Executor {

    /**
     * Leitet den übergebenen Alarm weiter an BosMon.
     *
     * @param alarm Der weiterzuleitende Alarm
     * @throws
     * de.ffwbeetzsommerfeld.bosmon.mailreader.BosMonTriggerExecutionException
     */
    public void execute(Alarm alarm) throws BosMonTriggerExecutionException;

}
