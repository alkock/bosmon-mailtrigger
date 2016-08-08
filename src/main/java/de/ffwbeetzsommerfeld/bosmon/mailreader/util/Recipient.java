package de.ffwbeetzsommerfeld.bosmon.mailreader.util;

import de.ffwbeetzsommerfeld.bosmon.mailreader.Alarm;
import java.util.List;

/**
 * Simples Observer pattern
 * @author jhomuth
 */
public interface Recipient {
    
    public void deliver(List<Alarm> alarms);
    
}
