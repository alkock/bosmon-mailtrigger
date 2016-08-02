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
    private static Config instance;
    private static Properties bosMonMailReaderProps = new Properties();
    
    private Config(){
        
    }
    
    
    
    public static void init(File properties){
        try {
            bosMonMailReaderProps.load(new FileReader(properties));
        } catch (IOException ex) {
            Logger.getLogger(BosmonMailReader.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }
    
    public static String get(String key){
        return bosMonMailReaderProps.getProperty(key);
    }
}
