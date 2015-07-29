package pl.comarch.soc.socmessenger;


import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class OnlineUsersCallback implements MqttCallback {
    MessagesHandler messagesHandler;

    public OnlineUsersCallback() {
        super();
        this.messagesHandler = MessagesHandler.getInstance();
    }

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

        messagesHandler.handle(topic, message);

//        if(topic.equals(Topics.ONLINE_USERS)) {
////            handleOnlineUsers(message);
//            //TODO call updateOnlineUsers() from MainActivity
//        } else if(topic.equals(Topics.Regex.USER_STATUS)) {
//            //TODO get username and call updateOnlineUsers() from MainActivity
//        } else if(topic.equals(Topics.Regex.MESSAGE)) {
//            //TODO get publisher's and receiver's names and call updateMessage() from MessageActivity


    }

}

