package pl.comarch.soc.socmessenger.singletons;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import pl.comarch.soc.socmessenger.Configuration;
import pl.comarch.soc.socmessenger.OnlineUsersCallback;
import pl.comarch.soc.socmessenger.Topics;


public class MqttConnectionHandler extends MqttClient {

    private static MqttConnectionHandler instance;
    private static MqttConnectOptions options;


    private MqttConnectionHandler() throws MqttException {
        super(Configuration.SERVER_URI, Configuration.USERNAME, null);
    }

    public synchronized static MqttConnectionHandler getInstance() {
        if(instance == null) {
            try {
                instance = new MqttConnectionHandler();

                options = new MqttConnectOptions();
                options.setWill(Topics.USER_STATUS_TOPIC, Topics.Content.OFFLINE_STATUS, Configuration.DEFAULT_QOS, false);
                options.setKeepAliveInterval(Configuration.DEFAULT_KEEPALIVE);
            } catch (MqttException e) {
                e.printStackTrace();
                instance = null;
            }
        }
        return instance;
    }

    @Override
    public void connect() throws MqttException {
        if(!this.isConnected()) {
            super.connect(options);
            instance.setCallback(new OnlineUsersCallback());
            instance.onValidConnection();
        }
    }

    private void onValidConnection() {
        try {
            instance.subscribe(Topics.ONLINE_USERS);
            instance.subscribe(Topics.Wildcard.USER_STATUS);
            instance.publish(Topics.USER_STATUS_TOPIC, new MqttMessage(Topics.Content.ONLINE_STATUS));
            instance.publish(Topics.ONLINE_USERS, new MqttMessage("get".getBytes()));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
