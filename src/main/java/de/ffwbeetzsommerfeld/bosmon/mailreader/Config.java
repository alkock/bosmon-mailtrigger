package de.ffwbeetzsommerfeld.bosmon.mailreader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasse dient zum holen von Konfigurationsparametern
 * 
 * @author jhomuth
 */
public class Config {

    private static Properties bosMonMailReaderProps = new Properties();
    
    /**
     * Key für die Konfigurationseinstellung der Email Server URL
     */
    public static final String KEY_PROP_IMAP_SERVER = "IMAP_SERVER";
    /**
     * Key für die Konfigurationseinstellung des Email Benutzernames
     */
    public static final String KEY_PROP_IMAP_USER = "IMAP_USER";
    /**
     * Key für die Konfigurationseinstellung des Email Passworts
     */
    public static final String KEY_PROP_IMAP_PASS = "IMAP_PASS";
    /**
     * Key für die Konfigurationseinstellung des BosMon Benutzernames (Webserver)
     */
    public static final String KEY_BOSMON_USER = "BOSMON_USER";
    /**
     * Key für die Konfigurationseinstellung des BosMon Passworts (Webserver)
     */
    public static final String KEY_BOSMON_PASS = "BOSMON_PASS";
    /**
     * Key für die Konfigurationseinstellung des BosMon Webservers (Name)
     */
    public static final String KEY_BOSMON_CHANNEL_NAME = "BOSMON_CHANNEL_NAME";
    /**
     * Key für die Konfigurationseinstellung des BosMon Servername (localhost oder ip, etc.) (Webserver)
     */
    public static final String KEY_BOSMON_SERVER_NAME = "BOSMON_SERVER_NAME";
    /**
     * Key für die Konfigurationseinstellung des BosMon Webserver Ports
     */
    public static final String KEY_BOSMON_SERVER_PORT = "BOSMON_SERVER_PORT";
    /**
     * Key für die Konfigurationseinstellung der erlaubten Absender-Adresse
     */
    public static final String KEY_ALLOWED_SENDER = "ALLOWED_SENDER";
    /**
     * Key für die Konfigurationseinstellung ob Absender-Adressprüfung an oder aus ist
     */
    public static final String KEY_SENDER_ADDRESS_VALIDATION = "SENDER_ADDRESS_VALIDATION";
    /**
     * Key für die Konfigurationseinstellung des Pfades zur BosMonDial.exe
     */
    public static final String KEY_BOSMON_DIAL_EXE = "BOSMON_DIAL_EXE";
    /**
     * Key für die Konfigurationseinstellung des maximalen alters einer Alarm-Email (in Minuten)
     */
    public static final String KEY_MAX_ALARM_AGE = "MAX_ALARM_AGE";
    /**
     * Key für die Konfigurationseinstellung des Unterdrückungszeit bei Mehrfachalarmierung
     */
    public static final String KEY_ALARM_SUPRESS_TIME = "ALARM_SUPRESS_TIME";
    
    /**
     * Key für die Konfigurationseinstellung der Wartezeit zwischen zwei Email-Abholvorgängen
     */
    public static final String KEY_POLLING_SECONDS = "POLLING_SECONDS";
    /**
     * Key für die Konfigurationseinstellung ob der Email Abrufprozess auf Debug laufen soll
     */
    public static final String KEY_MAIL_TRANSFER_DEBUG = "DEBUG_MAIL";

    private Config() {

    }

    /**
     * Initialisiert die Konfiguration. Liest die Konfiguration aus der übergebenen Datei aus.
     * @param properties 
     */
    public static void init(File properties) {
        try {
            bosMonMailReaderProps.load(new FileReader(properties));
        } catch (IOException ex) {
            Logger.getLogger(Postman.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    /**
     * Holt den konfigurierten Wert für übergebenen Key aus der Konfiguration
     * @param key 
     * @return 
     */
    public static String get(String key) {
        return bosMonMailReaderProps.getProperty(key);
    }
}
