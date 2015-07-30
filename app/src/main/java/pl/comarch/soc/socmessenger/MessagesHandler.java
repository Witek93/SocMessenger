package pl.comarch.soc.socmessenger;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.comarch.soc.socmessenger.constants.Topics;


public class MessagesHandler implements MqttCallback {

    private Map<String, CallableOnMessage> instances;
    private static MessagesHandler instance;


    private MessagesHandler() {
        instances = new HashMap<>();
    }


    synchronized static public MessagesHandler getInstance() {
        if(instance == null) {
            instance = new MessagesHandler();
        }
        return instance;
    }


    public void register(String name, CallableOnMessage instance) {
        instances.put(name, instance);
    }


    public void handle(String topic, String message) {
        Log.i("MessagesHandler", topic + ": " + message);
        if(isPlainTopic(topic)) {
            instances.get(topic).call(topic, message);
        } else {
            handleWildcardTopics(topic, message);
        }
    }

    private boolean isPlainTopic(String topic) {
        return instances.containsKey(topic);
    }


    private void handleWildcardTopics(String topic, String message) {
        if(isUserStatusWildcard(topic, message)) {
            instances.get(Topics.Wildcard.USER_STATUS).call(topic, message);
        }
    }


    private boolean isUserStatusWildcard(String topic, String message) {
        Pattern pattern = Pattern.compile(Topics.Regex.USER_STATUS);
        Matcher matcher = pattern.matcher(topic);
        return matcher.matches();
    }


    @Override
    public void connectionLost(Throwable throwable) {
        Log.i("MQTT", "Connection lost in callback");
        //TODO: reconnect on lost connection
        throwable.printStackTrace();
    }


    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = mqttMessage.toString();
        Log.i("MQTT", "message arrived: " + topic + ": " + mqttMessage.toString());
        getInstance().handle(topic, message);
    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        Log.i("MQTT", "Delivery complete in callback");
    }
}
