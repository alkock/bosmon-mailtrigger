package de.ffwbeetzsommerfeld.bosmon.mailreader.util;

import de.ffwbeetzsommerfeld.bosmon.mailreader.Alarm;
import java.util.List;

/**
 * Simples observer pattern
 * @author jhomuth
 */
public interface AlarmHeadquarter {
    
    /**
     * Liefert alle Alarme an die registrierten Empfänger aus
     * @param alarms 
     */
    public void deliverAlarms(List<Alarm> alarms);
    
    /**
     * Registiert einen Empfänger bei der "Leitstelle"
     * @param recipient 
     */
    public void registerRecipient(Recipient recipient);
    
    /**
     * Derigistiert einen Empfänger bei der Leitstelle
     * @param recipient 
     */
    public void deRegisterRecipient(Recipient recipient);
    
    
    
}
