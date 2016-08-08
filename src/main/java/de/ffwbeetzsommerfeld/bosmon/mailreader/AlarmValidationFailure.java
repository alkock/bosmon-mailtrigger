package de.ffwbeetzsommerfeld.bosmon.mailreader;

/**
 * Klasse beschreibt Validierungsfehler die bei einem Alarm auftreten können
 * 
 * @author jhomuth
 */
public enum AlarmValidationFailure {
    
    /**
     * Die Absenderadresse stimmt nicht mit der konfigurierten Adresse überein
     */
    WRONG_SENDER_ADDRESS {
        @Override
        public String getValidationMessage() {
            return "Alarm ist vom falschen Absender";
        }
    },
    /**
     * Dieser Alarm wurde bereits an BosMon übertragen
     */
    ALARM_ALREADY_FIRED {
        @Override
        public String getValidationMessage() {
             return "Alarm ist bereits verarbeitet";
        }
    },
    /**
     * Dieser Alarm ist zu alt.
     */
    ALARM_TO_OLD {
        @Override
        public String getValidationMessage() {
            return "Alarm ist zu alt";
        }
    };
    
    /**
     * Diese Methode liefert eine menschenlesbare Beschreibung des Validierungsfehlers
     * @return 
     */
    public abstract String getValidationMessage();
    
}
