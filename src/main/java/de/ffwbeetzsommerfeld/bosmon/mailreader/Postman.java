package de.ffwbeetzsommerfeld.bosmon.mailreader;

import com.sun.mail.imap.IMAPFolder;
import de.ffwbeetzsommerfeld.bosmon.mailreader.util.Recipient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.ffwbeetzsommerfeld.bosmon.mailreader.util.AlarmHeadquarter;

/**
 * Diese Klasse bietet die grundsätzliche Funktionalität um Email abzuholen und
 * auszuwerten.
 *
 * @author jhomuth
 */
public class Postman implements AlarmHeadquarter {

    private static final Logger LOG = Logger.getLogger(Postman.class.getSimpleName());

    private List<Recipient> recipients = new ArrayList<>();

    /**
     * Diese Methode holt die Alarm-Emails ab.
     *
     * @return Eine Liste von Alarmen die via Email empfangen wurden. Diese
     * Liste kann auch leer sein, aber niemals null
     * @throws MessagingException im Fall das die Emails nicht abgeholt werden
     * konnten.
     */
    public void fetchMailsAndQuitConnection() throws MessagingException {
        List<Alarm> alarmMails = new ArrayList<>();

        /* Hole Authentifiezierungsdatens */
        String url = Config.get(Config.KEY_PROP_IMAP_SERVER);
        String username = Config.get(Config.KEY_PROP_IMAP_USER);
        String password = Config.get(Config.KEY_PROP_IMAP_PASS);
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.debug", Config.get(Config.KEY_MAIL_TRANSFER_DEBUG));
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        try {
            LOG.fine("Connecting to IMAP server: " + url);
            store.connect(url, username, password);

            String folderName = "INBOX";
            IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);
            try {
                if (!folder.isOpen()) {
                    /* Wir löschen die Mails sofort nach dem abholen, also brauchen wir ReadWrite */
                    folder.open(Folder.READ_WRITE);
                }

                long largestUid = folder.getUIDNext() - 1;
                int chunkSize = 500;
                for (long offset = 0; offset < largestUid; offset += chunkSize) {
                    long start = Math.max(1, largestUid - offset - chunkSize + 1);
                    long end = Math.max(1, largestUid - offset);

                    Message[] messages = folder.getMessagesByUID(start, end);
                    FetchProfile metadataProfile = new FetchProfile();
                    metadataProfile.add(FetchProfile.Item.FLAGS);
                    // Lade alle wichtigen Felder
                    metadataProfile.add(FetchProfile.Item.ENVELOPE);
                    folder.fetch(messages, metadataProfile);
                    for (int i = messages.length - 1; i >= 0; i--) {
                        Message message = messages[i];
                        boolean isRead = message.isSet(Flags.Flag.SEEN);

                        if (!isRead) {
                            LOG.log(Level.INFO, "Nachrichtentyp: {0}", message.getDataHandler().getContentType());
                            /* Die Nachricht wurde noch nicht gelesen, scheint also neu zu sein */
                            try {
                                Alarm alarmMail = new Alarm();
                                for (Address address : message.getFrom()) {
                                    alarmMail.setFromAddress(address.toString());
                                }
                                if (message.getContent() instanceof Multipart) {
                                    Multipart mP = (Multipart) message.getContent();
                                    alarmMail.setMessage(handleMultipart(mP));
                                } else {
                                    alarmMail.setMessage((String) message.getContent());
                                }

                                alarmMail.setRic((String) (message.getSubject().length() > 7 ? message.getSubject().substring(0, 7) : message.getSubject()));
                                alarmMail.setAlarmTime(message.getSentDate());
                                alarmMails.add(alarmMail);
                            } catch (IOException ex) {
                                Logger.getLogger(Postman.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        /* Markiere die Email als gelesen und lösche sie sofort danach */
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
        this.deliverAlarms(alarmMails);
    }

    public static String handleMultipart(Multipart multipart) throws MessagingException, IOException {

        for (int i = 0, n = multipart.getCount(); i < n; i++) {
            Part p = (multipart.getBodyPart(i));
            if(p.getContentType().toLowerCase().substring(0, 10).equals("text/plain")){
                return (String) p.getContent();
            }
        }
        return "Kein Alarmtext";
    }

    @Override
    public void deliverAlarms(List<Alarm> alarms) {
        if (alarms != null && !alarms.isEmpty()) {
            for (Recipient recipient : recipients) {
                recipient.deliver(alarms);
            }
        } else {
            LOG.fine("Keine neuen Alarme empfangen...");
        }
    }

    /**
     * Registriert einen neuen Empfänger für eingehende Alarme
     *
     * @param recipient
     */
    @Override
    public void registerRecipient(Recipient recipient) {
        recipients.add(recipient);
    }

    /**
     * Enfernt einen Empfänger aus der Liste der Empfänger für eingehende Alarme
     *
     * @param recipient
     */
    @Override
    public void deRegisterRecipient(Recipient recipient) {
        recipients.remove(recipient);
    }

}
