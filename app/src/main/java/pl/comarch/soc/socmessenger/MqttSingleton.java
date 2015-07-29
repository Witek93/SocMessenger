package pl.comarch.soc.socmessenger;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;


public class MqttSingleton extends MqttClient {

    private static MqttSingleton instance;
    private static MqttConnectOptions options;


    private MqttSingleton() throws MqttException {
        super(Configuration.SERVER_URI, Configuration.USERNAME, null);
    }

    synchronized static MqttSingleton getInstance() {
        if(instance == null) {
            try {
                instance = new MqttSingleton();

                options = new MqttConnectOptions();
                options.setWill(Topics.USER_STATUS_TOPIC, Topics.Content.OFFLINE_STATUS, Configuration.DEFAULT_QOS, false);
                options.setKeepAliveInterval(Configuration.DEFAULT_KEEPALIVE);

                instance.connect();
            } catch (MqttException e) {
                e.printStackTrace();
                instance = null;
            }
        }
        return instance;
    }

    @Override
    public void connect() throws MqttException {
        super.connect(options);
        instance.setCallback(new OnlineUsersCallback());
    }
}
