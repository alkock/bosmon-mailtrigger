/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ffwbeetzsommerfeld.bosmon.mailreader;

import com.sun.mail.imap.IMAPFolder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhomuth
 */
public class Postman {

    private static Logger logger = Logger.getLogger(Postman.class.getSimpleName());

    

    public List<Alarm> fetchMails() throws MessagingException {
        List<Alarm> alarmMails = new ArrayList<>();
        String url = Config.get(Config.PROP_IMAP_SERVER);
        String username = Config.get(Config.PROP_IMAP_USER);
        String password = Config.get(Config.PROP_IMAP_PASS);
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        try {
            System.out.println("Connecting to IMAP server: " + url);
            store.connect(url, username, password);

            String folderName = "INBOX";
            IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);

            int totalNumberOfMessages = 0;
            try {
                if (!folder.isOpen()) {
                    folder.open(Folder.READ_WRITE);
                }

                long largestUid = folder.getUIDNext() - 1;
                int chunkSize = 500;
                for (long offset = 0; offset < largestUid; offset += chunkSize) {
                    long start = Math.max(1, largestUid - offset - chunkSize + 1);
                    long end = Math.max(1, largestUid - offset);

                    Message[] messages = folder.getMessagesByUID(start, end);
                    totalNumberOfMessages += messages.length;

                    // this instance could be created outside the loop as well
                    FetchProfile metadataProfile = new FetchProfile();
                    // load flags, such as SEEN (read), ANSWERED, DELETED, ...
                    metadataProfile.add(FetchProfile.Item.FLAGS);
                    // also load From, To, Cc, Bcc, ReplyTo, Subject and Date
                    metadataProfile.add(FetchProfile.Item.ENVELOPE);
                    // we could as well load the entire messages (headers and body, including all "attachments")
                    // metadataProfile.add(IMAPFolder.FetchProfileItem.MESSAGE);
                    folder.fetch(messages, metadataProfile);

                    for (int i = messages.length - 1; i >= 0; i--) {
                        Message message = messages[i];
                        boolean isRead = message.isSet(Flags.Flag.SEEN);

                        if (!isRead) {
                            try {
                                Alarm alarmMail = new Alarm();
                                for (Address address : message.getFrom()) {
                                    alarmMail.setFromAddress(address.toString());
                                }
                                alarmMail.setMessage((String) message.getContent());
                                alarmMail.setRic((String) message.getSubject());
                                alarmMail.setAlarmTime(message.getSentDate());
                                alarmMails.add(alarmMail);
                                
                                System.out.println(alarmMail.getMessage());
                            } catch (IOException ex) {
                                Logger.getLogger(Postman.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }                        
                        message.setFlag(Flags.Flag.SEEN, Boolean.TRUE);
                        message.setFlag(Flags.Flag.DELETED, Boolean.TRUE);
                    }
                }
            } finally {
                if (folder.isOpen()) {
                    folder.close(true);
                }
            }

        } finally {
            store.close();
        }
        return alarmMails;
    }
   
}
