/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ffwbeetzsommerfeld.bosmon.mailreader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhomuth
 */
public class Config {

    private static Properties bosMonMailReaderProps = new Properties();

    public static final String PROP_IMAP_SERVER = "IMAP_SERVER";
    public static final String PROP_IMAP_USER = "IMAP_USER";
    public static final String PROP_IMAP_PASS = "IMAP_PASS";
    public static final String BOSMON_USER = "BOSMON_USER";
    public static final String BOSMON_PASS = "BOSMON_PASS";
    public static final String BOSMON_CHANNEL_NAME = "BOSMON_CHANNEL_NAME";
    public static final String BOSMON_SERVER_NAME = "BOSMON_SERVER_NAME";
    public static final String BOSMON_SERVER_PORT = "BOSMON_SERVER_PORT";
    public static final String ALLOWED_SENDER = "ALLOWED_SENDER";

    private Config() {

    }

    public static void init(File properties) {
        try {
            bosMonMailReaderProps.load(new FileReader(properties));
        } catch (IOException ex) {
            Logger.getLogger(Postman.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    public static String get(String key) {
        return bosMonMailReaderProps.getProperty(key);
    }
}
