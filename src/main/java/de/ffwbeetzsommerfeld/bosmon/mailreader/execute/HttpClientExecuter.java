package de.ffwbeetzsommerfeld.bosmon.mailreader.execute;

import de.ffwbeetzsommerfeld.bosmon.mailreader.Alarm;
import de.ffwbeetzsommerfeld.bosmon.mailreader.BosMonExecutor;
import de.ffwbeetzsommerfeld.bosmon.mailreader.BosMonTriggerExecutionException;
import de.ffwbeetzsommerfeld.bosmon.mailreader.config.Config;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * Klasse ist verantwortlich um einen Alarm an BosMon via Http weiterzugeben
 *
 * @author jhomuth
 */
public class HttpClientExecuter implements Executor {
    
    /**
     * Logger für diese Klasse
     */
    private static final Logger LOG = Logger.getLogger(HttpClientExecuter.class.getSimpleName());

    /**
     * @see Executor#execute(de.ffwbeetzsommerfeld.bosmon.mailreader.Alarm)
     */
    @Override
    public void execute(Alarm alarm) throws BosMonTriggerExecutionException {
        CloseableHttpResponse bosMonResponse = null;
        CloseableHttpClient httpclient = null;
        try {
            String bosMonUsername = Config.get(Config.KEY_BOSMON_USER);
            String bosMonPassword = Config.get(Config.KEY_BOSMON_PASS);
            if (bosMonUsername != null && bosMonPassword != null && !bosMonUsername.isEmpty() && !bosMonPassword.isEmpty()) {
                CredentialsProvider provider = new BasicCredentialsProvider();
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(bosMonUsername, bosMonPassword);
                provider.setCredentials(AuthScope.ANY, credentials);
                httpclient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();
                LOG.log(Level.INFO, "Setze Benutzername {0} und Passwort **ausgeblendet** für BosMon Request.", bosMonUsername);
            }else{
                LOG.log(Level.INFO,"Kein Benutzername und/oder Passwort gesetzt. Authentifizierung nicht unterstützt.");
                httpclient = HttpClients.custom().build();
            }

            HttpPost httpPost = new HttpPost(String.format("http://%s:%s/telegramin/%s/input.xml", Config.get(Config.KEY_BOSMON_SERVER_NAME), Config.get(Config.KEY_BOSMON_SERVER_PORT), Config.get(Config.KEY_BOSMON_CHANNEL_NAME)));
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("type", "pocsag"));
            nvps.add(new BasicNameValuePair("address", alarm.getRic()));
            nvps.add(new BasicNameValuePair("flags", "0"));
            nvps.add(new BasicNameValuePair("function", "b"));
            nvps.add(new BasicNameValuePair("message", alarm.getMessage().replace("\n", Config.get(Config.KEY_LINE_SEPARATOR)).replace("\r", Config.get(Config.KEY_LINE_SEPARATOR))));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            bosMonResponse = httpclient.execute(httpPost);
            HttpEntity entity = bosMonResponse.getEntity();
            EntityUtils.consume(entity);
            LOG.log(Level.INFO, "Statuscode des Requests: {0}", bosMonResponse.getStatusLine().getStatusCode());
        } catch (IOException ex) {
            throw new BosMonTriggerExecutionException("Konnte den Http-Request zu BosMon nicht absetzten", ex);
        } finally {
            try {
                if (bosMonResponse != null) {
                    bosMonResponse.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(BosMonExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                if (httpclient != null) {
                    httpclient.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(HttpClientExecuter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
