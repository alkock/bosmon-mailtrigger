package de.ffwbeetzsommerfeld.bosmon.mailreader.execute;

import de.ffwbeetzsommerfeld.bosmon.mailreader.Alarm;
import de.ffwbeetzsommerfeld.bosmon.mailreader.BosMonTriggerExecutionException;
import de.ffwbeetzsommerfeld.bosmon.mailreader.Config;

/**
 *
 * @author jhomuth
 */
public interface Executor {

    /**
     *
     * @param alarm
     * @throws
     * de.ffwbeetzsommerfeld.bosmon.mailreader.BosMonTriggerExecutionException
     */
    public void execute(Alarm alarm) throws BosMonTriggerExecutionException;

}
