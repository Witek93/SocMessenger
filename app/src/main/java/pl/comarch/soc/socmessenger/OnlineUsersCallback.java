package pl.comarch.soc.socmessenger;


import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class OnlineUsersCallback implements MqttCallback {

    @Override
    public void connectionLost(Throwable throwable) {
        Log.i("MQTT", "Connection lost in callback");
        throwable.printStackTrace();
    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        Log.i("MQTT", "Delivery complete in callback");
    }


    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = mqttMessage.toString();
        Log.i("MQTT", "message arrived: " + topic + ": " + mqttMessage.toString());
        MessagesHandler.getInstance().handle(topic, message);
    }

}

