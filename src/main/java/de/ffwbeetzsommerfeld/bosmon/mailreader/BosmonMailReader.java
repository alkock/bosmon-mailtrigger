/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ffwbeetzsommerfeld.bosmon.mailreader;

import com.sun.mail.imap.IMAPFolder;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

import javax.mail.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhomuth
 */
public class BosmonMailReader {

    private static Logger logger = Logger.getLogger(BosmonMailReader.class.getName());

    private static final String PROP_IMAP_SERVER = "IMAP_SERVER";
    private static final String PROP_IMAP_USER = "IMAP_USER";
    private static final String PROP_IMAP_PASS = "IMAP_PASS";

    public static void main(String[] args) {
        Config.init(new File(args[0]));

        String server = Config.get(PROP_IMAP_SERVER);
        String username = Config.get(PROP_IMAP_USER);
        String password = Config.get(PROP_IMAP_PASS);
        while (true) {
            try {
                new BosmonMailReader().process(server, username, password);
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Fehler beim Emails laden", e);
            }
            logger.log(Level.INFO, "Emails checked, waiting 20 Seconds");
            try {
                Thread.sleep(20000);
            } catch (InterruptedException ex) {
                Logger.getLogger(BosmonMailReader.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
        }
    }

    private void process(String url, String username, String password) throws MessagingException {
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
                            for (Address message1 : message.getFrom()) {
                                
                                
                            }
                            try {
                                this.callBosMon((String) message.getContent());
                            } catch (BosMonTriggerExecutionException | IOException ex) {
                                Logger.getLogger(BosmonMailReader.class.getName()).log(Level.SEVERE, "BosMon konnte nicht getriggert werden", ex);
                            }
                        }
                        try {
                            System.out.println(message.getContent());
                        } catch (IOException ex) {
                            Logger.getLogger(BosmonMailReader.class.getName()).log(Level.SEVERE, null, ex);
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
    }

    private void callBosMon(String content) throws BosMonTriggerExecutionException {
        try {
            Runtime.getRuntime().exec("cmd /c start bosmon-trigger.bat");
        } catch (IOException ex) {
            throw new BosMonTriggerExecutionException("Unable to execute BosMon Trigger script", ex);
        }
//        curl --basic -u "Benutzername:Passwort" http://lokale IP:Port/telegramin/in1/input.xml --data "type=pocsag&address=1234567&flags=0&function=a&message=Hallo" -vk
    }

    public boolean messageAlreadySent() {
        return false;
    }

}
