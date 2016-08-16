/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ffwbeetzsommerfeld.bosmon.mailreader.util;

import de.ffwbeetzsommerfeld.bosmon.mailreader.Alarm;
import de.ffwbeetzsommerfeld.bosmon.mailreader.Config;

/**
 *
 * @author jhomuth
 */
public enum Executor {

    CURL {
        @Override
        public String[] getCommand(Alarm alarm) {
            String[] curlCommand = {Config.get(Config.KEY_CURL_EXE),
                "--basic",
                String.format("-u %s:%s", Config.get(Config.KEY_BOSMON_USER),Config.get(Config.KEY_BOSMON_PASS)),
                String.format("%s:%s/telegramin/%s/input.xml", Config.get(Config.KEY_BOSMON_SERVER_NAME), Config.get(Config.KEY_BOSMON_SERVER_PORT), Config.get(Config.KEY_BOSMON_CHANNEL_NAME)),
                String.format("--data \"type=pocsag&address=%s&flags=0&function=b&message=%s\"", alarm.getRic(), alarm.getMessage()),
                "-vk"};
            return curlCommand;
        }
    },
    BOSMON_DIAL {
        @Override
        public String[] getCommand(Alarm alarm) {
            String[] bosMonDialCommand = {Config.get(Config.KEY_BOSMON_DIAL_EXE),
                "-username " + Config.get(Config.KEY_BOSMON_USER),
                "-password " + Config.get(Config.KEY_BOSMON_PASS),
                "-alertaddress " + alarm.getRic(),
                "-alertmessage " + alarm.getMessage(),
                "-close"};
            return bosMonDialCommand;
        }
    };

    public abstract String[] getCommand(Alarm alarm);

}
