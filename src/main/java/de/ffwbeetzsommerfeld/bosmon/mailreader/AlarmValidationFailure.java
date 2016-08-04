/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ffwbeetzsommerfeld.bosmon.mailreader;

/**
 *
 * @author jhomuth
 */
public enum AlarmValidationFailure {
    WRONG_SENDER_ADDRESS {
        @Override
        public String getValidationMessage() {
            return "Alarm ist vom falschen Absender";
        }
    },
    ALARM_ALREADY_FIRED {
        @Override
        public String getValidationMessage() {
             return "Alarm ist bereits verarbeitet";
        }
    },
    ALARM_TO_OLD {
        @Override
        public String getValidationMessage() {
            return "Alarm ist zu alt";
        }
    };
    
    public abstract String getValidationMessage();
    
}
