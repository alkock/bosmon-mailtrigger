/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ffwbeetzsommerfeld.bosmon.mailreader.execute;

import de.ffwbeetzsommerfeld.bosmon.mailreader.Alarm;
import de.ffwbeetzsommerfeld.bosmon.mailreader.BosMonExecutor;
import de.ffwbeetzsommerfeld.bosmon.mailreader.BosMonTriggerExecutionException;
import de.ffwbeetzsommerfeld.bosmon.mailreader.Config;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author jhomuth
 */
public class HttpClientExecuter implements Executor {

    @Override
    public void execute(Alarm alarm) throws BosMonTriggerExecutionException {
        CloseableHttpResponse bosMonResponse = null;
        try {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(Config.get(Config.KEY_BOSMON_USER), Config.get(Config.KEY_BOSMON_PASS));
            provider.setCredentials(AuthScope.ANY, credentials);

            CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();
            HttpPost httpPost = new HttpPost(String.format("http://%s:%s/telegramin/%s/input.xml", Config.get(Config.KEY_BOSMON_SERVER_NAME), Config.get(Config.KEY_BOSMON_SERVER_PORT), Config.get(Config.KEY_BOSMON_CHANNEL_NAME)));
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("type", "pocsag"));
            nvps.add(new BasicNameValuePair("address", alarm.getRic()));
            nvps.add(new BasicNameValuePair("flags", "0"));
            nvps.add(new BasicNameValuePair("function", "b"));
            nvps.add(new BasicNameValuePair("message", alarm.getMessage()));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            bosMonResponse = httpclient.execute(httpPost);

            HttpEntity entity = bosMonResponse.getEntity();
            EntityUtils.consume(entity);
            
        } catch (IOException ex) {
            throw new BosMonTriggerExecutionException("Unable to emit HTTP Request", ex);
            
        } finally {
            try {
                if (bosMonResponse != null) {
                    bosMonResponse.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(BosMonExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
