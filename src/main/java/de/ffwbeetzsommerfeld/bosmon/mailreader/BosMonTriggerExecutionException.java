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
public class BosMonTriggerExecutionException extends Exception {

    /**
     * Creates a new instance of <code>BosMonTriggerExecutionException</code>
     * without detail message.
     */
    public BosMonTriggerExecutionException() {
    }

    /**
     * Constructs an instance of <code>BosMonTriggerExecutionException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public BosMonTriggerExecutionException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>BosMonTriggerExecutionException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     * @param ex
     */
    public BosMonTriggerExecutionException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
