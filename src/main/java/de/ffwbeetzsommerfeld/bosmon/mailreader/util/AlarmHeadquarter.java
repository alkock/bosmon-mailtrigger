package de.ffwbeetzsommerfeld.bosmon.mailreader.util;

import de.ffwbeetzsommerfeld.bosmon.mailreader.Alarm;
import java.util.List;

/**
 * Simples observer pattern
 * @author jhomuth
 */
public interface AlarmHeadquarter {
    
    public void deliverAlarms(List<Alarm> alarms);
    
    public void registerRecipient(Recipient recipient);
    
    public void deRegisterRecipient(Recipient recipient);
    
    
    
}
