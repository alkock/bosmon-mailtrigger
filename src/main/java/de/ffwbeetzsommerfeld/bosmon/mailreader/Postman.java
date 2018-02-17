package de.ffwbeetzsommerfeld.bosmon.mailreader;

import de.ffwbeetzsommerfeld.bosmon.mailreader.config.Config;
import com.sun.mail.imap.IMAPFolder;
import de.ffwbeetzsommerfeld.bosmon.mailreader.util.Recipient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.*;
import java.util.Properties;
import de.ffwbeetzsommerfeld.bosmon.mailreader.util.AlarmHeadquarter;
import javax.mail.search.FlagTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diese Klasse bietet die grundsätzliche Funktionalität um Email abzuholen und
 * auszuwerten.
 *
 * @author jhomuth
 */
public class Postman implements AlarmHeadquarter {

    private static final Logger LOG = LoggerFactory.getLogger(Postman.class);

    private List<Recipient> recipients = new ArrayList<>();

    /**
     * Diese Methode holt die Alarm-Emails ab und stellt Sie den Abonennten zu
     * (Liste recipients)
     *
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
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        try {
            LOG.trace("Connecting to IMAP server: {}", url);
            store.connect(url, username, password);

            String folderName = "INBOX";
            IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);
            try {
                if (!folder.isOpen()) {
                    /* Wir setzen die Mails auf gelesen also brauchen wir ReadWrite */
                    folder.open(Folder.READ_WRITE);
                }

                Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                FetchProfile metadataProfile = new FetchProfile();
                metadataProfile.add(FetchProfile.Item.FLAGS);

                // Lade alle wichtigen Felder
                metadataProfile.add(FetchProfile.Item.ENVELOPE);
                folder.fetch(messages, metadataProfile);
                for (int i = messages.length - 1; i >= 0; i--) {
                    Message message = messages[i];
                    boolean isRead = message.isSet(Flags.Flag.SEEN);

                    if (!isRead) {
                        LOG.info("Nachrichtentyp: {}", message.getDataHandler().getContentType());
                        /* Die Nachricht wurde noch nicht gelesen, scheint also neu zu sein */
                        String fromMailAddress = message.getFrom()[0].toString();
                        Boolean foundValidFrom = Boolean.TRUE;
                        if (Boolean.valueOf(Config.get(Config.KEY_SENDER_ADDRESS_VALIDATION))) {
                            foundValidFrom = Boolean.FALSE;
                            String allowedSender = Config.get(Config.KEY_ALLOWED_SENDER);
                            String[] split = allowedSender.split(",");
                            for (String string : split) {
                                LOG.info("Checking email "+string);
                                if (fromMailAddress.contains(string)) {
                                    foundValidFrom = Boolean.TRUE;
                                }
                            }
                        }
                        if (foundValidFrom) {
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
                               LOG.error("",ex);
                            }

                        } else {
                            LOG.warn("Illegal sender detected ({})", fromMailAddress);
                            message.setFlag(Flags.Flag.FLAGGED, Boolean.TRUE);
                        }
                        message.setFlag(Flags.Flag.SEEN, Boolean.TRUE);
                        /* Markiere die Email als gelesen */
                    }
//                        message.setFlag(Flags.Flag.DELETED, Boolean.TRUE);
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

    /**
     * Versucht aus der Multipart Mail den "normalen" Textteil zu finden. Wird
     * der erste Typ text/plain gefunden wird dieser zurück gegeben. Wenn kein
     * Text/Plain gefunden wurde, wird "Kein Alarmtext" returned.
     *
     * @param multipart
     * @return
     * @throws MessagingException
     * @throws IOException
     */
    public static String handleMultipart(Multipart multipart) throws MessagingException, IOException {

        for (int i = 0, n = multipart.getCount(); i < n; i++) {
            Part p = (multipart.getBodyPart(i));
            if (p.getContentType().toLowerCase().substring(0, 10).equals("text/plain")) {
                return (String) p.getContent();
            }
        }
        return "Kein Alarmtext";
    }

    /**
     * @see AlarmHeadquarter#deliverAlarms(java.util.List)
     */
    @Override
    public void deliverAlarms(List<Alarm> alarms) {
        if (alarms != null && !alarms.isEmpty()) {
            for (Recipient recipient : recipients) {
                recipient.deliver(alarms);
            }
        } else {
            LOG.trace("Keine neuen Alarme empfangen...");
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
