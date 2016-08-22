package de.ffwbeetzsommerfeld.bosmon.mailreader.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation für die Vorkonfiguration/Beschreibung von Konfigurationsparametern
 * @author jhomuth
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigParam {
    
    /**
     * Ist dieser Konfigurationsparameter zwingend anzugeben (true/false)
     * @return 
     */
    boolean mandatory() default false;
    
    /**
     * Defaultwert eines Konfigurationsparameters sofern dieser nicht gesetzt wurde
     * @return 
     */
    String defaultValue() default "";
    
}
