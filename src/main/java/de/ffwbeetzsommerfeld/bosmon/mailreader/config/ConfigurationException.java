package de.ffwbeetzsommerfeld.bosmon.mailreader.config;

/**
 * Exception für den Fall das die Konfiguration nicht valide ist.
 * Eine Ausnahme dieser Art sollte zum Beenden des Programms führen
 * @author jhomuth
 */
public class ConfigurationException extends Exception {

    /**
     * Creates a new instance of <code>ConfigurationException</code> without
     * detail message.
     */
    public ConfigurationException() {
    }

    /**
     * Constructs an instance of <code>ConfigurationException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ConfigurationException(String msg) {
        super(msg);
    }
}
