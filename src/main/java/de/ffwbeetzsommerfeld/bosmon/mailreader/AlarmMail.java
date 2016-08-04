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
public class AlarmMail {

    private String subject;

    private String content;
    
        private String fromAddress;

    /**
     * Get the value of fromAddress
     *
     * @return the value of fromAddress
     */
    public String getFromAddress() {
        return fromAddress;
    }

    /**
     * Set the value of fromAddress
     *
     * @param fromAddress new value of fromAddress
     */
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }


    /**
     * Get the value of content
     *
     * @return the value of content
     */
    public String getContent() {
        return content;
    }

    /**
     * Set the value of content
     *
     * @param content new value of content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the value of subject
     *
     * @return the value of subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set the value of subject
     *
     * @param subject new value of subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

}
