package de.ffwbeetzsommerfeld.bosmon.mailreader.config;

import de.ffwbeetzsommerfeld.bosmon.mailreader.Postman;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @ConfigParam(mandatory = true)
    public static final String KEY_PROP_IMAP_SERVER = "IMAP_SERVER";
    /**
     * Key für die Konfigurationseinstellung des Email Benutzernames
     */
    @ConfigParam(mandatory = true)
    public static final String KEY_PROP_IMAP_USER = "IMAP_USER";
    /**
     * Key für die Konfigurationseinstellung des Email Passworts
     */
    @ConfigParam(mandatory = true)
    public static final String KEY_PROP_IMAP_PASS = "IMAP_PASS";
    /**
     * Key für die Konfigurationseinstellung des BosMon Benutzernames
     * (Webserver)
     */
    @ConfigParam(mandatory = false)
    public static final String KEY_BOSMON_USER = "BOSMON_USER";
    /**
     * Key für die Konfigurationseinstellung des BosMon Passworts (Webserver)
     */
    @ConfigParam(mandatory = false)
    public static final String KEY_BOSMON_PASS = "BOSMON_PASS";
    /**
     * Key für die Konfigurationseinstellung des BosMon Webservers (Name)
     */
    @ConfigParam(mandatory = false, defaultValue = "in")
    public static final String KEY_BOSMON_CHANNEL_NAME = "BOSMON_CHANNEL_NAME";
    /**
     * Key für die Konfigurationseinstellung des BosMon Servername (localhost
     * oder ip, etc.) (Webserver)
     */
    @ConfigParam(mandatory = false, defaultValue = "127.0.0.1")
    public static final String KEY_BOSMON_SERVER_NAME = "BOSMON_SERVER_NAME";
    /**
     * Key für die Konfigurationseinstellung des BosMon Webserver Ports
     */
    @ConfigParam(mandatory = false, defaultValue = "80")
    public static final String KEY_BOSMON_SERVER_PORT = "BOSMON_SERVER_PORT";
    /**
     * Key für die Konfigurationseinstellung der erlaubten Absender-Adresse
     */
    @ConfigParam(mandatory = false, defaultValue = "")
    public static final String KEY_ALLOWED_SENDER = "ALLOWED_SENDER";
    /**
     * Key für die Konfigurationseinstellung ob Absender-Adressprüfung an oder
     * aus ist
     */
    @ConfigParam(mandatory = false, defaultValue = "false")
    public static final String KEY_SENDER_ADDRESS_VALIDATION = "SENDER_ADDRESS_VALIDATION";
    /**
     * Key für die Konfigurationseinstellung des maximalen alters einer
     * Alarm-Email (in Minuten)
     */
    @ConfigParam(mandatory = false, defaultValue = "5")
    public static final String KEY_MAX_ALARM_AGE = "MAX_ALARM_AGE";
    /**
     * Key für die Konfigurationseinstellung des Unterdrückungszeit bei
     * Mehrfachalarmierung
     */
    @ConfigParam(mandatory = false, defaultValue = "5")
    public static final String KEY_ALARM_SUPRESS_TIME = "ALARM_SUPRESS_TIME";

    /**
     * Key für die Konfigurationseinstellung der Wartezeit zwischen zwei
     * Email-Abholvorgängen
     */
    @ConfigParam(mandatory = false, defaultValue = "30")
    public static final String KEY_POLLING_SECONDS = "POLLING_SECONDS";
    /**
     * Key für die Konfigurationseinstellung ob bei jedem Alarm eine spezielle
     * RIC forciert werden soll
     */
    @ConfigParam(mandatory = false, defaultValue = "")
    public static final String KEY_FORCE_RIC = "FORCE_RIC";

    /**
     * Key für die Konfigurationseinstellung welches Zeichen beim auftrennen von
     * Zeilen als Trennzeichen verwendet werden soll
     */
    @ConfigParam(mandatory = false, defaultValue = "|")
    public static final String KEY_LINE_SEPARATOR = "LINE_SEPARATOR";

    /**
     * Logger für diese Klasse
     */
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    /**
     * Don't instanciate this class
     */
    private Config() {

    }

    /**
     * Initialisiert die Konfiguration. Liest die Konfiguration aus der
     * übergebenen Datei aus.
     *
     * @param properties
     * @throws
     * de.ffwbeetzsommerfeld.bosmon.mailreader.config.ConfigurationException
     */
    public static void init(File properties) throws ConfigurationException {
        try {
            bosMonMailReaderProps.load(new FileReader(properties));
            validate(bosMonMailReaderProps);
            printConfiguration();

        } catch (IOException ex) {
            LOG.error("", ex);
            System.exit(1);
        }
    }

    /**
     * Holt den konfigurierten Wert für übergebenen Key aus der Konfiguration
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        return bosMonMailReaderProps.getProperty(key);
    }

    /**
     * Validiert die Konfiguration
     *
     * @param properties
     * @throws ConfigurationException
     */
    private static void validate(Properties properties) throws ConfigurationException {
        for (Field field : Config.class.getDeclaredFields()) {
            for (Annotation a : field.getDeclaredAnnotations()) {
                if (a instanceof ConfigParam) {
                    try {
                        /* We got a config param */
                        String value = (String) field.get(null);
                        ConfigParam annotation = (ConfigParam) a;
                        if (annotation.mandatory() && (annotation.defaultValue() == null || annotation.defaultValue().isEmpty())) {
                            String propertyValue = (String) properties.get(value);
                            if (propertyValue == null || propertyValue.isEmpty()) {
                                throw new ConfigurationException("Der Konfigurationsparameter " + value + " muss gesetzt werden");
                            }
                        }
                        if (!annotation.mandatory() && (annotation.defaultValue() != null || !annotation.defaultValue().isEmpty())) {
                            String propertyValue = (String) properties.get(value);
                            if (propertyValue == null || propertyValue.isEmpty()) {
                                properties.setProperty(value, annotation.defaultValue());
                            }
                        }
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        LOG.error("", ex);
                    }
                }
            }
        }
    }

    /**
     * Schreibt die Konfiguration in's Log
     */
    private static void printConfiguration() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("Konfiguration:").append("\n");
        builder.append("=======================").append("\n");
        for (Object object : bosMonMailReaderProps.keySet()) {
            String key = (String) object;
            builder.append(pad(key + ": ", 30, " ")).append(bosMonMailReaderProps.get(key)).append("\n");
            builder.append(pad("-", 60, "-")).append("\n");
        }
        builder.append("\n");
        LOG.info(builder.toString());
    }

    public static String pad(String str, int size, String padChar) {
        StringBuffer padded = new StringBuffer(str);
        while (padded.length() < size) {
            padded.append(padChar);
        }
        return padded.toString();
    }
}
