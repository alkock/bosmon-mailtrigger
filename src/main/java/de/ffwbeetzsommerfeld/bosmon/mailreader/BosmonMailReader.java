/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ffwbeetzsommerfeld.bosmon.mailreader;

import com.sun.mail.imap.IMAPFolder;
import java.io.IOException;

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

    public static void main(String[] args) {
        String server = "imap.1blu.de";
        String username = "o37889_0-alarm-kremmen";
        String password = "1a2b3c4d";

        try {
            new BosmonMailReader().process(server, username, password);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Fehler beim Emails laden", e);
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

            Folder root = store.getDefaultFolder();
            Folder[] folders = root.list();
            System.out.println("Select a folder");
            for (int i = 0; i < folders.length; i++) {
                System.out.println("\t" + folders[i].getName());
            }

            String folderName = "INBOX";
            IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);

            long afterFolderSelectionTime = System.nanoTime();
            int totalNumberOfMessages = 0;
            try {
                if (!folder.isOpen()) {
                    folder.open(Folder.READ_WRITE);
                }

                /*
				 * Now we fetch the message from the IMAP folder in descending order.
				 *
				 * This way the new mails arrive with the first chunks and older mails
				 * afterwards.
                 */
                long largestUid = folder.getUIDNext() - 1;
                int chunkSize = 500;
                for (long offset = 0; offset < largestUid; offset += chunkSize) {
                    long start = Math.max(1, largestUid - offset - chunkSize + 1);
                    long end = Math.max(1, largestUid - offset);

                    /*
					 * The next line fetches the existing messages within the
					 * given range from the server.
					 *
					 * The messages are not loaded entirely and contain hardly
					 * any information. The Message-instances are mostly empty.
                     */
                    long beforeTime = System.nanoTime();
                    Message[] messages = folder.getMessagesByUID(start, end);
                    totalNumberOfMessages += messages.length;
                    System.out.println("found " + messages.length + " messages (took " + (System.nanoTime() - beforeTime) / 1000 / 1000 + " ms)");

                    /*
					 * If we would access e.g. the subject of a message right away
					 * it would be fetched from the IMAP server lazily.
					 *
					 * Fetching the subjects of all messages one by one would
					 * produce many requests to the IMAP server and take too
					 * much time.
					 *
					 * Instead with the following lines we load some information
					 * for all messages with one single request to save some
					 * time here.
                     */
                    beforeTime = System.nanoTime();
                    // this instance could be created outside the loop as well
                    FetchProfile metadataProfile = new FetchProfile();
                    // load flags, such as SEEN (read), ANSWERED, DELETED, ...
                    metadataProfile.add(FetchProfile.Item.FLAGS);
                    // also load From, To, Cc, Bcc, ReplyTo, Subject and Date
                    metadataProfile.add(FetchProfile.Item.ENVELOPE);
                    // we could as well load the entire messages (headers and body, including all "attachments")
                    // metadataProfile.add(IMAPFolder.FetchProfileItem.MESSAGE);
                    folder.fetch(messages, metadataProfile);
                    System.out.println("loaded messages (took " + (System.nanoTime() - beforeTime) / 1000 / 1000 + " ms)");

                  
                    beforeTime = System.nanoTime();
                    for (int i = messages.length - 1; i >= 0; i--) {
                        Message message = messages[i];
                        long uid;
                        uid = folder.getUID(message);
                        boolean isRead = message.isSet(Flags.Flag.SEEN);

                        if (!isRead) {
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
                    System.out.println("Listed message (took " + (System.nanoTime() - beforeTime) / 1000 / 1000 + " ms)");
                }
            } finally {
                if (folder.isOpen()) {
                    folder.close(true);
                }
            }

            System.out.println("\nListed all " + totalNumberOfMessages + " messages (took " + (System.nanoTime() - afterFolderSelectionTime) / 1000 / 1000 + " ms)");
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
